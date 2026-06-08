package com.phuthanh.controller.auth;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.AppWindowManager;
import com.phuthanh.helper.AuthHelper;
import com.phuthanh.helper.update.AppUpdateManager;
import com.phuthanh.store.AppSession;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class LoginController {

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnLogin;
    @FXML
    private ComboBox<String> cbSystem;
    @FXML
    private ImageView logoapp;
    @FXML
    private Label lblVersion;

    private String systemVersion;

    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private final AppWindowManager appWindowManager = new AppWindowManager();

    @FXML
    private void initialize() {
        btnLogin.setDefaultButton(true);

        cbSystem.getItems().addAll(
                "Hệ thống kho",
                "Hệ thống kinh doanh");

        cbSystem.getSelectionModel().selectFirst();
        logoapp.setImage(new Image(getClass().getResourceAsStream("/images/logoDesktop.png")));

        systemVersion = "2026.06.06.01";
        lblVersion.setText("Phiên bản " + systemVersion);

        // 🔥 GỌI LỚP QUẢN LÝ UPDATE RIÊNG BIỆT TẠI ĐÂY

        AppUpdateManager updateManager = new AppUpdateManager(systemVersion, btnLogin);
        updateManager.checkUpdate();
    }

    @FXML
    private void onLoginClick() {
        String systemName = cbSystem.getValue();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (systemName == null || systemName.isEmpty()) {
            showDialog(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn hệ thống!");
            return;
        }

        if (username.isEmpty() || password.isEmpty()) {
            showDialog(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        AuthHelper authHelper = new AuthHelper();
        boolean success = authHelper.login(username, password);

        if (!success) {
            showDialog(Alert.AlertType.ERROR, "Thất bại", "Sai tên đăng nhập hoặc mật khẩu!");
            return;
        }

        String systemCode = mapSystemCode(systemName);
        AppSession.getInstance().setUsername(username);
        appWindowManager.openSystem(systemCode);

        showDialog(Alert.AlertType.INFORMATION, "Thành công", "Đăng nhập thành công vào " + systemName + "!");
    }

    @FXML
    private void onClickLabel() {
        boolean checkVersion = customDialogNotification.showDialogConfirm("Kiểm tra phiên bản", null,
                "Bạn có muốn cập nhật không?", "Có", "Không");
        if (checkVersion) {
            AppUpdateManager updateManager = new AppUpdateManager(systemVersion + "1", btnLogin);
            updateManager.checkUpdate();
        }
    }

    private String mapSystemCode(String systemName) {
        switch (systemName) {
            case "Hệ thống kho":
                return "WAREHOUSE";
            case "Hệ thống kinh doanh":
                return "BUSINESS";
            default:
                return "";
        }
    }

    private void showDialog(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}