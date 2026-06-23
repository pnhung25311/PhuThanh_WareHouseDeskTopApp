package com.phuthanh.store;

import java.util.HashMap;
import java.util.Map;
import javafx.stage.Stage;

public class AppSession {
    
    private String username;
    
    // TỐI ƯU RAM: Lưu trữ các cửa sổ hệ thống con
    private final Map<String, Stage> systemStages = new HashMap<>();

    // Hạn chế khởi tạo từ bên ngoài
    private AppSession() {}

    /**
     * TỐI ƯU ĐA LUỒNG (Thread-Safe & Lazy Loading):
     * Sử dụng Holder lớp nội tĩnh giúp Singleton khởi tạo an toàn, cực nhẹ và không gây tốn RAM chờ.
     */
    private static class InstanceHolder {
        private static final AppSession INSTANCE = new AppSession();
    }

    public static AppSession getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /* ================= GETTER / SETTER ================= */

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
        if (stage == null) return;
        systemStages.put(systemCode, stage);
    }

    /** * ⭐ TỐI ƯU RAM: Gỡ Stage và chặt đứt liên kết đồ họa 
     * Đảm bảo Garbage Collector có thể thu hồi toàn bộ FXML layout của Stage đó ngay lập tức.
     */
    public void removeStage(String systemCode) {
        Stage stage = systemStages.remove(systemCode);
        releaseStageMemory(stage);
    }

    /** * ⭐ TỐI ƯU RAM & CLEAN CODE: Đóng và hủy toàn bộ giao diện khi Đăng xuất (Logout)
     */
    public void clearSession() {
        this.username = null; // Xóa thông tin tài khoản khỏi bộ nhớ đệm
        
        for (Stage stage : systemStages.values()) {
            if (stage != null) {
                stage.close();
                releaseStageMemory(stage);
            }
        }
        systemStages.clear();
        
        // Gợi ý cho JVM dọn rác RAM ngay lập tức sau khi xóa toàn bộ cây đồ họa nặng
        System.gc();
    }

    /**
     * HÀM PHỤ TRỢ TỐI ƯU BỘ NHỚ: 
     * Ép bẻ gãy Scene Graph của JavaFX để giải phóng RAM triệt để cho các Node giao diện ngầm bên trong.
     */
    private void releaseStageMemory(Stage stage) {
        if (stage != null) {
            try {
                // Chặt đứt liên kết giữa Stage và Scene chính (Giải phóng toàn bộ FXML Layout và dữ liệu TableView gắn kèm)
                stage.setScene(null); 
            } catch (Exception e) {
                // Tránh xung đột luồng nếu giao diện đang trong trạng thái đóng dở dang
            }
        }
    }
}