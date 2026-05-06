package com.phuthanh.business.screen.user;

// import com.phuthanh.business.controller.HomeBusinessController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeBusiness {
    public void show(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxmlBusiness/businessHome.fxml"));
            Parent root = loader.load();
            // HomeBusinessController homeController = loader.getController();

            Scene scene = new Scene(root);
            stage.setTitle("Hệ thống kinh doanh");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
