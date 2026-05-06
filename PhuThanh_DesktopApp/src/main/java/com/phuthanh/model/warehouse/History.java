package com.phuthanh.model.warehouse;

import java.sql.Date;

public class History {
    private String HistoryAID;
    private String DataWareHouseAID;
    private double Qty;
    private int ID_Employee;
    private int Partner;
    private String Remark;
    private Date Time;
    private String TransferGroupID;
    private String LastUser;
    private Date LastTime;

    public History(String historyAID, String dataWareHouseAID, double qty, int iD_Employee, int partner, String remark, String transferGroupID,
            Date time, String lastUser, Date lastTime) {
        HistoryAID = historyAID;
        DataWareHouseAID = dataWareHouseAID;
        Qty = qty;
        ID_Employee = iD_Employee;
        Partner = partner;
        Remark = remark;
        TransferGroupID = transferGroupID;
        Time = time;
        LastUser = lastUser;
        LastTime = lastTime;
    }

    public String getHistoryAID() {
        return HistoryAID;
    }

    public void setHistoryAID(String historyAID) {
        HistoryAID = historyAID;
    }

    public String getDataWareHouseAID() {
        return DataWareHouseAID;
    }

    public void setDataWareHouseAID(String dataWareHouseAID) {
        DataWareHouseAID = dataWareHouseAID;
    }

    public double getQty() {
        return Qty;
    }

    public void setQty(double qty) {
        Qty = qty;
    }

    public int getID_Employee() {
        return ID_Employee;
    }

    public void setID_Employee(int iD_Employee) {
        ID_Employee = iD_Employee;
    }

    public int getPartner() {
        return Partner;
    }

    public void setPartner(int partner) {
        Partner = partner;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public Date getTime() {
        return Time;
    }

    public void setTime(Date time) {
        Time = time;
    }

    public String getLastUser() {
        return LastUser;
    }

    public void setLastUser(String lastUser) {
        LastUser = lastUser;
    }

    public Date getLastTime() {
        return LastTime;
    }

    public void setLastTime(Date lastTime) {
        LastTime = lastTime;
    }

    public String getTransferGroupID() {
        return TransferGroupID;
    }
    
    public void setTransferGroupID(String transferGroupID) {
        TransferGroupID = transferGroupID;
    }
}
