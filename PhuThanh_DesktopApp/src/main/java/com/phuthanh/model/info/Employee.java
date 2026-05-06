package com.phuthanh.model.info;

public class Employee {
    private int EmployeeID;
    private String NameEmployee;

    public Employee(int employeeID, String nameEmployee) {
        EmployeeID = employeeID;
        NameEmployee = nameEmployee;
    }

    public int getEmployeeID() {
        return EmployeeID;
    }

    public void setEmployeeID(int employeeID) {
        EmployeeID = employeeID;
    }

    public String getNameEmployee() {
        return NameEmployee;
    }

    public void setNameEmployee(String nameEmployee) {
        NameEmployee = nameEmployee;
    }

    // Giúp ComboBox hiển thị tên quốc gia
    @Override
    public String toString() {
        return NameEmployee;
    }
}
