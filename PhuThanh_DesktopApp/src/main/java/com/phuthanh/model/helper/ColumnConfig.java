package com.phuthanh.model.helper;

public class ColumnConfig {
    private String id; // Ví dụ: BusinessName
    private String label; // Ví dụ: Tài sản thuộc đơn vị nào
    private boolean visible = true; // Mặc định là hiện

    public ColumnConfig(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    } // Hiển thị trên ListView

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
