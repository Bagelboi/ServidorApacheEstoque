package org.dlpk.database;

import org.dlpk.objects.Colecionavel;
import org.dlpk.objects.Cristal;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Bind;

import java.util.List;
import java.util.Optional;

public interface CristalRepo {

    @SqlUpdate("INSERT INTO Cristal (sku, ean, titulo, estoque, cor, tamanho, descricao, peso) VALUES (:sku, :ean, :titulo, :estoque, :cor, :tamanho, :descricao, :peso)")
    void insert(@BindBean Cristal cristal);

    @SqlQuery("SELECT * FROM Cristal WHERE sku = :sku")
    @RegisterBeanMapper(Cristal.class)
    Optional<Cristal> findBySku(@Bind("sku") String sku);

    @SqlQuery("SELECT * FROM Cristal WHERE ean = :ean")
    @RegisterBeanMapper(Cristal.class)
    Optional<Cristal> findByEan(@Bind("ean") String ean);

    @SqlQuery("SELECT * FROM Cristal")
    @RegisterBeanMapper(Cristal.class)
    List<Cristal> findAll();

    @SqlUpdate("UPDATE Cristal SET ean = :ean, titulo = :titulo, estoque = :estoque, cor = :cor, tamanho = :tamanho, descricao = :descricao, peso = :peso WHERE sku = :sku")
    void update(@BindBean Cristal cristal);

    @SqlUpdate("DELETE FROM Cristal WHERE sku = :sku")
    void delete(@Bind("sku") String sku);

    @SqlUpdate("UPDATE Cristal SET estoque = estoque + :estoque WHERE sku = :sku")
    void updateEstoque(@Bind("sku") String sku, @Bind("estoque") Integer delta);


}
