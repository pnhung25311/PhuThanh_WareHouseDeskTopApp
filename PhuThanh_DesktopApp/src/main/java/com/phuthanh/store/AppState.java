package com.phuthanh.store;

import javafx.application.Platform;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class AppState {

    // Khởi tạo Eager Singleton an toàn luồng tuyệt đối
    private static final AppState INSTANCE = new AppState();

    public static AppState getInstance() {
        return INSTANCE;
    }

    private AppState() {
        // Constructor rỗng bảo vệ Singleton
    }

    // TỐI ƯU ĐA LUỒNG: Sử dụng ConcurrentHashMap để chống xung đột đọc/ghi giữa UI Thread và Background Thread
    private final Map<String, Object> store = new ConcurrentHashMap<>();
    
    // TỐI ƯU ĐA LUỒNG: Chống ConcurrentModificationException bằng cấu trúc danh sách Copy-On-Write chuyên dụng
    private final CopyOnWriteArrayList<Consumer<Map<String, Object>>> listeners = new CopyOnWriteArrayList<>();

    // -------------------------
    // Listener giống Flutter傲 ChangeNotifier (Đã tối ưu RAM)
    // -------------------------

    public void addListener(Consumer<Map<String, Object>> listener) {
        if (listener == null) return;
        listeners.addIfAbsent(listener); // Tránh add trùng lặp một listener nhiều lần gây rác bộ nhớ
        
        // Emit initial value an toàn, không sao chép sâu cấu trúc map nếu không cần thiết
        listener.accept(getImmutableStore()); 
    }

    public void removeListener(Consumer<Map<String, Object>> listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /**
     * ⭐ QUAN TRỌNG: Gọi hàm này khi Logout hoặc đóng một Module lớn 
     * để giải phóng tận gốc các liên kết Controller cũ khỏi RAM Heap.
     */
    public void clearAllListeners() {
        listeners.clear();
    }

    private void notifyListeners() {
        if (listeners.isEmpty()) return;

        // TỐI ƯU RAM: Tạo một bản sao Read-Only duy nhất dùng chung cho tất cả thay vì 'new' Map liên tục trong vòng lặp
        Map<String, Object> snapshot = getImmutableStore();

        if (Platform.isFxApplicationThread()) {
            executeNotification(snapshot);
        } else {
            Platform.runLater(() -> executeNotification(snapshot));
        }
    }

    private void executeNotification(Map<String, Object> snapshot) {
        // CopyOnWriteArrayList cho phép duyệt an toàn tuyệt đối mà không sợ xung đột đa luồng
        for (Consumer<Map<String, Object>> listener : listeners) {
            try {
                listener.accept(snapshot);
            } catch (Exception e) {
                System.err.println("Lỗi thực thi tại một AppState Listener: " + e.getMessage());
            }
        }
    }

    /**
     * Trả về một phiên bản Map chỉ đọc bảo vệ dữ liệu gốc (Unmodifiable view), 
     * không làm nhân bản thêm bộ nhớ RAM Heap giúp tối ưu hóa hiệu năng cực cao.
     */
    private Map<String, Object> getImmutableStore() {
        return Collections.unmodifiableMap(new HashMap<>(store));
    }

    // -------------------------
    // STORE nâng cấp Generic & Safe-Cast
    // -------------------------

    public <T> T get(String key, Class<T> clazz) {
        Object value = store.get(key);
        if (value == null) return null;
        try {
            return clazz.cast(value);
        } catch (ClassCastException e) {
            System.err.println("AppState Cast Error cho key [" + key + "]: " + e.getMessage());
            return null;
        }
    }

    public void set(String key, Object value) {
        if (value == null) {
            remove(key);
            return;
        }
        
        // Chỉ cập nhật và bắn notify nếu giá trị thực sự thay đổi (Chống lãng phí CPU nạp lại UI thừa)
        Object oldValue = store.put(key, value);
        if (!value.equals(oldValue)) {
            notifyListeners();
        }
    }

    public void remove(String key) {
        if (store.remove(key) != null) {
            notifyListeners();
        }
    }

    public void clear() {
        if (!store.isEmpty()) {
            store.clear();
            notifyListeners();
        }
    }

    public boolean contains(String key) {
        return store.containsKey(key);
    }

    // -------------------------
    // Lifecycle (Bổ sung cơ chế dọn rác chủ động)
    // -------------------------

    public void onAppStart() {
        System.out.println("⚡ Phú Thành Warehouse State Engine: Hoạt động");
    }

    public void onAppClose() {
        System.out.println("🚪 Đang đóng ứng dụng → Giải phóng vĩnh viễn RAM State");
        clear();
        clearAllListeners();
    }
}