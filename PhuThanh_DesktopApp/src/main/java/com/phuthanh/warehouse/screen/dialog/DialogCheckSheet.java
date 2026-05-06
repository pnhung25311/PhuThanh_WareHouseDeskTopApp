package com.phuthanh.warehouse.screen.dialog;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.warehouse.DataCheck;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.model.warehouse.Product;
import com.phuthanh.model.warehouse.WareHouse;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DialogCheckSheet {

    // ===== Buttons =====
    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    // ===== TextFields =====
    @FXML
    private TextField txtProductID;

    @FXML
    private TextField txtPartNo;

    @FXML
    private TextField txtNameProduct;

    @FXML
    private TextField txtCountry;

    @FXML
    private TextField txtSupplier;

    @FXML
    private TextField txtUnit;

    @FXML
    private TextField txtQty_WareHouse;

    @FXML
    private TextField txtQty_Check;

    @FXML
    private TextField txtQty_Differrent;

    // ===== TextArea =====
    @FXML
    private TextArea txtRemarkOfHistory;
    private static final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private static final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private static final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    private String productAID;
    private String sheetAID;
    private DrawerItem selectedDrawerItem;
    private Runnable CallBack;
    // private static final FunctionHelper functionHelper = new FunctionHelper();

    // ===== Initialize =====
    @FXML
    private void initialize() {
        // Nếu chỉ xem, có thể disable
        // txtProductID.setDisable(true);

        // Ví dụ: chỉ cho nhập số
        // txtQty_Check.textProperty().addListener((obs, oldV, newV) -> {
        // if (!newV.matches("\\d*")) {
        // txtQty_Check.setText(oldV);
        // }
        // });
        setupAutoFill();
    }

    private void setupAutoFill() {

        txtProductID.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // mất focus
                try {
                    String id = txtProductID.getText().trim();
                    String proAID = dbCRUDHelper.returnAID("Product", "ProductAID", "ProductID", id);
                    txtCountry.setText("");
                    txtNameProduct.setText("");
                    txtPartNo.setText("");
                    txtSupplier.setText("");
                    txtUnit.setText("");
                    txtQty_Check.setText("");
                    txtQty_WareHouse.setText("");
                    txtQty_Differrent.setText("");
                    txtRemarkOfHistory.setText("");
                    if (proAID != null) {
                        DataCheck dataCheck = dbInfoHelper.getDataChecktByAID(
                                selectedDrawerItem.getWareHouseDataCheck(),
                                sheetAID, proAID);
                        if (dataCheck != null) {
                            productAID = proAID;
                            // txtProductID.setText(dataCheck.getProductID());
                            txtPartNo.setText(dataCheck.getIdPartNo());
                            txtNameProduct.setText(dataCheck.getNameProduct());
                            txtCountry.setText(dataCheck.getNameCountry());
                            txtSupplier.setText(dataCheck.getNameSupplier());
                            txtUnit.setText(dataCheck.getNameUnit());
                            txtQty_Check.setText(dataCheck.getQtyCheck() + "");
                            txtQty_Differrent.setText(dataCheck.getQtyDifferent() + "");
                            txtQty_WareHouse.setText(dataCheck.getQtyWareHouse() + "");
                        } else {
                            Product product = dbInfoHelper.getProductByID(id);
                            productAID = proAID;

                            String whAID = dbCRUDHelper.returnAID(selectedDrawerItem.getWareHouseHistory(),
                                    "DataWareHouseAID", "ProductAID", productAID);
                            WareHouse wh = dbInfoHelper.getWareHouseByAID(whAID);
                            String nameCountry = "";
                            String nameSupplier = "";
                            String nameUnit = "";
                            if (wh != null) {
                                productAID = proAID;
                                // txtProductID.setText(dataCheck.getProductID());
                                txtNameProduct.setText(product.getNameProduct());
                                txtPartNo.setText(product.getID_PartNo());
                                nameCountry = dbCRUDHelper.returnAID("Country", "Name", "CountryID",
                                        product.getCountryID() + "");
                                nameSupplier = dbCRUDHelper.returnAID("Supplier", "Name", "SupplierID",
                                        product.getSupplierID() + "");
                                nameUnit = dbCRUDHelper.returnAID("Unit", "Name", "UnitID",
                                        product.getUnitID() + "");
                                txtCountry.setText(nameCountry);
                                txtSupplier.setText(nameSupplier);
                                txtUnit.setText(nameUnit);
                                txtQty_WareHouse.setText(wh.getQty() + "");
                            } else {
                                txtNameProduct.setText(product.getNameProduct());
                                txtPartNo.setText(product.getID_PartNo());
                                nameCountry = dbCRUDHelper.returnAID("Country", "Name", "CountryID",
                                        product.getCountryID() + "");
                                nameSupplier = dbCRUDHelper.returnAID("Supplier", "Name", "SupplierID",
                                        product.getSupplierID() + "");
                                nameUnit = dbCRUDHelper.returnAID("Unit", "Name", "UnitID",
                                        product.getUnitID() + "");
                                txtCountry.setText(nameCountry);
                                txtSupplier.setText(nameSupplier);
                                txtUnit.setText(nameUnit);

                            }

                            // String
                        }

                    } else {
                        customDialogNotification.showDialog("Cảnh báo", "Không tìm thấy sản phẩm với Mã: " + id,
                                Alert.AlertType.WARNING);
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    System.out.println(e.getMessage());
                }
            }

        });

        txtQty_Check.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                double qtyWH = 0;
                double qtyDiff = 0;
                double qtyCheck = txtQty_Check.getText().trim().isEmpty() ? 0
                        : Double.parseDouble(txtQty_Check.getText().trim());
                if (txtQty_WareHouse != null && txtQty_WareHouse.getText() != null) {
                    qtyWH = Double.parseDouble(txtQty_WareHouse.getText().trim());
                }
                try {
                    qtyCheck = Double.parseDouble(txtQty_Check.getText().trim());
                } catch (NumberFormatException e) {
                    qtyCheck = 0;
                }
                qtyDiff = qtyCheck - qtyWH;
                txtQty_Differrent.setText(String.valueOf(qtyDiff));
            }
        });

    }

    public void initData(DrawerItem item, String sheetAID, Runnable callBack) {
        this.selectedDrawerItem = item;
        this.sheetAID = sheetAID;
        this.CallBack = callBack;
    }

    // ===== Events =====
    @FXML
    private void onSave() {
        try {
            Account accountFromState = AppState.getInstance().get("Account", Account.class);
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            // String productAID = dbCRUDHelper.returnAID("Product", "ProductAID",
            // "ProductID",
            // txtProductID.getText().trim());
            String remak = "";
            if (txtRemarkOfHistory.getText() != null) {
                remak = txtRemarkOfHistory.getText().trim();
            }
            List<Object> values = Arrays.asList(sheetAID, productAID, txtQty_WareHouse.getText().trim(),
                    txtQty_Check.getText().trim(), txtQty_Differrent.getText().trim(), remak,
                    accountFromState.getUserName(),
                    timestamp);
            List<String> columnsSheet = new ArrayList<>(ArrayCRUD.sheetDataColumns);
            columnsSheet.remove("CheckAID");
            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            System.out.println(selectedItemFromState.getWareHouseCheckDataBase());

            int row = dbCRUDHelper.insert(selectedItemFromState.getWareHouseCheckDataBase(), columnsSheet, values);
            if (row > 0) {
                customDialogNotification.showDialog("Thành công", "Tạo phiếu kiểm kho thành công",
                        Alert.AlertType.INFORMATION);
                if (CallBack != null) {
                    CallBack.run();
                }
                onCloseClick();
            } else {
                customDialogNotification.showDialog("Thành công", "Tạo phiếu kiểm kho thất bại",
                        Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @FXML
    private void onCloseClick() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
