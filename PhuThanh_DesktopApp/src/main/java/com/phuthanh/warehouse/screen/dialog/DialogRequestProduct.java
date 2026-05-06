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
import com.phuthanh.model.info.Country;
import com.phuthanh.model.info.Manufacturer;
import com.phuthanh.model.info.Supplier;
import com.phuthanh.model.info.Unit;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.model.warehouse.Product;
// import com.phuthanh.model.Vehicle;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DialogRequestProduct {

    @FXML
    private ComboBox<Country> CountryID;
    @FXML
    private ComboBox<Unit> UnitID;
    @FXML
    private ComboBox<Supplier> SupplierID;
    @FXML
    private ComboBox<Supplier> SupplierActualID;
    @FXML
    private ComboBox<Manufacturer> ManufacturerID;
    // @FXML
    // private ComboBox<Vehicle> VehicleTypeID;
    // TextFields
    @FXML
    private TextField txtProductID;
    @FXML
    private TextField txtIndustrial;
    @FXML
    private TextField txtPartNo;
    @FXML
    private TextField txtKeeton;
    @FXML
    private TextField txtNameProduct;
    @FXML
    private TextField txtParameter;
    @FXML
    private TextField txtVehicleDetail;
    @FXML
    private TextField txtReplacedPartNo;
    @FXML
    private TextField txtVehicleCluster;
    @FXML
    private TextField txtVehicelID;
    @FXML
    private TextArea txtRemark;
    @FXML
    private TextArea txtRemarkOfRequest;

    @FXML
    private TextField txtImage1;
    @FXML
    private TextField txtImage2;
    @FXML
    private TextField txtImage3;
    private String _vehicleTypeID;
    private int _manufacturerID;
    private int _countryID;
    private int _supplierID;
    private int _supplierActualID;
    private int _unitID;
    private static final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private static final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private static final FunctionHelper functionHelper = new FunctionHelper();
    private static final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    @FXML
    public void initialize() {
        loadComboBox();
        setDefault();
        System.out.println("Product ID in initialize: " + productAID);
        // if (productAID != null && !productAID.isEmpty()) {
        // loadItem();
        // }
    }

    private void loadItem() {
        Product product = dbInfoHelper.getProductByAID(productAID);
        System.out.println(product.getUnitID());
        if (product != null) {
            safeSetText(txtProductID, product.getProductID());
            safeSetText(txtIndustrial, product.getID_Industrial());
            safeSetText(txtPartNo, product.getID_PartNo());
            safeSetText(txtKeeton, product.getID_Keeton());
            safeSetText(txtNameProduct, product.getNameProduct());
            safeSetText(txtParameter, product.getParameter());
            safeSetText(txtVehicleDetail, product.getVehicleDetail());
            safeSetText(txtReplacedPartNo, product.getID_ReplacedPartNo());
            safeSetText(txtVehicleCluster, product.getVehicleCluster());
            safeSetText(txtImage1, product.getImg1());
            safeSetText(txtImage2, product.getImg2());
            safeSetText(txtImage3, product.getImg3());
            safeSetText(txtRemark, product.getRemark());

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
            _manufacturerID = product.getManufacturerID();
            _vehicleTypeID = product.getVehicleTypeID();
            _countryID = product.getCountryID();
            _supplierID = product.getSupplierID();
            _supplierActualID = product.getSupplierActualID();
            _unitID = product.getUnitID();
            safeSetText(txtVehicelID, functionHelper.convertVehicle(_vehicleTypeID, dbInfoHelper.getAllVehicels()));

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
        // List<Vehicle> vehicles = dbInfoHelper.getAllVehicels();
        // CustomCombobox.setupComboBox(VehicleTypeID, vehicles, Vehicle::getVehicleID,
        // Vehicle::getVehicleTypeName);
        List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
        CustomCombobox.setupComboBox(SupplierID, suppliers, Supplier::getSupplierID, Supplier::getName);
        List<Supplier> supplierActuals = dbInfoHelper.getAllSuppliers();
        CustomCombobox.setupComboBox(SupplierActualID, supplierActuals, Supplier::getSupplierID, Supplier::getName);
    }

    @FXML
    private void onCloseClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onSave(ActionEvent event) {
        try {

            // 🚨 ĐẶT LOG Ở ĐÂY
            debugInputs();

            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            Account accountFromState = AppState.getInstance().get("Account", Account.class);
            String table = selectedItemFromState.getWareHouseRequestDataBase();
            // String rqAID = functionHelper.generateCodeAID("RQ");
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);

            List<Object> values = Arrays.asList(productAID, txtProductID.getText().trim(),
                    txtKeeton.getText().trim(), txtIndustrial.getText().trim(), txtPartNo.getText().trim(),
                    txtReplacedPartNo.getText().trim(),
                    txtNameProduct.getText().trim(), txtParameter.getText().trim(),
                    // VehicleTypeID.getSelectionModel().getSelectedItem() != null
                    // ? VehicleTypeID.getSelectionModel().getSelectedItem().getVehicleID() + ""
                    // : _vehicleTypeID,
                    _vehicleTypeID,
                    txtVehicleDetail.getText().trim(), txtVehicleCluster.getText().trim(),
                    ManufacturerID.getSelectionModel().getSelectedItem() != null
                            ? ManufacturerID.getSelectionModel().getSelectedItem().getManufacturerID() + ""
                            : _manufacturerID,
                    CountryID.getSelectionModel().getSelectedItem() != null
                            ? CountryID.getSelectionModel().getSelectedItem().getCountryID() + ""
                            : _countryID,
                    SupplierActualID.getSelectionModel().getSelectedItem() != null
                            ? SupplierActualID.getSelectionModel().getSelectedItem().getSupplierID() + ""
                            : _supplierActualID,
                    SupplierID.getSelectionModel().getSelectedItem() != null
                            ? SupplierID.getSelectionModel().getSelectedItem().getSupplierID() + ""
                            : _supplierID,
                    UnitID.getSelectionModel().getSelectedItem() != null
                            ? UnitID.getSelectionModel().getSelectedItem().getUnitID() + ""
                            : _unitID,
                    txtImage1.getText(), txtImage2.getText(), txtImage3.getText(),
                    txtRemark.getText().trim(), null,
                    accountFromState.getUserName(), timestamp, null,
                    null, txtRemarkOfRequest.getText().trim(), timestamp);
            List<String> columnsRequestProduct = new ArrayList<>(ArrayCRUD.requestProductColumns);
            columnsRequestProduct.remove("RequestAID");
            boolean checkProduct = dbCRUDHelper.isWareHouseExists(productAID, table);

            if (checkProduct) {
                customDialogNotification.showDialog("Lỗi", "Yêu cầu xóa đã có",
                        Alert.AlertType.INFORMATION);
            } else {
                int row = dbCRUDHelper.insert(table, columnsRequestProduct, values);

                if (row > 0) {
                    customDialogNotification.showDialog("Thành công", "Tạo yêu cầu thành công",
                            Alert.AlertType.INFORMATION);
                    if (onCreateSuccess != null) {
                        onCreateSuccess.run();
                    }
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi", "Đã có lỗi xảy ra: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Runnable onCreateSuccess;

    public void setOnCreateSuccess(Runnable callback) {
        this.onCreateSuccess = callback;
    }

    private String productAID; // biến lưu giá trị truyền vào

    public void setProductAID(String productAID) {
        System.out.println("Received Product ID: " + productAID);
        initData(productAID);

    }

    // public void initData(String productAID) {
    // this.productAID = productAID;
    // System.out.println("Init Product ID: " + productAID);
    // if (productAID != null && !productAID.isEmpty()) {
    // loadItem();
    // }

    // }
    public void initData(String productAID) {
        this.productAID = productAID;
        loadItem(); // Chạy sau khi initialize() hoàn thành
    }

    private void setDefault() {
        txtIndustrial.setText("");
        txtPartNo.setText("");
        txtKeeton.setText("");
        txtNameProduct.setText("");
        txtParameter.setText("");
        txtVehicleDetail.setText("");
        txtReplacedPartNo.setText("");
        txtVehicleCluster.setText("");
        txtRemark.setText("");
        txtRemarkOfRequest.setText("");
    }

    private void safeSetText(TextField tf, String value) {
        tf.setText(value == null ? "" : value);
    }

    private void safeSetText(TextArea ta, String value) {
        ta.setText(value == null ? "" : value);
    }

    private void debugInputs() {
        System.out.println("===== DEBUG INPUTS =====");
        System.out.println("txtProductID: " + txtProductID + " | text=" + safe(txtProductID));
        System.out.println("txtIndustrial: " + txtIndustrial + " | text=" + safe(txtIndustrial));
        System.out.println("txtPartNo: " + txtPartNo + " | text=" + safe(txtPartNo));
        System.out.println("txtKeeton: " + txtKeeton + " | text=" + safe(txtKeeton));
        System.out.println("txtNameProduct: " + txtNameProduct + " | text=" + safe(txtNameProduct));
        System.out.println("txtParameter: " + txtParameter + " | text=" + safe(txtParameter));
        System.out.println("txtVehicleDetail: " + txtVehicleDetail + " | text=" + safe(txtVehicleDetail));
        System.out.println("txtReplacedPartNo: " + txtReplacedPartNo + " | text=" + safe(txtReplacedPartNo));
        System.out.println("txtVehicleCluster: " + txtVehicleCluster + " | text=" + safe(txtVehicleCluster));
        System.out.println("txtImage1: " + txtImage1 + " | text=" + safe(txtImage1));
        System.out.println("txtImage2: " + txtImage2 + " | text=" + safe(txtImage2));
        System.out.println("txtImage3: " + txtImage3 + " | text=" + safe(txtImage3));
        System.out.println("txtRemark: " + safeArea(txtRemark));
        System.out.println("txtRemarkOfRequest: " + safeArea(txtRemarkOfRequest));
        System.out.println("=========================");
    }

    private String safe(TextField tf) {
        return tf == null ? "TEXTFIELD_IS_NULL" : (tf.getText() == null ? "TEXT_NULL" : tf.getText());
    }

    private String safeArea(TextArea ta) {
        return ta == null ? "TEXTAREA_IS_NULL" : (ta.getText() == null ? "TEXT_NULL" : ta.getText());
    }

    @FXML
    private void onVehicelID(ActionEvent event) {
    }

}
