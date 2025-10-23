package org.dlpk.database;

import com.typesafe.config.ConfigFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public final class RepositorySingleton {
    public static Jdbi jdbi = Jdbi.create(ConfigFactory.load().getString("app.database.url") );

    static {
        jdbi.installPlugin( new SqlObjectPlugin());
        jdbi.withHandle( handle -> {
            handle.execute("CREATE TABLE IF NOT EXISTS Colecionavel ( sku VARCHAR PRIMARY KEY, ean VARCHAR, titulo VARCHAR, estoque INT DEFAULT 0, marca VARCHAR)");

            // Cristal table
            handle.execute("CREATE TABLE IF NOT EXISTS Cristal ( sku VARCHAR PRIMARY KEY, ean VARCHAR, titulo VARCHAR, estoque INT DEFAULT 0, cor VARCHAR, tamanho VARCHAR, descricao VARCHAR)");

            handle.execute("CREATE TABLE IF NOT EXISTS Pedido (id INT PRIMARY KEY, data DATE, cliente VARCHAR(255), transporte VARCHAR(255), pagamento VARCHAR(255), nota_fiscal INT, coleta INT, OC INT)");

            handle.execute("CREATE TABLE IF NOT EXISTS PedidoProduto(id INTEGER PRIMARY KEY AUTOINCREMENT, pedido INTEGER, produto VARCHAR, quantia INT, FOREIGN KEY(pedido) REFERENCES Pedido(id) ON DELETE CASCADE)");

            return null;
        } );
    }


}
