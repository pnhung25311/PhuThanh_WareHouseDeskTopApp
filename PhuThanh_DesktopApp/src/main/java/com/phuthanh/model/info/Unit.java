package com.phuthanh.model.info;

public class Unit {
    private int UnitID;
    private String Name;


    public Unit(int unitID, String name) {
        UnitID = unitID;
        Name = name;
    }


    // Giúp ComboBox hiển thị tên quốc gia
    @Override
    public String toString() {
        return Name;
    }


    public int getUnitID() {
        return UnitID;
    }


    public void setUnitID(int unitID) {
        UnitID = unitID;
    }


    public String getName() {
        return Name;
    }


    public void setName(String name) {
        Name = name;
    }
}
