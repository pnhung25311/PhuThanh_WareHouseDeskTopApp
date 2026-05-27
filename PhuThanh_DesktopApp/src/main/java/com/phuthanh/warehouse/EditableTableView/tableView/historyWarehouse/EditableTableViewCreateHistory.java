package com.phuthanh.warehouse.EditableTableView.tableView.historyWarehouse;

import javafx.beans.property.StringProperty;
import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.controlsfx.control.textfield.TextFields;

import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.custom.CustomCombobox;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbHelper;
import com.phuthanh.model.info.*;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;
import com.phuthanh.warehouse.EditableTableView.modelTable.HistoryWareHouseFX;

public class EditableTableViewCreateHistory {

    private final TableView<HistoryWareHouseFX> table = new TableView<>();
    private final ObservableList<HistoryWareHouseFX> data = FXCollections.observableArrayList();
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final DrawerItem selectedDrawerItem;
    private final String typeHistory;

    // ===== LOAD DATA =====
    private final List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
    private final List<Employee> employees = dbInfoHelper.getAllEmployee();

    private final DbCRUDHelper dao = new DbCRUDHelper();
    private final ArrayCRUD arrayCRUD = new ArrayCRUD();

    public EditableTableViewCreateHistory(DrawerItem item, String typeHistory) {
        this.selectedDrawerItem = item;
        this.typeHistory = typeHistory;
        createTable();
    }

    public TableView<HistoryWareHouseFX> getTable() {
        return table;
    }

    // ================= TABLE =================
    private void createTable() {

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);

        enableDoubleClickEdit();
        enablePasteFromExcel();
        TableColumn<HistoryWareHouseFX, String> colProductID = colString("Mã sản phẩm", 150, p -> p.getProductID());
        colProductID.setOnEditCommit(e -> {
            HistoryWareHouseFX row = e.getRowValue();
            row.getProductID().set(e.getNewValue());
            loadProductFromDB(row); // ⭐ AUTO LOAD HERE
        });

        table.getColumns().add(colProductID);
        table.getColumns().add(colString("Số lượng", 120, p -> p.getQty()));

        table.getColumns().add(colCombo("Nhân viên", employees,
                Employee::getEmployeeID,
                Employee::getNameEmployee,
                p -> p.getId_Employee()));

        table.getColumns().add(colCombo("Đối tác", suppliers,
                Supplier::getSupplierID,
                Supplier::getName,
                p -> p.getSupplierID()));

        table.getColumns().add(colString("Ghi chú", 250, p -> p.getRemark()));
        table.getColumns().add(colDate("Thời gian", 250, p -> p.getTime()));
        table.getColumns().add(colString("Vị trí", 200, p -> p.getLocation()));

        table.setItems(data);
    }

    // ================= STRING COLUMN =================
    private TableColumn<HistoryWareHouseFX, String> colString(
            String title,
            double width,
            Function<HistoryWareHouseFX, StringProperty> propertyGetter) {

        TableColumn<HistoryWareHouseFX, String> col = new TableColumn<>(title);
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
                HistoryWareHouseFX row = getTableView().getItems().get(getIndex());
                propertyGetter.apply(row).set(newValue);
            }
        });

        return col;
    }

    // ================= DOUBLE CLICK =================
    private void enableDoubleClickEdit() {
        table.setRowFactory(tv -> {
            TableRow<HistoryWareHouseFX> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1 && !row.isEmpty()) {
                    TablePosition<HistoryWareHouseFX, ?> pos = table.getFocusModel().getFocusedCell();
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

        TablePosition<HistoryWareHouseFX, ?> focusedCell = table.getFocusModel().getFocusedCell();

        int startRow = focusedCell.getRow();
        int startCol = focusedCell.getColumn();

        // Nếu chưa chọn cell → fallback mode (append)
        if (startRow < 0 || startCol < 0) {
            for (String row : rows) {

                if (row.trim().isEmpty())
                    continue;

                String[] cols = row.split("\\t");

                HistoryWareHouseFX h = new HistoryWareHouseFX();

                if (cols.length > 0)
                    h.getHistoryAID().set(cols[0]);
                if (cols.length > 1)
                    h.getDataWareHouseAID().set(cols[1]);
                if (cols.length > 2)
                    h.getQty().set(cols[2]);

                data.add(h);
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
                data.add(new HistoryWareHouseFX());
            }

            HistoryWareHouseFX h = data.get(targetRow);

            for (int j = 0; j < cols.length; j++) {

                int targetCol = startCol + j;
                String value = cols[j];

                switch (targetCol) {
                    case 0 -> h.getProductID().set(value);
                    case 1 -> h.getQty().set(value);
                    case 2 -> h.getId_Employee().set(
                            findIdByName(employees, Employee::getEmployeeID, Employee::getNameEmployee, value));
                    case 3 -> h.getSupplierID().set(
                            findIdByName(suppliers, Supplier::getSupplierID, Supplier::getName, value));
                    case 4 -> h.getRemark().set(value);
                }
            }
        }

        table.refresh();
    }

    // ================= ADD ROW =================
    public void addNewRow() {
        System.out.println("Add new row");
        data.add(new HistoryWareHouseFX());
    }

    // ================= SAVE DB (BATCH) =================
    public void saveToDatabase() throws SQLException {

        Account user = AppState.getInstance().get("Account", Account.class);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        Map<String, Double> qtyCache = new HashMap<>();

        try (Connection conn = DbHelper.getConnection()) {

            conn.setAutoCommit(false);

            String insertSQL = "INSERT INTO %s (DataWareHouseAID, Qty, ID_Employee, Partner, Remark, Time, TransferGroupID, LastUser, LastTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    .formatted(selectedDrawerItem.getWareHouseDataBaseHistory());

            String updateWarehouseSQL = "UPDATE %s SET Qty = ?, LocationID = ?, LastUser = ?, LastTime = GETDATE() WHERE DataWareHouseAID = ? "
                    .formatted(selectedDrawerItem.getWareHouseDataBase());

            try (
                    PreparedStatement psInsert = conn.prepareStatement(insertSQL);
                    PreparedStatement psUpdate = conn.prepareStatement(updateWarehouseSQL)) {

                int batchCount = 0;

                for (HistoryWareHouseFX p : data) {

                    if (p.getProductID().get() == null || p.getProductID().get().isBlank())
                        continue;

                    String productID = p.getProductID().get();
                    double qty = Double.parseDouble(p.getQty().get());

                    if (qty == 0) {
                        throw new SQLException("Qty = 0 không hợp lệ: " + productID);
                    }
                    qty = typeHistory.equals("IMPORT") ? Math.abs(qty) : -Math.abs(qty);

                    // ===== 1. TÌM AID =====
                    String aid = dao.returnAID(
                            selectedDrawerItem.getWareHouseTable(),
                            "DataWareHouseAID",
                            "ProductID",
                            productID);

                    // ===== 2. CHƯA CÓ WAREHOUSE → TẠO =====
                    if (aid == null || aid.isEmpty()) {

                        String proaid = dao.returnAID("Product", "ProductAID", "ProductID", productID);

                        if (proaid == null || proaid.isEmpty()) {
                            throw new SQLException("Không tìm thấy Product: " + productID);
                        }
                        List<String> columnsWarehouse = new ArrayList<>(arrayCRUD.warehouseColumns);
                        columnsWarehouse.remove("DataWareHouseAID");
                        List<Object> values = Arrays.asList(
                                proaid, 0, 0,
                                "", "",
                                now, user.getUserName(), null);
                        dao.insert(selectedDrawerItem.getWareHouseDataBase(), columnsWarehouse, values);

                        aid = dao.returnAID(
                                selectedDrawerItem.getWareHouseTable(),
                                "DataWareHouseAID",
                                "ProductID",
                                productID);
                    }

                    // ===== 3. INSERT HISTORY =====
                    psInsert.setString(1, aid);
                    psInsert.setDouble(2, qty);
                    psInsert.setObject(3, p.getId_Employee().get());
                    psInsert.setObject(4, p.getSupplierID().get());
                    psInsert.setString(5, safe(p.getRemark().get()));
                    psInsert.setObject(6, p.getTime().get());
                    psInsert.setObject(7, null);
                    psInsert.setString(8, user.getUserName());
                    psInsert.setTimestamp(9, now);

                    psInsert.addBatch();

                    // ===== 4. TÍNH TỒN MỚI (CACHE) =====
                    double currentQty;

                    if (qtyCache.containsKey(aid)) {
                        currentQty = qtyCache.get(aid);
                    } else {
                        currentQty = dao.sumQtyHistory(selectedDrawerItem.getWareHouseDataBaseHistory(),
                                Integer.parseInt(aid));
                    }

                    double newQty = currentQty + qty;
                    qtyCache.put(aid, newQty);

                    // ===== 5. UPDATE WAREHOUSE =====
                    psUpdate.setDouble(1, newQty);
                    psUpdate.setObject(2, p.getLocation().get());
                    psUpdate.setString(3, user.getUserName());
                    psUpdate.setString(4, aid);

                    psUpdate.addBatch();

                    batchCount++;

                    if (batchCount % 300 == 0) {
                        psInsert.executeBatch();
                        psUpdate.executeBatch();
                    }
                }

                psInsert.executeBatch();
                psUpdate.executeBatch();

                conn.commit();
                System.out.println("SAVE HISTORY SUCCESS (Transaction)");

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
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
            HistoryWareHouseFX p = table.getSelectionModel().getSelectedItem();
            if (p != null)
                data.remove(p);
        });

        return new ToolBar(btnAdd, btnSave, btnDelete);
    }

    // ================= COMBOBOX =================
    private <T> TableColumn<HistoryWareHouseFX, Integer> colCombo(
            String title,
            List<T> sourceList,
            java.util.function.Function<T, Integer> getId,
            java.util.function.Function<T, String> getName,
            javafx.util.Callback<HistoryWareHouseFX, javafx.beans.property.IntegerProperty> prop) {

        TableColumn<HistoryWareHouseFX, Integer> col = new TableColumn<>(title);
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
                // 🔥 chọn item
                combo.setOnAction(e -> {
                    HistoryWareHouseFX row = getTableView().getItems().get(getIndex());
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

    // ================= DATE COLUMN =================
    private TableColumn<HistoryWareHouseFX, LocalDate> colDate(
            String title, int w,
            javafx.util.Callback<HistoryWareHouseFX, javafx.beans.property.ObjectProperty<LocalDate>> prop) {

        TableColumn<HistoryWareHouseFX, LocalDate> col = new TableColumn<>(title);
        col.setPrefWidth(w);
        col.setEditable(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        col.setCellValueFactory(c -> prop.call(c.getValue()));

        col.setCellFactory(column -> new TableCell<>() {

            private DatePicker datePicker;

            @Override
            public void startEdit() {
                if (isEmpty())
                    return;

                super.startEdit();

                if (datePicker == null) {
                    datePicker = new DatePicker(getItem());

                    // format hiển thị dd/MM/yyyy
                    datePicker.setConverter(new StringConverter<>() {
                        @Override
                        public String toString(LocalDate date) {
                            return date == null ? "" : formatter.format(date);
                        }

                        @Override
                        public LocalDate fromString(String text) {
                            if (text == null || text.isBlank())
                                return null;
                            return LocalDate.parse(text, formatter);
                        }
                    });

                    // chọn ngày → commit ngay
                    datePicker.setOnAction(e -> commitEdit(datePicker.getValue()));

                    // Enter / ESC
                    datePicker.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ENTER) {
                            commitEdit(datePicker.getValue());
                        } else if (e.getCode() == KeyCode.ESCAPE) {
                            cancelEdit();
                        }
                    });
                }

                datePicker.setValue(getItem());
                setGraphic(datePicker);
                setText(null);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(formatDate(getItem()));
                setGraphic(null);
            }

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                if (isEditing()) {
                    if (datePicker != null)
                        datePicker.setValue(item);
                    setGraphic(datePicker);
                    setText(null);
                } else {
                    setText(formatDate(item));
                    setGraphic(null);
                }
            }

            private String formatDate(LocalDate date) {
                return date == null ? "" : formatter.format(date);
            }
        });

        col.setOnEditCommit(e -> {
            HistoryWareHouseFX row = e.getRowValue();
            prop.call(row).set(e.getNewValue());
        });

        return col;
    }

    private void loadProductFromDB(HistoryWareHouseFX row) {

        String productID = row.getProductID().get();
        if (productID == null || productID.isBlank())
            return;

        try {
            Map<String, Object> rs = dao.selectOneDynamic(
                    selectedDrawerItem.getWareHouseTable(),
                    "*",
                    "ProductID = ?",
                    List.of(productID));

            if (rs == null) {
                System.out.println("Không tìm thấy Product: " + productID);
                return;
            }

            // ===== STRING =====
            row.getLocation().set((String) rs.get("LocationID"));

            table.refresh();
            System.out.println("AUTO LOAD DONE");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
