package com.phuthanh.model.info;

public class Manufacturer {
    private int ManufacturerID;
    private String Name;    
    public Manufacturer(int manufacturerID, String name) {
        ManufacturerID = manufacturerID;
        Name = name;
    }
    public int getManufacturerID() {
        return ManufacturerID;
    }
    public void setManufacturerID(int manufacturerID) {
        ManufacturerID = manufacturerID;
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
