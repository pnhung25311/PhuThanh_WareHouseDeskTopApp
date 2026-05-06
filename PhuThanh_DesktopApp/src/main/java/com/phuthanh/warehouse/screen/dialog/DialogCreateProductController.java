package com.phuthanh.warehouse.screen.dialog;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// import java.util.Optional;
// import java.util.function.Function;

// import com.phuthanh.Main;
import com.phuthanh.custom.CustomCombobox;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
// import com.phuthanh.helper.DbHelperCheckProductID;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.model.info.Country;
import com.phuthanh.model.info.Manufacturer;
import com.phuthanh.model.info.Purpose;
import com.phuthanh.model.info.Segment;
import com.phuthanh.model.info.Supplier;
import com.phuthanh.model.info.Unit;
import com.phuthanh.model.warehouse.Product;
// import com.phuthanh.model.Vehicle;
import com.phuthanh.network.ApiClient;
import com.phuthanh.utils.ArrayCRUD;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
// import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
// import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogCreateProductController {

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
    @FXML
    private ComboBox<Segment> SegmentID;
    @FXML
    private ComboBox<Purpose> PurposeID;
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
    private TextField txtPurpose;
    @FXML
    private TextArea txtRemark;
    @FXML
    private Label lblNotification;

    @FXML
    private TextField txtImage1;
    @FXML
    private TextField txtImage2;
    @FXML
    private TextField txtImage3;

    @FXML
    private Button btnVehicelID;
    @FXML
    private TextField txtVehicelID;

    private String img1;
    private String img2;
    private String img3;
    // private String _vehicleTypeID;
    // private int _manufacturerID;
    // private int _countryID;
    // private int _supplierID;
    // private int _supplierActualID;
    // private int _unitID;
    private String vehicelID;
    String img1Url = null, img2Url = null, img3Url = null;
    private static final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private static final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private static final FunctionHelper functionHelper = new FunctionHelper();
    // private static final DbHelperCheckProductID dbHelperCheckProductID = new
    // DbHelperCheckProductID();
    private Product model;
    private static final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    @FXML
    private void onBrowseImage1() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh 1");
        File file = fileChooser.showOpenDialog(txtImage1.getScene().getWindow());
        if (file != null) {
            this.img1 = file.getAbsolutePath();
            txtImage1.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void onBrowseImage2() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh 2");
        File file = fileChooser.showOpenDialog(txtImage2.getScene().getWindow());
        if (file != null) {
            this.img2 = file.getAbsolutePath();
            txtImage2.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void onBrowseImage3() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh 3");
        File file = fileChooser.showOpenDialog(txtImage3.getScene().getWindow());
        if (file != null) {
            this.img3 = file.getAbsolutePath();
            txtImage3.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void initialize() {
        loadComboBox();
        // txtProductIDMain.setEditable(false);
        System.out.println("Product ID in initialize: " + productAID);
        if (productAID != null && !productAID.isEmpty()) {
            loadItem();
            lblNotification.setVisible(false);
        } else {
            String productIDMain = dbInfoHelper.GenerateProductIDMainCode();
            String notification = "Mã hiện tại là " + productIDMain;
            lblNotification.setText(notification);
            // txtProductIDMain.setText(productIDMain);
        }
    }

    private void loadItem() {
        Product product = dbInfoHelper.getProductByAID(productAID);
        System.out.println(product);
        if (product != null) {
            model = product;
            // txtProductIDMain.setText(product.getProductIDMain());
            txtProductID.setText(product.getProductID());
            txtIndustrial.setText(product.getID_Industrial());
            txtPartNo.setText(product.getID_PartNo());
            txtKeeton.setText(product.getID_Keeton());
            txtNameProduct.setText(product.getNameProduct());
            txtParameter.setText(product.getParameter());
            txtVehicleDetail.setText(product.getVehicleDetail());
            txtReplacedPartNo.setText(product.getID_ReplacedPartNo());
            txtVehicleCluster.setText(product.getVehicleCluster());
            txtRemark.setText(product.getRemark());
            txtImage1.setText(product.getImg1() != null ? product.getImg1() : "");
            txtImage2.setText(product.getImg2() != null ? product.getImg2() : "");
            txtImage3.setText(product.getImg3() != null ? product.getImg3() : "");
            System.out.println("=================>" + product.getCountryID());
            vehicelID = product.getVehicleTypeID();
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
            functionHelper.selectComboBoxItemById(SegmentID, product.getSegmentID(), Segment::getSegmentID);
            functionHelper.selectComboBoxItemById(PurposeID, product.getPurposeID(), Purpose::getPurposeID);

            // _manufacturerID = product.getManufacturerID();
            // // _vehicleTypeID = product.getVehicleTypeID();
            // _countryID = product.getCountryID();
            // _supplierID = product.getSupplierID();
            // _supplierActualID = product.getSupplierActualID();
            // _unitID = product.getUnitID();
            img1Url = product.getImg1();
            img2Url = product.getImg2();
            img3Url = product.getImg3();

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
        List<Purpose> purposes = dbInfoHelper.getAllPurposes();
        CustomCombobox.setupComboBox(PurposeID, purposes, Purpose::getPurposeID, Purpose::getName);
        List<Segment> segments = dbInfoHelper.getAllSegments();
        CustomCombobox.setupComboBox(SegmentID, segments, Segment::getSegmentID, Segment::getName);
    }

    @FXML
    private void onCloseClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onSave(ActionEvent event) {
        try {
            ApiClient apiClient = new ApiClient();
            ProductFormData data = getFormData(apiClient);

            boolean exists = productAID != null;

            List<String> columnsInsert = new ArrayList<>(ArrayCRUD.productColumns);
            List<String> columnsUpdate = new ArrayList<>(ArrayCRUD.productColumns);

            columnsInsert.removeAll(List.of("ProductAID"));
            columnsUpdate.removeAll(List.of("ProductAID"));

            if (exists) {
                int rows = dbCRUDHelper.update(
                        "Product",
                        columnsUpdate,
                        buildUpdateRow(data),
                        "ProductAID = ?",
                        List.of(model.getProductAID()));
                if (rows > 0)
                    closeWindow(event);

            } else {
                if (!validateInput())
                    return;
                // int PurposeID = Integer.parseInt(data.purposeId.toString());

                List<List<Object>> batch = List.of(
                        // buildInsertRow(data, "D", PurposeID),
                        // buildInsertRow(data, "P", PurposeID),
                        // buildInsertRow(data, "T", PurposeID),
                        buildInsertRow(data));

                int[] rows = dbCRUDHelper.insertBatch("Product", columnsInsert, batch);
                if (rows.length > 0)
                    closeWindow(event);
            }

            if (onCreateSuccess != null)
                onCreateSuccess.run();

        } catch (Exception e) {
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi",
                    "Lưu sản phẩm thất bại: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    public boolean isValidCode(String text) {

        if (text == null)
            return false;

        // Chuẩn hoá dữ liệu từ Excel / TextField
        text = text
                .replace("\u00A0", "")
                .replaceAll("\\s+", "")
                .trim();

        // Chỉ cần đủ 7 ký tự
        return text.length() == 7;
    }

    private boolean validateInput() {
        String productId = txtProductID.getText().trim();

        // Kiểm tra rỗng
        if (productId.isEmpty()) {
            customDialogNotification.showDialog("Cảnh báo", "Bạn chưa nhập mã sản phẩm!", Alert.AlertType.WARNING);
            return false;
        }

        // Kiểm tra định dạng
        // if (!isValidCode(productId)) {
        // customDialogNotification.showDialog("Cảnh báo",
        // "Mã sản phẩm không hợp lệ! Mã sản phẩm phải gồm 1 chữ cái theo sau là 9 chữ
        // số. Ví dụ: A123456789",
        // Alert.AlertType.WARNING);
        // return false;
        // }

        // Kiểm tra tồn tại trong database
        try {
            boolean exists = dbCRUDHelper.isProductIdExists(productId);
            if (exists) {
                customDialogNotification.showDialog("Cảnh báo", "Mã sản phẩm '" + productId + "' đã tồn tại!",
                        Alert.AlertType.WARNING);
                return false;
            }
            // boolean checkid =
            // dbHelperCheckProductID.isExistDMVT(txtProductID.getText().toString());

            // if (checkid) {

            // Alert alert = new Alert(
            // Alert.AlertType.CONFIRMATION,
            // "Mã VT đã tồn tại.\nBạn có muốn tiếp tục không?",
            // ButtonType.YES,
            // ButtonType.NO);

            // alert.setTitle("Thông báo");
            // alert.setHeaderText("Trùng mã vật tư");

            // Optional<ButtonType> result = alert.showAndWait();

            // return result.isPresent() && result.get() == ButtonType.YES;
            // }
            // return;

        } catch (SQLException e) {
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi", "Không thể kiểm tra mã sản phẩm: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            return false;
        }

        // Nếu đến đây => hợp lệ
        System.out.println("Product ID is valid.");
        return true;
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

    public void initData(String productAID) {
        this.productAID = productAID;
        System.out.println("Init Product ID: " + productAID);
        if (productAID != null && !productAID.isEmpty()) {
            loadItem();
        }
    }

    private String safeTrim(TextField tf) {
        return (tf == null || tf.getText() == null) ? "" : tf.getText().trim();
    }

    private String safeTrim(TextArea ta) {
        return (ta == null || ta.getText() == null) ? "" : ta.getText().trim();
    }

    @FXML
    private void onVehicelID(ActionEvent event) {
        try {
            // FXMLLoader loader = new
            // FXMLLoader(getClass().getResource("/fxml/dialogSelectLocation.fxml"));
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogSelectVehicel.fxml"));

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Chọn loại xe ");
            stage.setScene(new Scene(root));

            DialogSelectVehicelController controller = loader.getController();
            controller.setDialogStage(stage); // ⚠️ quan trọng: phải set stage trước
            controller.initData(vehicelID);
            // stage.initModality(Modality.WINDOW_MODAL);
            // stage.initOwner(Main.getPrimaryStage());
            stage.showAndWait(); // Hiển thị dialog modal

            // Lấy kết quả
            String ids = controller.getSelectedIds();
            String names = controller.getSelectedNames();
            System.out.println("Selected IDs: " + ids);
            System.out.println("Selected Names: " + names);
            txtVehicelID.setText(names); // Hoặc ids tùy theo yêu cầu
            vehicelID = ids;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String uploadImage(ApiClient apiClient, String path) throws Exception {
        if (path == null || path.isEmpty())
            return null;
        File file = new File(path);
        return apiClient.postFile("upload/" + txtProductID.getText().trim(), file, null, "file");
    }

    class ProductFormData {
        String productIdMain, productId, keeton, industrial, partNo, replacedPartNo;
        String nameProduct, parameter, vehicleDetail, vehicleCluster, remark;
        Object manufacturerId, countryId, supplierActualId, supplierId;
        Object unitId, segmentId, purposeId;
        String img1Url, img2Url, img3Url;
        Timestamp timestamp;
    }

    private ProductFormData getFormData(ApiClient apiClient) throws Exception {

        ProductFormData d = new ProductFormData();
        d.productId = safeTrim(txtProductID);
        d.productIdMain = removePrefixLetters(safeTrim(txtProductID));
        d.keeton = safeTrim(txtKeeton);
        d.industrial = safeTrim(txtIndustrial);
        d.partNo = safeTrim(txtPartNo);
        d.replacedPartNo = safeTrim(txtReplacedPartNo);
        d.nameProduct = safeTrim(txtNameProduct);
        d.parameter = safeTrim(txtParameter);
        d.vehicleDetail = safeTrim(txtVehicleDetail);
        d.vehicleCluster = safeTrim(txtVehicleCluster);
        d.remark = safeTrim(txtRemark);

        // upload ảnh
        d.img1Url = uploadImage(apiClient, img1);
        d.img2Url = uploadImage(apiClient, img2);
        d.img3Url = uploadImage(apiClient, img3);

        // combo values
        d.manufacturerId = functionHelper.getComboBoxItemById(ManufacturerID, Manufacturer::getManufacturerID,
                Manufacturer::getName);
        d.countryId = functionHelper.getComboBoxItemById(CountryID, Country::getCountryID, Country::getName);
        d.supplierActualId = functionHelper.getComboBoxItemById(SupplierActualID, Supplier::getSupplierID,
                Supplier::getName);
        d.supplierId = functionHelper.getComboBoxItemById(SupplierID, Supplier::getSupplierID, Supplier::getName);
        d.unitId = functionHelper.getComboBoxItemById(UnitID, Unit::getUnitID, Unit::getName);
        d.segmentId = functionHelper.getComboBoxItemById(SegmentID, Segment::getSegmentID, Segment::getName);
        d.purposeId = functionHelper.getComboBoxItemById(PurposeID, Purpose::getPurposeID, Purpose::getName);

        d.timestamp = Timestamp.valueOf(LocalDateTime.now());

        return d;
    }

    private List<Object> buildInsertRow(ProductFormData d) {
        return Arrays.asList(
                d.productIdMain, d.productId,
                d.keeton, d.industrial, d.partNo, d.replacedPartNo,
                d.nameProduct, d.parameter,
                vehicelID, d.vehicleDetail, d.vehicleCluster,
                d.manufacturerId, d.countryId, d.supplierId, d.supplierActualId,
                d.unitId, d.segmentId, d.purposeId,
                d.img1Url, d.img2Url, d.img3Url,
                d.remark, d.timestamp);
    }

    private List<Object> buildUpdateRow(ProductFormData d) {
        return Arrays.asList(
                
                d.productIdMain,d.productId, d.keeton,
                d.industrial, d.partNo, d.replacedPartNo,
                d.nameProduct, d.parameter,
                vehicelID, d.vehicleDetail, d.vehicleCluster,
                d.manufacturerId, d.countryId, d.supplierId, d.supplierActualId,
                d.unitId, d.segmentId, d.purposeId,
                d.img1Url, d.img2Url, d.img3Url,
                d.remark, d.timestamp);
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private String removePrefixLetters(String input) {
        if (input == null)
            return null;
        return input.replaceFirst("^[A-Za-z]+", "");
    }
}
