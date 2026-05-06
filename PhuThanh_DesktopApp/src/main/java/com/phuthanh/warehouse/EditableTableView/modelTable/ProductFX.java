package com.phuthanh.warehouse.EditableTableView.modelTable;

import javafx.beans.property.*;
import java.time.LocalDate;

public class ProductFX {

    private final StringProperty productAID = new SimpleStringProperty();
    private final StringProperty productIDMain = new SimpleStringProperty();
    private final StringProperty productID = new SimpleStringProperty();
    private final StringProperty idKeeton = new SimpleStringProperty();
    private final StringProperty idIndustrial = new SimpleStringProperty();
    private final StringProperty idPartNo = new SimpleStringProperty();
    private final StringProperty idReplacedPartNo = new SimpleStringProperty();
    private final StringProperty nameProduct = new SimpleStringProperty();
    private final StringProperty parameter = new SimpleStringProperty();
    private final IntegerProperty vehicleTypeID = new SimpleIntegerProperty();
    private final IntegerProperty manufacturerID = new SimpleIntegerProperty();
    private final IntegerProperty countryID = new SimpleIntegerProperty();
    private final IntegerProperty supplierActualID = new SimpleIntegerProperty();
    private final IntegerProperty supplierID = new SimpleIntegerProperty();
    private final IntegerProperty unitID = new SimpleIntegerProperty();
    private final StringProperty vehicleDetail = new SimpleStringProperty();
    private final StringProperty vehicleCluster = new SimpleStringProperty();
    private final IntegerProperty segmentID = new SimpleIntegerProperty();
    private final IntegerProperty purposeID = new SimpleIntegerProperty();
    private final StringProperty remark = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> lastTime = new SimpleObjectProperty<>();

    // ===== constructor rỗng (dùng khi paste Excel)
    public ProductFX() {
    }

    // ===== Getter / Setter / Property =====
    public StringProperty productAIDProperty() {
        return productAID;
    }

    public StringProperty productIDMainProperty() {
        return productIDMain;
    }

    public StringProperty productIDProperty() {
        return productID;
    }

    public StringProperty idKeetonProperty() {
        return idKeeton;
    }

    public StringProperty idIndustrialProperty() {
        return idIndustrial;
    }

    public StringProperty idPartNoProperty() {
        return idPartNo;
    }

    public StringProperty idReplacedPartNoProperty() {
        return idReplacedPartNo;
    }

    public StringProperty nameProductProperty() {
        return nameProduct;
    }

    public StringProperty parameterProperty() {
        return parameter;
    }

    public IntegerProperty vehicleTypeIDProperty() {
        return vehicleTypeID;
    }

    public IntegerProperty manufacturerIDProperty() {
        return manufacturerID;
    }

    public IntegerProperty countryIDProperty() {
        return countryID;
    }

    public IntegerProperty supplierActualIDProperty() {
        return supplierActualID;
    }

    public IntegerProperty supplierIDProperty() {
        return supplierID;
    }

    public IntegerProperty unitIDProperty() {
        return unitID;
    }

    public StringProperty vehicleDetailProperty() {
        return vehicleDetail;
    }

    public StringProperty vehicleClusterProperty() {
        return vehicleCluster;
    }

    public IntegerProperty segmentIDProperty() {
        return segmentID;
    }

    public IntegerProperty purposeIDProperty() {
        return purposeID;
    }

    public StringProperty remarkProperty() {
        return remark;
    }

    public ObjectProperty<LocalDate> lastTimeProperty() {
        return lastTime;
    }

    // ===== helper parse int (dùng khi paste Excel)
    public void setIntSafe(IntegerProperty prop, String value) {
        try {
            prop.set(Integer.parseInt(value));
        } catch (Exception e) {
            prop.set(0);
        }
    }
}
