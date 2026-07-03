package com.phuthanh.warehouse.screen.dialog;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.phuthanh.custom.CustomCombobox;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.info.Employee;
import com.phuthanh.model.info.Supplier;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class DialogTransferHistoryWareHouse {

    // ===== FROM =====
    @FXML
    private TextField txtQtyFrom;
    @FXML
    private TextField txtLocationID;

    @FXML
    private ComboBox<DrawerItem> WareHouseFrom;

    @FXML
    private ComboBox<Employee> EmployeeFrom;

    @FXML
    private ComboBox<Supplier> PartnerFrom;

    @FXML
    private DatePicker txtTimeFrom;

    @FXML
    private TextArea txtRemarkFrom;

    // ===== TO =====
    @FXML
    private TextField txtQtyTo;

    @FXML
    private ComboBox<DrawerItem> WareHouseTo;

    @FXML
    private ComboBox<Employee> EmployeeTo;

    @FXML
    private ComboBox<Supplier> PartnerTo;

    @FXML
    private DatePicker txtTimeTo;

    @FXML
    private TextArea txtRemarkTo;

    // ===== BUTTONS =====
    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;
    private DrawerItem selectedDrawerItem;
    private String codeAID;
    private Runnable callback;
    List<Supplier> suppliersHistory;
    List<DrawerItem> warehouse;
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private final ArrayCRUD arrayCRUD = new ArrayCRUD();
    private final CustomCombobox customCombobox = new CustomCombobox();

    // ===== INITIALIZE =====
    @FXML
    private void initialize() {
        // TODO: load dữ liệu combobox, set default value
        // loadComboBox();
        setupAutoFill();
    }

    // ===== EVENTS =====
    @FXML
    private void onSave() {
        try {
            Account accountFromState = AppState.getInstance().get("Account", Account.class);
            DrawerItem warehouseTo = WareHouseTo.getSelectionModel().getSelectedItem();
            if (warehouseTo == null) {
                customDialogNotification.showDialog("Lỗi", "Vui lòng chọn kho hàng chuyển đến!", Alert.AlertType.ERROR);
                return;
            }

            List<String> columnsHistory = new ArrayList<>(arrayCRUD.historyColumns);
            columnsHistory.remove("HistoryAID");
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            String whAIDFrom = codeAID;

            String productAID = dbCRUDHelper.returnAID(selectedDrawerItem.getWareHouseTable(), "ProductAID",
                    "DataWareHouseAID", whAIDFrom);
            String whAIDTo = dbCRUDHelper.returnAID(warehouseTo.getWareHouseTable(), "DataWareHouseAID",
                    "ProductAID", productAID);
            if (whAIDTo == null || whAIDTo.isEmpty()) {
                String tableWh = warehouseTo.getWareHouseDataBase();
                List<String> columnsWarehouse = new ArrayList<>(arrayCRUD.warehouseColumns);
                columnsWarehouse.remove("DataWareHouseAID");
                List<Object> values = Arrays.asList(
                        productAID, 0, 0,
                        "", "",
                        timestamp, accountFromState.getUserName(), "");
                dbCRUDHelper.insert(tableWh, columnsWarehouse, values);
                whAIDTo = dbCRUDHelper.returnAID(warehouseTo.getWareHouseTable(), "DataWareHouseAID",
                        "ProductAID", productAID);
            }
            // else {
            // customDialogNotification.showDialog("Thất bại", "Kho hàng chuyển đến chưa có
            // sản phẩm này!",
            // Alert.AlertType.ERROR);
            // }
            String tableFrom = selectedDrawerItem.getWareHouseDataBaseHistory();
            String tableTo = warehouseTo.getWareHouseDataBaseHistory();
            String transFerCode = functionHelper.generateTransferGroupID(selectedDrawerItem.getWareHouseID(),
                    warehouseTo.getWareHouseID());

            List<Object> valuesHisFrom = Arrays.asList(whAIDFrom, txtQtyFrom.getText().trim(),
                    functionHelper.getComboBoxItemById(EmployeeFrom, Employee::getEmployeeID,
                            Employee::getNameEmployee),
                    functionHelper.getComboBoxItemById(PartnerFrom, Supplier::getSupplierID, Supplier::getName),
                    txtRemarkFrom.getText().trim(), txtTimeFrom.getValue(), transFerCode,
                    accountFromState.getUserName(), timestamp);
            List<Object> valuesHisTo = Arrays.asList(whAIDTo, txtQtyTo.getText().trim(),
                    functionHelper.getComboBoxItemById(EmployeeTo, Employee::getEmployeeID, Employee::getNameEmployee),
                    functionHelper.getComboBoxItemById(PartnerTo, Supplier::getSupplierID, Supplier::getName),
                    txtRemarkTo.getText().trim(), txtTimeTo.getValue(), transFerCode,
                    accountFromState.getUserName(), timestamp);
            int rowFrom = dbCRUDHelper.insert(tableFrom, columnsHistory, valuesHisFrom);
            int rowTo = dbCRUDHelper.insert(tableTo, columnsHistory, valuesHisTo);
            String message = "";
            if (rowFrom > 0) {
                String tableHistory = selectedDrawerItem.getWareHouseDataBaseHistory();
                String tableWh = selectedDrawerItem.getWareHouseDataBase();

                double totalQty = dbCRUDHelper.sumQtyHistory(tableHistory, Integer.parseInt(codeAID));
                System.out.println(totalQty);

                int updateRow = dbCRUDHelper.update(tableWh, Arrays.asList("Qty", "LocationID", "LastTime"),
                        Arrays.asList(totalQty, txtLocationID.getText().trim(), timestamp), "DataWareHouseAID = ?",
                        Arrays.asList(codeAID));
                // System.out.println(updateRow);
                if (updateRow > 0) {
                    message += "Lưu lịch sử " + selectedDrawerItem.getNameWareHouse() + " thành công!\n";
                }
            }
            if (rowTo > 0) {
                String tableHistory = warehouseTo.getWareHouseDataBaseHistory();
                String tableWh = warehouseTo.getWareHouseDataBase();

                double totalQty = dbCRUDHelper.sumQtyHistory(tableHistory, Integer.parseInt(whAIDTo));
                System.out.println(totalQty);

                int updateRow = dbCRUDHelper.update(tableWh, Arrays.asList("Qty", "LocationID", "LastTime"),
                        Arrays.asList(totalQty, txtLocationID.getText().trim(), timestamp), "DataWareHouseAID=?",
                        Arrays.asList(whAIDTo));
                if (updateRow > 0) {
                    message += "Lưu lịch sử " + warehouseTo.getNameWareHouse() + " thành công!\n";
                }
            }
            if (callback != null) {
                callback.run();
            }
            customDialogNotification.showDialog("Thành công", message, Alert.AlertType.INFORMATION);
            onCloseClick();
        } catch (Exception e) {
            // TODO: handle exception
            customDialogNotification.showDialog("Thất bại", e.getMessage(), Alert.AlertType.ERROR);

        }
        // TODO: xử lý lưu dữ liệu
    }

    @FXML
    private void onCloseClick() {
        // TODO: đóng dialog
        btnCancel.getScene().getWindow().hide();
    }

    private void loadComboBox() {
        selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);

        suppliersHistory = dbInfoHelper.getAllSuppliersById4();
        customCombobox.setupComboBox(PartnerFrom, suppliersHistory, Supplier::getSupplierID, Supplier::getName);
        List<Employee> employees = dbInfoHelper.getAllEmployee();
        customCombobox.setupComboBox(EmployeeFrom, employees, Employee::getEmployeeID, Employee::getNameEmployee);

        // List<Supplier> suppliersHistoryTo = dbInfoHelper.getAllSuppliers();
        // CustomCombobox.setupComboBox(PartnerTo, suppliersHistoryTo,
        // Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(EmployeeTo, employees, Employee::getEmployeeID, Employee::getNameEmployee);

        warehouse = dbInfoHelper.getWareHouseDataBase();
        // CustomCombobox.setupComboBox(WareHouseFrom, warehouseForm,
        // DrawerItem::getWareHouseID, DrawerItem::getWareHouseName);
        WareHouseFrom.getItems().addAll(warehouse);
        WareHouseFrom.getSelectionModel().select(selectedDrawerItem);
        if (selectedDrawerItem != null) {
            warehouse.stream()
                    .filter(w -> w.getWareHouseID() == selectedDrawerItem.getWareHouseID())
                    .findFirst()
                    .ifPresent(w -> WareHouseFrom.getSelectionModel().select(w));
        }
        WareHouseTo.getItems().addAll(warehouse);

        suppliersHistory.stream()
                .filter(s -> s.getSupplierID() == selectedDrawerItem.getWareHouseSupplierID())
                .findFirst()
                .ifPresent(s -> PartnerTo.getSelectionModel().select(s));

        PartnerTo.getItems().addAll(suppliersHistory);

    }

    public void initData(DrawerItem item, String AID, Runnable cb) {
        this.selectedDrawerItem = item;
        this.codeAID = AID;
        this.callback = cb;
        loadComboBox();
        try {
            String productID = dbCRUDHelper.returnAID(selectedDrawerItem.getWareHouseTable(), "ProductID",
                    "DataWareHouseAID", codeAID.toString());
            String location = dbCRUDHelper.returnAID("vwDataWareHouse", "LocationID",
                    "ProductID", productID);
            txtLocationID.setText(location);
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Lỗi lấy LocationID: " + e.getMessage());
        }

    }

    private void setupAutoFill() {
        txtQtyFrom.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // mất focus
                double qtyFrom = Double.parseDouble(txtQtyFrom.getText().trim());
                double qtyTo = qtyFrom * -1;
                txtQtyTo.setText(String.valueOf(qtyTo));

            }
        });
        PartnerFrom.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null)
                return;
            warehouse.stream()
                    .filter(s -> s.getWareHouseSupplierID() == newVal.getSupplierID())
                    .findFirst()
                    .ifPresent(s -> WareHouseTo.getSelectionModel().select(s));

            System.out.println("Đã chọn: " + newVal.getSupplierID());
        });
        EmployeeTo.valueProperty().bindBidirectional(EmployeeFrom.valueProperty());
        txtTimeTo.valueProperty().bind(txtTimeFrom.valueProperty());

    }

}
