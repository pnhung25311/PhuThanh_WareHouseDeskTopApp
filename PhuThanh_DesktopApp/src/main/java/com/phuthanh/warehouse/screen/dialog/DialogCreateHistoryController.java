package com.phuthanh.warehouse.screen.dialog;

import java.io.IOException;
// import java.lang.reflect.AccessFlag.Location;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// import com.phuthanh.Main;
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
// import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogCreateHistoryController {

    @FXML
    private TextField txtProductID; // Mã keeton
    @FXML
    private TextField txtIndustrial; // Mã công nghiệp
    @FXML
    private TextField txtPartNo; // Danh điểm
    @FXML
    private TextField txtReplacedPartNo; // Danh điểm tương đương
    @FXML
    private TextField txtVehicleCluster; // Danh điểm chi tiết
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
    // @FXML
    // private ComboBox<Supplier> SupplierID;
    @FXML
    private ComboBox<Supplier> SupplierActualID;
    @FXML
    private TextField txtImage1;
    @FXML
    private TextField txtImage2;
    @FXML
    private TextField txtImage3;
    @FXML
    private ComboBox<Supplier> PartnerID;
    @FXML
    private ComboBox<Manufacturer> ManufacturerID;

    // ==== DÒNG VỊ TRÍ, SL, MÃ HÓA ĐƠN ====
    @FXML
    private TextField txtLocation;
    // @FXML
    // private Button btnLocation;
    @FXML
    private TextField txtQty_Expected;
    @FXML
    private TextField txt_QtyWareHouse;
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
    private TextArea txtRemarkOfHistory;
    @FXML
    private TextArea txtRemark;

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
    private boolean isCreate = false;
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final CustomCombobox customCombobox = new CustomCombobox();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private final ArrayCRUD arrayCRUD = new ArrayCRUD();

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
        // setReadOnly();

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
                    functionHelper.selectComboBoxItemById(PartnerID, product.getSupplierID(), Supplier::getSupplierID);
                    functionHelper.selectComboBoxItemById(SupplierActualID, product.getSupplierActualID(),
                            Supplier::getSupplierID);
                    functionHelper.selectComboBoxItemById(UnitID, product.getUnitID(), Unit::getUnitID);
                    txtTime.setValue(LocalDate.now());
                    txtImage1.setText(product.getImg1() != null ? product.getImg1() : "");
                    txtImage2.setText(product.getImg2() != null ? product.getImg2() : "");
                    txtImage3.setText(product.getImg3() != null ? product.getImg3() : "");
                    txtRemark.setText(product.getRemark());

                } else {
                    customDialogNotification.showDialog("Cảnh báo", "Không tìm thấy sản phẩm với Mã: " + id,
                            Alert.AlertType.WARNING);
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
        // setReadOnly();
    }

    public void initData(String wareHouseAID, boolean iscreate, boolean isaddHistory, boolean isupdate) {
        this.WareHouseAID = wareHouseAID;
        this.isCreate = iscreate;
        this.isAddHistory = isaddHistory;
        this.isUpdate = isupdate;
        System.out.println("Init Product ID: " + wareHouseAID);
        if (wareHouseAID != null && !wareHouseAID.isEmpty()) {
            loadItem();
        }
    }

    private void loadComboBox() {
        List<Country> countries = dbInfoHelper.getAllCountries();
        customCombobox.setupComboBox(CountryID, countries, Country::getCountryID, Country::getName);
        List<Unit> units = dbInfoHelper.getAllUnits();
        customCombobox.setupComboBox(UnitID, units, Unit::getUnitID, Unit::getName);
        List<Manufacturer> manufactures = dbInfoHelper.getAllManufacturer();
        customCombobox.setupComboBox(ManufacturerID, manufactures, Manufacturer::getManufacturerID,
                Manufacturer::getName);
        List<Vehicle> vehicles = dbInfoHelper.getAllVehicels();
        customCombobox.setupComboBox(VehicleTypeID, vehicles, Vehicle::getVehicleID, Vehicle::getVehicleTypeName);
        // List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
        // customCombobox.setupComboBox(SupplierID, suppliers, Supplier::getSupplierID, Supplier::getName);
        List<Supplier> supplierActuals = dbInfoHelper.getAllSuppliers();
        customCombobox.setupComboBox(SupplierActualID, supplierActuals, Supplier::getSupplierID, Supplier::getName);
        List<Supplier> suppliersHistory = dbInfoHelper.getAllSuppliers();
        customCombobox.setupComboBox(PartnerID, suppliersHistory, Supplier::getSupplierID, Supplier::getName);
        List<Employee> employee = dbInfoHelper.getAllEmployee();
        customCombobox.setupComboBox(EmployeeID, employee, Employee::getEmployeeID, Employee::getNameEmployee);
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
            // locationID = wh.getLocationID();
            txtLocation.setText(wh.getLocationID());
            txt_QtyWareHouse.setText(wh.getQty() + "");
            txtRemarkOfWareHouse.setText(wh.getRemark());
            // txtLocation.setText(functionHelper.convertLocation(wh.getLocationID(),
            // dbInfoHelper.getAllLocation()));
            txtRemark.setText(product.getRemark());
            txtImage1.setText(product.getImg1());
            txtImage2.setText(product.getImg2());
            txtImage3.setText(product.getImg3());

            // Chọn giá trị trong ComboBox
            functionHelper.selectComboBoxItemById(ManufacturerID, product.getManufacturerID(),
                    Manufacturer::getManufacturerID);
            // functionHelper.selectComboBoxItemById(VehicleTypeID,
            // product.getVehicleTypeID(), Vehicle::getVehicleID);
            functionHelper.selectComboBoxItemById(CountryID, product.getCountryID(), Country::getCountryID);
            // functionHelper.selectComboBoxItemById(SupplierID, product.getSupplierID(), Supplier::getSupplierID);
            functionHelper.selectComboBoxItemById(SupplierActualID, product.getSupplierActualID(),
                    Supplier::getSupplierID);
            functionHelper.selectComboBoxItemById(UnitID, product.getUnitID(), Unit::getUnitID);
            functionHelper.selectComboBoxItemById(PartnerID, product.getSupplierActualID(), Supplier::getSupplierID);
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
            // stage.initModality(Modality.WINDOW_MODAL);
            // stage.initOwner(Main.getPrimaryStage());
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
        // Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        // stage.close();
        try {
            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            Account accountFromState = AppState.getInstance().get("Account", Account.class);
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            String table = selectedItemFromState.getWareHouseDataBase();
            String tableHistory = selectedItemFromState.getWareHouseDataBaseHistory();
            System.out.println(table);
            // String hisAID = functionHelper.generateCodeAID("LS");
            double Qty_Expected = txtQty_Expected.getText().isEmpty() ? 0
                    : Double.parseDouble(txtQty_Expected.getText());
            double Qty_History = txtQty_History.getText().isEmpty() ? 0
                    : Double.parseDouble(txtQty_History.getText());
            List<String> columnsWarehouse = new ArrayList<>(arrayCRUD.warehouseColumns);
            columnsWarehouse.remove("DataWareHouseAID");
            List<String> columnsHistory = new ArrayList<>(arrayCRUD.historyColumns);
            columnsHistory.remove("HistoryAID");
            columnsHistory.remove("TransferGroupID");
            int _employeeID = functionHelper.getComboBoxItemById(EmployeeID, Employee::getEmployeeID,
                    Employee::getNameEmployee);
            int _partnerID = functionHelper.getComboBoxItemById(PartnerID, Supplier::getSupplierID, Supplier::getName);

            List<Object> valuesAddHistory = Arrays.asList(
                    WareHouseAID, Qty_History,
                    _employeeID, _partnerID,
                    functionHelper.safeTrim(txtRemarkOfHistory),
                    txtTime.getValue(), accountFromState.getUserName(), timestamp);

            List<Object> valuesUpdate = Arrays.asList(Qty_Expected,
                    functionHelper.safeTrim(txtID_Bill),
                    functionHelper.safeTrim(txtLocation),
                    timestamp, accountFromState.getUserName(), functionHelper.safeTrim(txtRemarkOfWareHouse));
            int rowHistory = 0;

            if (isAddHistory) {
                if (!txtQty_History.getText().isBlank()) {
                    String tableWh = selectedItemFromState.getWareHouseDataBase();
                    rowHistory = dbCRUDHelper.insert(tableHistory, columnsHistory, valuesAddHistory);
                    if (rowHistory > 0) {
                        // System.out.println(DbCRUDHelper.sumQtyHistory(tableHistory, whAID));
                        double totalQty = dbCRUDHelper.sumQtyHistory(tableHistory, Integer.parseInt(WareHouseAID));

                        dbCRUDHelper.update(tableWh, Arrays.asList("Qty", "LastTime"),
                                Arrays.asList(totalQty, timestamp), "DataWareHouseAID = ?",
                                Arrays.asList(WareHouseAID));
                        // System.out.println(updateRow);
                        customDialogNotification.showDialog("Thành công", "Tạo mới lịch sử nhập xuất thành công",
                                Alert.AlertType.INFORMATION);
                    }
                }
                if (onCreateSuccess != null) {
                    onCreateSuccess.run();
                }
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            }

            if (isUpdate) {
                int row = dbCRUDHelper.update(table, Arrays.asList("Qty_Expected", "ID_Bill", "LocationID",
                        "LastTime", "LastUser", "Remark"), valuesUpdate, "DataWareHouseAID = ?",
                        Arrays.asList(WareHouseAID));
                if (row > 0) {
                    if (onCreateSuccess != null) {
                        onCreateSuccess.run();
                    }
                    customDialogNotification.showDialog("Thành công", "Cập nhật kho hàng thành công",
                            Alert.AlertType.INFORMATION);
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close();
                } else {
                    customDialogNotification.showDialog("Lỗi", "Cập nhật kho hàng thất bại",
                            Alert.AlertType.ERROR);
                }
            }
            if (isCreate) {
                // String whAID = functionHelper.generateCodeAID("WH");
                List<Object> values = Arrays.asList(
                        productAID, 0, Qty_Expected,
                        functionHelper.safeTrim(txtID_Bill),
                        functionHelper.safeTrim(txtLocation),
                        timestamp, accountFromState.getUserName(), functionHelper.safeTrim(txtRemarkOfWareHouse));

                if (!dbCRUDHelper.isWareHouseExists(productAID, table)) {
                    String tableWh = selectedItemFromState.getWareHouseDataBase();
                    int row = dbCRUDHelper.insert(tableWh, columnsWarehouse, values);
                    if (row > 0) {
                        customDialogNotification.showDialog("Thành công", "Tạo mới kho hàng thành công",
                                Alert.AlertType.INFORMATION);
                    }

                    if (!txtQty_History.getText().isBlank()) {
                        String codeWhAID = dbCRUDHelper.returnAID(selectedItemFromState.getWareHouseDataBase(),
                                "DataWareHouseAID",
                                "ProductAID", productAID);
                        List<Object> valuesHistory = Arrays.asList(
                                codeWhAID, Qty_History,
                                functionHelper.getComboBoxItemById(EmployeeID, Employee::getEmployeeID,
                                        Employee::getNameEmployee),
                                functionHelper.getComboBoxItemById(PartnerID, Supplier::getSupplierID,
                                        Supplier::getName),
                                functionHelper.safeTrim(txtRemarkOfHistory),
                                txtTime.getValue(), accountFromState.getUserName(), timestamp);
                        rowHistory = dbCRUDHelper.insert(tableHistory, columnsHistory, valuesHistory);
                        if (rowHistory > 0) {
                            // System.out.println(DbCRUDHelper.sumQtyHistory(tableHistory, whAID));
                            double totalQty = dbCRUDHelper.sumQtyHistory(tableHistory, Integer.parseInt(codeWhAID));
                            dbCRUDHelper.update(tableWh, Arrays.asList("Qty"),
                                    Arrays.asList(totalQty), "DataWareHouseAID = ?", Arrays.asList(codeWhAID));

                            customDialogNotification.showDialog("Thành công", "Tạo mới lịch sử nhập xuất thành công",
                                    Alert.AlertType.INFORMATION);
                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            stage.close();
                        }
                    }
                    if (onCreateSuccess != null) {
                        onCreateSuccess.run();
                    }
                    closeAndClearAllData(null);
                } else {
                    customDialogNotification.showDialog("Lỗi", "Sản phẩm đã tồn tại trong kho hàng",
                            Alert.AlertType.ERROR);
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

    // private void setReadOnly() {
    // VehicleTypeID.setDisable(true);
    // ManufacturerID.setDisable(true);
    // CountryID.setDisable(true);
    // SupplierID.setDisable(true);
    // SupplierActualID.setDisable(true);
    // UnitID.setDisable(true);
    // // txtRemark.setEditable()
    // System.out.println(isAddHistory);
    // // txtProductID.setEditable(false);
    // if (isAddHistory) {
    // // btnLocation.setDisable(true);
    // txtQty_Expected.setEditable(!isAddHistory);
    // txtID_Bill.setEditable(!isAddHistory);
    // // txtProductID.setEditable(!isAddHistory);
    // }
    // if (isUpdate) {
    // // txtProductID.setEditable(!isAddHistory);
    // txtQty_History.setEditable(!isUpdate);
    // EmployeeID.setDisable(true);
    // PartnerID.setDisable(true);
    // txtTime.setDisable(true);
    // txtRemarkOfHistory.setEditable(false);
    // }
    // }

    private void textFieldNumberOnly() {
        functionHelper.allowOnlyNumber(txtQty_Expected);
        functionHelper.allowOnlyNumber(txtQty_History);
    }


    /**
     * Hàm tự động clear sạch toàn bộ dữ liệu trên form và đóng Dialog
     */
    private void closeAndClearAllData(ActionEvent event) {
        // ---- 1. XÓA DỮ LIỆU CÁC TEXTFIELD ----
        TextField[] fields = {
            txtProductID, txtIndustrial, txtPartNo, txtReplacedPartNo, 
            txtVehicleCluster, txtKeeton, txtNameProduct, txtParameter, 
            txtVehicleDetail, txtImage1, txtImage2, txtImage3, 
            txtLocation, txtQty_Expected, txt_QtyWareHouse, txtID_Bill, txtQty_History
        };
        for (TextField field : fields) {
            if (field != null) field.clear();
        }

        // ---- 2. XÓA DỮ LIỆU CÁC TEXTAREA ----
        if (txtRemarkOfWareHouse != null) txtRemarkOfWareHouse.clear();
        if (txtRemarkOfHistory != null) txtRemarkOfHistory.clear();
        if (txtRemark != null) txtRemark.clear();

        // ---- 3. RESET CÁC COMBOBOX VỀ TRẠNG THÁI TRỐNG ----
        ComboBox<?>[] comboBoxes = {
            VehicleTypeID, EmployeeID, CountryID, UnitID, 
            SupplierActualID, PartnerID, ManufacturerID
        };
        for (ComboBox<?> combo : comboBoxes) {
            if (combo != null) {
                combo.getSelectionModel().clearSelection();
            }
        }

        // ---- 4. RESET DATEPICKER ----
        if (txtTime != null) {
            txtTime.setValue(null); 
            // Hoặc nếu muốn mặc định là ngày hôm nay thì dùng: txtTime.setValue(LocalDate.now());
        }

        // ---- 5. RESET CÁC BIẾN TRẠNG THÁI TRONG CONTROLLER ----
        this.productAID = null;
        this.locationID = null;
        this.WareHouseAID = null;
        this.isUpdate = false;
        this.isAddHistory = false;
        this.isCreate = false;

        // ---- 6. ĐÓNG DIALOG (STAGE) ----
        if (event != null && event.getSource() != null) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

}
