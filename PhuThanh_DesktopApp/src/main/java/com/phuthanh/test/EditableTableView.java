package com.phuthanh.test;

import com.phuthanh.custom.CustomCombobox;
import com.phuthanh.test.model.Person;

import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;

public class EditableTableView {

    private final TableView<Person> table = new TableView<>();
    private final ObservableList<Person> data = FXCollections.observableArrayList();

    public EditableTableView() {
        createTable();
    }

    public TableView<Person> getTable() {
        return table;
    }

    // ================= CREATE TABLE =================
    private void createTable() {

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);

        data.addAll(
                new Person("1", "Hưng", "Admin"),
                new Person("2", "An", "User"),
                new Person("3", "Bình", "Manager"));
        table.setItems(data);

        enableDoubleClickEdit(table);
        enablePasteFromExcel();
        // table.setMaxHeight(100);
        table.setPrefHeight(10000);
        // ===== NAME COLUMN (TextField) =====
        TableColumn<Person, String> idcol = new TableColumn<>("Id");
        idcol.setPrefWidth(200);
        idcol.setCellValueFactory(c -> c.getValue().getId());
        idcol.setEditable(true);

        idcol.setCellFactory(TextFieldTableCell.forTableColumn());
        idcol.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));

        TableColumn<Person, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(200);
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        nameCol.setEditable(true);

        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));

        // ===== ROLE COLUMN (ComboBox) =====
        TableColumn<Person, String> roleCol = new TableColumn<>("Role");
        roleCol.setPrefWidth(200);
        roleCol.setCellValueFactory(c -> c.getValue().roleProperty());
        roleCol.setEditable(true);

        ObservableList<String> roles = FXCollections.observableArrayList(
                "Admin", "User", "Manager", "Guest");

        roleCol.setCellFactory(col -> new TableCell<Person, String>() {

            private final ComboBox<String> combo = new ComboBox<>(roles);

            {
                combo.setMaxWidth(Double.MAX_VALUE);
                CustomCombobox.setupComboBox(combo, roles, null, null);

                combo.setOnAction(e -> {
                    Person p = (Person) getTableRow().getItem();
                    if (p != null) {
                        p.setRole(combo.getValue());
                        commitEdit(combo.getValue());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else {
                    combo.setValue(item);
                    setGraphic(combo);
                }
            }
        });

        table.getColumns().addAll(idcol, nameCol, roleCol);
    }

    // ===== DOUBLE CLICK EDIT =====
    private <S> void enableDoubleClickEdit(TableView<S> table) {
        table.setRowFactory(tv -> {
            TableRow<S> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {

                    int rowIndex = row.getIndex();
                    TablePosition<S, ?> pos = table.getFocusModel().getFocusedCell();

                    if (pos != null) {
                        // ⭐ FIX lỗi generics ở đây
                        @SuppressWarnings("unchecked")
                        TableColumn<S, Object> column = (TableColumn<S, Object>) pos.getTableColumn();

                        table.edit(rowIndex, column);
                    }
                }
            });

            return row;
        });
    }

    // ================= ADD NEW ROW =================
    public void addNewRow() {
        Person newPerson = new Person("", "", "Guest");
        data.add(newPerson);
        table.scrollTo(newPerson);
        table.getSelectionModel().select(newPerson);
    }

    private void enablePasteFromExcel() {

        table.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.V) {
                pasteFromClipboard();
            }
        });
    }

    private void pasteFromClipboard() {

        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasString())
            return;

        String clipboardText = clipboard.getString();
        String[] rows = clipboardText.split("\\r?\\n");

        // ⭐ Lấy vị trí cell đang chọn
        TablePosition<Person, ?> focusedCell = table.getFocusModel().getFocusedCell();

        int startRow = focusedCell.getRow();
        int startCol = focusedCell.getColumn();

        // Nếu chưa chọn cell nào -> add row như cũ
        if (startRow < 0 || startCol < 0) {
            for (String row : rows) {
                if (row.trim().isEmpty())
                    continue;

                String[] cols = row.split("\\t");
                String id = cols.length > 0 ? cols[0] : "";
                String name = cols.length > 1 ? cols[1] : "";
                String role = cols.length > 2 ? cols[2] : "Guest";

                data.add(new Person(id, name, role));
            }
            return;
        }

        // ⭐ Paste theo vị trí đang chọn
        for (int i = 0; i < rows.length; i++) {

            String rowText = rows[i];
            if (rowText.trim().isEmpty())
                continue;

            String[] cols = rowText.split("\\t");

            int targetRow = startRow + i;

            // Nếu thiếu row → tạo mới
            if (targetRow >= data.size()) {
                data.add(new Person("", "", "Guest"));
            }

            Person person = data.get(targetRow);

            for (int j = 0; j < cols.length; j++) {
                int targetCol = startCol + j;
                String value = cols[j];

                switch (targetCol) {
                    case 0 -> person.setId(value);
                    case 1 -> person.setName(value);
                    case 2 -> person.setRole(value);
                }
            }
        }

        table.refresh();
    }

}
