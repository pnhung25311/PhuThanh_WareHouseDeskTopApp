package com.phuthanh.controller.auth;

import com.phuthanh.helper.AppWindowManager;
import com.phuthanh.helper.AuthHelper;
import com.phuthanh.store.AppSession;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

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
    private void initialize() {
        btnLogin.setDefaultButton(true);

        cbSystem.getItems().addAll(
                "Hệ thống kho",
                "Hệ thống kinh doanh");

        cbSystem.getSelectionModel().selectFirst(); // chọn mặc định
        logoapp.setImage(
                new Image(getClass().getResourceAsStream("/images/logoDesktop.png")));
    }

    @FXML
    private void onLoginClick() {

        String systemName = cbSystem.getValue();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // ===== validate =====
        if (systemName == null || systemName.isEmpty()) {
            showDialog(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn hệ thống!");
            return;
        }

        if (username.isEmpty() || password.isEmpty()) {
            showDialog(Alert.AlertType.WARNING, "Thiếu thông tin",
                    "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        // ===== login =====
        AuthHelper authHelper = new AuthHelper();
        boolean success = authHelper.login(username, password);

        if (!success) {
            showDialog(Alert.AlertType.ERROR, "Thất bại",
                    "Sai tên đăng nhập hoặc mật khẩu!");
            return;
        }

        // ===== map hệ thống =====
        String systemCode = mapSystemCode(systemName);

        // 🔥 lưu session user
        AppSession.getInstance().setUsername(username);

        // 🔥 mở / focus hệ thống (KHÔNG đóng login)
        AppWindowManager.openSystem(systemCode);

        showDialog(Alert.AlertType.INFORMATION, "Thành công",
                "Đăng nhập thành công vào " + systemName + "!");
    }

    // map tên hiển thị -> code hệ thống
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