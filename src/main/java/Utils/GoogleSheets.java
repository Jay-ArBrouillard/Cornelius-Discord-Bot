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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GoogleSheets {
    private static String APPLICATION_NAME = "players";
    private static String SPREAD_SHEET_ID = System.getenv("SPREAD_SHEET_ID");
    private static HttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Sheets service;

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
        InputStream in = GoogleSheets.class.getResourceAsStream("/google-credentials.json");
        Credential credential = GoogleCredential.fromStream(in, HTTP_TRANSPORT, JSON_FACTORY).createScoped(SCOPES);
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

    public static boolean addUser(String id, String name) {
        try {
            if (service == null) getSheetsService();

            //If user does not exist add them
            ValueRange response = service.spreadsheets().values()
                    .get(SPREAD_SHEET_ID, "players")
                    .execute();
            List<List<Object>> allUsers = response.getValues();
            for (List row : allUsers) {
                if (row.get(0).equals(id)) {
                    return false; //User exists already
                }
            }

            String now = getCurrentDateTime();

            ValueRange appendBody = new ValueRange()
                .setValues(Arrays.asList(
                        Arrays.asList(id, name, "0", "0", "0", "0", now, now)
                ));
            service.spreadsheets().values()
                .append(SPREAD_SHEET_ID, "players", appendBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUser(String id, boolean isWin, boolean isDraw) {
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
                        int wins = Integer.parseInt((String) row.get(2));
                        double losses = Double.parseDouble((String) row.get(3));
                        int draws = Integer.parseInt((String) row.get(4));

                        if (isWin) {
                            wins++;
                            ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(wins)));
                            service.spreadsheets().values()
                                    .update(SPREAD_SHEET_ID, "C"+rowNumber, body)
                                    .setValueInputOption("RAW")
                                    .execute();
                            if (losses == 0) losses = 1;
                            body = new ValueRange().setValues(Arrays.asList(Arrays.asList((wins+draws)/losses)));
                            service.spreadsheets().values()
                                    .update(SPREAD_SHEET_ID, "F"+rowNumber, body)
                                    .setValueInputOption("RAW")
                                    .execute();
                            changeUpdatedOnColumn(rowNumber);
                            return true;
                        }
                        else if (!isWin && !isDraw) {
                            losses++;
                            ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(losses)));
                            service.spreadsheets().values()
                                    .update(SPREAD_SHEET_ID, "D"+rowNumber, body)
                                    .setValueInputOption("RAW")
                                    .execute();
                            body = new ValueRange().setValues(Arrays.asList(Arrays.asList((wins+draws)/losses)));
                            service.spreadsheets().values()
                                    .update(SPREAD_SHEET_ID, "F"+rowNumber, body)
                                    .setValueInputOption("RAW")
                                    .execute();
                            changeUpdatedOnColumn(rowNumber);
                            return true;
                        }
                        else if (isDraw) {
                            draws++;
                            ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(draws)));
                            service.spreadsheets().values()
                                    .update(SPREAD_SHEET_ID, "E"+rowNumber, body)
                                    .setValueInputOption("RAW")
                                    .execute();
                            if (losses == 0) losses = 1;
                            body = new ValueRange().setValues(Arrays.asList(Arrays.asList((wins+draws)/losses)));
                            service.spreadsheets().values()
                                    .update(SPREAD_SHEET_ID, "F"+rowNumber, body)
                                    .setValueInputOption("RAW")
                                    .execute();
                            changeUpdatedOnColumn(rowNumber);
                            return true;
                        }
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
            String date = getCurrentDateTime();

            ValueRange appendBody = new ValueRange().setValues(Arrays.asList(Arrays.asList(player1, player2, id1, id2, winnerName, loserName, isDraw, matchLength, date)));

            service.spreadsheets().values()
                    .append(SPREAD_SHEET_ID, "matches", appendBody)
                    .setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS")
                    .setIncludeValuesInResponse(true)
                    .execute();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void changeUpdatedOnColumn(int rowNumber) throws IOException {
        ValueRange body = new ValueRange().setValues(Arrays.asList(Arrays.asList(getCurrentDateTime())));
        service.spreadsheets().values()
                .update(SPREAD_SHEET_ID, "H"+rowNumber, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        String datetime = sdf.format(d);
        return datetime;
    }
}
