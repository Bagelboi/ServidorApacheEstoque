package org.dlpk.objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dlpk.enums.EVENTO_ESTOQUE;
import org.dlpk.enums.TRANSPORTE;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Romaneio {
     Integer id;
     Integer numeroRomaneio;
     LocalDate dataEmissao;
    // Dados do destinatário
     String destinatarioNome;
     String destinatarioDocumento; //cpf ou cnpj

     List<RomaneioProduto> produtos;
     
     String contato;
     String coleta;
     String vendedor;
     TRANSPORTE transporte;
     String oc;
     Integer notaFiscal;
     LocalDate dataCriacao;
     String condPagamento;

     Float descontoValorTotal;
     boolean lancado;
     String observacoes;

    public Float getPrecoTotal() {
        Float total = 0.0f;
        if (produtos != null) {
            for (RomaneioProduto p : produtos) {
                total += p.getValor_unidade() * p.getQuantidade();
            }
        }
        return total;
    }


    public List<EventoEstoque> asEventoEstoque(EVENTO_ESTOQUE tipo) {
        List<EventoEstoque> eventos = new ArrayList<>();

        if (produtos != null) {
            for (RomaneioProduto p : produtos) {
                EventoEstoque evento = new EventoEstoque();
                evento.setSku( p.getSku() );
                evento.setData(dataEmissao);
                evento.setTipo(tipo);
                evento.setQuantidade(p.getQuantidade());
                evento.setOrigem("Romaneio #" + numeroRomaneio);
                eventos.add(evento);
            }
        }

        return eventos;
    }
}