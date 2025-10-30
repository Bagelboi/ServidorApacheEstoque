package org.dlpk.database;

import org.dlpk.objects.Colecionavel;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Bind;

import java.util.List;
import java.util.Optional;

public interface ColecionavelRepo {

    @SqlQuery("SELECT * FROM Colecionavel WHERE sku = :sku")
    @RegisterBeanMapper(Colecionavel.class)
    Optional<Colecionavel> findBySku(@Bind("sku") String sku);

    @SqlQuery("SELECT * FROM Colecionavel WHERE ean = :ean")
    @RegisterBeanMapper(Colecionavel.class)
    Optional<Colecionavel> findByEan(@Bind("ean") String ean);

    @SqlQuery("SELECT * FROM Colecionavel")
    @RegisterBeanMapper(Colecionavel.class)
    List<Colecionavel> findAll();

        @SqlUpdate("INSERT INTO Colecionavel (sku, ean, titulo, estoque, marca, peso, precoPadrao) VALUES (:sku, :ean, :titulo, :estoque, :marca, :peso, :precoPadrao)")
        void insert(@BindBean Colecionavel colecionavel);

        @SqlUpdate("UPDATE Colecionavel SET ean = :ean, titulo = :titulo, estoque = :estoque, marca = :marca, peso = :peso, precoPadrao = :precoPadrao WHERE sku = :sku")
        void update(@BindBean Colecionavel colecionavel);

    @SqlUpdate("DELETE FROM Colecionavel WHERE sku = :sku")
    void delete(@Bind("sku") String sku);

    @SqlUpdate("UPDATE Colecionavel SET estoque = estoque + :estoque WHERE sku = :sku")
    void updateEstoque(@Bind("sku") String sku, @Bind("estoque") Integer delta);


}
