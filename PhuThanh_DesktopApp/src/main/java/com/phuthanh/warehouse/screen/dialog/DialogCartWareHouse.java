package com.phuthanh.warehouse.screen.dialog;

import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.FunctionExportExcel;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.helper.TabViewHelper;
import com.phuthanh.helper.dialog.ConfigDialog;
import com.phuthanh.manager.TableViewManager;
import com.phuthanh.model.helper.ColumnConfig;
import com.phuthanh.model.helper.ExcelColumn;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.warehouse.CCBdata;
import com.phuthanh.store.AppState;
import com.phuthanh.warehouse.EditableTableView.tableView.cart.*;
import com.phuthanh.warehouse.contextmenu.TabContextMenuCart;
import com.phuthanh.warehouse.helper.CartFilterManager;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DialogCartWareHouse {

    // ================= FXML COMPONENTS =================
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnReload;
    @FXML
    private Button btnCreate;
    @FXML
    private DatePicker dpFromDate, dpToDate, dpFromDateReport, dpToDateReport;
    @FXML
    private TableView<ObservableList<String>> tabCart, tabRequest;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button btnExportExcel, btnCancel;
    @FXML
    private ComboBox<CCBdata> cbbTypeCart;

    // ================= HELPERS & MANAGERS =================
    private final TabViewHelper tabViewHelper = new TabViewHelper();
    private final DbTableHelper dbTableHelper = new DbTableHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final FunctionExportExcel functionExportExcel = new FunctionExportExcel();
    private final TabContextMenuCart tabContextMenuCart = new TabContextMenuCart();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final ConfigDialog configDialog = new ConfigDialog();

    private CartFilterManager filterManager;
    private ObservableList<ObservableList<String>> allDataCart;
    private ObservableList<ObservableList<String>> allDataRequest;

    private final Map<TableView<?>, TableViewManager> tableManagers = new HashMap<>();
    // Lưu trạng thái checkbox: Table -> (RowID -> BooleanProperty)
    private final Map<TableView<?>, Map<String, BooleanProperty>> checkMap = new HashMap<>();

    // Tập hợp lưu các listener toàn cục để tháo gỡ khi cleanup tránh memory leak
    private final Map<BooleanProperty, ChangeListener<Boolean>> activeListeners = new HashMap<>();

    private static final Set<Integer> NUMERIC_COLUMNS = Set.of(19, 20, 21, 22, 23, 24, 25, 26);

    // ================= INITIALIZATION =================
    public void initialize() {
        setCurrentMonth(dpFromDate, dpToDate);
        setCurrentNull(dpFromDateReport, dpToDateReport);
        loadComboBox();

        filterManager = new CartFilterManager(txtSearch, dpFromDate, dpToDate);
        filterManager.attachAutoFilter(this::applySearchFilter);

        loadDataCart();

        tabViewHelper.clickItemSaveAID(tabCart);
        tabViewHelper.clickItemSaveAID(tabRequest);

        tabContextMenuCart.attachDefaultContextMenu(tabCart, tabViewHelper::getSelectedAID, this::loadDataCart);
        tabContextMenuCart.attachDefaultContextMenuRequest(tabRequest, tabViewHelper::getSelectedAID,
                this::loadDataCart);

        registerListeners();
    }

    private void loadComboBox() {
        cbbTypeCart.getItems().addAll(
                new CCBdata(0, "Tổng hợp"),
                new CCBdata(1, "Phiếu nhập"),
                new CCBdata(2, "Phiếu xuất"),
                new CCBdata(3, "Phiếu điều chuyển"));
        cbbTypeCart.getSelectionModel().selectFirst();
    }

    private void registerListeners() {
        // Gom các bộ lắng nghe DatePicker và ComboBox lại cho gọn sạch
        ChangeListener<Object> triggerDataReload = (obs, oldVal, newVal) -> {
            if (newVal != oldVal)
                loadDataCart();
        };

        cbbTypeCart.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != oldVal)
                loadDataCart();
        });

        dpFromDate.valueProperty().addListener(triggerDataReload);
        dpToDate.valueProperty().addListener(triggerDataReload);
        dpFromDateReport.valueProperty().addListener(triggerDataReload);
        dpToDateReport.valueProperty().addListener(triggerDataReload);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                System.out.println("Đang chọn tab: " + newTab.getText());
            }
        });
    }

    // ================= DATA PROCESSING =================
    private String convertSQLquery() {
        String fromdate = dpFromDate.getValue().toString();
        String todate = dpToDate.getValue().toString();
        String fromdateReport = dpFromDateReport.getValue() != null ? dpFromDateReport.getValue().toString() : null;
        String todateReport = dpToDateReport.getValue() != null ? dpToDateReport.getValue().toString() : null;

        int typeCartID = cbbTypeCart.getValue().getId();
        Account account = AppState.getInstance().get("Account", Account.class);

        // Trong DialogCartWareHouse.java, hàm convertSQLquery()
        String rawColumns = configDialog.getJoinedColumnIds("tabCart.txt");
        String column = "";

        if (rawColumns.trim().isBlank() || rawColumns.equals("*")) {
            column = "*";
        } else {
            // Tách các cột theo dấu phẩy, bao bọc mỗi cột bằng [] rồi nối lại
            column = Arrays.stream(rawColumns.split(","))
                    .map(col -> "[" + col.trim() + "]")
                    .collect(Collectors.joining(", "));
        }
        System.out.println("==========================");
        System.out.println(column);

        System.out.println("==========================");

        StringBuilder sql = new StringBuilder("SELECT " + column + " FROM vwCart WHERE ");

        // 1. Append điều kiện TypeCart
        if (typeCartID >= 1 && typeCartID <= 3) {
            sql.append("TypeCartID = ").append(typeCartID).append(" AND ");
        }

        // 2. Append điều kiện Date mặc định
        sql.append("dbo.fnFromDateToDate(DeliveryTime, '").append(fromdate).append("', '").append(todate)
                .append("') = 1");

        // 3. Append phân quyền theo Role (Tối ưu ghép chuỗi rẽ nhánh)
        String role = account.getRole();
        switch (role) {
            case "ADMIN":
            case "ACCOUNTANT":
                // Không cần thêm điều kiện đặc thù
                break;
            case "WAREHOUSE":
                sql.append(
                        " AND StatusID = 0 AND (DeliveryID IN (41,42,43,44,45,236) OR SourceID IN (41,42,43,44,45,236))");
                break;
            case "BACKOFFICE":
                List<Integer> groupTeam = dbInfoHelper.getGroupTeam(account.getTeamGroup());
                String result = groupTeam.stream().map(Object::toString).collect(Collectors.joining(","));
                sql.append(" AND (AccountID = ").append(account.getAccountID())
                        .append(" OR EmployeeID IN(").append(result).append("))");
                break;
            default: // Các role còn lại
                sql.append(" AND (AccountID = ").append(account.getAccountID())
                        .append(" OR EmployeeID = ").append(account.getEmployeeID()).append(")");
                break;
        }

        // 4. Append điều kiện Report Date nếu có
        if (fromdateReport != null && todateReport != null) {
            sql.append(" AND dbo.fnFromDateToDate(ReportDate, '").append(fromdateReport).append("', '")
                    .append(todateReport).append("') = 1");
        }

        sql.append(" ORDER BY LastTime DESC, CartAID DESC");
        return sql.toString();
    }

    private void loadDataCart() {
        // Giải phóng listener cũ trước khi xóa map dữ liệu cũ để tránh leak RAM
        clearActivePropertiesAndListeners();
        checkMap.clear();

        Account account = AppState.getInstance().get("Account", Account.class);
        String sql = convertSQLquery();

        if ("WAREHOUSE".equals(account.getRole()) || "ADMIN".equals(account.getRole())) {
            allDataCart = dbTableHelper.loadDataTable(tabCart, sql);
            allDataRequest = dbTableHelper.loadDataTable(tabRequest,
                    "SELECT * FROM vwRequestCart ORDER BY LastTimeOfRequest DESC, CartAID DESC");
        } else {
            allDataCart = dbTableHelper.loadDataTable(tabCart, sql);
            allDataRequest = dbTableHelper.loadDataTable(tabRequest, "SELECT * FROM vwRequestCart WHERE AccountID = "
                    + account.getAccountID() + " OR EmployeeID =" + account.getEmployeeID()
                    + " ORDER BY LastTimeOfRequest DESC, CartAID DESC");
        }

        formatAllTableData(allDataCart);
        formatAllTableData(allDataRequest);

        setTableData(tabCart, allDataCart);
        setTableData(tabRequest, allDataRequest);

        applySearchFilter();
    }

    private void applySearchFilter() {
        String keyword = txtSearch.getText();
        tableManagers.values().forEach(manager -> {
            if (manager.getFilteredData() != null) {
                manager.getFilteredData().setPredicate(row -> filterManager.matchRow(row, keyword));
            }
        });
    }

    // ================= ACTIONS =================
    @FXML
    private void onReload() {
        checkMap.values().forEach(rowMap -> rowMap.values().forEach(p -> p.set(false)));
        tableManagers.values().forEach(TableViewManager::clearAllFilters);
        txtSearch.clear();
        loadDataCart();
    }

    @FXML
    private void onResetFilter() {
        tableManagers.values().forEach(TableViewManager::clearAllFilters);
        setCurrentMonth(dpFromDate, dpToDate);
        setCurrentNull(dpFromDateReport, dpToDateReport);
        txtSearch.clear();
        applySearchFilter();
    }

    @FXML
    private void onCreate() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/dialogCreateCart.fxml"));
            Parent root = loader.load();
            DialogCreateCartImport controller = loader.getController();
            controller.setInitialData(this::loadDataCart, "CREATE", null, null, null);

            Stage dialog = new Stage();
            dialog.setTitle("Thêm đơn hàng");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onExportExcelReport() {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        if (selectedTab == null) {
            customDialogNotification.showDialog("Lỗi", "Không có tab nào được chọn", Alert.AlertType.ERROR);
            return;
        }

        TableView<ObservableList<String>> selectedTable = getSelectedTableView();
        if (selectedTable == null || selectedTable.getItems().isEmpty()) {
            customDialogNotification.showDialog("Thông báo", "Tab không có dữ liệu để xuất", Alert.AlertType.WARNING);
            return;
        }

        ObservableList<ObservableList<String>> checkedRows = getCheckedRows(selectedTable);
        if (checkedRows.isEmpty()) {
            customDialogNotification.showDialog("Thông báo", "Vui lòng chọn ít nhất 1 dòng để export",
                    Alert.AlertType.WARNING);
            return;
        }

        TableView<ObservableList<String>> tempTable = new TableView<>();
        tempTable.getColumns().addAll(selectedTable.getColumns().filtered(c -> !"SELECT_COL".equals(c.getId())));
        tempTable.setItems(checkedRows);

        if (functionExportExcel.exportExcelByTemplate(tempTable, stage)) {
            Map<String, BooleanProperty> rowMap = checkMap.get(selectedTable);
            if (rowMap != null) {
                rowMap.values().forEach(p -> p.set(false));
            }
            customDialogNotification.showDialog("Thành công", "Xuất Excel thành công", Alert.AlertType.INFORMATION);
        } else {
            customDialogNotification.showDialog("Lỗi", "Xuất Excel thất bại", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onExportExcel() {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        if (functionHelper.exportExcel(tabCart, stage, "Đơn hàng")) {
            customDialogNotification.showDialog("Thành công", "Xuất Excel thành công", Alert.AlertType.INFORMATION);
        } else {
            customDialogNotification.showDialog("Lỗi", "Xuất Excel thất bại", Alert.AlertType.ERROR);
        }
    }

    private TableView<ObservableList<String>> getSelectedTableView() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null)
            return null;

        return switch (selectedTab.getText()) {
            case "Đơn hàng" -> tabCart;
            case "Danh sách yêu cầu" -> tabRequest;
            default -> null;
        };
    }

    @FXML
    private void onExportPDF() {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        List<ExcelColumn> cols = List.of(
                new ExcelColumn("STT", -1), new ExcelColumn("Tên sản phẩm", 7),
                new ExcelColumn("Danh điểm", 6), new ExcelColumn("Hãng Sản Xuất", 9),
                new ExcelColumn("Xuất xứ", 11), new ExcelColumn("ĐVT", 13),
                new ExcelColumn("SL", 16), new ExcelColumn("Số HĐ", 100),
                new ExcelColumn("Ghi chú", 33), new ExcelColumn("Mã sản phẩm", 5));

        if (functionHelper.exportPdfFromTemplate(tabCart, cols, stage)) {
            customDialogNotification.showDialog("Thành công", "Xuất PDF thành công", Alert.AlertType.INFORMATION);
        } else {
            customDialogNotification.showDialog("Lỗi", "Xuất PDF thất bại", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onClose() {
        btnCancel.getScene().getWindow().hide();
        cleanup();
        System.gc();
    }

    @FXML
    private void onImprotExcel() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogImportExcelCart.fxml"));
            Parent root = loader.load();
            DialogImportExcelCart controller = loader.getController();
            controller.initData(this::loadDataCart);

            Stage dialog = new Stage();
            dialog.setTitle("Nhập excel");
            dialog.setScene(new Scene(root));
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onImportTableView() {
        TabPane localTabPane = new TabPane();
        String[] titles = { "Nhập hàng", "Xuất hàng", "Điều chuyển", "Cập nhật", "Xuất hóa đơn", "Xác nhận kho",
                "Xác nhận kế toán", "Xóa đơn hàng" };

        EditableTableViewCreateCart cartImport = new EditableTableViewCreateCart(1);
        EditableTableViewCreateCart cartExport = new EditableTableViewCreateCart(2);
        EditableTableViewCreateCart cartTransfer = new EditableTableViewCreateCart(3);

        Object[] controllers = {
                cartImport, cartExport, cartTransfer,
                new EditableTableViewUpdateCart(), new EditableTableViewExportBillCart(),
                new EditableTableViewConfirmCart(1), new EditableTableViewConfirmCart(2),
                new EditableTableViewDeleteCart()
        };

        for (int i = 0; i < titles.length; i++) {
            Tab tab = new Tab(titles[i]);
            tab.setClosable(false);
            BorderPane pane = new BorderPane();

            // Phản chiếu invoke method động thông qua cấu trúc class chung của bạn (nếu có
            // interface sẽ tốt hơn)
            try {
                pane.setTop((javafx.scene.Node) controllers[i].getClass().getMethod("createToolbar")
                        .invoke(controllers[i]));
                pane.setCenter(
                        (javafx.scene.Node) controllers[i].getClass().getMethod("getTable").invoke(controllers[i]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            tab.setContent(pane);
            localTabPane.getTabs().add(tab);
        }

        Stage dialog = new Stage();
        dialog.setScene(new Scene(localTabPane, 1000, 600));
        dialog.setTitle("Nhập liệu sản phẩm");
        dialog.setResizable(true);
        dialog.show();
    }

    private void setCurrentMonth(DatePicker fromDate, DatePicker toDate) {
        LocalDate now = LocalDate.now();
        fromDate.setValue(now.withDayOfMonth(1));
        toDate.setValue(now.withDayOfMonth(now.lengthOfMonth()));
    }

    private void setCurrentNull(DatePicker fromDate, DatePicker toDate) {
        fromDate.setValue(null);
        toDate.setValue(null);
    }

    @FXML
    private void onReconciliation() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxmlBusiness/dialogReconciliation.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.setTitle("Đối soát");
            dialog.setScene(new Scene(root));
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
            // Loại bỏ tất cả dấu phẩy (chuẩn US) hoặc dấu chấm (chuẩn VN) đóng vai trò phân
            // cách hàng nghìn
            // Chỉ giữ lại dấu phân cách thập phân chuẩn để parse sang Double.
            String cleanedValue = value.trim();

            // Nếu chuỗi chứa cả dấu phẩy và dấu chấm (Ví dụ: 541,000.00 hoặc 541.000,00)
            if (cleanedValue.contains(",") && cleanedValue.contains(".")) {
                if (cleanedValue.indexOf(",") < cleanedValue.indexOf(".")) {
                    cleanedValue = cleanedValue.replace(",", ""); // Chuẩn US: xóa phẩy
                } else {
                    cleanedValue = cleanedValue.replace(".", "").replace(",", "."); // Chuẩn VN: xóa chấm, đổi phẩy
                                                                                    // thành chấm
                }
            } else {
                // Nếu chỉ có 1 loại dấu phân cách, kiểm tra xem nó có phải phân cách hàng nghìn
                // không
                // Thường dữ liệu DB số nguyên như 541000 sẽ không dính, nhưng nếu view trả về
                // "541.000" thì cần xóa dấu chấm
                if (cleanedValue.matches(".*\\.\\d{3}$")) {
                    cleanedValue = cleanedValue.replace(".", "");
                } else if (cleanedValue.matches(".*,\\d{3}$")) {
                    cleanedValue = cleanedValue.replace(",", "");
                }
            }

            double number = Double.parseDouble(cleanedValue);
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

            if (number == Math.floor(number)) {
                return new java.text.DecimalFormat("#,###", symbols).format(number);
            }
            return new java.text.DecimalFormat("#,###.##", symbols).format(number);
        } catch (Exception e) {
            return value; // Nếu lỗi thì trả về chuỗi gốc ban đầu không format
        }
    }

    private void formatAllTableData(ObservableList<ObservableList<String>> data) {
        for (ObservableList<String> row : data) {
            for (int i : NUMERIC_COLUMNS) {
                if (i < row.size()) {
                    row.set(i, formatNumberIfPossible(row.get(i)));
                }
            }
        }
    }

    private void setTableData(TableView<ObservableList<String>> table, ObservableList<ObservableList<String>> data) {
        if (data == null)
            return;

        // 🔥 BƯỚC 1: TỰ ĐỘNG GÁN ID THEO TIÊU ĐỀ CHO TẤT CẢ CÁC CỘT MỚI SINH RA TỪ
        // DATABASE
        // Việc này giúp hàm restoreColumnOrder có cơ sở đối chiếu ID để xếp lại vị trí
        // cột khi tải lại dữ liệu.
        int colSize = table.getColumns().size();
        for (int i = 0; i < colSize; i++) {
            TableColumn<ObservableList<String>, ?> col = table.getColumns().get(i);

            // Nếu cột đã có ID định danh "SELECT_COL" (cột checkbox) thì giữ nguyên
            if ("SELECT_COL".equals(col.getId())) {
                continue;
            }

            // Lấy tên tiêu đề hiển thị trên bảng làm ID định danh duy nhất (Ví dụ:
            // "CartAID", "DeliveryTime",...)
            if (col.getText() != null && !col.getText().isBlank()) {
                col.setId(col.getText().trim());
            } else {
                col.setId("DynamicCol_" + i); // Phòng trường hợp cột không có tiêu đề chữ
            }
        }

        // 🔥 BƯỚC 2: Thêm cột CheckBox chọn dòng vào vị trí đầu tiên
        addCheckBoxColumn(table);

        // 🔥 BƯỚC 3: Quản lý vòng đời nạp/tải lại dữ liệu của bảng thông qua
        // TableViewManager
        TableViewManager manager = tableManagers.get(table);
        if (manager == null) {
            manager = new TableViewManager();
            manager.setupTableView(table, data);
            tableManagers.put(table, manager);
        } else {
            // Nếu manager đã tồn tại, gọi reloadData để đổ dữ liệu mới VÀ ép giao diện sắp
            // xếp lại theo vị trí cũ
            manager.reloadData(data);
        }
    }

    private void addCheckBoxColumn(TableView<ObservableList<String>> table) {
        if (table.getColumns().stream().anyMatch(c -> "SELECT_COL".equals(c.getId())))
            return;

        TableColumn<ObservableList<String>, Boolean> colSelect = new TableColumn<>("✔");
        CheckBox selectAll = new CheckBox();
        colSelect.setGraphic(selectAll);
        colSelect.setId("SELECT_COL");
        colSelect.setPrefWidth(40);
        colSelect.setEditable(true);

        checkMap.putIfAbsent(table, new HashMap<>());
        Map<String, BooleanProperty> rowMap = checkMap.get(table);

        selectAll.selectedProperty().addListener((obs, oldVal, isSelected) -> {
            rowMap.values().forEach(prop -> prop.set(isSelected));
            table.refresh();
        });

        colSelect.setCellValueFactory(param -> {
            String rowId = getRowId(param.getValue());
            BooleanProperty prop = rowMap.computeIfAbsent(rowId, k -> new SimpleBooleanProperty(false));

            // 🔥 GIẢI PHÁP TRÁNH LEAK RAM: Nếu prop đã có listener cũ, xóa nó đi trước khi
            // gán mới
            if (activeListeners.containsKey(prop)) {
                prop.removeListener(activeListeners.get(prop));
            }

            ChangeListener<Boolean> listener = (obs, oldVal, newVal) -> {
                boolean allChecked = rowMap.values().stream().allMatch(BooleanProperty::get);
                selectAll.setSelected(allChecked);
            };

            prop.addListener(listener);
            activeListeners.put(prop, listener); // Lưu vết để dọn dẹp sau này

            return prop;
        });

        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        table.getColumns().add(colSelect);
        table.setEditable(true);
    }

    private ObservableList<ObservableList<String>> getCheckedRows(TableView<ObservableList<String>> table) {
        ObservableList<ObservableList<String>> result = FXCollections.observableArrayList();
        Map<String, BooleanProperty> rowMap = checkMap.get(table);
        if (rowMap == null)
            return result;

        for (ObservableList<String> row : table.getItems()) {
            BooleanProperty checked = rowMap.get(getRowId(row));
            if (checked != null && checked.get()) {
                result.add(row);
            }
        }
        return result;
    }

    private String getRowId(ObservableList<String> row) {
        return (row != null && !row.isEmpty()) ? row.get(0) : "";
    }

    private void clearActivePropertiesAndListeners() {
        // Hủy toàn bộ liên kết Listener tránh rò rỉ RAM
        activeListeners.forEach(BooleanProperty::removeListener);
        activeListeners.clear();
    }

    public void cleanup() {
        clearActivePropertiesAndListeners();

        tableManagers.values().forEach(m -> {
            m.clearAllFilters();
            m.dispose();
        });
        tableManagers.clear();

        txtSearch.clear();

        if (tabCart != null)
            tabCart.setItems(null);
        if (tabRequest != null)
            tabRequest.setItems(null);

        if (allDataCart != null)
            allDataCart.clear();
        if (allDataRequest != null)
            allDataRequest.clear();

        checkMap.clear();
    }

    @FXML
    private void onSort() {
        List<ColumnConfig> columnsFromDB = dbInfoHelper.getAllConfigColumnCart();

        configDialog.showDialog("tabCart.txt", columnsFromDB);
    }
}