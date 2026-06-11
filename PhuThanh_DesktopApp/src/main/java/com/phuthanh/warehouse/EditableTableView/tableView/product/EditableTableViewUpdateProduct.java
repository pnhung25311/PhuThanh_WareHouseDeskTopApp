package com.phuthanh.warehouse.EditableTableView.tableView.product;

import javafx.beans.property.StringProperty;
import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.model.info.*;
import com.phuthanh.warehouse.EditableTableView.modelTable.ProductFX;
@SuppressWarnings("unchecked")
public class EditableTableViewUpdateProduct {

    private final TableView<ProductFX> table = new TableView<>();
    private final ObservableList<ProductFX> data = FXCollections.observableArrayList();
    private final DbCRUDHelper dao = new DbCRUDHelper();
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();

    // ================= MASTER DATA =================
    private final List<Country> countries = dbInfoHelper.getAllCountries();
    private final List<Manufacturer> manufacturers = dbInfoHelper.getAllManufacturer();
    private final List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
    private final List<Unit> units = dbInfoHelper.getAllUnits();
    private final List<Segment> segments = dbInfoHelper.getAllSegments();
    private final List<Purpose> purposes = dbInfoHelper.getAllPurposes();
    // ===== UNDO STACK =====
    private final List<List<ProductFX>> historyStack = new ArrayList<>();
    private boolean isRestoring = false; // tránh loop khi restore

    public EditableTableViewUpdateProduct() {
        createTable();
        enablePaste();
        enableDoubleClickEdit();
    }

    public TableView<ProductFX> getTable() {
        return table;
    }

    // =========================================================
    // AUTO LOAD PRODUCT FROM DB ⭐⭐⭐
    // =========================================================
    private void loadProductFromDB(ProductFX row) {

        String productID = row.productIDProperty().get();
        if (productID == null || productID.isBlank())
            return;

        try {
            Map<String, Object> rs = dao.selectOneDynamic(
                    "Product",
                    "*",
                    "ProductID = ?",
                    List.of(productID));

            if (rs == null) {
                System.out.println("Không tìm thấy Product: " + productID);
                return;
            }

            // ===== STRING =====
            row.productIDMainProperty().set((String) rs.get("ProductIDMain"));
            row.idKeetonProperty().set((String) rs.get("ID_Keeton"));
            row.idIndustrialProperty().set((String) rs.get("ID_Industrial"));
            row.idPartNoProperty().set((String) rs.get("ID_PartNo"));
            row.idReplacedPartNoProperty().set((String) rs.get("ID_ReplacedPartNo"));
            row.nameProductProperty().set((String) rs.get("NameProduct"));
            row.parameterProperty().set((String) rs.get("Parameter"));
            row.remarkProperty().set((String) rs.get("VehicleDetail"));
            row.remarkProperty().set((String) rs.get("VehicleCluster"));
            row.remarkProperty().set((String) rs.get("Remark"));

            // ===== INTEGER SAFE (KHÔNG BAO GIỜ CRASH) =====
            row.manufacturerIDProperty().set(toInteger(rs.get("ManufacturerID")));
            row.countryIDProperty().set(toInteger(rs.get("CountryID")));
            row.supplierIDProperty().set(toInteger(rs.get("SupplierID")));
            row.supplierActualIDProperty().set(toInteger(rs.get("SupplierActualID")));
            row.unitIDProperty().set(toInteger(rs.get("UnitID")));
            row.segmentIDProperty().set(toInteger(rs.get("SegmentID")));
            row.purposeIDProperty().set(toInteger(rs.get("PurposeID")));

            table.refresh();
            System.out.println("AUTO LOAD DONE");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // =========================================================
    // CREATE TABLE
    // =========================================================
    private void createTable() {

        table.setEditable(true);
        table.setItems(data);

        // ⭐⭐⭐ PRODUCT ID (HOOK AUTO LOAD)
        TableColumn<ProductFX, String> colProductID = colString("Mã sản phẩm", 160, p -> p.productIDProperty());

        colProductID.setOnEditCommit(e -> {
            ProductFX row = e.getRowValue();
            row.productIDProperty().set(e.getNewValue());
            loadProductFromDB(row); // ⭐ AUTO LOAD HERE
        });

        table.getColumns().add(colProductID);

        table.getColumns().add(colString("Mã gốc", 140, p -> p.productIDMainProperty()));
        table.getColumns().add(colString("Keeton", 120, p -> p.idKeetonProperty()));
        table.getColumns().add(colString("Mã công nghiệp", 120, p -> p.idIndustrialProperty()));
        table.getColumns().add(colString("Danh điểm", 150, p -> p.idPartNoProperty()));
        table.getColumns().add(colString("Danh điểm tương đương", 150, p -> p.idReplacedPartNoProperty()));
        table.getColumns().add(colString("Tên sản phẩm", 250, p -> p.nameProductProperty()));
        table.getColumns().add(colString("Thông số kỹ thuật", 250, p -> p.parameterProperty()));
        table.getColumns().add(colString("Ghi chú", 250, p -> p.remarkProperty()));

        table.getColumns().add(colCombo("Hãng sản xuất", manufacturers,
                Manufacturer::getManufacturerID, Manufacturer::getName, p -> p.manufacturerIDProperty()));

        table.getColumns().add(colCombo("Quốc gia", countries,
                Country::getCountryID, Country::getName, p -> p.countryIDProperty()));

        table.getColumns().add(colCombo("Nhà cung cấp", suppliers,
                Supplier::getSupplierID, Supplier::getName, p -> p.supplierIDProperty()));

        table.getColumns().add(colCombo("Nhà cung cấp thực tế", suppliers,
                Supplier::getSupplierID, Supplier::getName, p -> p.supplierActualIDProperty()));

        table.getColumns().add(colCombo("Đơn vị tính", units,
                Unit::getUnitID, Unit::getName, p -> p.unitIDProperty()));

        table.getColumns().add(colCombo("Mảng kinh doanh", segments,
                Segment::getSegmentID, Segment::getName, p -> p.segmentIDProperty()));

        table.getColumns().add(colCombo("Mục đích", purposes,
                Purpose::getPurposeID, Purpose::getName, p -> p.purposeIDProperty()));
    }

    // =========================================================
    // STRING COLUMN
    // =========================================================
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

    // =========================================================
    // COMBO COLUMN
    // =========================================================
    private <T> TableColumn<ProductFX, Integer> colCombo(
            String title,
            List<T> sourceList,
            java.util.function.Function<T, Integer> getId,
            java.util.function.Function<T, String> getName,
            javafx.util.Callback<ProductFX, javafx.beans.property.IntegerProperty> prop) {

        TableColumn<ProductFX, Integer> col = new TableColumn<>(title);
        col.setPrefWidth(180);
        col.setCellValueFactory(c -> prop.call(c.getValue()).asObject());

        col.setCellFactory(column -> new TableCell<>() {

            private final ComboBox<T> combo = new ComboBox<>();
            // private boolean updating = false;
            private final ObservableList<T> originalList = FXCollections.observableArrayList(sourceList);
            // private final FilteredList<T> filteredList = new FilteredList<>(originalList,
            // p -> true);

            {
                combo.setEditable(true);
                combo.setItems(originalList);

                combo.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(T obj) {
                        return obj == null ? "" : getName.apply(obj);
                    }

                    @Override
                    public T fromString(String s) {
                        return null;
                    }
                });
                TextFields.bindAutoCompletion(combo.getEditor(), combo.getItems());
                combo.setOnAction(e -> {
                    ProductFX row = getTableView().getItems().get(getIndex());
                    if (row == null)
                        return;

                    T item = combo.getValue();
                    if (item == null)
                        return;

                    prop.call(row).set(getId.apply(item));

                });
                combo.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(T obj) {
                        return obj == null ? "" : getName.apply(obj);
                    }

                    @Override
                    public T fromString(String text) {
                        if (text == null || text.isBlank())
                            return null;

                        // tìm item theo name
                        for (T item : originalList) {
                            String name = getName.apply(item);
                            if (name != null && name.equalsIgnoreCase(text)) {
                                return item;
                            }
                        }

                        return combo.getValue(); // 🔥 cực quan trọng
                    }
                });
                // 🔥 commit khi mất focus
                combo.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (!newV) {
                        combo.hide();
                    }
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                T selected = originalList.stream()
                        .filter(i -> {
                            Integer id = getId.apply(i);
                            return id != null && id.equals(item);
                        })
                        .findFirst()
                        .orElse(null);

                combo.setValue(selected);

                // 🔥 QUAN TRỌNG NHẤT: sync text hiển thị
                if (selected != null) {
                    combo.getEditor().setText(getName.apply(selected));
                    System.out.println(getName.apply(selected));
                    System.out.println(getId.apply(selected));
                } else {
                    combo.getEditor().clear();
                }

                setGraphic(combo);
            }
        });

        return col;
    }

    // =========================================================
    // PASTE EXCEL ⭐ AUTO LOAD WHEN PASTE PRODUCT ID
    // =========================================================
    private void enablePaste() {
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
                case 0 -> {
                    p.productIDProperty().set(null);
                    loadProductFromDB(p);
                }
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
        TablePosition<ProductFX, ?> pos = table.getFocusModel().getFocusedCell();

        for (int i = 0; i < rows.length; i++) {

            String[] cols = rows[i].split("\\t");
            int r = pos.getRow() + i;
            if (r >= data.size())
                data.add(new ProductFX());
            ProductFX p = data.get(r);

            if (cols.length > 0) {
                p.productIDProperty().set(cols[0]);
                loadProductFromDB(p); // ⭐ AUTO LOAD
            }
        }

        table.refresh();
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

    // =========================================================
    // UPDATE ALL
    // =========================================================
    public void updateAllRows() {

        for (ProductFX p : data) {

            if (p.productIDProperty().get() == null || p.productIDProperty().get().isEmpty())
                continue;

            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("ProductIDMain", p.productIDMainProperty().get());
            fields.put("ID_Keeton", p.idKeetonProperty().get());
            fields.put("ID_Industrial", p.idIndustrialProperty().get());
            fields.put("ID_PartNo", p.idPartNoProperty().get());
            fields.put("ID_ReplacedPartNo", p.idReplacedPartNoProperty().get());
            fields.put("NameProduct", p.nameProductProperty().get());
            fields.put("Parameter", p.parameterProperty().get());
            fields.put("ManufacturerID", p.manufacturerIDProperty().get());
            fields.put("CountryID", p.countryIDProperty().get());
            fields.put("SupplierID", p.supplierIDProperty().get());
            fields.put("SupplierActualID", p.supplierActualIDProperty().get());
            fields.put("UnitID", p.unitIDProperty().get());
            fields.put("SegmentID", p.segmentIDProperty().get());
            fields.put("PurposeID", p.purposeIDProperty().get());
            fields.put("Remark", p.remarkProperty().get());
            fields.put("LastTime", Timestamp.valueOf(LocalDateTime.now()));

            try {
                dao.updateDynamic("Product", fields,
                        "ProductID = ?",
                        List.of(p.productIDProperty().get()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("UPDATE DONE");
    }

    // =========================================================
    // TOOLBAR
    // =========================================================
    public ToolBar createToolbar() {
        Button add = new Button("➕ Thêm 10 dòng");
        Button update = new Button("💾 Lưu");
        Button btnFillColumn = new Button("Fill xuống cột");
        Button btnClearData = new Button("Clear dữ liệu");

        add.setOnAction(e -> addNewRow(10));
        update.setOnAction(e -> updateAllRows());
        btnFillColumn.setOnAction(e -> fillSelectedColumn());

        btnClearData.setOnAction(e -> {
            clearDataTableview();
        });

        return new ToolBar(add, update, btnFillColumn, btnClearData);
    }

    private void clearDataTableview() {
        table.getItems().clear();
    }

    public void addNewRow(int numberOfRows) {
        System.out.println("Add " + numberOfRows + " new rows");

        for (int i = 0; i < numberOfRows; i++) {
            data.add(new ProductFX());
        }
    }

    // DOUBLE CLICK EDIT
    private void enableDoubleClickEdit() {
        table.setRowFactory(tv -> {
            TableRow<ProductFX> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    table.edit(row.getIndex(), table.getColumns().get(0));
                }
            });
            return row;
        });
    }

    private Integer toInteger(Object value) {
        if (value == null)
            return 0;
        return ((Number) value).intValue();
    }

    private void fillSelectedColumn() {

        TablePosition<ProductFX, ?> pos = table.getFocusModel().getFocusedCell();

        if (pos == null || pos.getRow() < 0) {
            return;
        }

        // int columnIndex = pos.getColumn();
        int rowIndex = pos.getRow();

        ProductFX selectedRow = table.getItems().get(rowIndex);
        TableColumn<ProductFX, ?> column = pos.getTableColumn();

        // lấy value của cell đang chọn
        Object value = column.getCellObservableValue(selectedRow).getValue();

        if (value == null)
            return;

        // 🔥 APPLY CHO TOÀN BỘ ROW
        for (ProductFX row : table.getItems()) {
            setValueToColumn(row, column, value);
        }

        table.refresh();
    }

    private void setValueToColumn(ProductFX row, TableColumn<ProductFX, ?> column, Object value) {

        String title = column.getText();

        switch (title) {
            case "Hãng SX" -> row.manufacturerIDProperty().set((Integer) value);
            case "Hãng xe" -> row.vehicleTypeIDProperty().set((Integer) value);
            case "Nước SX" -> row.countryIDProperty().set((Integer) value);
            case "ĐVT" -> row.unitIDProperty().set((Integer) value);
            case "Mã sản phẩm" -> row.productIDProperty().set((String) value);
            case "Danh điểm" -> row.idPartNoProperty().set((String) value);
            case "Danh điểm tương đương" -> row.idReplacedPartNoProperty().set((String) value);
            case "Tên sản phẩm" -> row.nameProductProperty().set((String) value);
            case "Keeton" -> row.idKeetonProperty().set((String) value);
            case "Thông số" -> row.idKeetonProperty().set((String) value);
            case "Dòng xe" -> row.vehicleDetailProperty().set((String) value);
            case "Cụm xe" -> row.vehicleClusterProperty().set((String) value);
            case "NCC giấy tờ" -> row.supplierIDProperty().set((Integer) value);
            case "NCC thực tế" -> row.supplierActualIDProperty().set((Integer) value);
            case "Mãng kinh doanh" -> row.segmentIDProperty().set((Integer) value);
            case "Mục đích" -> row.purposeIDProperty().set((Integer) value);
            case "Ghi chú" -> row.remarkProperty().set((String) value);
        }
    }

}