package org.dlpk.spark;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.dlpk.objects.Colecionavel;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

public class CsvHelper {


    public static void prepRequest(Request req) {
        String location = "/tmp"; // temp directory for file uploads
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        long maxRequestSize = 20 * 1024 * 1024; // 20MB
        int fileSizeThreshold = 1024 * 1024; // 1MB
        MultipartConfigElement multipartConfig = new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold);
        req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfig);
    }

    public static InputStream getInputStream(Request req) throws ServletException, IOException {
        Part filePart = req.raw().getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            return null;
        }
        return filePart.getInputStream();
    }

    public static <T> String exportRoute(List<T> list, Request req, Response res) {
        res.type("text/csv; charset=UTF-8"); // tell browser it's CSV text
        try (PrintWriter writer = res.raw().getWriter()) {
            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(writer)
                    .withSeparator(',')
                    .withApplyQuotesToAll(false)
                    .build();

            beanToCsv.write(list);
            writer.flush(); // make sure all data is sent
        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            return "Erro ao exportar colecionáveis: " + e.getMessage();
        }

        return null;
    }
}
