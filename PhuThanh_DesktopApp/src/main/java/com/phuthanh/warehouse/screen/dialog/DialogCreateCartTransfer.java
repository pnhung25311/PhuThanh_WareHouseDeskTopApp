package com.phuthanh.warehouse.screen.dialog;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.phuthanh.custom.CustomCombobox;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.helper.function.SendNotificationByTelegram;
import com.phuthanh.model.business.ProductBusiness;
import com.phuthanh.model.info.Account;
// import com.phuthanh.model.info.Bill;
// import com.phuthanh.model.info.Bill;
import com.phuthanh.model.info.Business;
import com.phuthanh.model.info.Country;
import com.phuthanh.model.info.Employee;
import com.phuthanh.model.info.Manufacturer;
// import com.phuthanh.model.info.Payment;
// import com.phuthanh.model.info.Payment;
import com.phuthanh.model.info.Supplier;
// import com.phuthanh.model.info.TypeCart;
import com.phuthanh.model.info.Unit;
import com.phuthanh.model.info.Vehicle;
// import com.phuthanh.model.warehouse.CCBdata;
import com.phuthanh.model.warehouse.Product;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;
import com.phuthanh.warehouse.screen.dialog.DialogSelectVehicelController.VehicleTypeItem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DialogCreateCartTransfer {

    // ================= LABEL =================
    @FXML
    private Label lblRemarkOfRequest, lblDeliveryTime;

    // ================= TEXTFIELD =================
    @FXML
    private TextField txtProductID, txtPartNo, txtNameProduct, txtQty, txtPriceCost, txtVehicelID, txtGrossPriceVAT,
            txtParameter;

    @FXML
    private Button btnVehicelID;
    // ================= TEXTAREA =================
    @FXML
    private TextArea txtRemark, txtRemarkOfRequest;

    // ================= DATE =================
    @FXML
    private DatePicker txtDeliveryTime = new DatePicker();
    @FXML
    private DatePicker txtReportDate = new DatePicker();

    // ================= COMBOBOX =================
    @FXML
    private ComboBox<Supplier> SourceID;
    @FXML
    private ComboBox<Supplier> DeliveryID;
    @FXML
    private ComboBox<Manufacturer> ManufacturerID;
    @FXML
    private ComboBox<Unit> UnitID;
    @FXML
    private ComboBox<Country> CountryID;
    @FXML
    private ComboBox<Employee> EmployeeID;
    @FXML
    private ComboBox<Business> BusinessID;
    @FXML
    private ComboBox<Vehicle> VehicleTypeID;

    // ================= BUTTON =================
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private Runnable callBack;
    private String productID;
    private String vehicelID;

    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private final SendNotificationByTelegram sendNotificationByTelegram = new SendNotificationByTelegram();
    private final ArrayCRUD arrayCRUD = new ArrayCRUD();
    private final CustomCombobox customCombobox = new CustomCombobox();

    // =====================================================
    // INIT
    // =====================================================
    @FXML
    private void initialize() {
        System.out.println("Initializing DialogCreateCart...");
        loadVehicleTypes();
        loadComboBox();

        // auto load product
        txtProductID.textProperty().addListener((obs, o, productID) -> {
            loadDataProduct(productID);
        });
        textFieldNumberOnly();
        textFieldListener();
        functionHelper.setupMoneyField(txtPriceCost);
        functionHelper.setupMoneyField(txtGrossPriceVAT);

    }

    private void loadComboBox() {
        List<Supplier> suppliers = dbInfoHelper.getAllSuppliersById4();
        List<Employee> employees = dbInfoHelper.getAllEmployee();
        List<Manufacturer> manufacturers = dbInfoHelper.getAllManufacturer();
        List<Unit> units = dbInfoHelper.getAllUnits();
        List<Country> countries = dbInfoHelper.getAllCountries();
        // List<TypeCart> typeCarts = dbInfoHelper.getAllTypeCarts();
        List<Business> businesses = dbInfoHelper.getAllBusiness();

        customCombobox.setupComboBox(SourceID, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(DeliveryID, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(EmployeeID, employees, Employee::getEmployeeID, Employee::getNameEmployee);
        customCombobox.setupComboBox(ManufacturerID, manufacturers, Manufacturer::getManufacturerID,
                Manufacturer::getName);
        customCombobox.setupComboBox(UnitID, units, Unit::getUnitID, Unit::getName);
        customCombobox.setupComboBox(CountryID, countries, Country::getCountryID, Country::getName);
        customCombobox.setupComboBox(BusinessID, businesses, Business::getBusinessID, Business::getName);

        Account account = AppState.getInstance().get("Account", Account.class);
        functionHelper.selectComboBoxItemById(EmployeeID, account.getEmployeeID(), Employee::getEmployeeID);

        // StatusVAT.getItems().addAll(
        // new CCBdata(1, "Đã xuất VAT"),
        // new CCBdata(0, "Chưa xuất VAT"));

    }

    public void setInitialData(Runnable cb, String iseditMode, String codeAID, String prodID,
            ProductBusiness proBusiness) {
        this.callBack = cb;
        this.productID = prodID;

        if (productID != null && !productID.isEmpty()) {
            txtProductID.setText(productID);
            // loadDvcataProduct(productID);
        }
    }

    // =====================================================
    // AUTO LOAD PRODUCT
    // =====================================================
    private void loadDataProduct(String productID) {
        try {
            Product product = dbInfoHelper.getProductByID(productID);

            if (product == null) {
                txtPartNo.clear();
                txtNameProduct.clear();
                return;
            }
            vehicelID = product.getVehicleTypeID();
            loadItem(vehicelID);

            txtPartNo.setText(product.getID_PartNo());
            txtNameProduct.setText(product.getNameProduct());
            txtParameter.setText(product.getParameter());

            // 🔥 FK có thể = 0 -> nghĩa là NULL trong DB
            if (product.getManufacturerID() > 0)
                functionHelper.selectComboBoxItemById(
                        ManufacturerID, product.getManufacturerID(), Manufacturer::getManufacturerID);

            if (product.getCountryID() > 0)
                functionHelper.selectComboBoxItemById(
                        CountryID, product.getCountryID(), Country::getCountryID);

            if (product.getUnitID() > 0)
                functionHelper.selectComboBoxItemById(
                        UnitID, product.getUnitID(), Unit::getUnitID);

            autoSelectBusinessID(productID);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ObservableList<VehicleTypeItem> masterList = FXCollections.observableArrayList();

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

    private double safeParseDouble(String text) {
        if (text == null || text.trim().isEmpty())
            return 0;
        try {
            return Double.parseDouble(text.replace(",", ""));
        } catch (Exception e) {
            return -1; // dùng -1 để detect invalid number
        }
    }

    private void textFieldListener() {
        txtDeliveryTime.setValue(LocalDate.now());
        // txtReportDate.setValue(LocalDate.now());
    }

    @FXML
    private void onSave() {
        try {

            // ================= LẤY TEXT =================
            String productID = safeTrim(txtProductID);
            String partNo = safeTrim(txtPartNo);
            String nameProduct = safeTrim(txtNameProduct);
            String qty = safeTrim(txtQty);
            String priceCostText = safeTrim(txtPriceCost);
            String grossPriceVAT = safeTrim(txtGrossPriceVAT);
            String parameter = safeTrim(txtParameter);

            // String contractID = safeTrim(txtContractID);

            // ================= VALIDATE TEXT =================
            if (productID.isEmpty()) {
                customDialogNotification.showDialog("Lỗi", "Chưa nhập mã sản phẩm!", Alert.AlertType.ERROR);
                return;
            }

            if (nameProduct.isEmpty()) {
                customDialogNotification.showDialog("Lỗi", "Chưa nhập tên sản phẩm!", Alert.AlertType.ERROR);
                return;
            }

            if (txtDeliveryTime.getValue() == null) {
                customDialogNotification.showDialog("Lỗi", "Chưa chọn ngày giao!", Alert.AlertType.ERROR);
                return;
            }

            double priceNumber = safeParseDouble(priceCostText);
            double grossPriceVATNumber = safeParseDouble(grossPriceVAT);

            // ================= LẤY ACCOUNT =================
            Account account = AppState.getInstance().get("Account", Account.class);
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            String productAID = dbCRUDHelper.returnAID("Product", "ProductAID", "ProductID", productID);

            int sourceID = functionHelper.getComboBoxItemById(SourceID, Supplier::getSupplierID, Supplier::getName);
            int deliveryID = functionHelper.getComboBoxItemById(DeliveryID, Supplier::getSupplierID, Supplier::getName);

            int employeeID = functionHelper.getComboBoxItemById(EmployeeID,
                    Employee::getEmployeeID,
                    Employee::getNameEmployee);
            int manufacturerID = functionHelper.getComboBoxItemById(ManufacturerID,
                    Manufacturer::getManufacturerID,
                    Manufacturer::getName);
            int countryID = functionHelper.getComboBoxItemById(CountryID,
                    Country::getCountryID,
                    Country::getName);
            int unitID = functionHelper.getComboBoxItemById(UnitID,
                    Unit::getUnitID,
                    Unit::getName);
            // int statusVAT = getStatusVAT(StatusVAT);
            int businessID = functionHelper.getComboBoxItemById(BusinessID,
                    Business::getBusinessID,
                    Business::getName);

            // ================= VALIDATE COMBOBOX =================
            boolean isValid = validateRequiredFields(
                    productID,
                    nameProduct,
                    qty,
                    sourceID,
                    deliveryID,
                    unitID,
                    txtDeliveryTime.getValue());

            if (!isValid)
                return;

            // ================= PREPARE COLUMN =================
            List<String> cartColumns = new ArrayList<>(arrayCRUD.cartColumns);
            cartColumns.remove("CartAID");
            cartColumns.remove("CartID");
            cartColumns.remove("StatusVAT");
            cartColumns.remove("ContractID");
            // cartColumns.remove("Qty");
            cartColumns.remove("PriceNET");
            cartColumns.remove("Total");
            cartColumns.remove("Cogs");
            cartColumns.remove("PriceVAT");
            cartColumns.remove("PaymentID");
            cartColumns.remove("BillID");
            cartColumns.remove("ProductAIDVAT");
            // cartColumns.remove("PriceCost");
            cartColumns.remove("InvoiceNumber");

            List<Object> values = Arrays.asList(
                    account.getAccountID(), productAID, partNo, nameProduct,
                    manufacturerID, countryID, unitID, vehicelID, parameter, businessID, qty, grossPriceVATNumber, sourceID,
                    deliveryID, employeeID, false, txtDeliveryTime.getValue(), txtReportDate.getValue(),
                    priceNumber, safeTrim(txtRemark), 3, now);

            dbCRUDHelper.insert("Cart", cartColumns, values);
            customDialogNotification.showDialog("Thành công", "Tạo phiếu thành công", Alert.AlertType.INFORMATION);
            String telegram = sendNotificationByTelegram
                    .sendTelegramNotification("Bạn có 1 đơn hàng mới từ " + account.getFullName().toString());
            System.out.println("Telegram response: " + telegram);
            if (callBack != null)
                callBack.run();
            closeWindow();

        } catch (

        Exception e) {
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi", "Lưu thất bại: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onCloseClick() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private String safeTrim(TextInputControl txt) {
        return txt == null || txt.getText() == null ? "" : txt.getText().trim();
    }

    private void autoSelectBusinessID(String maVatTu) {
        if (maVatTu == null || maVatTu.isEmpty())
            return;

        // Lấy ký tự đầu của mã
        String firstChar = maVatTu.substring(0, 1).toUpperCase();

        // Duyệt ComboBox JavaFX
        for (int i = 0; i < BusinessID.getItems().size(); i++) {
            Business item = BusinessID.getItems().get(i);

            String name = item.getName().trim();
            String firstCharCbb = name.substring(0, 1).toUpperCase();

            if (firstChar.equals(firstCharCbb)) {
                BusinessID.getSelectionModel().select(i);
                break;
            }
        }
    }

    private boolean validateRequiredFields(
            String productID,
            String nameProduct,
            String qty,
            int sourceID,
            int deliveryID,
            int unitID,
            LocalDate deliveryDate) {

        if (productID.isEmpty()) {
            customDialogNotification.showDialog("Lỗi", "Chưa nhập mã sản phẩm!", Alert.AlertType.ERROR);
            return false;
        }

        if (nameProduct.isEmpty()) {
            customDialogNotification.showDialog("Lỗi", "Chưa nhập tên sản phẩm!", Alert.AlertType.ERROR);
            return false;
        }

        if (qty.isEmpty()) {
            customDialogNotification.showDialog("Lỗi", "Chưa nhập số lượng!", Alert.AlertType.ERROR);
            return false;
        }

        if (sourceID == 0) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn nguồn hàng!", Alert.AlertType.ERROR);
            return false;
        }

        if (deliveryID == 0) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn nơi nhận!", Alert.AlertType.ERROR);
            return false;
        }

        if (unitID == 0) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn đơn vị tính!", Alert.AlertType.ERROR);
            return false;
        }

        if (deliveryDate == null) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn ngày giao!", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void textFieldNumberOnly() {
        functionHelper.allowOnlyNumber(txtQty);
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

}