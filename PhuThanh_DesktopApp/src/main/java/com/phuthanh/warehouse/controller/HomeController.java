package com.phuthanh.warehouse.controller;

import com.jfoenix.controls.JFXHamburger;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.custom.TabContextMenuHandler;
import com.phuthanh.drawer.DrawerActionListener;
import com.phuthanh.drawer.DrawerController;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.helper.TabViewHelper;
import com.phuthanh.helper.dialog.ConfigDialog;
import com.phuthanh.manager.DrawerManager;
import com.phuthanh.manager.TableViewManager;
import com.phuthanh.model.helper.ColumnConfig;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.store.AppState;
import com.phuthanh.store.CartState;
import com.phuthanh.warehouse.EditableTableView.tableView.historyWarehouse.EditableTableViewCreateHistory;
import com.phuthanh.warehouse.EditableTableView.tableView.product.EditableTableViewCreateProduct;
import com.phuthanh.warehouse.EditableTableView.tableView.product.EditableTableViewDeleteProduct;
import com.phuthanh.warehouse.EditableTableView.tableView.product.EditableTableViewUpdateProduct;
import com.phuthanh.warehouse.screen.dialog.*;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.geometry.Pos;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HomeController implements DrawerActionListener {
    @FXML
    private TabPane jtable;
    @FXML
    private Tab tabInformation, tabDetails, tabRequest, tabProductIDMain;
    @FXML
    private TableView<ObservableList<String>> tabInformationTable, tabDetailTable, tabRequestTable,
            tabProductIDMainTable;
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnSearch, btnGuarantee, btnHistory, btnCart, btnImportExcel, btnCheckSheet, btnChart,
            btnDetailsProduct;
    @FXML
    private ComboBox<String> cbbSearch;
    @FXML
    private JFXHamburger hamburger;
    @FXML
    private AnchorPane drawer, overlay;
    @FXML
    private Label cartBadge;

    private final TabViewHelper tabViewHelper = new TabViewHelper();
    private final DrawerManager drawerManager = new DrawerManager();
    private final Map<TableView<?>, TableViewManager> tableManagers = new HashMap<>();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private final DbTableHelper dbTableHelper = new DbTableHelper();
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final TabContextMenuHandler tabContextMenuHandler = new TabContextMenuHandler();
    private final ConfigDialog configDialog = new ConfigDialog();
    private final PauseTransition searchTask = new PauseTransition(Duration.millis(500));

    private DrawerController drawerController;
    private DrawerItem selectedDrawerItem;
    private double drawerWidth;

    private FilteredList<ObservableList<String>> filteredInfoData;
    private FilteredList<ObservableList<String>> filteredRequestData;
    private FilteredList<ObservableList<String>> filteredProductIDMainData;

    private ObservableList<ObservableList<String>> allData;
    private ObservableList<ObservableList<String>> allDataProductIDMain;
    private ObservableList<ObservableList<String>> allDataRequest;

    private boolean userRole;
    private int change = 0;
    private ScheduledExecutorService scheduler;
    private boolean notificationShowing = false;
    private final ChangeListener<String> searchListener = (obs, oldText, newText) -> {
        searchTask.setOnFinished(event -> triggerRealtimeSearch(newText));
        searchTask.playFromStart();
    };
    private static final String SEARCH_ALL = "Tất cả";

    public void initialize() {
        selectedDrawerItem = new DrawerItem("1", "Danh mục sản phẩm", "vwProduct", 0, "", "Product", "",
                "vwRequestProduct", "RequestProduct", "", "", "", "", 0, "", "");
        loadProductTable();

        drawerWidth = drawer.getPrefWidth();
        drawer.setTranslateX(-drawerWidth);
        overlay.setVisible(false);
        overlay.setMouseTransparent(true);

        tabInformation.setText(selectedDrawerItem.getNameWareHouse());
        tabRequest.setText("Thùng rác ");
        tabDetails.setDisable(true);
        jtable.getTabs().remove(tabDetails);
        AppState.getInstance().set("selectedDrawerItem", selectedDrawerItem);
        btnHistory.setVisible(false);
        btnCheckSheet.setVisible(false);
        userRole = Boolean.TRUE.equals(AppState.getInstance().get("UserRole", Boolean.class));

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/drawer.fxml"));
            AnchorPane drawerPane = loader.load();
            drawerController = loader.getController();
            drawerController.setListener(this);
            drawer.getChildren().setAll(drawerPane);
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadSearchColumns(tabInformationTable);

        tabContextMenuHandler.attachDefaultContextMenu(tabDetailTable, () -> tabViewHelper.getSelectedAID(),
                this::loadProductTable);
        tabContextMenuHandler.attachDefaultContextMenu(tabInformationTable, () -> tabViewHelper.getSelectedAID(),
                this::loadProductTable);
        tabContextMenuHandler.attachRequestContextMenu(tabRequestTable, () -> tabViewHelper.getSelectedAID());
        tabContextMenuHandler.setOnReloadCallback(this::loadProductTable);

        hamburger.setOnMouseClicked(e -> drawerManager.toggleDrawer(drawer, overlay, drawerWidth));
        overlay.setOnMouseClicked(e -> drawerManager.hideDrawer(drawer, overlay, drawerWidth));

        txtSearch.textProperty().removeListener(searchListener);
        txtSearch.textProperty().addListener(searchListener);
        cbbSearch.setOnAction(e -> triggerRealtimeSearch(txtSearch.getText()));

        btnSearch.setVisible(false);
        btnSearch.setManaged(false);

        tabViewHelper.clickItemSaveAID(tabInformationTable);
        tabViewHelper.clickItemSaveAID(tabDetailTable);
        tabViewHelper.clickItemSaveAID(tabRequestTable);
        tabViewHelper.clickItemSaveAID(tabProductIDMainTable);

        jtable.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            txtSearch.clear();
            if (newTab == tabInformation) {
                loadSearchColumns(tabInformationTable);
                if (filteredInfoData != null)
                    tabInformationTable.setItems(filteredInfoData);
            } else if (newTab == tabRequest) {
                loadSearchColumns(tabRequestTable);
                if (filteredRequestData != null)
                    tabRequestTable.setItems(filteredRequestData);
            } else if (newTab == tabProductIDMain) {
                loadSearchColumns(tabProductIDMainTable);
                if (filteredProductIDMainData != null)
                    tabProductIDMainTable.setItems(filteredProductIDMainData);
            }
        });

        cartBadge.textProperty().bind(CartState.getInstance().cartCountProperty().asString());
        cartBadge.visibleProperty().bind(CartState.getInstance().cartCountProperty().greaterThan(0));
        startCartWatcher();
    }

    // 🌟 ĐÃ FIX: Hàm openDialog hỗ trợ Owner, truyền initializer, tự nhận biết
    // show() khi dùng Modality.NONE để tránh đứng luồng cha.
    // 🌟 HÀM OPENDIALOG HOÀN CHỈNH - ĐÃ FIX LỖI RÒ RỈ LISTENER VÀ BỘ NHỚ
    private <T> T openDialog(String fxmlPath, String title, boolean resizable, Modality modality, Window owner,
            Consumer<T> initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlPath));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.setTitle(title);

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.setResizable(resizable);

            // Khai báo các Listener ở dạng biến để có thể gỡ bỏ (remove) chính xác khi đóng
            // dialog
            ChangeListener<Boolean> showingListener = (obs, oldVal, newVal) -> {
                if (!newVal && dialog.isShowing()) {
                    dialog.close();
                }
            };

            ChangeListener<Boolean> iconifiedListener = (obs, oldVal, newVal) -> {
                if (newVal) {
                    dialog.hide();
                } else {
                    dialog.show();
                }
            };

            // 1. Xử lý Modality và liên kết vòng đời Owner
            if (modality == Modality.APPLICATION_MODAL) {
                if (owner != null)
                    dialog.initOwner(owner);
                dialog.initModality(modality);
            } else {
                dialog.initModality(Modality.NONE);

                // Nếu có cửa sổ cha và dùng Modality.NONE, thiết lập liên kết trạng thái
                // ẩn/hiện
                if (owner instanceof Stage) {
                    Stage parentStage = (Stage) owner;
                    parentStage.showingProperty().addListener(showingListener);
                    parentStage.iconifiedProperty().addListener(iconifiedListener);
                }
            }

            // Lấy controller từ FXML
            T controller = loader.getController();

            // Thực hiện chạy khởi tạo dữ liệu (nếu có truyền vào)
            if (initializer != null && controller != null) {
                initializer.accept(controller);
            }

            // 2. Xử lý sự kiện khi Dialog bị đóng (Ẩn đi)
            dialog.setOnHidden(e -> {
                // 🔥 KHẮC PHỤC LỖI NHẢY MÀN HÌNH: Gỡ bỏ ngay listener khỏi cửa sổ cha để giải
                // phóng tài nguyên
                if (modality != Modality.APPLICATION_MODAL && owner instanceof Stage) {
                    Stage parentStage = (Stage) owner;
                    parentStage.showingProperty().removeListener(showingListener);
                    parentStage.iconifiedProperty().removeListener(iconifiedListener);
                    System.out.println("♻️ Đã gỡ bỏ liên kết vòng đời với Stage cha thành công.");
                }

                // Giải phóng cấu trúc giao diện Node
                if (dialog.getScene() != null) {
                    dialog.getScene().setRoot(new AnchorPane());
                }
                dialog.setScene(null);

                // Nếu Controller có hàm hủy (implements AutoCloseable) thì kích hoạt đóng các
                // tiến trình ngầm bên trong
                if (controller instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) controller).close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                // Ép dọn rác bộ nhớ đệm
                System.gc();
                System.out.println("✅ Đã đóng Dialog và giải phóng bộ nhớ Controller.");
            });

            // 3. Hiển thị cửa sổ theo đúng chế độ khóa (Modal) hoặc không khóa
            if (modality == Modality.APPLICATION_MODAL) {
                dialog.showAndWait();
            } else {
                dialog.show();
            }

            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void triggerRealtimeSearch(String keyword) {
        if (keyword == null)
            return;

        String selectedHeader = cbbSearch.getSelectionModel().getSelectedItem();
        TableView<ObservableList<String>> currentTable = getCurrentTable();
        TableViewManager manager = tableManagers.get(currentTable);

        if (manager != null) {
            int colIndex = (selectedHeader == null || SEARCH_ALL.equals(selectedHeader)) ? -1
                    : getColumnIndexByHeader(selectedHeader, currentTable);
            boolean isQuantityCol = "Số lượng".equals(selectedHeader);

            String cleanKeyword = isQuantityCol ? keyword.trim().toLowerCase()
                    : keyword.trim().toLowerCase().replace("-", "").replace(".", "");

            // Gọi phương thức mới đã sửa ở TableViewManager
            manager.updateSearchPredicate(cleanKeyword, colIndex, isQuantityCol);

            currentTable.refresh();
        }
    }

    private boolean matchRow(ObservableList<String> row, String cleanKeyword, int colIndex, boolean isQuantityCol) {
        if (colIndex != -1) {
            return checkCell(row.get(colIndex), cleanKeyword, isQuantityCol);
        }
        // Nếu tìm trên toàn bộ dòng, duyệt danh sách
        for (String cell : row) {
            if (checkCell(cell, cleanKeyword, isQuantityCol))
                return true;
        }
        return false;
    }

    private boolean checkCell(String cell, String keyword, boolean isQuantityCol) {
        if (cell == null || cell.isEmpty())
            return false;
        String val = cell.toLowerCase();
        if (!isQuantityCol) {
            val = val.replace("-", "").replace(".", "");
        }
        return val.contains(keyword);
    }

    // Helper để lấy table đang active
    private TableView<ObservableList<String>> getCurrentTable() {
        Tab selected = jtable.getSelectionModel().getSelectedItem();
        if (selected == tabInformation)
            return tabInformationTable;
        if (selected == tabRequest)
            return tabRequestTable;
        if (selected == tabProductIDMain)
            return tabProductIDMainTable;
        return tabInformationTable;
    }

    @FXML
    private void onCreateClick() {
        // if (!userRole) {
        // customDialogNotification.showDialog("Thông tin", "Bạn không có quyền của
        // kho", Alert.AlertType.WARNING);
        // return;
        // }
        Account acc = AppState.getInstance().get("Account", Account.class);
        if (acc.getRole().trim().equals("WAREHOUSE")) {
        }
        if (acc.getRole().trim().equals("ACCOUNTANT")) {
            customDialogNotification.showDialog("Thông tin", "Bạn không có quyền tạo mã kho", Alert.AlertType.WARNING);
            return;
        }
        if (acc.getRole().trim().equals("ADMIN")) {
        }
        if (acc.getRole().trim().equals("BACKOFFICE")) {
        }
        if (acc.getRole().trim().equals("BUSINESS")
                || acc.getRole().trim().equals("IMPORT")) {
            // customDialogNotification.showDialog("Thông tin", "Bạn không có quyền tạo mã kho", Alert.AlertType.WARNING);
            // return;
        }

        Window parentWindow = jtable.getScene().getWindow(); // Lấy cửa sổ cha hiện tại

        if (selectedDrawerItem.getWareHouseCategory() > 0) {
            // Chuyển sang Modality.NONE nếu muốn lớp cha vẫn click và thao tác được khi
            // dialog này mở
            openDialog("fxml/dialogCreateHistory.fxml", "Thêm mới kho", false, Modality.NONE, parentWindow,
                    (DialogCreateHistoryController ctrl) -> {
                        ctrl.setProductAID(null, true, false, false);
                        ctrl.setOnCreateSuccess(this::loadProductTable);
                    });
        } else {
            openDialog("fxml/dialogCreateProduct.fxml", "Thêm mới sản phẩm", false, Modality.NONE, parentWindow,
                    (DialogCreateProductController ctrl) -> {
                        ctrl.setOnCreateSuccess(this::loadProductTable);
                    });
        }
    }

    @FXML
    private void onOpenDetailsProduct() {
        Window parentWindow = jtable.getScene().getWindow();
        openDialog("fxml/dialogDetailsProduct.fxml", "Chi tiết sản phẩm", false, Modality.NONE, parentWindow, null);
    }

    @FXML
    private void onOpenHistory() {
        DrawerItem currentItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
        Window parentWindow = jtable.getScene().getWindow();
        openDialog("fxml/dialogHistoryWareHouse.fxml", "Lịch sử nhập xuất", true, Modality.NONE, parentWindow,
                (HistoryController ctrl) -> {
                    ctrl.initData(currentItem, null, this::loadProductTable);
                });
    }

    @FXML
    private void ImportExcel() {
        Account acc = AppState.getInstance().get("Account", Account.class);
        if (acc.getRole().trim().equals("WAREHOUSE")) {
        }
        if (acc.getRole().trim().equals("ACCOUNTANT")) {
            customDialogNotification.showDialog("Thông tin", "Bạn không có quyền tạo mã kho", Alert.AlertType.WARNING);
            return;
        }
        if (acc.getRole().trim().equals("ADMIN")) {
        }
        if (acc.getRole().trim().equals("BACKOFFICE")) {
        }
        if (acc.getRole().trim().equals("BUSINESS")
                || acc.getRole().trim().equals("IMPORT")) {
            // customDialogNotification.showDialog("Thông tin", "Bạn không có quyền tạo mã kho", Alert.AlertType.WARNING);
            // return;
        }

        Window parentWindow = jtable.getScene().getWindow(); // Lấy cửa sổ cha hiện tại


        openDialog("fxml/dialogImportExcel.fxml", "Nhập excel", true, Modality.NONE, parentWindow,
                (DialogImportExcel ctrl) -> {
                    ctrl.initData(this::loadProductTable, selectedDrawerItem);
                });
    }

    @FXML
    private void ImportTableView() {
        if (!userRole) {
            customDialogNotification.showDialog("Thông tin", "Bạn không có quyền của kho", Alert.AlertType.WARNING);
            return;
        }
        if (selectedDrawerItem.getWareHouseCategory() > 0)
            openEditHistoryTable();
        else
            openEditProductTable();
    }

    private void loadProductTable() {
        clearTableMemory();
        String sql = convertSQLquery(selectedDrawerItem);

        if (selectedDrawerItem != null) {
            allData = dbTableHelper.loadDataTable(tabInformationTable, sql);
        } else {
            allData = dbTableHelper.loadDataTable(tabInformationTable, sql);
        }
        allDataRequest = dbTableHelper.loadDataTable(tabRequestTable, "SELECT * FROM "
                + selectedDrawerItem.getWareHouseRequest() + " ORDER BY LastTime DESC, ProductID DESC");
        allDataProductIDMain = dbTableHelper.loadDataTable(tabProductIDMainTable,
                "SELECT * FROM vwProductIDMain ORDER BY ProductIDMain");

        filteredInfoData = new FilteredList<>(allData);
        filteredRequestData = new FilteredList<>(allDataRequest);
        filteredProductIDMainData = new FilteredList<>(allDataProductIDMain);

        setTableData(tabInformationTable, filteredInfoData);
        setTableData(tabProductIDMainTable, filteredProductIDMainData);
        setTableData(tabRequestTable, filteredRequestData);

        // tabInformationTable.setItems(filteredInfoData);
        // tabRequestTable.setItems(filteredRequestData);
        // tabProductIDMainTable.setItems(filteredProductIDMainData);
        tabContextMenuHandler.attachDefaultContextMenu(tabDetailTable, () -> tabViewHelper.getSelectedAID(),
                this::loadProductTable);
        tabContextMenuHandler.attachDefaultContextMenu(tabInformationTable, () -> tabViewHelper.getSelectedAID(),
                this::loadProductTable);
        tabContextMenuHandler.attachRequestContextMenu(tabRequestTable, () -> tabViewHelper.getSelectedAID());

        loadSearchColumns(tabInformationTable);
    }

    private void clearTableMemory() {
        tabInformationTable.setItems(null);
        tabRequestTable.setItems(null);
        tabProductIDMainTable.setItems(null);

        if (allData != null) {
            allData.clear();
            allData = null;
        }
        if (allDataRequest != null) {
            allDataRequest.clear();
            allDataRequest = null;
        }
        if (allDataProductIDMain != null) {
            allDataProductIDMain.clear();
            allDataProductIDMain = null;
        }

        filteredInfoData = null;
        filteredRequestData = null;
        filteredProductIDMainData = null;
    }

    @Override
    public void onDrawerItemClicked(DrawerItem item) {
        drawerManager.hideDrawer(drawer, overlay, drawerWidth);
        selectedDrawerItem = new DrawerItem(item.getWareHouseID(), item.getNameWareHouse(), item.getWareHouseTable(),
                item.getWareHouseCategory(), item.getWareHouseHistory(), item.getWareHouseDataBase(),
                item.getWareHouseDataBaseHistory(), item.getWareHouseRequest(), item.getWareHouseRequestDataBase(),
                item.getWareHouseUpdateHistoryDataBase(), item.getWareHouseUpdateHistory(),
                item.getWareHouseSheetDataBase(), item.getWareHouseCheckDataBase(), item.getWareHouseSupplierID(),
                item.getWareHouseSheet(), item.getWareHouseDataCheck());

        tabInformation.setText(selectedDrawerItem.getNameWareHouse());
        btnHistory.setVisible(selectedDrawerItem.getWareHouseCategory() > 0);
        btnCheckSheet.setVisible(selectedDrawerItem.getWareHouseCategory() > 0);
        tabRequest.setText("Thùng rác ");
        AppState.getInstance().set("selectedDrawerItem", selectedDrawerItem);

        tableManagers.clear();
        loadProductTable();

        if (!tabInformationTable.getColumns().isEmpty() && !tabInformationTable.getItems().isEmpty()) {
            tabInformationTable.getSelectionModel().select(0, tabInformationTable.getColumns().get(0));
        }

        tabContextMenuHandler.attachDefaultContextMenu(tabDetailTable, () -> tabViewHelper.getSelectedAID(),
                this::loadProductTable);
        tabContextMenuHandler.attachDefaultContextMenu(tabInformationTable, () -> tabViewHelper.getSelectedAID(),
                this::loadProductTable);
        tabInformation.getTabPane().getSelectionModel().select(tabInformation);

        System.gc();
    }

    @FXML
    private void onSearch() {
    }

    @FXML
    private void onReload() {
        System.out.println("♻️ Đang kích hoạt dọn dẹp và nạp lại dữ liệu tối ưu RAM...");
        txtSearch.textProperty().removeListener(searchListener);
        if (tabInformationTable != null) {
            tabInformationTable.setItems(null);
            tabInformationTable.getColumns().clear();
        }
        if (tabProductIDMainTable != null) {
            tabProductIDMainTable.setItems(null);
            tabProductIDMainTable.getColumns().clear();
        }
        if (tabRequestTable != null) {
            tabRequestTable.setItems(null);
            tabRequestTable.getColumns().clear();
        }

        if (tableManagers != null) {
            tableManagers.values().forEach(manager -> {
                try {
                    manager.clearAllFilters();
                } catch (Exception e) {
                }
            });
        }
        clearTableMemory();
        loadProductTable();
        txtSearch.textProperty().addListener(searchListener);
        System.gc();
        System.out.println("✅ Đã nạp lại dữ liệu thành công, bộ nhớ đã được giải phóng.");
    }

    @FXML
    private void onCheckSheet() {
        Window parentWindow = jtable.getScene().getWindow();
        openDialog("fxml/dialogCheckSheetWareHouse.fxml", "Kiểm kho", false, Modality.NONE, parentWindow,
                (DialogCheckSheetWareHouse ctrl) -> {
                    ctrl.initData(selectedDrawerItem);
                });
    }

    @FXML
    private void onOpenAppendix() {
        Window parentWindow = jtable.getScene().getWindow();
        openDialog("fxml/dialogAppendix.fxml", "Phụ lục", false, Modality.NONE, parentWindow, null);
    }

    @FXML
    private void onOpenChart() {
        Window parentWindow = jtable.getScene().getWindow();
        openDialog("fxml/dialogStatistical.fxml", "Thống kê " + selectedDrawerItem.getNameWareHouse(), true,
                Modality.NONE, parentWindow, (DialogStatistical ctrl) -> {
                    ctrl.setInit(selectedDrawerItem);
                });
    }

    @FXML
    private void onGuarantee() {
        Window parentWindow = jtable.getScene().getWindow();
        openDialog("fxml/dialogGuaranteeWareHouse.fxml", "Bảo hành", true, Modality.NONE, parentWindow, null);
    }

    @FXML
    private void onCart() {
        Window parentWindow = jtable.getScene().getWindow();
        openDialog("fxml/dialogCartWareHouse.fxml", "Giỏ hàng", true, Modality.NONE, parentWindow, null);
    }

    private void loadSearchColumns(TableView<ObservableList<String>> table) {
        cbbSearch.getItems().clear();
        cbbSearch.getItems().add(SEARCH_ALL);
        for (TableColumn<?, ?> col : table.getColumns()) {
            String header = col.getText();
            if (col.isVisible() && header != null && !header.trim().isEmpty())
                cbbSearch.getItems().add(header);
        }
        cbbSearch.getSelectionModel().select(0);
    }

    private int getColumnIndexByHeader(String header, TableView<ObservableList<String>> table) {
        for (int i = 0; i < table.getColumns().size(); i++) {
            if (header.equals(table.getColumns().get(i).getText()))
                return i;
        }
        return -1;
    }

    private void setTableData(TableView<ObservableList<String>> table, ObservableList<ObservableList<String>> data) {
        TableViewManager manager = tableManagers.get(table);
        if (manager == null) {
            manager = new TableViewManager();
            manager.setupTableView(table, data);
            tableManagers.put(table, manager);
        } else {
            manager.reloadData(data);
        }
        manager.setupTableView(table, data);
        table.setItems(manager.getFilteredData());
    }

    private void startCartWatcher() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                int count = dbInfoHelper.getCartCountFromDB();
                Platform.runLater(() -> {
                    if (count > change && !notificationShowing) {
                        notificationShowing = true;
                        Notifications.create().title("Thông báo").text("Có đơn hàng mới").position(Pos.BOTTOM_RIGHT)
                                .hideAfter(Duration.seconds(3)).onAction(e -> onCart()).showInformation();

                        PauseTransition delay = new PauseTransition(Duration.seconds(3));
                        delay.setOnFinished(e -> notificationShowing = false);
                        delay.play();
                    }
                    change = count;
                    CartState.getInstance().setCartCount(count);
                });
            } catch (Exception e) {
                System.err.println("Lỗi đồng bộ dữ liệu giỏ hàng ngầm: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    @FXML
    private void onExportExcel() {
        try {
            if (selectedDrawerItem.getWareHouseCategory() == 0) {
                Stage stage = (Stage) jtable.getScene().getWindow();
                if (functionHelper.exportExcel(tabInformationTable, stage, "sheet1")) {
                    customDialogNotification.showDialog("Thành công", "Xuất Excel thành công",
                            Alert.AlertType.INFORMATION);
                }
            } else {
                Window parentWindow = jtable.getScene().getWindow();
                openDialog("fxml/dialogExportExcelWareHouse.fxml", "Thêm sản phẩm", true, Modality.NONE, parentWindow,
                        null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdownCartWatcher() {
        if (scheduler != null && !scheduler.isShutdown())
            scheduler.shutdownNow();
        if (searchListener != null)
            txtSearch.textProperty().removeListener(searchListener);
        clearTableMemory();
    }

    private void openEditProductTable() {
        TabPane tabPane = new TabPane();
        Tab tabCreate = new Tab("Thêm sản phẩm"), tabUpdate = new Tab("Sửa sản phẩm"),
                tabDelete = new Tab("Xóa sản phẩm");
        tabCreate.setClosable(false);
        tabUpdate.setClosable(false);
        tabDelete.setClosable(false);

        EditableTableViewCreateProduct pCreate = new EditableTableViewCreateProduct();
        EditableTableViewUpdateProduct pUpdate = new EditableTableViewUpdateProduct();
        EditableTableViewDeleteProduct pDelete = new EditableTableViewDeleteProduct();

        tabCreate.setContent(new BorderPane(pCreate.getTable(), pCreate.createToolbar(), null, null, null));
        tabUpdate.setContent(new BorderPane(pUpdate.getTable(), pUpdate.createToolbar(), null, null, null));
        tabDelete.setContent(new BorderPane(pDelete.getTable(), pDelete.createToolbar(), null, null, null));

        tabPane.getTabs().addAll(tabCreate, tabUpdate, tabDelete);
        Stage dialog = new Stage();
        dialog.setScene(new Scene(tabPane, 1000, 600));
        dialog.setTitle("Nhập liệu sản phẩm");

        // Thiết lập liên kết với cửa sổ chính nhưng không khóa
        dialog.initOwner(jtable.getScene().getWindow());
        dialog.initModality(Modality.NONE);

        dialog.setOnHidden(e -> {
            if (pCreate.getTable() != null) {
                pCreate.getTable().setItems(null);
                pCreate.getTable().getColumns().clear();
            }
            if (pUpdate.getTable() != null) {
                pUpdate.getTable().setItems(null);
                pUpdate.getTable().getColumns().clear();
            }
            if (pDelete.getTable() != null) {
                pDelete.getTable().setItems(null);
                pDelete.getTable().getColumns().clear();
            }

            tabCreate.setContent(null);
            tabUpdate.setContent(null);
            tabDelete.setContent(null);

            tabPane.getTabs().clear();
            dialog.setScene(null);

            System.gc();
            System.out.println("♻️ [RAM] Đã dọn dẹp sạch sẽ bộ nhớ Tab Nhập Liệu Sản Phẩm.");
        });
        dialog.show(); // Dùng show() để cha tương tác bình thường
    }

    private void openEditHistoryTable() {
        TabPane tabPane = new TabPane();
        Tab tabImport = new Tab("Nhập hàng"), tabExport = new Tab("Xuất hàng");
        tabImport.setClosable(false);
        tabExport.setClosable(false);

        EditableTableViewCreateHistory hImport = new EditableTableViewCreateHistory(selectedDrawerItem, "IMPORT");
        EditableTableViewCreateHistory hExport = new EditableTableViewCreateHistory(selectedDrawerItem, "EXPORT");

        tabImport.setContent(new BorderPane(hImport.getTable(), hImport.createToolbar(), null, null, null));
        tabExport.setContent(new BorderPane(hExport.getTable(), hExport.createToolbar(), null, null, null));

        tabPane.getTabs().addAll(tabImport, tabExport);
        Stage dialog = new Stage();
        dialog.setScene(new Scene(tabPane, 1000, 600));
        dialog.setTitle("Nhập liệu lịch sử");

        // Thiết lập liên kết với cửa sổ chính nhưng không khóa
        dialog.initOwner(jtable.getScene().getWindow());
        dialog.initModality(Modality.NONE);

        dialog.setOnHidden(e -> {
            if (hImport.getTable() != null) {
                hImport.getTable().setItems(null);
                hImport.getTable().getColumns().clear();
            }
            if (hExport.getTable() != null) {
                hExport.getTable().setItems(null);
                hExport.getTable().getColumns().clear();
            }

            tabImport.setContent(null);
            tabExport.setContent(null);

            tabPane.getTabs().clear();
            dialog.setScene(null);

            System.gc();
            System.out.println("♻️ [RAM] Đã dọn dẹp sạch sẽ bộ nhớ Tab Lịch Sử Kho.");
        });
        dialog.show(); // Dùng show() để cha tương tác bình thường
    }

    @FXML
    private void onSort() {

        if (selectedDrawerItem.getWareHouseCategory() == 0) {
            List<ColumnConfig> columnsFromDB = dbInfoHelper.getAllConfigColumnProduct();
            columnsFromDB.add(0, new ColumnConfig("ProductAID", "Mã định danh"));
            configDialog.showDialog("tabProduct.txt", columnsFromDB);
        } else {
            List<ColumnConfig> columnsFromDB = dbInfoHelper.getAllConfigColumnWareHouse();
            columnsFromDB.add(0, new ColumnConfig("DataWareHouseAID", "Mã định danh"));
            configDialog.showDialog("tabWareHouse.txt", columnsFromDB);
        }

    }

    private String convertSQLquery(DrawerItem item) {

        // Trong DialogCartWareHouse.java, hàm convertSQLquery()
        String rawColumns = item.getWareHouseCategory() == 0 ? configDialog.getJoinedColumnIds("tabProduct.txt")
                : configDialog.getJoinedColumnIds("tabWareHouse.txt");
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

        StringBuilder sql = new StringBuilder(
                "SELECT " + column + " FROM " + item.getWareHouseTable() + " ORDER BY LastTime DESC, ProductID DESC ");

        return sql.toString();
    }

}