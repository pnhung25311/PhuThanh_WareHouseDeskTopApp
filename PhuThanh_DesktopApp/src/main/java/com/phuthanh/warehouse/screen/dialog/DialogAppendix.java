package com.phuthanh.warehouse.screen.dialog;

import java.util.Optional;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.TabViewHelper;
import com.phuthanh.manager.TableViewManager;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.info.Appendix;
import com.phuthanh.model.warehouse.OptionAction;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

public class DialogAppendix {
    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<OptionAction> cbFilter;

    @FXML
    private Button btnCreate;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnDelete;

    @FXML
    private TableView<ObservableList<String>> tableView;
    private ObservableList<ObservableList<String>> masterData = FXCollections.observableArrayList();
    private FilteredList<ObservableList<String>> filteredData;
    private static final DbTableHelper dbTableHelper = new DbTableHelper();
    private static final TableViewManager tableViewManager = new TableViewManager();
    private static final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private static final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private static final TabViewHelper tabViewHelper = new TabViewHelper();
    private boolean isEditMode = false;

    public void initialize() {
        loadComboBox();
        tableViewManager.setupTableView(tableView, masterData);
        tabViewHelper.clickItemSaveAID(tableView);
        Account account = AppState.getInstance().get("Account", Account.class);
        if (account.getRole().equals("ADMIN")) {
            isEditMode = true;
        }

        // Load dữ liệu mặc định
        dbTableHelper.loadTableConvertAppendix(
                tableView,
                "",
                "SELECT ManufacturerID AS IdAppendix, Name AS NameAppendix FROM Manufacturer");

        // Gán dữ liệu vào masterData
        masterData.setAll(tableView.getItems());

        // Áp dụng search (CHỈ 1 LẦN)
        applySearch();

        cbFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadTableData();
            }
            txtSearch.clear();
        });

    }

    private void loadComboBox() {
        OptionAction vehicleType = new OptionAction("VehicleType", "Hãng xe");
        OptionAction manufacturer = new OptionAction("Manufacturer", "Hãng sản xuất");
        OptionAction country = new OptionAction("Country", "Quốc gia");
        OptionAction employee = new OptionAction("Employee", "Nhân viên");
        // OptionAction supplierActual = new OptionAction("SupplierActual", "Nhà cung
        // cấp thực tế");
        OptionAction supplier = new OptionAction("Supplier", "Nhà cung cấp");
        OptionAction supplierInCountry = new OptionAction("SupplierInCountry", "Nhà cung cấp trong nước");
        OptionAction supplierOutSideCountry = new OptionAction("SupplierOutSideCountry", "Nhà cung cấp ngoài nước");
        OptionAction wareHouse = new OptionAction("WareHouse", "Kho");
        OptionAction customer = new OptionAction("Customer", "Khách hàng");
        OptionAction unit = new OptionAction("Unit", "Đơn vị tính");
        OptionAction payment = new OptionAction("Payment", "Hình thức thanh toán");
        OptionAction bill = new OptionAction("Bill", "Hóa đơn");
        OptionAction location = new OptionAction("Location", "Vị trí");
        OptionAction segment = new OptionAction("Segment", "Mảng kinh doanh");
        OptionAction purpose = new OptionAction("Purpose", "Mục đích");
        OptionAction typeCart = new OptionAction("TypeCart", "Loại phiếu hàng");

        // OptionAction contract = new OptionAction("Contract", "Hợp đồng");
        cbFilter.getItems().addAll(manufacturer, vehicleType, country, employee, supplier, supplierInCountry,
                supplierOutSideCountry, wareHouse, customer, unit, payment, bill, location, segment, purpose, typeCart);
        cbFilter.getSelectionModel().selectFirst();
    }

    private void loadTableData() {
        String actionId = cbFilter.getValue().getId();
        Appendix cfg = ArrayCRUD.CONFIG.get(actionId);

        if (cfg == null)
            return;

        String sql = "SELECT " + cfg.getIdCol() + " AS IdAppendix, "
                + cfg.getNameCol() + " AS NameAppendix FROM " + cfg.getTable();

        // các filter riêng Supplier
        if (actionId.equals("SupplierInCountry"))
            sql += " WHERE Category = 2";
        if (actionId.equals("SupplierOutSideCountry"))
            sql += " WHERE Category = 1";
        if (actionId.equals("Customer"))
            sql += " WHERE Category = 3";
        if (actionId.equals("WareHouse"))
            sql += " WHERE Category = 4";

        dbTableHelper.loadTableConvertAppendix(tableView, actionId, sql);
        masterData.setAll(tableView.getItems());
        applySearch();
    }

    private void applySearch() {
        filteredData = new FilteredList<>(masterData, p -> true);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal == null ? "" : newVal.toLowerCase();

            filteredData.setPredicate(row -> {
                if (keyword.isEmpty())
                    return true;

                // Tìm trong toàn bộ cột
                for (String cell : row) {
                    if (cell != null && cell.toLowerCase().contains(keyword)) {
                        return true;
                    }
                }
                return false;
            });
        });

        SortedList<ObservableList<String>> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    @FXML
    private void onCreate() {
        String actionId = cbFilter.getValue().getId();
        Appendix cfg = ArrayCRUD.CONFIG.get(actionId);
        if (cfg == null)
            return;

        if (cfg.isAdminOnly() && !isEditMode) {
            customDialogNotification.showDialog("Thông tin", "Chỉ ADMIN mới có quyền thêm", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Nhập tên mới");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            if (name.isBlank())
                return;

            String sql = "INSERT INTO " + cfg.getTable() + "(" + cfg.getNameCol()+ ") VALUES (N'" + name + "')";

            // category riêng supplier
            if (actionId.contains("Supplier") || actionId.equals("Customer") || actionId.equals("WareHouse")) {
                int cat = switch (actionId) {
                    case "SupplierInCountry" -> 2;
                    case "SupplierOutSideCountry" -> 1;
                    case "Customer" -> 3;
                    case "WareHouse" -> 4;
                    default -> 0;
                };
                if (cat != 0)
                    sql = "INSERT INTO Supplier(Name,Category) VALUES (N'" + name + "'," + cat + ")";
            }

            dbCRUDHelper.executeUpdate(sql);
            loadTableData();
        });
    }

    @FXML
    private void onUpdate() {

        String id = tabViewHelper.getSelectedAID();

        if (id == null) {
            customDialogNotification.showDialog(
                    "Thông báo",
                    "Vui lòng chọn dữ liệu cần sửa",
                    Alert.AlertType.WARNING);
            return;
        }

        // Lấy tên hiện tại từ table (cột 1)
        ObservableList<String> row = tableView.getSelectionModel().getSelectedItem();
        String oldName = row.get(1);

        OptionAction selected = cbFilter.getSelectionModel().getSelectedItem();
        String actionId = selected.getId();

        TextInputDialog dialog = new TextInputDialog(oldName);
        dialog.setTitle("Cập nhật phụ lục");
        dialog.setHeaderText("Nhập tên mới");
        dialog.setContentText("Tên:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {

            if (name.trim().isEmpty())
                return;

            String sql = "";

            switch (actionId) {

                case "Manufacturer":
                    sql = "UPDATE Manufacturer SET Name=N'" + name + "' WHERE ManufacturerID=" + id;
                    break;

                case "VehicleType":
                    sql = "UPDATE VehicleType SET VehicleTypeName=N'" + name + "' WHERE VehicleTypeID=" + id;
                    break;

                case "Country":
                    sql = "UPDATE Country SET Name=N'" + name + "' WHERE CountryID=" + id;
                    break;

                case "Employee":
                    sql = "UPDATE Employee SET NameEmployee=N'" + name + "' WHERE EmployeeID=" + id;
                    break;

                case "SupplierInCountry":
                case "SupplierOutSideCountry":
                case "Customer":
                case "WareHouse":
                    sql = "UPDATE Supplier SET Name=N'" + name + "' WHERE SupplierID=" + id;
                    break;

                case "Unit":
                    sql = "UPDATE Unit SET Name=N'" + name + "' WHERE UnitID=" + id;
                    break;

                case "Payment":
                    sql = "UPDATE Payment SET Name=N'" + name + "' WHERE PaymentID=" + id;
                    break;

                case "Bill":
                    if (isEditMode) {
                        sql = "UPDATE Bill SET Name=N'" + name + "' WHERE BillID=" + id;

                    } else {
                        customDialogNotification.showDialog(
                                "Thông tin",
                                "Chỉ ADMIN mới có quyền sửa tên hóa đơn",
                                Alert.AlertType.WARNING);
                        return;
                    }
                    break;

                case "Location":
                    sql = "UPDATE Location SET NameLocation=N'" + name + "' WHERE LocationID=" + id;
                    break;

                case "Contract":
                    sql = "UPDATE Contract SET Name=N'" + name + "' WHERE ContractID=" + id;
                    break;
                case "Segment":
                    sql = "UPDATE Segment SET Name=N'" + name + "' WHERE SegmentID=" + id;
                    break;
                case "Purpose":
                    sql = "UPDATE Purpose SET Name=N'" + name + "' WHERE PurposeID=" + id;
                    break;

                case "TypeCart":
                    if (isEditMode) {
                        sql = "UPDATE TypeCart SET Name=N'" + name + "' WHERE TypeCartID=" + id;

                    } else {
                        customDialogNotification.showDialog(
                                "Thông tin",
                                "Chỉ ADMIN mới có quyền sửa tên loại phiếu",
                                Alert.AlertType.WARNING);
                        return;
                    }
                    break;
            }

            if (!sql.isEmpty()) {
                dbCRUDHelper.executeUpdate(sql);
                loadTableData();
            }
        });
    }

    @FXML
    private void onDelete() {

        String id = tabViewHelper.getSelectedAID();

        if (id == null) {
            customDialogNotification.showDialog(
                    "Thông báo",
                    "Vui lòng chọn dữ liệu cần xóa",
                    Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa?");
        confirm.setContentText("Dữ liệu sẽ không thể khôi phục!");

        Optional<javafx.scene.control.ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != javafx.scene.control.ButtonType.OK)
            return;

        OptionAction selected = cbFilter.getSelectionModel().getSelectedItem();
        String actionId = selected.getId();

        String sql = "";

        switch (actionId) {

            case "Manufacturer":
                sql = "DELETE FROM Manufacturer WHERE ManufacturerID=" + id;
                break;

            case "VehicleType":
                sql = "DELETE FROM VehicleType WHERE VehicleTypeID=" + id;
                break;

            case "Country":
                sql = "DELETE FROM Country WHERE CountryID=" + id;
                break;

            case "Employee":
                sql = "DELETE FROM Employee WHERE EmployeeID=" + id;
                break;

            case "SupplierInCountry":
            case "SupplierOutSideCountry":
            case "Customer":
            case "WareHouse":
                sql = "DELETE FROM Supplier WHERE SupplierID=" + id;
                break;

            case "Unit":
                sql = "DELETE FROM Unit WHERE UnitID=" + id;
                break;

            case "Payment":
                sql = "DELETE FROM Payment WHERE PaymentID=" + id;
                break;

            case "Bill":
                if (isEditMode) {
                    sql = "DELETE FROM Bill WHERE BillID=" + id;
                } else {
                    customDialogNotification.showDialog(
                            "Thông tin",
                            "Chỉ ADMIN mới có quyền xóa hóa đơn",
                            Alert.AlertType.WARNING);
                    return;
                }
                break;

            case "Location":
                sql = "DELETE FROM Location WHERE LocationID=" + id;
                break;

            case "Contract":
                sql = "DELETE FROM Contract WHERE ContractID=" + id;
                break;
            case "Segment":
                sql = "DELETE FROM Segment WHERE SegmentID=" + id;
                break;
            case "Purpose":
                sql = "DELETE FROM Purpose WHERE PurposeID=" + id;
                break;
            case "TypeCart":
                if (isEditMode) {
                    sql = "DELETE FROM TypeCart WHERE TypeCartID=" + id;
                } else {
                    customDialogNotification.showDialog(
                            "Thông tin",
                            "Chỉ ADMIN mới có quyền xóa loại phiếu",
                            Alert.AlertType.WARNING);
                    return;
                }
                break;
        }

        if (!sql.isEmpty()) {
            dbCRUDHelper.executeUpdate(sql);
            loadTableData();
        }
    }

}
