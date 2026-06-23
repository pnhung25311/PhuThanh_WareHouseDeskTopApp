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
import javafx.beans.value.WeakChangeListener;
import javafx.beans.value.ChangeListener;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
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
    private Button btnReload, btnSearchMass, btnAppendix, btnExportExcel, btnImportExcel;
    @FXML
    private Button btnDetailsProduct, btnGuarantee, btnCart;
    @FXML
    private Label cartBadge;
    @FXML
    private Tab tabV, tabProductIDMain;

    // ==================== DATA MODELS ====================
    private final ObservableList<ProductBusiness> masterData = FXCollections.observableArrayList();
    private FilteredList<ProductBusiness> filteredData;
    private final ObservableList<ObservableList<String>> masterDataSummary = FXCollections.observableArrayList();

    // ==================== CONFIGURATION & CACHE ====================
    private final Map<String, Field> productFieldMap = new LinkedHashMap<>();
    private final Map<String, Integer> summaryColumnIndexMap = new LinkedHashMap<>();
    private boolean isProductFieldMapInitialized = false;

    // ==================== SERVICES & HELPERS ====================
    private final StockComputeService stockService = new StockComputeService();
    private final TabViewHelper tabViewHelper = new TabViewHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final TabContextMenuBusiness contextMenu = new TabContextMenuBusiness();
    private final CustomDialogNotification dialogNotification = new CustomDialogNotification();
    private final TableViewManagerBusiness tableManagerBusiness = new TableViewManagerBusiness();
    private final DbTableHelper dbTableHelper = new DbTableHelper();
    private final TableViewManager tableManagerSummary = new TableViewManager();

    // Quản lý luồng tập trung nhằm tiết kiệm RAM tối đa thay vì tạo Thread vô tội
    // vạ
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true); // Tự động đóng luồng khi ứng dụng tắt
        return t;
    });

    // Giữ tham chiếu mạnh (Strong References) để tránh bị GC thu gom quá sớm khi
    // dùng Weak Listener
    // Giữ tham chiếu mạnh (Strong References) để tránh bị GC thu gom quá sớm khi
    // dùng Weak Listener
    private ChangeListener<String> textSearchListener;
    private ChangeListener<String> cbSearchColumnListener; // Đổi từ cbSearchListener thành cbSearchColumnListener

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
        tableManagerSummary.setupTableView(tabProductIDMainTable, masterDataSummary);
    }

    private void setupProductTable() {
        filteredData = new FilteredList<>(masterData, p -> true);
        tableView.setItems(filteredData);
        tableManagerBusiness.setupTableView(tableView, filteredData);

        contextMenu.attachDefaultContextMenu(
                tableView,
                tabViewHelper::getSelectedAID,
                tabViewHelper::getSelectedProductBusiness,
                this::loadData);

        tabViewHelper.clickItemSaveAIDBusiness(tableView);
        tabViewHelper.clickItemSaveAID(tabProductIDMainTable);
    }

    private void setupSearchFeature() {
        cbSearchColumn.getItems().add("Tất cả");
        cbSearchColumn.getSelectionModel().selectFirst();

        // Sử dụng WeakChangeListener phòng chống rò rỉ bộ nhớ (Memory Leak)
        textSearchListener = (obs, old, newVal) -> applyFilter();
        cbSearchColumnListener = (obs, old, newVal) -> applyFilter();

        txtSearch.textProperty().addListener(new WeakChangeListener<>(textSearchListener));
        cbSearchColumn.valueProperty().addListener(new WeakChangeListener<>(cbSearchColumnListener));
    }

    private void setupTabChangeListener() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            // Khi rời khỏi Tab cũ, giải phóng nội dung của nó luôn
            if (oldTab != null) {
                oldTab.setContent(null); // Xóa sạch giao diện bên trong Tab cũ
            }

            // Khi vào Tab mới, nạp lại giao diện mới từ đầu
            if (newTab == tabV) {
                // Nạp lại giao diện hoặc clear data cũ đi
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

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex != null)
                ex.printStackTrace();
        });

        executorService.submit(task);
        stockService.clearCache();
    }

    private void loadSummaryData() {
        List<ObservableList<String>> loadedData = dbTableHelper.loadDataTable(
                tabProductIDMainTable,
                "SELECT * FROM vwProductIDMainBusiness ORDER BY ProductIDMain");
        masterDataSummary.setAll(loadedData);
        applyFilter();
    }

    // ==================== SEARCH COMBO BOX ====================
    private void refreshProductSearchComboBox() {
        cbSearchColumn.getItems().clear();
        cbSearchColumn.getItems().add("Tất cả");

        // Tối ưu hóa: Chỉ phân tích Reflection 1 lần duy nhất thay vì chạy lại mỗi lần
        // đổi Tab
        if (!isProductFieldMapInitialized) {
            productFieldMap.clear();
            for (Field field : ProductBusiness.class.getDeclaredFields()) {
                var annotation = field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class);
                if (annotation != null) {
                    field.setAccessible(true);
                    String header = annotation.value();
                    productFieldMap.put(header, field);
                }
            }
            isProductFieldMapInitialized = true;
        }

        cbSearchColumn.getItems().addAll(productFieldMap.keySet());
        cbSearchColumn.getSelectionModel().selectFirst();
    }

    private void refreshSummarySearchComboBox() {
        cbSearchColumn.getItems().clear();
        summaryColumnIndexMap.clear();
        cbSearchColumn.getItems().add("Tất cả");

        int index = 0;
        for (TableColumn<?, ?> column : tabProductIDMainTable.getColumns()) {
            String header = column.getText();
            if (header != null && !header.isEmpty()) {
                cbSearchColumn.getItems().add(header);
                summaryColumnIndexMap.put(header, index);
            }
            index++;
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
        tableManagerBusiness.setExternalPredicate(createProductPredicate(keyword, selectedHeader));
    }

    private Predicate<ProductBusiness> createProductPredicate(String keyword, String selectedHeader) {
        // Tối ưu hóa hiệu năng: Tiền xử lý chuỗi regex chuẩn hóa 1 lần duy nhất ở ngoài
        // vòng lặp
        final boolean isSearchAll = "Tất cả".equals(selectedHeader);
        boolean isQuantityCol = "Số lượng".equals(selectedHeader);
        final String sanitizedKeyword = isQuantityCol ? keyword.toLowerCase()
                : keyword.replace("-", "").replace(".", "");

        return product -> {
            if (keyword.isEmpty())
                return true;

            try {
                if (isSearchAll) {
                    return productFieldMap.values().stream()
                            .map(field -> getFieldValueSafely(product, field))
                            .anyMatch(val -> val.replace("-", "").replace(".", "").contains(sanitizedKeyword));
                }
                if (isQuantityCol) {
                    return productFieldMap.values().stream()
                            .map(field -> getFieldValueSafely(product, field))
                            .anyMatch(val -> val.contains(sanitizedKeyword));
                }

                Field field = productFieldMap.get(selectedHeader);
                if (field == null)
                    return true;

                return getFieldValueSafely(product, field).contains(keyword);
            } catch (Exception e) {
                return false;
            }
        };
    }

    private void filterSummaryTable(String keyword) {
        if (keyword.isEmpty()) {
            tableManagerSummary.clearAllFilters();
            return;
        }

        String selectedHeader = cbSearchColumn.getValue();
        if ("Tất cả".equals(selectedHeader)) {
            System.out.println("Searching all columns: " + keyword);
        } else {
            Integer columnIndex = summaryColumnIndexMap.get(selectedHeader);
            if (columnIndex != null) {
                System.out.println("Searching column " + selectedHeader + " (" + columnIndex + "): " + keyword);
            }
        }
    }

    private String getFieldValueSafely(Object obj, Field field) {
        try {
            Object value = field.get(obj);
            return value == null ? "" : value.toString().toLowerCase();
        } catch (IllegalAccessException e) {
            return "";
        }
    }

    // ==================== UI ACTIONS ====================
    @FXML
    private void onReload() {
        loadData();
        tableManagerBusiness.clearAllFilters();
        tableManagerSummary.clearAllFilters();
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
            boolean success = functionHelper.exportExcel(tableView, stage, "sheet1");

            String title = success ? "Thành công" : "Lỗi";
            String message = success ? "Xuất Excel thành công" : "Xuất Excel thất bại";
            Alert.AlertType type = success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;

            dialogNotification.showDialog(title, message, type);
        } catch (Exception e) {
            dialogNotification.showDialog("Lỗi", "Xuất Excel thất bại: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onImportExcel() {
        System.out.println("Import Excel...");
    }

    @FXML
    private void onOpenDetailsProduct() {
        openModalDialog("fxml/dialogDetailsProduct.fxml", "Chi tiết sản phẩm", loader -> {
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

    private void openDialog(String fxmlPath, String title, boolean resizable, Consumer<FXMLLoader> initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlPath));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle(title);
            dialog.setScene(new Scene(root));
            dialog.setResizable(resizable);

            if (initializer != null) {
                initializer.accept(loader);
            }

            // BẮT SỰ KIỆN KHI ĐÓNG CỬA SỔ
            dialog.setOnCloseRequest(event -> {
                // 1. Xóa scene gốc để cắt đứt liên kết với cửa sổ (Stage)
                dialog.setScene(null);

                // 2. Giải phóng các nút, bảng, textfield bên trong giao diện vừa đóng
                if (root instanceof javafx.scene.layout.Pane) {
                    ((javafx.scene.layout.Pane) root).getChildren().clear();
                }
            });

            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openModalDialog(String fxmlPath, String title, Consumer<FXMLLoader> initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlPath));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle(title);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);

            if (initializer != null) {
                initializer.accept(loader);
            }

            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== PUBLIC METHODS ====================
    public void updateCartBadge(int count) {
        cartBadge.setText(String.valueOf(count));
        cartBadge.setVisible(count > 0);
    }

    @FXML
    private void OnSearchMass() {
        TabPane searchTabPane = new TabPane();
        Tab tabProductID = new Tab("Tìm theo Mã sản phẩm");
        Tab tabPartNo = new Tab("Tìm theo danh điểm");

        tabProductID.setClosable(false);
        tabPartNo.setClosable(false);

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

        searchTabPane.getTabs().addAll(tabProductID, tabPartNo);
        Stage dialog = new Stage();

        dialog.setScene(new Scene(searchTabPane, 1000, 600));
        dialog.setTitle("Nhập liệu sản phẩm");
        dialog.setResizable(true);
        dialog.show();
    }
}