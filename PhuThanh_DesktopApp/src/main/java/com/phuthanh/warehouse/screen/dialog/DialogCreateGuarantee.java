package com.phuthanh.warehouse.screen.dialog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.sql.Timestamp;

import com.phuthanh.custom.CustomCombobox;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.info.Supplier;
import com.phuthanh.model.warehouse.Guarantee;
import com.phuthanh.model.warehouse.Product;
import com.phuthanh.network.ApiClient;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class DialogCreateGuarantee {
    @FXML
    private Button btnSave, btnCancel;
    @FXML
    private TextArea txtRemark, txtReason;
    @FXML
    private TextField txtProductBroken, txtPartNoBroken, txtNameBroken,
            txtProductGuarantee, txtPartNoGuarantee, txtNameGuarantee,
            txtTimeUsage, txtQty;
    @FXML
    private DatePicker txtTimeStart, txtTimeBroken, txtTimeGuarantee;
    @FXML
    private TextField txtImg1, txtImg2, txtImg3;
    @FXML
    private Button btnImg1, btnImg2, btnImg3;
    @FXML
    private ComboBox<Supplier> SupplierGuarantee;

    private Runnable callBack;
    private boolean isEditMode = false;
    private String CodeAID;

    private String img1, img2, img3, supplierGuaranteeID;
    String img1Url = null, img2Url = null, img3Url = null;
    private   final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private   final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private   final FunctionHelper functionHelper = new FunctionHelper();
    private   final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private   final CustomCombobox customCombobox = new CustomCombobox();
    private final ArrayCRUD arrayCRUD = new ArrayCRUD();


    public void initialize() {
        // Khởi tạo logic nếu cần
        loadComboBox();
        txtTimeStart.valueProperty().addListener((obs, old, newVal) -> calculateTimeUsage());
        txtTimeBroken.valueProperty().addListener((obs, old, newVal) -> calculateTimeUsage());
        txtProductBroken.textProperty().addListener((obs, old, newVal) -> {
            loadDataProduct(newVal, txtPartNoBroken, txtNameBroken);
        });
        txtProductGuarantee.textProperty().addListener((obs, old, newVal) -> {
            loadDataProduct(newVal, txtPartNoGuarantee, txtNameGuarantee);
        });

    }

    public void setInitialData(Runnable cb, boolean iseditMode, String codeAID) {
        this.callBack = cb;
        this.isEditMode = iseditMode;
        this.CodeAID = codeAID;

        if (CodeAID != null && !CodeAID.isEmpty()) {
            loadData();
        } 
    }

    @FXML
    private void onSave(ActionEvent event) {

        try {

            String guaranteeID = functionHelper.generateCodeBH();
            String productBrokenID = safeTrim(txtProductBroken);
            String productGuaranteeID = safeTrim(txtProductGuarantee);
            String qty = safeTrim(txtQty);
            String reason = safeTrim(txtReason);
            String remark = safeTrim(txtRemark);
            String timeUsage = safeTrim(txtTimeUsage);

            if (productBrokenID.isEmpty()) {
                customDialogNotification.showDialog("Lỗi", "Chưa nhập mã sản phẩm hỏng", Alert.AlertType.ERROR);
                return;
            }

            if (txtTimeStart.getValue() == null) {
                customDialogNotification.showDialog("Lỗi", "Chưa chọn ngày bắt đầu", Alert.AlertType.ERROR);
                return;
            }

            if (txtTimeBroken.getValue() == null) {
                customDialogNotification.showDialog("Lỗi", "Chưa chọn ngày hỏng", Alert.AlertType.ERROR);
                return;
            }

            ApiClient apiClient = new ApiClient();

            if (img1 != null && !img1.isEmpty()) {
                File file1 = new File(img1);
                img1Url = apiClient.postFile("upload-guarantee/" + guaranteeID, file1, null, "file");
            }

            if (img2 != null && !img2.isEmpty()) {
                File file2 = new File(img2);
                img2Url = apiClient.postFile("upload-guarantee/" + guaranteeID, file2, null, "file");
            }

            if (img3 != null && !img3.isEmpty()) {
                File file3 = new File(img3);
                img3Url = apiClient.postFile("upload-guarantee/" + guaranteeID, file3, null, "file");
            }

            Account accountFromState = AppState.getInstance().get("Account", Account.class);

            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());

            List<String> columnsGuarantee = new ArrayList<>(arrayCRUD.guaranteeColumns);
            columnsGuarantee.remove("GuaranteeAID");

            String productBroken = dbCRUDHelper.returnAID(
                    "Product",
                    "ProductAID",
                    "ProductID",
                    productBrokenID);

            String productGuarantee = dbCRUDHelper.returnAID(
                    "Product",
                    "ProductAID",
                    "ProductID",
                    productGuaranteeID);

            String supplierID = null;

            if (SupplierGuarantee.getSelectionModel().getSelectedItem() != null) {
                supplierID = SupplierGuarantee.getSelectionModel().getSelectedItem().getSupplierID() + "";
            } else {
                supplierID = supplierGuaranteeID;
            }

            List<Object> values = Arrays.asList(
                    guaranteeID,
                    productBroken,
                    txtTimeStart.getValue(),
                    txtTimeBroken.getValue(),
                    timeUsage,
                    reason,
                    productGuarantee,
                    txtTimeGuarantee.getValue(),
                    supplierID,
                    qty,
                    img1Url,
                    img2Url,
                    img3Url,
                    remark,
                    accountFromState.getUserName(),
                    timestamp);

            if (isEditMode) {

                int rows = dbCRUDHelper.update(
                        "Guarantee",
                        columnsGuarantee,
                        values,
                        "GuaranteeAID = ?",
                        Arrays.asList(CodeAID));

                if (rows > 0) {

                    customDialogNotification.showDialog(
                            "Thành công",
                            "Cập nhật phiếu bảo hành thành công",
                            Alert.AlertType.INFORMATION);

                    if (callBack != null)
                        callBack.run();

                    onCloseClick(event);
                }

            } else {

                int row = dbCRUDHelper.insert(
                        "Guarantee",
                        columnsGuarantee,
                        values);

                if (row > 0) {

                    customDialogNotification.showDialog(
                            "Thành công",
                            "Tạo mới phiếu bảo hành thành công",
                            Alert.AlertType.INFORMATION);

                    if (callBack != null)
                        callBack.run();

                    onCloseClick(event);
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            customDialogNotification.showDialog(
                    "Lỗi",
                    "Tạo phiếu bảo hành thất bại: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onCloseClick(ActionEvent event) {
        // Xử lý sự kiện đóng dialog
        btnCancel.getScene().getWindow().hide();
    }

    @FXML
    private void onBtnImg1() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh 1");
        File file = fileChooser.showOpenDialog(txtImg1.getScene().getWindow());
        if (file != null) {
            this.img1 = file.getAbsolutePath();
            txtImg1.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void onBtnImg2() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh 2");
        File file = fileChooser.showOpenDialog(txtImg2.getScene().getWindow());
        if (file != null) {
            this.img2 = file.getAbsolutePath();
            txtImg2.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void onBtnImg3() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh 3");
        File file = fileChooser.showOpenDialog(txtImg3.getScene().getWindow());
        if (file != null) {
            this.img3 = file.getAbsolutePath();
            txtImg3.setText(file.getAbsolutePath());
        }
    }

    private void calculateTimeUsage() {
        if (txtTimeStart.getValue() == null || txtTimeBroken.getValue() == null) {
            txtTimeUsage.setText("");
            return;
        }

        LocalDate start = txtTimeStart.getValue();
        LocalDate broken = txtTimeBroken.getValue();

        if (broken.isBefore(start)) {
            txtTimeUsage.setText("Ngày hỏng < ngày bắt đầu");
            txtTimeUsage.setStyle("-fx-text-fill: red;");
            return;
        }

        long days = ChronoUnit.DAYS.between(start, broken);
        txtTimeUsage.setText(String.valueOf(days));
        txtTimeUsage.setStyle("");
    }

    private void loadDataProduct(String productID, TextField txtPartNo, TextField txtName) {
        Product product = dbInfoHelper.getProductByID(productID);
        if (product != null) {
            txtPartNo.setText(product.getID_PartNo());
            txtName.setText(product.getNameProduct());
        } else {
            txtPartNo.setText("");
            txtName.setText("");
        }
    }

    public void loadData() {
        Guarantee model = dbInfoHelper.getGuaranteeByAID(CodeAID);
        if (model == null)
            return;

        // ===== Product Broken =====
        txtProductBroken.setText(model.getProductIDBroken());
        txtPartNoBroken.setText(model.getIdPartNoBroken());
        txtNameBroken.setText(model.getNameProductBroken());

        // ===== Time =====
        if (model.getTimeStart() != null) {
            txtTimeStart.setValue(model.getTimeStart().toLocalDate());
        }

        if (model.getTimeBroken() != null) {
            txtTimeBroken.setValue(model.getTimeBroken().toLocalDate());
        }
        if (model.getTimeGuarantee() != null) {
            txtTimeGuarantee.setValue(model.getTimeGuarantee().toLocalDate());
        }

        txtTimeUsage.setText(String.valueOf(model.getTimeUsage()));
        txtQty.setText(String.valueOf(model.getQty()));

        // ===== Reason =====
        txtReason.setText(model.getReasonBroken());

        txtImg1.setText(model.getImg1());
        txtImg2.setText(model.getImg2());
        txtImg3.setText(model.getImg3());

        // ===== Product Guarantee =====
        txtProductGuarantee.setText(model.getProductIDGuarantee());
        txtPartNoGuarantee.setText(model.getIdPartNoGuarantee());
        txtNameGuarantee.setText(model.getNameProductGuarantee());
        // txtGuaranteeID.setText(model.getGuaranteeID());

        // ===== Remark =====
        txtRemark.setText(model.getRemark());

        functionHelper.selectComboBoxItemById(SupplierGuarantee, model.getSupplierGuarantee(), Supplier::getSupplierID);
        supplierGuaranteeID = model.getSupplierGuarantee() + "";

    }

    private void loadComboBox() {
        List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
        customCombobox.setupComboBox(SupplierGuarantee, suppliers, Supplier::getSupplierID, Supplier::getName);
    }

    private String safeTrim(TextField txt) {
        return txt == null || txt.getText() == null ? "" : txt.getText().trim();
    }

    private String safeTrim(TextArea txt) {
        return txt == null || txt.getText() == null ? "" : txt.getText().trim();
    }

}
