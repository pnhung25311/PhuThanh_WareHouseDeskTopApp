package com.phuthanh.warehouse.screen.user;

import com.phuthanh.warehouse.controller.HomeController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeScreen {
    public void show(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/home.fxml"));
            Parent root = loader.load();
            HomeController homeController = loader.getController();

            Scene scene = new Scene(root);
            stage.setTitle("Hệ thống quản lý kho");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setResizable(true);


            // 🔥 Khi stage đóng, tắt CartWatcher
            stage.setOnCloseRequest(e -> {
                if (homeController != null) {
                    homeController.shutdownCartWatcher();
                    System.out.println("Cart watcher stopped on app close.");
                }
            });

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
