package org.dlpk.spark;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.RequiredArgsConstructor;

import org.dlpk.database.ColecionavelRepo;
import org.dlpk.database.RepositorySingleton;
import org.dlpk.objects.Colecionavel;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

        post("/colecionaveis/import", (req, res) -> {

            String location = "/tmp"; // temp directory for file uploads
            long maxFileSize = 10 * 1024 * 1024; // 10MB
            long maxRequestSize = 20 * 1024 * 1024; // 20MB
            int fileSizeThreshold = 1024 * 1024; // 1MB
            MultipartConfigElement multipartConfig = new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold);
            req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfig);

            Part filePart = req.raw().getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                res.status(400);
                return "Nenhum arquivo CSV enviado.";
            }

            try (Reader reader = new InputStreamReader(filePart.getInputStream())) {

                List<Colecionavel> colecionaveis = new CsvToBeanBuilder<Colecionavel>(reader)
                        .withType(Colecionavel.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build()
                        .parse();

                RepositorySingleton.jdbi.useExtension(ColecionavelRepo.class, dao -> {
                    for (Colecionavel produto : colecionaveis) {
                        // Ensure SKU format
                        if (!produto.getSku().startsWith("FU")) {
                            produto.setSku("FU" + produto.getSku());
                        }

                        Optional<Colecionavel> existing = dao.findBySku(produto.getSku());
                        if (existing.isPresent()) {
                            dao.update(produto);
                        } else {
                            dao.insert(produto);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "Erro ao processar o arquivo CSV: " + e.getMessage();
            }

            res.redirect("/colecionaveis");
            return null;
        });


        get("/colecionaveis/export", (req, res) -> {
            res.type("text/csv; charset=UTF-8"); // tell browser it's CSV text

            List<Colecionavel> colecionaveis = RepositorySingleton.jdbi.withExtension(
                    ColecionavelRepo.class, ColecionavelRepo::findAll
            );

            try (PrintWriter writer = res.raw().getWriter()) {
                StatefulBeanToCsv<Colecionavel> beanToCsv = new StatefulBeanToCsvBuilder<Colecionavel>(writer)
                        .withSeparator(',')
                        .withApplyQuotesToAll(false)
                        .build();

                beanToCsv.write(colecionaveis);
                writer.flush(); // make sure all data is sent
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "Erro ao exportar colecionáveis: " + e.getMessage();
            }

            return null; // do NOT return anything else
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

    private Float parseFloat(String s) {
        try { return Float.parseFloat(s); } catch (Exception e) { return 0.0f; }
    }

    private String render(Map<String, Object> model, String templatePath) {
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, templatePath));
    }
}
