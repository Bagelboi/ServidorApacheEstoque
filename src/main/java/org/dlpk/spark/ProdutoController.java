// ...existing code...
package org.dlpk.spark;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static spark.Spark.*;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.google.gson.Gson;
import org.dlpk.database.ColecionavelRepo;
import org.dlpk.database.CristalRepo;
import org.dlpk.database.RepositorySingleton;
import org.dlpk.objects.Colecionavel;
import org.dlpk.objects.Cristal;
import org.dlpk.objects.Produto;
import spark.Request;

import javax.servlet.http.Part;
import javax.swing.text.html.Option;

public class ProdutoController {

    private final Gson gson = new Gson();

    public void setupRoutes() {
                // Get produto by SKU
        get("/produto/sku/:sku", (req, res) -> {
            String sku = req.params("sku");
            Optional<?> produto = findProduto(sku);
            return gson.toJson(toJsonExists(produto));
        });


        get("/produto/ean/:ean", (req, res) -> {
            String ean = req.params("ean");
            Optional<?> produto = findProdutoByEAN(ean);
            res.type("application/json");
            return gson.toJson(toJsonExists(produto));
        });
    }

    public HashMap<String, Object> toJsonExists(Optional<?> produto) {
        HashMap<String, Object> model = new HashMap<>();
        if (produto.isPresent()) {
            Produto p = (Produto) produto.get();
            model.put("exists", true);
            model.put("sku", p.getSku());
            model.put("ean", p.getEan());
            model.put("nome", p.getTitulo());
            model.put("estoque", p.getEstoque());
            model.put("peso", p.getPeso());
            model.put("precoPadrao", p.getPrecoPadrao());
        } else {
            model.put("exists", false);
        }
        return model;
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

    public Optional<?> findProdutoByEAN(String EAN) {
        if (EAN == null) return Optional.empty();

        Optional<?> produto = Optional.empty();

        if (!produto.isPresent()) //colecionavel
            produto = RepositorySingleton.jdbi.withExtension(ColecionavelRepo.class, dao -> dao.findByEan(EAN));
        if (!produto.isPresent()) //cristal
            produto = RepositorySingleton.jdbi.withExtension(CristalRepo.class, dao -> dao.findBySku(EAN));

        return produto;
    }

    public String getProdutoEAN(String sku) {
        Optional<?> p = findProduto(sku);
        if (p.isPresent()) {
            System.out.println( ((Produto) p.get()).getEan() );
            return ((Produto) p.get()).getEan();
        }
        return "";
    }

    public static void extractProduto(Request req, String sku_pre, Produto c) {
        c.setSku(sku_pre + req.queryParams("sku"));
        c.setPeso(parseFloat(req.queryParams( "peso") ));
        c.setEan(req.queryParams("ean"));
        c.setTitulo(req.queryParams("titulo"));
        c.setEstoque(parseInt(req.queryParams("estoque")));
        c.setPrecoPadrao(parseFloat(req.queryParams("precoPadrao")));
    }


}