package com.phuthanh.editableTable.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Product {

    private final StringProperty ProductAID = new SimpleStringProperty("");
    private final StringProperty ProductIDMain = new SimpleStringProperty("");
    private final StringProperty ProductID = new SimpleStringProperty("");
    private final StringProperty ID_Keeton = new SimpleStringProperty("");
    private final StringProperty ID_Industrial = new SimpleStringProperty("");
    private final StringProperty ID_PartNo = new SimpleStringProperty("");
    private final StringProperty ID_ReplacedPartNo = new SimpleStringProperty("");
    private final StringProperty NameProduct = new SimpleStringProperty("");
    private final StringProperty Parameter = new SimpleStringProperty("");
    private final StringProperty VehicleTypeID = new SimpleStringProperty("");
    private final StringProperty VehicleDetail = new SimpleStringProperty("");
    private final StringProperty VehicleCluster = new SimpleStringProperty("");
    private final StringProperty ManufacturerID = new SimpleStringProperty("");
    private final StringProperty CountryID = new SimpleStringProperty("");
    private final StringProperty SupplierID = new SimpleStringProperty("");
    private final StringProperty SupplierActualID = new SimpleStringProperty("");
    private final StringProperty UnitID = new SimpleStringProperty("");
    private final StringProperty SegmentID = new SimpleStringProperty("");
    private final StringProperty PurposeID = new SimpleStringProperty("");
    private final StringProperty Img1 = new SimpleStringProperty("");
    private final StringProperty Img2 = new SimpleStringProperty("");
    private final StringProperty Img3 = new SimpleStringProperty("");
    private final StringProperty Remark = new SimpleStringProperty("");
    private final StringProperty LastTime = new SimpleStringProperty("");

    // ===== PROPERTY METHODS (QUAN TRỌNG) =====
    public StringProperty ProductAIDProperty() {
        return ProductAID;
    }

    public StringProperty ProductIDMainProperty() {
        return ProductIDMain;
    }

    public StringProperty ProductIDProperty() {
        return ProductID;
    }

    public StringProperty ID_KeetonProperty() {
        return ID_Keeton;
    }

    public StringProperty ID_IndustrialProperty() {
        return ID_Industrial;
    }

    public StringProperty ID_PartNoProperty() {
        return ID_PartNo;
    }

    public StringProperty ID_ReplacedPartNoProperty() {
        return ID_ReplacedPartNo;
    }

    public StringProperty NameProductProperty() {
        return NameProduct;
    }

    public StringProperty ParameterProperty() {
        return Parameter;
    }

    public StringProperty VehicleTypeIDProperty() {
        return VehicleTypeID;
    }

    public StringProperty VehicleDetailProperty() {
        return VehicleDetail;
    }

    public StringProperty VehicleClusterProperty() {
        return VehicleCluster;
    }

    public StringProperty ManufacturerIDProperty() {
        return ManufacturerID;
    }

    public StringProperty CountryIDProperty() {
        return CountryID;
    }

    public StringProperty SupplierIDProperty() {
        return SupplierID;
    }

    public StringProperty SupplierActualIDProperty() {
        return SupplierActualID;
    }

    public StringProperty UnitIDProperty() {
        return UnitID;
    }

    public StringProperty SegmentIDProperty() {
        return SegmentID;
    }

    public StringProperty PurposeIDProperty() {
        return PurposeID;
    }

    public StringProperty Img1Property() {
        return Img1;
    }

    public StringProperty Img2Property() {
        return Img2;
    }

    public StringProperty Img3Property() {
        return Img3;
    }

    public StringProperty RemarkProperty() {
        return Remark;
    }

    public StringProperty LastTimeProperty() {
        return LastTime;
    }

}