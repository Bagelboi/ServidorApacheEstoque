package org.dlpk.objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Colecionavel extends Produto{
    String marca;

    @Override
    public int getSheetsColumns() {
        return super.getSheetsColumns() + 3;
    }
}
