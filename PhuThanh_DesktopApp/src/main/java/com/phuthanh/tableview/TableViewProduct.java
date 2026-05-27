package com.phuthanh.tableview;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import java.util.*;

@SuppressWarnings("unchecked")
public class TableViewProduct {

    /* ===== MODEL ===== */
    private   class Row {
        String dbKey;     // tên cột DB (KEY THẬT)
        String header;    // tên hiển thị (VN)
        String value;

        Row(String dbKey, String header, String value) {
            this.dbKey = dbKey;
            this.header = header;
            this.value = value;
        }
    }

    private final ObservableList<Row> rows = FXCollections.observableArrayList();
    private Stage dialog;

    /* ===== PUBLIC API ===== */

    public void showDialog(
            String title,
            List<String> dbColumns,
            Map<String, String> columnMap,     // DB -> VN
            Map<String, Object> initData
    ) {
        buildData(dbColumns, columnMap, initData);
        show(title);
    }

    /* ===== BUILD DATA ===== */

    private void buildData(
            List<String> dbColumns,
            Map<String, String> columnMap,
            Map<String, Object> initData
    ) {
        rows.clear();

        for (String dbKey : dbColumns) {

            String headerVN = columnMap != null
                    ? columnMap.getOrDefault(dbKey, dbKey)
                    : dbKey;

            Object v = initData.get(dbKey);

            rows.add(new Row(
                    dbKey,
                    headerVN,
                    v == null ? "" : v.toString()
            ));
        }
    }

    /* ===== UI ===== */

    private void show(String title) {

        TableView<Row> table = new TableView<>(rows);
        table.setEditable(true);

        TableColumn<Row, String> colHeader = new TableColumn<>("Thuộc tính");
        colHeader.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().header));
        colHeader.setEditable(false);
        colHeader.setPrefWidth(180);

        TableColumn<Row, String> colValue = new TableColumn<>("Giá trị");
        colValue.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().value));

        colValue.setCellFactory(
                TextFieldTableCell.forTableColumn(new DefaultStringConverter()));

        colValue.setOnEditCommit(e ->
                e.getRowValue().value = e.getNewValue());

        table.getColumns().addAll(colHeader, colValue);

        Button btnSave = new Button("Lưu");
        btnSave.setOnAction(e -> {
            // bạn gọi hàm lưu ở đây nếu muốn
            System.out.println(getData());
        });

        Button btnClose = new Button("Đóng");
        btnClose.setOnAction(e -> dialog.close());

        HBox footer = new HBox(10, btnSave, btnClose);
        footer.setPadding(new Insets(10));
        footer.setStyle("-fx-alignment: center-right;");

        BorderPane root = new BorderPane(table);
        root.setBottom(footer);

        dialog = new Stage();
        dialog.setTitle(title);
        dialog.setScene(new Scene(root, 480, 380));
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }

    /* ===== GET DATA (QUAN TRỌNG) ===== */

    public Map<String, Object> getData() {

        Map<String, Object> data = new LinkedHashMap<>();

        for (Row r : rows) {
            data.put(r.dbKey, r.value);
        }

        return data;
    }
}
