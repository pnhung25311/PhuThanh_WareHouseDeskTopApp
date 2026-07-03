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
import java.util.stream.Collectors;

// import com.phuthanh.Main;
import com.phuthanh.custom.CustomCombobox;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
// import com.phuthanh.helper.DbHelperCheckProductID;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.info.Country;
import com.phuthanh.model.info.Manufacturer;
import com.phuthanh.model.info.Segment;
import com.phuthanh.model.info.Supplier;
import com.phuthanh.model.info.Unit;
import com.phuthanh.model.info.Vehicle;
import com.phuthanh.model.warehouse.Product;
// import com.phuthanh.model.Vehicle;
import com.phuthanh.network.ApiClient;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;
import com.phuthanh.warehouse.screen.dialog.DialogSelectVehicelController.VehicleTypeItem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    // TextFields
    @FXML
    private TextField txtProductID, txtHScode;
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
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    // private final DbHelperCheckProductID dbHelperCheckProductID = new
    // DbHelperCheckProductID();
    private Product model;
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private ObservableList<VehicleTypeItem> masterList = FXCollections.observableArrayList();
    private final ArrayCRUD arrayCRUD = new ArrayCRUD();
    private final CustomCombobox customCombobox = new CustomCombobox();

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
            txtProductID.setText(productIDMain);
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
            txtHScode.setText(product.getHSCode());
            txtImage1.setText(product.getImg1() != null ? product.getImg1() : "");
            txtImage2.setText(product.getImg2() != null ? product.getImg2() : "");
            txtImage3.setText(product.getImg3() != null ? product.getImg3() : "");
            System.out.println("=================>" + product.getCountryID());
            vehicelID = product.getVehicleTypeID();
            loadItem(vehicelID);
            // Chọn giá trị trong ComboBox
            functionHelper.selectComboBoxItemById(ManufacturerID, product.getManufacturerID(),
                    Manufacturer::getManufacturerID);
            // functionHelper.selectComboBoxItemById(VehicleTypeID,
            // product.getVehicleTypeID(), Vehicle::getVehicleID);
            functionHelper.selectComboBoxItemById(CountryID, product.getCountryID(), Country::getCountryID);
            // functionHelper.selectComboBoxItemById(SupplierID, product.getSupplierID(),
            // Supplier::getSupplierID);
            functionHelper.selectComboBoxItemById(SupplierActualID, product.getSupplierActualID(),
                    Supplier::getSupplierID);
            functionHelper.selectComboBoxItemById(UnitID, product.getUnitID(), Unit::getUnitID);
            functionHelper.selectComboBoxItemById(SegmentID, product.getSegmentID(), Segment::getSegmentID);
            img1Url = product.getImg1();
            img2Url = product.getImg2();
            img3Url = product.getImg3();

        }
    }

    private void loadVehicleTypes() {
        try {
            // Lấy toàn bộ vehicle từ DB
            List<Vehicle> vehicles = dbInfoHelper.getAllVehicels();
            // nếu hàm của bạn tên getAllVehicles() thì đổi lại cho đúng

            masterList.clear();

            for (Vehicle v : vehicles) {
                masterList.add(new VehicleTypeItem(v));
            }

            System.out.println("Loaded vehicles: " + masterList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadItem(String vh) {
        if (vh == null || vh.isEmpty()) {
            txtVehicelID.clear();
            return;
        }

        // "1,5,7" -> List<Integer>
        List<Integer> idList = Arrays.stream(vh.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // Tick checkbox trong masterList
        for (VehicleTypeItem item : masterList) {
            int id = item.getVehicel().getVehicleID();
            item.setSelected(idList.contains(id));
        }

        // Lấy danh sách item đã chọn
        String selectedNames = masterList.stream()
                .filter(VehicleTypeItem::isSelected)
                .map(item -> item.getVehicel().getVehicleTypeName())
                .collect(Collectors.joining(", \n"));

        System.out.println("Vehicle IDs từ DB = " + vh);
        System.out.println("Vehicle names = " + selectedNames);

        txtVehicelID.setText(selectedNames);
    }

    private void loadComboBox() {

        List<Country> countries = dbInfoHelper.getAllCountries();
        customCombobox.setupComboBox(CountryID, countries, Country::getCountryID, Country::getName);
        List<Unit> units = dbInfoHelper.getAllUnits();
        customCombobox.setupComboBox(UnitID, units, Unit::getUnitID, Unit::getName);
        List<Manufacturer> manufactures = dbInfoHelper.getAllManufacturer();
        customCombobox.setupComboBox(ManufacturerID, manufactures, Manufacturer::getManufacturerID,
                Manufacturer::getName);
        // List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
        // customCombobox.setupComboBox(SupplierID, suppliers, Supplier::getSupplierID,
        // Supplier::getName);
        List<Supplier> supplierActuals = dbInfoHelper.getAllSuppliers();
        customCombobox.setupComboBox(SupplierActualID, supplierActuals, Supplier::getSupplierID, Supplier::getName);
        List<Segment> segments = dbInfoHelper.getAllSegments();
        customCombobox.setupComboBox(SegmentID, segments, Segment::getSegmentID, Segment::getName);
        loadVehicleTypes();
        functionHelper.selectComboBoxItemById(SegmentID, 2, Segment::getSegmentID);

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
            System.out.println(data.supplierId);
            System.out.println(data.supplierActualId);

            boolean exists = productAID != null;

            List<String> columnsInsert = new ArrayList<>(arrayCRUD.productColumns);
            List<String> columnsUpdate = new ArrayList<>(arrayCRUD.productColumns);

            columnsInsert.removeAll(List.of("ProductAID"));
            columnsInsert.removeAll(List.of("UpdateUser"));
            columnsUpdate.removeAll(List.of("ProductAID"));
            columnsUpdate.removeAll(List.of("CreateTime"));
            columnsUpdate.removeAll(List.of("CreateUser"));

            String condition = data.partNo.isEmpty() ? data.parameter : data.partNo;
            if (condition.isEmpty()) {
                customDialogNotification.showDialog("Cảnh báo", "Bạn phải nhập Mã danh điểm hoặc Thông số kỹ thuật!",
                        Alert.AlertType.WARNING);
                return;
            }

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
                if (checkCombobox((int) data.supplierId, (int) data.supplierActualId,
                        (int) data.manufacturerId,
                        (int) data.countryId, (int) data.unitId) == false) {
                    customDialogNotification.showDialog("Cảnh báo",
                            "Phải chọn đủ các tiêu chí: Nhà cung cấp thực tế, Nhà sản xuất, Quốc gia, Đơn vị tính!",
                            Alert.AlertType.WARNING);
                    return;
                }
                if (checkCriteria(condition, (int) data.supplierId, (int) data.supplierActualId,
                        (int) data.manufacturerId,
                        (int) data.countryId, (int) data.unitId)) {
                    customDialogNotification.showDialog("Cảnh báo",
                            "Sản phẩm có cùng Mã danh điểm hoặc Thông số kỹ thuật đã tồn tại với Nhà cung cấp, Nhà sản xuất, Quốc gia, Đơn vị tính này!",
                            Alert.AlertType.WARNING);
                    boolean isUp = customDialogNotification.showDialogConfirm("Cảnh báo", "Bạn có muốn đẩy lên ko",
                            "Bạn có muốn đẩy lên ko", "", "");

                    if (!isUp) {
                        return;
                    }
                }

                List<List<Object>> batch = List.of(buildInsertRow(data));

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

        try {
            boolean exists = dbCRUDHelper.isProductIdExists(productId);
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
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogSelectVehicel.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Chọn loại xe ");
            stage.setScene(new Scene(root));

            // 🔥 SỬA LỖI QUAN TRỌNG: Gắn gốc owner là chính cửa sổ Thêm/Sửa sản phẩm hiện
            // tại
            // Lấy Stage hiện tại từ chính nút bấm hoặc textfield đang hiển thị
            Stage currentStage = (Stage) btnVehicelID.getScene().getWindow();
            stage.initOwner(currentStage);

            // Đặt kiểu khóa WINDOW_MODAL (chỉ khóa duy nhất cửa sổ sản phẩm này, không khóa
            // màn hình chính)
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            DialogSelectVehicelController controller = loader.getController();
            controller.setDialogStage(stage);
            controller.initData(vehicelID);

            // Hiển thị và đợi người dùng chọn xong
            stage.showAndWait();

            // Lấy kết quả sau khi đóng an toàn
            String ids = controller.getSelectedIds();
            String names = controller.getSelectedNames();
            System.out.println("Selected IDs: " + ids);
            System.out.println("Selected Names: " + names);
            txtVehicelID.setText(names);
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
        Object unitId, segmentId, hsCode, createUser, updateUser;
        String img1Url, img2Url, img3Url;
        Timestamp timestamp, createTime;
    }

    private ProductFormData getFormData(ApiClient apiClient) throws Exception {

        ProductFormData d = new ProductFormData();
        Account acc = AppState.getInstance().get("Account", Account.class);
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
        d.hsCode = safeTrim(txtHScode);

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
        d.supplierId = 0;
        // d.supplierId = functionHelper.getComboBoxItemById(SupplierID,
        // Supplier::getSupplierID, Supplier::getName);
        d.unitId = functionHelper.getComboBoxItemById(UnitID, Unit::getUnitID, Unit::getName);
        d.segmentId = functionHelper.getComboBoxItemById(SegmentID, Segment::getSegmentID, Segment::getName);

        d.timestamp = Timestamp.valueOf(LocalDateTime.now());
        d.createTime = Timestamp.valueOf(LocalDateTime.now());
        d.createUser = acc.getAccountID();
        d.updateUser = acc.getAccountID();

        return d;
    }

    private List<Object> buildInsertRow(ProductFormData d) {
        return Arrays.asList(
                d.productIdMain, d.productId,
                d.keeton, d.industrial, d.partNo, d.replacedPartNo,
                d.nameProduct, d.parameter,
                vehicelID, d.vehicleDetail, d.vehicleCluster,
                d.manufacturerId, d.countryId, d.supplierId, d.supplierActualId,
                d.unitId, d.segmentId, d.hsCode,
                d.img1Url, d.img2Url, d.img3Url,
                d.remark, d.timestamp, d.createTime, d.createUser);
    }

    private List<Object> buildUpdateRow(ProductFormData d) {
        return Arrays.asList(
                d.productIdMain, d.productId, d.keeton,
                d.industrial, d.partNo, d.replacedPartNo,
                d.nameProduct, d.parameter,
                vehicelID, d.vehicleDetail, d.vehicleCluster,
                d.manufacturerId, d.countryId, d.supplierId, d.supplierActualId,
                d.unitId, d.segmentId, d.hsCode,
                d.img1Url, d.img2Url, d.img3Url,
                d.remark, d.timestamp, d.updateUser);
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

    private boolean checkCriteria(String input, int supplierId, int supplierActualId, int manufacturerId,
            int countryId, int unitId) throws SQLException {
        return dbInfoHelper.checkCriteriaProduct(input, supplierId, supplierActualId, manufacturerId, countryId,
                unitId);
    }

    private boolean checkCombobox(Integer supplierId, Integer supplierActualId, Integer manufacturerId,
            Integer countryId, Integer unitId) {

        if (supplierActualId == null || manufacturerId == null || countryId == null
                || unitId == null) {
            return false;
        }

        return true;
    }

}
