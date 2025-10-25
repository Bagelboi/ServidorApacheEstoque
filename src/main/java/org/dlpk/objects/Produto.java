package org.dlpk.objects;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dlpk.sheets.SheetsObj;

@Data
@NoArgsConstructor
public class Produto implements SheetsObj {

    @CsvBindByName
    String sku;
    @CsvBindByName
    String ean;
    @CsvBindByName
    String titulo;
    @CsvBindByName
    Integer estoque;
    @CsvBindByName
    Float peso;
    public int getSheetsColumns() {
        return 3;
    }
}
