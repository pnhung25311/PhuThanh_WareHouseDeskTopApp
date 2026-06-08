package com.phuthanh.helper;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.*;
// import java.text.DecimalFormat;
import java.util.*;
import com.phuthanh.model.info.Vehicle;
import com.phuthanh.model.warehouse.DrawerItem;

public class DbTableHelper {
    private final FunctionHelper functionHelper = new FunctionHelper();

    /** --- CHUNG: Tạo cột TableView từ ResultSet --- */
    public void createColumns(
            TableView<ObservableList<String>> table,
            ResultSetMetaData meta,
            Map<String, String> columnMap,
            List<String> hiddenCols) throws SQLException {

        table.getColumns().clear();
        // ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        for (int i = 1; i <= colCount; i++) {
            final int colIndex = i - 1;
            String colName = meta.getColumnName(i);

            String displayName = columnMap != null && columnMap.get(colName) != null
                    ? columnMap.get(colName)
                    : colName;

            TableColumn<ObservableList<String>, String> column = new TableColumn<>(displayName);

            // lấy data theo index
            column.setCellValueFactory(param -> {
                String value = param.getValue().get(colIndex);
                return new ReadOnlyStringWrapper(value != null ? value : "");
            });
            // ẩn cột nếu cần
            if (hiddenCols != null && hiddenCols.contains(colName)) {
                column.setVisible(false);
            }
            table.getColumns().add(column);

        }
        columnMap.clear();

    }

    /** --- CHUNG: Thêm dữ liệu từ ResultSet vào ObservableList --- */
    public ObservableList<ObservableList<String>> addData(ResultSet rs, ResultSetMetaData rsmd) throws SQLException {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        int colCount = rs.getMetaData().getColumnCount();
        DbInfoHelper dbInfoHelper = new DbInfoHelper();

        // Cache vehicle lookup data once, not per row
        List<Vehicle> vehicles = dbInfoHelper.getAllVehicels();
        Map<Integer, String> vehicleMap = new HashMap<>();
        for (Vehicle v : vehicles) {
            vehicleMap.put(v.getVehicleID(), v.getVehicleTypeName().trim());
        }

        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();

            for (int i = 1; i <= colCount; i++) {
                Object value = rs.getObject(i);
                String val = (value == null) ? "" : value.toString();

                if ("VehicleTypeID".equalsIgnoreCase(rsmd.getColumnName(i))) {
                    val = functionHelper.convertVehicleOptimized(val, vehicleMap);
                }

                row.add(val);
            }

            data.add(row);
        }

        return data;
    }

    public ObservableList<ObservableList<String>> loadDataTable(TableView<ObservableList<String>> table,
            String sql) {
        System.out.println(sql);
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();

            createColumns(table, meta, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs, meta);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /** --- Lấy danh sách DrawerItem --- */
    public List<DrawerItem> getDrawerItemsFromDB() {
        List<DrawerItem> list = new ArrayList<>();
        String sql = "SELECT * FROM WareHouseTable WHERE WareHouseShowHide IS NULL";
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

    /*
     * typeCondition = null là tất cả
     * typeCondition = IMPORT là nhập hàng
     * typeCondition = EXPORT là xuất hàng
     */
    public ObservableList<ObservableList<String>> loadStatisticalEmployee(
            TableView<ObservableList<String>> table,
            String fromdate, String todate,
            String IdWareHouse,
            String typeCondition) {

        String query = """
                SELECT f.ID_Employee,
                       e.NameEmployee,
                       f.Qty,
                       f.TotalCount
                FROM dbo.fnDataWareHouseHistoryEmployee(?, ?, ?, ?) AS f
                     LEFT JOIN dbo.Employee e
                        ON f.ID_Employee = e.EmployeeID
                ORDER BY f.Qty DESC
                """;

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDate(1, java.sql.Date.valueOf(fromdate));
            ps.setDate(2, java.sql.Date.valueOf(todate));

            if (IdWareHouse == null || IdWareHouse.isBlank()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, IdWareHouse);
            }

            ps.setString(4, typeCondition);

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            createColumns(
                    table,
                    metaData,
                    functionHelper.getColumnMap(),
                    functionHelper.getColumnListShowhide());

            ObservableList<ObservableList<String>> data = addData(rs, metaData);

            table.setItems(data);

            return data;

        } catch (Exception e) {
            System.err.println("fromdate = " + fromdate);
            System.err.println("todate = " + todate);
            System.err.println("warehouse = " + IdWareHouse);
            System.err.println("type = " + typeCondition);

            e.printStackTrace();

            return FXCollections.observableArrayList();
        }
    }

    /*
     * typeCondition = null là tất cả
     * typeCondition = IMPORT là nhập hàng
     * typeCondition = EXPORT là xuất hàng
     */
    public ObservableList<ObservableList<String>> loadStatisticalProduct(
            TableView<ObservableList<String>> table,
            String fromdate, String todate, String IdWareHouse, String typeCondition) {
        // String query = "SELECT * FROM " + tableName + " ";
        String query = """
                        SELECT        f.ProductAID, p.ProductID, f.Qty, f.TotalCount
                        FROM            dbo.fnDataWareHouseHistoryProduct(?, ?, ?, ?) AS f LEFT OUTER JOIN
                                                dbo.Product AS p ON f.ProductAID = p.ProductAID
                        ORDER BY f.Qty DESC
                """;

        // String query = "SELECT * FROM " + tableName + " ";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, fromdate);
            ps.setString(2, todate);
            if (IdWareHouse == null || IdWareHouse.isBlank()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, IdWareHouse);
            }
            ps.setString(4, typeCondition);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            createColumns(table, metaData, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs, metaData);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<ObservableList<String>> loadStatisticalPartner(
            TableView<ObservableList<String>> table,
            String fromdate, String todate, String IdWareHouse, String typeCondition) {
        // String query = "SELECT * FROM " + tableName + " ";
        String query = """
                        SELECT        f.SupplierID, s.Name, f.Qty, f.TotalCount
                        FROM            dbo.fnDataWareHouseHistoryPartnerSupplier(?, ?, ?, ?) AS f LEFT OUTER JOIN
                                        dbo.Supplier AS s ON f.SupplierID = s.SupplierID
                        ORDER BY f.Qty DESC
                """;

        // String query = "SELECT * FROM " + tableName + " ";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, fromdate);
            ps.setString(2, todate);
            if (IdWareHouse == null || IdWareHouse.isBlank()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, IdWareHouse);
            }
            ps.setString(4, typeCondition);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            createColumns(table, metaData, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs, metaData);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<ObservableList<String>> loadStatisticalEmpolyeeWareHouse(
            TableView<ObservableList<String>> table,
            String fromdate, String todate, String IdWareHouse, String typeCondition) {
        // String query = "SELECT * FROM " + tableName + " ";
        String query = """
                SELECT LastUser AS ProductAID, LastUser, TotalCount, Qty
                FROM dbo.fnDataWareHouseHistoryEmployeeWareHouse(?, ?, ?, ?) AS f
                ORDER BY f.Qty DESC
                                """;

        // String query = "SELECT * FROM " + tableName + " ";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, fromdate);
            ps.setString(2, todate);
            if (IdWareHouse == null || IdWareHouse.isBlank()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, IdWareHouse);
            }
            ps.setString(4, typeCondition);
            ResultSet rs = ps.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            createColumns(table, metaData, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs, metaData);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /*
     * typeCondition = null là tất cả
     * typeCondition = IMPORT là nhập hàng
     * typeCondition = EXPORT là xuất hàng
     */
    public ObservableList<ObservableList<String>> loadStatisticalEmployeeDetails(
            TableView<ObservableList<String>> table,
            String fromdate, String todate, String employeeId, String typeCondition, String idwh) {
        // String query = "SELECT * FROM " + tableName + " ";

        // String query = """
        // SELECT *
        // FROM dbo.fnDataWareHouseHistoryDetails(?, ?, ?, ?) AS f
        // WHERE ID_Employee = ?
        // ORDER BY Time, LastTime
        // """;

        String query = """
                SELECT        p.ProductAID, p.ProductID, p.ID_Keeton, p.ID_Industrial, p.ID_PartNo, p.ID_ReplacedPartNo, p.NameProduct, p.Parameter, p.VehicleTypeID, p.VehicleDetail, p.VehicleCluster, p.Remark, p.ManufacturerName, p.CountryName,
                                         p.SupplierActualName, p.SupplierName, p.UnitName, tblSUM.Qty
                FROM            dbo.vwProduct AS p RIGHT OUTER JOIN
                                             (SELECT        ProductAID, SUM(CASE WHEN Qty < 0 THEN Qty * - 1 ELSE Qty END) AS Qty
                                               FROM            dbo.fnDataWareHouseHistoryDetails(?,?,?,?) AS f
                                               WHERE        (ID_Employee = ?)
                                               GROUP BY ProductAID) AS tblSUM ON p.ProductAID = tblSUM.ProductAID
                                """;

        // String query = "SELECT * FROM " + tableName + " ";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, fromdate);
            ps.setString(2, todate);
            if (idwh == null || idwh.isBlank()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, idwh);
            }

            // qtyType dùng 3 lần
            ps.setString(4, typeCondition);
            ps.setString(5, employeeId);

            ResultSet rs = ps.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            createColumns(table, metaData, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());

            ObservableList<ObservableList<String>> data = addData(rs, metaData);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }

    }

    /*
     * typeCondition = null là tất cả
     * typeCondition = IMPORT là nhập hàng
     * typeCondition = EXPORT là xuất hàng
     */
    public ObservableList<ObservableList<String>> loadStatisticalProductDetails(
            TableView<ObservableList<String>> table,
            String fromdate, String todate, String ProductID, String typeCondition, String idwh) {
        // String query = "SELECT * FROM " + tableName + " ";

        String query = """
                SELECT        p.ProductAID, p.ProductID, p.ID_Keeton, p.ID_Industrial, p.ID_PartNo, p.ID_ReplacedPartNo, p.NameProduct, p.Parameter, p.VehicleTypeID, p.VehicleDetail, p.VehicleCluster, p.Remark, p.ManufacturerName, p.CountryName,
                                         p.SupplierActualName, p.SupplierName, p.UnitName, tblSUM.Qty
                FROM            dbo.vwProduct AS p RIGHT OUTER JOIN
                                             (SELECT        ProductAID, SUM(CASE WHEN Qty < 0 THEN Qty * - 1 ELSE Qty END) AS Qty
                                               FROM            dbo.fnDataWareHouseHistoryDetails(?,?,?,?) AS f
                                               WHERE        (ProductAID = ?)
                                               GROUP BY ProductAID) AS tblSUM ON p.ProductAID = tblSUM.ProductAID
                """;

        // String query = "SELECT * FROM " + tableName + " ";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, fromdate);
            ps.setString(2, todate);
            if (idwh == null || idwh.isBlank()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, idwh);
            }

            // qtyType dùng 3 lần
            ps.setString(4, typeCondition);
            ps.setString(5, ProductID);

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            createColumns(table, metaData, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());

            ObservableList<ObservableList<String>> data = addData(rs, metaData);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }

    }

    /*
     * typeCondition = null là tất cả
     * typeCondition = IMPORT là nhập hàng
     * typeCondition = EXPORT là xuất hàng
     */
    public ObservableList<ObservableList<String>> loadStatisticalPartnerDetails(
            TableView<ObservableList<String>> table,
            String fromdate, String todate, String SupplierID, String typeCondition, String idwh) {
        // String query = "SELECT * FROM " + tableName + " ";

        String query = """
                SELECT        p.ProductAID, p.ProductID, p.ID_Keeton, p.ID_Industrial, p.ID_PartNo, p.ID_ReplacedPartNo, p.NameProduct, p.Parameter, p.VehicleTypeID, p.VehicleDetail, p.VehicleCluster, p.Remark, p.ManufacturerName, p.CountryName,
                                         p.SupplierActualName, p.SupplierName, p.UnitName, tblSUM.Qty
                FROM            dbo.vwProduct AS p RIGHT OUTER JOIN
                                             (SELECT        ProductAID, SUM(CASE WHEN Qty < 0 THEN Qty * - 1 ELSE Qty END) AS Qty
                                               FROM            dbo.fnDataWareHouseHistoryDetails(?,?,?,?) AS f
                                               WHERE        (SupplierID = ?)
                                               GROUP BY ProductAID) AS tblSUM ON p.ProductAID = tblSUM.ProductAID
                """;

        // String query = "SELECT * FROM " + tableName + " ";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, fromdate);
            ps.setString(2, todate);
            if (idwh == null || idwh.isBlank()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, idwh);
            }

            // qtyType dùng 3 lần
            ps.setString(4, typeCondition);
            ps.setString(5, SupplierID);

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            createColumns(table, metaData, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());

            ObservableList<ObservableList<String>> data = addData(rs, metaData);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }

    }

    /*
     * typeCondition = null là tất cả
     * typeCondition = IMPORT là nhập hàng
     * typeCondition = EXPORT là xuất hàng
     */
    public ObservableList<ObservableList<String>> loadStatisticalEmployeeWareHouseDetails(
            TableView<ObservableList<String>> table,
            String fromdate, String todate, String LastUser, String typeCondition, String idwh) {
        // String query = "SELECT * FROM " + tableName + " ";

        String query = """
                SELECT        p.ProductAID, p.ProductID, p.ID_Keeton, p.ID_Industrial, p.ID_PartNo, p.ID_ReplacedPartNo, p.NameProduct, p.Parameter, p.VehicleTypeID, p.VehicleDetail, p.VehicleCluster, p.Remark, p.ManufacturerName, p.CountryName,
                                         p.SupplierActualName, p.SupplierName, p.UnitName, tblSUM.Qty
                FROM            dbo.vwProduct AS p RIGHT OUTER JOIN
                                             (SELECT        ProductAID, SUM(CASE WHEN Qty < 0 THEN Qty * - 1 ELSE Qty END) AS Qty
                                               FROM            dbo.fnDataWareHouseHistoryDetails(?,?,?,?) AS f
                                               WHERE        (LastUser = ?)
                                               GROUP BY ProductAID) AS tblSUM ON p.ProductAID = tblSUM.ProductAID
                """;

        // String query = "SELECT * FROM " + tableName + " ";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, fromdate);
            ps.setString(2, todate);
            if (idwh == null || idwh.isBlank()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, idwh);
            }

            // qtyType dùng 3 lần
            ps.setString(4, typeCondition);
            ps.setString(5, LastUser);

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            createColumns(table, metaData, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());

            ObservableList<ObservableList<String>> data = addData(rs, metaData);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }

    }

}
