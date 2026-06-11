package com.phuthanh.warehouse.EditableTableView.tableView.cart;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.warehouse.EditableTableView.modelTable.CartFX;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
@SuppressWarnings("unchecked")
public class EditableTableViewConfirmCart {
    private final TableView<CartFX> table = new TableView<>();
    private final ObservableList<CartFX> data = FXCollections.observableArrayList();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    // ===== LOAD DATA =====

    private final DbCRUDHelper dao = new DbCRUDHelper();
    private final int typeConfirm;

    public EditableTableViewConfirmCart(int type) {
        this.typeConfirm = type;
        createTable();
        setStyleTableView(table);
    }

    public TableView<CartFX> getTable() {
        return table;
    }

    // ================= TABLE =================
    private void createTable() {

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);

        enableDoubleClickEdit();
        enablePasteFromExcel();

        TableColumn<CartFX, String> colCartAID = colString("Mã đơn hàng", 150, p -> p.getCartAID());
        table.getColumns().add(colCartAID);

        table.setItems(data);
    }

    // ================= STRING COLUMN =================
    private TableColumn<CartFX, String> colString(
            String title,
            double width,
            Function<CartFX, StringProperty> propertyGetter) {

        TableColumn<CartFX, String> col = new TableColumn<>(title);
        col.setPrefWidth(200);

        // bind dữ liệu
        col.setCellValueFactory(cell -> propertyGetter.apply(cell.getValue()));

        col.setCellFactory(tc -> new TableCell<>() {

            private TextField textField;

            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    createTextField();
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }

            @Override
            public void updateItem(String item, boolean empty) {
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

                // ENTER → commit
                textField.setOnAction(e -> commitEdit(textField.getText()));

                // mất focus → commit
                textField.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (!newV)
                        commitEdit(textField.getText());
                });
                textField.setOnKeyPressed(e -> {

                    TableView<CartFX> tv = getTableView();

                    int row = getIndex();
                    int col = tv.getColumns().indexOf(getTableColumn());

                    switch (e.getCode()) {

                        case TAB -> {
                            commitEdit(textField.getText());

                            if (e.isShiftDown()) {
                                moveToCell(row, Math.max(0, col - 1));
                            } else {
                                moveToCell(row, col + 1);
                            }

                            e.consume();
                        }

                        case ENTER -> {
                            commitEdit(textField.getText());

                            moveToCell(row + 1, col);

                            e.consume();
                        }

                        case RIGHT -> {
                            commitEdit(textField.getText());

                            moveToCell(row, col + 1);

                            e.consume();
                        }

                        case LEFT -> {
                            commitEdit(textField.getText());

                            moveToCell(row, Math.max(0, col - 1));

                            e.consume();
                        }

                        case DOWN -> {
                            commitEdit(textField.getText());

                            moveToCell(row + 1, col);

                            e.consume();
                        }

                        case UP -> {
                            commitEdit(textField.getText());

                            moveToCell(Math.max(0, row - 1), col);

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

                // update model
                CartFX row = getTableView().getItems().get(getIndex());
                propertyGetter.apply(row).set(newValue);
            }

        }

        );

        return col;
    }

    // ================= DOUBLE CLICK =================
    private void enableDoubleClickEdit() {
        table.setRowFactory(tv -> {
            TableRow<CartFX> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1 && !row.isEmpty()) {
                    TablePosition<CartFX, ?> pos = table.getFocusModel().getFocusedCell();
                    table.edit(row.getIndex(), pos.getTableColumn());
                }
            });
            return row;
        });
    }

    // ================= PASTE EXCEL =================
    private void enablePasteFromExcel() {
        table.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.V)
                paste();
        });
    }

    private void paste() {

        Clipboard cb = Clipboard.getSystemClipboard();
        if (!cb.hasString())
            return;

        String[] rows = cb.getString().split("\\r?\\n");

        TablePosition<CartFX, ?> focusedCell = table.getFocusModel().getFocusedCell();

        int startRow = focusedCell.getRow();
        int startCol = focusedCell.getColumn();

        // Nếu chưa chọn cell → fallback mode (append)
        if (startRow < 0 || startCol < 0) {
            for (String row : rows) {

                if (row.trim().isEmpty())
                    continue;

                String[] cols = row.split("\\t");

                CartFX c = new CartFX();

                if (cols.length > 0)
                    c.getProductID().set(cols[0]);
                if (cols.length > 1)
                    c.getId_PartNo().set(cols[1]);
                if (cols.length > 2)
                    c.getNameProduct().set(cols[2]);

                data.add(c);
            }
            table.refresh();
            return;
        }

        // ===== PASTE THEO VỊ TRÍ CELL =====
        for (int i = 0; i < rows.length; i++) {

            String rowText = rows[i];
            if (rowText.trim().isEmpty())
                continue;

            String[] cols = rowText.split("\\t");

            int targetRow = startRow + i;

            if (targetRow >= data.size()) {
                data.add(new CartFX());
            }

            CartFX c = data.get(targetRow);

            for (int j = 0; j < cols.length; j++) {

                int targetCol = startCol + j;
                String value = cols[j];
                switch (targetCol) {
                    case 0 -> {
                        c.getCartAID().set(value);
                    }
                }

            }
        }
        table.refresh();
    }

    // ================= ADD ROW =================
    public void addNewRow(int numberOfRows) {
        System.out.println("Add " + numberOfRows + " new rows");

        for (int i = 0; i < numberOfRows; i++) {
            data.add(new CartFX());
        }
    }

    // ================= SAVE DB (BATCH) =================
    public void saveToDatabase() throws SQLException {

        List<String> columns = List.of(
                "Status", "LastTime");

        List<List<Object>> rows = new ArrayList<>();
        List<List<Object>> whereValuesList = new ArrayList<>();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        for (CartFX c : data) {

            if (c.getCartAID().get() == null ||
                    c.getCartAID().get().isBlank())
                continue;
            String str = c.getCartAID().get();
            if (str.contains(",")) {
                String[] arr = str.split(",");
                for (String s : arr) {
                    whereValuesList.add(List.of(s.trim()));
                    rows.add(Arrays.asList(typeConfirm, now));
                }
            } else {
                whereValuesList.add(List.of(c.getCartAID().get()));
                rows.add(Arrays.asList(typeConfirm, now));
            }

            System.out.println("CartAID = [" + c.getCartAID().get() + "]");
        }
        System.out.println("==============");
        System.out.println(whereValuesList);
        System.out.println(rows);
        System.out.println("==============");


        int[] rowsAffected = dao.updateBatch("Cart", columns, rows, List.of("CartAID"), whereValuesList);

        boolean ok = Arrays.stream(rowsAffected)
                .noneMatch(r -> r == Statement.EXECUTE_FAILED);
        if (ok) {
            customDialogNotification.showDialog("Thành công", "Lưu thành công", Alert.AlertType.INFORMATION);
            clearDataTableview();
        } else {
            customDialogNotification.showDialog("Có dòng lưu thất bại", "Lưu thất bại", Alert.AlertType.ERROR);
        }
    }

    private void clearDataTableview() {
        table.getItems().clear();
    }

    // ================= TOOLBAR =================
    public ToolBar createToolbar() {

        Button btnAdd = new Button("➕ Thêm 10 dòng");
        Button btnSave = new Button("💾 Lưu");
        Button btnDelete = new Button("🗑 Xóa dòng");
        Button btnClearData = new Button("Clear dữ liệu");

        btnAdd.setOnAction(e -> addNewRow(10));

        btnSave.setOnAction(e -> {
            try {
                saveToDatabase();
                System.out.println("Saved DB success");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnClearData.setOnAction(e -> {
            clearDataTableview();
        });

        btnDelete.setOnAction(e -> {
            CartFX c = table.getSelectionModel().getSelectedItem();
            if (c != null)
                data.remove(c);
        });

        return new ToolBar(btnAdd, btnSave, btnDelete, btnClearData);
    }

    // private String cleanExcelValue(String value) {
    //     if (value == null)
    //         return "";

    //     // bỏ ký tự ẩn Excel
    //     value = value.replace("\r", "").trim();

    //     // bỏ dấu phân cách hàng nghìn 1,234 -> 1234
    //     value = value.replace(",", "");

    //     // bỏ .0 do Excel copy số nguyên
    //     if (value.matches("\\d+\\.0"))
    //         value = value.substring(0, value.length() - 2);

    //     return value;
    // }

    private void setStyleTableView(TableView<?> table) {

        Platform.runLater(() -> {

            // 🔵 Làm to ScrollBar
            for (Node node : table.lookupAll(".scroll-bar")) {
                if (node instanceof ScrollBar sb) {

                    if (sb.getOrientation() == Orientation.VERTICAL) {
                        sb.setPrefWidth(22);
                        sb.setMinWidth(22);
                        sb.setMaxWidth(22);
                    } else {
                        sb.setPrefHeight(22);
                        sb.setMinHeight(22);
                        sb.setMaxHeight(22);
                    }
                }
            }

            // 🔥 CSS Excel viết trực tiếp trong code
            String css = """
                        .scroll-bar:vertical {
                            -fx-pref-width: 22;
                        }
                        .scroll-bar:horizontal {
                            -fx-pref-height: 22;
                        }

                        .scroll-bar .track {
                            -fx-background-color: #F1F1F1;
                            -fx-background-radius: 10;
                        }

                        .scroll-bar .thumb {
                            -fx-background-color: #C1C1C1;
                            -fx-background-radius: 10;
                        }

                        .scroll-bar .thumb:hover {
                            -fx-background-color: #A8A8A8;
                        }

                        .scroll-bar .thumb:pressed {
                            -fx-background-color: #8E8E8E;
                        }

                        .scroll-bar .increment-button,
                        .scroll-bar .decrement-button {
                            -fx-background-color: transparent;
                            -fx-padding: 0;
                        }
                    """;

            // 👉 Inject CSS trực tiếp vào Scene
            table.getScene().getStylesheets().add(
                    "data:text/css," + css.replace("\n", ""));
        });
    }

    private void moveToCell(int row, int col) {

        int maxCol = table.getColumns().size() - 1;

        if (col < 0)
            col = 0;

        if (col > maxCol)
            col = maxCol;

        if (row < 0)
            row = 0;

        if (row >= table.getItems().size()) {
            addNewRow(1);
        }

        final int targetRow = row;
        final int targetCol = col;

        Platform.runLater(() -> {

            TableColumn<CartFX, ?> column = table.getColumns().get(targetCol);

            table.getSelectionModel()
                    .clearAndSelect(targetRow, column);

            table.getFocusModel()
                    .focus(targetRow, column);

            table.scrollTo(targetRow);

            table.edit(targetRow, column);
        });
    }

}
