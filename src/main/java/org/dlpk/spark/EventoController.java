// ...existing code...
package org.dlpk.spark;

import com.google.gson.Gson;
import org.dlpk.database.EventoRepo;
import org.dlpk.database.RepositorySingleton;
import org.dlpk.objects.EventoConferencia;
import org.dlpk.objects.EventoEstoque;
import org.dlpk.enums.EVENTO_ESTOQUE;
import org.jdbi.v3.core.Jdbi;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class EventoController {

    private ProdutoController produtoController = new ProdutoController();

    // Initialize with a Jdbi instance: EventoController.init(jdbi);
    public void setupRoutes() {


        // creation handler
        post("/evento/estoque/new", (req, res) -> {
            String sku = req.queryParams("sku");

            if (!produtoController.findProduto(sku).isPresent())
                return null; //produto não existe

            String dataStr = req.queryParams("data");

            // parse date, default to today if missing
            LocalDate data;
            try {
                if (dataStr == null || dataStr.trim().isEmpty()) {
                    data = LocalDate.now();
                } else {
                    data = LocalDate.parse(dataStr);
                }
            } catch (IllegalArgumentException e) {
                data = LocalDate.now();
            }

            Integer quantidade = Integer.parseInt(req.queryParams("quantidade"));
            EVENTO_ESTOQUE tipo = EVENTO_ESTOQUE.valueOf(req.queryParams("tipo"));
            // build event
            EventoEstoque evento = new EventoEstoque();
            evento.setSku(sku);
            evento.setData(data);
            evento.setTipo(tipo);
            evento.setQuantidade(quantidade);
            evento.setOrigem(req.queryParams("origem"));

                int id = RepositorySingleton.jdbi.withExtension( EventoRepo.class, dao -> dao.insertEstoque(evento) );
                evento.setId(id);

                if (tipo == EVENTO_ESTOQUE.DEDUCAO) {
                    produtoController.removeEstoque(sku, quantidade);
                } else {
                    produtoController.addEstoque(sku, quantidade);
                }


            res.redirect("/evento/estoque");
            return null;
        });


        // show form + list
        get("/evento/estoque", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            List<EventoEstoque> eventos = RepositorySingleton.jdbi.withExtension( EventoRepo.class, EventoRepo::findAllEstoque );
            model.put("eventos", eventos);
            return new ModelAndView(model, "evento-estoque.hbs");
        }, new HandlebarsTemplateEngine());

                // show form + list
        get("/evento/conferencia", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            List<EventoConferencia> eventos = RepositorySingleton.jdbi.withExtension( EventoRepo.class, EventoRepo::findAllConferencia );
            model.put("eventos", eventos);
            return new ModelAndView(model, "evento-conferencia.hbs");
        }, new HandlebarsTemplateEngine());

        // POST for EventoConferencia
        post("/evento/conferencia/new", (req, res) -> {
            // Parse JSON body
            Gson gson = new Gson();
            EventoConferenciaInput input = gson.fromJson(req.body(), EventoConferenciaInput.class);

            // Validate SKU exists
            if (!produtoController.findProduto(input.sku).isPresent())
                return ""; // Produto não existe

            EventoConferencia evento = new EventoConferencia();
            evento.setSku(input.sku);
            evento.setData(LocalDate.now());
            evento.setEstoque_novo(input.estoque_novo);
            evento.setOrigem(input.origem);

            // Save to DB
            int id = RepositorySingleton.jdbi.withExtension(EventoRepo.class, dao -> dao.insertConferencia(evento));
            evento.setId(id);

            produtoController.addEstoque(input.sku, input.estoque_novo - input.estoque_velho);

            return null; // No redirect here
        });
    }

    // Helper class for JSON input
    private static class EventoConferenciaInput {
        String sku;
        int estoque_novo;
        int estoque_velho;
        String origem;
    }
}
// ...existing code...