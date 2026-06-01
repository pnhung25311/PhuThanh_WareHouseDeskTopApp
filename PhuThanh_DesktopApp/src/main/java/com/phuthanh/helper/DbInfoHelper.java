package com.phuthanh.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.phuthanh.model.info.Account;
import com.phuthanh.model.info.Bill;
import com.phuthanh.model.info.Business;
import com.phuthanh.model.info.Country;
import com.phuthanh.model.info.Employee;
import com.phuthanh.model.info.Location;
import com.phuthanh.model.info.Manufacturer;
import com.phuthanh.model.info.Payment;
import com.phuthanh.model.info.Purpose;
import com.phuthanh.model.info.Segment;
import com.phuthanh.model.info.Supplier;
import com.phuthanh.model.info.TypeCart;
import com.phuthanh.model.info.Unit;
import com.phuthanh.model.info.Vehicle;
import com.phuthanh.model.warehouse.Cart;
import com.phuthanh.model.warehouse.DataCheck;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.model.warehouse.Guarantee;
import com.phuthanh.model.warehouse.History;
import com.phuthanh.model.warehouse.PartNo;
import com.phuthanh.model.warehouse.Product;
import com.phuthanh.model.warehouse.RequestCart;
import com.phuthanh.model.warehouse.RequestHistoryWareHouse;
import com.phuthanh.model.warehouse.Sheet;
import com.phuthanh.model.warehouse.WareHouse;
import com.phuthanh.store.AppState;

public class DbInfoHelper {
    public List<Country> getAllCountries() {
        List<Country> countries = new ArrayList<>();
        String sql = "SELECT CountryID, Name FROM Country ";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("CountryID");
                String name = rs.getString("Name");
                countries.add(new Country(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return countries;
    }

    public List<Account> getAllAccount() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM Account ";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("AccountID");
                String userName = rs.getString("UserName");
                String passWord = rs.getString("PassWord");
                String fullName = rs.getString("FullName");
                String role = rs.getString("Role");
                int employeeID = rs.getInt("EmployeeID");

                accounts.add(new Account(id, userName, passWord, fullName, role, employeeID));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accounts;
    }

    public List<Unit> getAllUnits() {
        List<Unit> units = new ArrayList<>();
        String sql = "SELECT UnitID, Name FROM Unit ";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("UnitID");
                String name = rs.getString("Name");
                units.add(new Unit(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return units;
    }

    public List<Bill> getAllBills() {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT BillID, Name FROM Bill";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("BillID");
                String name = rs.getString("Name");
                bills.add(new Bill(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bills;
    }

    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT PaymentID, Name FROM Payment";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("PaymentID");
                String name = rs.getString("Name");
                payments.add(new Payment(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT SupplierID, CASE WHEN NameCompany IS NULL Then Name ELSE Name + ' - ' + NameCompany END AS Name, Category, NameCompany, Address, Taxcode, PhoneNumber, Email FROM Supplier ";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("SupplierID");
                int category = rs.getInt("Category");
                String name = rs.getString("Name");
                String nameCompany = rs.getString("NameCompany");
                String address = rs.getString("Address");
                String taxcode = rs.getString("Taxcode");
                String phoneNumber = rs.getString("PhoneNumber");
                String email = rs.getString("Email");
                suppliers.add(new Supplier(id, name, category, nameCompany, address, taxcode, phoneNumber, email));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return suppliers;
    }

    public Supplier getSuppliersByID(String condition) {

        Supplier suppliers = null;

        String sql = """
                SELECT SupplierID, Name, Category, NameCompany,
                       Address, Taxcode, PhoneNumber, Email
                FROM Supplier
                WHERE SupplierID = ?
                """;

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, condition);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    suppliers = new Supplier();

                    suppliers.setSupplierID(rs.getInt("SupplierID"));
                    suppliers.setCategory(rs.getInt("Category"));
                    suppliers.setName(rs.getString("Name"));
                    suppliers.setNameCompany(rs.getString("NameCompany"));
                    suppliers.setAddress(rs.getString("Address"));
                    suppliers.setTaxcode(rs.getString("Taxcode"));
                    suppliers.setPhoneNumber(rs.getString("PhoneNumber"));
                    suppliers.setEmail(rs.getString("Email"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return suppliers;
    }

    public List<Purpose> getAllPurposes() {
        List<Purpose> purposes = new ArrayList<>();
        String sql = "SELECT PurposeID, Name FROM Purpose ";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("PurposeID");
                String name = rs.getString("Name");
                purposes.add(new Purpose(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return purposes;
    }

    public List<TypeCart> getAllTypeCarts() {
        List<TypeCart> typeCarts = new ArrayList<>();
        String sql = "SELECT TypeCartID, Name FROM TypeCart ";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("TypeCartID");
                String name = rs.getString("Name");
                typeCarts.add(new TypeCart(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return typeCarts;
    }

    // public List<Business> getAllBusinesses() {
    // List<Business> businesses = new ArrayList<>();
    // String sql = "SELECT BusinessID, Business, Name FROM Business ";
    // try (Connection conn = DbHelper.getConnection();
    // Statement stmt = conn.createStatement();
    // ResultSet rs = stmt.executeQuery(sql)) {
    // while (rs.next()) {
    // int id = rs.getInt("BusinessID");
    // String business = rs.getString("Business");
    // String name = rs.getString("Name");
    // businesses.add(new Business(id, business, name));
    // }
    // } catch (SQLException e) {
    // e.printStackTrace();
    // }
    // return businesses;
    // }

    public List<Segment> getAllSegments() {
        List<Segment> segments = new ArrayList<>();
        String sql = "SELECT SegmentID, Name FROM Segment ";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("SegmentID");
                String name = rs.getString("Name");
                segments.add(new Segment(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return segments;
    }

    public List<Business> getAllBusiness() {
        List<Business> business = new ArrayList<>();
        String sql = "SELECT BusinessID, CASE WHEN Name IS NULL THEN Business ELSE Business + ' - ' + Name END AS Name FROM Business";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("BusinessID");
                String name = rs.getString("Name");
                business.add(new Business(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return business;
    }

    // 1 là NCC ngước ngoài; 2 là NCC trong nước; 3 là khách hàng; 4 là kho
    public List<Supplier> getAllSuppliersById1() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT SupplierID, CASE WHEN NameCompany IS NULL Then Name ELSE Name + ' - ' + NameCompany END AS Name, Category, NameCompany, Address, Taxcode, PhoneNumber, Email FROM Supplier WHERE Category = 1";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("SupplierID");
                int category = rs.getInt("Category");
                String name = rs.getString("Name");
                String nameCompany = rs.getString("NameCompany");
                String address = rs.getString("Address");
                String taxcode = rs.getString("Taxcode");
                String phoneNumber = rs.getString("PhoneNumber");
                String email = rs.getString("Email");
                suppliers.add(new Supplier(id, name, category, nameCompany, address, taxcode, phoneNumber, email));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return suppliers;
    }

    public List<Supplier> getAllSuppliersById2() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT SupplierID, CASE WHEN NameCompany IS NULL Then Name ELSE Name + ' - ' + NameCompany END AS Name, Category, NameCompany, Address, Taxcode, PhoneNumber, Email FROM Supplier WHERE Category = 2";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("SupplierID");
                int category = rs.getInt("Category");
                String name = rs.getString("Name");
                String nameCompany = rs.getString("NameCompany");
                String address = rs.getString("Address");
                String taxcode = rs.getString("Taxcode");
                String phoneNumber = rs.getString("PhoneNumber");
                String email = rs.getString("Email");
                suppliers.add(new Supplier(id, name, category, nameCompany, address, taxcode, phoneNumber, email));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return suppliers;
    }

    public List<Supplier> getAllSuppliersById3() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT SupplierID, CASE WHEN NameCompany IS NULL Then Name ELSE Name + ' - ' + NameCompany END AS Name, Category, NameCompany, Address, Taxcode, PhoneNumber, Email FROM Supplier WHERE Category = 3";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("SupplierID");
                int category = rs.getInt("Category");
                String name = rs.getString("Name");
                String nameCompany = rs.getString("NameCompany");
                String address = rs.getString("Address");
                String taxcode = rs.getString("Taxcode");
                String phoneNumber = rs.getString("PhoneNumber");
                String email = rs.getString("Email");
                suppliers.add(new Supplier(id, name, category, nameCompany, address, taxcode, phoneNumber, email));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return suppliers;
    }

    public List<Supplier> getAllSuppliersById4() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT SupplierID, CASE WHEN NameCompany IS NULL Then Name ELSE Name + ' - ' + NameCompany END AS Name, Category, NameCompany, Address, Taxcode, PhoneNumber, Email FROM Supplier WHERE Category = 4";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("SupplierID");
                int category = rs.getInt("Category");
                String name = rs.getString("Name");
                String nameCompany = rs.getString("NameCompany");
                String address = rs.getString("Address");
                String taxcode = rs.getString("Taxcode");
                String phoneNumber = rs.getString("PhoneNumber");
                String email = rs.getString("Email");
                suppliers.add(new Supplier(id, name, category, nameCompany, address, taxcode, phoneNumber, email));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return suppliers;
    }

    public List<Manufacturer> getAllManufacturer() {
        List<Manufacturer> manufacturers = new ArrayList<>();
        String sql = "SELECT ManufacturerID, Name FROM Manufacturer ";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("ManufacturerID");
                String name = rs.getString("Name");
                manufacturers.add(new Manufacturer(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return manufacturers;
    }

    public List<Vehicle> getAllVehicels() {
        List<Vehicle> vehicels = new ArrayList<>();
        String sql = "SELECT VehicleTypeID, VehicleTypeName FROM VehicleType ";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("VehicleTypeID");
                String name = rs.getString("VehicleTypeName");
                vehicels.add(new Vehicle(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return vehicels;
    }

    public List<Employee> getAllEmployee() {
        List<Employee> emps = new ArrayList<>();
        String sql = "SELECT * FROM Employee ";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("EmployeeID");
                String name = rs.getString("NameEmployee");
                emps.add(new Employee(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return emps;
    }

    public List<Location> getAllLocation() {
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT * FROM Location ";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("LocationID");
                String name = rs.getString("NameLocation");
                locations.add(new Location(id, name));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return locations;
    }

    public Map<String, String> getAllLocationMap() {
        Map<String, String> locationMap = new HashMap<>();
        String sql = "SELECT * FROM Location";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("LocationID");
                String name = rs.getString("NameLocation");

                if (id != null && name != null) {
                    locationMap.put(id.trim(), name.trim());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return locationMap;
    }

    public Product getProductByID(String productID) {
        Product product = null;
        String sql = "SELECT * FROM vwProduct WHERE ProductID = ?";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String ProductAID = rs.getString("ProductAID");
                System.out.println("ProductAID: " + ProductAID);
                // String ProductIDMain = rs.getString("ProductIDMain");
                String ProductID = rs.getString("ProductID");
                String ID_Keeton = rs.getString("ID_Keeton");
                String ID_Industrial = rs.getString("ID_Industrial");
                String ID_PartNo = rs.getString("ID_PartNo");
                String ID_ReplacedPartNo = rs.getString("ID_ReplacedPartNo");
                // String ID_PartNoDetails = rs.getString("ID_PartNoDetails");
                String NameProduct = rs.getString("NameProduct");
                String Parameter = rs.getString("Parameter");
                String VehicleTypeID = rs.getString("VehicleTypeID");

                String VehicleDetail = rs.getString("VehicleDetail");
                String VehicleCluster = rs.getString("VehicleCluster");
                int ManufacturerID = rs.getInt("ManufacturerID");
                int CountryID = rs.getInt("CountryID");
                int SupplierActualID = rs.getInt("SupplierActualID");
                int SupplierID = rs.getInt("SupplierID");
                int UnitID = rs.getInt("UnitID");
                int SegmentID = rs.getInt("SegmentID");
                int PurposeID = rs.getInt("PurposeID");
                String PurposeName = rs.getString("PurposeName");
                String Img1 = rs.getString("Img1");
                String Img2 = rs.getString("Img2");
                String Img3 = rs.getString("Img3");
                String Remark = rs.getString("Remark");
                Timestamp ts = rs.getTimestamp("LastTime");
                String ManufacturerName = rs.getString("ManufacturerName");
                String UnitName = rs.getString("UnitName");
                String CountryName = rs.getString("CountryName");
                String SegmentName = rs.getString("SegmentName");

                LocalDateTime lastTime = ts != null ? ts.toLocalDateTime() : null;
                product = new Product(ProductAID, "", ProductID, ID_Keeton, ID_Industrial, ID_PartNo, ID_ReplacedPartNo,
                        NameProduct, Parameter, VehicleTypeID, ManufacturerID, CountryID, SupplierActualID, SupplierID,
                        UnitID, VehicleDetail, VehicleCluster, Img1, Img2, Img3, Remark,
                        lastTime != null ? java.sql.Date.valueOf(lastTime.toLocalDate()) : null, CountryName,
                        ManufacturerName, UnitName, SegmentName, SegmentID, PurposeID, PurposeName);
                System.out.println(product);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return product;
    }

    public Product getProductByAID(String productID) {
        Product product = null;
        String sql = "SELECT * FROM vwProduct WHERE ProductAID = ?";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String ProductAID = rs.getString("ProductAID");
                // String ProductIDMain = rs.getString("ProductIDMain");
                String ProductID = rs.getString("ProductID");
                String ID_Keeton = rs.getString("ID_Keeton");
                String ID_Industrial = rs.getString("ID_Industrial");
                String ID_PartNo = rs.getString("ID_PartNo");
                String ID_ReplacedPartNo = rs.getString("ID_ReplacedPartNo");
                // String ID_PartNoDetails = rs.getString("ID_PartNoDetails");
                String NameProduct = rs.getString("NameProduct");
                String Parameter = rs.getString("Parameter");
                String VehicleTypeID = rs.getString("VehicleTypeID");

                String VehicleDetail = rs.getString("VehicleDetail");
                String VehicleCluster = rs.getString("VehicleCluster");
                int ManufacturerID = rs.getInt("ManufacturerID");
                int CountryID = rs.getInt("CountryID");
                int SupplierActualID = rs.getInt("SupplierActualID");
                int SupplierID = rs.getInt("SupplierID");
                int UnitID = rs.getInt("UnitID");
                int SegmentID = rs.getInt("SegmentID");
                String SegmentName = rs.getString("SegmentName");
                int PurposeID = rs.getInt("PurposeID");
                String PurposeName = rs.getString("PurposeName");

                String Img1 = rs.getString("Img1");
                String Img2 = rs.getString("Img2");
                String Img3 = rs.getString("Img3");
                String Remark = rs.getString("Remark");
                Timestamp ts = rs.getTimestamp("LastTime");
                String ManufacturerName = rs.getString("ManufacturerName");
                String UnitName = rs.getString("UnitName");
                String CountryName = rs.getString("CountryName");
                LocalDateTime lastTime = ts != null ? ts.toLocalDateTime() : null;
                product = new Product(ProductAID, "", ProductID, ID_Keeton, ID_Industrial, ID_PartNo, ID_ReplacedPartNo,
                        NameProduct, Parameter, VehicleTypeID, ManufacturerID, CountryID, SupplierActualID, SupplierID,
                        UnitID, VehicleDetail, VehicleCluster, Img1, Img2, Img3, Remark,
                        lastTime != null ? java.sql.Date.valueOf(lastTime.toLocalDate()) : null, CountryName,
                        ManufacturerName, UnitName, SegmentName, SegmentID, PurposeID, PurposeName);
                System.out.println(product);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return product;
    }

    public WareHouse getWareHouseByAID(String _WareHouseAID) {

        WareHouse wh = null;
        DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
        String sql = "SELECT * FROM " + selectedItemFromState.getWareHouseDataBase() + " WHERE DataWareHouseAID = ?";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, _WareHouseAID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String WareHouseAID = rs.getString("DataWareHouseAID");
                String ProductAID = rs.getString("ProductAID");
                Double Qty = rs.getDouble("Qty");
                Double Qty_Expected = rs.getDouble("Qty_Expected");
                String ID_Bill = rs.getString("ID_Bill");
                String LocationID = rs.getString("LocationID");
                Timestamp ts = rs.getTimestamp("LastTime");

                String LastUser = rs.getString("LastUser");
                String Remark = rs.getString("Remark");
                LocalDateTime lastTime = ts != null ? ts.toLocalDateTime() : null;
                wh = new WareHouse(WareHouseAID, ProductAID, Qty, Qty_Expected, ID_Bill, LocationID,
                        lastTime != null ? java.sql.Date.valueOf(lastTime.toLocalDate()) : null, LastUser, Remark);
                System.out.println(wh);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return wh;
    }

    public History geHistoryByAID(String _historyAID) {
        History history = null;
        DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
        String sql = "SELECT * FROM " + selectedItemFromState.getWareHouseDataBaseHistory() + " WHERE HistoryAID = ?";
        System.out.println(sql);

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, _historyAID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String HistoryAID = rs.getString("HistoryAID");
                String DataWareHouseAID = rs.getString("DataWareHouseAID");
                Double Qty = rs.getDouble("Qty");
                int ID_Employee = rs.getInt("ID_Employee");
                int Partner = rs.getInt("Partner");
                String Remark = rs.getString("Remark");
                Timestamp Time = rs.getTimestamp("Time");
                String LastUser = rs.getString("LastUser");
                Timestamp LastTime = rs.getTimestamp("LastTime");
                String TransferGroupID = rs.getString("TransferGroupID");

                LocalDate lastTime = LastTime != null ? LastTime.toLocalDateTime().toLocalDate() : null;
                LocalDate time = Time != null ? Time.toLocalDateTime().toLocalDate() : null;
                history = new History(HistoryAID, DataWareHouseAID, Qty, ID_Employee, Partner, Remark, TransferGroupID,
                        time != null ? java.sql.Date.valueOf(time) : null, LastUser,
                        lastTime != null ? java.sql.Date.valueOf(lastTime) : null);
                System.out.println(history);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }

    public RequestHistoryWareHouse getRequestHistoryWareHouse(String _requestHistoryWareHouseAID) {
        RequestHistoryWareHouse historyWareHouse = null;
        DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
        String sql = "SELECT * FROM " + selectedItemFromState.getWareHouseUpdateHistoryDataBase()
                + " WHERE RequestAID = ?";
        System.out.println(sql);

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, _requestHistoryWareHouseAID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int RequestAID = rs.getInt("RequestAID");
                int HistoryAID = rs.getInt("HistoryAID");
                int DataWareHouseAID = rs.getInt("DataWareHouseAID");
                Double Qty = rs.getDouble("Qty");
                int ID_Employee = rs.getInt("ID_Employee");
                int Partner = rs.getInt("Partner");
                String Remark = rs.getString("Remark");
                Timestamp Time = rs.getTimestamp("Time");
                String TransferGroupID = rs.getString("TransferGroupID");
                String LastUser = rs.getString("LastUser");
                Timestamp LastTime = rs.getTimestamp("LastTime");
                String UserRequest = rs.getString("UserRequest");
                Timestamp TimeRequest = rs.getTimestamp("TimeRequest");
                System.out.println("TimeRequest: " + TimeRequest);
                Boolean Action = rs.getBoolean("Action");
                System.out.println("Action: " + Action);
                String UserConfirm = rs.getString("UserConfirm");
                Timestamp TimeConfirm = rs.getTimestamp("TimeConfirm");
                Timestamp LastTimeOfRequest = rs.getTimestamp("LastTime");

                LocalDate lastTime = LastTime != null ? LastTime.toLocalDateTime().toLocalDate() : null;
                LocalDate time = Time != null ? Time.toLocalDateTime().toLocalDate() : null;
                LocalDate timeRequest = TimeRequest != null ? TimeRequest.toLocalDateTime().toLocalDate() : null;
                LocalDate timeConfirm = TimeConfirm != null ? TimeConfirm.toLocalDateTime().toLocalDate() : null;
                LocalDate lastTimeOfRequest = LastTimeOfRequest != null
                        ? LastTimeOfRequest.toLocalDateTime().toLocalDate()
                        : null;
                historyWareHouse = new RequestHistoryWareHouse(RequestAID, HistoryAID, DataWareHouseAID, Qty,
                        (long) ID_Employee,
                        String.valueOf(Partner), Remark,
                        time != null ? java.sql.Date.valueOf(time) : null, TransferGroupID, LastUser,
                        lastTime != null ? LocalDateTime.of(lastTime, java.time.LocalTime.MIDNIGHT) : null,
                        UserRequest,
                        timeRequest != null ? java.sql.Date.valueOf(timeRequest) : null,
                        UserConfirm,
                        timeConfirm != null ? java.sql.Date.valueOf(timeConfirm) : null,
                        Action,
                        lastTimeOfRequest != null ? java.sql.Date.valueOf(lastTimeOfRequest) : null);
                System.out.println(historyWareHouse);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyWareHouse;
    }

    public PartNo getPartNoByAID(String _partNoAID) {
        PartNo partNo = null;
        String sql = "SELECT * FROM DetailsProduct WHERE PartNoAID = ?";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, _partNoAID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int partNoAID = rs.getInt("PartNoAID");
                String productID = rs.getString("ProductID");
                String idpartNo = rs.getString("ID_PartNo");
                String partNoID = rs.getString("PartNoID");
                String nameEnglish = rs.getString("NameEnglish");
                String nameVietNamese = rs.getString("NameVietNamese");
                double partNoQty = rs.getDouble("PartNoQty");
                String parameter = rs.getString("Parameter");
                String remark = rs.getString("Remark");
                Timestamp ts = rs.getTimestamp("LastTime");
                java.util.Date lastTime = ts != null ? new java.util.Date(ts.getTime()) : null;
                partNo = new PartNo(partNoAID, productID, idpartNo, partNoID, nameEnglish, nameVietNamese, partNoQty,
                        parameter, remark,
                        lastTime);
                System.out.println(partNo);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return partNo;
    }

    public Sheet getSheetByAID(String table, String codeAID) {
        Sheet sheet = null;

        String sql = "SELECT * FROM " + table + " WHERE SheetAID = ?";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codeAID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int sheetAID = rs.getInt("SheetAID");
                String sheetID = rs.getString("SheetID");
                String status = rs.getString("Status");
                String lastUser = rs.getString("LastUser");
                String remark = rs.getString("Remark");
                Timestamp ts = rs.getTimestamp("LastTime");
                Date lastTime = ts != null ? new Date(ts.getTime()) : null;
                sheet = new Sheet(sheetAID, sheetID, status, remark, lastUser, lastTime);
                System.out.println(sheet);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sheet;
    }

    public DataCheck getDataChecktByAID(String table, String codeAID, String proAID) {
        DataCheck dataCheck = null;

        String sql = "SELECT * FROM " + table + " WHERE SheetAID = ? AND ProductAID = ?";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codeAID);
            pstmt.setString(2, proAID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int checkAID = rs.getInt("CheckAID");
                int sheetAID = rs.getInt("SheetAID");
                int productAID = rs.getInt("ProductAID");
                String productID = rs.getString("ProductID");
                String idPartNo = rs.getString("ID_PartNo");
                String nameProduct = rs.getString("NameProduct");

                String nameCountry = rs.getString("NameCountry");
                String nameSupplier = rs.getString("NameSupplier");
                String nameUnit = rs.getString("NameUnit");
                double qtyWareHouse = rs.getDouble("QtyWareHouse");
                double qtyCheck = rs.getDouble("QtyCheck");
                double qtyDifferent = rs.getDouble("QtyDifferent");
                String lastUser = rs.getString("LastUser");
                String remark = rs.getString("Remark");
                Timestamp ts = rs.getTimestamp("LastTime");

                Date lastTime = ts != null ? new Date(ts.getTime()) : null;
                dataCheck = new DataCheck(checkAID, sheetAID, productAID, productID, idPartNo, nameProduct, nameCountry,
                        nameSupplier, nameUnit, qtyWareHouse, qtyCheck, qtyDifferent, remark, lastUser, lastTime);
                System.out.println(dataCheck);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataCheck;
    }

    /** --- Lấy danh sách DrawerItem --- */
    public List<DrawerItem> getWareHouseDataBase() {
        List<DrawerItem> list = new ArrayList<>();
        String sql = "SELECT * FROM WareHouseTable WHERE WareHouseShowHide IS NULL AND WareHouseCategory > 0";
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                DrawerItem item = new DrawerItem(
                        rs.getString("WareHouseID"),
                        rs.getString("NameWareHouse"),
                        rs.getString("WareHouseTable"),
                        rs.getInt("WareHouseCategory"),
                        rs.getString("WareHouseHistory"),
                        rs.getString("WareHouseDataBase"),
                        rs.getString("WareHouseDataBaseHistory"),
                        rs.getString("WareHouseRequest"),
                        rs.getString("WareHouseRequestDataBase"),
                        rs.getString("WareHouseUpdateHistoryDataBase"),
                        rs.getString("WareHouseUpdateHistory"),
                        rs.getString("WareHouseSheetDataBase"),
                        rs.getString("WareHouseCheckDataBase"),
                        rs.getInt("WareHouseSupplierID"),
                        rs.getString("WareHouseSheet"),
                        rs.getString("WareHouseDataCheck"));
                list.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Guarantee getGuaranteeByAID(String guaranteeAID) {

        Guarantee guarantee = null;

        String sql = """
                SELECT * FROM vwGuarantee WHERE GuaranteeAID = ?
                """;

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, guaranteeAID);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {

                guarantee = new Guarantee();

                guarantee.setGuaranteeAID(rs.getString("GuaranteeAID"));
                guarantee.setGuaranteeID(rs.getString("GuaranteeID"));

                // Product Broken
                guarantee.setProductBroken(rs.getString("ProductBroken"));
                guarantee.setProductIDBroken(rs.getString("ProductIDBroken"));
                guarantee.setIdPartNoBroken(rs.getString("ID_PartNoBroken"));
                guarantee.setNameProductBroken(rs.getString("NameProductBroken"));
                guarantee.setCountryNameBroken(rs.getString("CountryNameBroken"));
                guarantee.setUnitNameBroken(rs.getString("UnitNameBroken"));

                // Time
                Timestamp tsStart = rs.getTimestamp("TimeStart");
                Timestamp tsBroken = rs.getTimestamp("TimeBroken");
                Timestamp tsLast = rs.getTimestamp("LastTime");
                Timestamp tsGuarantee = rs.getTimestamp("TimeGuarantee");

                guarantee.setTimeStart(tsStart != null ? tsStart.toLocalDateTime() : null);
                guarantee.setTimeBroken(tsBroken != null ? tsBroken.toLocalDateTime() : null);
                guarantee.setLastTime(tsLast != null ? tsLast.toLocalDateTime() : null);
                guarantee.setTimeGuarantee(tsGuarantee != null ? tsGuarantee.toLocalDateTime() : null);

                guarantee.setTimeUsage(rs.getDouble("TimeUsage"));
                guarantee.setQty(rs.getDouble("Qty"));

                // Reason
                guarantee.setReasonBroken(rs.getString("ReasonBroken"));

                // Product Guarantee
                guarantee.setProductGuarantee(rs.getString("ProductGuarantee"));
                guarantee.setProductIDGuarantee(rs.getString("ProductIDGuarantee"));
                guarantee.setIdPartNoGuarantee(rs.getString("ID_PartNoGuarantee"));
                guarantee.setNameProductGuarantee(rs.getString("NameProductGuarantee"));
                guarantee.setUnitNameGuarantee(rs.getString("UnitNameGuarantee"));
                guarantee.setCountryNameGuarantee(rs.getString("CountryNameGuarantee"));

                // Other
                guarantee.setPartner(rs.getString("Partner"));
                guarantee.setSupplierGuarantee(rs.getInt("SupplierGuarantee"));
                guarantee.setImg1(rs.getString("Img1"));
                guarantee.setImg2(rs.getString("Img2"));
                guarantee.setImg3(rs.getString("Img3"));
                guarantee.setRemark(rs.getString("Remark"));
                guarantee.setLastUser(rs.getString("LastUser"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return guarantee;
    }

    public Cart getCartByAID(int cartAID) {

        String sql = "SELECT * FROM vwCart WHERE CartAID = ?";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cartAID);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return null;

            Cart c = new Cart();

            // ===== CART =====
            c.setCartAID(rs.getInt("CartAID"));
            // c.setCartID(rs.getInt("CartID"));
            c.setAccountID(rs.getInt("AccountID"));
            c.setProductAID(rs.getInt("ProductAID"));
            c.setProductAIDVAT(rs.getInt("ProductAIDVAT"));

            c.setQty(rs.getDouble("Qty"));
            c.setCogs(rs.getDouble("Cogs"));
            c.setPrice(rs.getDouble("Price")); // PriceNET AS Price
            c.setTotal(rs.getDouble("Total"));
            c.setCogs(rs.getDouble("Cogs"));
            c.setPriceVAT(rs.getDouble("PriceVAT"));
            c.setPriceCost(rs.getDouble("PriceCost"));
            c.setGrossPriceVAT(rs.getDouble("GrossPriceVAT"));
            c.setBillID(rs.getInt("BillID"));
            // ===== FK nullable =====
            c.setPaymentID(rs.getInt("PaymentID"));
            // c.setBillID((Integer) rs.getObject("BillID"));
            c.setSourceID(rs.getInt("SourceID"));
            c.setDeliveryID(rs.getInt("DeliveryID"));
            c.setEmployeeID(rs.getInt("EmployeeID"));
            c.setProductAIDVAT(rs.getInt("ProductAIDVAT"));

            c.setTypeCartID(rs.getInt("TypeCartID"));
            c.setStatusID(rs.getInt("StatusID"));
            c.setStatusVAT(rs.getInt("StatusVAT"));
            c.setNameStatusVAT(rs.getString("NameStatusVAT"));
            c.setContractID(rs.getString("ContractID"));
            c.setVehicleTypeID(rs.getString("VehicleTypeID"));
            c.setInvoiceNumber(rs.getString("InvoiceNumber"));
            c.setParameter(rs.getString("Parameter"));

            // ===== DATE =====
            Date d = rs.getDate("DeliveryTime");
            Date r = rs.getDate("ReportDate");
            if (d != null)
                c.setDeliveryTime(d.toLocalDate());
            if (r != null)
                c.setReportDate(r.toLocalDate());

            Timestamp t = rs.getTimestamp("LastTime");
            if (t != null)
                c.setLastTime(t.toLocalDateTime());

            c.setRemark(rs.getString("Remark"));

            // 🔥 NEW từ DataWarehouse
            c.setLocationID(rs.getString("LocationID"));

            // ===== ACCOUNT =====
            c.setCreator(rs.getString("Creator"));

            // ===== PRODUCT =====
            c.setProductID(rs.getString("ProductID")); // p.ProductID AS ProductIDVAT
            c.setProductIDVAT(rs.getString("ProductIDVAT")); // p.ProductID AS ProductIDVAT
            c.setPartNo(rs.getString("ID_PartNo"));
            c.setNameProduct(rs.getString("NameProduct"));
            c.setManufacturerID(rs.getInt("ManufacturerID"));
            c.setManufacturerName(rs.getString("ManufacturerName"));
            c.setCountryID(rs.getInt("CountryID"));
            c.setCountryName(rs.getString("CountryName"));
            c.setUnitID(rs.getInt("UnitID"));
            c.setUnitName(rs.getString("UnitName"));

            c.setBusinessID(rs.getInt("BusinessID"));

            // ===== PRODUCT VAT (đúng query) =====
            c.setProductIDVAT(rs.getString("ProductID"));

            // ===== JOIN NAME =====
            c.setNamePayment(rs.getString("NamePayment"));
            c.setNameBill(rs.getString("NameBill"));
            c.setNameSource(rs.getString("NameSource"));
            c.setNameDelivery(rs.getString("NameDelivery"));
            c.setProponent(rs.getString("Proponent"));
            c.setNameStatus(rs.getString("NameStatus"));

            return c;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Double> getSumaryHistory(String dataWarehouseAID, String table, String fromDate, String toDate) {

        Map<String, Double> result = new HashMap<>();

        StringBuilder sql = new StringBuilder("""
                    SELECT
                        SUM(CASE WHEN Qty > 0 THEN Qty ELSE 0 END) AS TotalImport,
                        SUM(CASE WHEN Qty < 0 THEN Qty ELSE 0 END) * -1 AS TotalExport
                    FROM %s
                    WHERE 1=1
                """.formatted(table));

        if (dataWarehouseAID != null && !dataWarehouseAID.isBlank()) {
            sql.append(" AND DataWareHouseAID = ?");
        } else {
            sql.append(" AND dbo.fnFromDateToDate(Time, '" + fromDate + "', '" + toDate
                    + "') = 1 "); // Nếu không có DataWarehouseAID, không trả về kết quả nào
        }

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (dataWarehouseAID != null && !dataWarehouseAID.isBlank()) {
                ps.setString(1, dataWarehouseAID);
            }

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                result.put("import", rs.getDouble("TotalImport"));
                result.put("export", rs.getDouble("TotalExport"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public int getCartCountFromDB() {
        int count = 0;

        String sql = "SELECT COUNT(*) FROM Cart WHERE Status = 0 AND (DeliveryID IN (41,42,43,44,45,236) OR SourceID IN (41,42,43,44,45,236)) ";
        // nếu có user thì dùng:
        // String sql = "SELECT COUNT(*) FROM Cart WHERE user_id = ?";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // nếu có user thì set:
            // ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    public RequestCart getRequestCartByAID(int requestAID) {

        String sql = "SELECT * FROM vwRequestCart WHERE RequestAID = ?";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestAID);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return null;

            RequestCart r = new RequestCart();

            // ===== BASIC =====
            r.setRequestAID(rs.getInt("RequestAID"));
            r.setCartAID(rs.getInt("CartAID"));
            // r.setCartID(rs.getString("CartID"));
            r.setAccountID(rs.getInt("AccountID"));
            r.setCreator(rs.getString("Creator"));

            // ===== PRODUCT =====
            r.setProductAID(rs.getInt("ProductAID"));
            r.setProductID(rs.getString("ProductID"));
            r.setProductAIDVAT(rs.getInt("ProductAIDVAT"));
            r.setProductIDVAT(rs.getString("ProductIDVAT"));

            r.setPartNo(rs.getString("ID_PartNo"));
            r.setNameProduct(rs.getString("NameProduct"));
            r.setManufacturerID(getInteger(rs, "ManufacturerID"));
            r.setManufacturerName(rs.getString("ManufacturerName"));
            r.setCountryID(getInteger(rs, "CountryID"));
            r.setCountryName(rs.getString("CountryName"));
            r.setUnitID(getInteger(rs, "UnitID"));
            r.setUnitName(rs.getString("UnitName"));
            r.setVehicleTypeID(rs.getString("VehicleTypeID"));
            r.setParameter(rs.getString("Parameter"));

            r.setStatusVAT(getInteger(rs, "StatusVAT"));
            r.setNameStatusVAT(rs.getString("NameStatusVAT"));
            r.setContractID(rs.getString("ContractID"));
            r.setInvoiceNumber(rs.getString("InvoiceNumber"));

            // ===== PRICE (FIXED) =====
            r.setQty(rs.getDouble("Qty"));
            r.setCogs(rs.getDouble("Cogs"));
            r.setPrice(rs.getDouble("PriceNET")); // ✔ đúng cột
            r.setTotal(rs.getDouble("Total"));
            r.setCogs(rs.getDouble("Cogs"));
            r.setPriceVAT(rs.getDouble("PriceVAT"));
            r.setGrossPriceVAT(rs.getDouble("GrossPriceVAT"));
            r.setPriceCost(rs.getDouble("PriceCost"));

            // ===== PAYMENT =====
            r.setPaymentID(getInteger(rs, "PaymentID"));
            r.setNamePayment(rs.getString("NamePayment"));

            r.setBillID(getInteger(rs, "BillID"));
            r.setNameBill(rs.getString("NameBill"));

            r.setSourceID(getInteger(rs, "SourceID"));
            r.setNameSource(rs.getString("NameSource"));

            r.setDeliveryID(getInteger(rs, "DeliveryID"));
            r.setNameDelivery(rs.getString("NameDelivery"));

            r.setEmployeeID(getInteger(rs, "EmployeeID"));
            r.setProponent(rs.getString("Proponent"));

            // ===== STATUS =====
            r.setStatusID(rs.getInt("StatusID"));
            r.setNameStatus(rs.getString("NameStatus"));
            r.setBusinessID(rs.getInt("BusinessID"));
            r.setBusinessName(rs.getString("BusinessName"));
            r.setTypeCartID(rs.getInt("TypeCartID"));
            r.setTypeCartName(rs.getString("TypeCartName"));

            // ===== TIME =====
            Date delivery = rs.getDate("DeliveryTime");
            Date report = rs.getDate("ReportDate");
            if (delivery != null)
                r.setDeliveryTime(delivery.toLocalDate());

            if (report != null)
                r.setReportDate(report.toLocalDate());

            r.setRemark(rs.getString("Remark"));

            Timestamp last = rs.getTimestamp("LastTime");
            if (last != null)
                r.setLastTime(last.toLocalDateTime());

            // ===== REQUEST =====
            r.setUserRequestID(getInteger(rs, "UserRequestID"));
            r.setNameRequest(rs.getString("NameRequest"));

            Timestamp tr = rs.getTimestamp("TimeRequest");
            if (tr != null)
                r.setTimeRequest(tr.toLocalDateTime());

            // ===== CONFIRM =====
            r.setUserConfirmID(getInteger(rs, "UserConfirmID"));
            r.setNameConfirm(rs.getString("NameConfirm"));

            Timestamp tc = rs.getTimestamp("TimeConfirm");
            if (tc != null)
                r.setTimeConfirm(tc.toLocalDateTime());

            // ===== ACTION (FIXED NAME) =====
            r.setActionID(getInteger(rs, "ActionID"));
            r.setNameAction(rs.getString("NameAction"));

            r.setRemarkOfRequest(rs.getString("RemarkOfRequest"));

            Timestamp lr = rs.getTimestamp("LastTimeOfRequest");
            if (lr != null)
                r.setLastTimeOfRequest(lr.toLocalDateTime());

            return r;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Integer getInteger(ResultSet rs, String column) throws SQLException {
        Object obj = rs.getObject(column);

        if (obj == null)
            return null;

        if (obj instanceof Integer)
            return (Integer) obj;

        if (obj instanceof Long)
            return ((Long) obj).intValue();

        if (obj instanceof Short)
            return ((Short) obj).intValue();

        if (obj instanceof Byte)
            return ((Byte) obj).intValue();

        // ⭐ FIX LỖI CỦA BẠN: BIT → Boolean
        if (obj instanceof Boolean)
            return (Boolean) obj ? 1 : 0;

        if (obj instanceof String)
            return Integer.parseInt((String) obj);

        if (obj instanceof Number)
            return ((Number) obj).intValue();

        throw new SQLException("Cannot convert column " + column + " to Integer. Type = " + obj.getClass());
    }

    public String GenerateProductIDMainCode() {
        String productIDMain = "";
        String sql = "SELECT dbo.fn_GenerateProductIDMainCode() AS ProductIDMain;";

        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productIDMain = rs.getString("ProductIDMain");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productIDMain;
    }

    public boolean checkCriteriaProduct(String partNoOrParameter,
            int supplierID,
            int supplierActualID,
            int manufacturerID,
            int countryID,
            int unitID) {

        String sql = """
                SELECT 1 AS CheckCriteria
                FROM Product
                WHERE (ID_PartNo = ? OR Parameter = ?)
                  AND supplierID = ?
                  AND supplierActualID = ?
                  AND manufacturerID = ?
                  AND countryID = ?
                  AND unitID = ? AND LEN(ProductID) = 7
                """;

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // set parameters
            ps.setString(1, partNoOrParameter);
            ps.setString(2, partNoOrParameter);
            ps.setInt(3, supplierID);
            ps.setInt(4, supplierActualID);
            ps.setInt(5, manufacturerID);
            ps.setInt(6, countryID);
            ps.setInt(7, unitID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("CheckCriteria");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // không tìm thấy thì false
    }
}