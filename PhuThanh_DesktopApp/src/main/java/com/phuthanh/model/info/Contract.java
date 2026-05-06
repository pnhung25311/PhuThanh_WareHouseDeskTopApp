package com.phuthanh.model.info;

public class Contract {
    private int ContractID;
    private String Name;

    public Contract(int contractID, String name) {
        ContractID = contractID;
        Name = name;
    }

    public int getContractID() {
        return ContractID;
    }

    public void setContractID(int contractID) {
        ContractID = contractID;
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
