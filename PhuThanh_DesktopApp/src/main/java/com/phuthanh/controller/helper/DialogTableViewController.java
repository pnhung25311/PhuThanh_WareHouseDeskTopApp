package com.phuthanh.controller.helper;

import com.phuthanh.helper.DbTableHelper;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class DialogTableViewController {
    @FXML
    private TableView<ObservableList<String>> tableView;
    private ObservableList<ObservableList<String>> requestCart;
    private final DbTableHelper dbTableHelper = new DbTableHelper();
    private String CodeAID;

    private void loadData() {
        String sql = """
                    SELECT CartAID, CartID, FullName, ProductID, ID_PartNo, NameProduct,
                           ManufacturerName, CountryName, UnitName, Qty, Price, Total,
                           PriceVAT, PriceNET, NamePayment, NameBill, NameSource,
                           NameDelivery, NameEmployee, DeliveryTime, Remark, LastTime, N'Dữ liệu mới' AS DataType
                    FROM vwRequestCart
                    WHERE CartAID = %s

                    UNION

                    SELECT CartAID, CartID, FullName, ProductID, ID_PartNo, NameProduct,
                           ManufacturerName, CountryName, UnitName, Qty, Price, Total,
                           PriceVAT, PriceNET, NamePayment, NameBill, NameSource,
                           NameDelivery, NameEmployee, DeliveryTime, Remark, LastTime, N'Dữ liệu cũ' AS DataType
                    FROM vwCart
                    WHERE CartAID = %s
                """.formatted(CodeAID, CodeAID);
        System.out.println("SQL: " + sql);

        requestCart = dbTableHelper.loadDataTable(tableView, sql);

        tableView.setItems(requestCart);
    }

    public void setInitialData(String CodeAID) {
        this.CodeAID = CodeAID;
        loadData();
    }

    @FXML
    private void onClose() {

    }
}
