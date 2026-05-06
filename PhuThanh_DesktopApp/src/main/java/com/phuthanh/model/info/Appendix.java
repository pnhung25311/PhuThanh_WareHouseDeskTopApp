package com.phuthanh.model.info;

public class Appendix {
    String table;
    String idCol;
    String nameCol;
    boolean adminOnly;

    public Appendix(String table, String idCol, String nameCol) {
        this(table, idCol, nameCol, false);
    }

    public Appendix(String table, String idCol, String nameCol, boolean adminOnly) {
        this.table = table;
        this.idCol = idCol;
        this.nameCol = nameCol;
        this.adminOnly = adminOnly;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getIdCol() {
        return idCol;
    }

    public void setIdCol(String idCol) {
        this.idCol = idCol;
    }

    public String getNameCol() {
        return nameCol;
    }

    public void setNameCol(String nameCol) {
        this.nameCol = nameCol;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }

    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    
}
