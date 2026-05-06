package com.phuthanh.model.warehouse;

import java.sql.Date;

public class Sheet {
    private int sheetAID;
    private String sheetID;
    private String status; // 'Hoàn thành' / 'Chưa hoàn thành'
    private String remark;
    private String lastUser;
    private Date lastTime;


    public Sheet(int sheetAID, String sheetID, String status, String remark, String lastUser, Date lastTime) {
        this.sheetAID = sheetAID;
        this.sheetID = sheetID;
        this.status = status;
        this.remark = remark;
        this.lastUser = lastUser;
        this.lastTime = lastTime;
    }

    public int getSheetAID() {
        return sheetAID;
    }

    public void setSheetAID(int sheetAID) {
        this.sheetAID = sheetAID;
    }

    public String getSheetID() {
        return sheetID;
    }

    public void setSheetID(String sheetID) {
        this.sheetID = sheetID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLastUser() {
        return lastUser;
    }

    public void setLastUser(String lastUser) {
        this.lastUser = lastUser;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

}
