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
            handle.execute("CREATE TABLE IF NOT EXISTS Colecionavel (sku VARCHAR PRIMARY KEY, ean VARCHAR, titulo VARCHAR, estoque INT DEFAULT 0, marca VARCHAR, peso FLOAT, precoPadrao FLOAT);");
            handle.execute("CREATE TABLE IF NOT EXISTS Cristal (sku VARCHAR PRIMARY KEY, ean VARCHAR, titulo VARCHAR, estoque INT DEFAULT 0, cor VARCHAR, tamanho VARCHAR, descricao VARCHAR, peso FLOAT, precoPadrao FLOAT);");

            // Romaneio table
            handle.execute("CREATE TABLE IF NOT EXISTS romaneio ( id INTEGER PRIMARY KEY AUTOINCREMENT, numeroRomaneio INTEGER NOT NULL UNIQUE, dataEmissao DATE, destinatarioNome VARCHAR(255), destinatarioDocumento VARCHAR(20), descontoValorTotal FLOAT, observacoes TEXT, lancado INTEGER DEFAULT 0, contato VARCHAR(255), coleta VARCHAR(255), vendedor VARCHAR(255), transporte VARCHAR(50), oc VARCHAR(255), notaFiscal INTEGER, dataCriacao DATE, condPagamento VARCHAR(255));");
            handle.execute("CREATE TABLE IF NOT EXISTS romaneio_produto ( id INTEGER PRIMARY KEY AUTOINCREMENT, romaneio_id INTEGER NOT NULL, sku VARCHAR(50) NOT NULL, quantidade INTEGER NOT NULL, valor_unidade FLOAT, FOREIGN KEY (romaneio_id) REFERENCES romaneio(id) ON DELETE CASCADE);");

            // Eventos table
            handle.execute("CREATE TABLE IF NOT EXISTS evento_estoque ( id INTEGER PRIMARY KEY AUTOINCREMENT, sku VARCHAR(50), data DATE, tipo VARCHAR(10), quantidade INT, origem VARCHAR(50) );");
            handle.execute("CREATE TABLE IF NOT EXISTS evento_conferencia ( id INTEGER PRIMARY KEY AUTOINCREMENT, sku VARCHAR(50), data DATE, estoque_novo INTEGER, origem VARCHAR(50) );");

            //handle.execute("CREATE TABLE IF NOT EXISTS Pedido (id INT PRIMARY KEY, data DATE, cliente VARCHAR(255), transporte VARCHAR(255), pagamento VARCHAR(255), nota_fiscal INT, coleta INT, OC INT)");

            //handle.execute("CREATE TABLE IF NOT EXISTS PedidoProduto(id INTEGER PRIMARY KEY AUTOINCREMENT, pedido INTEGER, produto VARCHAR, quantia INT, FOREIGN KEY(pedido) REFERENCES Pedido(id) ON DELETE CASCADE)");

            return null;
        } );
    }

    public static void clearDatabase() {
        jdbi.useHandle(handle -> {
            List<String> tables = handle.createQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';")
                    .mapTo(String.class)
                    .list();

            for (String table : tables) {
                handle.execute("DROP TABLE IF EXISTS " + table);
            }
        });
        System.out.println("[DB] Cleared all tables.");
    }

    /**
     * Saves a snapshot (copy) of the current SQLite database file.
     */
    public static String saveSnapshot() {
        String dbUrl = ConfigFactory.load().getString("app.database.url");
        if (!dbUrl.startsWith("jdbc:sqlite:")) {
            throw new IllegalStateException("Snapshots are only supported for SQLite databases.");
        }

        String dbPath = dbUrl.replace("jdbc:sqlite:", "");
        Path source = Paths.get(dbPath);
        if (!Files.exists(source)) {
            throw new IllegalStateException("Database file does not exist: " + dbPath);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String snapshotName = "snapshot_" + timestamp + ".db";
        Path snapshotPath = Paths.get(snapshotName);

        try {
            Files.copy(source, snapshotPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[DB] Snapshot saved: " + snapshotPath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save snapshot", e);
        }

        return snapshotName;
    }

    /**
     * Loads (restores) a snapshot by replacing the current DB file with it.
     */
    public static void loadSnapshot(String snapshotName) {
        String dbUrl = ConfigFactory.load().getString("app.database.url");
        if (!dbUrl.startsWith("jdbc:sqlite:")) {
            throw new IllegalStateException("Snapshots are only supported for SQLite databases.");
        }

        String dbPath = dbUrl.replace("jdbc:sqlite:", "");
        Path target = Paths.get(dbPath);
        Path snapshotPath = Paths.get(snapshotName);

        if (!Files.exists(snapshotPath)) {
            throw new IllegalArgumentException("Snapshot file not found: " + snapshotPath);
        }

        try {
            // Replace the database file
            Files.copy(snapshotPath, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[DB] Snapshot loaded: " + snapshotName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load snapshot", e);
        }
    }
}
