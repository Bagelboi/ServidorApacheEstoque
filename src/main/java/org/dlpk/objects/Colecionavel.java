package org.dlpk.objects;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Colecionavel extends Produto{
    @CsvBindByName
    String marca;

    @Override
    public int getSheetsColumns() {
        return super.getSheetsColumns() + 3;
    }
}
