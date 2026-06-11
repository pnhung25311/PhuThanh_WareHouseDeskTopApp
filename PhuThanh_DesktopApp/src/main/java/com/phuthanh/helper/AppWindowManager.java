package com.phuthanh.helper;

import com.phuthanh.business.screen.user.HomeBusiness;
import com.phuthanh.warehouse.screen.user.HomeScreen;
import com.phuthanh.store.AppSession;

import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AppWindowManager {

    public void openSystem(String systemCode, Stage stage) {

        Stage existingStage = AppSession.getInstance().getStage(systemCode);

        // ✅ Nếu đã mở → đưa lên trước
        if (existingStage != null) {
            existingStage.toFront();
            existingStage.requestFocus();
            return;
        }

        // ❗ Chưa mở → tạo cửa sổ mới
        Stage newStage = new Stage();
        Image icon = new Image(
                getClass().getResourceAsStream("/images/logo.png"));

        System.out.println(icon.getWidth() + " x " + icon.getHeight());

        stage.getIcons().add(icon);
        if (systemCode.equals("WAREHOUSE")) {
            new HomeScreen().show(newStage);
        } else if (systemCode.equals("BUSINESS")) {
            new HomeBusiness().show(newStage);
        }

        // ⭐ QUAN TRỌNG: khi user bấm X → xoá khỏi session
        newStage.setOnHidden(e -> {
            System.out.println("Window closed: " + systemCode);
            AppSession.getInstance().removeStage(systemCode);
        });

        // lưu lại để lần sau dùng tiếp
        AppSession.getInstance().saveStage(systemCode, newStage);
    }

}