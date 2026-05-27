package com.phuthanh.helper;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.*;
// import java.text.DecimalFormat;
import java.util.*;

import com.phuthanh.Main;
import com.phuthanh.model.info.Vehicle;
import com.phuthanh.model.warehouse.DrawerItem;

public class DbTableHelper {
    private final FunctionHelper functionHelper = new FunctionHelper();

    /** --- CHUNG: Tạo cột TableView từ ResultSet --- */
    public void createColumns(
            TableView<ObservableList<String>> table,
            ResultSet rs,
            Map<String, String> columnMap,
            List<String> hiddenCols) throws SQLException {

        table.getColumns().clear();
        ResultSetMetaData meta = rs.getMetaData();
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

            // 🔥 NẾU LÀ CỘT LINK → custom cell
            if (isLinkColumn(colName)) {
                column.setCellFactory(col -> new TableCell<>() {

                    private final Hyperlink hyperlink = new Hyperlink();

                    {
                        hyperlink.setOnAction(e -> {
                            String url = hyperlink.getText();
                            if (url != null && !url.isBlank()) {
                                Main.getHostServicesInstance().showDocument(url);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String url, boolean empty) {
                        super.updateItem(url, empty);

                        if (empty || url == null || url.isBlank()) {
                            setGraphic(null);
                        } else {
                            hyperlink.setText(url);
                            setGraphic(hyperlink);
                        }
                    }
                });
            }

            // ẩn cột nếu cần
            if (hiddenCols != null && hiddenCols.contains(colName)) {
                column.setVisible(false);
            }

            table.getColumns().add(column);
        }
    }

    private boolean isLinkColumn(String colName) {
        return colName.equalsIgnoreCase("Img1")
                || colName.equalsIgnoreCase("Img2")
                || colName.equalsIgnoreCase("Img3");
    }

    /** --- CHUNG: Thêm dữ liệu từ ResultSet vào ObservableList --- */
    public ObservableList<ObservableList<String>> addData(ResultSet rs) throws SQLException {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        int colCount = rs.getMetaData().getColumnCount();
        ResultSetMetaData rsmd = rs.getMetaData();
        DbInfoHelper dbInfoHelper = new DbInfoHelper();

        // List<Location> locations = dbInfoHelper.getAllLocation();
        List<Vehicle> vehicles = dbInfoHelper.getAllVehicels();
        // DecimalFormat numberFormat = new DecimalFormat("#,###");

        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();

            for (int i = 1; i <= colCount; i++) {
                Object value = rs.getObject(i);
                String val = (value == null) ? "" : value.toString();

                if ("VehicleTypeID".equalsIgnoreCase(rsmd.getColumnName(i))) {
                    val = functionHelper.convertVehicle(val, vehicles);
                }

                row.add(val);
            }

            data.add(row);
        }

        return data;
    }

    /** --- Load table bất kỳ --- */
    public ObservableList<ObservableList<String>> loadTable(TableView<ObservableList<String>> table,
            String tableName) {
        String query = "SELECT * FROM " + tableName;
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            createColumns(table, rs, null, null);
            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /** --- Load table có điều kiện --- */
    public ObservableList<ObservableList<String>> loadTableByFilter(TableView<ObservableList<String>> table,
            String tableName, String columnFilter, String filterValue) {
        String query = "SELECT * FROM " + tableName + " WHERE " + columnFilter + " = '" + filterValue + "'";
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            createColumns(table, rs, null, null);
            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /** --- Load table với columnMap và ẩn cột --- */
    public ObservableList<ObservableList<String>> loadTableConvert(TableView<ObservableList<String>> table,
            String tableName) {
        // String query = "SELECT * FROM " + tableName + " ";
        String query = "SELECT * FROM " + tableName + " ORDER BY LastTime DESC, ProductID DESC";
        // String query = "SELECT * FROM " + tableName + " ";
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<ObservableList<String>> loadTableConvertSheet(TableView<ObservableList<String>> table,
            String tableName) {
        // String query = "SELECT * FROM " + tableName + " ";
        String query = "SELECT * FROM " + tableName + " ORDER BY LastTime DESC";
        // String query = "SELECT * FROM " + tableName + " ";
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<ObservableList<String>> loadTableHistoryConvert(
            TableView<ObservableList<String>> table,
            String tableName, String fromDate, String toDate) {
        String query = "SELECT * FROM " + tableName
                + " WHERE dbo.fnFromDateToDate(Time, '" + fromDate + "', '" + toDate
                + "') = 1 "
                + "ORDER BY Time, LastTime";
        System.out.println(query);
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<ObservableList<String>> loadTableDetailsProductConvert(
            TableView<ObservableList<String>> table,
            String tableName, String fromDate, String toDate) {
        String query = "SELECT * FROM " + tableName
                + " WHERE dbo.fnFromDateToDate(LastTime, '" + fromDate + "', '" + toDate
                + "') = 1 ";
        // + "') = 1 ORDER BY LastTime DESC ";
        System.out.println(query);
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<ObservableList<String>> loadTableDetailsProductToProductAIDConvert(
            TableView<ObservableList<String>> table,
            String tableName, String fromDate, String toDate, String codeAID) {
        String query = "SELECT * FROM " + tableName
                + " WHERE dbo.fnFromDateToDate(LastTime, '" + fromDate + "', '" + toDate
                + "') = 1 AND ProductAID = '" + codeAID + "'  ";
        // + "') = 1 AND ProductAID = '" + codeAID + "' ORDER BY LastTime DESC ";
        System.out.println(query);
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<ObservableList<String>> loadTableGuaranteeConvert(TableView<ObservableList<String>> table,
            String tableName) {
        // String query = "SELECT * FROM " + tableName + " ";
        String query = "SELECT * FROM " + tableName + " ORDER BY LastTime DESC";
        // String query = "SELECT * FROM " + tableName + " ";
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<ObservableList<String>> loadDataTable(TableView<ObservableList<String>> table,
            String sql) {
        System.out.println(sql);
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /** --- Load table với columnMap và ẩn cột --- */
    public ObservableList<ObservableList<String>> loadTableRequestConvert(
            TableView<ObservableList<String>> table,
            String tableName) {
        // String query = "SELECT * FROM " + tableName + " ORDER BY TimeRequest DESC";
        String query = "SELECT * FROM " + tableName + " ORDER BY TimeRequest DESC";
        System.out.println(tableName);
        System.out.println(query);
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /** --- Load chi tiết table theo column = condition --- */
    public ObservableList<ObservableList<String>> loadTableDetails(
            TableView<ObservableList<String>> table,
            String tableName, String column, String condition) {
        String query;
        if (column == null && column == null) {
            query = "SELECT * FROM " + tableName + " ORDER BY LastTime DESC";
        } else {
            query = "SELECT * FROM " + tableName + " WHERE " + column + " = '" + condition + "' ORDER BY LastTime DESC";
        }
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<ObservableList<String>> loadTableDetailsHistory(
            TableView<ObservableList<String>> table,
            String tableName, String column, String condition) {
        String query;
        if (column == null && column == null) {
            query = "SELECT * FROM " + tableName + " ORDER BY Time, LastTime";
        } else {
            query = "SELECT * FROM " + tableName + " WHERE " + column + " = '" + condition + "'"
                    + " ORDER BY Time, LastTime";
        }
        System.out.println(query);
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /** --- Tìm kiếm trong table hiện có --- */
    public ObservableList<ObservableList<String>> searchTable(TableView<ObservableList<String>> table,
            String keyword) {
        ObservableList<ObservableList<String>> items = table.getItems();
        if (items == null)
            return FXCollections.observableArrayList();
        if (keyword == null || keyword.trim().isEmpty())
            return items;

        String lower = keyword.toLowerCase();
        ObservableList<ObservableList<String>> filtered = FXCollections.observableArrayList();
        for (ObservableList<String> row : items) {
            for (String cell : row) {
                if (cell != null && cell.toLowerCase().contains(lower)) {
                    filtered.add(row);
                    break;
                }
            }
        }
        table.setItems(filtered);
        return filtered;
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

    public ObservableList<ObservableList<String>> loadTableConvertAppendix(
            TableView<ObservableList<String>> table, String actionId, String query) {
        // String query = "SELECT * FROM " + tableName + " ";
        // String query = "SELECT * FROM " + tableName + " ORDER BY LastTime DESC";
        // String query = "SELECT * FROM " + tableName + " ";;
        try (Connection conn = DbHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
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
    public ObservableList<ObservableList<String>> loadStatisticalEmployee(
            TableView<ObservableList<String>> table,
            String fromdate, String todate, String IdWareHouse, String typeCondition) {
        // String query = "SELECT * FROM " + tableName + " ";
        String query = """
                        SELECT f.ID_Employee, e.NameEmployee, f.Qty, f.TotalCount
                        FROM dbo.fnDataWareHouseHistoryEmployee(?, ?, ?, ?) AS f
                            LEFT OUTER JOIN dbo.Employee AS e ON f.ID_Employee = e.EmployeeID
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

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
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

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
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

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
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

            createColumns(table, rs, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());
            ObservableList<ObservableList<String>> data = addData(rs);
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

            createColumns(table, rs,
                    functionHelper.getColumnMap(),
                    functionHelper.getColumnListShowhide());

            ObservableList<ObservableList<String>> data = addData(rs);
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

            createColumns(table, rs,
                    functionHelper.getColumnMap(),
                    functionHelper.getColumnListShowhide());

            ObservableList<ObservableList<String>> data = addData(rs);
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

            createColumns(table, rs,
                    functionHelper.getColumnMap(),
                    functionHelper.getColumnListShowhide());

            ObservableList<ObservableList<String>> data = addData(rs);
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

            createColumns(table, rs,
                    functionHelper.getColumnMap(),
                    functionHelper.getColumnListShowhide());

            ObservableList<ObservableList<String>> data = addData(rs);
            table.setItems(data);
            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }

    }


    
}
