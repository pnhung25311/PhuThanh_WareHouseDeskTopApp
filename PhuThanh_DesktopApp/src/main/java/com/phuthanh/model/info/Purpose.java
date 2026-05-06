package com.phuthanh.model.info;

public class Purpose {
    private int PurposeID;
    private String Name;

    public Purpose(int purposeID, String name) {
        PurposeID = purposeID;
        Name = name;
    }

    public int getPurposeID() {
        return PurposeID;
    }

    public void setPurposeID(int purposeID) {
        PurposeID = purposeID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    // Giúp ComboBox hiển thị tên quốc gia
    @Override
    public String toString() {
        return Name;
    }
}
