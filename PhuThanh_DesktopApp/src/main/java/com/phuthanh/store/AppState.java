package com.phuthanh.store;

import javafx.application.Platform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AppState {

    private static final AppState instance = new AppState();

    public static  AppState getInstance() {
        return instance;
    }

    private AppState() {
    }

    private final Map<String, Object> store = new HashMap<>();
    private final List<Consumer<Map<String, Object>>> listeners = new ArrayList<>();

    // -------------------------
    // Listener giống Flutter ChangeNotifier
    // -------------------------

    public void addListener(Consumer<Map<String, Object>> listener) {
        listeners.add(listener);
        listener.accept(new HashMap<>(store)); // emit initial
    }

    public void removeListener(Consumer<Map<String, Object>> listener) {
        if (listeners != null && listener != null) {
            listeners.remove(listener);
        }
    }

    public void clearAllListeners() {
        listeners.clear();
    }

    private void notifyListeners() {
        // đảm bảo chạy trên JavaFX UI Thread
        if (Platform.isFxApplicationThread()) {
            for (var l : listeners) {
                l.accept(new HashMap<>(store));
            }
        } else {
            Platform.runLater(() -> {
                for (var l : listeners) {
                    l.accept(new HashMap<>(store));
                }
            });
        }
    }

    // -------------------------
    // STORE giống Flutter
    // -------------------------

    public <T> T get(String key, Class<T> clazz) {
        Object value = store.get(key);
        if (value == null)
            return null;
        return clazz.cast(value);
    }

    public void set(String key, Object value) {
        store.put(key, value);
        notifyListeners();
    }

    public void remove(String key) {
        if (store.containsKey(key)) {
            store.remove(key);
            notifyListeners();
        }
    }

    public void clear() {
        store.clear();
        notifyListeners();
    }

    public boolean contains(String key) {
        return store.containsKey(key);
    }

    // -------------------------
    // Giả lập Lifecycle giống Flutter
    // -------------------------

    /** Gọi khi app start */
    public void onAppStart() {
        System.out.println("App started");
    }

    /** Gọi khi app close */
    public void onAppClose() {
        System.out.println("App closing → clear store");
        clear();
    }
}
