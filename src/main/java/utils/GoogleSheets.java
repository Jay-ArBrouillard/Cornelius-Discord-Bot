package utils;

import chess.ChessGameState;
import chess.tables.ChessPlayer;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import java.io.*;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static chess.ChessConstants.DRAW;

public class GoogleSheets {
    private static String APPLICATION_NAME = "Chess Records";
    private static String SPREAD_SHEET_ID = System.getenv("SPREAD_SHEET_ID");
    private static HttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Sheets service;
    private static DecimalFormat formatPercent = new DecimalFormat("##0.00");
    private static final String RANKED_TAB = "ranked";
    private static final String MATCHES_TAB = "matches";
    private static int rowNumber;
    private static int totalRows;
    private static final String MM_DD_YYYY_HH_MM_SS_A = "MM-dd-yyyy hh:mm:ss a";
    private static final String MMM_DD_YYYY_HH_MM_A = "MMM dd yyyy hh:mm a";
    private static final String MMM_DD_YYYY = "MMM dd yyyy";

    public GoogleSheets() {
        getSheetsService();
    }

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream targetStream = new ByteArrayInputStream(System.getenv("GOOGLE_CREDENTIALS").getBytes());
        //InputStream targetStream = GoogleSheets.class.getResourceAsStream("/credentials.json"); //For local testing
        Credential credential = GoogleCredential.fromStream(targetStream, HTTP_TRANSPORT, JSON_FACTORY).createScoped(SCOPES);
        return credential;
    }

    /**
     * Build and set an authorized Sheets API client service.
     *
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    public static void getSheetsService() {
        try {
            Credential credential = authorize();
            service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static synchronized List<List<Object>> getAllUsers() {
        try {
            if (service == null) getSheetsService();
            ValueRange response = service.spreadsheets().values().get(SPREAD_SHEET_ID, RANKED_TAB).execute();
            totalRows = response.getValues().size();
            return response.getValues();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Add new user if they don't exist and returns their elo value
     * @param id
     * @param name
     * @return
     */
    public static synchronized ChessPlayer addUser(String id, String name) {
        try {
            if (service == null) getSheetsService();

            ChessPlayer user;
            //Check if player exists

            List row = isRanked(id);
            if (row != null) { // Player exists in ranked table
                user = new ChessPlayer((String)row.get(0), (String)row.get(1), Double.parseDouble((String)row.get(2)), null,
                                Boolean.valueOf((String)row.get(4)), (String)row.get(5), Integer.parseInt((String)row.get(6)),
                                Integer.parseInt((String)row.get(7)), Integer.parseInt((String)row.get(8)),
                                Double.parseDouble((String)row.get(9)), Integer.parseInt((String)row.get(10)), (String)row.get(11),
                                (String)row.get(12), (String)row.get(13));
                if (!user.provisional) {
                    user.highestElo = Double.parseDouble((String)row.get(3));
                }
                return user;
            } else {
                // Player doesn't exist. Add them as a provisional player
                ZonedDateTime cst = Instant.now().atZone(ZoneId.of("America/Chicago"));
                String createdOn = getDate(cst, RANKED_TAB, false);
                String updatedOn = getDate(cst, RANKED_TAB, true);
                // New users get default elo of 1500 - Class C
                ValueRange appendBody = new ValueRange()
                        .setValues(Arrays.asList(
                                Arrays.asList(id, name, 1500, "-", true, "Class C", "0", "0", "0", "0", 0, "0 days 0 hours 0 minutes 0 seconds", createdOn, updatedOn)
                        ));
                // Add new user to provisional tab
                service.spreadsheets().values()
                        .append(SPREAD_SHEET_ID, RANKED_TAB, appendBody)
                        .setValueInputOption("RAW")
                        .setInsertDataOption("INSERT_ROWS")
                        .execute();

                // Update ranked sheet by elo rating and then name
                BatchUpdateSpreadsheetRequest busReq = new BatchUpdateSpreadsheetRequest();
                SortSpec eloSortSpec = new SortSpec();
                eloSortSpec.setDimensionIndex(2);
                eloSortSpec.setSortOrder("DESCENDING");
                SortSpec nameSortSpec = new SortSpec();
                nameSortSpec.setDimensionIndex(1);
                nameSortSpec.setSortOrder("ASCENDING");
                SortRangeRequest sortRangeRequest = new SortRangeRequest();
                GridRange gridRange = new GridRange();
                gridRange.setStartRowIndex(1); //If we don't set this then header column is sorted
                gridRange.setSheetId(1906592208);
                sortRangeRequest.setRange(gridRange);
                sortRangeRequest.setSortSpecs(Arrays.asList(eloSortSpec, nameSortSpec));
                Request request = new Request();
                request.setSortRange(sortRangeRequest);
                busReq.setRequests(Arrays.asList(request));
                service.spreadsheets().batchUpdate(SPREAD_SHEET_ID, busReq).execute();

                return new ChessPlayer(id, name, 1500, null, true, "Class C", 0, 0, 0, 0.0, 0, "0 days 0 hours 0 minutes 0 seconds", createdOn, updatedOn);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static synchronized ChessPlayer findUserByName(String name) {
        try {
            if (service == null) getSheetsService();

            ChessPlayer user;
            //Check if player exists
            List row = isRankedByName(name);
            if (row != null) { // Player exists in ranked table
                user = new ChessPlayer((String)row.get(0), (String)row.get(1), Double.parseDouble((String)row.get(2)), null,
                        Boolean.valueOf((String)row.get(4)), (String)row.get(5), Integer.parseInt((String)row.get(6)),
                        Integer.parseInt((String)row.get(7)), Integer.parseInt((String)row.get(8)),
                        Double.parseDouble((String)row.get(9)), Integer.parseInt((String)row.get(10)), (String)row.get(11),
                        (String)row.get(12), (String)row.get(13));
                if (!user.provisional) {
                    user.highestElo = Double.parseDouble((String)row.get(3));
                }
                return user;
            } else {
                // Player doesn't exist
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static synchronized ChessPlayer findUserClosestElo(double elo, String id) {
        try {
            if (service == null) getSheetsService();

            ValueRange response = service.spreadsheets().values().get(SPREAD_SHEET_ID, RANKED_TAB).execute();

            List closest;
            double closestDiff = Double.MAX_VALUE;
            Random rand = new Random();
            boolean isFirstRow = true;
            List<List> candidates = new ArrayList<>();
            for (List row : response.getValues()) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                };
                if (id.equalsIgnoreCase((String)row.get(0))) { // User can't be itself
                    continue;
                }
                if (!((String)row.get(0)).contains(System.getenv("OWNER_ID"))) { // Ensure opponent found is a computer
                    continue;
                }
                double currElo = Double.parseDouble((String)row.get(2));
                double currDiff = Math.abs(elo - currElo);
                if (currDiff < closestDiff) {
                    closestDiff = currDiff;
                    candidates.clear();
                    candidates.add(row);
                }
                else if (currDiff == closestDiff && rand.nextBoolean()) { //If multiple opponents of same elo then randomly select them
                    closestDiff = currDiff;
                    candidates.add(row);
                }
            }

            //Choose a random opponent from candidate list
            closest = candidates.get(rand.nextInt(candidates.size()));
            ChessPlayer user = new ChessPlayer((String)closest.get(0), (String)closest.get(1), Double.parseDouble((String)closest.get(2)), null,
                    Boolean.valueOf((String)closest.get(4)), (String)closest.get(5), Integer.parseInt((String)closest.get(6)),
                    Integer.parseInt((String)closest.get(7)), Integer.parseInt((String)closest.get(8)),
                    Double.parseDouble((String)closest.get(9)), Integer.parseInt((String)closest.get(10)), (String)closest.get(11),
                    (String)closest.get(12), (String)closest.get(13));
            if (!user.provisional) {
                user.highestElo = Double.parseDouble((String)closest.get(3));
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Finds a player of similar elo. Opponent must be +- the range of the passed elo.
     * EX: Given an elo of 1000 and a range of 50, then opponent can be anywhere from 950 - 1050
     * @param elo
     * @param id
     * @param range
     * @return
     */
    public static synchronized ChessPlayer findUserSimilarElo(double elo, String id, double range) {
        try {
            if (service == null) getSheetsService();

            ValueRange response = service.spreadsheets().values().get(SPREAD_SHEET_ID, RANKED_TAB).execute();

            Random rand = new Random();
            boolean isFirstRow = true;
            List<List> candidates = new ArrayList<>();
            double lowerBound = elo - range;
            double upperBound = elo + range;
            for (List row : response.getValues()) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                };
                if (id.equalsIgnoreCase((String)row.get(0))) { // User can't be itself
                    continue;
                }
                if (!((String)row.get(0)).contains(System.getenv("OWNER_ID"))) { // Ensure opponent found is a computer
                    continue;
                }
                double currElo = Double.parseDouble((String)row.get(2));
                if (currElo >= lowerBound && currElo <= upperBound) {
                    candidates.add(row);
                }
            }

            //Choose a random opponent from candidate list
            ChessPlayer user;
            if (candidates.size() == 0) {
                user = findUserClosestElo(elo, id);
            }
            else {
                List player = candidates.get(rand.nextInt(candidates.size()));
                user = new ChessPlayer((String)player.get(0), (String)player.get(1), Double.parseDouble((String)player.get(2)), null,
                        Boolean.valueOf((String)player.get(4)), (String)player.get(5), Integer.parseInt((String)player.get(6)),
                        Integer.parseInt((String)player.get(7)), Integer.parseInt((String)player.get(8)),
                        Double.parseDouble((String)player.get(9)), Integer.parseInt((String)player.get(10)), (String)player.get(11),
                        (String)player.get(12), (String)player.get(13));
                if (!user.provisional) {
                    user.highestElo = Double.parseDouble((String)player.get(3));
                }
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find player in ranked tab by id
     * @param id
     * @return
     * @throws IOException
     */
    private static synchronized List isRanked(String id) throws IOException {
        ValueRange response = service.spreadsheets().values().get(SPREAD_SHEET_ID, RANKED_TAB).execute();
        rowNumber = 1;
        totalRows = response.getValues().size();
        if (totalRows > 0) {
            for (List row : response.getValues()) {
                if (id.equalsIgnoreCase((String)row.get(0))) {
                    return row;
                }
                rowNumber++;
            }
        }
        return null;
    }

    /**
     * Find player in ranked tab by name
     * @param name
     * @return
     * @throws IOException
     */
    private static synchronized List isRankedByName(String name) throws IOException {
        ValueRange response = service.spreadsheets().values().get(SPREAD_SHEET_ID, RANKED_TAB).execute();
        rowNumber = 1;
        totalRows = response.getValues().size();
        if (totalRows > 0) {
            for (List row : response.getValues()) {
                if (name.equalsIgnoreCase((String)row.get(1))) {
                    return row;
                }
                rowNumber++;
            }
        }
        return null;
    }

    /**
     * Given a ChessGameState update both users (Elo, Provisional, Title, Wins, ... , Avg. Game Length, Updated On)
     * Everything except Created On column
     * Behaves like POST or full update
     * Returns EloStatus object
     */
    public static synchronized void updateUser(ChessPlayer user) {
        try {
            if (service == null) getSheetsService();
            List row = isRanked(user.discordId);
            if (row == null) {
                return;
            }
            List values = new ArrayList();
            values.add(user.discordId);
            values.add(user.name);
            values.add(user.elo);
            if (user.highestElo != null) {
                values.add(user.highestElo);
            }
            else {
                values.add("-"); //No highest elo until done with provisionals
            }
            values.add(user.provisional);
            values.add(user.title);
            values.add(user.wins);
            values.add(user.losses);
            values.add(user.draws);
            values.add(formatPercent.format(user.ratio));
            values.add(user.totalGames);
            values.add(user.totalGameTimeStr);
            values.add(user.createdOn);
            values.add(getDate(Instant.now().atZone(ZoneId.of("America/Chicago")), RANKED_TAB, true));

            ValueRange body = new ValueRange().setValues(Arrays.asList(values));
            service.spreadsheets().values()
                    .update(SPREAD_SHEET_ID, RANKED_TAB+"!A"+rowNumber, body)
                    .setValueInputOption("RAW")
                    .execute();

            // Update ranked sheet by elo rating and then name
            BatchUpdateSpreadsheetRequest busReq = new BatchUpdateSpreadsheetRequest();
            SortSpec eloSortSpec = new SortSpec();
            eloSortSpec.setDimensionIndex(2);
            eloSortSpec.setSortOrder("DESCENDING");
            SortSpec nameSortSpec = new SortSpec();
            nameSortSpec.setDimensionIndex(1);
            nameSortSpec.setSortOrder("ASCENDING");
            SortRangeRequest sortRangeRequest = new SortRangeRequest();
            GridRange gridRange = new GridRange();
            gridRange.setStartRowIndex(1); //If we don't set this then header column is sorted
            gridRange.setSheetId(1906592208);
            sortRangeRequest.setRange(gridRange);
            sortRangeRequest.setSortSpecs(Arrays.asList(eloSortSpec, nameSortSpec));
            Request request = new Request();
            request.setSortRange(sortRangeRequest);
            busReq.setRequests(Arrays.asList(request));
            service.spreadsheets().batchUpdate(SPREAD_SHEET_ID, busReq).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized boolean addMatch(ChessPlayer whiteSidePlayer, ChessPlayer blackSidePlayer, ChessGameState state) {
        try {
            if (service == null) getSheetsService();

            Long nowMillis = Instant.now().toEpochMilli();
            Long seconds = (nowMillis - state.getMatchStartTime()) / 1000;
            long days = TimeUnit.SECONDS.toDays(seconds);
            long hours = TimeUnit.SECONDS.toHours(seconds) - (days *24);
            long mins = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
            long secs = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);

            String matchLength = "" + days + " days " + hours + " hours " + mins + " minutes " + secs + " seconds";
            double whiteSidePrevElo = state.getPrevElo().get(whiteSidePlayer.discordId);
            double blackSidePrevElo = state.getPrevElo().get(blackSidePlayer.discordId);
            double p1Odds = EloRanking.calculateProbabilityOfWin(whiteSidePrevElo, blackSidePrevElo);
            double p2Odds = 1.0 - p1Odds;
            String p1EloDiff;
            String p2EloDiff;
            ValueRange appendBody = null;
            String status = state.getStatus();
            Instant utc = Instant.now();
            String updatedOn = getDate(utc.atZone(ZoneId.of("America/Chicago")), MATCHES_TAB, true);
            if (DRAW.equals(status)) {
                p1EloDiff = generateEloDiffString(whiteSidePrevElo, whiteSidePlayer.elo);
                p2EloDiff = generateEloDiffString(blackSidePrevElo, blackSidePlayer.elo);
                appendBody = new ValueRange().setValues(Arrays.asList(Arrays.asList(whiteSidePlayer.name, whiteSidePlayer.discordId, Math.round(whiteSidePlayer.elo)+" ("+p1EloDiff+")", formatPercent.format(p1Odds*100)+"%",
                                                                                    blackSidePlayer.name,  blackSidePlayer.discordId,  Math.round(blackSidePlayer.elo)+" ("+p2EloDiff+")", formatPercent.format(p2Odds*100)+"%",
                                                                                    "-", "-", DRAW.equals(status), state.isPlayerForfeited(), state.getFullMoves(), matchLength, updatedOn, utc.toString(), state.getMoveHistoryBuilder().toString())));
            } else if (state.getWinnerId().equals(whiteSidePlayer.discordId)) {
                p1EloDiff = generateEloDiffString(whiteSidePrevElo, whiteSidePlayer.elo);
                p2EloDiff = generateEloDiffString(blackSidePrevElo, blackSidePlayer.elo);
                appendBody = new ValueRange().setValues(Arrays.asList(Arrays.asList(whiteSidePlayer.name, whiteSidePlayer.discordId, Math.round(whiteSidePlayer.elo)+" ("+p1EloDiff+")", formatPercent.format(p1Odds*100)+"%",
                                                                                    blackSidePlayer.name,  blackSidePlayer.discordId, Math.round(blackSidePlayer.elo)+" ("+p2EloDiff+")", formatPercent.format(p2Odds*100)+"%",
                                                                                    whiteSidePlayer.name, blackSidePlayer.name, DRAW.equals(status), state.isPlayerForfeited(), state.getFullMoves(), matchLength, updatedOn, utc.toString(), state.getMoveHistoryBuilder().toString())));
            }
            else if (state.getWinnerId().equals(blackSidePlayer.discordId)) {
                p1EloDiff = generateEloDiffString(whiteSidePrevElo, whiteSidePlayer.elo);
                p2EloDiff = generateEloDiffString(blackSidePrevElo, blackSidePlayer.elo);
                appendBody = new ValueRange().setValues(Arrays.asList(Arrays.asList(whiteSidePlayer.name, whiteSidePlayer.discordId, Math.round(whiteSidePlayer.elo)+" ("+p1EloDiff+")", formatPercent.format(p1Odds *100)+"%",
                                                                                    blackSidePlayer.name,  blackSidePlayer.discordId, Math.round(blackSidePlayer.elo)+" ("+p2EloDiff+")", formatPercent.format(p2Odds*100)+"%",
                                                                                    blackSidePlayer.name, whiteSidePlayer.name, DRAW.equals(status), state.isPlayerForfeited(), state.getFullMoves(), matchLength, updatedOn, utc.toString(), state.getMoveHistoryBuilder().toString())));
            }

            //Append the new match
            service.spreadsheets().values()
                    .append(SPREAD_SHEET_ID, MATCHES_TAB, appendBody)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();

            //Sort Matches by updated on column
            BatchUpdateSpreadsheetRequest busReq = new BatchUpdateSpreadsheetRequest();
            SortSpec sortSpec = new SortSpec();
            sortSpec.setDimensionIndex(15); //Sort by UTC so we don't have any issues with UTC to CST conversion
            sortSpec.setSortOrder("DESCENDING");
            SortRangeRequest sortRangeRequest = new SortRangeRequest();
            GridRange gridRange = new GridRange();
            gridRange.setStartRowIndex(1); //If we don't set this then header column is sorted
            gridRange.setSheetId(2021381704);
            sortRangeRequest.setRange(gridRange);
            sortRangeRequest.setSortSpecs(Arrays.asList(sortSpec));
            Request sortRequest = new Request();
            sortRequest.setSortRange(sortRangeRequest);
            busReq.setRequests(Arrays.asList(sortRequest));
            service.spreadsheets().batchUpdate(SPREAD_SHEET_ID, busReq).execute();
            //Oldest match will now be the last row

            //Delete the 10001 row. Only saving 10000 matches maximum
            BatchUpdateSpreadsheetRequest deleteBatch = new BatchUpdateSpreadsheetRequest();
            Request deleteRequest = new Request()
                    .setDeleteDimension(new DeleteDimensionRequest()
                            .setRange(new DimensionRange()
                                    .setSheetId(2021381704)
                                    .setDimension("ROWS")
                                    .setStartIndex(10000)
                                    .setEndIndex(10001)
                            )
                    );
            deleteBatch.setRequests(Arrays.asList(deleteRequest));
            service.spreadsheets().batchUpdate(SPREAD_SHEET_ID, deleteBatch).execute();

            //Update total game time
            updateTotalGameTime(whiteSidePlayer, days, hours, mins, secs);
            updateTotalGameTime(blackSidePlayer, days, hours, mins, secs);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized boolean updateTotalGameTime(ChessPlayer p, long days, long hours, long mins, long secs) throws IOException {
        String timePlayedSoFar = p.totalGameTimeStr;
        String[] split = timePlayedSoFar.split("\\s+"); //Example: 0 days 0 hours 0 minutes 7 seconds
        days += Integer.parseInt(split[0].trim());
        hours += Integer.parseInt(split[2].trim());
        mins += Integer.parseInt(split[4].trim());
        secs +=  Integer.parseInt(split[6].trim());

        if (isRanked(p.discordId) == null) {
            return false;
        }

        long totalTimeSeconds = TimeUnit.DAYS.toSeconds(days) + TimeUnit.HOURS.toSeconds(hours) + TimeUnit.MINUTES.toSeconds(mins) + secs;

        days = TimeUnit.SECONDS.toDays(totalTimeSeconds);
        hours = TimeUnit.SECONDS.toHours(totalTimeSeconds) - (days *24);
        mins = TimeUnit.SECONDS.toMinutes(totalTimeSeconds) - (TimeUnit.SECONDS.toHours(totalTimeSeconds)* 60);
        secs = TimeUnit.SECONDS.toSeconds(totalTimeSeconds) - (TimeUnit.SECONDS.toMinutes(totalTimeSeconds) *60);

        String matchLength = "" + days + " days " + hours + " hours " + mins + " minutes " + secs + " seconds";
        ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(matchLength)));

        service.spreadsheets().values()
                .update(SPREAD_SHEET_ID, RANKED_TAB+"!L"+rowNumber, body)
                .setValueInputOption("RAW")
                .execute();
        return true;
    }

    private static String generateEloDiffString(double startingElo, double newElo) {
        String eloDiff = null;
        if (newElo >= startingElo) {
            eloDiff = "+"+Math.round(newElo-startingElo);
        }
        else if (newElo < startingElo) {
            eloDiff = "-"+Math.round(startingElo-newElo);
        }
        return eloDiff;
    }

    public static String getDate(ZonedDateTime cst, String sheetName, boolean isUpdatedOn) {
        if (sheetName.equals(MATCHES_TAB)) {
            return cst.format(DateTimeFormatter.ofPattern(MM_DD_YYYY_HH_MM_SS_A));
        }
        else { //RANKED Tab
            if (isUpdatedOn) { // Updated On
                return cst.format(DateTimeFormatter.ofPattern(MMM_DD_YYYY_HH_MM_A));
            }
            else { //Created On
                return cst.format(DateTimeFormatter.ofPattern(MMM_DD_YYYY));
            }
        }
    }
}
