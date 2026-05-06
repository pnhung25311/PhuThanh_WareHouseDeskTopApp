package com.phuthanh.warehouse.screen.dialog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbHelper;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.store.AppState;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class DialogExportExcelWareHouse {
    @FXML
    private DatePicker txtTime;

    @FXML
    private Button btnExportExcel;

    @FXML
    private Button btnCancel;
    TableView<ObservableList<String>> tableView;
    private static final DbTableHelper dbTableHelper = new DbTableHelper();
    private static final FunctionHelper functionHelper = new FunctionHelper();
    private static final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    @FXML
    private void initialize() {
        // Chạy khi FXML được load
        txtTime.setValue(LocalDate.now());
        tableView = new TableView<>();
        System.out.println("Controller loaded.");
    }

    public static ObservableList<ObservableList<String>> loadTableConvertByDate(
            TableView<ObservableList<String>> table,
            DrawerItem drawerItem,
            LocalDate fromDate,
            LocalDate toDate) {

        String query = """
                    SELECT  wh.DataWareHouseAID, wh.ProductID, wh.ProductAID, wh.ID_Keeton, wh.ID_Industrial,
                        wh.ID_PartNo, wh.ID_ReplacedPartNo, wh.NameProduct, wh.Parameter, wh.VehicleDetail, wh.VehicleCluster,
                        wh.CountryName, wh.SupplierName, wh.UnitName, wh.ManufacturerName, wh.SupplierActualName,
                        h.Qty, wh.Qty_Expected, wh.ID_Bill, wh.LocationID, wh.Img1, wh.Img2, wh.Img3, wh.RemarkOfProduct, wh.LastTime
                    FROM %s AS wh
                    LEFT OUTER JOIN (
                        SELECT DataWareHouseAID, SUM(Qty) AS Qty
                        FROM %s
                        WHERE dbo.fnFromDateToDate(Time, ?, ?) = 1
                        GROUP BY DataWareHouseAID
                    ) AS h ON wh.DataWareHouseAID = h.DataWareHouseAID
                    ORDER BY wh.LastTime DESC
                """
                .formatted(drawerItem.getWareHouseTable(), drawerItem.getWareHouseDataBaseHistory());

        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, fromDate.toString());
            ps.setString(2, toDate.toString());

            ResultSet rs = ps.executeQuery();

            dbTableHelper.createColumns(table, rs, functionHelper.getColumnMap(),
                    functionHelper.getColumnListShowhide());

            ObservableList<ObservableList<String>> data = dbTableHelper.addData(rs);

            table.setItems(data);

            return data;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    @FXML
    private void onExportExcel() {
        // Xử lý nút Xuất Excel
        System.out.println("Ngày chọn: " + txtTime.getValue());
        LocalDate from = LocalDate.of(1900, 1, 1);
        LocalDate to = txtTime.getValue();
        DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);

        loadTableConvertByDate(tableView, selectedItemFromState, from, to);

        Stage stage = (Stage) btnExportExcel.getScene().getWindow();
        boolean result = functionHelper.exportExcel(tableView,
                stage,
                "Sheet1");

        if (result) {
            customDialogNotification.showDialog("Thành công", "Xuất Excel thành công",
                    Alert.AlertType.INFORMATION);
        } else {
            System.out.println("Xuất thất bại!");
            customDialogNotification.showDialog("Lỗi", "Xuất Excel thất bại", Alert.AlertType.ERROR);

        }
    }

    @FXML
    private void onCancel() {
        // Xử lý nút Thoát
        btnCancel.getScene().getWindow().hide();
    }
}
