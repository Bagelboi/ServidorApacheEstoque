package org.dlpk.spark;

import lombok.RequiredArgsConstructor;
import org.dlpk.database.ColecionavelRepo;
import org.dlpk.database.CristalRepo;
import org.dlpk.database.PedidoRepo;
import org.dlpk.database.RepositorySingleton;
import org.dlpk.objects.Pedido;
import org.dlpk.objects.Produto;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.sql.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spark.Spark.*;
import static java.lang.Integer.parseInt;

@RequiredArgsConstructor
public class PedidoController {

    private Optional<?> findProduto(String sku) {
        if (sku.startsWith("FU")) {
            return RepositorySingleton.jdbi.withExtension(ColecionavelRepo.class, dao -> dao.findBySku(sku));
        }
        if (sku.startsWith("CR")) {
            return RepositorySingleton.jdbi.withExtension(CristalRepo.class, dao -> dao.findBySku(sku));
        }
        return Optional.empty();
    }

    private void addEstoque(String sku, Integer estoque_novo) {
        if (sku.startsWith("FU")) {
            RepositorySingleton.jdbi.useExtension(ColecionavelRepo.class, dao -> dao.updateEstoque(sku, estoque_novo));
        }
        if (sku.startsWith("CR")) {
            RepositorySingleton.jdbi.useExtension(CristalRepo.class, dao -> dao.updateEstoque(sku, estoque_novo));
        }
    }

    private void removeEstoque(String sku, Integer estoque_novo) {
        addEstoque(sku, -1*estoque_novo);
    }


    private void restoreEstoqueFromDeletedPedido(Pedido pedido) {
        if (pedido.getProdutos() != null) {
            for (Map.Entry<String, Integer> e : pedido.getProdutos().entrySet()) {
                addEstoque( e.getKey(), e.getValue() );
            }
        }
    }

    private void populatePedidoWithProdutos(Pedido pedido) {
        // load produtos map
        List<Map<String, Integer>> produtosList = RepositorySingleton.jdbi.withExtension(PedidoRepo.class, dao ->
                dao.getProdutosByPedido(pedido.getId())
        );

        HashMap<String, Integer> produtosMap = new LinkedHashMap<>();
        for (Map<String, Integer> item : produtosList) {
            // each Map is expected to have a single entry: produto => quantia
            produtosMap.putAll(item);
        }
        pedido.setProdutos(produtosMap);

    }

    public void setupRoutes() {

        // --- Create form ---
        get("/pedido/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("action", "/pedido");
            model.put("method", "post");
            model.put("buttonText", "Criar Pedido");
            return render(model, "pedido-form.hbs");
        });

        // --- Edit form ---
        get("/pedido/edit/:id", (req, res) -> {
            Integer id = parseInt(req.params("id"));
            Pedido pedido = RepositorySingleton.jdbi.withExtension(PedidoRepo.class, dao -> dao.getPedidoById(id));

            if (pedido == null) {
                res.redirect("/pedido");
                return null;
            }

            populatePedidoWithProdutos(pedido);

            Map<String, Object> model = new HashMap<>();
            model.put("pedido", pedido);
            model.put("action", "/pedido/update/" + id);
            model.put("method", "post");
            model.put("buttonText", "Salvar Alterações");
            return render(model, "pedido-form.hbs");
        });

        // --- Update submission ---
        post("/pedido/update/:id", (req, res) -> {
            Pedido pedido = extractPedido(req);
            Integer id = pedido.getId();

            // update pedido, clear produtos, reinsert produtos using useExtension
            RepositorySingleton.jdbi.useExtension(PedidoRepo.class, dao -> {
                restoreEstoqueFromDeletedPedido( dao.getPedidoById(id) );
                dao.updatePedido(pedido);
                dao.deleteAllProdutos(id);
                if (pedido.getProdutos() != null) {
                    for (Map.Entry<String, Integer> e : pedido.getProdutos().entrySet()) {
                        removeEstoque( e.getKey(), e.getValue() );
                        dao.insertPedidoProduto(id, e.getKey(), e.getValue());
                    }
                }
            });

            res.redirect("/pedido");
            return null;
        });

        // --- Delete Pedido ---
        post("/pedido/delete/:id", (req, res) -> {
            Integer id = parseInt(req.params("id"));
            RepositorySingleton.jdbi.useExtension(PedidoRepo.class, dao -> {
                restoreEstoqueFromDeletedPedido( dao.getPedidoById(id) );
                dao.deleteAllProdutos(id);
                dao.deletePedido(id);
            });
            res.redirect("/pedido");
            return null;
        });

        // --- View single Pedido (reusing list template) ---
        get("/pedido/:id", (req, res) -> {
            Integer id = parseInt(req.params("id"));
            Pedido pedido = RepositorySingleton.jdbi.withExtension(PedidoRepo.class, dao -> dao.getPedidoById(id));

            if (pedido == null) {
                res.redirect("/pedido");
                return null;
            }

            populatePedidoWithProdutos(pedido);

            Map<String, Object> model = new HashMap<>();
            model.put("pedidos", Collections.singletonList(pedido));
            return render(model, "pedidos-list.hbs");
        });

        // --- List all Pedidos ---
        get("/pedido", (req, res) -> {
            List<Pedido> pedidos = RepositorySingleton.jdbi.withExtension(PedidoRepo.class, PedidoRepo::listPedidos);

            // attach produtos for each pedido (uses withExtension for queries)
            for (Pedido p : pedidos) {
                populatePedidoWithProdutos(p);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("pedidos", pedidos);
            return render(model, "pedidos-list.hbs");
        });

        // --- Create submission ---
        post("/pedido", (req, res) -> {
            Pedido pedido = extractPedido(req);

            // insert pedido and its produtos using useExtension (matches CristalController style)
            RepositorySingleton.jdbi.useExtension(PedidoRepo.class, dao -> {
                dao.insertPedido(pedido);
                // insert products
                if (pedido.getProdutos() != null) {
                    for (Map.Entry<String, Integer> e : pedido.getProdutos().entrySet()) {
                        removeEstoque( e.getKey(), e.getValue() );
                        dao.insertPedidoProduto(pedido.getId(), e.getKey(), e.getValue());
                    }
                }
            });

            res.redirect("/pedido");
            return null;
        });

        get("/produto/estoque/:sku", (req, res) -> {
            String sku = req.params("sku");
            Optional<?> produto = findProduto(sku);

            if (produto.isPresent()) {
                Produto obj = (Produto) produto.get();
                res.type("application/json");
                return "{\"estoque\": " + obj.getEstoque() + "}";
            } else {
                res.status(404);
                return "SKU inválido";
            }
        });

    }

    // --- Helpers ---

    private Pedido extractPedido(Request req) {
        Pedido p = new Pedido();

        // safe parse like in your CristalController (but allow nulls)
        p.setId(parseIntOrNull(req.queryParams("id")));
        p.setData(Date.valueOf(req.queryParams("data")));
        p.setCliente(req.queryParams("cliente"));
        p.setTransporte(req.queryParams("transporte"));
        p.setPagamento(req.queryParams("pagamento"));
        p.setNota_fiscal(parseIntOrNull(req.queryParams("nota_fiscal")));
        p.setColeta(parseIntOrNull(req.queryParams("coleta")));
        p.setValor(parseDoubleOrNull(req.queryParams("valor")));
        p.setOC(parseIntOrNull(req.queryParams("OC")));

        // parse produtos keys like: produtos[SKU][quantia]
        Map<String, String[]> params = req.queryMap().toMap();
        HashMap<String, Integer> produtos = new HashMap<>();

        Pattern produtoPattern = Pattern.compile("^produtos\\[(.+?)\\]\\[quantia\\]$");

        params.forEach((k, v) -> {
            Matcher m = produtoPattern.matcher(k);
            if (m.matches()) {
                String sku = m.group(1);
                try {
                    int q = (v != null && v.length > 0 && !v[0].isEmpty()) ? Integer.parseInt(v[0]) : 0;
                    produtos.put(sku, q);
                } catch (NumberFormatException ex) {
                    produtos.put(sku, 0);
                }
            }
        });


        p.setProdutos(produtos);
        return p;
    }

    private Integer parseIntOrNull(String s) {
        try {
            return (s == null || s.isEmpty()) ? null : Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDoubleOrNull(String s) {
            try {
                return (s == null || s.isEmpty()) ? null : Double.parseDouble(s);
            } catch (Exception e) {
                return null;
            }
        }

    private String render(Map<String, Object> model, String templatePath) {
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, templatePath));
    }
}
