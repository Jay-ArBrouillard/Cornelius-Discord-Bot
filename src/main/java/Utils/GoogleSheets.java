package Utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
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
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static Sheets service;

    public GoogleSheets() {
        connect();
    }

    public static void connect() {
        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            final String range = "players!A1:F2";
            service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            service.spreadsheets().values()
                    .get(SPREAD_SHEET_ID, range)
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "google-credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
//        File file = new File("credentials.json");
//        FileWriter f2 = null;
//
//        try {
//            f2 = new FileWriter(file,false);
//            f2.write(System.getenv("GOOGLE_CREDENTIALS").toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            f2.close();
//        }

        System.out.println("fileExists:"+new File("google-credentials.json").exists());
        InputStream in = GoogleSheets.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//        if (in == null) {
//            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static boolean addUser(String id, String name) {
        try {
            if (service == null) connect();

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
            if (service == null) connect();
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
            if (service == null) connect();

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
