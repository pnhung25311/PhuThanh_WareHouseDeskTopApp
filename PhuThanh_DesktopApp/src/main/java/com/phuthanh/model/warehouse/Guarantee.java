package com.phuthanh.model.warehouse;

import java.time.LocalDateTime;

public class Guarantee {

    private String guaranteeAID;
    private String guaranteeID;

    // Product Broken
    private String productBroken;
    private String productIDBroken;
    private String idPartNoBroken;
    private String nameProductBroken;
    private String countryNameBroken;
    private String unitNameBroken;

    // Time info
    private LocalDateTime timeStart;
    private LocalDateTime timeBroken;
    private double timeUsage;
    private double qty;
    private String reasonBroken;

    // Product Guarantee
    private String productGuarantee;
    private String productIDGuarantee;
    private String idPartNoGuarantee;
    private String nameProductGuarantee;
    private String unitNameGuarantee;
    private String countryNameGuarantee;

    private LocalDateTime timeGuarantee;

    // Supplier
    private String partner;
    private int supplierGuarantee;

    // Images
    private String img1;
    private String img2;
    private String img3;

    // Other
    private String remark;
    private String lastUser;
    private LocalDateTime lastTime;

    public Guarantee() {
    }

    // Getter & Setter

    public String getGuaranteeAID() {
        return guaranteeAID;
    }

    public void setGuaranteeAID(String guaranteeAID) {
        this.guaranteeAID = guaranteeAID;
    }

    public String getGuaranteeID() {
        return guaranteeID;
    }

    public void setGuaranteeID(String guaranteeID) {
        this.guaranteeID = guaranteeID;
    }

    public String getProductBroken() {
        return productBroken;
    }

    public void setProductBroken(String productBroken) {
        this.productBroken = productBroken;
    }

    public String getProductIDBroken() {
        return productIDBroken;
    }

    public void setProductIDBroken(String productIDBroken) {
        this.productIDBroken = productIDBroken;
    }

    public String getIdPartNoBroken() {
        return idPartNoBroken;
    }

    public void setIdPartNoBroken(String idPartNoBroken) {
        this.idPartNoBroken = idPartNoBroken;
    }

    public String getNameProductBroken() {
        return nameProductBroken;
    }

    public void setNameProductBroken(String nameProductBroken) {
        this.nameProductBroken = nameProductBroken;
    }

    public String getCountryNameBroken() {
        return countryNameBroken;
    }

    public void setCountryNameBroken(String countryNameBroken) {
        this.countryNameBroken = countryNameBroken;
    }

    public String getUnitNameBroken() {
        return unitNameBroken;
    }

    public void setUnitNameBroken(String unitNameBroken) {
        this.unitNameBroken = unitNameBroken;
    }

    public LocalDateTime getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(LocalDateTime timeStart) {
        this.timeStart = timeStart;
    }

    public LocalDateTime getTimeBroken() {
        return timeBroken;
    }

    public void setTimeBroken(LocalDateTime timeBroken) {
        this.timeBroken = timeBroken;
    }

    public double getTimeUsage() {
        return timeUsage;
    }

    public void setTimeUsage(double timeUsage) {
        this.timeUsage = timeUsage;
    }

    public String getReasonBroken() {
        return reasonBroken;
    }

    public void setReasonBroken(String reasonBroken) {
        this.reasonBroken = reasonBroken;
    }

    public String getProductGuarantee() {
        return productGuarantee;
    }

    public void setProductGuarantee(String productGuarantee) {
        this.productGuarantee = productGuarantee;
    }

    public String getProductIDGuarantee() {
        return productIDGuarantee;
    }

    public void setProductIDGuarantee(String productIDGuarantee) {
        this.productIDGuarantee = productIDGuarantee;
    }

    public String getIdPartNoGuarantee() {
        return idPartNoGuarantee;
    }

    public void setIdPartNoGuarantee(String idPartNoGuarantee) {
        this.idPartNoGuarantee = idPartNoGuarantee;
    }

    public String getNameProductGuarantee() {
        return nameProductGuarantee;
    }

    public void setNameProductGuarantee(String nameProductGuarantee) {
        this.nameProductGuarantee = nameProductGuarantee;
    }

    public String getUnitNameGuarantee() {
        return unitNameGuarantee;
    }

    public void setUnitNameGuarantee(String unitNameGuarantee) {
        this.unitNameGuarantee = unitNameGuarantee;
    }

    public String getCountryNameGuarantee() {
        return countryNameGuarantee;
    }

    public void setCountryNameGuarantee(String countryNameGuarantee) {
        this.countryNameGuarantee = countryNameGuarantee;
    }

    public LocalDateTime getTimeGuarantee() {
        return timeGuarantee;
    }

    public void setTimeGuarantee(LocalDateTime timeGuarantee) {
        this.timeGuarantee = timeGuarantee;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getImg1() {
        return img1;
    }

    public void setImg1(String img1) {
        this.img1 = img1;
    }

    public String getImg2() {
        return img2;
    }

    public void setImg2(String img2) {
        this.img2 = img2;
    }

    public String getImg3() {
        return img3;
    }

    public void setImg3(String img3) {
        this.img3 = img3;
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

    public LocalDateTime getLastTime() {
        return lastTime;
    }

    public void setLastTime(LocalDateTime lastTime) {
        this.lastTime = lastTime;
    }

    public int getSupplierGuarantee() {
        return supplierGuarantee;
    }

    public void setSupplierGuarantee(int supplierGuarantee) {
        this.supplierGuarantee = supplierGuarantee;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    
}