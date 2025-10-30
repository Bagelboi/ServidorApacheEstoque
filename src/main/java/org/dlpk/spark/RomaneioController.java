// ...existing code...
package org.dlpk.spark;

import com.github.jknack.handlebars.Handlebars;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dlpk.database.ColecionavelRepo;
import org.dlpk.database.CristalRepo;
import org.dlpk.database.EventoRepo;
import org.dlpk.database.RomaneioRepo;
import org.dlpk.database.RepositorySingleton;
import org.dlpk.enums.TRANSPORTE;
import org.dlpk.objects.EventoEstoque;
import org.dlpk.objects.Produto;
import org.dlpk.objects.Romaneio;
import org.dlpk.objects.RomaneioProduto;
import org.dlpk.enums.EVENTO_ESTOQUE;
import org.eclipse.jetty.util.log.Slf4jLog;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spark.Spark.*;

@Slf4j
@RequiredArgsConstructor
public class RomaneioController {

    private ProdutoController produtoController = new ProdutoController();

    public void setupRoutes() {



        // Show creation form
        get("/rom/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("action", "/rom");
            model.put("method", "post");
            model.put("buttonText", "Criar Romaneio");
            return render(model, "romaneio-form.hbs");
        });


        // Show single romaneio by numero (with produtos and peso total)
        get("/rom/:numero", (req, res) -> {
            int numero = Integer.parseInt(req.params("numero"));
            Romaneio rom = RepositorySingleton.jdbi.withExtension(RomaneioRepo.class, dao -> dao.findRomaneioByNumero(numero));
            if (rom == null) {
                res.redirect("/rom");
                return null;
            }
            Integer romid = rom.getId();
            rom = RepositorySingleton.jdbi.withExtension(RomaneioRepo.class, dao -> dao.getRomaneioWithProdutos(romid));

            // build detailed product rows with peso info
            List<Map<String, Object>> produtoRows = new ArrayList<>();
            float pesoTotal = 0f;
            float precoTotal = 0f;
            if (rom.getProdutos() != null) {
                for (RomaneioProduto p : rom.getProdutos()) {
                    // If valor_unidade is -1, try to fetch precoPadrao from Produto
                    Float valorUnidade = p.getValor_unidade();
                    if (valorUnidade != null && valorUnidade < 0.0f) {
                        Optional<?> opt = produtoController.findProduto(p.getSku());
                        if (opt.isPresent()) {
                            Produto prod = (Produto) opt.get();
                            if (prod.getPrecoPadrao() != null) {
                                valorUnidade = prod.getPrecoPadrao();
                                p.setValor_unidade(valorUnidade);
                            }
                        }
                    }
                    Map<String, Object> row = new HashMap<>();
                    row.put("sku", p.getSku());
                    row.put("quantidade", p.getQuantidade());
                    row.put("valor_unidade", valorUnidade);
                    row.put("valor_total_row", valorUnidade * p.getQuantidade());
                    precoTotal += valorUnidade * p.getQuantidade();
                    Optional<?> opt = produtoController.findProduto(p.getSku());
                    if (opt.isPresent()) {
                        Produto prod = (Produto) opt.get();
                        Float pesoUnit = prod.getPeso() == null ? 0f : prod.getPeso();
                        float pesoLinha = pesoUnit * (p.getQuantidade() == null ? 0 : p.getQuantidade());
                        row.put("peso_unitario", pesoUnit);
                        row.put("peso_total", pesoLinha);
                        pesoTotal += pesoLinha;
                    } else {
                        row.put("peso_unitario", 0f);
                        row.put("peso_total", 0f);
                    }
                    produtoRows.add(row);
                }
            }

            Map<String, Object> model = new HashMap<>();
            model.put("romaneio", rom);
            model.put("produtosDetalhados", produtoRows);
            model.put("pesoTotal", pesoTotal);
            model.put("precoTotal", precoTotal);
            return render(model, "romaneio-list.hbs");
        });


        // Launch romaneio (lancar)
        post("/rom/lancar/:numero", (req, res) -> {
            int numero = Integer.parseInt(req.params("numero"));
            Romaneio rom = RepositorySingleton.jdbi.withExtension(RomaneioRepo.class, dao -> dao.findRomaneioByNumero(numero));
            if (rom == null) { res.redirect("/rom"); return null; }
            if (rom.isLancado()) { res.redirect("/rom"); return null; }

            // set as lancado with today's date
            RepositorySingleton.jdbi.useExtension(RomaneioRepo.class, dao -> dao.lancarRomaneio(rom.getId(), LocalDate.now()));

            // create events (deducao) and remove stock
            Romaneio full = RepositorySingleton.jdbi.withExtension(RomaneioRepo.class, dao -> dao.getRomaneioWithProdutos(rom.getId()));
            List<EventoEstoque> eventos = full.asEventoEstoque(EVENTO_ESTOQUE.DEDUCAO);
            RepositorySingleton.jdbi.useExtension(EventoRepo.class, dao -> {
                for (EventoEstoque ev : eventos) {
                    dao.insertEstoque(ev);
                }
            });
            // update product stocks
            if (full.getProdutos() != null) {
                for (RomaneioProduto p : full.getProdutos()) {
                    produtoController.removeEstoque(p.getSku(), p.getQuantidade());
                }
            }

            res.redirect("/rom");
            return null;
        });

        // Undo romaneio (undo)
        post("/rom/undo/:numero", (req, res) -> {
            int numero = Integer.parseInt(req.params("numero"));
            Romaneio rom = RepositorySingleton.jdbi.withExtension(RomaneioRepo.class, dao -> dao.findRomaneioByNumero(numero));
            if (rom == null) { res.redirect("/rom"); return null; }
            // undo launch
            RepositorySingleton.jdbi.useExtension(RomaneioRepo.class, dao -> dao.undoRomaneio(rom.getId()));

            Romaneio full = RepositorySingleton.jdbi.withExtension(RomaneioRepo.class, dao -> dao.getRomaneioWithProdutos(rom.getId()));
            List<EventoEstoque> eventos = full.asEventoEstoque(EVENTO_ESTOQUE.ADICAO);
            RepositorySingleton.jdbi.useExtension(EventoRepo.class, dao -> {
                for (EventoEstoque ev : eventos) {
                    dao.insertEstoque(ev);
                }
            });
            // restore stock
            if (full.getProdutos() != null) {
                for (RomaneioProduto p : full.getProdutos()) {
                    produtoController.addEstoque(p.getSku(), p.getQuantidade());
                }
            }

            res.redirect("/rom");
            return null;
        });

        // ...existing code...
        // Show edit form (update)
        get("/rom/update/:numero", (req, res) -> {
            int numero = Integer.parseInt(req.params("numero"));
            Romaneio rom = RepositorySingleton.jdbi.withExtension(RomaneioRepo.class, dao -> dao.findRomaneioByNumero(numero));
            if (rom == null) { res.redirect("/rom"); return null; }

            Integer id = rom.getId();
            rom = RepositorySingleton.jdbi.withExtension(RomaneioRepo.class, dao -> dao.getRomaneioWithProdutos(id));

            Map<String, Object> model = new HashMap<>();
            model.put("action", "/rom/update/" + numero);
            model.put("method", "post");
            model.put("buttonText", "Atualizar Romaneio");
            model.put("romaneio", rom); // prefill form with existing rom data
            return render(model, "romaneio-form.hbs");
        });

        // Handle update submission
        post("/rom/update/:numero", (req, res) -> {
            int numero = Integer.parseInt(req.params("numero"));
            Romaneio existing = RepositorySingleton.jdbi.withExtension(RomaneioRepo.class, dao -> dao.findRomaneioByNumero(numero));
            if (existing == null) { res.redirect("/rom"); return null; }

            Romaneio rom = extractRomaneio(req);
            // preserve DB id and created/lancado state if needed
            rom.setId(existing.getId());
            rom.setDataCriacao(existing.getDataCriacao());
            rom.setLancado(existing.isLancado());

            // update romaneio and produtos (assumes RomaneioRepo has updateRomaneioWithProdutos)
            RepositorySingleton.jdbi.useExtension(RomaneioRepo.class, dao -> dao.updateRomaneioWithProdutos(rom));
            res.redirect("/rom");
            return null;
        });

        // Delete romaneio
        post("/rom/delete/:numero", (req, res) -> {
            int numero = Integer.parseInt(req.params("numero"));
            Romaneio rom = RepositorySingleton.jdbi.withExtension(RomaneioRepo.class, dao -> dao.findRomaneioByNumero(numero));
            if (rom != null) {
                RepositorySingleton.jdbi.useExtension(RomaneioRepo.class, dao -> dao.deleteRomaneioWithProdutos(rom.getId()));
            }
            res.redirect("/rom");
            return null;
        });

        // List all romaneios
        get("/rom", (req, res) -> {
            List<Romaneio> roms = RepositorySingleton.jdbi.withExtension(RomaneioRepo.class, RomaneioRepo::findAllRomaneios);

            // populate produtos for each romaneio to compute totals if desired
            List<Map<String, Object>> romView = new ArrayList<>();
            for (Romaneio r : roms) {
                Romaneio full = RepositorySingleton.jdbi.withExtension(RomaneioRepo.class, dao -> dao.getRomaneioWithProdutos(r.getId()));
                float precoTotal = 0.0f;
                if (full.getProdutos() != null) {
                    for (RomaneioProduto p : full.getProdutos()) {
                        float valor = p.getValor_unidade() == null ? 0.0f : p.getValor_unidade();
                        int q = p.getQuantidade() == null ? 0 : p.getQuantidade();
                        precoTotal += valor * q;
                    }
                }
                Map<String, Object> m = new HashMap<>();
                m.put("romaneio", full);
                m.put("precoTotal", precoTotal);
                romView.add(m);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("romaneios", romView);
            return render(model, "romaneio-list.hbs");
        });

        // Create submission
        post("/rom", (req, res) -> {
            Romaneio rom = extractRomaneio(req);
            // insert romaneio and produtos
            rom.setDataCriacao( LocalDate.now() );
            RepositorySingleton.jdbi.useExtension(RomaneioRepo.class, dao -> dao.createRomaneioWithProdutos(rom));
            res.redirect("/rom");
            return null;
        });

    }

    // --- helpers ---
    private Romaneio extractRomaneio(Request req) {
        Romaneio r = new Romaneio();

        // basic fields
        String numStr = req.queryParams("numeroRomaneio");
        r.setNumeroRomaneio(parseIntOrNull(numStr));


        r.setDataEmissao(LocalDate.now());

        r.setDestinatarioNome(req.queryParams("destinatarioNome"));
        r.setDestinatarioDocumento(req.queryParams("destinatarioDocumento"));
        r.setContato(req.queryParams("contato"));
        r.setColeta(req.queryParams("coleta"));
        r.setVendedor(req.queryParams("vendedor"));
        r.setOc(req.queryParams("oc"));
        r.setNotaFiscal(parseIntOrNull(req.queryParams("notaFiscal")));
        r.setCondPagamento(req.queryParams("condPagamento"));
        r.setDescontoValorTotal(parseFloatOrNull(req.queryParams("descontoValorTotal")));
        r.setObservacoes(req.queryParams("observacoes"));
        r.setLancado(false);
        r.setTransporte(TRANSPORTE.valueOf( req.queryParams("transporte") ) );

        // --- NEW: parse produtos from JSON ---
        String produtosJson = req.queryParams("produtosJson");
        if (produtosJson != null && !produtosJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                RomaneioProduto[] produtosArray = gson.fromJson(produtosJson, RomaneioProduto[].class);
                r.setProdutos(Arrays.asList(produtosArray));
            } catch (Exception e) {
                r.setProdutos(new ArrayList<>()); // fallback empty
            }
        } else {
            r.setProdutos(new ArrayList<>());
        }

        return r;
    }

    private Integer parseIntOrNull(String s) {
        try {
            return (s == null || s.isEmpty()) ? null : Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private Float parseFloatOrNull(String s) {
        try {
            return (s == null || s.isEmpty()) ? null : Float.parseFloat(s);
        } catch (Exception e) {
            return null;
        }
    }

    private String render(Map<String, Object> model, String templatePath) {
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, templatePath));
    }
}
// ...existing code...