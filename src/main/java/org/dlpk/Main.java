package org.dlpk;

import static spark.Spark.*;

import spark.ModelAndView;
import spark.TemplateEngine;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        port(4567);

        // Handlebars engine
        TemplateEngine hbsEngine = new TemplateEngine() {
            Handlebars handlebars = new Handlebars();

            @Override
            public String render(ModelAndView modelAndView) {
                try {
                    Template template = handlebars.compile("templates/" + modelAndView.getViewName());
                    return template.apply(modelAndView.getModel());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("message", "Hello, Spark with Handlebars!");
            return hbsEngine.render(new ModelAndView(model, "index"));
        });

    }
}

