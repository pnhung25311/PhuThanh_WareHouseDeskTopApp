package com.phuthanh.model.warehouse;

import java.util.Date;

public class WareHouse {
    private String DataWareHouseAID;
    private String ProductAID;
    private double Qty;
    private double Qty_Expected;
    private String ID_Bill;
    private String LocationID;
    private Date LastTime;
    private String LastUser;
    private String Remark;

    public WareHouse(String dataWareHouseAID, String productAID, double qty, double qty_Expected, String iD_Bill,
            String locationID, Date lastTime, String lastUser, String remark) {
        DataWareHouseAID = dataWareHouseAID;
        ProductAID = productAID;
        Qty = qty;
        Qty_Expected = qty_Expected;
        ID_Bill = iD_Bill;
        LocationID = locationID;
        LastTime = lastTime;
        LastUser = lastUser;
        Remark = remark;
    }

    public String getDataWareHouseAID() {
        return DataWareHouseAID;
    }

    public void setDataWareHouseAID(String dataWareHouseAID) {
        DataWareHouseAID = dataWareHouseAID;
    }

    public String getProductAID() {
        return ProductAID;
    }

    public void setProductAID(String productAID) {
        ProductAID = productAID;
    }

    public double getQty() {
        return Qty;
    }

    public void setQty(double qty) {
        Qty = qty;
    }

    public double getQty_Expected() {
        return Qty_Expected;
    }

    public void setQty_Expected(double qty_Expected) {
        Qty_Expected = qty_Expected;
    }

    public String getID_Bill() {
        return ID_Bill;
    }

    public void setID_Bill(String iD_Bill) {
        ID_Bill = iD_Bill;
    }

    public String getLocationID() {
        return LocationID;
    }

    public void setLocationID(String locationID) {
        LocationID = locationID;
    }

    public Date getLastTime() {
        return LastTime;
    }

    public void setLastTime(Date lastTime) {
        LastTime = lastTime;
    }

    public String getLastUser() {
        return LastUser;
    }

    public void setLastUser(String lastUser) {
        LastUser = lastUser;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

}
