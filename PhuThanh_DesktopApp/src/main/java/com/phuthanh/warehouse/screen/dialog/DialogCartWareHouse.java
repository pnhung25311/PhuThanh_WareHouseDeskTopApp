package com.phuthanh.warehouse.screen.dialog;

import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.HashMap;
// import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.FunctionExportExcel;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.helper.TabViewHelper;
import com.phuthanh.manager.TableViewManager;
import com.phuthanh.model.helper.ExcelColumn;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.warehouse.CCBdata;
import com.phuthanh.store.AppState;
import com.phuthanh.warehouse.EditableTableView.tableView.cart.EditableTableViewCreateCart;
import com.phuthanh.warehouse.EditableTableView.tableView.cart.EditableTableViewUpdateCart;
import com.phuthanh.warehouse.contextmenu.TabContextMenuCart;
import com.phuthanh.warehouse.helper.CartFilterManager;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
// import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
// import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogCartWareHouse {

    // SEARCH
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnReload;
    @FXML
    private Button btnCreate;

    @FXML
    private DatePicker dpFromDate;
    @FXML
    private DatePicker dpToDate;

    // TABLE
    @FXML
    private TableView<ObservableList<String>> tabCart, tabRequest;
    @FXML
    private TabPane tabPane;

    // FOOTER
    @FXML
    private Button btnExportExcel;
    @FXML
    private Button btnCancel;
    @FXML
    private ComboBox<CCBdata> cbbTypeCart;

    private final TabViewHelper tabViewHelper = new TabViewHelper();
    // private final TableViewManager tableViewManager = new
    // TableViewManager();
    private final DbTableHelper dbTableHelper = new DbTableHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final FunctionExportExcel functionExportExcel = new FunctionExportExcel();
    private final TabContextMenuCart tabContextMenuCart = new TabContextMenuCart();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private CartFilterManager filterManager;

    private ObservableList<ObservableList<String>> allDataCart;

    private ObservableList<ObservableList<String>> allDataRequest;
    private final Map<TableView<?>, TableViewManager> tableManagers = new HashMap<>();
    // lưu trạng thái checkbox của từng row theo table
    // Table -> (RowID -> BooleanProperty)
    private final Map<TableView<?>, Map<String, BooleanProperty>> checkMap = new HashMap<>();

    // ================= INIT =================
    public void initialize() {
        // tạo filter manager
        setCurrentMonth(dpFromDate, dpToDate); // set ngày trước
        loadComboBox(); // load dữ liệu cho combobox trước (nếu có)
        cbbTypeCartListener(); // gắn listener cho combobox (nếu có)

        filterManager = new CartFilterManager(txtSearch, dpFromDate, dpToDate);

        // gắn listener auto filter
        filterManager.attachAutoFilter(() -> applySearchFilter());
        // cbStatus.setManaged(false);
        loadDataCart(); // load + auto filter
        tabViewHelper.clickItemSaveAID(tabCart);
        tabViewHelper.clickItemSaveAID(tabRequest);

        tabContextMenuCart.attachDefaultContextMenu(
                tabCart,
                () -> tabViewHelper.getSelectedAID(),
                () -> loadDataCart());
        tabContextMenuCart.attachDefaultContextMenuRequest(
                tabRequest,
                () -> tabViewHelper.getSelectedAID(),
                () -> loadDataCart());

        // btnCreate.setVisible(false);

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTab, newTab) -> {
                    if (newTab != null) {
                        System.out.println("Đang chọn tab: " + newTab.getText());
                    }
                });
        listenerDatePicker();
    }

    private void loadComboBox() {
        cbbTypeCart.getItems().addAll(
                new CCBdata(0, "Tổng hợp"),
                new CCBdata(1, "Phiếu nhập"),
                new CCBdata(2, "Phiếu xuất"),
                new CCBdata(3, "Phiếu điều chuyển"));
        cbbTypeCart.getSelectionModel().selectFirst();
    }

    private void cbbTypeCartListener() {
        cbbTypeCart.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != oldVal) {
                loadDataCart();
            }
        });
    }

    private void listenerDatePicker() {
        dpFromDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                loadDataCart();
            }
        });
        dpToDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                loadDataCart();
            }
        });
    }

    private String convertSQLquery() {
        String fromdate = dpFromDate.getValue().toString();
        String todate = dpToDate.getValue().toString();
        int typeCartID = cbbTypeCart.getValue().getId();
        Account account = AppState.getInstance().get("Account", Account.class);
        int getAccountID = account.getAccountID();
        int getEmployeeID = account.getEmployeeID();

        String sql = "";
        if (account.getRole().equals("ADMIN")
                || account.getRole().equals("ACCOUNTANT")) {
            switch (typeCartID) {
                case 0:
                    sql += "SELECT * FROM vwCart WHERE dbo.fnFromDateToDate(DeliveryTime, '" + fromdate + "', '"
                            + todate
                            + "') = 1 ORDER BY LastTime DESC, CartAID DESC";
                    break;
                case 1:
                    sql += "SELECT * FROM vwCart WHERE TypeCartID = 1 AND dbo.fnFromDateToDate(DeliveryTime, '"
                            + fromdate
                            + "', '" + todate + "') = 1 ORDER BY LastTime DESC, CartAID DESC";
                    break;
                case 2:
                    sql += "SELECT * FROM vwCart WHERE TypeCartID = 2 AND dbo.fnFromDateToDate(DeliveryTime, '"
                            + fromdate
                            + "', '" + todate + "') = 1 ORDER BY LastTime DESC, CartAID DESC";
                    break;
                case 3:
                    sql += "SELECT * FROM vwCart WHERE TypeCartID = 3 AND dbo.fnFromDateToDate(DeliveryTime, '"
                            + fromdate
                            + "', '" + todate + "') = 1 ORDER BY LastTime DESC, CartAID DESC";
                    break;
                default:
                    break;
            }
        } else if (account.getRole().equals("WAREHOUSE")) {
            switch (typeCartID) {
                case 0:
                    sql += "SELECT * FROM vwCart WHERE dbo.fnFromDateToDate(DeliveryTime, '" + fromdate + "', '"
                            + todate
                            + "') = 1  AND (DeliveryID IN (41,42,43,44,45,236) OR SourceID IN (41,42,43,44,45,236) )  ORDER BY LastTime DESC, CartAID DESC";
                    break;
                case 1:
                    sql += "SELECT * FROM vwCart WHERE TypeCartID = 1 AND dbo.fnFromDateToDate(DeliveryTime, '"
                            + fromdate
                            + "', '" + todate
                            + "') = 1  AND (DeliveryID IN (41,42,43,44,45,236) OR SourceID IN (41,42,43,44,45,236) )  ORDER BY LastTime DESC, CartAID DESC";
                    break;
                case 2:
                    sql += "SELECT * FROM vwCart WHERE TypeCartID = 2 AND dbo.fnFromDateToDate(DeliveryTime, '"
                            + fromdate
                            + "', '" + todate
                            + "') = 1  AND (DeliveryID IN (41,42,43,44,45,236) OR SourceID IN (41,42,43,44,45,236) )  ORDER BY LastTime DESC, CartAID DESC";
                    break;
                case 3:
                    sql += "SELECT * FROM vwCart WHERE TypeCartID = 3 AND dbo.fnFromDateToDate(DeliveryTime, '"
                            + fromdate
                            + "', '" + todate
                            + "') = 1  AND (DeliveryID IN (41,42,43,44,45,236) OR SourceID IN (41,42,43,44,45,236) )  ORDER BY LastTime DESC, CartAID DESC";
                    break;
                default:
                    break;
            }
        } else {
            switch (typeCartID) {
                case 0:
                    sql += "SELECT * FROM vwCart WHERE (AccountID = "
                            + getAccountID + " OR EmployeeID =" + getEmployeeID
                            + ") AND dbo.fnFromDateToDate(DeliveryTime, '" + fromdate + "', '" + todate
                            + "') = 1 ORDER BY LastTime DESC, CartAID DESC";
                    break;
                case 1:
                    sql += "SELECT * FROM vwCart WHERE TypeCartID = 1 AND (AccountID = "
                            + getAccountID + " OR EmployeeID =" + getEmployeeID
                            + ") AND dbo.fnFromDateToDate(DeliveryTime, '" + fromdate + "', '" + todate
                            + "') = 1 ORDER BY LastTime DESC, CartAID DESC";
                    break;
                case 2:
                    sql += "SELECT * FROM vwCart WHERE TypeCartID = 2 AND (AccountID = "
                            + getAccountID + " OR EmployeeID =" + getEmployeeID
                            + ") AND dbo.fnFromDateToDate(DeliveryTime, '" + fromdate + "', '" + todate
                            + "') = 1 ORDER BY LastTime DESC, CartAID DESC";
                    break;
                case 3:
                    sql += "SELECT * FROM vwCart WHERE TypeCartID = 3 AND (AccountID = "
                            + getAccountID + " OR EmployeeID =" + getEmployeeID
                            + ") AND dbo.fnFromDateToDate(DeliveryTime, '" + fromdate + "', '" + todate
                            + "') = 1 ORDER BY LastTime DESC, CartAID DESC";
                    break;
                default:
                    break;
            }
        }

        return sql;
    }

    // ================= LOAD DATA =================
    private void loadDataCart() {
        checkMap.clear();
        allDataCart = FXCollections.observableArrayList();
        allDataRequest = FXCollections.observableArrayList();
        Account account = AppState.getInstance().get("Account", Account.class);
        int getAccountID = account.getAccountID();
        int getEmployeeID = account.getEmployeeID();
        String sql = convertSQLquery();
        if (account.getRole().equals("WAREHOUSE") || account.getRole().equals("ADMIN")) {
            allDataCart = dbTableHelper.loadDataTable(tabCart, sql);
            allDataRequest = dbTableHelper.loadDataTable(tabRequest,
                    "SELECT * FROM vwRequestCart ORDER BY LastTimeOfRequest DESC, CartAID DESC");
        } else {
            allDataCart = dbTableHelper.loadDataTable(tabCart, sql);
            allDataRequest = dbTableHelper.loadDataTable(tabRequest, "SELECT * FROM vwRequestCart WHERE AccountID = "
                    + getAccountID + " OR EmployeeID =" + getEmployeeID
                    + " ORDER BY LastTimeOfRequest DESC, CartAID DESC");
        }
        formatAllTableData(allDataCart);
        formatAllTableData(allDataRequest);

        tabCart.refresh();
        tabRequest.refresh();
        if (tabCart.getItems() != null) {
            functionHelper.printRowColumns(tabCart.getItems().get(0));
        }

        setTableData(tabCart, allDataCart);
        setTableData(tabRequest, allDataRequest);
        applySearchFilter();

    }

    private void applySearchFilter() {

        String keyword = txtSearch.getText();

        // filter ALL tables
        tableManagers.values().forEach(manager -> {
            if (manager.getFilteredData() != null) {
                manager.getFilteredData()
                        .setPredicate(row -> filterManager.matchRow(row, keyword));
            }
        });
    }

    // ================= ACTION =================
    @FXML
    private void onReload() {
        // 🔥 clear toàn bộ checkbox của mọi table
        checkMap.values().forEach(rowMap -> rowMap.values().forEach(p -> p.set(false)));
        tableManagers.values().forEach(TableViewManager::clearAllFilters);
        txtSearch.clear();
        loadDataCart();
    }

    @FXML
    private void onResetFilter() {
        tableManagers.values().forEach(TableViewManager::clearAllFilters);
        setCurrentMonth(dpFromDate, dpToDate);
        txtSearch.clear();
        applySearchFilter();
    }

    @FXML
    private void onCreate() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCreateCart.fxml"));

            Parent root = loader.load();

            DialogCreateCartImport controller = loader.getController();
            controller.setInitialData(() -> loadDataCart(), "CREATE", null, null, null);

            Stage dialog = new Stage();
            dialog.setTitle("Thêm đơn hàng");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onExportExcelReport() {
        Stage stage = (Stage) tabPane.getScene().getWindow();

        // Lấy tab đang được chọn
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        if (selectedTab == null) {
            customDialogNotification.showDialog("Lỗi", "Không có tab nào được chọn",
                    Alert.AlertType.ERROR);
            return;
        }

        // Lấy TableView tương ứng với tab được chọn
        TableView<ObservableList<String>> selectedTable = getSelectedTableView();

        if (selectedTable == null) {
            customDialogNotification.showDialog("Lỗi",
                    "Không tìm thấy dữ liệu trong tab: " + selectedTab.getText(),
                    Alert.AlertType.ERROR);
            return;
        }

        // Kiểm tra table có dữ liệu không
        if (selectedTable.getItems().isEmpty()) {
            customDialogNotification.showDialog("Thông báo",
                    "Tab '" + selectedTab.getText() + "' không có dữ liệu để xuất",
                    Alert.AlertType.WARNING);
            return;
        }

        // Xuất Excel table đang chọn
        ObservableList<ObservableList<String>> checkedRows = getCheckedRows(selectedTable);

        if (checkedRows.isEmpty()) {
            customDialogNotification.showDialog(
                    "Thông báo",
                    "Vui lòng chọn ít nhất 1 dòng để export",
                    Alert.AlertType.WARNING);
            return;
        }

        // tạo table tạm chỉ chứa row đã chọn
        TableView<ObservableList<String>> tempTable = new TableView<>();
        tempTable.getColumns().addAll(selectedTable.getColumns().filtered(c -> !"SELECT_COL".equals(c.getId())));
        tempTable.setItems(checkedRows);

        boolean result = functionExportExcel.exportExcelByTemplate(tempTable, stage);

        if (result) {
            // clear tất cả checkbox sau khi export
            Map<String, BooleanProperty> rowMap = checkMap.get(selectedTable);
            if (rowMap != null) {
                rowMap.values().forEach(p -> p.set(false));
            }

            customDialogNotification.showDialog("Thành công",
                    "Xuất Excel tab '" + selectedTab.getText() + "' thành công",
                    Alert.AlertType.INFORMATION);
        } else {
            customDialogNotification.showDialog("Lỗi",
                    "Xuất Excel tab '" + selectedTab.getText() + "' thất bại",
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onExportExcel() {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        boolean result = functionHelper.exportExcel(tabCart, stage, "Đơn hàng");

        if (result) {
            customDialogNotification.showDialog("Thành công", "Xuất Excel thành công", Alert.AlertType.INFORMATION);
        } else {
            customDialogNotification.showDialog("Lỗi", "Xuất Excel thất bại", Alert.AlertType.ERROR);
        }
    }

    /**
     * Lấy TableView đang được chọn dựa vào tab active
     */
    private TableView<ObservableList<String>> getSelectedTableView() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        if (selectedTab == null) {
            return null;
        }

        String tabText = selectedTab.getText();

        switch (tabText) {
            case "Đơn hàng":
                return tabCart;
            case "Danh sách yêu cầu":
                return tabRequest;
            default:
                return null;
        }
    }

    @FXML
    private void onExportPDF() {

        Stage stage = (Stage) tabPane.getScene().getWindow();
        List<ExcelColumn> cols = List.of(
                new ExcelColumn("STT", -1),
                new ExcelColumn("Tên sản phẩm", 7),
                new ExcelColumn("Danh điểm", 6),
                new ExcelColumn("Hãng Sản Xuất", 9),
                new ExcelColumn("Xuất xứ", 11),
                new ExcelColumn("ĐVT", 13),
                new ExcelColumn("SL", 16),
                new ExcelColumn("Số HĐ", 100),
                new ExcelColumn("Ghi chú", 33),
                new ExcelColumn("Mã sản phẩm", 5));

        boolean result = functionHelper.exportPdfFromTemplate(tabCart, cols, stage);

        if (result) {
            customDialogNotification.showDialog("Thành công", "Xuất PDF thành công", Alert.AlertType.INFORMATION);
        } else {
            customDialogNotification.showDialog("Lỗi", "Xuất PDF thất bại", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onClose() {
        btnCancel.getScene().getWindow().hide();
    }

    @FXML
    private void onImprotExcel() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogImportExcelCart.fxml"));
            Parent root = loader.load();
            DialogImportExcelCart controller = loader.getController();
            controller.initData(() -> loadDataCart());

            Stage dialog = new Stage();
            dialog.setTitle("Nhập excel");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(true);
            // dialog.showAndWait();
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onImportTableView() {
        TabPane tabPane = new TabPane();
        Tab tabImport = new Tab("Nhập hàng");
        Tab tabExport = new Tab("Xuất hàng");
        Tab tabTransfer = new Tab("Điều chuyển");
        Tab tabUpdate = new Tab("Cập nhật");

        tabImport.setClosable(false);
        tabExport.setClosable(false);
        tabTransfer.setClosable(false);
        tabUpdate.setClosable(false);
        // tabProduct.setContent(new EditableTableViewCreateProduct().getTable());
        EditableTableViewCreateCart cartImport = new EditableTableViewCreateCart(1);
        EditableTableViewCreateCart cartExport = new EditableTableViewCreateCart(2);
        EditableTableViewCreateCart cartTransfer = new EditableTableViewCreateCart(3);
        EditableTableViewUpdateCart cartUpdate = new EditableTableViewUpdateCart();

        BorderPane rootImport = new BorderPane();
        rootImport.setTop(cartImport.createToolbar());
        rootImport.setCenter(cartImport.getTable());

        BorderPane rootExport = new BorderPane();
        rootExport.setTop(cartExport.createToolbar());
        rootExport.setCenter(cartExport.getTable());

        BorderPane rootTransfer = new BorderPane();
        rootTransfer.setTop(cartTransfer.createToolbar());
        rootTransfer.setCenter(cartTransfer.getTable());

        BorderPane rootUpdate = new BorderPane();
        rootUpdate.setTop(cartUpdate.createToolbar());
        rootUpdate.setCenter(cartUpdate.getTable());

        tabImport.setContent(rootImport);
        tabExport.setContent(rootExport);
        tabTransfer.setContent(rootTransfer);
        tabUpdate.setContent(rootUpdate);

        tabPane.getTabs().addAll(tabImport, tabExport, tabTransfer, tabUpdate);
        Stage dialog = new Stage();

        dialog.setScene(new Scene(tabPane, 1000, 600));
        dialog.setTitle("Nhập liệu sản phẩm");
        dialog.setResizable(true);

        dialog.show();
    }

    private void setCurrentMonth(DatePicker fromDate, DatePicker toDate) {
        LocalDate now = LocalDate.now();

        fromDate.setValue(now.withDayOfMonth(1));
        toDate.setValue(now.withDayOfMonth(now.lengthOfMonth()));
    }

    @FXML
    private void onReconciliation() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxmlBusiness/dialogReconciliation.fxml"));
            Parent root = loader.load();
            // DialogHistoryBusiness controller = loader.getController();
            // controller.initData("SELECT * FROM vwct90 ORDER BY ngay_ct DESC",
            // "SELECT * FROM vwct70y ORDER BY ngay_ct DESC"); // nếu cần truyền SQL tùy
            // chỉnh
            Stage dialog = new Stage();
            dialog.setTitle("Đối soát");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(true);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatNumberIfPossible(String value) {
        if (value == null || value.isBlank())
            return "";

        try {
            // bỏ dấu phẩy nếu có
            String clean = value.replace(",", "").trim();

            // parse số thập phân từ SQL
            double number = Double.parseDouble(clean);
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

            // nếu là số nguyên (27360000.00 -> 27360000)
            if (number == Math.floor(number)) {
                return new java.text.DecimalFormat("#,###", symbols)
                        .format(number);
            }

            // nếu còn phần lẻ (ví dụ 12.5)
            return new java.text.DecimalFormat("#,###.##", symbols)
                    .format(number);

        } catch (Exception e) {
            return value; // không phải số
        }
    }

    private final Set<Integer> NUMERIC_COLUMNS = Set.of(19, 20, 21, 22, 23, 24, 25);

    private void formatAllTableData(ObservableList<ObservableList<String>> data) {
        for (ObservableList<String> row : data) {
            for (int i = 0; i < row.size(); i++) {
                // row.set(i, formatNumberIfPossible(row.get(i)));
                // chỉ format các cột numeric
                if (NUMERIC_COLUMNS.contains(i)) {
                    row.set(i, formatNumberIfPossible(row.get(i)));
                }
            }
        }
    }

    private void setTableData(TableView<ObservableList<String>> table,
            ObservableList<ObservableList<String>> data) {

        if (data == null || data.isEmpty()) {
            System.err.println("⚠️ No data for table: " + table.getId());
            return;
        }

        TableViewManager manager = tableManagers.get(table);

        // chưa có manager → tạo mới
        if (manager == null) {
            System.out.println("🆕 Setup manager for: " + table);
            manager = new TableViewManager();
            manager.setupTableView(table, data);
            tableManagers.put(table, manager);
        }
        // đã có → reload data
        else {
            System.out.println("🔄 Reload data for: " + table);
            manager.reloadData(data);
        }
        addCheckBoxColumn(table);

    }

    private void addCheckBoxColumn(TableView<ObservableList<String>> table) {

        // nếu đã có rồi thì bỏ qua (tránh add lại khi reload)
        if (table.getColumns().stream().anyMatch(c -> "SELECT_COL".equals(c.getId())))
            return;

        TableColumn<ObservableList<String>, Boolean> colSelect = new TableColumn<>("✔");
        CheckBox selectAll = new CheckBox();
        colSelect.setGraphic(selectAll);
        colSelect.setId("SELECT_COL");
        colSelect.setPrefWidth(40);
        colSelect.setEditable(true);

        // tạo map cho table nếu chưa có
        checkMap.putIfAbsent(table, new HashMap<>());
        Map<String, BooleanProperty> rowMap = checkMap.get(table);

        selectAll.selectedProperty().addListener((obs, oldVal, isSelected) -> {

            for (BooleanProperty prop : rowMap.values()) {
                prop.set(isSelected);
            }

            table.refresh(); // update UI
        });

        // value factory (🔥 dùng RowID thay vì row object)
        colSelect.setCellValueFactory(param -> {
            ObservableList<String> row = param.getValue();
            String rowId = getRowId(row);

            rowMap.putIfAbsent(rowId, new SimpleBooleanProperty(false));

            BooleanProperty prop = rowMap.get(rowId);

            // 🔥 listener đồng bộ header
            prop.addListener((obs, oldVal, newVal) -> {

                boolean allChecked = rowMap.values().stream()
                        .allMatch(BooleanProperty::get);

                selectAll.setSelected(allChecked);
            });

            return prop;
        });

        // hiển thị checkbox
        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));

        // 👉 ADD CUỐI TABLE
        table.getColumns().add(colSelect);

        table.setEditable(true);
    }

    private ObservableList<ObservableList<String>> getCheckedRows(TableView<ObservableList<String>> table) {
        ObservableList<ObservableList<String>> result = FXCollections.observableArrayList();

        Map<String, BooleanProperty> rowMap = checkMap.get(table);
        if (rowMap == null)
            return result;

        // 🔥 loop theo row đang hiển thị trong table (sau filter/sort)
        for (ObservableList<String> row : table.getItems()) {
            String rowId = getRowId(row);
            BooleanProperty checked = rowMap.get(rowId);

            if (checked != null && checked.get()) {
                result.add(row);
            }
        }
        return result;
    }

    private String getRowId(ObservableList<String> row) {
        return row.get(0);
    }

}