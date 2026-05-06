package com.phuthanh.model.warehouse;

import java.util.Date;

public class DataCheck {
    private int checkAID;
    private int sheetAID;
    private int productAID;

    private String productID;
    private String idPartNo;
    private String nameProduct;

    private String nameCountry;
    private String nameSupplier;
    private String nameUnit;

    private double qtyWareHouse;
    private double qtyCheck;
    private double qtyDifferent;

    private String lastUser;
    private String remark;
    private Date lastTime;
    public DataCheck(int checkAID, int sheetAID, int productAID, String productID, String idPartNo, String nameProduct,
            String nameCountry, String nameSupplier, String nameUnit, double qtyWareHouse, double qtyCheck,
            double qtyDifferent, String lastUser, String remark, Date lastTime) {
        this.checkAID = checkAID;
        this.sheetAID = sheetAID;
        this.productAID = productAID;
        this.productID = productID;
        this.idPartNo = idPartNo;
        this.nameProduct = nameProduct;
        this.nameCountry = nameCountry;
        this.nameSupplier = nameSupplier;
        this.nameUnit = nameUnit;
        this.qtyWareHouse = qtyWareHouse;
        this.qtyCheck = qtyCheck;
        this.qtyDifferent = qtyDifferent;
        this.lastUser = lastUser;
        this.remark = remark;
        this.lastTime = lastTime;
    }
    public int getCheckAID() {
        return checkAID;
    }
    public void setCheckAID(int checkAID) {
        this.checkAID = checkAID;
    }
    public int getSheetAID() {
        return sheetAID;
    }
    public void setSheetAID(int sheetAID) {
        this.sheetAID = sheetAID;
    }
    public int getProductAID() {
        return productAID;
    }
    public void setProductAID(int productAID) {
        this.productAID = productAID;
    }
    public String getProductID() {
        return productID;
    }
    public void setProductID(String productID) {
        this.productID = productID;
    }
    public String getIdPartNo() {
        return idPartNo;
    }
    public void setIdPartNo(String idPartNo) {
        this.idPartNo = idPartNo;
    }
    public String getNameProduct() {
        return nameProduct;
    }
    public void setNameProduct(String nameProduct) {
        this.nameProduct = nameProduct;
    }
    public String getNameCountry() {
        return nameCountry;
    }
    public void setNameCountry(String nameCountry) {
        this.nameCountry = nameCountry;
    }
    public String getNameSupplier() {
        return nameSupplier;
    }
    public void setNameSupplier(String nameSupplier) {
        this.nameSupplier = nameSupplier;
    }
    public String getNameUnit() {
        return nameUnit;
    }
    public void setNameUnit(String nameUnit) {
        this.nameUnit = nameUnit;
    }
    public double getQtyWareHouse() {
        return qtyWareHouse;
    }
    public void setQtyWareHouse(double qtyWareHouse) {
        this.qtyWareHouse = qtyWareHouse;
    }
    public double getQtyCheck() {
        return qtyCheck;
    }
    public void setQtyCheck(double qtyCheck) {
        this.qtyCheck = qtyCheck;
    }
    public double getQtyDifferent() {
        return qtyDifferent;
    }
    public void setQtyDifferent(double qtyDifferent) {
        this.qtyDifferent = qtyDifferent;
    }
    public String getLastUser() {
        return lastUser;
    }
    public void setLastUser(String lastUser) {
        this.lastUser = lastUser;
    }
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    public Date getLastTime() {
        return lastTime;
    }
    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }


}
