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
import com.phuthanh.model.warehouse.Cart;
// import com.phuthanh.model.warehouse.Cart;
import com.phuthanh.model.warehouse.Product;
// import com.phuthanh.model.warehouse.RequestCart;
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

public class DialogCreateCartUpdate {

    // ================= LABEL =================
    @FXML
    private Label lblRemarkOfRequest, lblReportDate, lblDeliveryTime, lblBusiness, lblTypeCart;

    // ================= TEXTFIELD =================
    @FXML
    private TextField txtProductID, txtPartNo, txtNameProduct, txtQty, txtProductIDVAT, txtNameProductVAT,
            txtTotal, txtPriceVAT, txtPriceNET, txtCogs, txtContractID, txtVehicelID, txtInvoiceNumber;

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
    private String CodeAID;
    private Cart model;
    private String vehicelID;
    private ObservableList<VehicleTypeItem> masterList = FXCollections.observableArrayList();

    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
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

        if (isEditMode.equals("CREATEEX")) {
            customCombobox.setupComboBox(SourceID, suppliers4, Supplier::getSupplierID, Supplier::getName);
            customCombobox.setupComboBox(DeliveryID, suppliers, Supplier::getSupplierID, Supplier::getName);

        }
        if (isEditMode.equals("CREATEIM")) {
            customCombobox.setupComboBox(DeliveryID, suppliers4, Supplier::getSupplierID, Supplier::getName);
            customCombobox.setupComboBox(SourceID, suppliers, Supplier::getSupplierID, Supplier::getName);
        }
    }

    public void setInitialData(Runnable cb, String iseditMode, String codeAID, boolean isRequest) {
        this.callBack = cb;
        this.isEditMode = iseditMode;
        this.CodeAID = codeAID;

        loadComboboxIsEditMode();
        textFieldNumberOnly();

        if (CodeAID != null) {
            try {
                model = dbInfoHelper.getCartByAID(Integer.parseInt(CodeAID));
                if (model == null)
                    return;

                DecimalFormat df = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.US));

                // ===== PRODUCT =====
                txtProductID.setText(model.getProductID());
                txtProductIDVAT.setText(model.getProductIDVAT());
                txtPartNo.setText(model.getPartNo());
                txtNameProduct.setText(model.getNameProduct());
                vehicelID = model.getVehicleTypeID();
                loadItem(vehicelID);
                // ===== NUMBER =====
                txtQty.setText(String.valueOf(model.getQty()));
                txtPriceNET.setText(df.format(model.getPrice()));
                txtPriceVAT.setText(df.format(model.getPriceVAT()));
                txtCogs.setText(df.format(model.getCogs()));
                txtTotal.setText(df.format(model.getTotal()));

                // ===== DATE =====
                txtDeliveryTime.setValue(model.getDeliveryTime());
                txtReportDate.setValue(model.getReportDate());

                // ===== TEXT =====
                txtRemark.setText(model.getRemark());
                txtContractID.setText(model.getContractID());
                txtInvoiceNumber.setText(model.getInvoiceNumber());

                if (model.getTypeCartID() == 1) {
                    lblDeliveryTime.setText("Ngày nhập kho");
                    lblReportDate.setText("Ngày mua hàng");
                    lblBusiness.setText("Nhập về đơn vị nào");
                    lblTypeCart.setText("LOẠI PHIẾU NHẬP");
                    StatusVAT.setEditable(false);
                    txtContractID.setEditable(false);
                }
                if (model.getTypeCartID() == 2) {
                    lblDeliveryTime.setText("Ngày xuất kho");
                    lblReportDate.setText("Ngày giao hàng");
                    lblBusiness.setText("Đơn vị bán hàng trực tiếp");
                    lblTypeCart.setText("LOẠI PHIẾU XUẤT");
                }
                if (model.getTypeCartID() == 3) {
                    lblDeliveryTime.setText("Ngày điều chuyển");
                    lblReportDate.setText("Ngày giao hàng");
                    lblBusiness.setText("Hàng của đơn vị nào");
                    lblTypeCart.setText("LOẠI PHIẾU ĐIỀU CHUYỂN");
                }

                // ===== COMBOBOX =====
                functionHelper.selectComboBoxItemById(SourceID, model.getSourceID(), Supplier::getSupplierID);
                functionHelper.selectComboBoxItemById(DeliveryID, model.getDeliveryID(), Supplier::getSupplierID);
                functionHelper.selectComboBoxItemById(BillID, model.getBillID(), Bill::getBillID);
                functionHelper.selectComboBoxItemById(PaymentID, model.getPaymentID(), Payment::getPaymentID);
                functionHelper.selectComboBoxItemById(EmployeeID, model.getEmployeeID(), Employee::getEmployeeID);
                functionHelper.selectComboBoxItemById(ManufacturerID, model.getManufacturerID(),
                        Manufacturer::getManufacturerID);
                functionHelper.selectComboBoxItemById(CountryID, model.getCountryID(), Country::getCountryID);
                functionHelper.selectComboBoxItemById(UnitID, model.getUnitID(), Unit::getUnitID);
                functionHelper.selectComboBoxItemById(BusinessID, model.getBusinessID(), Business::getBusinessID);

                // VAT status
                StatusVAT.getSelectionModel().select(
                        model.getStatusVAT() != null && model.getStatusVAT() == 1 ? 0 : 1);
                if (!isRequest) {
                    if (model.getStatusID() == 1) {
                        txtProductID.setEditable(false);
                        txtQty.setEditable(false);
                        SourceID.setDisable(true);
                        DeliveryID.setDisable(true);
                        EmployeeID.setDisable(true);
                        txtDeliveryTime.setEditable(false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    private void loadDataProduct(String productID) {
        Product product = dbInfoHelper.getProductByID(productID);
        if (product != null) {
            txtPartNo.setText(product.getID_PartNo());
            txtNameProduct.setText(product.getNameProduct());
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

    private void textFieldListener() {
        txtDeliveryTime.setValue(LocalDate.now());
        txtQty.textProperty().addListener((obs, o, n) -> calculateTotal());
        txtPriceNET.textProperty().addListener((obs, o, n) -> calculateTotal());
    }

    @FXML
    private void onSave() {
        try {

            // ================= LẤY TEXT =================
            String productID = safeTrim(txtProductID);
            String productIDVAT = safeTrim(txtProductIDVAT);
            String partNo = safeTrim(txtPartNo);
            String nameProduct = safeTrim(txtNameProduct);
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

            // int sourceID = getSupplierID(SourceID);
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

            // ================= PREPARE COLUMN =================
            List<String> cartColumns = new ArrayList<>(arrayCRUD.cartColumns);
            cartColumns.remove("CartAID");
            cartColumns.remove("CartID");
            cartColumns.remove("TypeCartID");
            cartColumns.remove("PriceCost");
            cartColumns.remove("GrossPriceVAT");

            List<String> cartColumnsRequest = new ArrayList<>(arrayCRUD.requestCartColumns);
            cartColumnsRequest.removeAll(List.of("RequestAID"));
            cartColumnsRequest.remove("PriceCost");
            cartColumnsRequest.remove("GrossPriceVAT");
            // cartColumnsRequest.remove("CartID");

            List<String> cartColumnsInsert = new ArrayList<>(arrayCRUD.cartColumns);
            cartColumnsInsert.remove("CartAID");
            cartColumnsInsert.remove("CartID");
            cartColumnsInsert.remove("PriceCost");

            // if ("CREATEEX".equals(isEditMode) || "CREATEIM".equals(isEditMode)) {
            int actionID = isEditMode.equals("UPDATE") ? 1 : 0; // 2 = yêu cầu xuất kho, 3 = yêu cầu nhập kho

            List<Object> values = Arrays.asList(
                    account.getAccountID(), productAID, productAIDVAT, partNo, nameProduct,
                    manufacturerID, countryID, unitID, vehicelID, businessID, qty, price, total, cogsValue, priceVAT,
                    paymentID, billID, sourceID, deliveryID, employeeID, false, txtDeliveryTime.getValue(),
                    txtReportDate.getValue(), statusVAT, contractID, invoiceNumber, safeTrim(txtRemark), now);
            List<Object> valuesRequest = Arrays.asList(
                    model.getCartAID(), model.getCartID(), model.getAccountID(), productAID, productAIDVAT, partNo,
                    nameProduct, manufacturerID, countryID, unitID, vehicelID, businessID, qty, price, total, cogsValue,
                    priceVAT, paymentID, billID, sourceID, deliveryID, employeeID, model.getStatusID(), txtDeliveryTime.getValue(),
                    txtReportDate.getValue(), statusVAT, contractID, invoiceNumber, safeTrim(txtRemark),
                    model.getTypeCartID(), model.getLastTime(), account.getAccountID(), now, null, null, actionID,
                    safeTrim(txtRemarkOfRequest), now);
            List<Object> valuesInsert = Arrays.asList(
                    account.getAccountID(), productAID, productAIDVAT, partNo, nameProduct,
                    manufacturerID, countryID, unitID, vehicelID, businessID, qty, price, total, cogsValue, priceVAT,
                    paymentID, billID, sourceID, deliveryID, employeeID, false, txtDeliveryTime.getValue(),
                    txtReportDate.getValue(), statusVAT, contractID, invoiceNumber, safeTrim(txtRemark), 2, now);
            if (isEditMode.equals("UPDATE")) {
                if (model.getStatusID() == 0) {
                    int update = dbCRUDHelper.update("Cart", cartColumns, values, "CartAID = ?", List.of(CodeAID));
                    System.out.println(update);
                    customDialogNotification.showDialog("Thành công", "Cập nhật phiếu thành công",
                            Alert.AlertType.INFORMATION);
                } else {
                    int update = dbCRUDHelper.insert("RequestCart", cartColumnsRequest, valuesRequest);
                    System.out.println(update);
                    customDialogNotification.showDialog("Thành công", "Tạo phiếu yêu cầu thành công",
                            Alert.AlertType.INFORMATION);
                }
            } else if (isEditMode.equals("DELETE")) {
                if (model.getStatusID() == 0) {
                    int delete = dbCRUDHelper.delete("Cart", List.of("CartAID"), List.of(CodeAID));
                    System.out.println(delete);
                    customDialogNotification.showDialog("Thành công", "Xóa phiếu thành công",
                            Alert.AlertType.INFORMATION);
                } else {
                    int delete = dbCRUDHelper.insert("RequestCart", cartColumnsRequest, valuesRequest);
                    System.out.println(delete);
                    customDialogNotification.showDialog("Thành công", "Tạo phiếu yêu cầu thành công",
                            Alert.AlertType.INFORMATION);
                }
            } else if (isEditMode.equals("INSERT")) {
                int insert = dbCRUDHelper.insert("Cart", cartColumnsInsert, valuesInsert);
                System.out.println(insert);
                customDialogNotification.showDialog("Thành công", "Xóa phiếu thành công",
                        Alert.AlertType.INFORMATION);
            }

            // }

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
        return cb.getValue() == null ? 0 : cb.getValue().getId();
    }

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