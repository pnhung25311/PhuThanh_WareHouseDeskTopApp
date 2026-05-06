package com.phuthanh.editableTable.controller;

import com.phuthanh.editableTable.model.Product;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.*;

public class ProductTableController {

    @FXML private TableView<Product> table;

    @FXML private TableColumn<Product,String> colProductAID;
    @FXML private TableColumn<Product,String> colProductIDMain;
    @FXML private TableColumn<Product,String> colProductID;
    @FXML private TableColumn<Product,String> colID_Keeton;
    @FXML private TableColumn<Product,String> colID_Industrial;
    @FXML private TableColumn<Product,String> colID_PartNo;
    @FXML private TableColumn<Product,String> colID_ReplacedPartNo;
    @FXML private TableColumn<Product,String> colNameProduct;
    @FXML private TableColumn<Product,String> colParameter;
    @FXML private TableColumn<Product,String> colVehicleTypeID;
    @FXML private TableColumn<Product,String> colVehicleDetail;
    @FXML private TableColumn<Product,String> colVehicleCluster;
    @FXML private TableColumn<Product,String> colManufacturerID;
    @FXML private TableColumn<Product,String> colCountryID;
    @FXML private TableColumn<Product,String> colSupplierID;
    @FXML private TableColumn<Product,String> colSupplierActualID;
    @FXML private TableColumn<Product,String> colUnitID;
    @FXML private TableColumn<Product,String> colSegmentID;
    @FXML private TableColumn<Product,String> colPurposeID;
    @FXML private TableColumn<Product,String> colImg1;
    @FXML private TableColumn<Product,String> colImg2;
    @FXML private TableColumn<Product,String> colImg3;
    @FXML private TableColumn<Product,String> colRemark;
    @FXML private TableColumn<Product,String> colLastTime;

    private final ObservableList<Product> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        bind(colProductAID, p -> p.ProductAIDProperty());
        bind(colProductIDMain, p -> p.ProductIDMainProperty());
        bind(colProductID, p -> p.ProductIDProperty());
        bind(colID_Keeton, p -> p.ID_KeetonProperty());
        bind(colID_Industrial, p -> p.ID_IndustrialProperty());
        bind(colID_PartNo, p -> p.ID_PartNoProperty());
        bind(colID_ReplacedPartNo, p -> p.ID_ReplacedPartNoProperty());
        bind(colNameProduct, p -> p.NameProductProperty());
        bind(colParameter, p -> p.ParameterProperty());
        bind(colVehicleTypeID, p -> p.VehicleTypeIDProperty());
        bind(colVehicleDetail, p -> p.VehicleDetailProperty());
        bind(colVehicleCluster, p -> p.VehicleClusterProperty());
        bind(colManufacturerID, p -> p.ManufacturerIDProperty());
        bind(colCountryID, p -> p.CountryIDProperty());
        bind(colSupplierID, p -> p.SupplierIDProperty());
        bind(colSupplierActualID, p -> p.SupplierActualIDProperty());
        bind(colUnitID, p -> p.UnitIDProperty());
        bind(colSegmentID, p -> p.SegmentIDProperty());
        bind(colPurposeID, p -> p.PurposeIDProperty());
        bind(colImg1, p -> p.Img1Property());
        bind(colImg2, p -> p.Img2Property());
        bind(colImg3, p -> p.Img3Property());
        bind(colRemark, p -> p.RemarkProperty());
        bind(colLastTime, p -> p.LastTimeProperty());

        table.setItems(data);
        enablePasteExcel();
    }

    private void bind(TableColumn<Product,String> col,
                      java.util.function.Function<Product, javafx.beans.property.StringProperty> prop) {

        col.setCellValueFactory(c -> prop.apply(c.getValue()));
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setOnEditCommit(e -> prop.apply(e.getRowValue()).set(e.getNewValue()));
    }

    @FXML
    private void addProduct() {
        data.add(new Product());
        table.scrollTo(data.size()-1);
    }

    private void enablePasteExcel() {
        table.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode()== KeyCode.V) paste();
        });
    }

    private void paste() {
        Clipboard cb = Clipboard.getSystemClipboard();
        if(!cb.hasString()) return;

        for(String row : cb.getString().split("\\n")){
            Product p = new Product();
            String[] cols = row.split("\\t");
            if(cols.length>0) p.ProductAIDProperty().set(cols[0]);
            if(cols.length>1) p.ProductIDMainProperty().set(cols[1]);
            if(cols.length>2) p.ProductIDProperty().set(cols[2]);
            data.add(p);
        }
    }
}