package com.phuthanh.store;

import java.util.HashMap;
import java.util.Map;
import javafx.stage.Stage;

public class AppSession {
    private static  AppSession instance;

    private String username;

    // mỗi hệ thống có 1 cửa sổ riêng
    private Map<String, Stage> systemStages = new HashMap<>();

    public static  AppSession getInstance() {
        if (instance == null)
            instance = new AppSession();
        return instance;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public Stage getStage(String systemCode) {
        return systemStages.get(systemCode);
    }

    public void saveStage(String systemCode, Stage stage) {
        systemStages.put(systemCode, stage);
    }

    // ⭐ THÊM HÀM NÀY
    public void removeStage(String systemCode) {
        systemStages.remove(systemCode);
    }

    // ⭐ BONUS: đóng toàn bộ cửa sổ khi logout
    public void closeAllStages() {
        for (Stage stage : systemStages.values()) {
            if (stage != null) {
                stage.close();
            }
        }
        systemStages.clear();
    }
}