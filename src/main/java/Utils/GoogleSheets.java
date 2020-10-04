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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GoogleSheets {
    private static String APPLICATION_NAME = "players";
    private static String SPREAD_SHEET_ID = System.getenv("SPREAD_SHEET_ID");
    private static HttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Sheets service;
    private static DecimalFormat df = new DecimalFormat("##.00");

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
//        InputStream targetStream = GoogleSheets.class.getResourceAsStream("/credentials.json"); //For local testing
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
                        Arrays.asList(id, name, 1000, "0", "0", "0", "0", now, now)
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

    public static boolean updateUser(String id, boolean isWin, boolean isDraw, int thisElo, int otherElo) {
        try {
            if (service == null) getSheetsService();
            ValueRange response = service.spreadsheets().values()
                    .get(SPREAD_SHEET_ID, "players")
                    .execute();
            List<List<Object>> allUsers = response.getValues();
            if (allUsers == null || allUsers.isEmpty()) {
                return false;
            } else {
                int rowNumber = 1;
                for (List row : allUsers) {
                    if (row.get(0).equals(id)) {
                        int wins = Integer.parseInt((String) row.get(3));
                        double losses = Double.parseDouble((String) row.get(4));
                        int draws = Integer.parseInt((String) row.get(5));

                        //Calculate new elo
                        double probabilityWin = (1.0 / (1.0 + Math.pow(10, ((thisElo-otherElo) / 400)))); // Probability winning
                        double newEloRating = thisElo;

                        if (isWin) {
                            //Update wins
                            wins++;
                            ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(wins)));
                            service.spreadsheets().values()
                                    .update(SPREAD_SHEET_ID, "D"+rowNumber, body)
                                    .setValueInputOption("USER_ENTERED")
                                    .execute();
                            //Update Elo
                            if (newEloRating != -1) {
                                newEloRating = newEloRating + determineK(thisElo) * (1 - probabilityWin);
                                updateElo(rowNumber, newEloRating);
                            }
                            //Update "Updated On" Column
                            changeUpdatedOnColumn(rowNumber);
                        }
                        else if (!isWin && !isDraw) {
                            //Update losses
                            losses++;
                            ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(losses)));
                            service.spreadsheets().values()
                                    .update(SPREAD_SHEET_ID, "E"+rowNumber, body)
                                    .setValueInputOption("USER_ENTERED")
                                    .execute();
                            //Update Elo
                            if (newEloRating != -1) {
                                newEloRating = thisElo + determineK(thisElo) * (0 - probabilityWin);
                                updateElo(rowNumber, newEloRating);
                            }
                            //Update "Updated On" Column
                            changeUpdatedOnColumn(rowNumber);
                        }
                        else if (isDraw) {
                            //Update draws
                            draws++;
                            ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(draws)));
                            service.spreadsheets().values()
                                    .update(SPREAD_SHEET_ID, "F"+rowNumber, body)
                                    .setValueInputOption("USER_ENTERED")
                                    .execute();
                            //Update Elo
                            if (newEloRating != -1) {
                                newEloRating = thisElo + determineK(thisElo) * (0.5 - probabilityWin);
                                updateElo(rowNumber, newEloRating);
                            }
                            //Update "Updated On" Column
                            changeUpdatedOnColumn(rowNumber);
                        }

                        //Update Win Loss Ratio
                        if (losses == 0) losses = 1;
                        ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(df.format((wins+draws)/losses))));
                        service.spreadsheets().values()
                                .update(SPREAD_SHEET_ID, "G"+rowNumber, body)
                                .setValueInputOption("USER_ENTERED")
                                .execute();
                        return true;
                    }
                    rowNumber++;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addCompletedMatch(String player1, String player2, String id1, String id2, String winnerName, String loserName, boolean isDraw, Long startTime) {
        try {
            if (service == null) getSheetsService();

            Long seconds = (System.currentTimeMillis() - startTime) / 1000;
            int day = (int) TimeUnit.SECONDS.toDays(seconds);
            long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
            long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
            long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);

            String matchLength = "" + day + " days " + hours + " hours " + minute + " minutes " + second + " seconds";
            ValueRange appendBody = new ValueRange().setValues(Arrays.asList(Arrays.asList(player1, player2, id1, id2, winnerName, loserName, isDraw, matchLength, getCurrentDateTime())));

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

    private static void updateElo(int rowNumber, double newElo) throws IOException {
        ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList((int)newElo)));
        service.spreadsheets().values()
                .update(SPREAD_SHEET_ID, "C"+rowNumber, body)
                .setValueInputOption("RAW")
                .execute();
    }

    private static void changeUpdatedOnColumn(int rowNumber) throws IOException {
        ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(getCurrentDateTime())));
        service.spreadsheets().values()
                .update(SPREAD_SHEET_ID, "I"+rowNumber, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public static String getCurrentDateTime() {
        Date myDate = Date.from(Instant.now());
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm aa", Locale.ENGLISH);
        return formatter.format(myDate);
    }

    /**
     * Determine the rating constant K-factor based on current rating
     *
     * @param rating
     *            Player rating
     * @return K-factor
     */
    public static int determineK(int rating) {
        int K;
        if (rating < 2000) {
            K = 32;
        } else if (rating >= 2000 && rating < 2400) {
            K = 24;
        } else {
            K = 16;
        }
        return K;
    }
}
