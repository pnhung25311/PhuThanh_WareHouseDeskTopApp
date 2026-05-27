package com.phuthanh.model.info;

public class Supplier {
    private int SupplierID;
    private String Name;
    private int Category;
    private String NameCompany;
    private String Address;
    private String Taxcode;
    private String PhoneNumber;
    private String Email;

    public Supplier() {
    }

    public Supplier(int supplierID, String name, int category, String nameCompany, String address, String taxcode,
            String phoneNumber, String email) {
        SupplierID = supplierID;
        Name = name;
        Category = category;
        NameCompany = nameCompany;
        Address = address;
        Taxcode = taxcode;
        PhoneNumber = phoneNumber;
        Email = email;
    }

    public int getSupplierID() {
        return SupplierID;
    }

    public void setSupplierID(int supplierID) {
        SupplierID = supplierID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getCategory() {
        return Category;
    }

    public void setCategory(int category) {
        Category = category;
    }

    public String getNameCompany() {
        return NameCompany;
    }

    public void setNameCompany(String nameCompany) {
        NameCompany = nameCompany;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getTaxcode() {
        return Taxcode;
    }

    public void setTaxcode(String taxcode) {
        Taxcode = taxcode;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    // Giúp ComboBox hiển thị tên quốc gia
    @Override
    public String toString() {
        return Name;
    }
}
