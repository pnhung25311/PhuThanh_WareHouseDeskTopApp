package com.phuthanh;

import java.util.Locale;

import com.phuthanh.screens.auth.LoginScreen;
import com.phuthanh.store.AppState;
// import com.phuthanh.test.ComboScreenApp;
// import com.phuthanh.test.EditableTableView;

import javafx.application.Application;
import javafx.application.HostServices;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.layout.VBox;
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

        stage.setOnCloseRequest(e -> {
            AppState.getInstance().onAppClose();
        });
        new LoginScreen().show(stage);
        // new HomeScreen().show(stage);
        // new ComboScreenApp().show();;

        // EditableTableView tableComponent = new EditableTableView();

        // Button addBtn = new Button("➕ Add Row");
        // addBtn.setOnAction(e -> tableComponent.addNewRow());

        // VBox root = new VBox(10, addBtn, tableComponent.getTable());

        // Scene scene = new Scene(root);
        // stage.setTitle("JavaFX Editable Table Demo");
        // stage.setScene(scene);
        //     stage.setMaximized(true);

        // stage.show();
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
