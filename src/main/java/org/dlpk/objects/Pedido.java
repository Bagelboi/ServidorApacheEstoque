package org.dlpk.objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dlpk.sheets.SheetsObj;

import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
public class Pedido implements SheetsObj {
    Integer id;
    Date data;
    String cliente;
    String transporte;
    String pagamento;
    Integer nota_fiscal;
    Integer coleta;
    Double valor;
    Integer OC;
    HashMap<String, Integer> produtos; //sku e quantidade;

    public int getSheetsColumns() {
        return 8 + produtos.keySet().size();
    }

}
