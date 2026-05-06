package com.phuthanh.model.warehouse;

import java.time.LocalDateTime;
import java.util.Date;

public class RequestHistoryWareHouse {
    private int requestAID;
    private int historyAID;
    private int dataWareHouseAID;
    private double qty;
    private Long idEmployee;
    private String partner;
    private String remark;

    private Date time;
    private String TransferGroupID;
    private String lastUser;
    private LocalDateTime lastTime;

    private String userRequest;
    private Date timeRequest;

    private String userConfirm;
    private Date timeConfirm;

    private Boolean action;
    private Date lastTimeOfRequest;

    public RequestHistoryWareHouse(int requestAID, int historyAID, int dataWareHouseAID, double qty, Long idEmployee,
            String partner, String remark, Date time, String transferGroupID, String lastUser, LocalDateTime lastTime, String userRequest,
            Date timeRequest, String userConfirm, Date timeConfirm, Boolean action, Date lastTimeOfRequest) {
        this.requestAID = requestAID;
        this.historyAID = historyAID;
        this.dataWareHouseAID = dataWareHouseAID;
        this.qty = qty;
        this.idEmployee = idEmployee;
        this.partner = partner;
        this.remark = remark;
        this.time = time;
        this.TransferGroupID = transferGroupID;
        this.lastUser = lastUser;
        this.lastTime = lastTime;
        this.userRequest = userRequest;
        this.timeRequest = timeRequest;
        this.userConfirm = userConfirm;
        this.timeConfirm = timeConfirm;
        this.action = action;
        this.lastTimeOfRequest = lastTimeOfRequest;
    }

    public int getRequestAID() {
        return requestAID;
    }

    public void setRequestAID(int requestAID) {
        this.requestAID = requestAID;
    }

    public int getHistoryAID() {
        return historyAID;
    }

    public void setHistoryAID(int historyAID) {
        this.historyAID = historyAID;
    }

    public int getDataWareHouseAID() {
        return dataWareHouseAID;
    }

    public void setDataWareHouseAID(int dataWareHouseAID) {
        this.dataWareHouseAID = dataWareHouseAID;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public Long getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(Long idEmployee) {
        this.idEmployee = idEmployee;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getLastUser() {
        return lastUser;
    }

    public void setLastUser(String lastUser) {
        this.lastUser = lastUser;
    }

    public LocalDateTime getLastTime() {
        return lastTime;
    }

    public void setLastTime(LocalDateTime lastTime) {
        this.lastTime = lastTime;
    }

    public String getUserRequest() {
        return userRequest;
    }

    public void setUserRequest(String userRequest) {
        this.userRequest = userRequest;
    }

    public Date getTimeRequest() {
        return timeRequest;
    }

    public void setTimeRequest(Date timeRequest) {
        this.timeRequest = timeRequest;
    }

    public String getUserConfirm() {
        return userConfirm;
    }

    public void setUserConfirm(String userConfirm) {
        this.userConfirm = userConfirm;
    }

    public Date getTimeConfirm() {
        return timeConfirm;
    }

    public void setTimeConfirm(Date timeConfirm) {
        this.timeConfirm = timeConfirm;
    }

    public Boolean getAction() {
        return action;
    }

    public void setAction(Boolean action) {
        this.action = action;
    }

    public Date getLastTimeOfRequest() {
        return lastTimeOfRequest;
    }

    public void setLastTimeOfRequest(Date lastTimeOfRequest) {
        this.lastTimeOfRequest = lastTimeOfRequest;
    }
    public String getTransferGroupID() {
        return TransferGroupID;
    }
    public void setTransferGroupID(String transferGroupID) {
        TransferGroupID = transferGroupID;
    }

    // Getters and Setters
}
