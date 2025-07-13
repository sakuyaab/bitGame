package Bit;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class SheetLogger {
    private static final String APPLICATION_NAME = "BitRecord";
    private static final String SPREADSHEET_ID = "見せらせない😢";//
    private static final String RANGE = "'シート1'!A2"; 

    public static void appendLog(int trialCount, int secondsElapsed) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();

        int minutes = secondsElapsed / 60;
        int seconds = secondsElapsed % 60;

        List<List<Object>> values = List.of(
                List.of(trialCount + "回", String.format("%d分%d秒", minutes, seconds)));

        ValueRange body = new ValueRange().setValues(values);

        service.spreadsheets().values()
                .append(SPREADSHEET_ID, RANGE, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public static void writeHeader() throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();

        List<List<Object>> headers = List.of(
                List.of("試行回数", "クリア時間", "", "最小試行回数", "最短クリア時間"));

        ValueRange headerBody = new ValueRange().setValues(headers);
        service.spreadsheets().values()
                .update(SPREADSHEET_ID, "シート1!A1:E1", headerBody)
                .setValueInputOption("RAW")
                .execute();
    }

    private static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        FileInputStream inputStream = new FileInputStream("見せらせない😢"); 
        var credentials = ServiceAccountCredentials
                .fromStream(inputStream)
                .createScoped(List.of(SheetsScopes.SPREADSHEETS));

        var transport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = GsonFactory.getDefaultInstance();

        return new Sheets.Builder(transport, jsonFactory, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void writeSummary(int trialCount, int secondsElapsed) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();

        // A列の最小試行回数を取得
        ValueRange existing = service.spreadsheets().values()
                .get(SPREADSHEET_ID, "A2:A")
                .execute();

        int minTrial = trialCount;
        if (existing.getValues() != null) {
            for (List<Object> row : existing.getValues()) {
                try {
                    String value = row.get(0).toString().replace("回", "");
                    int current = Integer.parseInt(value);
                    if (current < minTrial)
                        minTrial = current;
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // B列の最短クリア時間を取得（秒で比較）
        ValueRange times = service.spreadsheets().values()
                .get(SPREADSHEET_ID, "B2:B")
                .execute();

        int minSeconds = secondsElapsed;
        if (times.getValues() != null) {
            for (List<Object> row : times.getValues()) {
                try {
                    String value = row.get(0).toString(); 
                    int min = value.indexOf("分");
                    int sec = value.indexOf("秒");
                    int m = Integer.parseInt(value.substring(0, min));
                    int s = Integer.parseInt(value.substring(min + 1, sec));
                    int total = m * 60 + s;
                    if (total < minSeconds)
                        minSeconds = total;
                } catch (Exception ignored) {
                }
            }
        }

        // 書き込むデータ（D2とE2に）
        String minTrialStr = minTrial + "回";
        String minTimeStr = String.format("%d分%d秒", minSeconds / 60, minSeconds % 60);

        ValueRange summary = new ValueRange().setValues(List.of(
                List.of(minTrialStr, minTimeStr)));

        service.spreadsheets().values()
                .update(SPREADSHEET_ID, "D2:E2", summary)
                .setValueInputOption("RAW")
                .execute();
    }

    public static String[] getBestRecords() throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();

        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, "シート1!D2:E2")
                .execute();

        List<List<Object>> values = response.getValues();
        String trial = "未記録";
        String time = "未記録";

        if (values != null && !values.isEmpty() && values.get(0).size() >= 2) {
            trial = values.get(0).get(0).toString(); 
            time = values.get(0).get(1).toString(); 
        }

        return new String[] { trial, time };
    }

}
