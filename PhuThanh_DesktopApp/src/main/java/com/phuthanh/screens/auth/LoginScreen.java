package com.phuthanh.screens.auth;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginScreen {

    public void show(Stage stage) {
        try {
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setTitle("Đăng nhập");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
