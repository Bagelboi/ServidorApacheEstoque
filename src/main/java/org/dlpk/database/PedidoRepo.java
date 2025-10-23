package org.dlpk.database;

import org.dlpk.objects.Pedido;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Map;

public interface PedidoRepo {

    // --- Pedido CRUD ---

    @SqlUpdate("INSERT INTO Pedido(id, data, cliente, transporte, pagamento, nota_fiscal, coleta, OC) " +
            "VALUES(:id, :data, :cliente, :transporte, :pagamento, :nota_fiscal, :coleta, :OC)")
    void insertPedido(@BindBean Pedido pedido);

    @SqlQuery("SELECT * FROM Pedido WHERE id = :id")
    @RegisterBeanMapper(Pedido.class)
    Pedido getPedidoById(@Bind("id") Integer id);

    @SqlQuery("SELECT * FROM Pedido")
    @RegisterBeanMapper(Pedido.class)
    List<Pedido> listPedidos();

    @SqlUpdate("UPDATE Pedido SET data = :data, cliente = :cliente, transporte = :transporte, " +
            "pagamento = :pagamento, nota_fiscal = :nota_fiscal, coleta = :coleta, OC = :OC WHERE id = :id")
    void updatePedido(@BindBean Pedido pedido);

    @SqlUpdate("DELETE FROM Pedido WHERE id = :id")
    void deletePedido(@Bind("id") Integer id);

    // --- PedidoProduto CRUD ---

    @SqlUpdate("INSERT INTO PedidoProduto(pedido, produto, quantia) VALUES(:pedidoId, :sku, :quantia)")
    void insertPedidoProduto(@Bind("pedidoId") Integer pedidoId, @Bind("sku") String sku, @Bind("quantia") Integer quantia);

    @SqlQuery("SELECT produto, quantia FROM PedidoProduto WHERE pedido = :pedidoId")
    List<Map<String, Integer>> getProdutosByPedido(@Bind("pedidoId") Integer pedidoId);

    @SqlUpdate("DELETE FROM PedidoProduto WHERE pedido = :pedidoId AND produto = :sku")
    void deleteProduto(@Bind("pedidoId") Integer pedidoId, @Bind("sku") String sku);

    @SqlUpdate("DELETE FROM PedidoProduto WHERE pedido = :pedidoId")
    void deleteAllProdutos(@Bind("pedidoId") Integer pedidoId);
}

