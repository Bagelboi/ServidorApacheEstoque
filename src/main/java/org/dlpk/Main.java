package org.dlpk;

import static spark.Spark.*;

import org.dlpk.sql.Database;
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

        // List users
        get("/", (req, res) -> {
            List<Map<String, Object>> users = new ArrayList<>();
            try (Connection conn = Database.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", rs.getInt("id"));
                    user.put("name", rs.getString("name"));
                    users.add(user);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Map<String, Object> model = new HashMap<>();
            model.put("users", users);
            return new ModelAndView(model, "index");
        }, hbsEngine);

        // Add user
        post("/add", (req, res) -> {
            String name = req.queryParams("name");
            if (name != null && !name.isBlank()) {
                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO users(name) VALUES(?)")) {
                    stmt.setString(1, name);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            res.redirect("/");
            return null;
        });
    }
}

