package com.phuthanh.warehouse.screen.dialog;

import java.io.IOException;
// import java.lang.reflect.AccessFlag.Location;
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
import com.phuthanh.model.info.Country;
import com.phuthanh.model.info.Employee;
import com.phuthanh.model.info.Manufacturer;
import com.phuthanh.model.info.Supplier;
import com.phuthanh.model.info.Unit;
import com.phuthanh.model.info.Vehicle;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.model.warehouse.Product;
import com.phuthanh.model.warehouse.WareHouse;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DialogRequestHistoryController {

    @FXML
    private TextField txtProductID; // Mã keeton
    @FXML
    private TextField txtIndustrial; // Mã công nghiệp
    @FXML
    private TextField txtPartNo; // Danh điểm
    @FXML
    private TextField txtReplacedPartNo; // Danh điểm tương đương
    @FXML
    private TextField txtVehicleCluster; // Danh điểm
    @FXML
    private TextField txtKeeton; // Mã sản phẩm
    @FXML
    private TextField txtNameProduct; // Tên sản phẩm
    @FXML
    private TextField txtParameter; // Thông số
    @FXML
    private TextField txtVehicleDetail; // Hãng xe
    @FXML
    private ComboBox<Vehicle> VehicleTypeID; // Loại xe
    @FXML
    private ComboBox<Employee> EmployeeID;

    @FXML
    private ComboBox<Country> CountryID;
    @FXML
    private ComboBox<Unit> UnitID;
    @FXML
    private ComboBox<Supplier> SupplierID;
    @FXML
    private ComboBox<Supplier> SupplierActualID;
    @FXML
    private ComboBox<Supplier> PartnerID;
    @FXML
    private ComboBox<Manufacturer> ManufacturerID;

    // ==== DÒNG VỊ TRÍ, SL, MÃ HÓA ĐƠN ====
    @FXML
    private TextField txtLocation;
    @FXML
    private Button btnLocation;
    @FXML
    private TextField txtQty_Expected;
    @FXML
    private TextField txtID_Bill;
    @FXML
    private TextField txtQty_History;
    @FXML
    DatePicker txtTime = new DatePicker();

    // ==== GHI CHÚ ====
    @FXML
    private TextArea txtRemarkOfWareHouse;
    @FXML
    private TextArea txtRemarkOfRequest;

    // ==== BUTTON ====
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    private String productAID;
    private String locationID;
    private String WareHouseAID; // biến lưu giá trị truyền vào
    private boolean isUpdate = false;
    private boolean isAddHistory = false;
    // private boolean isCreate = false;
    private String lastUserWh;
    private static final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private static final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private static final FunctionHelper functionHelper = new FunctionHelper();
    private static final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    @FXML
    public void initialize() {
        loadComboBox();
        textFieldNumberOnly();
        System.out.println("Product ID in initialize: " + WareHouseAID);
        if (WareHouseAID != null && !WareHouseAID.isEmpty()) {
            loadItem();
        } else {
            setupAutoFill();
        }
        setSupplier();
        setReadOnly();

    }

    private void setupAutoFill() {
        txtProductID.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // mất focus
                String id = txtProductID.getText().trim();
                Product product = dbInfoHelper.getProductByID(id);

                if (product != null) {
                    productAID = product.getProductAID();
                    txtProductID.setText(product.getProductID());
                    txtIndustrial.setText(product.getID_Industrial());
                    txtPartNo.setText(product.getID_PartNo());
                    txtKeeton.setText(product.getID_Keeton());
                    txtNameProduct.setText(product.getNameProduct());
                    txtParameter.setText(product.getParameter());
                    txtVehicleDetail.setText(product.getVehicleDetail());
                    txtReplacedPartNo.setText(product.getID_ReplacedPartNo());
                    txtVehicleCluster.setText(product.getVehicleCluster());
                    // txtRemark.setText(product.getRemark());

                    // Chọn giá trị trong ComboBox
                    functionHelper.selectComboBoxItemById(ManufacturerID, product.getManufacturerID(),
                            Manufacturer::getManufacturerID);
                    // functionHelper.selectComboBoxItemById(VehicleTypeID,
                    // product.getVehicleTypeID(),
                    // Vehicle::getVehicleID);
                    functionHelper.selectComboBoxItemById(CountryID, product.getCountryID(), Country::getCountryID);
                    functionHelper.selectComboBoxItemById(SupplierID, product.getSupplierID(), Supplier::getSupplierID);
                    functionHelper.selectComboBoxItemById(SupplierActualID, product.getSupplierActualID(),
                            Supplier::getSupplierID);
                    functionHelper.selectComboBoxItemById(UnitID, product.getUnitID(), Unit::getUnitID);
                    txtTime.setValue(LocalDate.now());

                } else {
                    customDialogNotification.showDialog("Cảnh báo", "Không tìm thấy sản phẩm với Mã: " + id,
                            Alert.AlertType.WARNING);
                }
            }
        });
    }

    private void setSupplier() {
        txtQty_History.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // mất focus
                String qty = txtQty_History.getText().trim();
                Double qtyPasre = Double.parseDouble(qty);
                if (qtyPasre > 0) {
                    List<Supplier> suppliers = dbInfoHelper.getAllSuppliersById2();
                    CustomCombobox.setupComboBox(PartnerID, suppliers, Supplier::getSupplierID, Supplier::getName);
                } else if (qtyPasre < 0) {
                    List<Supplier> suppliers = dbInfoHelper.getAllSuppliersById3();
                    CustomCombobox.setupComboBox(PartnerID, suppliers, Supplier::getSupplierID, Supplier::getName);
                } else {
                    List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
                    CustomCombobox.setupComboBox(PartnerID, suppliers, Supplier::getSupplierID, Supplier::getName);
                }

            }
        });
    }

    public void setProductAID(String WareHouseAID, boolean iscreate, boolean isaddHistory, boolean isupdate) {
        System.out.println("Received Product ID: " + WareHouseAID);
        System.out.println("Received Product ID: " + iscreate);
        System.out.println("Received Product ID: " + isaddHistory);
        System.out.println("Received Product ID: " + isupdate);
        initData(WareHouseAID, iscreate, isaddHistory, isupdate);
        setReadOnly();
    }

    public void initData(String wareHouseAID, boolean iscreate, boolean isaddHistory, boolean isupdate) {
        this.WareHouseAID = wareHouseAID;
        // this.isCreate = iscreate;
        this.isAddHistory = isaddHistory;
        this.isUpdate = isupdate;
        System.out.println("Init Product ID: " + wareHouseAID);
        if (wareHouseAID != null && !wareHouseAID.isEmpty()) {
            loadItem();
        }
    }

    private void loadComboBox() {
        List<Country> countries = dbInfoHelper.getAllCountries();
        CustomCombobox.setupComboBox(CountryID, countries, Country::getCountryID, Country::getName);
        List<Unit> units = dbInfoHelper.getAllUnits();
        CustomCombobox.setupComboBox(UnitID, units, Unit::getUnitID, Unit::getName);
        List<Manufacturer> manufactures = dbInfoHelper.getAllManufacturer();
        CustomCombobox.setupComboBox(ManufacturerID, manufactures, Manufacturer::getManufacturerID,
                Manufacturer::getName);
        List<Vehicle> vehicles = dbInfoHelper.getAllVehicels();
        CustomCombobox.setupComboBox(VehicleTypeID, vehicles, Vehicle::getVehicleID, Vehicle::getVehicleTypeName);
        List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
        CustomCombobox.setupComboBox(SupplierID, suppliers, Supplier::getSupplierID, Supplier::getName);
        List<Supplier> supplierActuals = dbInfoHelper.getAllSuppliers();
        CustomCombobox.setupComboBox(SupplierActualID, supplierActuals, Supplier::getSupplierID, Supplier::getName);
        List<Supplier> suppliersHistory = dbInfoHelper.getAllSuppliers();
        CustomCombobox.setupComboBox(PartnerID, suppliersHistory, Supplier::getSupplierID, Supplier::getName);
        List<Employee> employee = dbInfoHelper.getAllEmployee();
        CustomCombobox.setupComboBox(EmployeeID, employee, Employee::getEmployeeID, Employee::getNameEmployee);
    }

    private void loadItem() {
        WareHouse wh = dbInfoHelper.getWareHouseByAID(WareHouseAID);
        Product product = dbInfoHelper.getProductByAID(wh.getProductAID());
        System.out.println(product);
        if (product != null) {
            txtProductID.setText(product.getProductID());
            txtIndustrial.setText(product.getID_Industrial());
            txtPartNo.setText(product.getID_PartNo());
            txtKeeton.setText(product.getID_Keeton());
            txtNameProduct.setText(product.getNameProduct());
            txtParameter.setText(product.getParameter());
            txtVehicleDetail.setText(product.getVehicleDetail());
            txtReplacedPartNo.setText(product.getID_ReplacedPartNo());
            txtVehicleCluster.setText(product.getVehicleCluster());
            txtID_Bill.setText(wh.getID_Bill());
            txtQty_Expected.setText(String.valueOf(wh.getQty_Expected()));
            locationID = wh.getLocationID();
            txtLocation.setText(functionHelper.convertLocation(wh.getLocationID(), dbInfoHelper.getAllLocation()));
            // txtRemark.setText(product.getRemark());
            lastUserWh = wh.getLastUser();

            // Chọn giá trị trong ComboBox
            functionHelper.selectComboBoxItemById(ManufacturerID, product.getManufacturerID(),
                    Manufacturer::getManufacturerID);
            // functionHelper.selectComboBoxItemById(VehicleTypeID,
            // product.getVehicleTypeID(), Vehicle::getVehicleID);
            functionHelper.selectComboBoxItemById(CountryID, product.getCountryID(), Country::getCountryID);
            functionHelper.selectComboBoxItemById(SupplierID, product.getSupplierID(), Supplier::getSupplierID);
            functionHelper.selectComboBoxItemById(SupplierActualID, product.getSupplierActualID(),
                    Supplier::getSupplierID);
            functionHelper.selectComboBoxItemById(UnitID, product.getUnitID(), Unit::getUnitID);

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

    @FXML
    private void onLocation(ActionEvent event) {
        try {
            // FXMLLoader loader = new
            // FXMLLoader(getClass().getResource("/fxml/dialogSelectLocation.fxml"));
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogSelectLocation.fxml"));

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Select Location");
            stage.setScene(new Scene(root));

            DialogSelectLocationController controller = loader.getController();
            controller.setDialogStage(stage); // ⚠️ quan trọng: phải set stage trước
            controller.initData(locationID);

            stage.showAndWait(); // Hiển thị dialog modal

            // Lấy kết quả
            String ids = controller.getSelectedIds();
            String names = controller.getSelectedNames();
            System.out.println("Selected IDs: " + ids);
            System.out.println("Selected Names: " + names);
            txtLocation.setText(names); // Hoặc ids tùy theo yêu cầu
            locationID = ids;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        try {
            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            Account accountFromState = AppState.getInstance().get("Account", Account.class);
            String table = selectedItemFromState.getWareHouseRequestDataBase();
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            // String rqAID = functionHelper.generateCodeAID("RQ");
            // String tableHistory = selectedItemFromState.getWareHouseDataBaseHistory();
            System.out.println(table);
            List<Object> values = Arrays.asList(
                    WareHouseAID, "", productAID, 0, Double.parseDouble(txtQty_Expected.getText().trim()),
                    txtID_Bill.getText().trim(), locationID,
                    timestamp, lastUserWh, txtRemarkOfWareHouse.getText().trim(),
                    accountFromState.getUserName(), timestamp, null, null, txtRemarkOfRequest.getText().trim(),
                    timestamp);
            List<String> columnsrequestWareHouseColumns = new ArrayList<>(ArrayCRUD.requestWareHouseColumns);
            columnsrequestWareHouseColumns.remove("RequestAID"); // Loại bỏ cột RequestAID để sử dụng giá trị mặc định
                                                                 // tự động tăng

            int rowRequest = dbCRUDHelper.returnCount(table, "DataWareHouseAID", WareHouseAID);
            if (rowRequest == 0) {
                // int rowHistory = DbCRUDHelper.returnCount(tableHistory, "DataWareHouseAID",
                // WareHouseAID);
                // if (rowHistory == 0) {
                int row = dbCRUDHelper.insert(table, columnsrequestWareHouseColumns, values);
                if (row > 0) {
                    customDialogNotification.showDialog("Thành công", "Tạo yêu cầu thành công",
                            Alert.AlertType.INFORMATION);
                    if (onCreateSuccess != null) {
                        onCreateSuccess.run();
                    }

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close();
                }
                // } else {
                // customDialogNotification.showDialog("Thất bại", "Đã phát sinh lịch sử nhập
                // xuất!",
                // Alert.AlertType.WARNING);
                // }
            } else {
                customDialogNotification.showDialog("Thất bại", "Đã phát sinh yêu cầu!",
                        Alert.AlertType.WARNING);
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi", "Đã có lỗi xảy ra: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setReadOnly() {
        VehicleTypeID.setDisable(true);
        ManufacturerID.setDisable(true);
        CountryID.setDisable(true);
        SupplierID.setDisable(true);
        SupplierActualID.setDisable(true);
        UnitID.setDisable(true);
        System.out.println(isAddHistory);
        if (isAddHistory) {
            btnLocation.setDisable(true);
            txtQty_Expected.setEditable(!isAddHistory);
            txtID_Bill.setEditable(!isAddHistory);
            txtProductID.setEditable(!isAddHistory);
        }
        if (isUpdate) {
            txtProductID.setEditable(!isAddHistory);
            txtQty_History.setEditable(!isUpdate);
            EmployeeID.setDisable(true);
            PartnerID.setDisable(true);
            txtTime.setDisable(true);
        }
    }

    private void textFieldNumberOnly() {
        functionHelper.allowOnlyNumber(txtQty_Expected);
        functionHelper.allowOnlyNumber(txtQty_History);
    }

}
