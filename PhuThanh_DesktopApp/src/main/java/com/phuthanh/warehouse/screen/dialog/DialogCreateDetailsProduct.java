package com.phuthanh.warehouse.screen.dialog;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.model.warehouse.PartNo;
import com.phuthanh.utils.ArrayCRUD;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class DialogCreateDetailsProduct {

    // ===== TextField =====
    @FXML
    private TextField txtProductID;

    @FXML
    private TextField txtNameEnglish;

    @FXML
    private TextField txtPartNoID;

    @FXML
    private TextField txtNameVietnamese;

    @FXML
    private TextField txtID_PartNo, txtParameter;

    @FXML
    private TextField txtPartNoQty;

    // ===== TextArea =====
    @FXML
    private TextArea txtRemark;

    // ===== Button =====
    @FXML
    private Button btnSave;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnDelete;

    @FXML
    private Button btnCancel;
    private String partNoAID;
    private boolean isEditMode = false;
    private static final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private static final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private static final FunctionHelper functionHelper = new FunctionHelper();
    private static final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    // ===== Initialize =====
    @FXML
    private void initialize() {
        // Test ánh xạ
        System.out.println("Controller loaded OK");
        functionHelper.allowOnlyNumber(txtPartNoQty);

    }

    // ===== Events =====
    @FXML
    private void onSave() {
        try {
            System.out.println("Save clicked");
            List<String> columnsDetailProduct = new ArrayList<>(ArrayCRUD.detailsProduct);
            columnsDetailProduct.remove("PartNoAID");
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            String remark = "";
            String productID = "";
            String nameVietnamese = "";
            String nameEnglish = "";
            String partNoQty = "";
            String partNoID = "";
            String idpartNo = "";
            String parameter = "";
            if (txtRemark != null && txtRemark.getText() != null) {
                remark = txtRemark.getText().trim();
            }
            if (txtNameVietnamese != null && txtNameVietnamese.getText() != null) {
                nameVietnamese = txtNameVietnamese.getText().trim();
            }
            if (txtNameEnglish != null && txtNameEnglish.getText() != null) {
                nameEnglish = txtNameEnglish.getText().trim();
            }
            if (txtPartNoQty != null && txtPartNoQty.getText() != null) {
                partNoQty = txtPartNoQty.getText().trim();
            }
            if (txtPartNoID != null && txtPartNoID.getText() != null) {
                partNoID = txtPartNoID.getText().trim();
            }
            if (txtID_PartNo != null && txtID_PartNo.getText() != null) {
                idpartNo = txtID_PartNo.getText().trim();
            }
            if (txtParameter != null && txtParameter.getText() != null) {
                parameter = txtParameter.getText().trim();
            }
                        if (txtProductID != null && txtProductID.getText() != null) {
                productID = txtProductID.getText().trim();
            }
            List<Object> values = Arrays.asList(
                    productID, idpartNo, partNoID,
                    nameEnglish, nameVietnamese,
                    partNoQty, parameter, remark, timestamp);
            if (isEditMode) {
                int row = dbCRUDHelper.update("DetailsProduct", columnsDetailProduct, values, "PartNoAID = ?",
                        Arrays.asList(partNoAID));
                if (row > 0) {
                    customDialogNotification.showDialog("Thành công", "Cập nhật chi tiết sản phẩm thành công",
                            Alert.AlertType.INFORMATION);
                    if (onReload != null) {
                        onReload.run();
                        btnCancel.getScene().getWindow().hide();
                    }
                } else {
                    customDialogNotification.showDialog("Lỗi", "Cập nhật chi tiết sản phẩm thất bại",
                            Alert.AlertType.ERROR);
                }
            } else {
                boolean checkDetailsProduct = dbCRUDHelper.isCheck(txtPartNoID.getText().trim(), "DetailsProduct",
                        "PartNoID = ?");

                if (checkDetailsProduct) {
                    System.out.println("Chi tiết sản phẩm đã tồn tại!");
                    customDialogNotification.showDialog("Lỗi", "Mã đã tồn tại", Alert.AlertType.ERROR);
                    return;
                } else {
                    int row = dbCRUDHelper.insert("DetailsProduct", columnsDetailProduct, values);
                    if (row > 0) {
                        customDialogNotification.showDialog("Thành công", "Tạo mới chi tiết sản phẩm thành công",
                                Alert.AlertType.INFORMATION);
                        if (onReload != null) {
                            onReload.run();
                            btnCancel.getScene().getWindow().hide();
                        }
                    } else {
                        customDialogNotification.showDialog("Lỗi", "Tạo mới chi tiết sản phẩm thất bại",
                                Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    @FXML
    private void onCloseClick() {
        btnCancel.getScene().getWindow().hide();
    }

    private Runnable onReload;

    public void setCodeAID(String codeAID) {
        System.out.println("Received Product ID: " + codeAID);
        initData(codeAID);
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    public void initData(String codeAID) {
        this.partNoAID = codeAID;
        System.out.println("Init Product ID: " + partNoAID);
        if (partNoAID != null && !partNoAID.isEmpty()) {
            loadItem();
        }
    }

    public void setOnCreateSuccess(Runnable callback) {
        this.onReload = callback;
    }

    private void loadItem() {
        try {
            PartNo partNo = dbInfoHelper.getPartNoByAID(partNoAID);
            String productid = dbCRUDHelper.returnAID("vwDetailsProduct", "ProductID", "PartNoAID", partNoAID);
            if (partNo != null) {
                txtProductID.setText(productid);
                txtPartNoID.setText(partNo.getPartNoID());
                txtNameEnglish.setText(partNo.getNameEnglish());
                txtNameVietnamese.setText(partNo.getNameVietNamese());
                txtPartNoQty.setText(String.valueOf(partNo.getPartNoQty()));
                txtRemark.setText(partNo.getRemark());
                txtID_PartNo.setText(partNo.getIdpartNo());
                txtParameter.setText(partNo.getParameter());
            }
        } catch (Exception e) {
            // TODO: handle exception
            System.err.println("Error loading PartNo: " + e.getMessage());
        }

    }
}
