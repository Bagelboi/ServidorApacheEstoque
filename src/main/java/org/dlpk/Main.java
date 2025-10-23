package org.dlpk;

import static spark.Spark.*;

import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.dlpk.sheets.GoogleSheetsService;
import org.dlpk.sheets.oAuthAPI;
import org.dlpk.spark.ColecionavelController;
import org.dlpk.spark.CristalController;
import org.dlpk.spark.PedidoController;
import org.jdbi.v3.core.Jdbi;
import spark.ModelAndView;
import spark.TemplateEngine;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.*;

public class Main {
    public static void sheets() throws IOException, GeneralSecurityException {
        GoogleSheetsService sheetsService = new GoogleSheetsService(oAuthAPI.createSheetsChannel(), Optional.empty());
        System.out.println( (new Gson() ).toJson( sheetsService.getValues("pag1", "A2:C4") ) );
        // Handlebars engine
    }
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        port(4567);
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());
        new ColecionavelController().setupRoutes();
        new CristalController().setupRoutes();
        new PedidoController().setupRoutes();

    }
}

