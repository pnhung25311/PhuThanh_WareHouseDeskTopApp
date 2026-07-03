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
// import com.phuthanh.model.info.TypeCart;
import com.phuthanh.model.info.Unit;
import com.phuthanh.model.info.Vehicle;
import com.phuthanh.model.warehouse.CCBdata;
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

public class DialogCreateCartImportExport {

    // ================= LABEL =================
    @FXML
    private Label lblRemarkOfRequest, lblDeliveryTime, lblStatusVAT, lblContractID;

    // ================= TEXTFIELD =================
    @FXML
    private TextField txtProductID, txtPartNo, txtNameProduct, txtQty, txtProductIDVAT, txtNameProductVAT,
            txtTotal, txtPriceVAT, txtPriceNET, txtCogs, txtContractID, txtVehicelID, txtImportContractID;

    @FXML
    private TextField txtImportQty;
    @FXML
    private TextField txtImportPrice;
    @FXML
    private TextField txtImportTotal;
    @FXML
    private TextField txtImportPriceVAT;
    @FXML
    private TextField txtImportCogs;
    @FXML
    private TextField txtImportProductIDVAT;
    // ================= TEXTAREA =================
    @FXML
    private TextArea txtRemark, txtRemarkOfRequest, txtImportRemark;

    // ================= DATE =================
    @FXML
    private DatePicker txtDeliveryTime = new DatePicker();
    @FXML
    private DatePicker txtReportDate = new DatePicker();
    @FXML
    private DatePicker txtImportDate = new DatePicker();
    @FXML
    private DatePicker txtImportReportDate = new DatePicker();

    // ================= COMBOBOX =================
    @FXML
    private ComboBox<Supplier> SourceID, ImportSourceID;
    @FXML
    private ComboBox<Supplier> DeliveryID, ImportDeliveryID;
    @FXML
    private ComboBox<Bill> BillID, ImportBillID;
    @FXML
    private ComboBox<Manufacturer> ManufacturerID;
    @FXML
    private ComboBox<Unit> UnitID;
    @FXML
    private ComboBox<Country> CountryID;
    @FXML
    private ComboBox<Payment> PaymentID, ImportPaymentID;
    @FXML
    private ComboBox<Employee> EmployeeID, ImportEmployeeID;
    @FXML
    private ComboBox<Business> BusinessID, ImportBusinessID;
    @FXML
    private ComboBox<CCBdata> StatusVAT, ImportStatusVAT;
    @FXML
    private ComboBox<Contract> ContractID;

    // ================= BUTTON =================
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private Runnable callBack;
    private String isEditMode;
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
        initUIOnly();
    }

    private void initUIOnly() {
        loadComboBox();
        loadVehicleTypes();

        functionHelper.setupMoneyField(txtPriceNET);
        functionHelper.setupMoneyField(txtPriceVAT);
        functionHelper.setupMoneyField(txtCogs);
        functionHelper.setupMoneyField(txtImportCogs);
        functionHelper.setupMoneyField(txtImportPriceVAT);
        functionHelper.setupMoneyField(txtImportPrice);

        txtProductID.textProperty().addListener((obs, o, productID) -> {
            loadDataProduct(productID);
        });

        textFieldListener();
        textFieldListenerIm();
    }

    private void loadComboBox() {
        List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
        List<Bill> bills = dbInfoHelper.getAllBills();
        List<Payment> payments = dbInfoHelper.getAllPayments();
        List<Employee> employees = dbInfoHelper.getAllEmployee();
        List<Manufacturer> manufacturers = dbInfoHelper.getAllManufacturer();
        List<Unit> units = dbInfoHelper.getAllUnits();
        List<Country> countries = dbInfoHelper.getAllCountries();
        // List<TypeCart> typeCarts = dbInfoHelper.getAllTypeCarts();
        List<Business> businesses = dbInfoHelper.getAllBusiness();

        customCombobox.setupComboBox(SourceID, suppliers, Supplier::getSupplierID,
                Supplier::getName);
        customCombobox.setupComboBox(ImportSourceID, suppliers, Supplier::getSupplierID,
                Supplier::getName);

        customCombobox.setupComboBox(DeliveryID, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(ImportDeliveryID, suppliers, Supplier::getSupplierID, Supplier::getName);

        customCombobox.setupComboBox(BillID, bills, Bill::getBillID, Bill::getName);
        customCombobox.setupComboBox(ImportBillID, bills, Bill::getBillID, Bill::getName);

        customCombobox.setupComboBox(PaymentID, payments, Payment::getPaymentID, Payment::getName);
        customCombobox.setupComboBox(ImportPaymentID, payments, Payment::getPaymentID, Payment::getName);

        customCombobox.setupComboBox(EmployeeID, employees, Employee::getEmployeeID, Employee::getNameEmployee);
        customCombobox.setupComboBox(ImportEmployeeID, employees, Employee::getEmployeeID, Employee::getNameEmployee);

        customCombobox.setupComboBox(ManufacturerID, manufacturers, Manufacturer::getManufacturerID,
                Manufacturer::getName);
        customCombobox.setupComboBox(UnitID, units, Unit::getUnitID, Unit::getName);
        customCombobox.setupComboBox(CountryID, countries, Country::getCountryID, Country::getName);

        customCombobox.setupComboBox(BusinessID, businesses, Business::getBusinessID, Business::getName);
        customCombobox.setupComboBox(ImportBusinessID, businesses, Business::getBusinessID, Business::getName);

        Account account = AppState.getInstance().get("Account", Account.class);
        functionHelper.selectComboBoxItemById(EmployeeID, account.getEmployeeID(), Employee::getEmployeeID);
        functionHelper.selectComboBoxItemById(ImportEmployeeID, account.getEmployeeID(), Employee::getEmployeeID);

        ImportStatusVAT.getItems().addAll(
                new CCBdata(1, "Đã xuất VAT"),
                new CCBdata(0, "Chưa xuất VAT"));

    }

    public void setInitialData(Runnable cb, String iseditMode, String codeAID,
            ProductBusiness proBusiness) {
        System.out.println("<=========>");

        this.callBack = cb;
        this.isEditMode = iseditMode;
        this.productID = codeAID;
        this.productBusiness = proBusiness;

        // loadComboboxIsEditMode();
        textFieldNumberOnly();
        if (productID != null && !productID.isEmpty()) {
            txtProductID.setText(productID);
            txtProductIDVAT.setText(productID);
            txtImportProductIDVAT.setText(productID);
            // loadDvcataProduct(productID);
        }
        if (isEditMode != null) {
            // showhide();
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
            txtImportCogs.setText(df.format(cogsValue));
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

            txtPartNo.setText(product.getID_PartNo());
            txtNameProduct.setText(product.getNameProduct());

            vehicelID = product.getVehicleTypeID();
            loadItem(vehicelID);
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

            if ("CREATEIM".equals(isEditMode)) {

                // 🔥 CHỈ SELECT KHI ID > 0
                if (product.getSupplierActualID() > 0) {
                    functionHelper.selectComboBoxItemById(
                            SourceID,
                            product.getSupplierActualID(),
                            Supplier::getSupplierID);
                }

                functionHelper.selectComboBoxItemById(DeliveryID, 41, Supplier::getSupplierID);
            }

            if ("CREATEEX".equals(isEditMode)) {
                functionHelper.selectComboBoxItemById(
                        SourceID, 41, Supplier::getSupplierID);
            }

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
            txtPriceVAT.setText(String.valueOf(df.format(price)));

        } catch (Exception e) {
            txtTotal.setText("0");
        }
    }

    private void calculateTotalIm() {
        try {
            double qty = safeParseDouble(txtImportQty.getText());
            double price = safeParseDouble(txtImportPrice.getText());
            double total = Math.abs(qty) * price;

            DecimalFormat df = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.US));
            txtImportTotal.setText(df.format(total));
            txtImportPriceVAT.setText(String.valueOf(df.format(price)));

        } catch (Exception e) {
            txtImportTotal.setText("0");
        }
    }

    private void textFieldListener() {
        txtDeliveryTime.setValue(LocalDate.now());
        txtReportDate.setValue(LocalDate.now());
        // txtImportDate.setValue(LocalDate.now());
        txtQty.textProperty().addListener((obs, o, n) -> calculateTotal());
        txtPriceNET.textProperty().addListener((obs, o, n) -> calculateTotal());
    }

    private void textFieldListenerIm() {
        txtImportDate.setValue(LocalDate.now());
        txtImportReportDate.setValue(LocalDate.now());
        txtImportQty.textProperty().addListener((obs, o, n) -> calculateTotalIm());
        txtImportPrice.textProperty().addListener((obs, o, n) -> calculateTotalIm());
    }

    // =====================================================
    // SAVE
    // =====================================================
    @FXML
    private void onSave() {
        try {

            // ================= LẤY TEXT =================
            String productID = safeTrim(txtProductID);
            String productIDEx = safeTrim(txtImportProductIDVAT);
            String productIDVAT = safeTrim(txtProductIDVAT);
            String partNo = safeTrim(txtPartNo);
            String nameProduct = safeTrim(txtNameProduct);
            String qtyText = safeTrim(txtQty);
            String qtyImText = safeTrim(txtImportQty);
            String priceText = safeTrim(txtPriceNET);
            String priceImText = safeTrim(txtImportPrice);
            String cogsText = safeTrim(txtCogs);
            String cogsImText = safeTrim(txtImportCogs);
            String priceVATText = safeTrim(txtPriceVAT);
            String priceVATImText = safeTrim(txtImportPriceVAT);
            // String contractID = safeTrim(txtContractID);

            String contractID = safeTrim(txtImportContractID);

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
            if (!validateNumberField(qtyImText, "Số lượng"))
                return;
            if (!priceText.isEmpty() && !validateNumberField(priceText, "Giá NET"))
                return;
            if (!priceImText.isEmpty() && !validateNumberField(priceImText, "Giá NET"))
                return;
            if (!cogsText.isEmpty() && !validateNumberField(cogsText, "COGS"))
                return;
            if (!cogsImText.isEmpty() && !validateNumberField(cogsImText, "COGS"))
                return;
            if (!priceVATText.isEmpty() && !validateNumberField(priceVATText, "Giá VAT"))
                return;
            if (!priceVATImText.isEmpty() && !validateNumberField(priceVATImText, "Giá VAT"))
                return;

            if (txtDeliveryTime.getValue() == null) {
                customDialogNotification.showDialog("Lỗi", "Chưa chọn ngày giao của phần xuất!", Alert.AlertType.ERROR);
                return;
            }
            if (txtImportDate.getValue() == null) {
                customDialogNotification.showDialog("Lỗi", "Chưa chọn ngày giao của phần nhập!", Alert.AlertType.ERROR);
                return;
            }

            // ================= PARSE NUMBER =================
            double qtyNumber = safeParseDouble(qtyText);
            double qtyImNumber = safeParseDouble(qtyImText);
            double priceNumber = safeParseDouble(priceText);
            double priceImNumber = safeParseDouble(priceImText);
            double cogsValue = safeParseDouble(cogsText);
            double cogsExValue = safeParseDouble(cogsImText);
            double priceVATNumber = safeParseDouble(priceVATText);
            double priceVATImNumber = safeParseDouble(priceVATImText);
            double total = Math.abs(qtyNumber) * priceNumber;
            double totalEx = Math.abs(qtyImNumber) * priceImNumber;

            // Xuất kho -> đảo dấu số lượng
            if ("CREATEEX".equals(isEditMode)) {
                qtyNumber = -qtyNumber;
            }

            String qty = String.valueOf(qtyNumber);
            String price = String.valueOf(priceNumber);
            String priceVAT = String.valueOf(priceVATNumber);

            String qtyEx = String.valueOf(qtyImNumber);
            String priceEx = String.valueOf(priceImNumber);
            String priceVATEx = String.valueOf(priceVATImNumber);

            // ================= LẤY ACCOUNT =================
            Account account = AppState.getInstance().get("Account", Account.class);
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            String productAID = dbCRUDHelper.returnAID("Product", "ProductAID", "ProductID", productID);
            String productAIDEx = dbCRUDHelper.returnAID("Product", "ProductAID", "ProductID", productIDEx);
            String productAIDVAT = dbCRUDHelper.returnAID("Product", "ProductAID", "ProductID", productIDVAT);

            // int sourceID = getSupplierID(SourceID);
            // int deliveryID = getSupplierID(DeliveryID);
            // int billID = getBillID(BillID);
            // int paymentID = getPaymentID(PaymentID);
            // int employeeID = getEmployeeID(EmployeeID);
            // int manufacturerID = getManufacturerID(ManufacturerID);
            // int countryID = getCountryID(CountryID);
            // int unitID = getUnitID(UnitID);
            // int statusVAT = getStatusVAT(StatusVAT);
            // int businessID = getBusinessID(BusinessID);

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
            // int statusVAT = getStatusVAT(StatusVAT);
            int businessID = functionHelper.getComboBoxItemById(BusinessID,
                    Business::getBusinessID,
                    Business::getName);

            int sourceIDEx = functionHelper.getComboBoxItemById(ImportSourceID, Supplier::getSupplierID,
                    Supplier::getName);
            int deliveryIDEx = functionHelper.getComboBoxItemById(ImportDeliveryID, Supplier::getSupplierID,
                    Supplier::getName);
            int billIDEx = functionHelper.getComboBoxItemById(ImportBillID, Bill::getBillID, Bill::getName);
            int paymentIDEx = functionHelper.getComboBoxItemById(ImportPaymentID, Payment::getPaymentID,
                    Payment::getName);
            int employeeIDEx = functionHelper.getComboBoxItemById(ImportEmployeeID, Employee::getEmployeeID,
                    Employee::getNameEmployee);
            // int statusVAT = getStatusVAT(StatusVAT);
            int businessIDEx = functionHelper.getComboBoxItemById(ImportBusinessID,
                    Business::getBusinessID,
                    Business::getName);

            int statusVAT = getStatusVAT(ImportStatusVAT);

            // ================= VALIDATE COMBOBOX =================
            boolean isValidIm = validateRequiredFields(
                    productID,
                    nameProduct,
                    qty,
                    sourceID,
                    deliveryID,
                    billID,
                    unitID,
                    businessID,
                    txtDeliveryTime.getValue(), "phần Nhập");
            boolean isValidEx = validateRequiredFields(
                    productID,
                    nameProduct,
                    qty,
                    sourceIDEx,
                    deliveryIDEx,
                    billIDEx,
                    unitID,
                    businessID,
                    txtImportDate.getValue(), "phần Xuất");

            if (!isValidIm)
                return;
            if (!isValidEx)
                return;

            // ================= PREPARE COLUMN =================
            List<String> cartColumns = new ArrayList<>(arrayCRUD.cartColumns);
            cartColumns.remove("CartAID");
            cartColumns.remove("CartID");
            cartColumns.remove("PriceCost");
            cartColumns.remove("InvoiceNumber");

            // if ("CREATEEX".equals(isEditMode) || "CREATEIM".equals(isEditMode)) {

            List<Object> values = Arrays.asList(
                    account.getAccountID(), productAID, productAIDVAT, partNo, nameProduct,
                    manufacturerID, countryID, unitID, vehicelID, businessID, qty, price, total, cogsValue,
                    priceVAT, paymentID, billID, sourceID, deliveryID, employeeID,
                    false, txtDeliveryTime.getValue(), txtReportDate.getValue(), null, null, safeTrim(txtRemark), 1, now);
            List<Object> valuesEx = Arrays.asList(
                    account.getAccountID(), productAID, productAIDEx, partNo, nameProduct,
                    manufacturerID, countryID, unitID, vehicelID, businessIDEx, qtyEx, priceEx, totalEx, cogsExValue,
                    priceVATEx, paymentIDEx, billIDEx, sourceIDEx, deliveryIDEx, employeeIDEx,
                    false, txtImportDate.getValue(), txtImportReportDate.getValue(), statusVAT, contractID, safeTrim(txtImportRemark), 2, now);

            dbCRUDHelper.insertBatch("Cart", cartColumns, List.of(values, valuesEx));
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

    private void textFieldNumberOnly() {
        functionHelper.allowOnlyNumber(txtQty);
        functionHelper.allowOnlyNumber(txtImportQty);
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
            LocalDate deliveryDate, String typeValidate) {

        if (productID.isEmpty()) {
            customDialogNotification.showDialog("Lỗi", "Chưa nhập mã sản phẩm! " + typeValidate, Alert.AlertType.ERROR);
            return false;
        }

        if (nameProduct.isEmpty()) {
            customDialogNotification.showDialog("Lỗi", "Chưa nhập tên sản phẩm! " + typeValidate,
                    Alert.AlertType.ERROR);
            return false;
        }

        if (qty.isEmpty()) {
            customDialogNotification.showDialog("Lỗi", "Chưa nhập số lượng! " + typeValidate, Alert.AlertType.ERROR);
            return false;
        }

        if (sourceID == 0) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn nguồn hàng! " + typeValidate, Alert.AlertType.ERROR);
            return false;
        }

        if (deliveryID == 0) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn nơi nhận! " + typeValidate, Alert.AlertType.ERROR);
            return false;
        }

        if (billID == 0) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn loại hóa đơn! " + typeValidate,
                    Alert.AlertType.ERROR);
            return false;
        }
        if (businessID == 0) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn đơn vị! " + typeValidate,
                    Alert.AlertType.ERROR);
            return false;
        }

        if (unitID == 0) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn đơn vị tính! " + typeValidate, Alert.AlertType.ERROR);
            return false;
        }

        if (deliveryDate == null) {
            customDialogNotification.showDialog("Lỗi", "Chưa chọn ngày giao! " + typeValidate, Alert.AlertType.ERROR);
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

    private int getStatusVAT(ComboBox<CCBdata> cb) {
        Object value = cb.getValue();
        if (value == null)
            return 0;
        if (value instanceof CCBdata)
            return ((CCBdata) value).getId();
        return 0;
    }

}