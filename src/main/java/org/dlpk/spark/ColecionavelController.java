package org.dlpk.spark;

import lombok.RequiredArgsConstructor;
import org.dlpk.database.ColecionavelRepo;
import org.dlpk.database.RepositorySingleton;
import org.dlpk.objects.Colecionavel;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.*;

import static spark.Spark.*;

@RequiredArgsConstructor
public class ColecionavelController {

    public void setupRoutes() {

        // Show creation form
        get("/colecionaveis/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("action", "/colecionaveis");
            model.put("method", "post");
            model.put("buttonText", "Criar Colecionável");
            return render(model, "colecionavel-form.hbs");
        });

        // Show single
        get("/colecionaveis/:sku", (req, res) -> {
            String sku = req.params("sku");
            Optional<Colecionavel> colecionavel = RepositorySingleton.jdbi.withExtension(ColecionavelRepo.class, dao -> dao.findBySku(sku));

            Map<String, Object> model = new HashMap<>();
            colecionavel.ifPresent(c -> model.put("colecionaveis", Collections.singletonList(c)));
            return render(model, "colecionavel-list.hbs");
        });





        // Show edit form
        get("/colecionaveis/edit/:sku", (req, res) -> {
            String sku = req.params("sku");
            Optional<Colecionavel> colecionavel = RepositorySingleton.jdbi.withExtension(ColecionavelRepo.class, dao -> dao.findBySku(sku));

            if (colecionavel.isPresent()) {
                Map<String, Object> model = new HashMap<>();
                model.put("colecionavel", colecionavel.get());
                model.put("action", "/colecionaveis/update/" + sku);
                model.put("method", "post");
                model.put("buttonText", "Salvar Alterações");
                return render(model, "colecionavel-form.hbs");
            } else {
                res.redirect("/colecionaveis");
                return null;
            }
        });

        // Handle edit submission
        post("/colecionaveis/update/:sku", (req, res) -> {
            Colecionavel colecionavel = extractColecionavel(req);
            RepositorySingleton.jdbi.useExtension(ColecionavelRepo.class, dao -> dao.update(colecionavel));
            res.redirect("/colecionaveis");
            return null;
        });

        // Delete
        post("/colecionaveis/delete/:sku", (req, res) -> {
            String sku = req.params("sku");
            RepositorySingleton.jdbi.useExtension(ColecionavelRepo.class, dao -> dao.delete(sku));
            res.redirect("/colecionaveis");
            return null;
        });
        // Handle creation form
        post("/colecionaveis", (req, res) -> {
            Colecionavel colecionavel = extractColecionavel(req);
            RepositorySingleton.jdbi.useExtension(ColecionavelRepo.class, dao -> dao.insert(colecionavel));
            res.redirect("/colecionaveis");
            return null;
        });
        // List all
        get("/colecionaveis", (req, res) -> {
            List<Colecionavel> colecionaveis = RepositorySingleton.jdbi.withExtension(ColecionavelRepo.class, ColecionavelRepo::findAll);
            Map<String, Object> model = new HashMap<>();
            model.put("colecionaveis", colecionaveis);
            return render(model, "colecionavel-list.hbs");
        });
    }


    private Colecionavel extractColecionavel(Request req) {
        Colecionavel c = new Colecionavel();
        c.setSku("FU" + req.queryParams("sku"));
        c.setPeso(Float.parseFloat(req.queryParams( "peso") ));
        c.setEan(req.queryParams("ean"));
        c.setTitulo(req.queryParams("titulo"));
        c.setEstoque(parseInt(req.queryParams("estoque")));
        c.setMarca(req.queryParams("marca"));
        return c;
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private String render(Map<String, Object> model, String templatePath) {
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, templatePath));
    }
}
