package com.phuthanh.warehouse.EditableTableView.modelTable;

import java.time.LocalDate;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CartFX {
    private final StringProperty cartAID = new SimpleStringProperty();
    private final IntegerProperty accountID = new SimpleIntegerProperty();
    private final StringProperty creator = new SimpleStringProperty();
    private final StringProperty productAID = new SimpleStringProperty();
    private final StringProperty productID = new SimpleStringProperty();
    private final StringProperty productAIDVAT = new SimpleStringProperty();
    private final StringProperty productIDVAT = new SimpleStringProperty();
    private final StringProperty id_PartNo = new SimpleStringProperty();
    private final StringProperty nameProduct = new SimpleStringProperty();
    private final StringProperty qty = new SimpleStringProperty();
    private final StringProperty priceNET = new SimpleStringProperty();
    private final StringProperty total = new SimpleStringProperty();
    private final StringProperty priceVAT = new SimpleStringProperty();
    private final StringProperty cogs = new SimpleStringProperty();
    private final StringProperty priceCost = new SimpleStringProperty();
    private final StringProperty invoiceNumber = new SimpleStringProperty();
    private final IntegerProperty manufacturerID = new SimpleIntegerProperty();
    private final IntegerProperty countryID = new SimpleIntegerProperty();
    private final IntegerProperty unitID = new SimpleIntegerProperty();
    private final IntegerProperty vehicleTypeID = new SimpleIntegerProperty();
    private final IntegerProperty businessID = new SimpleIntegerProperty();
    private final IntegerProperty paymentID = new SimpleIntegerProperty();
    private final IntegerProperty billID = new SimpleIntegerProperty();
    private final IntegerProperty sourceID = new SimpleIntegerProperty();
    private final IntegerProperty deliveryID = new SimpleIntegerProperty();
    private final IntegerProperty employeeID = new SimpleIntegerProperty();
    private final IntegerProperty statusVAT = new SimpleIntegerProperty();
    private final StringProperty contractID = new SimpleStringProperty();
    private final StringProperty remark = new SimpleStringProperty();
    private final IntegerProperty typeCartID = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> deliveryTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> reportDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> lastTime = new SimpleObjectProperty<>();
    private final StringProperty grossPriceVAT = new SimpleStringProperty();
    private final StringProperty parameter = new SimpleStringProperty();


    public CartFX() {
    }

    public CartFX(CartFX c) {
        this.cartAID.set(c.getCartAID().get());
        this.accountID.set(c.getAccountID().get());
        this.creator.set(c.getCreator().get());
        this.productAID.set(c.getProductAID().get());
        this.productID.set(c.getProductID().get());
        this.productAIDVAT.set(c.getProductAIDVAT().get());
        this.productIDVAT.set(c.getProductIDVAT().get());
        this.id_PartNo.set(c.getId_PartNo().get());
        this.nameProduct.set(c.getNameProduct().get());
        this.qty.set(c.getQty().get());
        this.priceNET.set(c.getPriceNET().get());
        this.total.set(c.getTotal().get());
        this.priceVAT.set(c.getPriceVAT().get());
        this.cogs.set(c.getCogs().get());
        this.priceCost.set(c.getPriceCost().get());
        this.invoiceNumber.set(c.getInvoiceNumber().get());

        this.manufacturerID.set(c.getManufacturerID().get());
        this.countryID.set(c.getCountryID().get());
        this.unitID.set(c.getUnitID().get());
        this.vehicleTypeID.set(c.getVehicleTypeID().get());
        this.businessID.set(c.getBusinessID().get());
        this.paymentID.set(c.getPaymentID().get());
        this.billID.set(c.getBillID().get());
        this.sourceID.set(c.getSourceID().get());
        this.deliveryID.set(c.getDeliveryID().get());
        this.employeeID.set(c.getEmployeeID().get());
        this.statusVAT.set(c.getStatusVAT().get());
        this.typeCartID.set(c.getTypeCartID().get());

        this.contractID.set(c.getContractID().get());
        this.remark.set(c.getRemark().get());

        this.deliveryTime.set(c.getDeliveryTime().get());
        this.reportDate.set(c.getReportDate().get());
        this.lastTime.set(c.getLastTime().get());
        this.grossPriceVAT.set(c.getGrossPriceVAT().get());
        this.parameter.set(c.getParameter().get());
    }

    public StringProperty getCartAID() {
        return cartAID;
    }

    public IntegerProperty getAccountID() {
        return accountID;
    }

    public StringProperty getCreator() {
        return creator;
    }

    public StringProperty getGrossPriceVAT() {
        return grossPriceVAT;
    }

    public StringProperty getParameter() {
        return parameter;
    }

    public StringProperty getPriceCost() {
        return priceCost;
    }

    public StringProperty getInvoiceNumber() {
        return invoiceNumber;
    }

    public StringProperty getProductAID() {
        return productAID;
    }

    public StringProperty getProductID() {
        return productID;
    }

    public StringProperty getId_PartNo() {
        return id_PartNo;
    }

    public StringProperty getNameProduct() {
        return nameProduct;
    }

    public IntegerProperty getManufacturerID() {
        return manufacturerID;
    }

    public IntegerProperty getCountryID() {
        return countryID;
    }

    public IntegerProperty getUnitID() {
        return unitID;
    }

    public IntegerProperty getVehicleTypeID() {
        return vehicleTypeID;
    }

    public IntegerProperty getBusinessID() {
        return businessID;
    }

    public IntegerProperty getPaymentID() {
        return paymentID;
    }

    public IntegerProperty getBillID() {
        return billID;
    }

    public IntegerProperty getSourceID() {
        return sourceID;
    }

    public IntegerProperty getDeliveryID() {
        return deliveryID;
    }

    public IntegerProperty getEmployeeID() {
        return employeeID;
    }

    public IntegerProperty getStatusVAT() {
        return statusVAT;
    }

    public StringProperty getContractID() {
        return contractID;
    }

    public StringProperty getRemark() {
        return remark;
    }

    public IntegerProperty getTypeCartID() {
        return typeCartID;
    }

    public ObjectProperty<LocalDate> getLastTime() {
        return lastTime;
    }

    public ObjectProperty<LocalDate> getDeliveryTime() {
        return deliveryTime;
    }

    public StringProperty getProductAIDVAT() {
        return productAIDVAT;
    }

    public StringProperty getProductIDVAT() {
        return productIDVAT;
    }

    public StringProperty getQty() {
        return qty;
    }

    public StringProperty getPriceNET() {
        return priceNET;
    }

    public StringProperty getTotal() {
        return total;
    }

    public StringProperty getPriceVAT() {
        return priceVAT;
    }

    public StringProperty getCogs() {
        return cogs;
    }

    

    // ===== helper parse int (dùng khi paste Excel)
    public void setIntSafe(IntegerProperty prop, String value) {
        try {
            prop.set(Integer.parseInt(value));
        } catch (Exception e) {
            prop.set(0);
        }
    }

    public ObjectProperty<LocalDate> getReportDate() {
        return reportDate;
    }
}
