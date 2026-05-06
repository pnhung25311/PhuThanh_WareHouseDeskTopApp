package com.phuthanh.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DbHelperBusiness {

    private static final String URL_INTERNAL = "jdbc:sqlserver://192.168.1.100:1433;"
            + "databaseName=NB2024_App;"
            + "trustServerCertificate=true;useUnicode=true;characterEncoding=UTF-8";

    private static final String USER = "sa";
    private static final String PASS = "Sql2014";

    // ❗ KHÔNG final – KHÔNG static block
    private static HikariDataSource internalDS;
    // private static HikariDataSource publicDS;

    private static final Object LOCK = new Object();
    private static Boolean cachedInternalNetwork;

    /* ================= CONNECTION ================= */

    public static Connection getConnection() throws SQLException {
        // if (isInternalNetwork()) {
            return getInternalDS().getConnection();
        // } 
        // else {
        //     return getPublicDS().getConnection();
        // }
    }

    private static HikariDataSource getInternalDS() {
        if (internalDS == null) {
            synchronized (LOCK) {
                if (internalDS == null) {
                    internalDS = createDataSource(URL_INTERNAL, "Hikari-LAN");
                }
            }
        }
        return internalDS;
    }

    // private static HikariDataSource getPublicDS() {
    //     if (publicDS == null) {
    //         synchronized (LOCK) {
    //             if (publicDS == null) {
    //                 publicDS = createDataSource(URL_PUBLIC, "Hikari-PUBLIC");
    //             }
    //         }
    //     }
    //     return publicDS;
    // }

    /* ================= POOL CONFIG ================= */

    private static HikariDataSource createDataSource(String jdbcUrl, String poolName) {
        HikariConfig config = new HikariConfig();

        config.setPoolName(poolName);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(USER);
        config.setPassword(PASS);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10_000); // 10s
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);

        return new HikariDataSource(config);
    }

    /* ================= NETWORK ================= */

    public static boolean isInternalNetwork() {
        if (cachedInternalNetwork != null) {
            return cachedInternalNetwork;
        }

        try {
            URL url = new URI("http://checkip.amazonaws.com/").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                String publicIP = in.readLine().trim();
                cachedInternalNetwork = "14.224.207.115".equals(publicIP);
                return cachedInternalNetwork;
            }
        } catch (Exception e) {
            // lỗi mạng → mặc định LAN
            cachedInternalNetwork = true;
            return true;
        }
    }

    /* ================= TEST ================= */

    public static boolean checkConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
