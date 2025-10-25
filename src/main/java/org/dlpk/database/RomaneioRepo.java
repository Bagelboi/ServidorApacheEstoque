package org.dlpk.database;

import org.dlpk.objects.Romaneio;
import org.dlpk.objects.RomaneioProduto;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;



public interface RomaneioRepo {

    // --- ROMANEIO ---

    @SqlUpdate("INSERT INTO romaneio (numeroRomaneio, dataEmissao, destinatarioNome, destinatarioDocumento, descontoValorTotal, observacoes, lancado, contato, coleta, vendedor, transporte, oc, notaFiscal, dataCriacao, condPagamento) VALUES (:numeroRomaneio, :dataEmissao, :destinatarioNome, :destinatarioDocumento, :descontoValorTotal, :observacoes, :lancado, :contato, :coleta, :vendedor, :transporte, :oc, :notaFiscal, :dataCriacao, :condPagamento)")
    @GetGeneratedKeys
    int insertRomaneio(@BindBean Romaneio romaneio);

    @SqlQuery("SELECT * FROM romaneio WHERE id = :id")
    @RegisterBeanMapper(Romaneio.class)
    Romaneio findRomaneioById(@Bind("id") int id);

    @SqlQuery("SELECT * FROM romaneio WHERE numeroRomaneio = :num")
    @RegisterBeanMapper(Romaneio.class)
    Romaneio findRomaneioByNumero(@Bind("num") int num);

    @SqlQuery("SELECT * FROM romaneio")
    @RegisterBeanMapper(Romaneio.class)
    List<Romaneio> findAllRomaneios();

    @SqlUpdate("DELETE FROM romaneio WHERE id = :id")
    void deleteRomaneio(@Bind("id") int id);

    @SqlUpdate("UPDATE romaneio SET lancado = 1, dataEmissao = :data_emissao WHERE id = :id")
    void lancarRomaneio(@Bind("id") int id, @Bind("data_emissao") LocalDate data_emissao);

    @SqlUpdate("UPDATE romaneio SET lancado = 0 WHERE id = :id")
    void undoRomaneio(@Bind("id") int id);
    // --- ROMANEIO PRODUTO ---

    @SqlUpdate("INSERT INTO romaneio_produto (romaneio_id, sku, quantidade, valor_unidade) VALUES (:romaneio_id, :sku, :quantidade, :valor_unidade)")
    @GetGeneratedKeys
    int insertProduto(@BindBean RomaneioProduto produto);

    @SqlQuery("SELECT * FROM romaneio_produto WHERE romaneio_id = :romaneio_id")
    @RegisterBeanMapper(RomaneioProduto.class)
    List<RomaneioProduto> findProdutosByRomaneioId(@Bind("romaneio_id") int romaneio_id);

    @SqlUpdate("DELETE FROM romaneio_produto WHERE romaneio_id = :romaneio_id")
    void deleteProdutosByRomaneioId(@Bind("romaneio_id") int romaneio_id);


    default Romaneio getRomaneioWithProdutos(int id) {
        Romaneio romaneio = findRomaneioById(id);
        if (romaneio != null) {
            List<RomaneioProduto> produtos = findProdutosByRomaneioId(id);
            romaneio.setProdutos(produtos);
        }
        return romaneio;
    }

    default int createRomaneioWithProdutos(Romaneio romaneio) {
        int romaneioId = insertRomaneio(romaneio);
        if (romaneio.getProdutos() != null) {
            for (RomaneioProduto p : romaneio.getProdutos()) {
                p.setRomaneio_id(romaneioId);
                insertProduto(p);
            }
        }
        return romaneioId;
    }

    default void deleteRomaneioWithProdutos(int id) {
        deleteProdutosByRomaneioId(id);
        deleteRomaneio(id);
    }

    default void updateRomaneioWithProdutos(Romaneio rom) {
        deleteProdutosByRomaneioId(rom.getId());
        deleteRomaneio(rom.getId());
        createRomaneioWithProdutos(rom);
    }
}
