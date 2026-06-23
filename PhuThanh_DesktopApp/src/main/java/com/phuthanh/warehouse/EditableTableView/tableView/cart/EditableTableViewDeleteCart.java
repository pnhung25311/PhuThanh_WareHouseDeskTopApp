package com.phuthanh.warehouse.EditableTableView.tableView.cart;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;

public class EditableTableViewDeleteCart {
    private final TableView<StringProperty> table = new TableView<>();

    private final ObservableList<StringProperty> data = FXCollections.observableArrayList();

    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();

    public EditableTableViewDeleteCart() {
        createTable();
    }

    public TableView<StringProperty> getTable() {
        return table;
    }

    // ================= TABLE =================
    private void createTable() {

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);
        String header = "Mã đơn hàng";

        TableColumn<StringProperty, String> col = new TableColumn<>(header);

        col.setCellValueFactory(c -> c.getValue());

        col.setCellFactory(tc -> new TableCell<>() {

            private TextField textField;

            @Override
            public void startEdit() {
                if (isEmpty())
                    return;

                super.startEdit();
                createTextField();

                setText(null);
                setGraphic(textField);

                textField.selectAll();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem() == null ? "" : getItem());
                setGraphic(null);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    if (textField != null)
                        textField.setText(item);
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }

            private void createTextField() {

                textField = new TextField(getItem());

                textField.setOnAction(e -> commitEdit(textField.getText()));

                textField.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (!newV) {
                        commitEdit(textField.getText());
                    }
                });

                textField.setOnKeyPressed(e -> {

                    // TableView<StringProperty> tv = getTableView();

                    int row = getIndex();
                    // int col = tv.getColumns().indexOf(getTableColumn());

                    switch (e.getCode()) {

                        case ENTER -> {
                            commitEdit(textField.getText());
                            moveToCell(row + 1);
                            e.consume();
                        }

                        case DOWN -> {
                            commitEdit(textField.getText());
                            moveToCell(row + 1);
                            e.consume();
                        }

                        case UP -> {
                            commitEdit(textField.getText());
                            moveToCell(Math.max(0, row - 1));
                            e.consume();
                        }

                        default -> {
                        }
                    }
                });
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);

                StringProperty row = getTableView().getItems().get(getIndex());
                row.set(newValue);
            }
        });

        col.setPrefWidth(300);

        table.getColumns().add(col);
        table.setItems(data);
        enablePaste();
    }

    // ================= TOOLBAR =================
    public ToolBar createToolbar() {

        Button btnAdd = new Button("➕ Thêm dòng");
        Button btnDelete = new Button("🗑 Xóa dòng");
        Button btnClear = new Button("Clear");
        Button btnPrint = new Button("Lưu");

        btnAdd.setOnAction(e -> addNewRow());
        btnDelete.setOnAction(e -> deleteSelectedRow());
        btnPrint.setOnAction(e -> printAllData());

        return new ToolBar(btnAdd, btnDelete, btnClear, btnPrint);
    }

    // ================= ACTIONS =================
    public void addNewRow() {
        data.add(new SimpleStringProperty(""));
    }

    public void deleteSelectedRow() {
        int index = table.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            data.remove(index);
        }
    }

    // ================= PASTE EXCEL =================
    private void enablePaste() {

        table.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.V) {
                paste();
            }
        });
    }

    private void paste() {

        Clipboard cb = Clipboard.getSystemClipboard();
        if (!cb.hasString())
            return;

        String[] rows = cb.getString().split("\\r?\\n");

        // TablePosition<StringProperty, ?> focused =
        // table.getFocusModel().getFocusedCell();

        // int startRow = focused.getRow();
        int startRow = table.getFocusModel().getFocusedIndex();

        // ===== chưa focus cell → append =====
        if (startRow < 0) {
            for (String r : rows) {
                if (!r.isBlank()) {
                    data.add(new SimpleStringProperty(r));
                }
            }
            table.refresh();
            return;
        }

        // ===== paste theo vị trí =====
        for (int i = 0; i < rows.length; i++) {

            int targetRow = startRow + i;

            if (targetRow >= data.size()) {
                data.add(new SimpleStringProperty(""));
            }

            data.get(targetRow).set(rows[i]);
        }

        table.refresh();
    }

    // ================= MOVE CELL =================
    private void moveToCell(int row) {

        if (row < 0)
            row = 0;

        if (row >= table.getItems().size()) {
            addNewRow();
        }

        int targetRow = row;

        Platform.runLater(() -> {
            table.getSelectionModel().clearAndSelect(targetRow);
            table.getFocusModel().focus(targetRow);
            table.scrollTo(targetRow);
            table.edit(targetRow, table.getColumns().get(0));
        });
    }

    private void printAllData() {

        String condition = "";
        for (StringProperty property : data) {

            String keyword = property.get().trim();

            if (keyword.isEmpty()) {
                continue;
            }
            condition += keyword + ",";
        }
        if (condition.endsWith(",")) {
            condition = condition.substring(0, condition.length() - 1);
        }

        try {
            dbCRUDHelper.executeUpdate("DELETE FROM Cart WHERE CartAID IN (" + condition + ")");

            customDialogNotification.showDialog("Thành công", "Lưu thành công", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            // TODO: handle exception

            customDialogNotification.showDialog("Thất bại", "Lưu thất bại: " + e.getMessage(), Alert.AlertType.ERROR);
        }

    }

}
