package com.phuthanh.warehouse.EditableTableView.tableView;

// import javafx.application.Platform;
import javafx.collections.*;
// import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.controlsfx.control.textfield.TextFields;

import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.model.info.*;
import com.phuthanh.warehouse.EditableTableView.modelTable.ProductFX;

public class EditableTableViewProduct {

    private final TableView<ProductFX> table = new TableView<>();
    private final ObservableList<ProductFX> data = FXCollections.observableArrayList();
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();

    // ===== LOAD DATA =====
    private final List<Country> countries = dbInfoHelper.getAllCountries();
    private final List<Manufacturer> manufacturers = dbInfoHelper.getAllManufacturer();
    private final List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
    private final List<Unit> units = dbInfoHelper.getAllUnits();
    private final List<Segment> segments = dbInfoHelper.getAllSegments();
    private final List<Purpose> purposes = dbInfoHelper.getAllPurposes();
    private final List<Vehicle> vehicles = dbInfoHelper.getAllVehicels();

    private final DbCRUDHelper dao = new DbCRUDHelper();

    public EditableTableViewProduct() {
        createTable();
    }

    public TableView<ProductFX> getTable() {
        return table;
    }

    // ================= TABLE =================
    private void createTable() {

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);

        enableDoubleClickEdit();
        enablePasteFromExcel();

        table.getColumns().add(colString("Mã gốc", 150, p -> p.productIDMainProperty()));
        table.getColumns().add(colString("Mã sản phẩm", 150, p -> p.productIDProperty()));
        table.getColumns().add(colString("Keeton", 120, p -> p.idKeetonProperty()));
        table.getColumns().add(colString("Mã công nghiệp", 120, p -> p.idIndustrialProperty()));
        table.getColumns().add(colString("Danh điểm", 150, p -> p.idPartNoProperty()));
        table.getColumns().add(colString("Danh điểm tương đương", 150, p -> p.idReplacedPartNoProperty()));
        table.getColumns().add(colString("Tên sản phẩm", 250, p -> p.nameProductProperty()));
        table.getColumns().add(colString("Thông số", 250, p -> p.parameterProperty()));

        table.getColumns().add(colString("Dòng xe", 250, p -> p.vehicleDetailProperty()));
        table.getColumns().add(colString("Cụm xe", 250, p -> p.vehicleClusterProperty()));
        table.getColumns().add(colCombo("Hãng xe", vehicles,
                Vehicle::getVehicleID,
                Vehicle::getVehicleTypeName,
                p -> p.vehicleTypeIDProperty()));


        table.getColumns().add(colCombo("Hãng SX", manufacturers,
                Manufacturer::getManufacturerID,
                Manufacturer::getName,
                p -> p.manufacturerIDProperty()));

        table.getColumns().add(colCombo("Nước SX", countries,
                Country::getCountryID,
                Country::getName,
                p -> p.countryIDProperty()));

        table.getColumns().add(colCombo("NCC giấy tờ", suppliers,
                Supplier::getSupplierID,
                Supplier::getName,
                p -> p.supplierIDProperty()));
        table.getColumns().add(colCombo("NCC thực tế", suppliers,
                Supplier::getSupplierID,
                Supplier::getName,
                p -> p.supplierActualIDProperty()));

        table.getColumns().add(colCombo("ĐVT", units,
                Unit::getUnitID,
                Unit::getName,
                p -> p.unitIDProperty()));
        table.getColumns().add(colCombo("Mãng kinh doanh", segments,
                Segment::getSegmentID,
                Segment::getName,
                p -> p.segmentIDProperty()));
        table.getColumns().add(colCombo("Mục đích", purposes,
                Purpose::getPurposeID,
                Purpose::getName,
                p -> p.purposeIDProperty()));

        table.getColumns().add(colString("Ghi chú", 250, p -> p.remarkProperty()));

        // TableColumn<ProductFX, LocalDate> dateCol = new TableColumn<>("LastTime");
        // dateCol.setCellValueFactory(c -> c.getValue().lastTimeProperty());
        // table.getColumns().add(dateCol);

        table.setItems(data);
    }

    // ================= STRING COLUMN =================
    private TableColumn<ProductFX, String> colString(String title, int w,
            javafx.util.Callback<ProductFX, javafx.beans.property.StringProperty> prop) {

        TableColumn<ProductFX, String> col = new TableColumn<>(title);
        col.setPrefWidth(w);
        col.setCellValueFactory(c -> prop.call(c.getValue()));

        col.setCellFactory(tc -> new TableCell<>() {

            private TextField textField;

            @Override
            public void startEdit() {
                super.startEdit();

                if (textField == null) {
                    createTextField();
                }

                setGraphic(textField);
                setText(null);

                textField.setText(getItem());
                textField.selectAll();

                // ✅ QUAN TRỌNG: commit khi Enter
                textField.setOnAction(e -> commitEdit(textField.getText()));

                // ✅ QUAN TRỌNG: commit khi mất focus (backup)
                textField.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (!newV) {
                        commitEdit(textField.getText());
                    }
                });
            }

            private void createTextField() {
                textField = new TextField();

                // set value ban đầu
                textField.setText(getItem());

                textField.setOnAction(e -> commitEdit(textField.getText()));

                textField.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (!newV) {
                        commitEdit(textField.getText());
                    }
                });

                textField.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                });
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
                    if (textField != null) {
                        textField.setText(item);
                    }
                    setGraphic(textField);
                    setText(null);
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }
        });

        col.setOnEditCommit(e -> prop.call(e.getRowValue()).set(e.getNewValue()));

        return col;
    }

    // ================= DOUBLE CLICK =================
    private void enableDoubleClickEdit() {
        table.setRowFactory(tv -> {
            TableRow<ProductFX> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1 && !row.isEmpty()) {
                    TablePosition<ProductFX, ?> pos = table.getFocusModel().getFocusedCell();
                    table.edit(row.getIndex(), (TableColumn) pos.getTableColumn());
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

        TablePosition<ProductFX, ?> focusedCell = table.getFocusModel().getFocusedCell();

        int startRow = focusedCell.getRow();
        int startCol = focusedCell.getColumn();

        // Nếu chưa chọn cell → fallback mode (append)
        if (startRow < 0 || startCol < 0) {
            for (String row : rows) {

                if (row.trim().isEmpty())
                    continue;

                String[] cols = row.split("\\t");

                ProductFX p = new ProductFX();

                if (cols.length > 0)
                    p.productIDMainProperty().set(cols[0]);
                if (cols.length > 1)
                    p.productIDProperty().set(cols[1]);
                if (cols.length > 2)
                    p.nameProductProperty().set(cols[2]);

                data.add(p);
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
                data.add(new ProductFX());
            }

            ProductFX p = data.get(targetRow);

            for (int j = 0; j < cols.length; j++) {

                int targetCol = startCol + j;
                String value = cols[j];

                switch (targetCol) {
                    case 0 -> p.productIDMainProperty().set(value);
                    case 1 -> p.productIDProperty().set(value);
                    case 2 -> p.idKeetonProperty().set(value);
                    case 3 -> p.idIndustrialProperty().set(value);
                    case 4 -> p.idPartNoProperty().set(value);
                    case 5 -> p.idReplacedPartNoProperty().set(value);
                    case 6 -> p.nameProductProperty().set(value);
                    case 7 -> p.parameterProperty().set(value);
                    // nếu muốn extend thêm column thì add tiếp ở đây
                }
            }
        }

        table.refresh();
    }

    // ================= ADD ROW =================
    public void addNewRow() {
        data.add(new ProductFX());
    }

    // ================= SAVE DB (BATCH) =================
    public void saveToDatabase() throws SQLException {

        List<String> columns = List.of(
                "ProductIDMain",
                "ProductID",
                "ID_Keeton",
                "ID_Industrial",
                "ID_PartNo",
                "ID_ReplacedPartNo",
                "NameProduct",
                "Parameter",
                "VehicleTypeID",
                "VehicleDetail", "VehicleCluster", 
                "ManufacturerID",
                "CountryID",
                "SupplierID",
                "SupplierActualID",
                "UnitID",
                "SegmentID",
                "PurposeID",
                "Remark",
                "LastTime");

        List<List<Object>> rows = new ArrayList<>();

        for (ProductFX p : data) {

            if (p.productIDProperty().get() == null || p.productIDProperty().get().isBlank())
                continue;
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            rows.add(Arrays.asList(
                    safe(p.productIDMainProperty().get()),
                    safe(p.productIDProperty().get()),
                    safe(p.idKeetonProperty().get()),
                    safe(p.idIndustrialProperty().get()),
                    safe(p.idPartNoProperty().get()),
                    safe(p.idReplacedPartNoProperty().get()),
                    safe(p.nameProductProperty().get()),
                    safe(p.parameterProperty().get()),
                    p.vehicleTypeIDProperty().get(),
                    safe(p.vehicleDetailProperty().get()),
                    safe(p.vehicleClusterProperty().get()),
                    p.manufacturerIDProperty().get(),
                    p.countryIDProperty().get(),
                    p.supplierIDProperty().get(),
                    p.supplierActualIDProperty().get(),
                    p.unitIDProperty().get(),
                    p.segmentIDProperty().get(),
                    p.purposeIDProperty().get(),
                    safe(p.remarkProperty().get()), now));
        }

        dao.insertBatch("Product", columns, rows);
    }

    // ================= TOOLBAR =================
    public ToolBar createToolbar() {

        Button btnAdd = new Button("➕ Add");
        Button btnSave = new Button("💾 Save DB");
        Button btnDelete = new Button("🗑 Delete");

        btnAdd.setOnAction(e -> addNewRow());

        btnSave.setOnAction(e -> {
            try {
                saveToDatabase();
                System.out.println("Saved DB success");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnDelete.setOnAction(e -> {
            ProductFX p = table.getSelectionModel().getSelectedItem();
            if (p != null)
                data.remove(p);
        });

        return new ToolBar(btnAdd, btnSave, btnDelete);
    }

    // ================= COMBOBOX =================
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
            // private final FilteredList<T> filteredList = new FilteredList<>(originalList, p -> true);

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
                // 🔥 SEARCH LOGIC
                // combo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {

                //     if (updating)
                //         return;
                //     updating = true;

                //     try {
                //         filteredList.setPredicate(item -> {
                //             if (newVal == null || newVal.isBlank())
                //                 return true;
                //             String lower = newVal.toLowerCase();
                //             String name = getName.apply(item);
                //             return name != null && name.toLowerCase().contains(lower);
                //         });

                //         combo.hide();
                //         combo.show();

                //     } finally {
                //         updating = false;
                //     }
                // });
                // 🔥 chọn item
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

    private String safe(String v) {
        return v == null ? "" : v;
    }
}