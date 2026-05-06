package com.phuthanh.helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DBCallDataBusiness {
    private static final FunctionHelper functionHelper = new FunctionHelper();
    private static final DbTableHelper dbTableHelper = new DbTableHelper();

    // ⭐ METHOD CHÍNH: vừa tạo column vừa load data
    public ObservableList<ObservableList<String>> getDataSQL(
            TableView<ObservableList<String>> table,
            String sql) {

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        try (Connection conn = DbHelperBusiness.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // 🔥 1. TẠO COLUMN TỪ METADATA
            buildColumns(table, rs);
            dbTableHelper.createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());

            // 🔥 2. LOAD DATA
            data = addData(rs);


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    // ================= TẠO COLUMN =================
    private void buildColumns(TableView<ObservableList<String>> table, ResultSet rs) throws SQLException {

        table.getColumns().clear();

        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        for (int i = 0; i < colCount; i++) {
            final int colIndex = i;

            String columnName = meta.getColumnLabel(i + 1);

            TableColumn<ObservableList<String>, String> column =
                    new TableColumn<>(columnName);

            column.setCellValueFactory(param ->
                    new SimpleStringProperty(
                            param.getValue().get(colIndex)
                    ));

            column.setPrefWidth(150);
            table.getColumns().add(column);
        }
    }

    // ================= LOAD DATA =================
    private ObservableList<ObservableList<String>> addData(ResultSet rs) throws SQLException {

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        int colCount = rs.getMetaData().getColumnCount();

        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();

            for (int i = 1; i <= colCount; i++) {
                String val = rs.getString(i);
                row.add(val != null ? val : "");
            }

            data.add(row);
        }

        return data;
    }
}