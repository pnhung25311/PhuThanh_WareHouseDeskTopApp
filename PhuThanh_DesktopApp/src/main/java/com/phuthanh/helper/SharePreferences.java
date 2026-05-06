package com.phuthanh.helper;
import java.util.prefs.Preferences;


public class SharePreferences {
    
    // Preferences lưu cài đặt của ứng dụng
    private final Preferences prefs = Preferences.userRoot().node("MyAppSettings");

    // ----------------------
    // Lưu cài đặt dạng key-value
    // ----------------------
    public void saveSetting(String key, String value) {
        prefs.put(key, value);
    }

    public void saveSetting(String key, int value) {
        prefs.putInt(key, value);
    }

    public void saveSetting(String key, boolean value) {
        prefs.putBoolean(key, value);
    }

    // ----------------------
    // Lấy cài đặt
    // ----------------------
    public String getString(String key, String defaultValue) {
        return prefs.get(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public  boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    // ----------------------
    // Xóa cài đặt
    // ----------------------
    public void remove(String key) {
        prefs.remove(key);
    }

    // ----------------------
    // Reset tất cả cài đặt về mặc định
    // ----------------------
    public void resetAll() {
        try {
            prefs.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
