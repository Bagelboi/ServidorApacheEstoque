package org.dlpk.objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dlpk.enums.EVENTO_ESTOQUE;

import java.sql.Date;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class EventoEstoque {
    Integer id;
    String sku;
    LocalDate data;
    EVENTO_ESTOQUE tipo;
    Integer quantidade;
    String origem;
}
