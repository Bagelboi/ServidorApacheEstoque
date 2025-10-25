package org.dlpk.database;

import org.dlpk.objects.EventoEstoque;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import java.util.List;

public interface EventoRepo {
        //evento estoque
        @SqlUpdate("INSERT INTO evento_estoque (sku, data, tipo, quantidade, origem) VALUES (:sku, :data, :tipo, :quantidade, :origem)")
        @GetGeneratedKeys
        int insertEstoque(@BindBean EventoEstoque evento);

        @SqlQuery("SELECT * FROM evento_estoque WHERE id = :id")
        @RegisterBeanMapper(EventoEstoque.class)
        EventoEstoque findByIdEstoque(@Bind("id") Integer id);

        @SqlQuery("SELECT * FROM evento_estoque WHERE sku = :sku")
        @RegisterBeanMapper(EventoEstoque.class)
        List<EventoEstoque>  findAllBySkuEstoque(@Bind("sku") String sku);

        @SqlQuery("SELECT * FROM evento_estoque")
            @RegisterBeanMapper(EventoEstoque.class)
            List<EventoEstoque> findAllEstoque();

        @SqlQuery("SELECT * FROM evento_estoque WHERE origem = :org")
        @RegisterBeanMapper(EventoEstoque.class)
        List<EventoEstoque>  findAllByOrigemEstoque(@Bind("org") String origem);
}
