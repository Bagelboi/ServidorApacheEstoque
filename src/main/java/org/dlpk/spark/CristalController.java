package org.dlpk.spark;


import lombok.RequiredArgsConstructor;
import org.dlpk.database.CristalRepo;
import org.dlpk.database.RepositorySingleton;
import org.dlpk.objects.Cristal;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.*;

import static spark.Spark.*;

@RequiredArgsConstructor
public class CristalController {

    public void setupRoutes() {


        // Show creation form
        get("/cristais/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("action", "/cristais");
            model.put("method", "post");
            model.put("buttonText", "Criar Cristal");
            return render(model, "cristal-form.hbs");
        });

        // Show single (reuses list page with only one element)
        get("/cristais/:sku", (req, res) -> {
            String sku = req.params("sku");
            Optional<Cristal> cristal = RepositorySingleton.jdbi.withExtension(CristalRepo.class, dao -> dao.findBySku(sku));

            Map<String, Object> model = new HashMap<>();
            cristal.ifPresent(c -> model.put("cristais", Collections.singletonList(c)));
            return render(model, "cristal-list.hbs");
        });





        // Show edit form
        get("/cristais/edit/:sku", (req, res) -> {
            String sku = req.params("sku");
            Optional<Cristal> cristal = RepositorySingleton.jdbi.withExtension(CristalRepo.class, dao -> dao.findBySku(sku));

            if (cristal.isPresent()) {
                Map<String, Object> model = new HashMap<>();
                model.put("cristal", cristal.get());
                model.put("action", "/cristais/update/" + sku);
                model.put("method", "post");
                model.put("buttonText", "Salvar Alterações");
                return render(model, "cristal-form.hbs");
            } else {
                res.redirect("/cristais");
                return null;
            }
        });

        // Handle edit submission
        post("/cristais/update/:sku", (req, res) -> {
            Cristal cristal = extractCristal(req);
            RepositorySingleton.jdbi.useExtension(CristalRepo.class, dao -> dao.update(cristal));
            res.redirect("/cristais");
            return null;
        });

        // Delete crystal
        post("/cristais/delete/:sku", (req, res) -> {
            String sku = req.params("sku");
            RepositorySingleton.jdbi.useExtension(CristalRepo.class, dao -> dao.delete(sku));
            res.redirect("/cristais");
            return null;
        });

        // Handle creation form
        post("/cristais", (req, res) -> {
            Cristal cristal = extractCristal(req);
            RepositorySingleton.jdbi.useExtension(CristalRepo.class, dao -> dao.insert(cristal));
            res.redirect("/cristais");
            return null;
        });

        // List all
        get("/cristais", (req, res) -> {
            List<Cristal> cristais = RepositorySingleton.jdbi.withExtension(CristalRepo.class, CristalRepo::findAll);
            Map<String, Object> model = new HashMap<>();
            model.put("cristais", cristais);
            return render(model, "cristal-list.hbs");
        });
    }

    private Cristal extractCristal(Request req) {
        Cristal c = new Cristal();

        c.setSku("CR" + req.queryParams("sku"));
        c.setEan(req.queryParams("ean"));
        c.setPeso(Float.parseFloat(req.queryParams( "peso") ));
        c.setTitulo(req.queryParams("titulo"));
        c.setEstoque(parseInt(req.queryParams("estoque")));
        //below are non product fields
        c.setCor(req.queryParams("cor"));
        c.setTamanho(req.queryParams("tamanho"));
        c.setDescricao(req.queryParams("descricao"));
        return c;
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private String render(Map<String, Object> model, String templatePath) {
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, templatePath));
    }
}
