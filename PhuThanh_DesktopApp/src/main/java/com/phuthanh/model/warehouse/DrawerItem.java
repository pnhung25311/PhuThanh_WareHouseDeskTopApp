package com.phuthanh.model.warehouse;

public class DrawerItem {
    private String WareHouseID;
    private String NameWareHouse;
    private String WareHouseTable;
    private int WareHouseCategory;
    private String WareHouseHistory;
    private String WareHouseDataBase;
    private String WareHouseDataBaseHistory;
    private String WareHouseRequest;
    private String WareHouseRequestDataBase;
    private String WareHouseUpdateHistoryDataBase;
    private String WareHouseUpdateHistory;
    private String WareHouseSheetDataBase;
    private String WareHouseCheckDataBase;
    private int WareHouseSupplierID;
    private String WareHouseSheet;
    private String WareHouseDataCheck;

    public DrawerItem(String wareHouseID, String nameWareHouse, String wareHouseTable, int wareHouseCategory,
            String wareHouseHistory, String wareHouseDataBase, String wareHouseDataBaseHistory, String wareHouseRequest,
            String wareHouseRequestDataBase, String wareHouseUpdateHistoryDataBase, String wareHouseUpdateHistory,
            String wareHouseSheetDataBase, String wareHouseCheckDataBase, int wareHouseSupplierID,
            String wareHouseSheet, String wareHouseDataCheck) {
        WareHouseID = wareHouseID;
        NameWareHouse = nameWareHouse;
        WareHouseTable = wareHouseTable;
        WareHouseCategory = wareHouseCategory;
        WareHouseHistory = wareHouseHistory;
        WareHouseDataBase = wareHouseDataBase;
        WareHouseDataBaseHistory = wareHouseDataBaseHistory;
        WareHouseRequest = wareHouseRequest;
        WareHouseRequestDataBase = wareHouseRequestDataBase;
        WareHouseUpdateHistoryDataBase = wareHouseUpdateHistoryDataBase;
        WareHouseUpdateHistory = wareHouseUpdateHistory;
        WareHouseSheetDataBase = wareHouseSheetDataBase;
        WareHouseCheckDataBase = wareHouseCheckDataBase;
        WareHouseSupplierID = wareHouseSupplierID;
        WareHouseSheet = wareHouseSheet;
        WareHouseDataCheck = wareHouseDataCheck;
    }

    public String getWareHouseID() {
        return WareHouseID;
    }

    public void setWareHouseID(String wareHouseID) {
        WareHouseID = wareHouseID;
    }

    public String getNameWareHouse() {
        return NameWareHouse;
    }

    public void setNameWareHouse(String nameWareHouse) {
        NameWareHouse = nameWareHouse;
    }

    public String getWareHouseTable() {
        return WareHouseTable;
    }

    public void setWareHouseTable(String wareHouseTable) {
        WareHouseTable = wareHouseTable;
    }

    public int getWareHouseCategory() {
        return WareHouseCategory;
    }

    public void setWareHouseCategory(int wareHouseCategory) {
        WareHouseCategory = wareHouseCategory;
    }

    public String getWareHouseHistory() {
        return WareHouseHistory;
    }

    public void setWareHouseHistory(String wareHouseHistory) {
        WareHouseHistory = wareHouseHistory;
    }

    public String getWareHouseDataBase() {
        return WareHouseDataBase;
    }

    public void setWareHouseDataBase(String wareHouseDataBase) {
        WareHouseDataBase = wareHouseDataBase;
    }

    public String getWareHouseDataBaseHistory() {
        return WareHouseDataBaseHistory;
    }

    public void setWareHouseDataBaseHistory(String wareHouseDataBaseHistory) {
        WareHouseDataBaseHistory = wareHouseDataBaseHistory;
    }

    public String getWareHouseRequest() {
        return WareHouseRequest;
    }

    public void setWareHouseRequest(String wareHouseRequest) {
        WareHouseRequest = wareHouseRequest;
    }

    public String getWareHouseRequestDataBase() {
        return WareHouseRequestDataBase;
    }

    public void setWareHouseRequestDataBase(String wareHouseRequestDataBase) {
        WareHouseRequestDataBase = wareHouseRequestDataBase;
    }

    public String getWareHouseUpdateHistoryDataBase() {
        return WareHouseUpdateHistoryDataBase;
    }

    public void setWareHouseUpdateHistoryDataBase(String wareHouseUpdateHistoryDataBase) {
        WareHouseUpdateHistoryDataBase = wareHouseUpdateHistoryDataBase;
    }

    public String getWareHouseUpdateHistory() {
        return WareHouseUpdateHistory;
    }

    public void setWareHouseUpdateHistory(String wareHouseUpdateHistory) {
        WareHouseUpdateHistory = wareHouseUpdateHistory;
    }

    public String getWareHouseSheetDataBase() {
        return WareHouseSheetDataBase;
    }

    public void setWareHouseSheetDataBase(String wareHouseSheetDataBase) {
        WareHouseSheetDataBase = wareHouseSheetDataBase;
    }

    public String getWareHouseCheckDataBase() {
        return WareHouseCheckDataBase;
    }

    public void setWareHouseCheckDataBase(String wareHouseCheckDataBase) {
        WareHouseCheckDataBase = wareHouseCheckDataBase;
    }

    public int getWareHouseSupplierID() {
        return WareHouseSupplierID;
    }

    public void setWareHouseSupplierID(int wareHouseSupplierID) {
        WareHouseSupplierID = wareHouseSupplierID;
    }

    public String getWareHouseSheet() {
        return WareHouseSheet;
    }

    public void setWareHouseSheet(String wareHouseSheet) {
        WareHouseSheet = wareHouseSheet;
    }

    public String getWareHouseDataCheck() {
        return WareHouseDataCheck;
    }

    public void setWareHouseDataCheck(String wareHouseDataCheck) {
        WareHouseDataCheck = wareHouseDataCheck;
    }

    // Giúp ComboBox hiển thị tên quốc gia
    @Override
    public String toString() {
        return NameWareHouse;
    }

}
