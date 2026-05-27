package com.phuthanh.warehouse.screen.dialog;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.phuthanh.custom.CustomCombobox;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.info.Bill;
import com.phuthanh.model.info.Employee;
import com.phuthanh.model.info.Payment;
import com.phuthanh.model.info.Supplier;
import com.phuthanh.model.warehouse.Product;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DialogCreateCartSynthetic {

    @FXML
    private TextField txtProductID, txtPartNo, txtNameProduct,
            txtQty, txtPrice, txtTotal,
            txtPriceVAT, txtPriceNET, txtManufacturer, txtCountry, txtUnit,
            txtProductIDTransfer, txtPartNoTransfer, txtNameProductTransfer,
            txtQtyTransfer, txtPriceTransfer, txtTotalTransfer,
            txtPriceVATTransfer, txtPriceNETTransfer, txtManufacturerTransfer, txtCountryTransfer, txtUnitTransfer;

    @FXML
    private ComboBox<Payment> PaymentID, PaymentIDTransfer;

    @FXML
    private ComboBox<Supplier> SourceID, DeliveryID,
            SourceIDTransfer, DeliveryIDTransfer;

    @FXML
    private ComboBox<Bill> BillID, BillIDTransfer;

    @FXML
    private ComboBox<Employee> EmployeeID, EmployeeIDTransfer;

    @FXML
    private DatePicker txtDeliveryTime, txtDeliveryTimeTransfer;

    @FXML
    private TextArea txtRemark, txtRemarkTransfer;

    @FXML
    private Button btnSave, btnCancel;

    private Runnable callBack;
    private String isEditMode;
    private String productID;

    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final CustomCombobox customCombobox = new CustomCombobox();
    private final ArrayCRUD arrayCRUD = new ArrayCRUD();
    private   final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    // =====================================================
    // INIT
    // =====================================================
    @FXML
    private void initialize() {

        loadComboBox();

        // auto load product
        txtProductID.textProperty().addListener((obs, o, productID) -> {
            loadDataProduct(productID);
        });

        txtProductIDTransfer.textProperty().addListener((obs, o, productID) -> {
            loadDataProductTransfer(productID);
        });

        // auto tính tiền
        textFieldListener();
    }

    private void loadComboBox() {
        List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
        List<Bill> bills = dbInfoHelper.getAllBills();
        List<Payment> payments = dbInfoHelper.getAllPayments();
        List<Employee> employees = dbInfoHelper.getAllEmployee();

        customCombobox.setupComboBox(SourceID, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(DeliveryID, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(BillID, bills, Bill::getBillID, Bill::getName);
        customCombobox.setupComboBox(PaymentID, payments, Payment::getPaymentID, Payment::getName);
        customCombobox.setupComboBox(EmployeeID, employees, Employee::getEmployeeID, Employee::getNameEmployee);

        customCombobox.setupComboBox(SourceIDTransfer, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(DeliveryIDTransfer, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(BillIDTransfer, bills, Bill::getBillID, Bill::getName);
        customCombobox.setupComboBox(PaymentIDTransfer, payments, Payment::getPaymentID, Payment::getName);
        customCombobox.setupComboBox(EmployeeIDTransfer, employees, Employee::getEmployeeID, Employee::getNameEmployee);
    }

    public void setInitialData(Runnable cb, String iseditMode, String prodID) {
        this.callBack = cb;
        this.isEditMode = iseditMode;
        this.productID = prodID;
        textFieldNumberOnly();
        textFieldTransferListener();

        if (productID != null && !productID.isEmpty()) {
            txtProductID.setText(productID);
            txtProductIDTransfer.setText(productID);
            // loadDvcataProduct(productID);
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
            txtCountry.setText(product.getCountryName());
            txtManufacturer.setText(product.getManufacturerName());
            txtUnit.setText(product.getUnitName());
        } else {
            txtPartNo.clear();
            txtNameProduct.clear();
            txtPrice.clear();
            txtCountry.clear();
            txtManufacturer.clear();
            txtUnit.clear();
        }
    }

    private void loadDataProductTransfer(String productID) {
        Product product = dbInfoHelper.getProductByID(productID);
        if (product != null) {
            txtPartNoTransfer.setText(product.getID_PartNo());
            txtNameProductTransfer.setText(product.getNameProduct());
            txtCountryTransfer.setText(product.getCountryName());
            txtManufacturerTransfer.setText(product.getManufacturerName());
            txtUnitTransfer.setText(product.getUnitName());
        } else {
            txtPartNoTransfer.clear();
            txtNameProductTransfer.clear();
            txtPriceTransfer.clear();
            txtCountryTransfer.clear();
            txtManufacturerTransfer.clear();
            txtUnitTransfer.clear();
        }
    }

    // =====================================================
    // AUTO CALCULATE TOTAL
    // =====================================================
    private void calculateTotal() {
        try {
            double qty = txtQty.getText().isEmpty() ? 0 : Double.parseDouble(txtQty.getText());
            double convertQty = qty < 0 ? qty * -1 : qty;
            double price = txtPrice.getText().isEmpty() ? 0 : Double.parseDouble(txtPrice.getText());
            double total = Math.abs(convertQty) * price;

            DecimalFormat df = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.US));
            txtTotal.setText(df.format(total));

        } catch (Exception e) {
            txtTotal.setText("0");
        }
    }

    private void calculateTotalTransfer() {
        try {
            double qty = txtQtyTransfer.getText().isEmpty() ? 0 : Double.parseDouble(txtQtyTransfer.getText());
            double convertQty = qty < 0 ? qty * -1 : qty;
            double price = txtPriceTransfer.getText().isEmpty() ? 0 : Double.parseDouble(txtPriceTransfer.getText());
            double total = Math.abs(convertQty) * price;

            DecimalFormat df = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.US));
            txtTotalTransfer.setText(df.format(total));

        } catch (Exception e) {
            txtTotalTransfer.setText("0");
        }
    }

    private void textFieldListener() {
        txtQty.textProperty().addListener((obs, o, n) -> calculateTotal());
        txtPrice.textProperty().addListener((obs, o, n) -> calculateTotal());
    }

    private void textFieldTransferListener() {
        txtQtyTransfer.textProperty().addListener((obs, o, n) -> calculateTotalTransfer());
        txtPriceTransfer.textProperty().addListener((obs, o, n) -> calculateTotalTransfer());
    }

    // =====================================================
    // SAVE
    // =====================================================
    @FXML
    private void onSave() {
        try {
            String productID = safeTrim(txtProductID);
            String qty = safeTrim(txtQty);
            String price = safeTrim(txtPrice);
            String priceVAT = safeTrim(txtPriceVAT);
            String priceNET = safeTrim(txtPriceNET);
            double total = parseMoney(txtTotal.getText());

            String productIDTransfer = safeTrim(txtProductIDTransfer);
            String priceVATTransfer = safeTrim(txtPriceVATTransfer);
            String priceNETTransfer = safeTrim(txtPriceNETTransfer);
            String qtyTransfer = safeTrim(txtQtyTransfer);
            String priceTransfer = safeTrim(txtPriceTransfer);
            double totalTransfer = parseMoney(txtTotalTransfer.getText());

            if (productID.isEmpty()) {
                customDialogNotification.showDialog("Lỗi", "Chưa nhập mã sản phẩm!", Alert.AlertType.ERROR);
                return;
            }

            if (qty.isEmpty()) {
                customDialogNotification.showDialog("Lỗi", "Chưa nhập số lượng!", Alert.AlertType.ERROR);
                return;
            }

            if (txtDeliveryTime.getValue() == null) {
                customDialogNotification.showDialog("Lỗi", "Chưa chọn ngày giao!", Alert.AlertType.ERROR);
                return;
            }

            Account account = AppState.getInstance().get("Account", Account.class);
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            String productAID = dbCRUDHelper.returnAID("Product", "ProductAID", "ProductID", productID);
            String productAIDTransfer = dbCRUDHelper.returnAID("Product", "ProductAID", "ProductID", productIDTransfer);

            int sourceID = getSupplierID(SourceID);
            int deliveryID = getSupplierID(DeliveryID);
            int billID = getBillID(BillID);
            int paymentID = getPaymentID(PaymentID);
            int employeeID = getEmployeeID(EmployeeID);

            int sourceIDTransfer = getSupplierID(SourceIDTransfer);
            int deliveryIDTransfer = getSupplierID(DeliveryIDTransfer);
            int billIDTransfer = getBillID(BillIDTransfer);
            int paymentIDTransfer = getPaymentID(PaymentIDTransfer);
            int employeeIDTransfer = getEmployeeID(EmployeeIDTransfer);

            List<String> cartColumns = new ArrayList<>(arrayCRUD.cartColumns);
            cartColumns.remove("CartAID");
            cartColumns.remove("CartID");
            List<String> requestCartColumns = new ArrayList<>(arrayCRUD.requestCartColumns);
            requestCartColumns.remove("RequestAID");

            if (isEditMode.equals("SYNTHETIC")) {
                // nhập xuất bình thường
                List<Object> values = Arrays.asList(
                        account.getAccountID(), productAID, qty, price, total, priceVAT, priceNET, paymentID, billID,
                        sourceID, deliveryID, employeeID, false, txtDeliveryTime.getValue(),
                        safeTrim(txtRemark), now);
                // nhập xuất tổng hợp
                List<Object> valuesIM = Arrays.asList(
                        account.getAccountID(), productAIDTransfer, qtyTransfer, priceTransfer, totalTransfer,
                        priceVATTransfer,
                        priceNETTransfer, paymentIDTransfer, billIDTransfer,
                        sourceIDTransfer, 239, employeeIDTransfer, false, txtDeliveryTimeTransfer.getValue(),
                        safeTrim(txtRemarkTransfer), now);
                List<Object> valuesEX = Arrays.asList(
                        account.getAccountID(), productAIDTransfer, qtyTransfer, priceTransfer, totalTransfer,
                        priceVATTransfer,
                        priceNETTransfer, paymentIDTransfer, billIDTransfer,
                        239, deliveryIDTransfer, employeeIDTransfer, false, txtDeliveryTimeTransfer.getValue(),
                        safeTrim(txtRemarkTransfer), now);
                dbCRUDHelper.insertBatch("Cart", cartColumns, Arrays.asList(values, valuesIM, valuesEX));

                customDialogNotification.showDialog("Thành công", "Tạo phiếu thành công", Alert.AlertType.INFORMATION);

                if (callBack != null) {
                    callBack.run();
                }
                closeWindow();
            }

        } catch (Exception e) {
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi", "Lưu thất bại: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private int getSupplierID(ComboBox<Supplier> cb) {
        return cb.getValue() == null ? 0 : cb.getValue().getSupplierID();
    }

    private int getBillID(ComboBox<Bill> cb) {
        return cb.getValue() == null ? 0 : cb.getValue().getBillID();
    }

    private int getPaymentID(ComboBox<Payment> cb) {
        return cb.getValue() == null ? 0 : cb.getValue().getPaymentID();
    }

    private int getEmployeeID(ComboBox<Employee> cb) {
        return cb.getValue() == null ? 0 : cb.getValue().getEmployeeID();
    }

    private double parseMoney(String text) {
        if (text == null || text.isEmpty())
            return 0;
        return Double.parseDouble(text.replace(",", ""));
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
        functionHelper.allowOnlyNumber(txtQtyTransfer);
        functionHelper.allowOnlyNumber(txtPrice);
        functionHelper.allowOnlyNumber(txtPriceTransfer);
        functionHelper.allowOnlyNumber(txtPriceVAT);
        functionHelper.allowOnlyNumber(txtPriceNET);
        functionHelper.allowOnlyNumber(txtPriceVATTransfer);
        functionHelper.allowOnlyNumber(txtPriceNETTransfer);
    }
}