package com.phuthanh.model.warehouse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Cart {

    // ================= CART =================
    private int cartAID;
    private int cartID;
    private int accountID;
    private int productAID;

    private double qty;
    private double price;
    private double priceVAT;
    private double total;
    private double cogs;
    private double priceCost;
    private double grossPriceVAT;

    private Integer paymentID;
    private Integer billID;
    private Integer sourceID;
    private Integer supplierID;
    private Integer deliveryID;
    private Integer employeeID;
    private Integer businessID;
    private Integer typeCartID;

    private int statusID;
    private Integer statusVAT;

    private LocalDate deliveryTime;
    private LocalDate reportDate;
    private LocalDateTime lastTime;
    private String remark;
    private String contractID;

    // từ DataWarehouse
    private String VehicleTypeID;
    private String locationID;

    // ================= ACCOUNT =================
    private String creator;
    private String parameter;

    // ================= PRODUCT =================
    private String productID;
    private String partNo;
    private String nameProduct;

    private Integer manufacturerID;
    private String manufacturerName;
    private Integer countryID;
    private String countryName;
    private Integer unitID;
    private String unitName;

    // ================= BUSINESS =================
    private String businessName;
    private String typeCartName;

    // ================= JOIN NAME =================
    private String namePayment;
    private String nameBill;
    private String nameSource;
    private String nameDelivery;
    private String proponent;

    // ================= STATUS TEXT =================
    private String nameStatus;
    private String nameStatusVAT;

    // ================= PRODUCT VAT =================
    private Integer productAIDVAT;
    private String productIDVAT;
    private String invoiceNumber;

    public Cart() {
    }

    public int getCartAID() {
        return cartAID;
    }

    public void setCartAID(int cartAID) {
        this.cartAID = cartAID;
    }

    public int getCartID() {
        return cartID;
    }

    public void setCartID(int cartID) {
        this.cartID = cartID;
    }

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public int getProductAID() {
        return productAID;
    }

    public void setProductAID(int productAID) {
        this.productAID = productAID;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPriceVAT() {
        return priceVAT;
    }

    public void setPriceVAT(double priceVAT) {
        this.priceVAT = priceVAT;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getCogs() {
        return cogs;
    }

    public void setCogs(double cogs) {
        this.cogs = cogs;
    }

    public Integer getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(Integer paymentID) {
        this.paymentID = paymentID;
    }

    public Integer getBillID() {
        return billID;
    }

    public void setBillID(Integer billID) {
        this.billID = billID;
    }

    public Integer getSourceID() {
        return sourceID;
    }

    public void setSourceID(Integer sourceID) {
        this.sourceID = sourceID;
    }

    public Integer getDeliveryID() {
        return deliveryID;
    }

    public void setDeliveryID(Integer deliveryID) {
        this.deliveryID = deliveryID;
    }

    public Integer getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(Integer employeeID) {
        this.employeeID = employeeID;
    }

    public Integer getBusinessID() {
        return businessID;
    }

    public void setBusinessID(Integer businessID) {
        this.businessID = businessID;
    }

    public Integer getTypeCartID() {
        return typeCartID;
    }

    public void setTypeCartID(Integer typeCartID) {
        this.typeCartID = typeCartID;
    }

    public int getStatusID() {
        return statusID;
    }

    public void setStatusID(int statusID) {
        this.statusID = statusID;
    }

    public Integer getStatusVAT() {
        return statusVAT;
    }

    public void setStatusVAT(Integer statusVAT) {
        this.statusVAT = statusVAT;
    }

    public LocalDate getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(LocalDate deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public LocalDateTime getLastTime() {
        return lastTime;
    }

    public void setLastTime(LocalDateTime lastTime) {
        this.lastTime = lastTime;
    }

    public double getPriceCost() {
        return priceCost;
    }

    public void setPriceCost(double priceCost) {
        this.priceCost = priceCost;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getContractID() {
        return contractID;
    }

    public void setContractID(String contractID) {
        this.contractID = contractID;
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getNameProduct() {
        return nameProduct;
    }

    public void setNameProduct(String nameProduct) {
        this.nameProduct = nameProduct;
    }

    public Integer getManufacturerID() {
        return manufacturerID;
    }

    public void setManufacturerID(Integer manufacturerID) {
        this.manufacturerID = manufacturerID;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public Integer getCountryID() {
        return countryID;
    }

    public void setCountryID(Integer countryID) {
        this.countryID = countryID;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Integer getUnitID() {
        return unitID;
    }

    public void setUnitID(Integer unitID) {
        this.unitID = unitID;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTypeCartName() {
        return typeCartName;
    }

    public void setTypeCartName(String typeCartName) {
        this.typeCartName = typeCartName;
    }

    public String getNamePayment() {
        return namePayment;
    }

    public void setNamePayment(String namePayment) {
        this.namePayment = namePayment;
    }

    public String getNameBill() {
        return nameBill;
    }

    public void setNameBill(String nameBill) {
        this.nameBill = nameBill;
    }

    public String getNameSource() {
        return nameSource;
    }

    public void setNameSource(String nameSource) {
        this.nameSource = nameSource;
    }

    public String getNameDelivery() {
        return nameDelivery;
    }

    public void setNameDelivery(String nameDelivery) {
        this.nameDelivery = nameDelivery;
    }

    public String getProponent() {
        return proponent;
    }

    public void setProponent(String proponent) {
        this.proponent = proponent;
    }

    public String getNameStatus() {
        return nameStatus;
    }

    public void setNameStatus(String nameStatus) {
        this.nameStatus = nameStatus;
    }

    public String getNameStatusVAT() {
        return nameStatusVAT;
    }

    public void setNameStatusVAT(String nameStatusVAT) {
        this.nameStatusVAT = nameStatusVAT;
    }

    public Integer getProductAIDVAT() {
        return productAIDVAT;
    }

    public void setProductAIDVAT(Integer productAIDVAT) {
        this.productAIDVAT = productAIDVAT;
    }

    public String getProductIDVAT() {
        return productIDVAT;
    }

    public void setProductIDVAT(String productIDVAT) {
        this.productIDVAT = productIDVAT;
    }

    public String getVehicleTypeID() {
        return VehicleTypeID;
    }

    public void setVehicleTypeID(String vehicleTypeID) {
        VehicleTypeID = vehicleTypeID;
    }

    public double getGrossPriceVAT() {
        return grossPriceVAT;
    }

    public void setGrossPriceVAT(double grossPriceVAT) {
        this.grossPriceVAT = grossPriceVAT;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public Integer getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(Integer supplierID) {
        this.supplierID = supplierID;
    }
    

    // getters/setters generate bằng IDE là xong

}