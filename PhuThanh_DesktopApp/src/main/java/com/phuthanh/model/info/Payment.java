package com.phuthanh.model.info;

public class Payment {
    private int PaymentID;
    private String Name;

    public Payment(int paymentID, String name) {
        PaymentID = paymentID;
        Name = name;
    }

    public int getPaymentID() {
        return PaymentID;
    }

    public void setPaymentID(int paymentID) {
        PaymentID = paymentID;
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
