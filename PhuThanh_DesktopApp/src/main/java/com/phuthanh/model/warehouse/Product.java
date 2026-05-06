package com.phuthanh.model.warehouse;

import java.util.Date;

public class Product {
    private String ProductAID;
    private String ProductIDMain;
    private String ProductID;
    private String ID_Keeton;
    private String ID_Industrial;
    private String ID_PartNo;
    private String ID_ReplacedPartNo;
    private String NameProduct;
    private String Parameter;
    private String VehicleTypeID;
    private int ManufacturerID;
    private int CountryID;
    private int SupplierActualID;
    private int SupplierID;
    private int UnitID;
    private String VehicleDetail;
    private String VehicleCluster;
    private int SegmentID;
    private int PurposeID;
    private String Img1;
    private String Img2;
    private String Img3;
    private String Remark;
    private Date LastTime;  

    private String CountryName;
    private String ManufacturerName;    
    private String UnitName;
    private String SegmentName;
    private String PurposeName;



    public Product(String productAID, String productIDMain, String productID, String iD_Keeton, String iD_Industrial, String iD_PartNo,
            String iD_ReplacedPartNo, String nameProduct, String parameter, String vehicleTypeID, int manufacturerID,
            int countryID, int supplierActualID, int supplierID, int unitID, String vehicleDetail,
            String vehicleCluster, String img1, String img2, String img3, String remark, Date lastTime,
            String countryName, String manufacturerName, String unitName, String segmentName, int segmentID, int purposeID, String purposeName) {
        ProductAID = productAID;
        ProductIDMain = productIDMain;
        ProductID = productID;
        ID_Keeton = iD_Keeton;
        ID_Industrial = iD_Industrial;
        ID_PartNo = iD_PartNo;
        ID_ReplacedPartNo = iD_ReplacedPartNo;
        NameProduct = nameProduct;
        Parameter = parameter;
        VehicleTypeID = vehicleTypeID;
        ManufacturerID = manufacturerID;
        CountryID = countryID;
        SupplierActualID = supplierActualID;
        SupplierID = supplierID;
        UnitID = unitID;
        VehicleDetail = vehicleDetail;
        VehicleCluster = vehicleCluster;
        Img1 = img1;
        Img2 = img2;
        Img3 = img3;
        Remark = remark;
        LastTime = lastTime;
        CountryName = countryName;
        ManufacturerName = manufacturerName;
        UnitName = unitName;
        SegmentName = segmentName;
        SegmentID = segmentID;
        PurposeID = purposeID;
        PurposeName = purposeName;
    }


    public String getProductAID() {
        return ProductAID;
    }

    public void setProductAID(String productAID) {
        ProductAID = productAID;
    }

    public String getProductID() {
        return ProductID;
    }

    public void setProductID(String productID) {
        ProductID = productID;
    }

    public String getID_Keeton() {
        return ID_Keeton;
    }

    public void setID_Keeton(String iD_Keeton) {
        ID_Keeton = iD_Keeton;
    }

    public String getID_Industrial() {
        return ID_Industrial;
    }

    public void setID_Industrial(String iD_Industrial) {
        ID_Industrial = iD_Industrial;
    }

    public String getID_PartNo() {
        return ID_PartNo;
    }

    public void setID_PartNo(String iD_PartNo) {
        ID_PartNo = iD_PartNo;
    }

    public String getID_ReplacedPartNo() {
        return ID_ReplacedPartNo;
    }

    public void setID_ReplacedPartNo(String iD_ReplacedPartNo) {
        ID_ReplacedPartNo = iD_ReplacedPartNo;
    }

    public String getNameProduct() {
        return NameProduct;
    }

    public void setNameProduct(String nameProduct) {
        NameProduct = nameProduct;
    }

    public String getParameter() {
        return Parameter;
    }

    public void setParameter(String parameter) {
        Parameter = parameter;
    }

    public String getVehicleTypeID() {
        return VehicleTypeID;
    }

    public void setVehicleTypeID(String vehicleTypeID) {
        VehicleTypeID = vehicleTypeID;
    }

    public int getManufacturerID() {
        return ManufacturerID;
    }

    public void setManufacturerID(int manufacturerID) {
        ManufacturerID = manufacturerID;
    }

    public int getCountryID() {
        return CountryID;
    }

    public void setCountryID(int countryID) {
        CountryID = countryID;
    }

    public int getSupplierActualID() {
        return SupplierActualID;
    }

    public void setSupplierActualID(int supplierActualID) {
        SupplierActualID = supplierActualID;
    }

    public int getSupplierID() {
        return SupplierID;
    }

    public void setSupplierID(int supplierID) {
        SupplierID = supplierID;
    }

    public int getUnitID() {
        return UnitID;
    }

    public void setUnitID(int unitID) {
        UnitID = unitID;
    }

    public String getVehicleDetail() {
        return VehicleDetail;
    }

    public void setVehicleDetail(String vehicleDetail) {
        VehicleDetail = vehicleDetail;
    }

    

    public int getSegmentID() {
        return SegmentID;
    }

    public void setSegmentID(int segmentID) {
        SegmentID = segmentID;
    }

    public int getPurposeID() {
        return PurposeID;
    }

    public void setPurposeID(int purposeID) {
        PurposeID = purposeID;
    }

    public String getSegmentName() {
        return SegmentName;
    }

    public void setSegmentName(String segmentName) {
        SegmentName = segmentName;
    }

    public String getImg1() {
        return Img1;
    }

    public void setImg1(String img1) {
        Img1 = img1;
    }

    public String getImg2() {
        return Img2;
    }

    public void setImg2(String img2) {
        Img2 = img2;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public Date getLastTime() {
        return LastTime;
    }

    public void setLastTime(Date lastTime) {
        LastTime = lastTime;
    }

    public String getImg3() {
        return Img3;
    }

    public void setImg3(String img3) {
        Img3 = img3;
    }

    public String getVehicleCluster() {
        return VehicleCluster;
    }

    public void setVehicleCluster(String vehicleCluster) {
        VehicleCluster = vehicleCluster;
    }

    public String getCountryName() {
        return CountryName;
    }

    public void setCountryName(String countryName) {
        CountryName = countryName;
    }

    public String getManufacturerName() {
        return ManufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        ManufacturerName = manufacturerName;
    }

    public String getUnitName() {
        return UnitName;
    }

    public void setUnitName(String unitName) {
        UnitName = unitName;
    }

    public String getPurposeName() {
        return PurposeName;
    }

    public void setPurposeName(String purposeName) {
        PurposeName = purposeName;
    }

    public String getProductIDMain() {
        return ProductIDMain;
    }

    public void setProductIDMain(String productIDMain) {
        ProductIDMain = productIDMain;
    }

    // Getters and Setters

    
}
