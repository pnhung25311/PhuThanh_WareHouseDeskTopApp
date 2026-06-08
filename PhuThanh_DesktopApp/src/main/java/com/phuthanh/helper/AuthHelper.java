package com.phuthanh.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.phuthanh.model.info.Account;
import com.phuthanh.store.AppState;

public class AuthHelper {

    /**
     * Kiểm tra đăng nhập.
     * 
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return true nếu đăng nhập đúng, ngược lại false
     */
    public boolean login(String username, String password) {
        String sql = "SELECT * FROM Account WHERE UserName = ? AND PassWord = ? AND Status = 'ACTIVE_STATUS'";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    int id = rs.getInt("AccountID");
                    String userName = rs.getString("UserName");
                    String passWord = rs.getString("PassWord");
                    String fullName = rs.getString("FullName");
                    String role = rs.getString("Role");
                    int employeeID = rs.getInt("EmployeeID");
                    int teamGroup = rs.getInt("TeamGroup");

                    Boolean userRole = false;
                    if (role.equals("WAREHOUSE") || role.equals("ADMIN")) {
                        userRole = true;
                    } 

                    Account acc = new Account(id, userName, passWord, fullName, role, employeeID, teamGroup);
                    AppState.getInstance().set("Account", acc);
                    AppState.getInstance().set("UserRole", userRole);

                    return true; // đã tìm thấy user
                }

                return false; // không tìm thấy user
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
