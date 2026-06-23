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

public class DbHelper {

    private static final String URL_INTERNAL = "jdbc:sqlserver://192.168.1.54:2012;"
            + "databaseName=WarehouseDB;"
            + "trustServerCertificate=true;";

    private static final String URL_PUBLIC = "jdbc:sqlserver://14.224.207.115:2012;"
            + "databaseName=WarehouseDB;"
            + "trustServerCertificate=true;";

    // private static final String URL_INTERNAL = "jdbc:sqlserver://192.168.1.11:2000;"
    //         + "databaseName=WarehouseDB;"
    //         + "trustServerCertificate=true;";

    // private static final String URL_PUBLIC = "jdbc:sqlserver://14.224.207.115:2000;"
    //         + "databaseName=WarehouseDB;"
    //         + "trustServerCertificate=true;";

    private static final String USER = "sa";
    private static final String PASS = "Sql201293@";
    // private static final String PASS = "123";

    // TỐI ƯU RAM & ĐA LUỒNG: Sử dụng volatile để đảm bảo an toàn luồng và đồng bộ cache tức thì
    private static volatile HikariDataSource internalDS;
    private static volatile HikariDataSource publicDS;

    private static final Object LOCK = new Object();
    private static volatile Boolean cachedInternalNetwork;

    /* ================= CONNECTION ================= */

    public static Connection getConnection() throws SQLException {
        return isInternalNetwork() ? getInternalDS().getConnection() : getPublicDS().getConnection();
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

    private static HikariDataSource getPublicDS() {
        if (publicDS == null) {
            synchronized (LOCK) {
                if (publicDS == null) {
                    publicDS = createDataSource(URL_PUBLIC, "Hikari-PUBLIC");
                }
            }
        }
        return publicDS;
    }

    /* ================= POOL CONFIG ================= */

    private static HikariDataSource createDataSource(String jdbcUrl, String poolName) {
        HikariConfig config = new HikariConfig();

        config.setPoolName(poolName);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(USER);
        config.setPassword(PASS);

        // TỐI ƯU RAM CHO DESKTOP APP: Tinh chỉnh Pool Size vừa đủ hiệu năng
        config.setMaximumPoolSize(6);            // Giảm từ 10 xuống 6 giúp bớt tốn RAM cho luồng chạy ngầm của SQL Server
        config.setMinimumIdle(1);                // Giữ tối thiểu 1 kết nối sống
        config.setConnectionTimeout(6000);       // Giảm timeout xuống 6s để UI không bị đơ quá lâu nếu mất mạng
        config.setIdleTimeout(120_000);          // 2 phút không dùng thì thu hồi bớt kết nối thừa
        config.setMaxLifetime(600_000);          // 10 phút làm mới kết nối tránh treo connection ở server

        // TỐI ƯU DRIVER: Cấu hình cache câu lệnh của SQL Server trực tiếp giúp tiết kiệm tài nguyên RAM Heap
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "100");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "1024");
        
        // Chống rò rỉ kết nối (Connection Leak Detection) nếu quên đóng try-with-resources ở các Controller
        config.setLeakDetectionThreshold(15000); // Cảnh báo nếu giữ kết nối quá 15 giây

        return new HikariDataSource(config);
    }

    /* ================= NETWORK ================= */

    public static boolean isInternalNetwork() {
        // Double-checked locking tối ưu: Trả về ngay nếu đã có cache, không sinh thêm bất kỳ Object nào
        if (cachedInternalNetwork != null) {
            return cachedInternalNetwork;
        }

        synchronized (LOCK) {
            if (cachedInternalNetwork != null) {
                return cachedInternalNetwork;
            }

            HttpURLConnection conn = null;
            try {
                URL url = new URI("http://checkip.amazonaws.com/").toURL();
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(2500); // Tối ưu thời gian chờ kết nối mạng
                conn.setReadTimeout(2500);

                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String publicIP = in.readLine();
                    if (publicIP != null) {
                        publicIP = publicIP.trim();
                        cachedInternalNetwork = "14.224.207.115".equals(publicIP);
                        return cachedInternalNetwork;
                    }
                }
            } catch (Exception e) {
                // Chống lặp lại lỗi ngốn RAM: Đặt giá trị mặc định ngay cả khi lỗi để không bao giờ chạy lại khối try này nữa
                cachedInternalNetwork = true; 
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return cachedInternalNetwork;
        }
    }

    /* ================= SHUTDOWN (BỔ SUNG AN TOÀN RAM) ================= */

    /**
     * Giải phóng toàn bộ các kết nối database khi tắt ứng dụng hoặc đăng xuất.
     * Tránh việc các luồng ngầm của HikariCP vẫn treo lại trong RAM của hệ điều hành.
     */
    public static void shutdown() {
        synchronized (LOCK) {
            if (internalDS != null && !internalDS.isClosed()) {
                internalDS.close();
                internalDS = null;
            }
            if (publicDS != null && !publicDS.isClosed()) {
                publicDS.close();
                publicDS = null;
            }
            cachedInternalNetwork = null;
        }
    }

    /* ================= TEST ================= */

    public static boolean checkConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}