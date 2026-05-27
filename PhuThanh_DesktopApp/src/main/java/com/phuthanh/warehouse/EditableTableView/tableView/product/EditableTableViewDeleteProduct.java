package com.phuthanh.warehouse.EditableTableView.tableView.product;

import javafx.beans.property.StringProperty;
import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.input.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.warehouse.EditableTableView.modelTable.ProductFX;

public class EditableTableViewDeleteProduct {

    private final TableView<ProductFX> table = new TableView<>();
    private final ObservableList<ProductFX> data = FXCollections.observableArrayList();
    private final DbCRUDHelper dao = new DbCRUDHelper();
    // ===== UNDO STACK =====
    private final List<List<ProductFX>> historyStack = new ArrayList<>();
    private boolean isRestoring = false; // tránh loop khi restore

    public EditableTableViewDeleteProduct() {
        createTable();
    }

    public TableView<ProductFX> getTable() {
        return table;
    }

    // ================= TABLE =================
    private void createTable() {

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);

        enablePasteFromExcel();

        table.getColumns().add(colString("ProductID cần xoá", 250, p -> p.productIDProperty()));
        table.setItems(data);
    }

    // ================= ADD ROW =================
    public void addNewRow() {
        data.add(new ProductFX());
    }

    private TableColumn<ProductFX, String> colString(
            String title,
            double width,
            Function<ProductFX, StringProperty> propertyGetter) {

        TableColumn<ProductFX, String> col = new TableColumn<>(title);
        col.setPrefWidth(width);

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
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);

                // update model
                ProductFX row = getTableView().getItems().get(getIndex());
                propertyGetter.apply(row).set(newValue);
            }
        });

        return col;
    }

    // ================= PASTE EXCEL =================
    private void enablePasteFromExcel() {
        table.setOnKeyPressed(e -> {

            if (e.isControlDown() && e.getCode() == KeyCode.V) {
                pushHistory(); // lưu trước khi paste
                paste();
            }

            if (e.isControlDown() && e.getCode() == KeyCode.Z) {
                undo();
            }

            if (e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE) {
                deleteSelectedCells();
            }
        });
    }

    private void deleteSelectedCells() {

        pushHistory(); // lưu trước khi xóa

        for (TablePosition<?, ?> pos : table.getSelectionModel().getSelectedCells()) {

            int rowIndex = pos.getRow();
            int colIndex = pos.getColumn();

            if (rowIndex >= data.size())
                continue;

            ProductFX p = data.get(rowIndex);

            switch (colIndex) {
                case 0 -> p.productIDProperty().set(null);
                case 1 -> p.idKeetonProperty().set(null);
                case 2 -> p.idIndustrialProperty().set(null);
                case 3 -> p.idPartNoProperty().set(null);
                case 4 -> p.idReplacedPartNoProperty().set(null);
                case 5 -> p.nameProductProperty().set(null);
                case 6 -> p.parameterProperty().set(null);
                case 7 -> p.vehicleDetailProperty().set(null);
                case 8 -> p.vehicleClusterProperty().set(null);
                case 9 -> p.remarkProperty().set(null);
            }
        }

        table.refresh();
    }

    private void paste() {

        Clipboard cb = Clipboard.getSystemClipboard();
        if (!cb.hasString())
            return;

        String[] rows = cb.getString().split("\\r?\\n");

        for (String row : rows) {
            if (row.trim().isEmpty())
                continue;

            ProductFX p = new ProductFX();
            p.productIDProperty().set(row.trim());
            data.add(p);
        }

        table.refresh();
    }

    private void pushHistory() {
        if (isRestoring)
            return;
        historyStack.add(deepCopyData());
    }

    private void undo() {
        if (historyStack.isEmpty())
            return;

        isRestoring = true;

        List<ProductFX> lastState = historyStack.remove(historyStack.size() - 1);
        data.setAll(lastState);

        table.refresh();
        isRestoring = false;
    }

    private List<ProductFX> deepCopyData() {
        List<ProductFX> copy = new ArrayList<>();

        for (ProductFX p : data) {
            ProductFX n = new ProductFX();

            n.productIDProperty().set(p.productIDProperty().get());
            n.idKeetonProperty().set(p.idKeetonProperty().get());
            n.idIndustrialProperty().set(p.idIndustrialProperty().get());
            n.idPartNoProperty().set(p.idPartNoProperty().get());
            n.idReplacedPartNoProperty().set(p.idReplacedPartNoProperty().get());
            n.nameProductProperty().set(p.nameProductProperty().get());
            n.parameterProperty().set(p.parameterProperty().get());
            n.vehicleDetailProperty().set(p.vehicleDetailProperty().get());
            n.vehicleClusterProperty().set(p.vehicleClusterProperty().get());
            n.vehicleTypeIDProperty().set(p.vehicleTypeIDProperty().get());
            n.manufacturerIDProperty().set(p.manufacturerIDProperty().get());
            n.countryIDProperty().set(p.countryIDProperty().get());
            n.supplierIDProperty().set(p.supplierIDProperty().get());
            n.supplierActualIDProperty().set(p.supplierActualIDProperty().get());
            n.unitIDProperty().set(p.unitIDProperty().get());
            n.segmentIDProperty().set(p.segmentIDProperty().get());
            n.purposeIDProperty().set(p.purposeIDProperty().get());
            n.remarkProperty().set(p.remarkProperty().get());

            copy.add(n);
        }
        return copy;
    }

    // ================= DELETE DB =================
    public void deleteFromDatabase() throws SQLException {

        List<Object> ids = new ArrayList<>();

        for (ProductFX p : data) {
            String id = p.productIDProperty().get();

            if (id != null && !id.isBlank()) {
                ids.add(id);
            }
        }

        if (ids.isEmpty()) {
            System.out.println("Không có ProductID để xoá");
            return;
        }

        // 🔥 DELETE BATCH
        dao.deleteDynamic(
                "Product",
                "ProductID = ?",
                ids);

        System.out.println("Delete success: " + ids.size() + " sản phẩm");
        data.clear();
    }

    // ================= TOOLBAR =================
    public ToolBar createToolbar() {

        Button btnAdd = new Button("➕ Add Row");
        Button btnDeleteDB = new Button("🗑 Delete DB");
        Button btnRemoveRow = new Button("❌ Remove Row");

        btnAdd.setOnAction(e -> addNewRow());

        btnRemoveRow.setOnAction(e -> {
            ProductFX p = table.getSelectionModel().getSelectedItem();
            if (p != null)
                data.remove(p);
        });

        btnDeleteDB.setOnAction(e -> {
            try {
                deleteFromDatabase();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return new ToolBar(btnAdd, btnDeleteDB, btnRemoveRow);
    }
}