package com.phuthanh.business.EditableTableView;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.manager.TableViewManagerBusiness;
import com.phuthanh.model.business.ProductBusiness;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class SearchMassTableView {
    private final TableView<StringProperty> table = new TableView<>();
    private final ObservableList<StringProperty> data = FXCollections.observableArrayList();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final TableViewManagerBusiness tableViewManagerBusiness = new TableViewManagerBusiness();
    private final Stage stage;

    private final int typeSearch;
    private final ObservableList<ProductBusiness> items;
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    public SearchMassTableView(int typeSearch, Stage stage, ObservableList<ProductBusiness> items) {
        this.typeSearch = typeSearch;
        this.stage = stage;
        this.items = items;
        createTable();
    }

    public TableView<StringProperty> getTable() {
        return table;
    }

    public ObservableList<StringProperty> getData() {
        return data;
    }

    // ================= TABLE =================
    private void createTable() {

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);
        String header = switch (typeSearch) {
            case 1 -> "Mã sản phẩm";
            case 2 -> "Danh điểm";
            case 3 -> "Mã vạch";
            default -> "Value";
        };

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
        Button btnPrint = new Button("📤 Print Data");

        btnAdd.setOnAction(e -> addNewRow());
        btnDelete.setOnAction(e -> deleteSelectedRow());
        btnClear.setOnAction(e -> data.clear());
        btnPrint.setOnAction(e -> printAllData());

        return new ToolBar(btnAdd, btnDelete, btnClear, btnPrint);
    }

    private void printAllData() {
        TableView<ProductBusiness> tv = new TableView<>();
        // ObservableList<ProductBusiness> allData ;

        ObservableList<ProductBusiness> searchResult = FXCollections.observableArrayList();

        for (int i = 0; i < data.size(); i++) {

            String row = data.get(i).get();

            if (typeSearch == 2) {
                searchResult.addAll(items.stream()
                                .filter(item -> (item.danhDiem != null && !item.danhDiem.isEmpty() && item.danhDiem.contains(row))
                                        || (item.boDanhDiem != null && !item.boDanhDiem.isEmpty() && item.boDanhDiem.contains(row)))
                                .toList());
            } else {
                searchResult.addAll(items.stream()
                        .filter(item -> item.maVatTu.trim().equals(row))
                        .toList());
            }
        }
        try {
            tableViewManagerBusiness.createBusinessColumns(tv);

            tv.setItems(searchResult);

            boolean success = functionHelper.exportExcel(tv, stage, "sheet1");
            String title = success ? "Thành công" : "Lỗi";
            String message = success ? "Xuất Excel thành công" : "Xuất Excel thất bại";
            Alert.AlertType type = success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;
            customDialogNotification.showDialog(title, message, type);
            tv.getItems().clear();
        } catch (Exception e) {
            // TODO: handle exception
            // System.out.println(e.getMessage());
        }

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

        // TablePosition<StringProperty, ?> focused = table.getFocusModel().getFocusedCell();

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
}
