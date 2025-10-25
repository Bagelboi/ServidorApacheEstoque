package org.dlpk.objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dlpk.sheets.SheetsObj;

@Data
@NoArgsConstructor
public class Produto implements SheetsObj {
    String sku;
    String ean;
    String titulo;
    Integer estoque;
    Float peso;
    public int getSheetsColumns() {
        return 3;
    }
}
