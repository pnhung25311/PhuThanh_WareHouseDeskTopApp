package com.phuthanh.warehouse.EditableTableView.helper;


import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.model.info.*;
import java.util.List;

public class DataCache {
    private static final DbInfoHelper dbInfoHelper = new DbInfoHelper();

    // Toàn bộ dữ liệu tĩnh được đưa vào Cache Bộ nhớ tĩnh (Tải duy nhất 1 lần)
    public static final List<Country> COUNTRIES = dbInfoHelper.getAllCountries();
    public static final List<Manufacturer> MANUFACTURERS = dbInfoHelper.getAllManufacturer();
    public static final List<Supplier> SUPPLIERS = dbInfoHelper.getAllSuppliers();
    public static final List<Unit> UNITS = dbInfoHelper.getAllUnits();
    public static final List<Vehicle> VEHICLES = dbInfoHelper.getAllVehicels();
    public static final List<Business> BUSINESSES = dbInfoHelper.getAllBusiness();
    public static final List<Employee> EMPLOYEES = dbInfoHelper.getAllEmployee();
    public static final List<Bill> BILLS = dbInfoHelper.getAllBills();
    public static final List<Payment> PAYMENTS = dbInfoHelper.getAllPayments();
    
    /**
     * Hàm dùng khi người dùng nhấn nút 'F5/Làm mới dữ liệu' hoặc sau khi thực hiện 
     * thêm/sửa/xóa trực tiếp vào bảng danh mục gốc hệ thống.
     */
    public static void refreshAllCache() {
        COUNTRIES.clear(); COUNTRIES.addAll(dbInfoHelper.getAllCountries());
        MANUFACTURERS.clear(); MANUFACTURERS.addAll(dbInfoHelper.getAllManufacturer());
        SUPPLIERS.clear(); SUPPLIERS.addAll(dbInfoHelper.getAllSuppliers());
        UNITS.clear(); UNITS.addAll(dbInfoHelper.getAllUnits());
        VEHICLES.clear(); VEHICLES.addAll(dbInfoHelper.getAllVehicels());
        BUSINESSES.clear(); BUSINESSES.addAll(dbInfoHelper.getAllBusiness());
        EMPLOYEES.clear(); EMPLOYEES.addAll(dbInfoHelper.getAllEmployee());
        BILLS.clear(); BILLS.addAll(dbInfoHelper.getAllBills());
        PAYMENTS.clear(); PAYMENTS.addAll(dbInfoHelper.getAllPayments());
    }
}