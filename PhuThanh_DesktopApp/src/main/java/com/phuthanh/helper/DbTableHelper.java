package com.phuthanh.helper;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import com.phuthanh.model.info.Vehicle;
import com.phuthanh.model.warehouse.DrawerItem;

public class DbTableHelper {
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();

    // TỐI ƯU RAM: Quản lý Listener tĩnh tập trung để gỡ bỏ triệt để, chống rò rỉ
    // RAM đồ họa
    private ListChangeListener<TableColumn<ObservableList<String>, ?>> activeColumnOrderListener;

    /** --- CHUNG: Tạo cột TableView từ ResultSet --- */
    public void createColumns(
            TableView<ObservableList<String>> table,
            ResultSetMetaData meta,
            Map<String, String> columnMap,
            List<String> hiddenCols) throws SQLException {

        // TỐI ƯU RAM: Gỡ listener cũ trước khi dọn cột cũ để GC dọn sạch vùng nhớ
        if (activeColumnOrderListener != null) {
            table.getColumns().removeListener(activeColumnOrderListener);
        }

        // table.getColumns().clear();
        int colCount = meta.getColumnCount();
        List<TableColumn<ObservableList<String>, ?>> columnsToNew = new ArrayList<>(colCount);

        for (int i = 1; i <= colCount; i++) {
            final int colIndex = i - 1;
            String colName = meta.getColumnName(i);
            String displayName = columnMap.getOrDefault(colName, colName);

            TableColumn<ObservableList<String>, String> column = new TableColumn<>(displayName);
            column.setId(colName);
            column.setUserData(colIndex);
            column.setPrefWidth(200);

            column.setCellValueFactory(param -> {
                ObservableList<String> row = param.getValue();
                if (row != null && colIndex < row.size()) {
                    return new ReadOnlyStringWrapper(row.get(colIndex));
                }
                return new ReadOnlyStringWrapper("");
            });

            if (hiddenCols != null && hiddenCols.contains(colName)) {
                column.setVisible(false);
            }

            columnsToNew.add(column);
        }

        table.getColumns().setAll(columnsToNew);

        activeColumnOrderListener = c -> saveColumnOrder(table);
        table.getColumns().addListener(activeColumnOrderListener);

    }

    /**
     * --- CHUNG: Đọc dữ liệu từ ResultSet đổ vào danh sách (Đã triệt tiêu lỗi
     * undefined) ---
     */
    public ObservableList<ObservableList<String>> addData(ResultSet rs, ResultSetMetaData meta) throws SQLException {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        int colCount = meta.getColumnCount();

        // Định dạng số tiền tệ dùng chung tĩnh, giảm thiểu phân mảnh bộ nhớ RAM Heap
        java.text.DecimalFormat currencyFormatter = new java.text.DecimalFormat("#,###");
        List<Vehicle> vehicles = dbInfoHelper.getAllVehicels();
        Map<Integer, String> vehicleMap = new HashMap<>();
        for (Vehicle v : vehicles) {
            vehicleMap.put(v.getVehicleID(), v.getVehicleTypeName().trim());
        }

        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= colCount; i++) {
                String value = rs.getString(i);
                if (value == null) {
                    row.add("");
                    continue;
                }

                String colLabel = meta.getColumnLabel(i);

                if ("VehicleTypeID".equalsIgnoreCase(colLabel)) {
                    value = functionHelper.convertVehicleOptimized(value, vehicleMap);
                    row.add(value);
                }else if (colLabel.contains("Price") || colLabel.contains("Money") || colLabel.contains("Total")) {
                    try {
                        double amount = Double.parseDouble(value);
                        row.add(currencyFormatter.format(amount));
                    } catch (NumberFormatException e) {
                        row.add(value);
                    }
                } else if (colLabel.contains("Date") || colLabel.contains("Time")) {
                    row.add(value.replace(".0", ""));
                } else if ("Qty".equals(colLabel)) {
                    try {
                        double qty = Double.parseDouble(value);
                        if (qty == (long) qty) {
                            row.add(String.valueOf((long) qty));
                        } else {
                            row.add(String.valueOf(qty));
                        }
                    } catch (NumberFormatException e) {
                        row.add(value);
                    }
                } else {
                    row.add(value);
                }
            }
            data.add(row);
        }
        vehicleMap.clear();
        return data;
    }

    /**
     * --- 1. Tải bảng dữ liệu chung (Dùng Try-With-Resources tự đóng kết nối) ---
     */
    public ObservableList<ObservableList<String>> loadDataTable(TableView<ObservableList<String>> table, String query) {
        System.out.println(query);
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

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

    /** --- 2. Tải bảng dữ liệu lịch sử theo kho --- */
    public ObservableList<ObservableList<String>> loadDataTableHistory(
            TableView<ObservableList<String>> table, String tableName,
            String fromdate, String todate, String idwh) {

        String query = "SELECT * FROM " + tableName + " WHERE CAST(LastTime AS DATE) BETWEEN ? AND ?";
        if (idwh != null && !idwh.isBlank()) {
            query += " AND WareHouseSupplierID = ?";
        }
        query += " ORDER BY LastTime DESC, ProductID DESC";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, fromdate);
            ps.setString(2, todate);
            if (idwh != null && !idwh.isBlank()) {
                ps.setString(3, idwh);
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                createColumns(table, metaData, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());

                ObservableList<ObservableList<String>> data = addData(rs, metaData);
                table.setItems(data);
                return data;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /** --- 3. Tải dữ liệu báo cáo thống kê tổng hợp --- */
    public ObservableList<ObservableList<String>> loadDataTableStatistical(
            TableView<ObservableList<String>> table, String fromdate, String todate,
            String idwh, String typeCondition, String LastUser) {

        String query = """
                SELECT p.ProductAID, p.ProductIDMain, p.ProductName, p.ProductModel, p.ProductSide,
                       p.ProductColor, p.ProductUnit, ISNULL(tblSUM.Qty, 0) AS Qty, p.SupplierName,
                       p.ProductNotes, p.LastTime, p.LastUser, p.WareHouseSupplierID, p.NameWareHouse
                FROM vwProduct AS p
                INNER JOIN (SELECT ProductAID, SUM(CASE WHEN Qty < 0 THEN Qty * - 1 ELSE Qty END) AS Qty
                            FROM dbo.fnDataWareHouseHistoryDetails(?,?,?,?) AS f
                            WHERE (LastUser = ?)
                            GROUP BY ProductAID) AS tblSUM ON p.ProductAID = tblSUM.ProductAID
                """;

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, fromdate);
            ps.setString(2, todate);
            if (idwh == null || idwh.isBlank()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, idwh);
            }
            ps.setString(4, typeCondition);
            ps.setString(5, LastUser);

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                createColumns(table, metaData, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());

                ObservableList<ObservableList<String>> data = addData(rs, metaData);
                table.setItems(data);
                return data;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    private void saveColumnOrder(TableView<ObservableList<String>> table) {

    }
    
    /** --- BỔ SUNG SỬA LỖI: Lấy danh sách các kho hàng hiển thị lên Drawer --- */
    public ObservableList<DrawerItem> getDrawerItemsFromDB() {
        ObservableList<DrawerItem> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM WareHouseTable WHERE WareHouseShowHide IS NULL";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new com.phuthanh.model.warehouse.DrawerItem(
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
                        rs.getString("WareHouseDataCheck")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** --- BỔ SUNG SỬA LỖI: Thống kê Đối tác (Partner) --- */
    public ObservableList<ObservableList<String>> loadStatisticalPartner(
            TableView<ObservableList<String>> table, String fromdate, String todate, String idwh,
            String typeCondition) {

        String query = "{call sp_StatisticalPartner(?, ?, ?, ?)}";
        return executeStatisticalStoredProcedure(table, query, fromdate, todate, idwh, typeCondition);
    }

    /** --- BỔ SUNG SỬA LỖI: Thống kê Nhân viên kho (Employee WareHouse) --- */
    public ObservableList<ObservableList<String>> loadStatisticalEmpolyeeWareHouse(
            TableView<ObservableList<String>> table, String fromdate, String todate, String idwh,
            String typeCondition) {

        String query = "{call sp_StatisticalEmployeeWareHouse(?, ?, ?, ?)}";
        return executeStatisticalStoredProcedure(table, query, fromdate, todate, idwh, typeCondition);
    }

    /** --- BỔ SUNG SỬA LỖI: Thống kê Chi tiết Nhân viên (Employee Details) --- */
    public ObservableList<ObservableList<String>> loadStatisticalEmployeeDetails(
            TableView<ObservableList<String>> table, String fromdate, String todate, String idwh, String typeCondition,
            String employeeName) {

        String query = "{call sp_StatisticalEmployeeDetails(?, ?, ?, ?, ?)}";
        return executeStatisticalStoredProcedure(table, query, fromdate, todate, idwh, typeCondition, employeeName);
    }

    /** --- BỔ SUNG SỬA LỖI: Thống kê Chi tiết Sản phẩm (Product Details) --- */
    public ObservableList<ObservableList<String>> loadStatisticalProductDetails(
            TableView<ObservableList<String>> table, String fromdate, String todate, String idwh, String typeCondition,
            String productAID) {

        String query = "{call sp_StatisticalProductDetails(?, ?, ?, ?, ?)}";
        return executeStatisticalStoredProcedure(table, query, fromdate, todate, idwh, typeCondition, productAID);
    }

    /** --- BỔ SUNG SỬA LỖI: Thống kê Chi tiết Đối tác (Partner Details) --- */
    public ObservableList<ObservableList<String>> loadStatisticalPartnerDetails(
            TableView<ObservableList<String>> table, String fromdate, String todate, String idwh, String typeCondition,
            String partnerName) {

        String query = "{call sp_StatisticalPartnerDetails(?, ?, ?, ?, ?)}";
        return executeStatisticalStoredProcedure(table, query, fromdate, todate, idwh, typeCondition, partnerName);
    }

    /** --- BỔ SUNG SỬA LỖI: Thống kê Chi tiết Nhân viên Kho cụ thể --- */
    public ObservableList<ObservableList<String>> loadStatisticalEmployeeWareHouseDetails(
            TableView<ObservableList<String>> table, String fromdate, String todate, String idwh, String typeCondition,
            String employeeWhName) {

        String query = "{call sp_StatisticalEmployeeWareHouseDetails(?, ?, ?, ?, ?)}";
        return executeStatisticalStoredProcedure(table, query, fromdate, todate, idwh, typeCondition, employeeWhName);
    }

    /**
     * * HÀM DÙNG CHUNG TỐI ƯU RAM: Đóng gói việc thực thi Stored Procedure báo cáo
     * Tự động đóng mọi tài nguyên kết nối ngay sau khi load xong bảng dữ liệu.
     */
    private ObservableList<ObservableList<String>> executeStatisticalStoredProcedure(
            TableView<ObservableList<String>> table, String sql, String... params) {

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Map động các tham số đầu vào cho Procedure
            for (int i = 0; i < params.length; i++) {
                if (i == 2 && (params[i] == null || params[i].isBlank())) {
                    ps.setNull(3, java.sql.Types.VARCHAR); // Xử lý Id kho trống
                } else {
                    ps.setString(i + 1, params[i]);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();

                // Khởi tạo cột an toàn (chống leak RAM đồ họa do giữ listener cũ)
                createColumns(table, metaData, functionHelper.getColumnMap(), functionHelper.getColumnListShowhide());

                // Đọc dữ liệu chuyển đổi định dạng tối ưu chuỗi
                ObservableList<ObservableList<String>> data = addData(rs, metaData);
                table.setItems(data);
                return data;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
}