package com.phuthanh.warehouse.screen.dialog;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.phuthanh.custom.CustomCombobox;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.helper.function.SendNotificationByTelegram;
import com.phuthanh.model.business.ProductBusiness;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.info.Bill;
import com.phuthanh.model.info.Business;
import com.phuthanh.model.info.Contract;
import com.phuthanh.model.info.Country;
import com.phuthanh.model.info.Employee;
import com.phuthanh.model.info.Manufacturer;
import com.phuthanh.model.info.Payment;
import com.phuthanh.model.info.Supplier;
import com.phuthanh.model.info.Unit;
import com.phuthanh.model.info.Vehicle;
import com.phuthanh.model.warehouse.CCBdata;
// import com.phuthanh.model.warehouse.Cart;
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

public class DialogCreateCartExport {

    // ================= LABEL =================
    @FXML
    private Label lblRemarkOfRequest, lblDeliveryTime;

    // ================= TEXTFIELD =================
    @FXML
    private TextField txtProductID, txtPartNo, txtNameProduct, txtQty, txtProductIDVAT, txtNameProductVAT,
            txtTotal, txtPriceVAT, txtPriceNET, txtCogs, txtContractID, txtVehicelID, txtInvoiceNumber, txtParameter;

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
    private ComboBox<Bill> BillID;
    @FXML
    private ComboBox<Manufacturer> ManufacturerID;
    @FXML
    private ComboBox<Unit> UnitID;
    @FXML
    private ComboBox<Country> CountryID;
    @FXML
    private ComboBox<Payment> PaymentID;
    @FXML
    private ComboBox<Employee> EmployeeID;
    @FXML
    private ComboBox<Contract> ContractID;
    @FXML
    private ComboBox<CCBdata> StatusVAT;
    @FXML
    private ComboBox<Business> BusinessID;

    // ================= BUTTON =================
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private Runnable callBack;
    private String isEditMode;
    // private String CodeAID;
    private String productID;
    private ProductBusiness productBusiness;
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

        loadComboBox();
        loadVehicleTypes();

        // auto load product
        txtProductID.textProperty().addListener((obs, o, productID) -> {
            loadDataProduct(productID);
        });
        functionHelper.setupMoneyField(txtCogs);
        functionHelper.setupMoneyField(txtPriceNET);
        functionHelper.setupMoneyField(txtPriceVAT);

        // auto tính tiền
        textFieldListener();

    }

    private void loadComboBox() {
        List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
        List<Bill> bills = dbInfoHelper.getAllBills();
        List<Payment> payments = dbInfoHelper.getAllPayments();
        List<Employee> employees = dbInfoHelper.getAllEmployee();
        List<Manufacturer> manufacturers = dbInfoHelper.getAllManufacturer();
        List<Unit> units = dbInfoHelper.getAllUnits();
        List<Country> countries = dbInfoHelper.getAllCountries();
        List<Business> businesses = dbInfoHelper.getAllBusiness();

        customCombobox.setupComboBox(SourceID, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(DeliveryID, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(BillID, bills, Bill::getBillID, Bill::getName);
        customCombobox.setupComboBox(PaymentID, payments, Payment::getPaymentID, Payment::getName);
        customCombobox.setupComboBox(EmployeeID, employees, Employee::getEmployeeID, Employee::getNameEmployee);
        customCombobox.setupComboBox(ManufacturerID, manufacturers, Manufacturer::getManufacturerID,
                Manufacturer::getName);
        customCombobox.setupComboBox(UnitID, units, Unit::getUnitID, Unit::getName);
        customCombobox.setupComboBox(CountryID, countries, Country::getCountryID, Country::getName);
        customCombobox.setupComboBox(BusinessID, businesses, Business::getBusinessID, Business::getName);

        Account account = AppState.getInstance().get("Account", Account.class);
        functionHelper.selectComboBoxItemById(EmployeeID, account.getEmployeeID(), Employee::getEmployeeID);
        functionHelper.selectComboBoxItemById(BillID, 1, Bill::getBillID);
        functionHelper.selectComboBoxItemById(PaymentID, 3, Payment::getPaymentID);

        StatusVAT.getItems().addAll(
                new CCBdata(1, "Đã xuất VAT"),
                new CCBdata(0, "Chưa xuất VAT"));

    }

    private void loadComboboxIsEditMode() {
        List<Supplier> suppliers4 = dbInfoHelper.getAllSuppliersById4();
        // List<Supplier> suppliers3 = dbInfoHelper.getAllSuppliersById3();
        // List<Supplier> suppliers2 = dbInfoHelper.getAllSuppliersById2();
        // List<Supplier> suppliers1 = dbInfoHelper.getAllSuppliersById1();
        List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();

        // SourceID.getItems().clear();
        // DeliveryID.getItems().clear();

        if (isEditMode.equals("CREATEEX")) {
            customCombobox.setupComboBox(SourceID, suppliers4, Supplier::getSupplierID, Supplier::getName);
            customCombobox.setupComboBox(DeliveryID, suppliers, Supplier::getSupplierID, Supplier::getName);

        }
        if (isEditMode.equals("CREATEIM")) {
            customCombobox.setupComboBox(DeliveryID, suppliers4, Supplier::getSupplierID, Supplier::getName);
            customCombobox.setupComboBox(SourceID, suppliers, Supplier::getSupplierID, Supplier::getName);
        }
    }

    public void setInitialData(Runnable cb, String iseditMode, String codeAID, String prodID,
            ProductBusiness proBusiness) {
        this.callBack = cb;
        this.isEditMode = iseditMode;
        // this.CodeAID = codeAID;
        this.productID = prodID;
        this.productBusiness = proBusiness;

        loadComboboxIsEditMode();
        textFieldNumberOnly();
        if (productID != null && !productID.isEmpty()) {
            txtProductID.setText(productID);
            txtProductIDVAT.setText(productID);
            // loadDvcataProduct(productID);
        }
        if (isEditMode != null) {
            showhide();
        }
        if (productBusiness != null) {
            DecimalFormat df = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.US));

            double cogsValue = 0;

            if (productBusiness.giaVon1 != null) {
                try {
                    String raw = productBusiness.giaVon1.toString().trim();

                    if (!raw.isEmpty()) {
                        // bỏ dấu phẩy nếu dữ liệu đã có format 1,200
                        raw = raw.replace(",", "");

                        cogsValue = Double.parseDouble(raw);
                    }
                } catch (Exception ex) {
                    System.out.println("Lỗi parse COGS: " + productBusiness.giaVon1);
                }
            }

            txtCogs.setText(df.format(cogsValue));
        }

    }

    // =====================================================
    // AUTO LOAD PRODUCT
    // =====================================================
    private void loadDataProduct(String productID) {
        Product product = dbInfoHelper.getProductByID(productID);
        if (product != null) {
            txtPartNo.setText(product.getID_PartNo());
            txtNameProduct.setText(product.getNameProduct());
            vehicelID = product.getVehicleTypeID();
            txtParameter.setText(product.getParameter());
            loadItem(vehicelID);
            functionHelper.selectComboBoxItemById(ManufacturerID, product.getManufacturerID(),
                    Manufacturer::getManufacturerID);
            functionHelper.selectComboBoxItemById(CountryID, product.getCountryID(), Country::getCountryID);
            functionHelper.selectComboBoxItemById(UnitID, product.getUnitID(), Unit::getUnitID);

            if (isEditMode == "CREATEIM") {
                functionHelper.selectComboBoxItemById(SourceID, product.getSupplierActualID(), Supplier::getSupplierID);
                functionHelper.selectComboBoxItemById(DeliveryID, 41, Supplier::getSupplierID);
            }
            if (isEditMode == "CREATEEX") {
                functionHelper.selectComboBoxItemById(SourceID, 41, Supplier::getSupplierID);
            }

            autoSelectBusinessID(productID);

        } else {
            txtPartNo.clear();
            txtNameProduct.clear();
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

    // =====================================================
    // AUTO CALCULATE TOTAL
    // =====================================================
    private void calculateTotal() {
        try {
            double qty = safeParseDouble(txtQty.getText());
            double price = safeParseDouble(txtPriceNET.getText());
            double total = Math.abs(qty) * price;

            DecimalFormat df = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.US));
            txtTotal.setText(df.format(total));
            // System.out.println(price);
            txtPriceVAT.setText(String.valueOf(df.format(price)));

        } catch (Exception e) {
            txtTotal.setText("0");
        }
    }

    private void textFieldListener() {
        txtDeliveryTime.setValue(LocalDate.now());
        // txtReportDate.setValue(LocalDate.now());
        txtQty.textProperty().addListener((obs, o, n) -> calculateTotal());
        txtPriceNET.textProperty().addListener((obs, o, n) -> calculateTotal());
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

    @FXML
    private void onSave() {
        try {

            // ================= LẤY TEXT =================
            String productID = safeTrim(txtProductID);
            String productIDVAT = safeTrim(txtProductIDVAT);
            String partNo = safeTrim(txtPartNo);
            String nameProduct = safeTrim(txtNameProduct);
            String parameter = safeTrim(txtParameter);
            String qtyText = safeTrim(txtQty);
            String priceText = safeTrim(txtPriceNET);
            String cogsText = safeTrim(txtCogs);
            String priceVATText = safeTrim(txtPriceVAT);
            String contractID = safeTrim(txtContractID);
            String invoiceNumber = safeTrim(txtInvoiceNumber);

            // ================= VALIDATE TEXT =================
            if (productID.isEmpty()) {
                customDialogNotification.showDialog("Lỗi", "Chưa nhập mã sản phẩm!", Alert.AlertType.ERROR);
                return;
            }

            if (nameProduct.isEmpty()) {
                customDialogNotification.showDialog("Lỗi", "Chưa nhập tên sản phẩm!", Alert.AlertType.ERROR);
                return;
            }

            if (!validateNumberField(qtyText, "Số lượng"))
                return;
            if (!priceText.isEmpty() && !validateNumberField(priceText, "Giá NET"))
                return;
            if (!cogsText.isEmpty() && !validateNumberField(cogsText, "COGS"))
                return;
            if (!priceVATText.isEmpty() && !validateNumberField(priceVATText, "Giá VAT"))
                return;

            if (txtDeliveryTime.getValue() == null) {
                customDialogNotification.showDialog("Lỗi", "Chưa chọn ngày giao!", Alert.AlertType.ERROR);
                return;
            }

            // ================= PARSE NUMBER =================
            double qtyNumber = safeParseDouble(qtyText);
            // qtyNumber = qtyNumber * -1;
            double priceNumber = safeParseDouble(priceText);
            double cogsValue = safeParseDouble(cogsText);
            double priceVATNumber = safeParseDouble(priceVATText);
            double total = Math.abs(qtyNumber) * priceNumber;

            // Xuất kho -> đảo dấu số lượng
            // if ("CREATEEX".equals(isEditMode)) {
            // qtyNumber = -qtyNumber;
            // }

            String qty = String.valueOf(qtyNumber);
            String price = String.valueOf(priceNumber);
            String priceVAT = String.valueOf(priceVATNumber);

            // ================= LẤY ACCOUNT =================
            Account account = AppState.getInstance().get("Account", Account.class);
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            String productAID = dbCRUDHelper.returnAID("Product", "ProductAID", "ProductID", productID);
            String productAIDVAT = dbCRUDHelper.returnAID("Product", "ProductAID", "ProductID", productIDVAT);

            int sourceID = functionHelper.getComboBoxItemById(SourceID, Supplier::getSupplierID, Supplier::getName);
            int deliveryID = functionHelper.getComboBoxItemById(DeliveryID, Supplier::getSupplierID, Supplier::getName);
            int billID = functionHelper.getComboBoxItemById(BillID, Bill::getBillID, Bill::getName);
            int paymentID = functionHelper.getComboBoxItemById(PaymentID,
                    Payment::getPaymentID,
                    Payment::getName);
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
            int statusVAT = getStatusVAT(StatusVAT);
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
                    billID,
                    unitID,
                    businessID,
                    txtDeliveryTime.getValue());

            if (!isValid)
                return;

            // ================= PREPARE COLUMN =================
            List<String> cartColumns = new ArrayList<>(arrayCRUD.cartColumns);
            cartColumns.remove("CartAID");
            cartColumns.remove("CartID");
            cartColumns.remove("PriceCost");
            cartColumns.remove("GrossPriceVAT");

            List<Object> values = Arrays.asList(
                    account.getAccountID(), productAID, productAIDVAT, partNo, nameProduct,
                    manufacturerID, countryID, unitID, vehicelID, parameter, businessID, qty, price, total, cogsValue,
                    priceVAT, paymentID, billID, sourceID, deliveryID, employeeID,
                    false, txtDeliveryTime.getValue(), txtReportDate.getValue(), statusVAT, contractID, invoiceNumber,
                    safeTrim(txtRemark), 2, now);

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

    private int getStatusVAT(ComboBox<CCBdata> cb) {
        Object value = cb.getValue();
        if (value == null)
            return 0;
        if (value instanceof CCBdata)
            return ((CCBdata) value).getId();
        return 0;
    }

    // private int getContractID(ComboBox<Contract> cb) {
    // return cb.getValue() == null ? 0 : cb.getValue().getContractID();
    // }

    // private double parseMoney(String text) {
    // if (text == null || text.isEmpty())
    // return 0;
    // return Double.parseDouble(text.replace(",", ""));
    // }

    // =====================================================
    // CLOSE
    // =====================================================
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

    private void textFieldNumberOnly() {
        functionHelper.allowOnlyNumber(txtQty);
    }

    private void showhide() {

        if (isEditMode.equals("CREATEEX")) {
            lblDeliveryTime.setText("Ngày xuất kho:");
        }

        if (isEditMode.equals("CREATEIM")) {
            lblDeliveryTime.setText("Ngày nhập kho:");
        }

        boolean isRequestMode = "UPDATE".equals(isEditMode) ||
                "DELETE".equals(isEditMode);

        txtRemarkOfRequest.setManaged(isRequestMode);
        txtRemarkOfRequest.setVisible(isRequestMode);
        lblRemarkOfRequest.setManaged(isRequestMode);
        lblRemarkOfRequest.setVisible(isRequestMode);

        if ("DELETE".equals(isEditMode)) {
            txtProductID.setEditable(false);
            txtQty.setEditable(false);
            txtPriceVAT.setEditable(false);
            txtPriceNET.setEditable(false);
            txtRemark.setEditable(false);
            SourceID.setDisable(true);
            DeliveryID.setDisable(true);
            BillID.setDisable(true);
            PaymentID.setDisable(true);
            EmployeeID.setDisable(true);
            txtDeliveryTime.setDisable(true);
        }
    }

    private boolean validateRequiredFields(
            String productID,
            String nameProduct,
            String qty,
            int sourceID,
            int deliveryID,
            int billID,
            int unitID,
            int businessID,
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
            customDialogNotification.showDialog("Lỗi", "Chưa chọn nơi lấy hàng!", Alert.AlertType.ERROR);
            return false;
        }

        if (deliveryID == 0) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn nơi nhận hàng!", Alert.AlertType.ERROR);
            return false;
        }

        if (billID == 0) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn loại hóa đơn!", Alert.AlertType.ERROR);
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
        if (businessID == 0) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn đơn vị! ",
                    Alert.AlertType.ERROR);
            return false;
        }

        return true;
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

    private boolean validateNumberField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            customDialogNotification.showDialog("Lỗi", fieldName + " đang trống!", Alert.AlertType.ERROR);
            return false;
        }

        try {
            Double.parseDouble(value.replace(",", ""));
            return true;
        } catch (Exception e) {
            customDialogNotification.showDialog("Lỗi", fieldName + " không phải số hợp lệ!", Alert.AlertType.ERROR);
            return false;
        }
    }

}