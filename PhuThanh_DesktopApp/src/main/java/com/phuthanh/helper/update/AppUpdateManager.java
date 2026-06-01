package com.phuthanh.helper.update;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Optional;

import org.json.JSONObject;
import com.phuthanh.network.ApiClient;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AppUpdateManager {

    private final String currentVersion;
    private final Button triggerButton;
    private Stage downloadStage;

    private   final String ORIGINAL_JAR = "PhuThanh_DesktopApp-0.0.1-SNAPSHOT.jar";
    private   final String TEMP_JAR = "PhuThanh_DesktopApp-0.0.1-SNAPSHOT.jar";

    // 👉 Folder cố định lưu update
    private   final String UPDATE_DIR = "C:\\PY";
    private   final String TEMP_JAR_PATH = UPDATE_DIR + "\\" + TEMP_JAR;

    public AppUpdateManager(String currentVersion, Button triggerButton) {
        this.currentVersion = currentVersion;
        this.triggerButton = triggerButton;
    }

    // ================= CHECK UPDATE =================
    public void checkUpdate() {
        new Thread(() -> {
            try {
                ApiClient apiClient = new ApiClient();
                String json = apiClient.get("update/info");
                System.out.println("Update info: " + json);

                if (json != null && !json.isEmpty()) {
                    JSONObject jsonObject = new JSONObject(json);
                    String latestVersion = jsonObject.getString("latestVersion");
                    String downloadUrl = jsonObject.getString("downloadUrl");
                    String changeLog = jsonObject.getString("changeLog");

                    if (!currentVersion.equals(latestVersion)) {
                        Platform.runLater(() -> showUpdateDialog(downloadUrl, latestVersion, changeLog));
                    }
                }
            } catch (Exception e) {
                System.out.println("Không thể kiểm tra cập nhật: " + e.getMessage());
            }
        }).start();
    }

    // ================= DIALOG HỎI UPDATE =================
    private void showUpdateDialog(String downloadUrl, String newVersion, String note) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UNDECORATED);

        alert.setTitle("Cập nhật phần mềm");
        alert.setHeaderText("Đã có phiên bản mới: " + newVersion);
        alert.setContentText("Nội dung cập nhật: " + note + "\n\nBạn có muốn cập nhật ngay bây giờ không?");

        ButtonType updateButton = new ButtonType("Cập nhật", ButtonBar.ButtonData.OK_DONE);

        alert.getButtonTypes().setAll(updateButton);
        // alert.setOnCloseRequest(event -> {
        //     event.consume(); // chặn đóng cửa sổ
        // });
        Optional<ButtonType> result = alert.showAndWait();


        if (result.isPresent() && result.get() == updateButton) {
            showDownloadingUI();

            new Thread(() -> downloadUpdate(downloadUrl)).start();
        }
    }

    // ================= TẢI FILE UPDATE =================
    private void downloadUpdate(String downloadUrl) {
        try {
            ensureUpdateFolder();

            // Xóa file update cũ nếu tồn tại
            java.io.File oldFile = new java.io.File(TEMP_JAR_PATH);
            if (oldFile.exists()) {
                System.out.println("Delete old update file...");
                oldFile.delete();
            }

            System.out.println("Downloading update to: " + TEMP_JAR_PATH);

            URI url = new URI(downloadUrl);
            try (BufferedInputStream in = new BufferedInputStream(url.toURL().openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(TEMP_JAR_PATH)) {

                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            }

            Platform.runLater(() -> {
                if (downloadStage != null)
                    downloadStage.close();
            });

            executeRestart();

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                if (downloadStage != null)
                    downloadStage.close();
                showDialog(Alert.AlertType.ERROR, "Lỗi cập nhật", "Tải file thất bại.");
            });
        }
    }

    // ================= TẠO FOLDER C:\PY =================
    private void ensureUpdateFolder() {
        java.io.File dir = new java.io.File(UPDATE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // ================= UI ĐANG DOWNLOAD =================
    private void showDownloadingUI() {
        downloadStage = new Stage();
        downloadStage.initModality(Modality.APPLICATION_MODAL);
        downloadStage.setTitle("Đang cập nhật");

        Label label = new Label("Vui lòng đợi, hệ thống đang tải phiên bản mới...");
        ProgressIndicator progressIndicator = new ProgressIndicator();

        VBox layout = new VBox(15);
        layout.setStyle("-fx-padding: 20px; -fx-background-color: #f4f4f4;");
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(label, progressIndicator);

        Scene scene = new Scene(layout, 350, 150);
        downloadStage.setScene(scene);
        downloadStage.setResizable(false);
        downloadStage.show();
    }

    // ================= RESTART & COPY FILE =================
    private void executeRestart() {
        try {
            String jarDir = new java.io.File(
                    AppUpdateManager.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .getParent();

            String originalJarPath = jarDir + "\\" + ORIGINAL_JAR;

            System.out.println("App folder: " + jarDir);
            System.out.println("Copy from: " + TEMP_JAR_PATH);

            // Script CMD CHỜ FILE UNLOCK rồi mới copy
            String command = "echo Updating app... && " +
                    "set jar=\"" + originalJarPath + "\" && " +
                    "set newjar=\"" + TEMP_JAR_PATH + "\" && " +

                    // LOOP chờ app tắt hoàn toàn
                    ":loop && " +
                    "timeout /t 1 >nul && " +
                    "del /f /q %jar% >nul 2>&1 && " +
                    "if exist %jar% goto loop && " +

                    // copy jar mới
                    "copy /y %newjar% %jar% >nul && " +

                    // chạy lại app
                    "start javaw -jar %jar%";

            new ProcessBuilder("cmd.exe", "/c", command).start();

            // tắt app hiện tại
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
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