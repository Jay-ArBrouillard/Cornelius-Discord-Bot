package Utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.*;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GoogleSheets {
    private static String APPLICATION_NAME = "players";
    private static String SPREAD_SHEET_ID = System.getenv("SPREAD_SHEET_ID");
    private static HttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Sheets service;
    private static DecimalFormat formatPercent = new DecimalFormat("##0.00");
    private static DecimalFormat formatRatio = new DecimalFormat("##.00");

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
    public static int addUser(String id, String name) {
        try {
            if (service == null) getSheetsService();

            //If user does not exist add them
            ValueRange response = service.spreadsheets().values()
                    .get(SPREAD_SHEET_ID, "players")
                    .execute();
            List<List<Object>> allUsers = response.getValues();
            for (List row : allUsers) {
                if (row.get(0).equals(id)) {
                    return Integer.parseInt((String)row.get(2)); //User exists already
                }
            }

            String now = getCurrentDateTime();
            //New users get default elo of 1000
            ValueRange appendBody = new ValueRange()
                .setValues(Arrays.asList(
                        Arrays.asList(id, name, 1000, "Class E", "0", "0", "0", "0", 0, "0 days 0 hours 0 minutes 0 seconds", now, now)
                ));
            service.spreadsheets().values()
                .append(SPREAD_SHEET_ID, "players", appendBody)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute();

            return 1000;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Returns total games user played
     * @param id
     * @param isWin
     * @param isDraw
     * @param thisElo
     * @param otherElo
     * @return
     */
    public static EloStatus updateUser(String id, boolean isWin, boolean isDraw, int thisElo, int otherElo) {
        try {
            if (service == null) getSheetsService();
            ValueRange response = service.spreadsheets().values()
                    .get(SPREAD_SHEET_ID, "players")
                    .execute();
            List<List<Object>> allUsers = response.getValues();
            if (allUsers == null || allUsers.isEmpty()) {
                return null;
            } else {
                int rowNumber = 1;
                for (List row : allUsers) {
                    if (row.get(0).equals(id)) {
                        int wins = Integer.parseInt((String) row.get(4));
                        double losses = Double.parseDouble((String) row.get(5));
                        int draws = Integer.parseInt((String) row.get(6));
                        int totalGames = wins + (int) losses + draws;
                        EloStatus eloStatus = new EloStatus();
                        eloStatus.prevElo = thisElo;

                        //Calculate new elo
                        double probWin = (1.0 / (1.0 + Math.pow(10, ((otherElo-thisElo) / 400)))); // Probability winning
                        double newEloRating = thisElo;

                        if (isWin) {
                            //Update wins
                            wins++;
                            ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(wins)));
                            service.spreadsheets().values()
                                    .update(SPREAD_SHEET_ID, "E"+rowNumber, body)
                                    .setValueInputOption("RAW")
                                    .execute();
                            //Update Elo
                            if (newEloRating != -1) {
                                newEloRating = Math.round(newEloRating + determineK(thisElo, totalGames) * (1 - probWin));
                                updateEloAndTitle(rowNumber, newEloRating);
                            }
                            //Update "Updated On" Column
                            changeUpdatedOnColumn(rowNumber);
                        }
                        else if (!isWin && !isDraw) {
                            //Update losses
                            losses++;
                            ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(losses)));
                            service.spreadsheets().values()
                                    .update(SPREAD_SHEET_ID, "F"+rowNumber, body)
                                    .setValueInputOption("RAW")
                                    .execute();
                            //Update Elo
                            if (newEloRating != -1) {
                                newEloRating = Math.round(thisElo + determineK(thisElo, totalGames) * (0 - probWin));
                                updateEloAndTitle(rowNumber, newEloRating);
                            }
                            //Update "Updated On" Column
                            changeUpdatedOnColumn(rowNumber);
                        }
                        else if (isDraw) {
                            //Update draws
                            draws++;
                            ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(draws)));
                            service.spreadsheets().values()
                                    .update(SPREAD_SHEET_ID, "G"+rowNumber, body)
                                    .setValueInputOption("RAW")
                                    .execute();
                            //Update Elo
                            if (newEloRating != -1) {
                                newEloRating = Math.round(thisElo + determineK(thisElo, totalGames) * (0.5 - probWin));
                                updateEloAndTitle(rowNumber, newEloRating);
                            }
                            //Update "Updated On" Column
                            changeUpdatedOnColumn(rowNumber);
                        }
                        eloStatus.currElo = newEloRating;

                        //Update Win Loss Ratio
                        if (losses == 0) losses = 1;
                        ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(formatRatio.format((wins+draws)/losses))));
                        service.spreadsheets().values()
                                .update(SPREAD_SHEET_ID, "H"+rowNumber, body)
                                .setValueInputOption("USER_ENTERED")
                                .execute();
                        //Update Total Games Played
                        totalGames++;
                        eloStatus.totalGamesPlayed = totalGames;
                        body = new ValueRange().setValues(Arrays.asList(Arrays.asList(totalGames)));
                        service.spreadsheets().values()
                                .update(SPREAD_SHEET_ID, "I"+rowNumber, body)
                                .setValueInputOption("RAW")
                                .execute();
                        return eloStatus;
                    }
                    rowNumber++;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
                    .get(SPREAD_SHEET_ID, "matches")
                    .execute();
            List<List<Object>> allMatches = response.getValues();
            if (allMatches != null || !allMatches.isEmpty()) {
                for (List row : allMatches) {
                    if (row.get(2).equals(id) || row.get(3).equals(id)) {
                        String[] split = ((String) row.get(12)).split("\\s+"); //Example: 0 days 0 hours 0 minutes 7 seconds
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
                    .get(SPREAD_SHEET_ID, "players")
                    .execute();
            List<List<Object>> allPlayers = response.getValues();
            if (allPlayers != null || !allPlayers.isEmpty()) {
                int rowNumber = 1;
                for (List row : allPlayers) {
                    if (row.get(0).equals(id)) {
                        String matchLength = "" + days + " days " + hours + " hours " + minutes + " minutes " + seconds + " seconds";
                        ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(matchLength)));

                        service.spreadsheets().values()
                                .update(SPREAD_SHEET_ID, "J"+rowNumber, body)
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

    public static boolean addCompletedMatch(String player1, String player2, String id1, String id2, String winnerId, boolean isDraw, Long startTime, double moveCount, EloStatus p1EloStatus, EloStatus p2EloStatus) {
        try {
            if (service == null) getSheetsService();

            Long seconds = (System.currentTimeMillis() - startTime) / 1000;
            int day = (int) TimeUnit.SECONDS.toDays(seconds);
            long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
            long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
            long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);

            String matchLength = "" + day + " days " + hours + " hours " + minute + " minutes " + second + " seconds";
            double p1Odds = 1.0 / (1.0 + Math.pow(10.0, ((p2EloStatus.prevElo-p1EloStatus.prevElo) / 400.0)));
            double p2Odds = 1.0 - p1Odds;
            String p1EloDiff;
            String p2EloDiff;
            ValueRange appendBody;
            if (winnerId.equals(id1)) {
                p1EloDiff = generateEloDiffString(p1EloStatus.prevElo, p1EloStatus.currElo);
                p2EloDiff = generateEloDiffString(p2EloStatus.prevElo, p2EloStatus.currElo);
                appendBody = new ValueRange().setValues(Arrays.asList(Arrays.asList(player1, id1, Math.round(p2EloStatus.currElo)+" ("+p2EloDiff+")", formatPercent.format(p2Odds*100)+"%", player2,  id2, Math.round(p1EloStatus.currElo)+" ("+p1EloDiff+")", formatPercent.format(p1Odds*100)+"%", player1, player2, isDraw, moveCount, matchLength, getCurrentDateTime())));
            }
            else if (winnerId.equals(id2)) {
                p1EloDiff = generateEloDiffString(p1EloStatus.prevElo, p1EloStatus.currElo);
                p2EloDiff = generateEloDiffString(p2EloStatus.prevElo, p2EloStatus.currElo);
                appendBody = new ValueRange().setValues(Arrays.asList(Arrays.asList(player1, id1, Math.round(p2EloStatus.currElo)+" ("+p2EloDiff+")", formatPercent.format(p2Odds*100)+"%", player2,  id2,  Math.round(p1EloStatus.currElo)+" ("+p1EloDiff+")", formatPercent.format(p1Odds *100)+"%", player2, player1, isDraw, moveCount, matchLength, getCurrentDateTime())));
            }
            else { //draw
                p1EloDiff = generateEloDiffString(p1EloStatus.prevElo, p1EloStatus.currElo);
                p2EloDiff = generateEloDiffString(p2EloStatus.prevElo, p2EloStatus.currElo);
                appendBody = new ValueRange().setValues(Arrays.asList(Arrays.asList(player1, id1, Math.round(p1EloStatus.currElo)+" ("+p1EloDiff+")", formatPercent.format(p1Odds*100)+"%", player2,  id2,  Math.round(p2EloStatus.currElo)+" ("+p2EloDiff+")", formatPercent.format(p2Odds*100)+"%", "-", "-", isDraw, moveCount, matchLength, getCurrentDateTime())));
            }

            service.spreadsheets().values()
                    .append(SPREAD_SHEET_ID, "matches", appendBody)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();

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

    private static void updateEloAndTitle(int rowNumber, double newElo) throws IOException {
        ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList((int)newElo, determineTitle((int)newElo))));
        service.spreadsheets().values()
                .update(SPREAD_SHEET_ID, "C"+rowNumber, body)
                .setValueInputOption("RAW")
                .execute();
    }

    private static void changeUpdatedOnColumn(int rowNumber) throws IOException {
        ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(getCurrentDateTime())));
        service.spreadsheets().values()
                .update(SPREAD_SHEET_ID, "L"+rowNumber, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public static String getCurrentDateTime() {
        ZonedDateTime myDate = ZonedDateTime.now();
        return DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a").format(myDate);
    }

    /**
     * Determine the rating constant K-factor based on current rating
     *
     * @param rating
     *            Player rating
     * @return K-factor
     */
    public static int determineK(int rating, int totalGames) {
        int K;
        if (rating < 2000) {
            if (totalGames == 0) totalGames = 1;
            K = 800 / totalGames; //Round down
        } else if (rating >= 2000 && rating < 2400) {
            K = 24;
        } else {
            K = 16;
        }
        return K;
    }

    public static String determineTitle(int rating) {
        if (rating >= 2500) {
            return "Grandmaster (GM)";
        }
        else if (rating >= 2400) {
            return "International Master (IM)";
        }
        else if (rating >= 2300) {
            return "FIDE Master (FM)";
        }
        else if (rating >= 2200) {
            return "Senior Master (SM)";
        }
        else if (rating >= 2100) {
            return "Master (M)";
        }
        else if (rating >= 2000) {
            return "Candidate Master (CM)";
        }
        else if (rating >= 1800) {
            return "Class A";
        }
        else if (rating >= 1600) {
            return "Class B";
        }
        else if (rating >= 1400) {
            return "Class C";
        }
        else if (rating >= 1200) {
            return "Class D";
        }
        else if (rating >= 1000) {
            return "Class E";
        }
        else if (rating >= 800) {
            return "Class F";
        }
        else if (rating >= 600) {
            return "Class G";
        }
        else if (rating >= 400) {
            return "Class H";
        }
        else if (rating >= 200) {
            return "Class I";
        }
        else {
            return "Class J";
        }
    }
}
