package com.phuthanh.model.info;

public class Vehicle {
    private int VehicleID;
    private String VehicleTypeName;

    public Vehicle(int vehicleID, String vehicleTypeName) {
        VehicleID = vehicleID;
        VehicleTypeName = vehicleTypeName;
    }

    public int getVehicleID() {
        return VehicleID;
    }

    public void setVehicleID(int vehicleID) {
        VehicleID = vehicleID;
    }

    public String getVehicleTypeName() {
        return VehicleTypeName;
    }

    public void setVehicleTypeName(String vehicleTypeName) {
        VehicleTypeName = vehicleTypeName;
    }

    // Giúp ComboBox hiển thị tên quốc gia
    @Override
    public String toString() {
        return VehicleTypeName;
    }
}
