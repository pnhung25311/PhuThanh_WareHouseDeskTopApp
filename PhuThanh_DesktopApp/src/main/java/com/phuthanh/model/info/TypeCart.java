package com.phuthanh.model.info;

public class TypeCart {
    private int TypeCartID;
    private String Name;

    public TypeCart(int typeCartID, String name) {
        TypeCartID = typeCartID;
        Name = name;
    }

    public int getTypeCartID() {
        return TypeCartID;
    }

    public void setTypeCartID(int typeCartID) {
        TypeCartID = typeCartID;
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
