package com.phuthanh.warehouse.EditableTableView.tableView.product;

import javafx.beans.property.StringProperty;
import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.controlsfx.control.textfield.TextFields;

import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.model.info.*;
import com.phuthanh.warehouse.EditableTableView.modelTable.ProductFX;
@SuppressWarnings("unchecked")
public class EditableTableViewCreateProduct {

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
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    // ===== UNDO STACK =====
    private final List<List<ProductFX>> undoStack = new ArrayList<>();
    private final List<List<ProductFX>> redoStack = new ArrayList<>();
    private boolean isRestoring = false;
    private final int MAX_HISTORY = 50;
    // ==
    private final DbCRUDHelper dao = new DbCRUDHelper();

    public EditableTableViewCreateProduct() {
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

        table.setItems(data);

    }

    // ================= STRING COLUMN =================
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
                    pushHistory();
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
                if (!isRestoring) {
                    getTableView().getProperties().put("lastEdit", true);
                    pushHistory(); // 🔥 quan trọng nhất
                }
                super.commitEdit(newValue);

                // update model
                ProductFX row = getTableView().getItems().get(getIndex());
                propertyGetter.apply(row).set(newValue);
            }
        });

        return col;
    }

    // ================= DOUBLE CLICK =================
    private void enableDoubleClickEdit() {
        table.setRowFactory(tv -> {
            TableRow<ProductFX> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1 && !row.isEmpty()) {
                    TablePosition<ProductFX, ?> pos = table.getFocusModel().getFocusedCell();
                    table.edit(row.getIndex(), pos.getTableColumn());
                }
            });
            return row;
        });
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

            if (e.isControlDown() && e.getCode() == KeyCode.Y) {
                redo();
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

                // if (cols.length > 0)
                // p.productIDMainProperty().set(cols[0]);
                if (cols.length > 0)
                    p.productIDProperty().set(cols[0]);
                if (cols.length > 1)
                    p.nameProductProperty().set(cols[1]);

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
                    // case 0 -> p.productIDMainProperty().set(value);
                    case 0 -> p.productIDProperty().set(value);
                    case 1 -> p.idKeetonProperty().set(value);
                    case 2 -> p.idIndustrialProperty().set(value);
                    case 3 -> p.idPartNoProperty().set(value);
                    case 4 -> p.idReplacedPartNoProperty().set(value);
                    case 5 -> p.nameProductProperty().set(value);
                    case 6 -> p.parameterProperty().set(value);
                    case 7 -> p.vehicleDetailProperty().set(value);
                    case 8 -> p.vehicleClusterProperty().set(value);
                    case 9 -> p.vehicleTypeIDProperty().set(
                            findIdByName(vehicles, Vehicle::getVehicleID, Vehicle::getVehicleTypeName, value));
                    case 10 -> p.manufacturerIDProperty().set(
                            findIdByName(manufacturers, Manufacturer::getManufacturerID, Manufacturer::getName, value));
                    case 11 -> p.countryIDProperty().set(
                            findIdByName(countries, Country::getCountryID, Country::getName, value));
                    case 12 -> p.supplierIDProperty().set(
                            findIdByName(suppliers, Supplier::getSupplierID, Supplier::getName, value));
                    case 13 -> p.supplierActualIDProperty().set(
                            findIdByName(suppliers, Supplier::getSupplierID, Supplier::getName, value));
                    case 14 -> p.unitIDProperty().set(
                            findIdByName(units, Unit::getUnitID, Unit::getName, value));
                    case 15 -> p.segmentIDProperty().set(
                            findIdByName(segments, Segment::getSegmentID, Segment::getName, value));
                    case 16 -> p.purposeIDProperty().set(
                            findIdByName(purposes, Purpose::getPurposeID, Purpose::getName, value));
                    case 17 -> p.remarkProperty().set(value);
                    // case 11 -> p.parameterProperty().set(value);
                    // nếu muốn extend thêm column thì add tiếp ở đây
                }
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
        undoStack.add(deepCopyData());
        redoStack.clear(); // rất quan trọng

        // Giới hạn số lượng lịch sử
        if (undoStack.size() > MAX_HISTORY) {
            undoStack.remove(0);
        }
    }

    private void redo() {
        if (redoStack.isEmpty())
            return;

        isRestoring = true;

        undoStack.add(deepCopyData());
        List<ProductFX> state = redoStack.remove(redoStack.size() - 1);
        data.setAll(state);

        table.refresh();
        isRestoring = false;
    }

    private void undo() {
        if (undoStack.isEmpty())
            return;

        isRestoring = true;

        redoStack.add(deepCopyData());
        List<ProductFX> state = undoStack.remove(undoStack.size() - 1);
        data.setAll(state);

        table.refresh();
        isRestoring = false;
    }

    // ================= ADD ROW =================
    public void addNewRow(int numberOfRows) {
        System.out.println("Add " + numberOfRows + " new rows");
        pushHistory(); // lưu trước khi thêm
        for (int i = 0; i < numberOfRows; i++) {
            data.add(new ProductFX());
        }
    }

    private boolean checkCriteria(String input, int supplierId, int supplierActualId, int manufacturerId,
            int countryId, int unitId) throws SQLException {
        return dbInfoHelper.checkCriteriaProduct(input, supplierId, supplierActualId, manufacturerId, countryId,
                unitId);
    }

    private boolean checkCombobox(Integer supplierId, Integer supplierActualId, Integer manufacturerId,
            Integer countryId, Integer unitId) {
        if (supplierId == null || supplierActualId == null || manufacturerId == null || countryId == null
                || unitId == null) {
            return false;
        }

        return true;
    }

    private boolean validateInput(String productId) {

        // Kiểm tra rỗng
        if (productId.isEmpty()) {
            customDialogNotification.showDialog("Cảnh báo", "Bạn chưa nhập mã sản phẩm!", Alert.AlertType.WARNING);
            return false;
        }

        try {
            boolean exists = dao.isProductIdExists(productId);
            if (exists) {
                customDialogNotification.showDialog("Cảnh báo", "Mã sản phẩm '" + productId + "' đã tồn tại!",
                        Alert.AlertType.WARNING);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi", "Không thể kiểm tra mã sản phẩm: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            return false;
        }

        // Nếu đến đây => hợp lệ
        System.out.println("Product ID is valid.");
        return true;
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
            if (!validateInput(p.productIDProperty().get()))
                return;
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            String condition = p.idPartNoProperty().get() == null ? p.parameterProperty().get()
                    : p.idPartNoProperty().get();
            if (condition == null) {
                customDialogNotification.showDialog("Cảnh báo",
                        "Mã " + p.productIDProperty().get() + " phải nhập Mã danh điểm hoặc Thông số kỹ thuật!",
                        Alert.AlertType.WARNING);
                return;
            }

            if (checkCombobox(p.supplierIDProperty().get(), p.supplierActualIDProperty().get(),
                    p.manufacturerIDProperty().get(),
                    p.countryIDProperty().get(), p.unitIDProperty().get()) == false) {
                customDialogNotification.showDialog("Cảnh báo",
                        "Mã " + p.productIDProperty().get()
                                + " phải chọn đủ các tiêu chí: Nhà cung cấp, Nhà cung cấp thực tế, Nhà sản xuất, Quốc gia, Đơn vị tính!",
                        Alert.AlertType.WARNING);
                return;
            }

            if (checkCriteria(condition, p.supplierIDProperty().get(), p.supplierActualIDProperty().get(),
                    p.manufacturerIDProperty().get(),
                    p.countryIDProperty().get(), p.unitIDProperty().get())) {
                customDialogNotification.showDialog("Cảnh báo",
                        "Mã " + p.productIDProperty().get()
                                + " sản phẩm có cùng Mã danh điểm hoặc Thông số kỹ thuật đã tồn tại với Nhà cung cấp, Nhà sản xuất, Quốc gia, Đơn vị tính này!",
                        Alert.AlertType.WARNING);
                return;
            }

            rows.add(Arrays.asList(
                    removePrefixLetters(safe(p.productIDProperty().get())),
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

        int[] rowsAffected = dao.insertBatch("Product", columns, rows);

        boolean ok = Arrays.stream(rowsAffected)
                .noneMatch(r -> r == Statement.EXECUTE_FAILED);
        if (ok) {
            customDialogNotification.showDialog("Thành công", "Lưu thành công", Alert.AlertType.INFORMATION);
        } else {
            customDialogNotification.showDialog("Có dòng lưu thất bại", "Lưu thất bại", Alert.AlertType.ERROR);
        }

    }

    private String removePrefixLetters(String input) {
        if (input == null)
            return null;
        return input.replaceFirst("^[A-Za-z]+", "");
    }

    private void clearDataTableview() {
        table.getItems().clear();
    }

    // ================= TOOLBAR =================
    public ToolBar createToolbar() {

        Button btnAdd = new Button("➕ thêm 10 dòng");
        Button btnSave = new Button("💾 Lưu");
        Button btnDelete = new Button("🗑 Xóa dòng");
        Button btnFillColumn = new Button("Fill xuống cột");
        Button btnClearData = new Button("Clear dữ liệu");

        btnFillColumn.setOnAction(e -> fillSelectedColumn());

        btnClearData.setOnAction(e -> {
            clearDataTableview();
        });

        btnAdd.setOnAction(e -> addNewRow(10));

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

        return new ToolBar(btnAdd, btnSave, btnDelete, btnFillColumn, btnClearData);
    }

    // ================= COMBOBOX =================
    private <T> TableColumn<ProductFX, Integer> colCombo(
            String title,
            List<T> sourceList,
            java.util.function.Function<T, Integer> getId,
            java.util.function.Function<T, String> getName,
            javafx.util.Callback<ProductFX, javafx.beans.property.IntegerProperty> prop) {

        TableColumn<ProductFX, Integer> col = new TableColumn<>(title);
        col.setPrefWidth(200);
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
                // 🔥 SEARCH LOGIC
                // combo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {

                // if (updating)
                // return;
                // updating = true;

                // try {
                // filteredList.setPredicate(item -> {
                // if (newVal == null || newVal.isBlank())
                // return true;
                // String lower = newVal.toLowerCase();
                // String name = getName.apply(item);
                // return name != null && name.toLowerCase().contains(lower);
                // });

                // combo.hide();
                // combo.show();

                // } finally {
                // updating = false;
                // }
                // });
                // 🔥 chọn item
                combo.setOnAction(e -> {
                    ProductFX row = getTableView().getItems().get(getIndex());
                    if (row == null)
                        return;
                    pushHistory(); // lưu trước khi thay đổi

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

    private <T> Integer findIdByName(
            List<T> list,
            java.util.function.Function<T, Integer> getId,
            java.util.function.Function<T, String> getName,
            String text) {

        if (text == null || text.isBlank())
            return null;

        String search = text.trim().toLowerCase();

        for (T item : list) {
            String name = getName.apply(item);
            if (name != null && name.trim().toLowerCase().equals(search)) {
                return getId.apply(item);
            }
        }
        return null;
    }

    private String safe(String v) {
        return v == null ? "" : v;
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