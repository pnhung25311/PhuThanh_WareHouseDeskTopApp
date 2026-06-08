package com.phuthanh.helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.phuthanh.custom.CustomDialogNotification;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javafx.scene.control.Alert;

public class DbHelperCheckProductID {
        private static final CustomDialogNotification customDialogNotification = new CustomDialogNotification();


    /* ================= DB CONFIG ================= */

    private static final String URL_INTERNAL = "jdbc:sqlserver://192.168.1.100:1433;"
            + "databaseName=NB2024_App;"
            + "trustServerCertificate=true;"
            + "useUnicode=true;characterEncoding=UTF-8";

    private static final String USER = "sa";
    private static final String PASS = "Sql2014";

    /* ================= DATASOURCE ================= */

    private static HikariDataSource dataSource;
    private static final Object LOCK = new Object();

    /* ================= CONNECTION ================= */

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    private static HikariDataSource getDataSource() {

        if (dataSource == null) {
            synchronized (LOCK) {
                if (dataSource == null) {
                    dataSource = createDataSource();
                }
            }
        }

        return dataSource;
    }

    /* ================= POOL CONFIG ================= */

    private static HikariDataSource createDataSource() {

        HikariConfig config = new HikariConfig();

        config.setPoolName("DMVT-POOL");
        config.setJdbcUrl(URL_INTERNAL);
        config.setUsername(USER);
        config.setPassword(PASS);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }

    /* ================= QUERY ================= */

    public List<String> getListDMVT() {

        List<String> list = new ArrayList<>();

        try (Connection connection = getConnection();
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT ma_vt FROM dmvt")) {

            while (rs.next()) {
                list.add(rs.getString("ma_vt").trim());
            }

        } catch (SQLException e) {

            e.printStackTrace();

            customDialogNotification.showDialog(
                    "Lỗi",
                    "Không thể kết nối hệ thống kế toán!",
                    Alert.AlertType.ERROR);

            return new ArrayList<>();
        }

        return list;
    }

    public boolean isExistDMVT(String maVT) {
        if (maVT == null || maVT.isBlank())
            return false;

        List<String> dmvtList = getListDMVT();
        System.out.println(dmvtList);
        return dmvtList.contains(maVT.trim());
    }

    /* ================= TEST ================= */

    public static boolean checkConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
}