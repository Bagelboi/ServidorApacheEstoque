// ...existing code...
package org.dlpk.spark;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import org.dlpk.database.ColecionavelRepo;
import org.dlpk.database.CristalRepo;
import org.dlpk.database.RepositorySingleton;
import org.dlpk.objects.Produto;

public class ProdutoController {

    private final Gson gson = new Gson();

    public void setupRoutes() {
        // Get produto by SKU
        get("/produto/:sku", (req, res) -> {
            String sku = req.params("sku");
            Optional<?> produto = findProduto(sku);
            Map<String, Object> model = new HashMap<>();
            if (produto.isPresent()) {
                Produto p = (Produto) produto.get();
                model.put("exists", true);
                model.put("sku", p.getSku());
                model.put("nome", p.getTitulo());
                model.put("estoque", p.getEstoque());
                model.put("peso", p.getPeso());
            } else {
                model.put("exists", false);
            }
            res.type("application/json");
            return gson.toJson(model);
        });

    }

 
    public void addEstoque(String sku, Integer estoque_novo) {
        if (sku.startsWith("FU")) {
            RepositorySingleton.jdbi.useExtension(ColecionavelRepo.class, dao -> dao.updateEstoque(sku, estoque_novo));
        } else if (sku.startsWith("CR")) {
            RepositorySingleton.jdbi.useExtension(CristalRepo.class, dao -> dao.updateEstoque(sku, estoque_novo));
        }
    }

    public void removeEstoque(String sku, Integer estoque_novo) {
        addEstoque(sku, -1 * estoque_novo);
    }

    public Optional<?> findProduto(String sku) {
        if (sku == null) return Optional.empty();
        if (sku.startsWith("FU")) {
            return RepositorySingleton.jdbi.withExtension(ColecionavelRepo.class, dao -> dao.findBySku(sku));
        }
        if (sku.startsWith("CR")) {
            return RepositorySingleton.jdbi.withExtension(CristalRepo.class, dao -> dao.findBySku(sku));
        }
        return Optional.empty();
    }

}