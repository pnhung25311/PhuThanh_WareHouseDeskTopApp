package com.phuthanh.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.phuthanh.model.helper.ExcelColumn;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class ConvertHTMLtoPDF {
    public void htmlToPdf(String html, File file) throws Exception {

        html = html.replace("&nbsp;", "&#160;");

        OutputStream os = new FileOutputStream(file);
        PdfRendererBuilder builder = new PdfRendererBuilder();

        // ⭐ BASE URI để load font từ file:///
        String baseUri = new File("src/main/resources/").toURI().toString();
        builder.withHtmlContent(html, baseUri);

        builder.toStream(os);

        // ⭐ Embed font Unicode
        builder.useFont(
                new File("C:/Windows/Fonts/arial.ttf"),
                "Arial",
                400,
                PdfRendererBuilder.FontStyle.NORMAL,
                true); // 👈 QUAN TRỌNG

        builder.run();
        os.close();
    }

    public String buildHeaders(List<ExcelColumn> columns) {

        StringBuilder html = new StringBuilder();

        for (ExcelColumn col : columns) {

            // ⭐ bỏ qua cột lỗi index
            // if (col.columnIndex < 0)
            // continue;

            html.append("<th>")
                    .append(col.header)
                    .append("</th>");
        }

        return html.toString();
    }

    public String buildRows(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns) {

        StringBuilder html = new StringBuilder();

        for (int i = 0; i < tableView.getItems().size(); i++) {

            ObservableList<String> rowData = tableView.getItems().get(i);
            html.append("<tr>");

            // loop giống Excel
            for (int j = 0; j < columns.size(); j++) {

                ExcelColumn mapCol = columns.get(j);
                String value = "";

                // ===== STT =====
                if (j == 0) {
                    value = String.valueOf(i + 1);
                } else {
                    if (mapCol.columnIndex >= 0 && mapCol.columnIndex < rowData.size()) {
                        value = rowData.get(mapCol.columnIndex);
                    }
                }

                html.append("<td>")
                        .append(escapeHtml(value))
                        .append("</td>");
            }

            html.append("</tr>");
        }

        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null)
            return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

}
