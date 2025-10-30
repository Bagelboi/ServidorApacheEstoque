package org.dlpk.objects;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class EventoConferencia {
    Integer id;
    String sku;
    LocalDate data;
    Integer estoque_novo;
    String origem;
}
