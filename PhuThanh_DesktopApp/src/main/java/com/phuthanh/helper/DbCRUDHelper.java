package com.phuthanh.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DbCRUDHelper {

    /**
     * Hàm insert dùng chung cho mọi bảng
     *
     * @param tableName Tên bảng
     * @param columns   Danh sách cột (ví dụ: "name", "age", "country_id")
     * @param values    Danh sách giá trị tương ứng
     * @return số dòng bị ảnh hưởng
     * @throws SQLException
     */
    public int insert(String tableName, List<String> columns, List<Object> values) throws SQLException {
        if (columns.size() != values.size()) {
            System.out.println("columns: "+columns.size());
            System.out.println("Value: "+values.size());
            for (int i = 0; i < columns.size(); i++) {
                System.out.println(columns.get(i)+"=="+values.get(i));
            }
            throw new IllegalArgumentException("Số cột và số giá trị phải bằng nhau");
        }

        // Tạo chuỗi column1, column2, column3...
        String columnString = String.join(", ", columns);

        // Tạo chuỗi ?, ?, ? dựa trên số cột
        String placeholders = String.join(", ", "?".repeat(columns.size()).split(""));

        String sql = "INSERT INTO " + tableName + " (" + columnString + ") VALUES (" + placeholders + ")";
        System.out.println("SQL Insert: " + sql);

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.size(); i++) {
                ps.setObject(i + 1, values.get(i)); // JDBC index bắt đầu từ 1
                System.out.println(values.get(i));
                // System.out.println(columns.get(i));
            }

            return ps.executeUpdate();
        }
    }

    /**
     * Hàm insert dùng chung cho mọi bảng
     *
     * @param tableName  Tên bảng
     * @param columns    Danh sách cột (ví dụ: "name", "age", "country_id")
     * @param rowsValues Danh sách list giá trị tương ứng
     * @return số dòng bị ảnh hưởng
     * @throws SQLException
     */
    public int[] insertBatch(String tableName, List<String> columns, List<List<Object>> rowsValues)
            throws SQLException {

        if (rowsValues == null || rowsValues.isEmpty()) {
            throw new IllegalArgumentException("Danh sách dữ liệu rỗng");
        }

        // kiểm tra số cột từng row
        for (List<Object> row : rowsValues) {
            if (row.size() != columns.size()) {
                throw new IllegalArgumentException("Số cột và số giá trị của mỗi dòng phải bằng nhau");
            }
        }

        // Tạo chuỗi column1, column2...
        String columnString = String.join(", ", columns);

        // Tạo ?, ?, ?
        String placeholders = String.join(", ", java.util.Collections.nCopies(columns.size(), "?"));

        String sql = "INSERT INTO " + tableName + " (" + columnString + ") VALUES (" + placeholders + ")";
        System.out.println("SQL Batch Insert: " + sql);

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            for (List<Object> row : rowsValues) {

                for (int i = 0; i < row.size(); i++) {
                    ps.setObject(i + 1, row.get(i));
                }

                ps.addBatch(); // thêm vào batch
            }

            return ps.executeBatch(); // trả về số dòng insert từng batch
        }
    }

    /**
     * Hàm update dùng chung cho mọi bảng
     *
     * @param tableName   Tên bảng
     * @param columns     Danh sách cột cần cập nhật
     * @param values      Giá trị tương ứng
     * @param whereClause Điều kiện WHERE (ví dụ: "id = ?")
     * @param whereValues Giá trị điều kiện WHERE
     * @return số dòng bị ảnh hưởng
     * @throws SQLException
     */
    public int update(String tableName, List<String> columns, List<Object> values,
            String whereClause, List<Object> whereValues) throws SQLException {
        System.out.println("Updating table1: " + tableName);
        if (columns.size() != values.size()) {
            System.out.println("Columns: " + columns.size());
            System.out.println("Values: " + values.size());
            throw new IllegalArgumentException("Số cột và số giá trị phải bằng nhau");
        }
        System.out.println("Updating table2: " + tableName);

        // Tạo chuỗi col1 = ?, col2 = ?, col3 = ?
        List<String> sets = new ArrayList<>();
        for (String col : columns) {
            sets.add(col + " = ? ");
        }
        String setString = String.join(", ", sets);

        String sql = "UPDATE " + tableName + " SET " + setString;

        if (whereClause != null && !whereClause.isBlank()) {
            sql += " WHERE " + whereClause;
        }

        System.out.println("SQL Update: " + sql);

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;

            // Set values cho SET
            for (Object v : values) {
                ps.setObject(index++, v);
            }

            // Set values cho WHERE
            if (whereValues != null) {
                for (Object wv : whereValues) {
                    ps.setObject(index++, wv);
                }
            }

            return ps.executeUpdate();
        }
    }

    public int updateDynamic(String table, Map<String, Object> fields, String whereClause, List<Object> whereValues)
            throws SQLException {

        if (fields.isEmpty())
            return 0;

        StringBuilder sql = new StringBuilder("UPDATE " + table + " SET ");
        List<Object> values = new ArrayList<>();

        // build SET col=?, col=?, ...
        for (String col : fields.keySet()) {
            sql.append(col).append("=?,");
            values.add(fields.get(col));
        }

        sql.setLength(sql.length() - 1);
        sql.append(" WHERE ").append(whereClause);

        values.addAll(whereValues);

        // 🔥 chạy JDBC trực tiếp (không dùng executeUpdate)
        try (java.sql.Connection conn = DbHelper.getConnection();
                java.sql.PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            return stmt.executeUpdate();
        }
    }

    /**
     * Batch UPDATE nhiều dòng với nhiều cột SET và nhiều cột WHERE.
     *
     * Mỗi phần tử trong updateValuesList và whereValuesList tương ứng 1 dòng
     * update.
     * Thứ tự giá trị phải khớp thứ tự cột.
     *
     * Ví dụ SQL:
     * UPDATE table SET col1=?, col2=? WHERE w1=? AND w2=?
     *
     * @param tableName        tên bảng
     * @param updateColumns    các cột cần cập nhật (SET)
     * @param updateValuesList giá trị update theo từng dòng
     * @param whereColumns     các cột điều kiện (WHERE)
     * @param whereValuesList  giá trị điều kiện theo từng dòng
     * @return mảng số dòng bị ảnh hưởng của từng batch
     * @throws SQLException nếu lỗi DB
     */
    public int[] updateBatch(String tableName, List<String> updateColumns, List<List<Object>> updateValuesList,
            List<String> whereColumns, List<List<Object>> whereValuesList) throws SQLException {

        if (updateValuesList.size() != whereValuesList.size())
            throw new IllegalArgumentException("Số row update và where phải bằng nhau");

        // SET col1=?, col2=?
        String setClause = updateColumns.stream()
                .map(c -> c + " = ?")
                .collect(Collectors.joining(", "));

        // WHERE colA=? AND colB=? AND colC=?
        String whereClause = whereColumns.stream()
                .map(c -> c + " = ?")
                .collect(Collectors.joining(" AND "));

        String sql = "UPDATE " + tableName +
                " SET " + setClause +
                " WHERE " + whereClause;

        System.out.println("SQL Batch: " + sql);

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < updateValuesList.size(); i++) {

                int index = 1;

                // set UPDATE values
                for (Object v : updateValuesList.get(i)) {
                    ps.setObject(index++, v);
                }

                // set WHERE values
                for (Object w : whereValuesList.get(i)) {
                    ps.setObject(index++, w);
                }

                ps.addBatch();
            }

            return ps.executeBatch();
        }
    }

    public double sumQtyHistory(String tableName, int aid) {

        String sql = "SELECT SUM(Qty) AS Qty FROM "
                + tableName
                + " WHERE DataWareHouseAID = ?";

        System.out.println("update qty: " + sql + " | aid=" + aid);

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, aid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    double result = rs.getDouble("Qty");

                    System.out.println("SUM Qty = " + result);
                    return result;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Hàm delete dùng chung cho mọi bảng
     *
     * @param tableName    Tên bảng
     * @param whereColumns Danh sách cột điều kiện (ví dụ: "id", "status")
     * @param whereValues  Danh sách giá trị tương ứng (ví dụ: "5", "12")
     * @return số dòng bị ảnh hưởng
     * @throws SQLException
     */
    public int delete(String tableName, List<String> whereColumns, List<Object> whereValues)
            throws SQLException {

        // Account accountFromState = AppState.getInstance().get("Account",
        // Account.class);

        if (whereColumns == null || whereColumns.isEmpty()) {
            throw new IllegalArgumentException("Cần ít nhất một điều kiện WHERE để tránh xóa toàn bộ bảng.");
        }
        if (whereColumns.size() != whereValues.size()) {
            throw new IllegalArgumentException("Số cột điều kiện và số giá trị phải bằng nhau");
        }

        // Tạo chuỗi: col1 = ?, col2 = ?, ...
        List<String> conditions = new ArrayList<>();
        for (String col : whereColumns) {
            conditions.add(col + " = ?");
        }
        String whereString = String.join(" AND ", conditions);

        String sql = "DELETE FROM " + tableName + " WHERE " + whereString;
        System.out.println("SQL Delete: " + sql);

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Truyền giá trị vào PreparedStatement
            for (int i = 0; i < whereValues.size(); i++) {
                ps.setObject(i + 1, whereValues.get(i));
                System.out.println("WHERE param " + (i + 1) + ": " + whereValues.get(i));
            }

            return ps.executeUpdate();
        }
    }

    public int deleteDynamic(String table, String whereClause, List<Object> params) throws SQLException {

        String sql = "DELETE FROM " + table + " WHERE " + whereClause;

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            return ps.executeUpdate();
        }
    }

    /**
     * Kiểm tra xem ProductID đã tồn tại trong bảng Products chưa
     * 
     * @param productId ID sản phẩm cần kiểm tra
     * @return true nếu tồn tại, false nếu chưa tồn tại
     * @throws SQLException
     */
    public boolean isProductIdExists(String productId) throws SQLException {
        // Gọi hàm scalar SQL Server fnCheckProductID
        String sql = "SELECT dbo.fnCheckProductID(?) AS ExistsFlag";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // fnCheckProductID trả về BIT (0 hoặc 1)
                    return rs.getBoolean("ExistsFlag");
                }
            // rs.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Nếu lỗi SQL, mặc định trả về false
            return false;
        }

        return false;
    }

    /**
     * Kiểm tra xem ProductID đã tồn tại trong bảng Products chưa
     * 
     * @param ProductAID AID kho cần kiểm tra
     * @param tableWh    tên bảng kho
     * @return true nếu tồn tại, false nếu chưa tồn tại
     * @throws SQLException
     */
    public boolean isWareHouseExists(String ProductAID, String tableWh) throws SQLException {
        // Gọi hàm scalar SQL Server fnCheckProductID
        String sql = "SELECT 1 AS ExistsFlag FROM " + tableWh + " WHERE ProductAID = ?";
        System.out.println(sql);
        System.out.println(ProductAID);
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ProductAID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // fnCheckProductID trả về BIT (0 hoặc 1)
                    boolean result = rs.getBoolean("ExistsFlag");
                    System.out.println("isWareHouseExists result: " + result);
                    return rs.getBoolean("ExistsFlag");
                }
                // rs.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Nếu lỗi SQL, mặc định trả về false
            return false;
        }

        return false;
    }

    /**
     * Kiểm tra xem ProductID đã tồn tại trong bảng Products chưa
     * 
     * @param ProductAID AID kho cần kiểm tra
     * @param tableWh    tên bảng kho
     * @param condition  điều kiện kiểm tra ví dụ "ProductAID = ?"
     * @return true nếu tồn tại, false nếu chưa tồn tại
     * @throws SQLException
     */
    public boolean isCheck(String ProductAID, String tableWh, String condition) throws SQLException {
        // Gọi hàm scalar SQL Server fnCheckProductID
        String sql = "SELECT 1 AS ExistsFlag FROM " + tableWh + " WHERE " + condition;
        System.out.println(sql);
        System.out.println(ProductAID);
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ProductAID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // fnCheckProductID trả về BIT (0 hoặc 1)
                    boolean result = rs.getBoolean("ExistsFlag");
                    System.out.println("isWareHouseExists result: " + result);
                    return rs.getBoolean("ExistsFlag");
                }
                // rs.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Nếu lỗi SQL, mặc định trả về false
            return false;
        }

        return false;
    }

    /**
     * Kiểm tra xem đã tồn tại trong bảng chưa
     * 
     * @param ProductAID AID kho cần kiểm tra
     * @param table      tên bảng
     * @param condition  điều kiện kiểm tra ví dụ "id = ? AND name = ?"
     * @param params     điều kiện kiểm tra ví dụ "001", "002"
     * @return true nếu tồn tại, false nếu chưa tồn tại
     * @throws SQLException
     */
    public boolean isExists(String table, String condition, Object... params) {
        String sql = "SELECT 1 FROM " + table + " WHERE " + condition;
        System.out.println(sql);

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String returnAID(String tableName, String column, String columnCondition, String codeAID)
            throws SQLException {
        String sql = "SELECT " + column + " FROM " + tableName + " WHERE " + columnCondition + "  = ?";
        System.out.println(sql);
        System.out.println(codeAID);

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codeAID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // fnCheckProductID trả về BIT (0 hoặc 1)
                    return rs.getString(column);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Nếu lỗi SQL, mặc định trả về false
            return "";
        }

        return "";
    }

    public int returnCount(String tableName, String columnCondition, String codeAID)
            throws SQLException {
        String sql = "SELECT COUNT(*) AS Count FROM " + tableName + " WHERE " + columnCondition + "  = ?";
        System.out.println(sql);
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codeAID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // fnCheckProductID trả về BIT (0 hoặc 1)
                    return rs.getInt("Count");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Nếu lỗi SQL, mặc định trả về false
            return -1;
        }

        return 0;
    }

    public void executeUpdate(String sql) {
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.executeUpdate(); // ✅ đúng

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> selectOneDynamic(
            String tableName,
            String columns,
            String whereClause,
            List<Object> whereValues) throws Exception {

        String sql = "SELECT " + columns + " FROM " + tableName + " WHERE " + whereClause;
                System.out.println(sql);
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < whereValues.size(); i++) {
                ps.setObject(i + 1, whereValues.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;

                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                Map<String, Object> row = new HashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }

                return row;
            }
        }
    }
}
