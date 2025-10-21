package org.dlpk.sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import java.io.IOException;
import java.util.*;

public class GoogleSheetsService {

    private final Sheets sheetsService;
    private final String spreadsheetId;

    public GoogleSheetsService(Sheets sheetsService, String spreadsheetId) {
        this.sheetsService = sheetsService;
        this.spreadsheetId = spreadsheetId;
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
}
