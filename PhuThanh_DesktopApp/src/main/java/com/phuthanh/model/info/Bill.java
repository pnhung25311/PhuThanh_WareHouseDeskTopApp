package com.phuthanh.model.info;

public class Bill {
    private int BillID;
    private String Name;

    public Bill(int billID, String name) {
        BillID = billID;
        Name = name;
    }

    public int getBillID() {
        return BillID;
    }

    public void setBillID(int billID) {
        BillID = billID;
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
