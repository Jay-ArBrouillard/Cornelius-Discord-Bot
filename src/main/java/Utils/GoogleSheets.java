package Utils;

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

    /**
     * Add new user if they don't exist and returns their elo value
     * @param id
     * @param name
     * @return
     */
    public static ChessPlayer addUser(String id, String name) {
        try {
            if (service == null) getSheetsService();

            ChessPlayer user;
            //Check if player exists

            List row = isRanked(id);
            if (row != null) { // Player exists in ranked table
                user = new ChessPlayer((String)row.get(0), (String)row.get(1), Integer.parseInt((String)row.get(2)),
                                Boolean.valueOf((String)row.get(3)), (String)row.get(4), Integer.parseInt((String)row.get(5)),
                                Integer.parseInt((String)row.get(6)), Integer.parseInt((String)row.get(7)),
                                Double.parseDouble((String)row.get(8)), Integer.parseInt((String)row.get(9)), (String)row.get(10),
                                (String)row.get(11), (String)row.get(12));
                return user;
            } else {
                // Player doesn't exist. Add them as a provisional player
                String now = getDate(Instant.now().toEpochMilli());
                // New users get default elo of 1200 - Class D
                ValueRange appendBody = new ValueRange()
                        .setValues(Arrays.asList(
                                Arrays.asList(id, name, 1200, true, "Class D", "0", "0", "0", "0", 0, "0 days 0 hours 0 minutes 0 seconds", now, now)
                        ));
                // Add new user to provisional tab
                service.spreadsheets().values()
                        .append(SPREAD_SHEET_ID, RANKED_TAB, appendBody)
                        .setValueInputOption("RAW")
                        .setInsertDataOption("INSERT_ROWS")
                        .execute();

                // Update ranked sheet by elo rating
                BatchUpdateSpreadsheetRequest busReq = new BatchUpdateSpreadsheetRequest();
                SortSpec sortSpec = new SortSpec();
                sortSpec.setDimensionIndex(2);
                sortSpec.setSortOrder("DESCENDING");
                SortRangeRequest sortRangeRequest = new SortRangeRequest();
                GridRange gridRange = new GridRange();
                gridRange.setSheetId(1906592208);
                sortRangeRequest.setRange(gridRange);
                sortRangeRequest.setSortSpecs(Arrays.asList(sortSpec));
                Request request = new Request();
                request.setSortRange(sortRangeRequest);
                busReq.setRequests(Arrays.asList(request));
                service.spreadsheets().batchUpdate(SPREAD_SHEET_ID, busReq).execute();

                return new ChessPlayer(id, name, 1200, true, "Class D", 0, 0, 0, 0.0, 0, "0 days 0 hours 0 minutes 0 seconds", now, now);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ChessPlayer findUserByName(String name) {
        try {
            if (service == null) getSheetsService();

            ChessPlayer user;
            //Check if player exists
            List row = isRankedByName(name);
            if (row != null) { // Player exists in ranked table
                user = new ChessPlayer((String)row.get(0), (String)row.get(1), Integer.parseInt((String)row.get(2)),
                        Boolean.valueOf((String)row.get(3)), (String)row.get(4), Integer.parseInt((String)row.get(5)),
                        Integer.parseInt((String)row.get(6)), Integer.parseInt((String)row.get(7)),
                        Double.parseDouble((String)row.get(8)), Integer.parseInt((String)row.get(9)), (String)row.get(10),
                        (String)row.get(11), (String)row.get(12));
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

    public static ChessPlayer findUserClosestElo(int elo) {
        try {
            if (service == null) getSheetsService();

            ValueRange response = service.spreadsheets().values().get(SPREAD_SHEET_ID, RANKED_TAB).execute();

            List closest = null;
            int closestDiff = Integer.MAX_VALUE;
            Random rand = new Random();
            boolean isFirstRow = true;
            for (List row : response.getValues()) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                };
                int currElo = Integer.parseInt((String)row.get(2));
                int currDiff = Math.abs(elo - currElo);
                if (currDiff < closestDiff) {
                    closest = row;
                    closestDiff = currDiff;
                }
                else if (currDiff == closestDiff && rand.nextBoolean()) { //If multiple opponents of same elo then randomly select them
                    closest = row;
                    closestDiff = currDiff;
                }
            }

            return new ChessPlayer((String)closest.get(0), (String)closest.get(1), Integer.parseInt((String)closest.get(2)),
                    Boolean.valueOf((String)closest.get(3)), (String)closest.get(4), Integer.parseInt((String)closest.get(5)),
                    Integer.parseInt((String)closest.get(6)), Integer.parseInt((String)closest.get(7)),
                    Double.parseDouble((String)closest.get(8)), Integer.parseInt((String)closest.get(9)), (String)closest.get(10),
                    (String)closest.get(11), (String)closest.get(12));
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
    private static List isRanked(String id) throws IOException {
        ValueRange response = service.spreadsheets().values().get(SPREAD_SHEET_ID, RANKED_TAB).execute();
        rowNumber = 1;
        totalRows = response.getValues().size();
        for (List row : response.getValues()) {
            if (id.equalsIgnoreCase((String)row.get(0))) {
                return row;
            }
            rowNumber++;
        }
        return null;
    }

    /**
     * Find player in ranked tab by name
     * @param name
     * @return
     * @throws IOException
     */
    private static List isRankedByName(String name) throws IOException {
        ValueRange response = service.spreadsheets().values().get(SPREAD_SHEET_ID, RANKED_TAB).execute();
        rowNumber = 1;
        totalRows = response.getValues().size();
        for (List row : response.getValues()) {
            if (row.get(1).equals(name)) {
                return row;
            }
            rowNumber++;
        }
        return null;
    }

    /**
     * Given a ChessGameState update both users (Elo, Provisional, Title, Wins, ... , Avg. Game Length, Updated On)
     * Everything except Created On column
     * Behaves like POST or full update
     * Returns EloStatus object
     */
    public static void updateUser(ChessPlayer user) {
        try {
            if (service == null) getSheetsService();
            if (isRanked(user.discordId) == null) return;

            List row = isRanked(user.discordId);
            if (row != null) {
                System.out.println("addUser - userFound: " + row.get(0) + ", " + row.get(1));
                System.out.println("At row: " + rowNumber);
            }
            else {
                System.out.println("addUser - no user was found for: " + user.discordId + ", " + user.name);
            }

            List values = new ArrayList();
            values.add(user.discordId);
            values.add(user.name);
            values.add(user.elo);
            values.add(user.provisional);
            values.add(user.title);
            values.add(user.wins);
            values.add(user.losses);
            values.add(user.draws);
            values.add(formatPercent.format(user.ratio));
            values.add(user.totalGames);
            values.add(user.avgGameLength);
            values.add(user.createdOn);
            values.add(getDate(Instant.now().toEpochMilli()));

            ValueRange body = new ValueRange().setValues(Arrays.asList(values));
            service.spreadsheets().values()
                    .update(SPREAD_SHEET_ID, RANKED_TAB+"!A"+rowNumber, body)
                    .setValueInputOption("RAW")
                    .execute();

            // Update ranked sheet by elo rating
            BatchUpdateSpreadsheetRequest busReq = new BatchUpdateSpreadsheetRequest();
            SortSpec sortSpec = new SortSpec();
            sortSpec.setDimensionIndex(2);
            sortSpec.setSortOrder("DESCENDING");
            SortRangeRequest sortRangeRequest = new SortRangeRequest();
            GridRange gridRange = new GridRange();
            gridRange.setSheetId(1906592208);
            sortRangeRequest.setRange(gridRange);
            sortRangeRequest.setSortSpecs(Arrays.asList(sortSpec));
            Request request = new Request();
            request.setSortRange(sortRangeRequest);
            busReq.setRequests(Arrays.asList(request));
            service.spreadsheets().batchUpdate(SPREAD_SHEET_ID, busReq).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean updateAvgGameLength(String id) {
        try {
            if (service == null) getSheetsService();
            long days = 0;
            long hours = 0;
            long minutes = 0;
            long seconds = 0;
            int totalMatches = 0;
            ValueRange response = service.spreadsheets().values()
                    .get(SPREAD_SHEET_ID, MATCHES_TAB)
                    .execute();
            List<List<Object>> allMatches = response.getValues();
            if (allMatches != null || !allMatches.isEmpty()) {
                for (List row : allMatches) {
                    if (row.get(1).equals(id) || row.get(5).equals(id)) {
                        String[] split = ((String) row.get(13)).split("\\s+"); //Example: 0 days 0 hours 0 minutes 7 seconds
                        days += Integer.parseInt(split[0].trim());
                        hours += Integer.parseInt(split[2].trim());
                        minutes += Integer.parseInt(split[4].trim());
                        seconds +=  Integer.parseInt(split[6].trim());
                        totalMatches++;
                    }
                }
            }

            //Process values
            if (totalMatches == 0) totalMatches = 1;
            long totalTimeSeconds = Math.round((TimeUnit.DAYS.toSeconds(days) + TimeUnit.HOURS.toSeconds(hours) +
                                    TimeUnit.MINUTES.toSeconds(minutes) + seconds) / totalMatches);

            days = TimeUnit.SECONDS.toDays(totalTimeSeconds);
            hours = TimeUnit.SECONDS.toHours(totalTimeSeconds) - (days *24);
            minutes = TimeUnit.SECONDS.toMinutes(totalTimeSeconds) - (TimeUnit.SECONDS.toHours(totalTimeSeconds)* 60);
            seconds = TimeUnit.SECONDS.toSeconds(totalTimeSeconds) - (TimeUnit.SECONDS.toMinutes(totalTimeSeconds) *60);

            response = service.spreadsheets().values()
                    .get(SPREAD_SHEET_ID, RANKED_TAB)
                    .execute();
            List<List<Object>> allPlayers = response.getValues();
            if (allPlayers != null || !allPlayers.isEmpty()) {
                int rowNumber = 1;
                for (List row : allPlayers) {
                    if (row.get(0).equals(id)) {
                        String matchLength = "" + days + " days " + hours + " hours " + minutes + " minutes " + seconds + " seconds";
                        ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(matchLength)));

                        service.spreadsheets().values()
                                .update(SPREAD_SHEET_ID, RANKED_TAB+"!K"+rowNumber, body)
                                .setValueInputOption("RAW")
                                .execute();
                        return true;
                    }
                    rowNumber++;
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addMatch(ChessPlayer whiteSidePlayer, ChessPlayer blackSidePlayer, ChessGameState state) {
        try {
            if (service == null) getSheetsService();

            Long millis = Instant.now().toEpochMilli();
            Long seconds = (millis - state.getMatchStartTime()) / 1000;
            int day = (int) TimeUnit.SECONDS.toDays(seconds);
            long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
            long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
            long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);

            String matchLength = "" + day + " days " + hours + " hours " + minute + " minutes " + second + " seconds";
            double whiteSidePrevElo = (double) state.getPrevElo().get(whiteSidePlayer.discordId);
            double blackSidePrevElo = (double) state.getPrevElo().get(blackSidePlayer.discordId);
            double p1Odds = EloRanking.calculateProbabilityOfWin((int)whiteSidePrevElo, (int)blackSidePrevElo);
            double p2Odds = 1.0 - p1Odds;
            String p1EloDiff;
            String p2EloDiff;
            ValueRange appendBody = null;
            String status = state.getStatus();
            if (DRAW.equals(status)) {
                p1EloDiff = generateEloDiffString(whiteSidePrevElo, whiteSidePlayer.elo);
                p2EloDiff = generateEloDiffString(blackSidePrevElo, blackSidePlayer.elo);
                appendBody = new ValueRange().setValues(Arrays.asList(Arrays.asList(whiteSidePlayer.name, whiteSidePlayer.discordId, Math.round(whiteSidePlayer.elo)+" ("+p1EloDiff+")", formatPercent.format(p1Odds*100)+"%",
                                                                                    blackSidePlayer.name,  blackSidePlayer.discordId,  Math.round(blackSidePlayer.elo)+" ("+p2EloDiff+")", formatPercent.format(p2Odds*100)+"%",
                                                                                    "-", "-", DRAW.equals(status), state.isPlayerForfeited(), state.getTotalMoves(), matchLength, getDate(millis))));
            } else if (state.getWinnerId().equals(whiteSidePlayer.discordId)) {
                p1EloDiff = generateEloDiffString(whiteSidePrevElo, whiteSidePlayer.elo);
                p2EloDiff = generateEloDiffString(blackSidePrevElo, blackSidePlayer.elo);
                appendBody = new ValueRange().setValues(Arrays.asList(Arrays.asList(whiteSidePlayer.name, whiteSidePlayer.discordId, Math.round(whiteSidePlayer.elo)+" ("+p1EloDiff+")", formatPercent.format(p1Odds*100)+"%",
                                                                                    blackSidePlayer.name,  blackSidePlayer.discordId, Math.round(blackSidePlayer.elo)+" ("+p2EloDiff+")", formatPercent.format(p2Odds*100)+"%",
                                                                                    whiteSidePlayer.name, blackSidePlayer.name, DRAW.equals(status), state.isPlayerForfeited(), state.getTotalMoves(), matchLength, getDate(millis))));
            }
            else if (state.getWinnerId().equals(blackSidePlayer.discordId)) {
                p1EloDiff = generateEloDiffString(whiteSidePrevElo, whiteSidePlayer.elo);
                p2EloDiff = generateEloDiffString(blackSidePrevElo, blackSidePlayer.elo);
                appendBody = new ValueRange().setValues(Arrays.asList(Arrays.asList(whiteSidePlayer.name, whiteSidePlayer.discordId, Math.round(whiteSidePlayer.elo)+" ("+p1EloDiff+")", formatPercent.format(p1Odds *100)+"%",
                                                                                    blackSidePlayer.name,  blackSidePlayer.discordId, Math.round(blackSidePlayer.elo)+" ("+p2EloDiff+")", formatPercent.format(p2Odds*100)+"%",
                                                                                    blackSidePlayer.name, whiteSidePlayer.name, DRAW.equals(status), state.isPlayerForfeited(), state.getTotalMoves(), matchLength, getDate(millis))));
            }

            service.spreadsheets().values()
                    .append(SPREAD_SHEET_ID, "matches", appendBody)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();

            //Sort Matches by updated on column
            BatchUpdateSpreadsheetRequest busReq = new BatchUpdateSpreadsheetRequest();
            SortSpec sortSpec = new SortSpec();
            sortSpec.setDimensionIndex(14);
            sortSpec.setSortOrder("DESCENDING");
            SortRangeRequest sortRangeRequest = new SortRangeRequest();
            GridRange gridRange = new GridRange();
            gridRange.setSheetId(2021381704);
            sortRangeRequest.setRange(gridRange);
            sortRangeRequest.setSortSpecs(Arrays.asList(sortSpec));
            Request request = new Request();
            request.setSortRange(sortRangeRequest);
            busReq.setRequests(Arrays.asList(request));
            service.spreadsheets().batchUpdate(SPREAD_SHEET_ID, busReq).execute();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    public static String getDate(Long pCurrentTimeMs) {
        Instant i = Instant.ofEpochMilli(pCurrentTimeMs);
        return DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss a").format(ZonedDateTime.ofInstant(i, ZoneId.of("America/Chicago")));
    }
}
