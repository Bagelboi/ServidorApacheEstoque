package org.dlpk.sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.util.*;

public class GoogleSheetsService {

    private final Sheets sheetsService;
    private final String spreadsheetId;

    public static String toSpreadsheetNotation(int column, int row) {
        if (column <= 0 || row <= 0)
            throw new IllegalArgumentException("Column and row numbers must be positive");

        StringBuilder columnLabel = new StringBuilder();

        while (column > 0) {
            int remainder = (column - 1) % 26;
            columnLabel.insert(0, (char) ('A' + remainder));
            column = (column - 1) / 26;
        }

        return columnLabel.toString() + row;
    }

    public GoogleSheetsService(Sheets sheetsService, Optional<String> spreadsheetId) {
        this.sheetsService = sheetsService;
        if (spreadsheetId.isPresent())
            this.spreadsheetId = spreadsheetId.get();
        else
            this.spreadsheetId = ConfigFactory.load().getString("app.sheets.id");
    }

    /**
     * Get values from a sheet page and range.
     * Example: getValues("Sheet1", "A1:B10")
     */
    public List<List<Object>> getValues(String page, String range) throws IOException {
        String fullRange = page + "!" + range;
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, fullRange)
                .execute();
        return response.getValues() != null ? response.getValues() : new ArrayList<>();
    }

    /**
     * Set values into a sheet range.
     * Example: setValues("Sheet1", "A1:B1", Arrays.asList(Arrays.asList("Hello", "World")))
     */
    public void setValues(String page, String range, List<List<Object>> newValues) throws IOException {
        String fullRange = page + "!" + range;
        ValueRange body = new ValueRange().setValues(newValues);
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, fullRange, body)
                .setValueInputOption("RAW")
                .execute();
    }

    /**
     * Create a new sheet (page) inside the spreadsheet.
     * Example: createPage("NewSheet")
     */
    public void createPage(String pageName) throws IOException {
        AddSheetRequest addSheetRequest = new AddSheetRequest()
                .setProperties(new SheetProperties().setTitle(pageName));

        Request request = new Request().setAddSheet(addSheetRequest);

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                .setRequests(Collections.singletonList(request));

        sheetsService.spreadsheets().batchUpdate(spreadsheetId, body).execute();
    }

    // batch
    public Map<String, List<List<Object>>> getValuesBatch(List<String> ranges) throws IOException {
        BatchGetValuesResponse response = sheetsService.spreadsheets().values()
                .batchGet(spreadsheetId)
                .setRanges(ranges)
                .execute();

        Map<String, List<List<Object>>> result = new LinkedHashMap<>();

        List<ValueRange> valueRanges = response.getValueRanges();
        for (ValueRange vr : valueRanges) {
            String range = vr.getRange();
            List<List<Object>> values = vr.getValues();
            result.put(range, values != null ? values : new ArrayList<>());
        }

        return result;
    }

    public void setValuesBatch(Map<String, List<List<Object>>> data) throws IOException {
        List<ValueRange> dataRanges = new ArrayList<>();
        for (Map.Entry<String, List<List<Object>>> entry : data.entrySet()) {
            dataRanges.add(new ValueRange()
                    .setRange(entry.getKey())
                    .setValues(entry.getValue()));
        }

        BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                .setValueInputOption("RAW")
                .setData(dataRanges);

        sheetsService.spreadsheets().values()
                .batchUpdate(spreadsheetId, body)
                .execute();
    }

}
