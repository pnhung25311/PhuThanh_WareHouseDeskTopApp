package com.phuthanh.warehouse.screen.dialog;

import java.sql.SQLException;
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
import com.phuthanh.model.info.Bill;
import com.phuthanh.model.info.Country;
import com.phuthanh.model.info.Employee;
import com.phuthanh.model.info.Manufacturer;
import com.phuthanh.model.info.Payment;
import com.phuthanh.model.info.Supplier;
import com.phuthanh.model.info.Unit;
import com.phuthanh.model.warehouse.Cart;
import com.phuthanh.model.warehouse.RequestCart;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DialogViewDataRequestCart {

    // ===== OLD =====
    @FXML
    private TextField txtProductID, txtProductIDNew, txtPartNo, txtPartNoNew, txtNameProduct, txtNameProductNew,
            txtProductIDVAT, txtProductIDVATNew, txtCogs, txtPriceCost, txtGrossPriceVAT, txtContractID,
            txtInvoiceNumber;
    @FXML
    private ComboBox<Manufacturer> ManufacturerID, ManufacturerIDNew;
    @FXML
    private ComboBox<Country> CountryIDNew, CountryID;
    @FXML
    private ComboBox<Unit> UnitIDNew, UnitID;
    @FXML
    private TextField txtQty, txtQtyNew, txtPriceNET, txtPriceNETNew, txtPriceVAT, txtPriceVATNew, txtTotal,
            txtTotalNew, txtCogsNew, txtPriceCostNew, txtGrossPriceVATNew, txtContractIDNew, txtInvoiceNumberNew;
    @FXML
    private ComboBox<Payment> PaymentID, PaymentIDNew;
    @FXML
    private ComboBox<Supplier> SourceID, SourceIDNew, DeliveryID, DeliveryIDNew;
    @FXML
    private ComboBox<Bill> BillID, BillIDNew;
    @FXML
    private ComboBox<Employee> EmployeeID, EmployeeIDNew;

    @FXML
    private DatePicker txtDeliveryTime, txtDeliveryTimeNew, txtReportDate, txtReportDateNew;
    @FXML
    private TextArea txtRemark, txtRemarkNew;

    // ===== NEW =====
    @FXML
    private TextField txtProductIDTransfer, txtPartNoTransfer, txtNameProductTransfer;
    private String codeAID;

    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private final ArrayCRUD arrayCRUD = new ArrayCRUD();
    private final CustomCombobox customCombobox = new CustomCombobox();
    // private Cart cartModel;
    private RequestCart requestCartModel;
    private Runnable callback;

    @FXML
    public void initialize() {
        loadComboBox();
        functionHelper.setupMoneyField(txtPriceNET);
        functionHelper.setupMoneyField(txtPriceVAT);
        functionHelper.setupMoneyField(txtTotal);
        functionHelper.setupMoneyField(txtPriceCost);
        functionHelper.setupMoneyField(txtGrossPriceVAT);
        functionHelper.setupMoneyField(txtPriceNETNew);
        functionHelper.setupMoneyField(txtPriceVATNew);
        functionHelper.setupMoneyField(txtTotalNew);
        functionHelper.setupMoneyField(txtPriceCostNew);
        functionHelper.setupMoneyField(txtGrossPriceVATNew);

    }

    // ===== LOAD DATA =====
    public void setData(String AID, Runnable cb) {
        this.codeAID = AID;
        this.callback = cb;
        if (codeAID != null) {
            loadDataRequestCart(codeAID);
        }
    }

    // ===== CONFIRM =====
    @FXML
    private void onConfirm() {
        try {
            boolean confirm = customDialogNotification.showDialogConfirm("Xác nhận yêu cầu",
                    "Bạn có chắc muốn xác nhận yêu cầu không?",
                    "Hành động này không thể hoàn tác.", "Xác nhận", "Thoát");
            if (confirm) {
                String getAction = dbCRUDHelper.returnAID(
                        "RequestCart", "Action", "RequestAID", codeAID);
                System.out.println("CurrentAID trong onConfirm: " + getAction);
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                List<String> cartColumns = new ArrayList<>(arrayCRUD.cartColumns);
                cartColumns.remove("CartAID");
                cartColumns.remove("CartID");
                Account account = AppState.getInstance().get("Account", Account.class);

                if (getAction.equals("1")) {
                    // RequestCart rc = dbInfoHelper.getRequestCartByAID(Integer.parseInt(codeAID));
                    List<Object> values = Arrays.asList(
                            requestCartModel.getAccountID(), requestCartModel.getProductAID(),
                            requestCartModel.getQty(), requestCartModel.getPrice(),
                            requestCartModel.getTotal(), requestCartModel.getPriceVAT(), requestCartModel.getPrice(),
                            requestCartModel.getPaymentID(), requestCartModel.getBillID(),
                            requestCartModel.getSourceID(), requestCartModel.getDeliveryID(),
                            requestCartModel.getEmployeeID(), false, requestCartModel.getDeliveryTime(),
                            requestCartModel.getRemark(), now);
                    int rowUpdated = dbCRUDHelper.update("Cart", cartColumns, values, "CartAID = ?",
                            Arrays.asList(requestCartModel.getCartAID()));
                    if (rowUpdated > 0) {
                        dbCRUDHelper.update("RequestCart", Arrays.asList("UserConfirm", "TimeConfirm"),
                                Arrays.asList(account.getAccountID(), now), "RequestAID = ?", Arrays.asList(codeAID));
                        customDialogNotification.showDialog("Xác nhận thành công",
                                "Yêu cầu đã được xác nhận và chuyển vào kho hàng.", Alert.AlertType.INFORMATION);
                        if (callback != null) {
                            callback.run();
                        }
                    } else {
                        customDialogNotification.showDialog("Xác nhận thất bại",
                                "Đã có lỗi xảy ra khi xác nhận yêu cầu. Vui lòng thử lại.", Alert.AlertType.ERROR);
                    }
                } else {
                    RequestCart rc = dbInfoHelper.getRequestCartByAID(Integer.parseInt(codeAID));
                    int rowUpdated = dbCRUDHelper.delete("Cart", Arrays.asList("CartAID"),
                            Arrays.asList(rc.getCartAID()));
                    if (rowUpdated > 0) {
                        dbCRUDHelper.update("RequestCart", Arrays.asList("UserConfirm", "TimeConfirm"),
                                Arrays.asList(account.getAccountID(), now), "RequestAID = ?", Arrays.asList(codeAID));
                        customDialogNotification.showDialog("Xác nhận thành công",
                                "Yêu cầu đã được xác nhận và chuyển vào kho hàng.", Alert.AlertType.INFORMATION);
                        if (callback != null) {
                            callback.run();
                        }
                    } else {
                        customDialogNotification.showDialog("Xác nhận thất bại",
                                "Đã có lỗi xảy ra khi xác nhận yêu cầu. Vui lòng thử lại.", Alert.AlertType.ERROR);
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // TODO call service save/update
        System.out.println("Saved product: " + codeAID);
        close();
    }

    @FXML
    private void onCloseClick() {
        close();
    }

    private void close() {
        txtProductID.getScene().getWindow().hide();
    }

    private void loadDataCart(String cartAID) {
        Cart model = dbInfoHelper.getCartByAID(Integer.parseInt(cartAID)); // gọi DAO lấy 1 item
        if (model == null)
            return;
        // cartModel = model;
        // ===== Product Info =====
        txtProductID.setText(model.getProductID());
        txtPartNo.setText(model.getPartNo());
        txtNameProduct.setText(model.getNameProduct());
        txtPriceNET.setText(String.valueOf(model.getPrice()));
        txtTotal.setText(String.valueOf(model.getTotal()));
        txtPriceVAT.setText(String.valueOf(model.getPriceVAT()));

        // ===== Quantity =====
        txtProductIDVAT.setText(model.getProductIDVAT());

        // ===== Quantity =====
        txtQty.setText(String.valueOf(model.getQty()));

        // ===== Delivery Time =====
        if (model.getDeliveryTime() != null) {
            txtDeliveryTime.setValue(model.getDeliveryTime());
        }
        if (model.getReportDate() != null) {
            txtReportDate.setValue(model.getReportDate());
        }
        txtCogs.setText(model.getCogs() + "");
        txtContractID.setText(model.getContractID());
        txtInvoiceNumber.setText(model.getInvoiceNumber() + "");
        txtPriceCost.setText(model.getPriceCost() + "");
        txtGrossPriceVAT.setText(model.getGrossPriceVAT() + "");

        // ===== Remark =====
        txtRemark.setText(model.getRemark());

        // ===== Partner / Supplier =====
        functionHelper.selectComboBoxItemById(ManufacturerID, model.getManufacturerID(),
                Manufacturer::getManufacturerID);
        functionHelper.selectComboBoxItemById(CountryID, model.getCountryID(), Country::getCountryID);
        functionHelper.selectComboBoxItemById(UnitID, model.getUnitID(), Unit::getUnitID);
        functionHelper.selectComboBoxItemById(SourceID, model.getSourceID(), Supplier::getSupplierID);
        functionHelper.selectComboBoxItemById(SourceID, model.getSourceID(), Supplier::getSupplierID);
        functionHelper.selectComboBoxItemById(SourceID, model.getSourceID(), Supplier::getSupplierID);
        functionHelper.selectComboBoxItemById(DeliveryID, model.getDeliveryID(), Supplier::getSupplierID);
        functionHelper.selectComboBoxItemById(BillID, model.getBillID(), Bill::getBillID);
        functionHelper.selectComboBoxItemById(PaymentID, model.getPaymentID(), Payment::getPaymentID);
        functionHelper.selectComboBoxItemById(EmployeeID, model.getEmployeeID(), Employee::getEmployeeID);

        // ===== Status (optional, nếu có control hiển thị) =====
        // txtStatus.setText(model.getStatus());
    }

    private void loadDataRequestCart(String cartAID) {
        RequestCart model = dbInfoHelper.getRequestCartByAID(Integer.parseInt(cartAID)); // gọi DAO lấy 1 item
        if (model == null)
            return;
        requestCartModel = model;
        loadDataCart(model.getCartAID() + "");
        // ===== Product Info =====
        txtProductIDNew.setText(model.getProductID());
        txtPartNoNew.setText(model.getPartNo());
        txtNameProductNew.setText(model.getNameProduct());
        txtPriceNETNew.setText(String.valueOf(model.getPrice()));
        txtTotalNew.setText(String.valueOf(model.getTotal()));
        txtPriceVATNew.setText(String.valueOf(model.getPriceVAT()));

        // ===== Quantity =====
        txtProductIDVATNew.setText(model.getProductIDVAT() + "");

        // ===== Quantity =====
        txtQtyNew.setText(String.valueOf(model.getQty()));

        // ===== Delivery Time =====
        if (model.getDeliveryTime() != null) {
            txtDeliveryTimeNew.setValue(model.getDeliveryTime());
        }
        if (model.getReportDate() != null) {
            txtReportDateNew.setValue(model.getReportDate());
        }

        // ===== Remark =====
        txtRemarkNew.setText(model.getRemark());
        txtCogsNew.setText(model.getCogs() + "");
        // txtContractIDNew.setText(model.getContractID()+"");
        txtInvoiceNumberNew.setText(model.getInvoiceNumber() + "");
        txtPriceCostNew.setText(model.getPriceCost() + "");
        txtGrossPriceVATNew.setText(model.getGrossPriceVAT() + "");
        txtContractIDNew.setText(model.getContractID());

        // ===== Partner / Supplier =====
        System.out.println(model.getManufacturerID());
        System.out.println(model.getCountryID());
        System.out.println(model.getUnitID());

        functionHelper.selectComboBoxItemById(ManufacturerIDNew, model.getManufacturerID(),
                Manufacturer::getManufacturerID);
        functionHelper.selectComboBoxItemById(CountryIDNew, model.getCountryID(), Country::getCountryID);
        functionHelper.selectComboBoxItemById(UnitIDNew, model.getUnitID(), Unit::getUnitID);
        functionHelper.selectComboBoxItemById(SourceIDNew, model.getSourceID(), Supplier::getSupplierID);
        functionHelper.selectComboBoxItemById(DeliveryIDNew, model.getDeliveryID(), Supplier::getSupplierID);
        functionHelper.selectComboBoxItemById(BillIDNew, model.getBillID(), Bill::getBillID);
        functionHelper.selectComboBoxItemById(PaymentIDNew, model.getPaymentID(), Payment::getPaymentID);
        functionHelper.selectComboBoxItemById(EmployeeIDNew, model.getEmployeeID(), Employee::getEmployeeID);

        // ===== Status (optional, nếu có control hiển thị) =====
        // txtStatus.setText(model.getStatus());
    }

    private void loadComboBox() {
        List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
        List<Bill> bills = dbInfoHelper.getAllBills();
        List<Payment> payments = dbInfoHelper.getAllPayments();
        List<Employee> employees = dbInfoHelper.getAllEmployee();
        List<Manufacturer> manufacturers = dbInfoHelper.getAllManufacturer();
        List<Unit> units = dbInfoHelper.getAllUnits();
        List<Country> countries = dbInfoHelper.getAllCountries();

        customCombobox.setupComboBox(SourceID, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(SourceIDNew, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(DeliveryID, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(DeliveryIDNew, suppliers, Supplier::getSupplierID, Supplier::getName);
        customCombobox.setupComboBox(BillID, bills, Bill::getBillID, Bill::getName);
        customCombobox.setupComboBox(BillIDNew, bills, Bill::getBillID, Bill::getName);
        customCombobox.setupComboBox(PaymentID, payments, Payment::getPaymentID, Payment::getName);
        customCombobox.setupComboBox(PaymentIDNew, payments, Payment::getPaymentID, Payment::getName);
        customCombobox.setupComboBox(EmployeeID, employees, Employee::getEmployeeID, Employee::getNameEmployee);
        customCombobox.setupComboBox(EmployeeIDNew, employees, Employee::getEmployeeID, Employee::getNameEmployee);
        customCombobox.setupComboBox(ManufacturerID, manufacturers, Manufacturer::getManufacturerID,
                Manufacturer::getName);
        customCombobox.setupComboBox(ManufacturerIDNew, manufacturers, Manufacturer::getManufacturerID,
                Manufacturer::getName);
        customCombobox.setupComboBox(UnitID, units, Unit::getUnitID, Unit::getName);
        customCombobox.setupComboBox(UnitIDNew, units, Unit::getUnitID, Unit::getName);
        customCombobox.setupComboBox(CountryID, countries, Country::getCountryID, Country::getName);
        customCombobox.setupComboBox(CountryIDNew, countries, Country::getCountryID, Country::getName);

    }

}