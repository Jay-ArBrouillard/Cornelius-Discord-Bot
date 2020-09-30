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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
            System.out.println(e.getMessage());
            //Do nothing
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
        InputStream in = GoogleSheets.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            System.out.println(CREDENTIALS_FILE_PATH + " didnt work trying new");
            System.out.println(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
            System.out.println(System.getenv("GOOGLE_CREDENTIALS"));
            System.out.println(System.getenv("google-credentials.json"));
            in = GoogleSheets.class.getResourceAsStream(System.getenv("GOOGLE_CREDENTIALS"));
            if  (in == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
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
            //Do nothing
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
            //Do nothing
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
