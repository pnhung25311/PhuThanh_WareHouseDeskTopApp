package com.phuthanh;

import java.util.Locale;

import com.phuthanh.screens.auth.LoginScreen;
import com.phuthanh.store.AppState;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage primaryStage;
    private static HostServices hostServices;

    @Override
    public void start(Stage stage) {
        Locale.setDefault(Locale.of("vi", "VN")); // ✨ Chuyển toàn bộ UI sang tiếng Việt
        AppState.getInstance().onAppStart();
        primaryStage = stage;
        hostServices = getHostServices();
        Image icon = new Image(
                getClass().getResourceAsStream("/images/logo.png"));

        System.out.println(icon.getWidth() + " x " + icon.getHeight());

        stage.getIcons().add(icon);
        stage.setTitle("Test");

        stage.setOnCloseRequest(e -> {
            AppState.getInstance().onAppClose();
        });
        new LoginScreen().show(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getPrimaryStage() {
        return primaryStage; // <--- Hàm để lấy primaryStage
    }

    public static HostServices getHostServicesInstance() {
        return hostServices;
    }
}
