package com.phuthanh.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.phuthanh.model.info.Appendix;

public class ArrayCRUD {
        // List chứa tên các cột sản phẩm
        public List<String> productColumns = new ArrayList<>(Arrays.asList(
                        "ProductAID", "ProductIDMain", "ProductID",
                        "ID_Keeton", "ID_Industrial", "ID_PartNo",
                        "ID_ReplacedPartNo", "NameProduct", "Parameter", "VehicleTypeID",
                        "VehicleDetail", "VehicleCluster", "ManufacturerID", "CountryID", "SupplierID",
                        "SupplierActualID", "UnitID", "SegmentID", "HSCode", "Img1", "Img2", "Img3", "Remark",
                        "LastTime", "CreateTime", "CreateUser", "UpdateUser"));
        // List chứa tên các cột kho hàng
        public List<String> warehouseColumns = new ArrayList<>(Arrays.asList(
                        "DataWareHouseAID", "ProductAID", "Qty", "Qty_Expected", "ID_Bill", "LocationID",
                        "LastTime", "LastUser", "Remark"));
        // List chứa tên các cột lịch sử kho hàng
        public List<String> historyColumns = new ArrayList<>(Arrays.asList(
                        "HistoryAID", "DataWareHouseAID", "Qty", "ID_Employee", "Partner", "Remark",
                        "Time", "TransferGroupID", "LastUser", "LastTime"));
        // List chứa tên các cột yêu cầu sản phẩm
        public List<String> requestProductColumns = new ArrayList<>(Arrays.asList(
                        "RequestAID", "ProductAID", "ProductID", "ID_Keeton", "ID_Industrial", "ID_PartNo",
                        "ID_ReplacedPartNo", "NameProduct", "Parameter", "VehicleTypeID",
                        "VehicleDetail", "VehicleCluster", "ManufacturerID", "CountryID", "SupplierID",
                        "SupplierActualID", "UnitID", "Img1", "Img2", "Img3", "RemarkOfProduct", "LastTimeOfProduct",
                        "UserRequest", "TimeRequest", "UserConfirm", "TimeConfirm",
                        "RemarkOfRequest", "LastTime"));
        // List chứa tên các cột yêu cầu kho
        public List<String> requestWareHouseColumns = new ArrayList<>(Arrays.asList(
                        "RequestAID", "DataWareHouseAID", "DataWareHouseID", "ProductAID", "Qty", "Qty_Expected",
                        "ID_Bill", "LocationID", "LastTimeOfWareHouse", "LastUser",
                        "RemarkOfWareHouse", "UserRequest", "TimeRequest", "UserConfirm", "TimeConfirm",
                        "RemarkOfRequest", "LastTime"));
        // List chứa tên các cột cập nhật lịch sử
        public List<String> updateHistoryColumns = new ArrayList<>(Arrays.asList(
                        "UpdateAID", "HistoryAID", "DataWareHouseAID", "Qty", "ID_Employee", "Partner", "Remark",
                        "Time", "LastUser", "LastTime"));

        public List<String> requestHistoryDataWareHouse = new ArrayList<>(Arrays.asList(
                        "RequestAID", "HistoryAID", "DataWareHouseAID", "Qty", "ID_Employee", "Partner", "Remark",
                        "TransferGroupID", "Time", "LastUser", "LastTimeOfHistory", "UserRequest", "TimeRequest",
                        "UserConfirm",
                        "TimeConfirm", "Action", "LastTime"));

        public List<String> detailsProduct = new ArrayList<>(Arrays.asList(
                        "PartNoAID", "ProductID", "ID_PartNo", "PartNoID", "NameEnglish", "NameVietNamese", "PartNoQty",
                        "Parameter", "Remark", "LastTime"));

        public List<String> sheetColumns = new ArrayList<>(Arrays.asList(
                        "SheetAID", "SheetID", "Status", "Remark", "LastUser", "LastTime"));
        public List<String> sheetDataColumns = new ArrayList<>(Arrays.asList(
                        "CheckAID", "SheetAID", "ProductAID", "QtyWareHouse", "QtyCheck", "QtyDifferent", "Remark",
                        "LastUser", "LastTime"));
        public final Map<String, String> HEADER_MAPPING = Map.ofEntries(
                        Map.entry("ma keeton", "ID_Keeton"),
                        Map.entry("ma cong nghiep", "ID_Industrial"),
                        Map.entry("danh diem", "ID_PartNo"),
                        Map.entry("bo danh diem tuong duong", "ID_ReplacedPartNo"),

                        Map.entry("ten san pham", "NameProduct"),
                        Map.entry("thong so", "Parameter"),

                        Map.entry("hang xe", "VehicleTypeID"),
                        Map.entry("dong xe", "VehicleDetail"),
                        Map.entry("cum xe", "VehicleCluster"),

                        Map.entry("hang san xuat", "ManufacturerID"),
                        Map.entry("nuoc san xuat", "CountryID"),

                        Map.entry("nha cung cap thuc te", "SupplierActualID"),
                        Map.entry("nha cung cap hop dong", "SupplierID"),

                        Map.entry("dvt", "UnitID"),
                        Map.entry("hscode", "HSCode"),

                        Map.entry("hinh 1", "Img1"),
                        Map.entry("hinh 2", "Img2"),
                        Map.entry("hinh 3", "Img3"),

                        Map.entry("ghi chu", "Remark"));
        public List<String> guaranteeColumns = new ArrayList<>(Arrays.asList(
                        "GuaranteeAID", "GuaranteeID", "ProductBroken", "TimeStart", "TimeBroken", "TimeUsage",
                        "ReasonBroken", "ProductGuarantee", "TimeGuarantee", "SupplierGuarantee", "Qty", "Img1", "Img2",
                        "Img3", "Remark", "LastUser", "LastTime"));

        public List<String> cartColumns = new ArrayList<>(Arrays.asList(
                        "CartAID", "CartID", "AccountID", "ProductAID", "ProductAIDVAT", "ID_PartNo", "NameProduct",
                        "ManufacturerID", "CountryID", "UnitID", "VehicleTypeID", "Parameter","SupplierID", "BusinessID", "Qty",
                        "PriceNET",
                        "Total", "Cogs", "PriceVAT", "GrossPriceVAT", "PaymentID", "BillID", "SourceID", "DeliveryID",
                        "EmployeeID", "Status", "DeliveryTime", "ReportDate", "StatusVAT", "ContractID", "PriceCost",
                        "InvoiceNumber", "Remark", "TypeCartID", "LastTime"));

        public List<String> requestCartColumns = new ArrayList<>(Arrays.asList(
                        "RequestAID", "CartAID", "CartID", "AccountID", "ProductAID", "ProductAIDVAT", "ID_PartNo",
                        "NameProduct", "ManufacturerID", "CountryID", "UnitID", "VehicleTypeID", "Parameter", "SupplierID",
                        "BusinessID", "Qty",
                        "PriceNET", "Total", "Cogs", "PriceVAT", "GrossPriceVAT", "PaymentID", "BillID", "SourceID",
                        "DeliveryID", "EmployeeID", "Status", "DeliveryTime", "ReportDate", "StatusVAT", "ContractID",
                        "PriceCost", "InvoiceNumber", "Remark", "TypeCartID", "LastTime", "UserRequest", "TimeRequest",
                        "UserConfirm", "TimeConfirm", "Action", "RemarkOfRequest", "LastTimeOfRequest"));
        public Map<String, Appendix> CONFIG = Map.ofEntries(
                        Map.entry("Manufacturer", new Appendix("Manufacturer", "ManufacturerID", "Name")),
                        Map.entry("VehicleType", new Appendix("VehicleType", "VehicleTypeID", "VehicleTypeName")),
                        Map.entry("Country", new Appendix("Country", "CountryID", "Name")),
                        Map.entry("Employee", new Appendix("Employee", "EmployeeID", "NameEmployee")),

                        Map.entry("Supplier", new Appendix("Supplier", "SupplierID", "Name")),
                        Map.entry("SupplierInCountry", new Appendix("Supplier", "SupplierID", "Name")),
                        Map.entry("SupplierOutSideCountry", new Appendix("Supplier", "SupplierID", "Name")),
                        Map.entry("Customer", new Appendix("Supplier", "SupplierID", "Name")),
                        Map.entry("WareHouse", new Appendix("Supplier", "SupplierID", "Name")),

                        Map.entry("Unit", new Appendix("Unit", "UnitID", "Name")),
                        Map.entry("Business", new Appendix("Business", "Business", "Name", true)),
                        Map.entry("Payment", new Appendix("Payment", "PaymentID", "Name")),
                        Map.entry("Bill", new Appendix("Bill", "BillID", "Name", true)),
                        Map.entry("Location", new Appendix("Location", "LocationID", "NameLocation")),
                        Map.entry("Contract", new Appendix("Contract", "ContractID", "Name")),
                        Map.entry("Segment", new Appendix("Segment", "SegmentID", "Name")),
                        Map.entry("Purpose", new Appendix("Purpose", "PurposeID", "Name")),
                        Map.entry("TypeCart", new Appendix("TypeCart", "TypeCartID", "Name", true)));

}
