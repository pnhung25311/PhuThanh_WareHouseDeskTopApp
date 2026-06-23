package com.phuthanh.warehouse.EditableTableView.tableView.cart;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.controlsfx.control.textfield.TextFields;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.helper.function.SendNotificationByTelegram;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.info.Bill;
import com.phuthanh.model.info.Business;
import com.phuthanh.model.info.Country;
import com.phuthanh.model.info.Employee;
import com.phuthanh.model.info.Manufacturer;
import com.phuthanh.model.info.Payment;
import com.phuthanh.model.info.Supplier;
import com.phuthanh.model.info.Unit;
import com.phuthanh.model.info.Vehicle;
import com.phuthanh.model.warehouse.CCBdata;
import com.phuthanh.store.AppState;
import com.phuthanh.warehouse.EditableTableView.modelTable.CartFX;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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
import javafx.util.StringConverter;
@SuppressWarnings("unchecked")
public class EditableTableViewUpdateCart {
    private final TableView<CartFX> table = new TableView<>();
    private final ObservableList<CartFX> data = FXCollections.observableArrayList();
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final SendNotificationByTelegram sendNotificationByTelegram = new SendNotificationByTelegram();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    // ===== LOAD DATA =====
    private final List<Country> countries = dbInfoHelper.getAllCountries();
    private final List<Manufacturer> manufacturers = dbInfoHelper.getAllManufacturer();
    private final List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
    private final List<Unit> units = dbInfoHelper.getAllUnits();
    private final List<Vehicle> vehicles = dbInfoHelper.getAllVehicels();
    private final List<Business> businesses = dbInfoHelper.getAllBusiness();
    private final List<Employee> employees = dbInfoHelper.getAllEmployee();
    private final List<Bill> bills = dbInfoHelper.getAllBills();
    private final List<Payment> payments = dbInfoHelper.getAllPayments();
    private final List<CCBdata> StatusVAT = new ArrayList<>();

    private final DbCRUDHelper dao = new DbCRUDHelper();

    public EditableTableViewUpdateCart() {
        createTable();
        setStyleTableView(table);
    }

    public TableView<CartFX> getTable() {
        return table;
    }

    // ================= TABLE =================
    private void createTable() {
        StatusVAT.add(new CCBdata(1, "Đã xuất VAT"));
        StatusVAT.add(new CCBdata(0, "Chưa xuất VAT"));

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);

        enableDoubleClickEdit();
        enablePasteFromExcel();

        String business = "", deliveryTime = "", reportDate = "", priceCost = "";
        business = "Nhập/xuất hàng về đơn vị";
        deliveryTime = "Ngày nhập/xuất kho";
        reportDate = "Ngày mua/xuất hóa đơn";
        priceCost = "Giá mua/bán dự kiến";

        TableColumn<CartFX, String> colCartAID = colString("Mã đơn hàng", 150, p -> p.getCartAID());
        TableColumn<CartFX, String> colProductID = colString("Mã sản phẩm", 150, p -> p.getProductID());
        TableColumn<CartFX, String> colQty = colString("Số lượng", 150, p -> p.getQty());
        TableColumn<CartFX, String> colPriceNET = colMoney("Đơn giá", 150, p -> p.getPriceNET());
        colCartAID.setOnEditCommit(e -> {
            CartFX row = e.getRowValue();
            row.getCartAID().set(e.getNewValue());
            loadProductFromDB(row); // ⭐ AUTO LOAD HERE
        });
        colPriceNET.setOnEditCommit(e -> {
            CartFX row = e.getRowValue();
            String formatted = formatNumber(e.getNewValue());
            row.getPriceNET().set(formatted);
            loadTotal(row); // ⭐ AUTO LOAD HERE
        });
        colQty.setOnEditCommit(e -> {
            CartFX row = e.getRowValue();
            String formatted = formatNumber(e.getNewValue());
            row.getQty().set(formatted);
            loadTotal(row); // ⭐ AUTO LOAD HERE
        });
        table.getColumns().add(colCartAID);
        table.getColumns().add(colProductID);
        table.getColumns().add(colString("Danh điểm", 120, c -> c.getId_PartNo()));
        table.getColumns().add(colString("Tên sản phẩm", 120, c -> c.getNameProduct()));
        table.getColumns().add(colString("Thông số", 120, c -> c.getParameter()));

        table.getColumns().add(colCombo("Hãng SX", manufacturers,
                Manufacturer::getManufacturerID,
                Manufacturer::getName,
                c -> c.getManufacturerID()));
        table.getColumns().add(colCombo("Nước SX", countries,
                Country::getCountryID,
                Country::getName,
                c -> c.getCountryID()));
        table.getColumns().add(colCombo("ĐVT", units,
                Unit::getUnitID,
                Unit::getName,
                c -> c.getUnitID()));
        table.getColumns().add(colCombo("Hãng xe", vehicles,
                Vehicle::getVehicleID,
                Vehicle::getVehicleTypeName,
                c -> c.getVehicleTypeID()));
        table.getColumns().add(colQty);
        table.getColumns().add(colPriceNET);
        table.getColumns().add(colMoney("Thành tiền", 150, c -> c.getTotal()));
        table.getColumns().add(colMoney("Giá VAT", 150, c -> c.getPriceVAT()));
        table.getColumns().add(colMoney("Giá vốn tham thảo", 150, c -> c.getCogs()));
        table.getColumns().add(colString("Mã sản phẩm VAT", 150, c -> c.getProductIDVAT()));
        table.getColumns().add(colCombo("Hóa đơn", bills,
                Bill::getBillID,
                Bill::getName,
                c -> c.getBillID()));
        table.getColumns().add(colCombo("Tình trạng thanh toán", payments,
                Payment::getPaymentID,
                Payment::getName,
                c -> c.getPaymentID()));
        table.getColumns().add(colString(priceCost, 120, c -> c.getPriceCost()));

        table.getColumns().add(colMoney("Giá bán VAT dự kiến", 150, c -> c.getGrossPriceVAT()));

        table.getColumns().add(colString("Số hóa đơn", 150, c -> c.getInvoiceNumber()));

        table.getColumns().add(colCombo("Trạng thái VAT", StatusVAT,
                CCBdata::getId,
                CCBdata::getName,
                c -> c.getStatusVAT()));
        table.getColumns().add(colString("Hợp đồng", 150, c -> c.getContractID()));

        table.getColumns().add(colCombo(business, businesses,
                Business::getBusinessID,
                Business::getName,
                c -> c.getBusinessID()));
        table.getColumns().add(colCombo("Nơi lấy hàng", suppliers,
                Supplier::getSupplierID,
                Supplier::getName,
                c -> c.getSourceID()));
        table.getColumns().add(colCombo("Nơi giao hàng", suppliers,
                Supplier::getSupplierID,
                Supplier::getName,
                c -> c.getDeliveryID()));
        table.getColumns().add(colDate(deliveryTime, 150, c -> c.getDeliveryTime()));
        table.getColumns().add(colDate(reportDate, 150, c -> c.getReportDate()));
        table.getColumns().add(colCombo("Nhân viên", employees,
                Employee::getEmployeeID,
                Employee::getNameEmployee,
                c -> c.getEmployeeID()));

        table.getColumns().add(colString("Ghi chú", 250, p -> p.getRemark()));

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

    private TableColumn<CartFX, String> colMoney(
            String title,
            double width,
            Function<CartFX, StringProperty> propertyGetter) {

        TableColumn<CartFX, String> col = new TableColumn<>(title);
        col.setPrefWidth(width);

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

                // ⭐⭐ GẮN FORMATTER TIỀN TỆ NGAY TẠI ĐÂY ⭐⭐
                functionHelper.setupMoneyField(textField);

                textField.setOnAction(e -> commitEdit(textField.getText()));

                textField.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (!newV)
                        commitEdit(textField.getText());
                });
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);

                CartFX row = getTableView().getItems().get(getIndex());
                // ⭐ FIX QUAN TRỌNG
                String formatted = formatNumber(newValue);
                propertyGetter.apply(row).set(formatted);

                // ⭐⭐ THÊM DÒNG NÀY ⭐⭐
                loadTotal(row);
            }
        });

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
                String value = cleanExcelValue(cols[j]);
                switch (targetCol) {
                    case 0 -> {
                        c.getCartAID().set(value);
                        loadProductFromDB(c); // ⭐ ADD DÒNG NÀY
                    }
                    case 1 -> c.getProductID().set(value);
                    case 2 -> c.getId_PartNo().set(value);
                    case 3 -> c.getNameProduct().set(value);
                    case 4 -> c.getParameter().set(value);
                    case 5 -> c.getManufacturerID().set(
                            findIdByName(manufacturers, Manufacturer::getManufacturerID, Manufacturer::getName,
                                    value));
                    case 6 -> c.getCountryID().set(
                            findIdByName(countries, Country::getCountryID, Country::getName, value));
                    case 7 -> c.getUnitID().set(
                            findIdByName(units, Unit::getUnitID, Unit::getName, value));
                    case 8 -> c.getVehicleTypeID()
                            .set(findIdByName(vehicles, Vehicle::getVehicleID, Vehicle::getVehicleTypeName, value));
                    case 9 -> {
                        c.getQty().set(value.isEmpty() ? "0" : value);
                    }
                    case 10 -> {
                        c.getPriceNET().set(value);
                    }
                    case 11 -> c.getTotal().set(value);
                    case 12 -> c.getPriceVAT().set(value);
                    case 13 -> c.getCogs().set(value);
                    case 14 -> c.getProductIDVAT().set(value);
                    case 15 -> c.getBillID().set(
                            findIdByName(bills, Bill::getBillID, Bill::getName, value));
                    case 16 -> c.getPaymentID().set(
                            findIdByName(payments, Payment::getPaymentID, Payment::getName, value));
                    // case 15 -> c.getPriceCost().set(value);
                    case 17 -> c.getInvoiceNumber().set(value);

                    case 18 -> c.getStatusVAT().set(
                            findIdByName(StatusVAT, CCBdata::getId, CCBdata::getName, value));
                    case 19 -> c.getContractID().set(value);
                    case 20 -> c.getBusinessID().set(
                            findIdByName(businesses, Business::getBusinessID, Business::getName, value));
                    case 21 -> c.getSourceID().set(
                            findIdByName(suppliers, Supplier::getSupplierID, Supplier::getName, value));
                    case 22 -> c.getDeliveryID().set(
                            findIdByName(suppliers, Supplier::getSupplierID, Supplier::getName, value));
                    case 23 ->
                        c.getDeliveryTime().set(LocalDate.parse(value, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    case 24 ->
                        c.getReportDate().set(LocalDate.parse(value, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    case 25 -> c.getEmployeeID().set(
                            findIdByName(employees, Employee::getEmployeeID, Employee::getNameEmployee, value));
                    case 26 -> c.getRemark().set(value);
                }

            }
        }
        // ===== SAU KHI PASTE XONG TOÀN BỘ =====
        for (CartFX row : data) {
            loadTotal(row);
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

        List<String> columns = List.of("AccountID", "ProductAID", "ProductAIDVAT", "ID_PartNo", "NameProduct",
                "ManufacturerID", "CountryID", "UnitID", "VehicleTypeID", "Parameter", "BusinessID", "Qty", "PriceNET", "Total",
                "Cogs", "PriceVAT", "GrossPriceVAT", "PaymentID", "BillID", "SourceID", "DeliveryID", "EmployeeID",
                "Status", "DeliveryTime", "ReportDate", "StatusVAT", "ContractID", "PriceCost", "InvoiceNumber",
                "Remark",
                "TypeCartID", "LastTime");

        List<List<Object>> rows = new ArrayList<>();
        List<List<Object>> whereValuesList = new ArrayList<>();
        Account user = AppState.getInstance().get("Account", Account.class);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        for (CartFX c : data) {

            if (c.getProductID().get() == null ||
                    c.getProductID().get().isBlank())
                continue;
            String productAid = dao.returnAID("Product", "ProductAID",
                    "ProductID",
                    c.getProductID().get());
            String productAidVat = dao.returnAID("Product", "ProductAID",
                    "ProductID",
                    c.getProductIDVAT().get());
            String priceNET = parseNumber(c.getPriceNET().get()) + "";
            String priceVat = parseNumber(c.getPriceVAT().get()) + "";
            String total = parseNumber(c.getTotal().get()) + "";
            String cogs = parseNumber(c.getCogs().get()) + "";
            String priceCost = parseNumber(c.getPriceCost().get()) + "";
            String grossPriceVAT = parseNumber(c.getGrossPriceVAT().get()) + "";

            whereValuesList.add(List.of(c.getCartAID().get()));
            rows.add(Arrays.asList(
                    user.getAccountID(), productAid, productAidVat,
                    safe(c.getId_PartNo().get()), safe(c.getNameProduct().get()), c.getManufacturerID().get(),
                    c.getCountryID().get(), c.getUnitID().get(), c.getVehicleTypeID().get(), c.getParameter().get(),
                    c.getBusinessID().get(), safe(c.getQty().get()), priceNET, total, cogs, priceVat, grossPriceVAT,
                    c.getPaymentID().get(), c.getBillID().get(), c.getSourceID().get(), c.getDeliveryID().get(),
                    c.getEmployeeID().get(), 0, c.getDeliveryTime().get(), c.getReportDate().get(),
                    c.getStatusVAT().get(), safe(c.getContractID().get()), priceCost,
                    safe(c.getInvoiceNumber().get()), safe(c.getRemark().get()), c.getTypeCartID().get(), now));
        }

        int[] rowsAffected = dao.updateBatch("Cart", columns, rows, List.of("CartAID"), whereValuesList);
        Account account = AppState.getInstance().get("Account", Account.class);

        boolean ok = Arrays.stream(rowsAffected)
                .noneMatch(r -> r == Statement.EXECUTE_FAILED);
        if (ok) {
            String telegram = sendNotificationByTelegram
                    .sendTelegramNotification(
                            "Bạn có " + rows.size() + " đơn hàng mới từ " + account.getFullName().toString());
            System.out.println("Telegram response: " + telegram);
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
        Button btnFillColumn = new Button("Fill xuống cột");
        Button btnClearData = new Button("Clear dữ liệu");
        btnFillColumn.setOnAction(e -> fillSelectedColumn());

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

        return new ToolBar(btnAdd, btnSave, btnDelete, btnFillColumn, btnClearData);
    }

    private void fillSelectedColumn() {

        TablePosition<CartFX, ?> pos = table.getFocusModel().getFocusedCell();

        if (pos == null || pos.getRow() < 0) {
            return;
        }

        // int columnIndex = pos.getColumn();
        int rowIndex = pos.getRow();

        CartFX selectedRow = table.getItems().get(rowIndex);
        TableColumn<CartFX, ?> column = pos.getTableColumn();

        // lấy value của cell đang chọn
        Object value = column.getCellObservableValue(selectedRow).getValue();

        if (value == null)
            return;

        // 🔥 APPLY CHO TOÀN BỘ ROW
        for (CartFX row : table.getItems()) {
            setValueToColumn(row, column, value);
        }

        table.refresh();
    }

    private void setValueToColumn(CartFX row, TableColumn<CartFX, ?> column, Object value) {

        String title = column.getText();

        switch (title) {
            case "Hãng SX" -> row.getManufacturerID().set((Integer) value);
            case "Hãng xe" -> row.getVehicleTypeID().set((Integer) value);
            case "Nước SX" -> row.getCountryID().set((Integer) value);
            case "ĐVT" -> row.getUnitID().set((Integer) value);
            case "Hóa đơn" -> row.getBillID().set((Integer) value);
            case "Tình trạng thanh toán" -> row.getPaymentID().set((Integer) value);
            case "Trạng thái VAT" -> row.getStatusVAT().set((Integer) value);
            case "Nhập hàng về đơn vị" -> row.getBusinessID().set((Integer) value);
            case "Xuất hàng từ đơn vị" -> row.getBusinessID().set((Integer) value);
            case "Hàng của đơn vị" -> row.getBusinessID().set((Integer) value);
            case "Nơi lấy hàng" -> row.getSourceID().set((Integer) value);
            case "Nơi giao hàng" -> row.getDeliveryID().set((Integer) value);
            case "Nhân viên" -> row.getEmployeeID().set((Integer) value);
            case "Ghi chú" -> row.getRemark().set((String) value);
            case "Số lượng" -> row.getQty().set((String) value);
            case "Đơn giá" -> row.getPriceNET().set((String) value);
            case "Thành tiền" -> row.getTotal().set((String) value);
            case "Giá VAT" -> row.getPriceVAT().set((String) value);
            case "Giá vốn tham thảo" -> row.getCogs().set((String) value);
            case "Giá mua NET dự kiến" -> row.getPriceCost().set((String) value);
            case "Giá bán NET dự kiến" -> row.getPriceCost().set((String) value);
            case "Ngày nhập kho" -> row.getDeliveryTime().set((LocalDate) value);
            case "Ngày xuất kho" -> row.getDeliveryTime().set((LocalDate) value);
            case "Ngày điều chuyển kho" -> row.getDeliveryTime().set((LocalDate) value);
            case "Ngày mua hóa đơn" -> row.getReportDate().set((LocalDate) value);
            case "Ngày xuất hóa đơn" -> row.getReportDate().set((LocalDate) value);
            case "Ngày giao hàng" -> row.getReportDate().set((LocalDate) value);
            case "Hợp đồng" -> row.getContractID().set((String) value);
            case "Mã sản phẩm" -> row.getProductID().set((String) value);
            case "Mã sản phẩm VAT" -> row.getProductIDVAT().set((String) value);
            case "Danh điểm" -> row.getId_PartNo().set((String) value);
            case "Tên sản phẩm" -> row.getNameProduct().set((String) value);
            case "Thông số" -> row.getParameter().set((String) value);
            case "Giá bán VAT dự kiến" -> row.getGrossPriceVAT().set((String) value);
        }
    }

    // ================= COMBOBOX =================
    private <T> TableColumn<CartFX, Integer> colCombo(
            String title,
            List<T> sourceList,
            java.util.function.Function<T, Integer> getId,
            java.util.function.Function<T, String> getName,
            javafx.util.Callback<CartFX, javafx.beans.property.IntegerProperty> prop) {

        TableColumn<CartFX, Integer> col = new TableColumn<>(title);
        col.setPrefWidth(150);
        col.setMinWidth(200);
        col.setMaxWidth(200);
        col.setResizable(false);
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
                    CartFX row = getTableView().getItems().get(getIndex());
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
                combo.setPrefWidth(column.getWidth() - 20);
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
                } else {
                    combo.getEditor().clear();
                }

                setGraphic(combo);
            }
        });

        return col;
    }

    private TableColumn<CartFX, LocalDate> colDate(
            String title, int w,
            javafx.util.Callback<CartFX, javafx.beans.property.ObjectProperty<LocalDate>> prop) {

        TableColumn<CartFX, LocalDate> col = new TableColumn<>(title);
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
            CartFX row = e.getRowValue();
            prop.call(row).set(e.getNewValue());
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

    private void loadProductFromDB(CartFX row) {

        String productID = row.getCartAID().get();
        if (productID == null || productID.isBlank())
            return;

        try {
            Map<String, Object> rs = dao.selectOneDynamic(
                    "vwCart",
                    "*",
                    "CartAID = ?",
                    List.of(productID));

            if (rs == null) {
                System.out.println("Không tìm thấy Product: " + productID);
                return;
            }
            System.out.println(rs.get("VehicleTypeID") + "================");

            // ===== STRING =====
            row.getProductAID().set(String.valueOf(rs.get("ProductAID")));
            row.getProductAIDVAT().set(String.valueOf(rs.get("ProductAIDVAT")));
            row.getProductID().set(String.valueOf(rs.get("ProductID")));
            row.getProductIDVAT().set(String.valueOf(rs.get("ProductIDVAT")));
            row.getId_PartNo().set(String.valueOf(rs.get("ID_PartNo")));
            row.getNameProduct().set(String.valueOf(rs.get("NameProduct")));
            row.getParameter().set(String.valueOf(rs.get("Parameter")));
            row.getQty().set(String.valueOf(rs.get("Qty")));
            row.getPriceNET().set(rs.get("Price") == null ? "0" : String.valueOf(rs.get("Price")));
            row.getTotal().set(rs.get("Total") == null ? "0" : String.valueOf(rs.get("Total")));
            row.getCogs().set(rs.get("Cogs") == null ? "0" : String.valueOf(rs.get("Cogs")));
            row.getPriceVAT().set(rs.get("PriceVAT") == null ? "0" : String.valueOf(rs.get("PriceVAT")));
            row.getGrossPriceVAT().set(rs.get("GrossPriceVAT") == null ? "" : String.valueOf(rs.get("GrossPriceVAT")));
            row.getPriceCost().set(rs.get("PriceCost") == null ? "0" : String.valueOf(rs.get("PriceCost")));
            row.getContractID().set(rs.get("ContractID")== null?"":String.valueOf(rs.get("ContractID")));
            row.getInvoiceNumber().set(String.valueOf(rs.get("InvoiceNumber")));
            row.getRemark().set((String) rs.get("Remark"));
            row.getManufacturerID().set(toInteger(rs.get("ManufacturerID")));
            row.getUnitID().set(toInteger(rs.get("UnitID")));
            row.getCountryID().set(toInteger(rs.get("CountryID")));
            row.getVehicleTypeID().set(toInteger(rs.get("VehicleTypeID")));
            row.getPaymentID().set(toInteger(rs.get("PaymentID")));
            row.getBillID().set(toInteger(rs.get("BillID")));
            row.getBusinessID().set(toInteger(rs.get("BusinessID")));
            row.getSourceID().set(toInteger(rs.get("SourceID")));
            row.getDeliveryID().set(toInteger(rs.get("DeliveryID")));
            row.getEmployeeID().set(toInteger(rs.get("EmployeeID")));
            row.getStatusVAT().set(toInteger(rs.get("StatusVAT")));
            row.getTypeCartID().set(toInteger(rs.get("TypeCartID")));
            row.getDeliveryTime().set(toDate(rs.get("DeliveryTime")));
            row.getReportDate().set(toDate(rs.get("ReportDate")));

            table.refresh();
            System.out.println("AUTO LOAD DONE");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadTotal(CartFX row) {
        if (row == null)
            return;

        double qty = parseNumber(row.getQty().get());
        double price = parseNumber(row.getPriceNET().get());

        double total = qty * price;

        row.getTotal().set(formatNumber(String.valueOf(total)));
    }

    private Integer toInteger(Object value) {
        if (value == null)
            return 0;

        // 1️⃣ Nếu đã là Number thì dùng luôn
        if (value instanceof Number n) {
            return n.intValue();
        }

        // 2️⃣ Nếu là String → parse
        if (value instanceof String s) {
            s = s.trim();

            if (s.isEmpty())
                return 0;

            // bỏ dấu phân cách 1.150.000 → 1150000
            s = s.replace(".", "").replace(",", "");

            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Không parse được int: " + s);
                return 0;
            }
        }

        // 3️⃣ fallback mọi kiểu khác
        return 0;
    }

    private LocalDate toDate(Object value) {
        if (value == null)
            return null;

        // 1️⃣ Nếu đã là LocalDate
        if (value instanceof LocalDate d) {
            return d;
        }

        // 2️⃣ Nếu là java.sql.Date
        if (value instanceof java.sql.Date d) {
            return d.toLocalDate();
        }

        // 3️⃣ Nếu là java.util.Date
        if (value instanceof java.util.Date d) {
            return d.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        // 4️⃣ Nếu là String
        if (value instanceof String s) {
            s = s.trim();

            if (s.isEmpty())
                return null;

            // thử nhiều format
            String[] patterns = {
                    // "yyyy-MM-dd",
                    "dd/MM/yyyy",
                    // "MM/dd/yyyy",
                    // "yyyy/MM/dd"
            };

            for (String pattern : patterns) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                    return LocalDate.parse(s, formatter);
                } catch (Exception ignored) {
                }
            }

            System.out.println("Không parse được date: " + s);
            return null;
        }

        // fallback
        return null;
    }

    private String cleanExcelValue(String value) {
        if (value == null)
            return "";

        // bỏ ký tự ẩn Excel
        value = value.replace("\r", "").trim();

        // bỏ dấu phân cách hàng nghìn 1,234 -> 1234
        value = value.replace(",", "");

        // bỏ .0 do Excel copy số nguyên
        if (value.matches("\\d+\\.0"))
            value = value.substring(0, value.length() - 2);

        return value;
    }

    private String formatNumber(String raw) {

        if (raw == null || raw.isBlank())
            return "0";

        raw = raw.replace(",", "");

        try {

            double value = Double.parseDouble(raw);

            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setGroupingSeparator(',');
            symbols.setDecimalSeparator('.');

            DecimalFormat df = new DecimalFormat("#,##0.###", symbols);

            return df.format(value);

        } catch (Exception e) {
            return "0";
        }
    }

    private Double parseNumber(Object valueObj) {

        if (valueObj == null)
            return 0.0;

        String value;

        if (valueObj instanceof StringProperty sp) {
            value = sp.get();
        } else {
            value = valueObj.toString();
        }

        if (value == null || value.isBlank())
            return 0.0;

        value = value.replace(",", "");

        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

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
