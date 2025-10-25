package org.dlpk.objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RomaneioProduto {
    Integer id;
    Integer romaneio_id;
    String sku;
    Integer quantidade;
    Float valor_unidade;
}
