package com.phuthanh.warehouse.EditableTableView.modelTable;

import java.time.LocalDate;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HistoryWareHouseFX {
    private final StringProperty historyAID = new SimpleStringProperty();
    private final StringProperty dataWareHouseAID = new SimpleStringProperty();
    private final StringProperty productID = new SimpleStringProperty();
    private final StringProperty qty = new SimpleStringProperty();
    private final IntegerProperty id_Employee = new SimpleIntegerProperty();
    private final IntegerProperty supplierID = new SimpleIntegerProperty();
    private final StringProperty remark = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> time = new SimpleObjectProperty<>();
    private final StringProperty transFerGroupID = new SimpleStringProperty();
    private final StringProperty lastUser = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> lastTime = new SimpleObjectProperty<>();
    private final StringProperty location = new SimpleStringProperty();

    public HistoryWareHouseFX() {
    }

    public StringProperty getHistoryAID() {
        return historyAID;
    }

    public StringProperty getDataWareHouseAID() {
        return dataWareHouseAID;
    }

    public StringProperty getQty() {
        return qty;
    }

    public IntegerProperty getId_Employee() {
        return id_Employee;
    }

    public IntegerProperty getSupplierID() {
        return supplierID;
    }

    public StringProperty getRemark() {
        return remark;
    }

    public void setTime(LocalDate date) {
        time.set(date);
    }

    public ObjectProperty<LocalDate> getTime() {
        return time;
    }

    public StringProperty getTransFerGroupID() {
        return transFerGroupID;
    }

    public StringProperty getLastUser() {
        return lastUser;
    }

    public void setLastTime(LocalDate date) {
        lastTime.set(date);
    }

    public ObjectProperty<LocalDate> getLastTime() {
        return lastTime;
    }

    public StringProperty getLocation() {
        return location;
    }

    // ===== helper parse int (dùng khi paste Excel)
    public void setIntSafe(IntegerProperty prop, String value) {
        try {
            prop.set(Integer.parseInt(value));
        } catch (Exception e) {
            prop.set(0);
        }
    }

    public StringProperty getProductID() {
        return productID;
    }

}
