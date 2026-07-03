package com.phuthanh.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DbCRUDHelper {

    public int insert(String tableName, List<String> columns, List<Object> values) throws SQLException {
        if (columns.size() != values.size()) {
            logMismatch(columns, values);
            throw new IllegalArgumentException("Số cột và số giá trị phải bằng nhau");
        }

        String columnString = String.join(", ", columns);
        String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
        String sql = "INSERT INTO " + tableName + " (" + columnString + ") VALUES (" + placeholders + ")";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                ps.setObject(i + 1, values.get(i));
            }
            return ps.executeUpdate();
        }
    }

    private void logMismatch(List<String> columns, List<Object> values) {
        System.out.println("columns: " + columns.size() + " | values: " + values.size());
        int min = Math.min(columns.size(), values.size());
        for (int i = 0; i < min; i++) {
            System.out.println(columns.get(i) + " == " + values.get(i));
        }
    }

    public int[] insertBatch(String tableName, List<String> columns, List<List<Object>> rowsValues)
            throws SQLException {
        if (rowsValues == null || rowsValues.isEmpty()) {
            throw new IllegalArgumentException("Danh sách dữ liệu rỗng");
        }

        String columnString = String.join(", ", columns);
        String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
        String sql = "INSERT INTO " + tableName + " (" + columnString + ") VALUES (" + placeholders + ")";

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (List<Object> row : rowsValues) {
                if (row.size() != columns.size()) {
                    throw new IllegalArgumentException("Số cột và số giá trị của mỗi dòng phải bằng nhau");
                }
                for (int i = 0; i < row.size(); i++) {
                    ps.setObject(i + 1, row.get(i));
                }
                ps.addBatch();
            }
            return ps.executeBatch();
        }
    }

    public int update(String tableName, List<String> columns, List<Object> values, String whereClause,
            List<Object> whereValues) throws SQLException {
        if (columns.size() != values.size()) {
            System.out.println("Số cột và số giá trị phải bằng nhau");
            throw new IllegalArgumentException("Số cột và số giá trị phải bằng nhau");
        }

        // Tối ưu hóa chuỗi bằng Stream, loại bỏ vòng lặp thủ công sinh rác RAM
        String setString = columns.stream().map(col -> col + " = ?").collect(Collectors.joining(", "));
        String sql = "UPDATE " + tableName + " SET " + setString
                + ((whereClause != null && !whereClause.isBlank()) ? " WHERE " + whereClause : "");

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            for (Object v : values) {
                ps.setObject(index++, v);
            }
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

        StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ");
        List<Object> values = new ArrayList<>();

        fields.forEach((col, value) -> {
            sql.append(col).append("=?,");
            values.add(value);
        });
        sql.setLength(sql.length() - 1); // Cắt bỏ ký tự phẩy dư ở cuối
        sql.append(" WHERE ").append(whereClause);
        values.addAll(whereValues);

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            return stmt.executeUpdate();
        }
    }

    public int[] updateBatch(String tableName, List<String> updateColumns, List<List<Object>> updateValuesList,
            List<String> whereColumns, List<List<Object>> whereValuesList) throws SQLException {
        if (updateValuesList.size() != whereValuesList.size()) {
            throw new IllegalArgumentException("Số row update và where phải bằng nhau");
        }

        String setClause = updateColumns.stream().map(c -> c + " = ?").collect(Collectors.joining(", "));
        String whereClause = whereColumns.stream().map(c -> c + " = ?").collect(Collectors.joining(" AND "));
        String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + whereClause;

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < updateValuesList.size(); i++) {
                int index = 1;
                for (Object v : updateValuesList.get(i)) {
                    ps.setObject(index++, v);
                }
                for (Object w : whereValuesList.get(i)) {
                    ps.setObject(index++, w);
                }
                ps.addBatch();
            }
            return ps.executeBatch();
        }
    }

    public double sumQtyHistory(String tableName, int aid) {
        String sql = "SELECT SUM(Qty) AS Qty FROM " + tableName + " WHERE DataWareHouseAID = ?";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, aid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("Qty");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int delete(String tableName, List<String> whereColumns, List<Object> whereValues) throws SQLException {
        if (whereColumns == null || whereColumns.isEmpty() || whereColumns.size() != whereValues.size()) {
            throw new IllegalArgumentException("Tham số điều kiện WHERE không hợp lệ");
        }

        String whereString = whereColumns.stream().map(col -> col + " = ?").collect(Collectors.joining(" AND "));
        String sql = "DELETE FROM " + tableName + " WHERE " + whereString;

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < whereValues.size(); i++) {
                ps.setObject(i + 1, whereValues.get(i));
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

    public boolean isProductIdExists(String productId) {
        String sql = "SELECT dbo.fnCheckProductID(?) AS ExistsFlag";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getBoolean("ExistsFlag");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isWareHouseExists(String productAID, String tableWh) {
        String sql = "SELECT 1 AS ExistsFlag FROM " + tableWh + " WHERE ProductAID = ?";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productAID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isExists(String table, String condition, Object... params) {
        String sql = "SELECT 1 FROM " + table + " WHERE " + condition;
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
        String sql = "SELECT " + column + " FROM " + tableName + " WHERE " + columnCondition + " = ?";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codeAID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getString(column);
            }
        }
        return "";
    }

    public int returnCount(String tableName, String columnCondition, String codeAID) throws SQLException {
        String sql = "SELECT COUNT(*) AS Count FROM " + tableName + " WHERE " + columnCondition + " = ?";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codeAID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("Count");
            }
        }
        return -1;
    }

    public void executeUpdate(String sql) {
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> selectOneDynamic(String tableName, String columns, String whereClause,
            List<Object> whereValues) throws Exception {
        String sql = "SELECT " + columns + " FROM " + tableName + " WHERE " + whereClause;
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < whereValues.size(); i++) {
                ps.setObject(i + 1, whereValues.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;
                ResultSetMetaData meta = rs.getMetaData();
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                return row;
            }
        }
    }

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
}