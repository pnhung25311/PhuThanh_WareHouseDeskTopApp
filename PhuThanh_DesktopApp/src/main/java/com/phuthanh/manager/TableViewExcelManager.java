package com.phuthanh.manager;

import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.scene.input.*;

public class TableViewExcelManager {

    public <T> void setupTableView(TableView<T> table) {

        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        enableEnterEdit(table);
        enableCopy(table);

        table.getSelectionModel().getSelectedCells()
                .addListener((ListChangeListener<TablePosition>) c -> table.refresh());
    }

    // ================= ENTER EDIT =================
    private <T> void enableEnterEdit(TableView<T> table) {
        table.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                var selected = table.getSelectionModel().getSelectedCells();
                if (!selected.isEmpty()) {
                    TablePosition<T, ?> pos = selected.get(0);
                    table.edit(pos.getRow(), pos.getTableColumn());
                    e.consume();
                }
            }
        });
    }

    // ================= COPY =================
    private <T> void enableCopy(TableView<T> table) {
        table.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.C) {
                copy(table);
                e.consume();
            }
        });
    }

    private <T> void copy(TableView<T> table) {
        var selected = table.getSelectionModel().getSelectedCells();
        if (selected.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        var cols = table.getVisibleLeafColumns();

        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;

        for (var p : selected) {
            int colIndex = table.getVisibleLeafIndex(p.getTableColumn());

            minRow = Math.min(minRow, p.getRow());
            maxRow = Math.max(maxRow, p.getRow());
            minCol = Math.min(minCol, colIndex);
            maxCol = Math.max(maxCol, colIndex);
        }

        for (int r = minRow; r <= maxRow; r++) {
            boolean first = true;
            for (int c = minCol; c <= maxCol; c++) {
                Object value = cols.get(c).getCellData(r);
                if (!first) sb.append("\t");
                sb.append(value == null ? "" : value.toString());
                first = false;
            }
            sb.append("\n");
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    // ================= FOCUS =================
    public <T> void focusFirstCell(TableView<T> table) {
        if (!table.getItems().isEmpty() && !table.getColumns().isEmpty()) {
            table.requestFocus();
            table.getSelectionModel().select(0, table.getVisibleLeafColumn(0));
        }
    }
}