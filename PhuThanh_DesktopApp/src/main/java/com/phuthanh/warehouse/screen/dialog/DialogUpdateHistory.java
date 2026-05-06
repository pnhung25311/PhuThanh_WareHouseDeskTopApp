package com.phuthanh.warehouse.screen.dialog;

import java.sql.Timestamp;
import java.time.LocalDate;
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
import com.phuthanh.model.warehouse.History;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DialogUpdateHistory {

    @FXML
    private ComboBox<Employee> EmployeeID;
    @FXML
    private ComboBox<Supplier> PartnerID;
    @FXML
    private TextField txtQty_History;
    @FXML
    private DatePicker txtTime;
    @FXML
    private TextArea txtRemarkOfHistory;
    @FXML
    private Label txtRequest;
    private String HistoryAID;
    private int Status;

    private String _employeeID;
    private String _partnerID;
    // private String _employeeIDOld;
    // private String _partnerIDOld;
    // private String _dataWareHouseAIDOld;
    LocalDate localDateOld;
    private static final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private static final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private static final FunctionHelper functionHelper = new FunctionHelper();
    private static final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    public void initialize() {
        loadComboBox();
        setSupplier();

        EmployeeID.setOnAction(e -> {
            _employeeID = EmployeeID.getSelectionModel().getSelectedItem() != null
                    ? EmployeeID.getSelectionModel().getSelectedItem().getEmployeeID() + ""
                    : null;
        });

        PartnerID.setOnAction(e -> {
            _partnerID = PartnerID.getSelectionModel().getSelectedItem() != null
                    ? PartnerID.getSelectionModel().getSelectedItem().getSupplierID() + ""
                    : null;
        });
    }

    private void loadComboBox() {
        List<Supplier> suppliersHistory = dbInfoHelper.getAllSuppliers();
        CustomCombobox.setupComboBox(PartnerID, suppliersHistory, Supplier::getSupplierID, Supplier::getName);
        List<Employee> employee = dbInfoHelper.getAllEmployee();
        CustomCombobox.setupComboBox(EmployeeID, employee, Employee::getEmployeeID, Employee::getNameEmployee);
    }

    private void setSupplier() {
        txtQty_History.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // mất focus
                // String qty = txtQty_History.getText().trim();
                // Double qtyPasre = Double.parseDouble(qty);
                // if (qtyPasre > 0) {
                //     List<Supplier> suppliers = dbInfoHelper.getAllSuppliersById2();
                //     CustomCombobox.setupComboBox(PartnerID, suppliers, Supplier::getSupplierID, Supplier::getName);
                // } else if (qtyPasre < 0) {
                //     List<Supplier> suppliers = dbInfoHelper.getAllSuppliersById3();
                //     CustomCombobox.setupComboBox(PartnerID, suppliers, Supplier::getSupplierID, Supplier::getName);
                // } else {
                    // List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
                    // CustomCombobox.setupComboBox(PartnerID, suppliers, Supplier::getSupplierID, Supplier::getName);
                // }

            }
        });
    }

    private void loadItem() {

        History history = dbInfoHelper.geHistoryByAID(HistoryAID);

        if (history != null) {

            txtQty_History.setText(history.getQty() + "");
            txtRemarkOfHistory.setText(history.getRemark());

            if (history.getTime() != null) {
                LocalDate localDate = history.getTime().toLocalDate();
                localDateOld = localDate;
                txtTime.setValue(localDate);
            }

            functionHelper.selectComboBoxItemById(EmployeeID,
                    history.getID_Employee(),
                    Employee::getEmployeeID);

            functionHelper.selectComboBoxItemById(PartnerID,
                    history.getPartner(),
                    Supplier::getSupplierID);

            _employeeID = EmployeeID.getSelectionModel().getSelectedItem() != null
                    ? EmployeeID.getSelectionModel().getSelectedItem().getEmployeeID() + ""
                    : null;

            _partnerID = PartnerID.getSelectionModel().getSelectedItem() != null
                    ? PartnerID.getSelectionModel().getSelectedItem().getSupplierID() + ""
                    : null;
        }
    }

    public void setHistoryAID(String historyAID, int status) {
        System.out.println("Received Product ID: " + historyAID);
        initData(historyAID, status);
    }

    public void initData(String historyAID, int status) {
        this.HistoryAID = historyAID;
        this.Status = status;
        System.out.println("Init Product ID: " + historyAID);
        if (historyAID != null && !historyAID.isEmpty()) {
            loadItem();
        }
        if (status == 0) {
            setReadOnly();
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        try {
            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            System.out.println("Timestamp hiện tại: " + timestamp);
            Account accountFromState = AppState.getInstance().get("Account", Account.class);
            String table = selectedItemFromState.getWareHouseUpdateHistoryDataBase();
            History history = dbInfoHelper.geHistoryByAID(HistoryAID);
            String remark = "";
            if (txtRemarkOfHistory != null && txtRemarkOfHistory.getText() != null) {
                remark = txtRemarkOfHistory.getText().trim();
            }
            List<Object> values = Arrays.asList(history.getHistoryAID(), history.getDataWareHouseAID(),
                    txtQty_History.getText().trim(), _employeeID, _partnerID,
                    remark, history.getTransferGroupID(), txtTime.getValue(), history.getLastUser(),
                    history.getLastTime(),
                    accountFromState.getUserName(), timestamp, null, null, Status, timestamp);
            System.out.println("Values to insert: " + values);
            List<String> columnsRequest = new ArrayList<>(ArrayCRUD.requestHistoryDataWareHouse);
            columnsRequest.remove("RequestAID");

            int row = dbCRUDHelper.insert(table, columnsRequest, values);
            if (row > 0) {
                customDialogNotification.showDialog("Thông tin", "Tạo yêu cầu thành công",
                        Alert.AlertType.INFORMATION);
            } else {
                customDialogNotification.showDialog("Lỗi", "Tạo yêu cầu thất bại", Alert.AlertType.ERROR);
            }
            // }
            if (onCreateSuccess != null) {
                onCreateSuccess.run();
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (

        Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi", "Đã có lỗi xảy ra: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Runnable onCreateSuccess;

    public void setOnCreateSuccess(Runnable callback) {
        this.onCreateSuccess = callback;
    }

    @FXML
    private void onCloseClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void setReadOnly() {
        txtQty_History.setEditable(false);
        txtTime.setDisable(true);
        txtRemarkOfHistory.setEditable(false);
        PartnerID.setDisable(true);
        EmployeeID.setDisable(true);
    }
}
