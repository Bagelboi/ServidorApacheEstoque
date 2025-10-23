package org.dlpk.objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Cristal extends Produto {
    String cor;
    String tamanho;
    String descricao;

    @Override
    public int getSheetsColumns() {
        return super.getSheetsColumns() + 3;
    }
}
