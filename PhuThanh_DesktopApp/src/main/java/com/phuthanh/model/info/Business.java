package com.phuthanh.model.info;

public class Business {
    private int BusinessID;
    private String Business;
    private String Name;

    public Business(int businessID, String business, String name) {
        BusinessID = businessID;
        Business = business;
        Name = name;
    }

    public Business(int businessID, String name) {
        BusinessID = businessID;
        Name = name;
    }

    public Business() {
    }

    public int getBusinessID() {
        return BusinessID;
    }

    public void setBusinessID(int businessID) {
        BusinessID = businessID;
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

    public String getBusiness() {
        return Business;
    }

    public void setBusiness(String business) {
        Business = business;
    }
}
