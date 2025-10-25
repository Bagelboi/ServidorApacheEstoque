package org.dlpk.database;

import com.typesafe.config.ConfigFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public final class RepositorySingleton {
    public static Jdbi jdbi = Jdbi.create(ConfigFactory.load().getString("app.database.url") );

    static {
        jdbi.installPlugin( new SqlObjectPlugin());
        jdbi.withHandle( handle -> {
            // Produtos table
            handle.execute("CREATE TABLE IF NOT EXISTS Cristal ( sku VARCHAR PRIMARY KEY, ean VARCHAR, titulo VARCHAR, estoque INT DEFAULT 0, cor VARCHAR, tamanho VARCHAR, descricao VARCHAR, peso FLOAT);");
            handle.execute("CREATE TABLE IF NOT EXISTS Colecionavel ( sku VARCHAR PRIMARY KEY, ean VARCHAR, titulo VARCHAR, estoque INT DEFAULT 0, marca VARCHAR, peso FLOAT);");

            // Romaneio table
            handle.execute("CREATE TABLE IF NOT EXISTS romaneio ( id INTEGER PRIMARY KEY AUTOINCREMENT, numeroRomaneio INTEGER NOT NULL UNIQUE, dataEmissao DATE NOT NULL, destinatarioNome VARCHAR(255), destinatarioDocumento VARCHAR(20), descontoValorTotal FLOAT, observacoes TEXT, lancado INTEGER DEFAULT 0, contato VARCHAR(255), coleta VARCHAR(255), vendedor VARCHAR(255), transporte VARCHAR(50), oc VARCHAR(255), notaFiscal INTEGER, dataCriacao DATE, condPagamento VARCHAR(255));");
            handle.execute("CREATE TABLE IF NOT EXISTS romaneio_produto ( id INTEGER PRIMARY KEY AUTOINCREMENT, romaneio_id INTEGER NOT NULL, sku VARCHAR(50) NOT NULL, quantidade INTEGER NOT NULL, valor_unidade FLOAT, FOREIGN KEY (romaneio_id) REFERENCES romaneio(id) ON DELETE CASCADE);");


            // Eventos table
            handle.execute("CREATE TABLE IF NOT EXISTS evento_estoque ( id INTEGER PRIMARY KEY AUTOINCREMENT, sku VARCHAR(50), data DATE, tipo VARCHAR(10), quantidade INT, origem VARCHAR(50) );");


            //handle.execute("CREATE TABLE IF NOT EXISTS Pedido (id INT PRIMARY KEY, data DATE, cliente VARCHAR(255), transporte VARCHAR(255), pagamento VARCHAR(255), nota_fiscal INT, coleta INT, OC INT)");

            //handle.execute("CREATE TABLE IF NOT EXISTS PedidoProduto(id INTEGER PRIMARY KEY AUTOINCREMENT, pedido INTEGER, produto VARCHAR, quantia INT, FOREIGN KEY(pedido) REFERENCES Pedido(id) ON DELETE CASCADE)");

            return null;
        } );
    }


}
