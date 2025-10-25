package org.dlpk.objects;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Cristal extends Produto {
    @CsvBindByName
    String cor;
    @CsvBindByName
    String tamanho;
    @CsvBindByName
    String descricao;

    @Override
    public int getSheetsColumns() {
        return super.getSheetsColumns() + 3;
    }
}
