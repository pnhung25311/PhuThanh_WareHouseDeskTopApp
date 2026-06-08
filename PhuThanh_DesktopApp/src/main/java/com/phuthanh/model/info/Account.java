package com.phuthanh.model.info;

public class Account {
    private int AccountID;
    private String UserName;
    private String PassWord;
    private String FullName;
    private String Role;
    private int EmployeeID;
    private int TeamGroup;
    public Account(int accountID, String userName, String passWord, String fullName, String role, int employeeID, int teamGroup) {
        AccountID = accountID;
        UserName = userName;
        PassWord = passWord;
        FullName = fullName;
        EmployeeID = employeeID;
        TeamGroup = teamGroup;
        Role = role;
    }
    public int getTeamGroup() {
        return TeamGroup;
    }
    public void setTeamGroup(int teamGroup) {
        TeamGroup = teamGroup;
    }
    public int getAccountID() {
        return AccountID;
    }
    public void setAccountID(int accountID) {
        AccountID = accountID;
    }
    public String getUserName() {
        return UserName;
    }
    public void setUserName(String userName) {
        UserName = userName;
    }
    public String getPassWord() {
        return PassWord;
    }
    public void setPassWord(String passWord) {
        PassWord = passWord;
    }
    public String getFullName() {
        return FullName;
    }
    public void setFullName(String fullName) {
        FullName = fullName;
    }
    public String getRole() {
        return Role;
    }
    public void setRole(String role) {
        Role = role;
    }
    public int getEmployeeID() {
        return EmployeeID;
    }
    public void setEmployeeID(int employeeID) {
        EmployeeID = employeeID;
    }

    @Override
    public String toString() {
        return FullName;
    }
    
}
