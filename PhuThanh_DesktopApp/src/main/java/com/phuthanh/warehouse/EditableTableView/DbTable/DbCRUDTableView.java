package com.phuthanh.warehouse.EditableTableView.DbTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.phuthanh.helper.DbHelper;
import com.phuthanh.model.warehouse.Product;

public class DbCRUDTableView {
    
    // INSERT BATCH
    public void insertBatch(String sql, List<List<Object>> rows) throws SQLException {
        try(Connection conn = DbHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            for(List<Object> row : rows){
                for(int i=0;i<row.size();i++)
                    ps.setObject(i+1, row.get(i));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // UPDATE BATCH
    public void updateBatch(String sql, List<List<Object>> rows) throws SQLException {
        try(Connection conn = DbHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            for(List<Object> row : rows){
                for(int i=0;i<row.size();i++)
                    ps.setObject(i+1, row.get(i));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // DELETE BATCH
    public void deleteBatch(String sql, List<Object> ids) throws SQLException {
        try(Connection conn = DbHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            for(Object id : ids){
                ps.setObject(1, id);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // LOAD ALL PRODUCT
    public List<Product> getAllProducts() throws SQLException {

        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM Product";

        try(Connection conn = DbHelper.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()){
                Product p = new Product();
                p.setProductIDMain(rs.getString("ProductIDMain"));
                p.setProductID(rs.getString("ProductID"));
                p.setID_Keeton(rs.getString("ID_Keeton"));
                p.setID_Industrial(rs.getString("ID_Industrial"));
                p.setID_PartNo(rs.getString("ID_PartNo"));
                p.setID_ReplacedPartNo(rs.getString("ID_ReplacedPartNo"));
                // p.setNameProduct(rs.getString("NameProduct"));
                // p.setParameter(rs.getString("Parameter"));
                // p.setVehicleTypeID(rs.getInt("VehicleTypeID")+"");
                // p.setVehicleDetail(rs.getString("VehicleDetail"));
                // p.setVehicleCluster(rs.getString("VehicleCluster"));
                // p.setManufacturerID(rs.getInt("ManufacturerID"));
                // p.setCountryID(rs.getInt("CountryID"));
                // p.setSupplierID(rs.getInt("SupplierID"));
                // p.setSupplierActualID(rs.getInt("SupplierActualID"));
                // p.setUnitID(rs.getInt("UnitID"));
                // p.setSegmentID(rs.getInt("SegmentID"));
                // p.setPurposeID(rs.getInt("PurposeID"));
                // p.setRemark(rs.getString("Remark"));
                list.add(p);
            }
        }
        return list;
    }
}
