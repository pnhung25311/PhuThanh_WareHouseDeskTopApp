package com.phuthanh.business.controller;

import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

import com.phuthanh.business.EditableTableView.SearchMassTableView;
import com.phuthanh.business.contextmenu.TabContextMenuBusiness;
import com.phuthanh.business.screen.dialog.DialogHistoryBusiness;
import com.phuthanh.business.service.BusinessService;
import com.phuthanh.business.service.StockComputeService;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.helper.TabViewHelper;
import com.phuthanh.manager.TableViewManager;
import com.phuthanh.manager.TableViewManagerBusiness;
import com.phuthanh.model.business.ProductBusiness;

public class HomeBusinessController {

    // ==================== FXML COMPONENTS ====================
    @FXML
    private TableView<ProductBusiness> tableView;
    @FXML
    private TableView<ObservableList<String>> tabProductIDMainTable;
    @FXML
    private TabPane tabPane;
    @FXML
    private ComboBox<String> cbSearchColumn;
    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnReload, btnSearchMass;
    @FXML
    private Button btnAppendix;
    @FXML
    private Button btnExportExcel;
    @FXML
    private Button btnImportExcel;
    @FXML
    private Button btnDetailsProduct;
    @FXML
    private Button btnGuarantee;
    @FXML
    private Button btnCart;

    @FXML
    private Label cartBadge;
    @FXML
    private Tab tabV;
    @FXML
    private Tab tabProductIDMain;

    // ==================== DATA MODELS ====================
    private final ObservableList<ProductBusiness> masterData = FXCollections.observableArrayList();
    private FilteredList<ProductBusiness> filteredData;

    private final ObservableList<ObservableList<String>> masterDataSummary = FXCollections.observableArrayList();

    // ==================== SEARCH CONFIGURATION ====================
    private final Map<String, Field> productFieldMap = new HashMap<>();
    private final Map<String, Integer> summaryColumnIndexMap = new HashMap<>();

    // ==================== SERVICES & HELPERS ====================
    private final StockComputeService stockService = new StockComputeService();

    private final TabViewHelper TAB_VIEW_HELPER = new TabViewHelper();
    private final FunctionHelper FUNCTION_HELPER = new FunctionHelper();
    private final TabContextMenuBusiness CONTEXT_MENU = new TabContextMenuBusiness();
    private final CustomDialogNotification DIALOG_NOTIFICATION = new CustomDialogNotification();
    private final TableViewManagerBusiness TABLE_MANAGER_BUSINESS = new TableViewManagerBusiness();
    private final DbTableHelper DB_TABLE_HELPER = new DbTableHelper();
    // private final NumberFormatter NUMBER_FORMATTER = new
    // NumberFormatter();
    private final TableViewManager TABLE_MANAGER_SUMMARY = new TableViewManager();

    // ==================== INITIALIZATION ====================
    @FXML
    public void initialize() {
        setupSummaryTable();
        setupProductTable();
        setupSearchFeature();
        setupTabChangeListener();
        loadData();
    }

    private void setupSummaryTable() {
        TABLE_MANAGER_SUMMARY.setupTableView(tabProductIDMainTable, masterDataSummary);
    }

    private void setupProductTable() {
        filteredData = new FilteredList<>(masterData, p -> true);
        tableView.setItems(filteredData);
        TABLE_MANAGER_BUSINESS.setupTableView(tableView, filteredData);

        CONTEXT_MENU.attachDefaultContextMenu(
                tableView,
                TAB_VIEW_HELPER::getSelectedAID,
                TAB_VIEW_HELPER::getSelectedProductBusiness,
                this::loadData);

        TAB_VIEW_HELPER.clickItemSaveAIDBusiness(tableView);
        TAB_VIEW_HELPER.clickItemSaveAID(tabProductIDMainTable);
    }

    private void setupSearchFeature() {
        cbSearchColumn.getItems().add("Tất cả");
        cbSearchColumn.getSelectionModel().selectFirst();

        txtSearch.textProperty().addListener((obs, old, newVal) -> applyFilter());
        cbSearchColumn.valueProperty().addListener((obs, old, newVal) -> applyFilter());
    }

    private void setupTabChangeListener() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabV) {
                refreshProductSearchComboBox();
            } else if (newTab == tabProductIDMain) {
                refreshSummarySearchComboBox();
            }
            applyFilter();
        });
    }

    // ==================== DATA LOADING ====================
    private void loadData() {
        loadProductData();
        loadSummaryData();
    }

    private void loadProductData() {
        Task<List<ProductBusiness>> task = new Task<>() {
            @Override
            protected List<ProductBusiness> call() throws Exception {
                return new BusinessService().getAllProducts();
            }
        };

        task.setOnSucceeded(e -> {
            masterData.setAll(task.getValue());
            refreshProductSearchComboBox();
            tableView.refresh();
        });

        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
        stockService.clearCache();
    }

    private void loadSummaryData() {
        List<ObservableList<String>> loadedData = DB_TABLE_HELPER.loadDataTable(
                tabProductIDMainTable,
                "SELECT * FROM vwProductIDMainBusiness ORDER BY ProductIDMain");
        masterDataSummary.setAll(loadedData);
        applyFilter();
    }

    // ==================== SEARCH COMBO BOX ====================
    private void refreshProductSearchComboBox() {
        cbSearchColumn.getItems().clear();
        productFieldMap.clear();

        cbSearchColumn.getItems().add("Tất cả");

        for (Field field : ProductBusiness.class.getDeclaredFields()) {
            var annotation = field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class);
            if (annotation != null) {
                field.setAccessible(true);
                String header = annotation.value();
                cbSearchColumn.getItems().add(header);
                productFieldMap.put(header, field);
            }
        }

        cbSearchColumn.getSelectionModel().selectFirst();
    }

    private void refreshSummarySearchComboBox() {
        cbSearchColumn.getItems().clear();
        summaryColumnIndexMap.clear();

        cbSearchColumn.getItems().add("Tất cả");

        int index = 0;
        for (TableColumn<?, ?> column : tabProductIDMainTable.getColumns()) {
            String header = column.getText();
            cbSearchColumn.getItems().add(header);
            summaryColumnIndexMap.put(header, index++);
        }

        cbSearchColumn.getSelectionModel().selectFirst();
    }

    // ==================== FILTER LOGIC ====================
    private void applyFilter() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();

        if (currentTab == tabV) {
            filterProductTable(keyword);
        } else if (currentTab == tabProductIDMain) {
            filterSummaryTable(keyword);
        }
    }

    private void filterProductTable(String keyword) {
        if (keyword.isEmpty()) {
            filteredData.setPredicate(null);
            return;
        }

        String selectedHeader = cbSearchColumn.getValue();
        // filteredData.setPredicate(createProductPredicate(keyword, selectedHeader));
        TABLE_MANAGER_BUSINESS.setExternalPredicate(
                createProductPredicate(keyword, selectedHeader));
    }

    private Predicate<ProductBusiness> createProductPredicate(String keyword, String selectedHeader) {
        return product -> {
            if (keyword.isEmpty())
                return true;

            try {
                if ("Tất cả".equals(selectedHeader)) {
                    return productFieldMap.values().stream()
                            .map(field -> getFieldValueSafely(product, field))
                            .anyMatch(value -> value.contains(keyword));
                }

                Field field = productFieldMap.get(selectedHeader);
                if (field == null)
                    return true;

                return getFieldValueSafely(product, field).contains(keyword);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        };
    }

    private void filterSummaryTable(String keyword) {
        if (keyword.isEmpty()) {
            TABLE_MANAGER_SUMMARY.clearAllFilters();
            return;
        }

        String selectedHeader = cbSearchColumn.getValue();

        if ("Tất cả".equals(selectedHeader)) {
            // TODO: Implement search across all columns in TableViewManager
            System.out.println("Searching all columns: " + keyword);
        } else {
            Integer columnIndex = summaryColumnIndexMap.get(selectedHeader);
            if (columnIndex != null) {
                System.out.println("Searching column " + selectedHeader + " (" + columnIndex + "): " + keyword);
                // TODO: Add search by specific column in TableViewManager
            }
        }
    }

    // ✅ Fixed: Không throw Exception
    private String getFieldValueSafely(Object obj, Field field) {
        try {
            Object value = field.get(obj);
            return value == null ? "" : value.toString().toLowerCase();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return "";
        }
    }

    // ==================== UI ACTIONS ====================
    @FXML
    private void onReload() {
        loadData();
        TABLE_MANAGER_BUSINESS.clearAllFilters();
        TABLE_MANAGER_SUMMARY.clearAllFilters();
        txtSearch.clear();
    }

    @FXML
    private void onOpenAppendix() {
        openDialog("fxml/dialogAppendix.fxml", "Phụ lục", false);
    }

    @FXML
    private void onExportExcel() {
        try {
            Stage stage = (Stage) tabPane.getScene().getWindow();
            boolean success = FUNCTION_HELPER.exportExcel(tableView, stage, "sheet1");

            String title = success ? "Thành công" : "Lỗi";
            String message = success ? "Xuất Excel thành công" : "Xuất Excel thất bại";
            Alert.AlertType type = success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;

            DIALOG_NOTIFICATION.showDialog(title, message, type);
        } catch (Exception e) {
            e.printStackTrace();
            DIALOG_NOTIFICATION.showDialog("Lỗi", "Xuất Excel thất bại: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onImportExcel() {
        // TODO: Implement import Excel functionality
        System.out.println("Import Excel...");
    }

    @FXML
    private void onOpenDetailsProduct() {
        openModalDialog("fxml/dialogDetailsProduct.fxml", "Chi tiết sản phẩm", loader -> {
            // DialogDetailsProduct controller = loader.getController();
            // controller.initData("", false);
        });
    }

    @FXML
    private void onGuarantee() {
        openDialog("fxml/dialogGuaranteeWareHouse.fxml", "Bảo hành", true);
    }

    @FXML
    private void onCart() {
        openDialog("fxml/dialogCartWareHouse.fxml", "Đơn hàng", true);
    }

    @FXML
    private void onHistory() {
        openDialog("fxmlBusiness/dialogHistoryBusiness.fxml", "Lịch sử giao dịch", true, loader -> {
            DialogHistoryBusiness controller = loader.getController();
            controller.initData(
                    "SELECT * FROM vwct90 WHERE sl_nhap > 0 ORDER BY ngay_ct DESC",
                    "SELECT * FROM vwct70y WHERE sl_nhap > 0 ORDER BY ngay_ct DESC");
        });
    }

    @FXML
    private void onReconciliation() {
        openDialog("fxmlBusiness/dialogReconciliation.fxml", "Lịch sử giao dịch", true);
    }

    // ==================== HELPER METHODS ====================
    private void openDialog(String fxmlPath, String title, boolean resizable) {
        openDialog(fxmlPath, title, resizable, null);
    }

    private void openDialog(String fxmlPath, String title, boolean resizable, DialogConsumer consumer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlPath));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle(title);
            dialog.setScene(new Scene(root));
            dialog.setResizable(resizable);

            if (consumer != null) {
                consumer.accept(loader);
            }

            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openModalDialog(String fxmlPath, String title, ModalDialogConsumer consumer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlPath));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle(title);
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);

            if (consumer != null) {
                consumer.accept(loader);
            }

            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== FUNCTIONAL INTERFACES ====================
    @FunctionalInterface
    private interface DialogConsumer {
        void accept(FXMLLoader loader);
    }

    @FunctionalInterface
    private interface ModalDialogConsumer {
        void accept(FXMLLoader loader);
    }

    // ==================== PUBLIC METHODS ====================
    public void updateCartBadge(int count) {
        cartBadge.setText(String.valueOf(count));
        cartBadge.setVisible(count > 0);
    }

    @FXML
    private void OnSearchMass() {
        TabPane tabPane = new TabPane();
        Tab tabProductID = new Tab("Tìm theo Mã sản phẩm");
        Tab tabPartNo = new Tab("Tìm theo danh điểm");
        tabProductID.setClosable(false);
        tabPartNo.setClosable(false);
        System.out.println(tableView.getItems().get(0).maVatTu);
        Stage stage = (Stage) btnSearchMass.getScene().getWindow();
        ObservableList<ProductBusiness> items = tableView.getItems();

        SearchMassTableView searchMassProductIDTableView = new SearchMassTableView(1, stage, items);
        SearchMassTableView searchMassPartNoTableView = new SearchMassTableView(2, stage, items);

        BorderPane searchProductID = new BorderPane();
        searchProductID.setTop(searchMassProductIDTableView.createToolbar());
        searchProductID.setCenter(searchMassProductIDTableView.getTable());

        tabProductID.setContent(searchProductID);

        BorderPane searchPartNo = new BorderPane();
        searchPartNo.setTop(searchMassPartNoTableView.createToolbar());
        searchPartNo.setCenter(searchMassPartNoTableView.getTable());

        tabPartNo.setContent(searchPartNo);

        tabPane.getTabs().add(tabProductID);
        tabPane.getTabs().add(tabPartNo);
        Stage dialog = new Stage();

        dialog.setScene(new Scene(tabPane, 1000, 600));
        dialog.setTitle("Nhập liệu sản phẩm");
        dialog.setResizable(true);

        dialog.show();
    }

}