package com.phuthanh.warehouse.controller;

import com.jfoenix.controls.JFXHamburger;
// import com.phuthanh.Main;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.custom.TabContextMenuHandler;
import com.phuthanh.drawer.DrawerActionListener;
import com.phuthanh.drawer.DrawerController;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.helper.TabViewHelper;
import com.phuthanh.manager.DrawerManager;
import com.phuthanh.manager.TableViewManager;
import com.phuthanh.model.warehouse.DrawerItem;
// import com.phuthanh.screen.dialog.DialogUpdateHistory;
import com.phuthanh.store.AppState;
import com.phuthanh.store.CartState;
import com.phuthanh.warehouse.EditableTableView.tableView.historyWarehouse.EditableTableViewCreateHistory;
import com.phuthanh.warehouse.EditableTableView.tableView.product.EditableTableViewCreateProduct;
import com.phuthanh.warehouse.EditableTableView.tableView.product.EditableTableViewDeleteProduct;
import com.phuthanh.warehouse.EditableTableView.tableView.product.EditableTableViewUpdateProduct;
import com.phuthanh.warehouse.screen.dialog.DialogAppendix;
import com.phuthanh.warehouse.screen.dialog.DialogCartWareHouse;
import com.phuthanh.warehouse.screen.dialog.DialogCheckSheetWareHouse;
import com.phuthanh.warehouse.screen.dialog.DialogCreateHistoryController;
import com.phuthanh.warehouse.screen.dialog.DialogCreateProductController;
import com.phuthanh.warehouse.screen.dialog.DialogImportExcel;
import com.phuthanh.warehouse.screen.dialog.DialogStatistical;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
// import javafx.beans.property.IntegerProperty;
// import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.controlsfx.control.Notifications;
import javafx.geometry.Pos;

public class HomeController implements DrawerActionListener {
    @FXML
    private TabPane jtable;
    @FXML
    private Tab tabInformation;
    @FXML
    private Tab tabDetails;
    @FXML
    private Tab tabRequest, tabProductIDMain;
    @FXML
    private TableView<ObservableList<String>> tabInformationTable;
    @FXML
    private TableView<ObservableList<String>> tabDetailTable;
    @FXML
    private TableView<ObservableList<String>> tabRequestTable;
    @FXML
    private TableView<ObservableList<String>> tabProductIDMainTable;
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnGuarantee;
    @FXML
    private ComboBox<String> cbbSearch;
    @FXML
    private JFXHamburger hamburger;
    @FXML
    private AnchorPane drawer;
    @FXML
    private AnchorPane overlay;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnCart;
    @FXML
    private Button btnImportExcel;
    @FXML
    private Button btnCheckSheet;
    @FXML
    private Button btnChart;
    @FXML
    private Button btnDetailsProduct;
    @FXML
    private Label cartBadge;

    // state giỏ hàng (global đơn giản)
    // private final IntegerProperty cartCount = new SimpleIntegerProperty(0);
    private final TabViewHelper tabViewHelper = new TabViewHelper();
    private final DrawerManager drawerManager = new DrawerManager();
    private final Map<TableView<?>, TableViewManager> tableManagers = new HashMap<>();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    private DrawerController drawerController;
    private DrawerItem selectedDrawerItem;
    private double drawerWidth;
    private String AIDInfo = tabViewHelper.getSelectedAID();
    public Runnable onReloadRequested;
    private ObservableList<ObservableList<String>> allData;
    private ObservableList<ObservableList<String>> allDataProductIDMain;
    private ObservableList<ObservableList<String>> allDataRequest;
    private final DbTableHelper dbTableHelper = new DbTableHelper();
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final TabContextMenuHandler tabContextMenuHandler = new TabContextMenuHandler();
    boolean userRole;
    private int change = 0;
    private ScheduledExecutorService scheduler;
    private boolean notificationShowing = false;

    public void initialize() {
        selectedDrawerItem = new DrawerItem(
                "1", "Danh mục sản phẩm", "vwProduct", 0, "", "Product", "", "vwRequestProduct", "RequestProduct", "",
                "", "", "", 0, "", "");
        // tableViewManager = new TableViewManager();
        loadProductTable();

        drawerWidth = drawer.getPrefWidth();
        drawer.setTranslateX(-drawerWidth);
        overlay.setVisible(false);
        overlay.setMouseTransparent(true);

        tabInformation.setText(selectedDrawerItem.getNameWareHouse());
        // tabRequest.setText("Thùng rác " +
        // selectedDrawerItem.getNameWareHouse().toLowerCase());
        tabRequest.setText("Thùng rác ");
        tabDetails.setDisable(true);
        jtable.getTabs().remove(tabDetails);
        AppState.getInstance().set("selectedDrawerItem", selectedDrawerItem);
        btnHistory.setVisible(false);
        // btnChart.setVisible(false);
        btnCheckSheet.setVisible(false);
        userRole = Boolean.TRUE.equals(
                AppState.getInstance().get("UserRole", Boolean.class));
        try {
            // FXMLLoader loader = new
            // FXMLLoader(getClass().getResource("/fxml/Drawer.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/drawer.fxml"));

            AnchorPane drawerPane = loader.load();
            drawerController = loader.getController();
            drawerController.setListener(this);
            drawer.getChildren().setAll(drawerPane);
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadSearchColumns(tabInformationTable);
        // loadSearchColumns(tabInformationTable);
        tabContextMenuHandler.attachDefaultContextMenu(tabDetailTable, () -> tabViewHelper.getSelectedAID(),
                () -> loadProductTable());
        tabContextMenuHandler.attachDefaultContextMenu(tabInformationTable, () -> tabViewHelper.getSelectedAID(),
                () -> loadProductTable());
        tabContextMenuHandler.attachRequestContextMenu(tabRequestTable, () -> tabViewHelper.getSelectedAID());
        // setupTableContextMenus();
        tabContextMenuHandler.setOnReloadCallback(() -> loadProductTable());

        hamburger.setOnMouseClicked(e -> drawerManager.toggleDrawer(drawer, overlay, drawerWidth));
        overlay.setOnMouseClicked(e -> drawerManager.hideDrawer(drawer, overlay, drawerWidth));

        txtSearch.textProperty().addListener((obs, oldText, newText) -> {
            searchByColumnRealtime(newText, allData, tabInformationTable);
            searchByColumnRealtime(newText, allDataRequest, tabRequestTable);
            searchByColumnRealtime(newText, allDataProductIDMain, tabProductIDMainTable);
        });
        cbbSearch.setOnAction(e -> {
            searchByColumnRealtime(txtSearch.getText(), allData, tabInformationTable);
            searchByColumnRealtime(txtSearch.getText(), allDataRequest, tabRequestTable);
            searchByColumnRealtime(txtSearch.getText(), allDataProductIDMain, tabProductIDMainTable);

        });
        btnSearch.setVisible(false);
        btnSearch.setManaged(false);
        tabViewHelper.clickItemSaveAID(tabInformationTable);
        tabViewHelper.clickItemSaveAID(tabDetailTable);
        tabViewHelper.clickItemSaveAID(tabRequestTable);
        tabViewHelper.clickItemSaveAID(tabProductIDMainTable);

        // Sửa phần listener của jtable.getSelectionModel()
        jtable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTab, newTab) -> {
                    txtSearch.clear();

                    if (newTab == tabInformation) {
                        loadSearchColumns(tabInformationTable);
                        // 🔥 Khôi phục dữ liệu đã filter (nếu có)
                        TableViewManager manager = getTableManager(tabInformationTable);

                        if (manager != null && manager.getFilteredData() != null) {
                            tabInformationTable.setItems(manager.getFilteredData());
                        } else if (allData != null) {
                            tabInformationTable.setItems(allData);
                        }
                    }
                    if (newTab == tabRequest) {
                        loadSearchColumns(tabRequestTable);
                        if (allDataRequest != null) {
                            tabRequestTable.setItems(allDataRequest);
                        }
                    }
                    if (newTab == tabProductIDMain) {
                        loadSearchColumns(tabProductIDMainTable);
                        if (allDataProductIDMain != null) {
                            tabProductIDMainTable.setItems(allDataProductIDMain);
                        }
                    }
                });

        // bind UI với state
        cartBadge.textProperty().bind(
                CartState.getInstance().cartCountProperty().asString());

        cartBadge.visibleProperty().bind(
                CartState.getInstance().cartCountProperty().greaterThan(0));
        startCartWatcher();
    }

    @FXML
    private void onCreateClick() {
        if (!userRole) {
            customDialogNotification.showDialog("Thông tin", "Bạn không có quyền của kho",
                    Alert.AlertType.WARNING);
            return;
        } else {
            if (selectedDrawerItem.getWareHouseCategory() > 0) {
                openDialogWareHouse(AIDInfo);
            } else {
                openDialogProduct(AIDInfo);
            }
        }

    }

    @FXML
    private void onOpenDetailsProduct() {
        try {
            // FXMLLoader loader = new FXMLLoader(
            // tabContextMenuHandler.class.getResource("/fxml/dialogDetailsProduct.fxml"));
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogDetailsProduct.fxml"));

            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Chi tiết sản phẩm");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ImportExcel() {
        try {
            if (!userRole) {
                customDialogNotification.showDialog("Thông tin", "Bạn không có quyền của kho",
                        Alert.AlertType.WARNING);
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/dialogImportExcel.fxml"));
            Parent root = loader.load();

            DialogImportExcel controller = loader.getController();
            controller.initData(() -> loadProductTable(), selectedDrawerItem);

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
    private void ImportTableView() {
        if (!userRole) {
            customDialogNotification.showDialog("Thông tin", "Bạn không có quyền của kho",
                    Alert.AlertType.WARNING);
            return;
        }

        if (selectedDrawerItem.getWareHouseCategory() > 0) {
            openEditHistoryTable();
        } else {
            openEditProductTable();
        }
    }

    @FXML
    private void onOpenHistory() {
        try {
            DrawerItem selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            // FXMLLoader loader = new FXMLLoader(
            // tabContextMenuHandler.class.getResource("/fxml/dialogHistoryWareHouse.fxml"));
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogHistoryWareHouse.fxml"));

            Parent root = loader.load();
            HistoryController controller = loader.getController();
            controller.initData(selectedDrawerItem, null, () -> loadProductTable());

            Stage dialog = new Stage();
            dialog.setTitle("Lịch sử nhập xuất");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(true);
            // dialog.showAndWait();
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDialogWareHouse(String id) {
        try {
            // FXMLLoader loader = new
            // FXMLLoader(getClass().getResource("/fxml/dialogCreateHistory.fxml"));
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCreateHistory.fxml"));

            Parent root = loader.load();
            DialogCreateHistoryController controller = loader.getController();

            // Truyền giá trị String
            controller.setProductAID(id, true, false, false);

            // Callback khi save thành công
            controller.setOnCreateSuccess(this::loadProductTable);

            Stage dialog = new Stage();
            dialog.setTitle("Thêm mới kho");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDialogProduct(String id) {
        try {
            // FXMLLoader loader = new
            // FXMLLoader(getClass().getResource("/fxml/dialogCreateProduct.fxml"));
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCreateProduct.fxml"));

            Parent root = loader.load();
            DialogCreateProductController controller = loader.getController();

            // Truyền giá trị String
            controller.setProductAID(id);

            // Callback khi save thành công
            controller.setOnCreateSuccess(this::loadProductTable);

            Stage dialog = new Stage();
            dialog.setTitle("Thêm mới sản phẩm");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProductTable() {
        allData = FXCollections.observableArrayList();
        allDataRequest = FXCollections.observableArrayList();
        allDataProductIDMain = FXCollections.observableArrayList();

        if (selectedDrawerItem != null) {
            ObservableList<ObservableList<String>> allDataWH = dbTableHelper.loadDataTable(tabInformationTable,
                    "SELECT * FROM " + selectedDrawerItem.getWareHouseTable()
                            + " ORDER BY LastTime DESC, ProductID DESC");
            allData = allDataWH;
        } else {
            ObservableList<ObservableList<String>> allDataP = dbTableHelper.loadDataTable(tabInformationTable,
                    "SELECT * FROM vwProduct ORDER BY LastTime DESC, ProductID DESC");
            allData = allDataP;
        }

        // Load các dữ liệu khác
        allDataRequest = dbTableHelper.loadDataTable(
                tabRequestTable, "SELECT * FROM " + selectedDrawerItem.getWareHouseRequest()
                        + " ORDER BY LastTime DESC, ProductID DESC");
        allDataProductIDMain = dbTableHelper.loadDataTable(tabProductIDMainTable,
                "SELECT * FROM vwProductIDMain ORDER BY ProductIDMain");

        // ✅ QUAN TRỌNG: Set data cho table
        setTableData(tabInformationTable, allData);
        setTableData(tabProductIDMainTable, allDataProductIDMain);
        setTableData(tabRequestTable, allDataRequest);

        // Set data cho các table khác
        tabRequestTable.setItems(allDataRequest);
        // tabProductIDMainTable.setItems(allDataProductIDMain);
        loadSearchColumns(tabInformationTable);
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
        if (selectedDrawerItem.getWareHouseCategory() > 0) {
            btnHistory.setVisible(true);
            btnCheckSheet.setVisible(true);

        } else {
            btnHistory.setVisible(false);
            btnCheckSheet.setVisible(false);

        }
        tabRequest.setText("Thùng rác ");
        System.out.println(selectedDrawerItem.getNameWareHouse());
        AppState.getInstance().set("selectedDrawerItem", selectedDrawerItem);

        resetTableManager();
        loadProductTable();

        if (!tabInformationTable.getColumns().isEmpty() && !tabInformationTable.getItems().isEmpty()) {
            tabInformationTable.getSelectionModel().select(0, tabInformationTable.getColumns().get(0));
        }

        tabContextMenuHandler.attachDefaultContextMenu(tabDetailTable, () -> tabViewHelper.getSelectedAID(),
                () -> loadProductTable());
        tabContextMenuHandler.attachDefaultContextMenu(tabInformationTable, () -> tabViewHelper.getSelectedAID(),
                () -> loadProductTable());
        // Chuyển về tab Thông tin
        tabInformation.getTabPane().getSelectionModel().select(tabInformation);
        // Focus cell đầu tiên
        // tableViewManager.focusFirstCell(tabInformationTable);

    }

    @FXML
    private void onSearch() {
        // loadProductTable();
    }

    @FXML
    private void onReload() {

        loadProductTable();

        tableManagers.values()
                .forEach(TableViewManager::clearAllFilters);
    }

    @FXML
    private void onCheckSheet() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCheckSheetWareHouse.fxml"));

            Parent root = loader.load();
            DialogCheckSheetWareHouse controller = loader.getController();
            controller.initData(selectedDrawerItem);

            Stage dialog = new Stage();
            dialog.setTitle("Kiểm kho");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(false);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onOpenAppendix() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogAppendix.fxml"));

            Parent root = loader.load();
            DialogAppendix controller = loader.getController();

            Stage dialog = new Stage();
            dialog.setOnHidden(e -> controller.cleanup());
            dialog.setTitle("Phụ lục");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            // dialog.setResizable(false);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final String SEARCH_ALL = "Tất cả";

    private void loadSearchColumns(TableView<ObservableList<String>> table) {
        cbbSearch.getItems().clear();

        // ⭐ Thêm "Tất cả" làm item đầu
        cbbSearch.getItems().add(SEARCH_ALL);

        for (TableColumn<?, ?> col : table.getColumns()) {
            String header = col.getText();
            if (col.isVisible() && header != null && !header.trim().isEmpty()) {
                cbbSearch.getItems().add(header);
            }
        }

        // ⭐ chọn "Tất cả" mặc định
        cbbSearch.getSelectionModel().select(0);
    }

    private int getColumnIndexByHeader(String header, TableView<ObservableList<String>> table) {
        for (int i = 0; i < table.getColumns().size(); i++) {
            TableColumn<?, ?> col = table.getColumns().get(i);

            // BẮT BUỘC: tránh NullPointerException
            String colHeader = col.getText();
            if (colHeader == null)
                continue;

            if (colHeader.equals(header)) {
                return i;
            }
        }
        return -1;
    }

    // Sửa method searchByColumnRealtime trong HomeController
    private void searchByColumnRealtime(String keyword,
            ObservableList<ObservableList<String>> rawData,
            TableView<ObservableList<String>> table) {
        if (rawData == null)
            return;

        String selectedHeader = cbbSearch.getSelectionModel().getSelectedItem();
        if (selectedHeader == null) {
            restoreFilteredData(table);
            return;
        }

        keyword = keyword == null ? "" : keyword.trim().toLowerCase();

        if (keyword.isEmpty()) {
            restoreFilteredData(table);
            return;
        }

        // 🔥 QUAN TRỌNG: Lấy nguồn dữ liệu hiện tại (đã filter)
        ObservableList<ObservableList<String>> sourceData = getCurrentDisplayData(table);

        ObservableList<ObservableList<String>> filtered = FXCollections.observableArrayList();

        if (SEARCH_ALL.equals(selectedHeader)) {
            for (ObservableList<String> row : sourceData) {
                for (String cell : row) {
                    if (cell != null && cell.toLowerCase().contains(keyword)) {
                        filtered.add(row);
                        break;
                    }
                }
            }
        } else {
            int colIndex = getColumnIndexByHeader(selectedHeader, table);
            if (colIndex < 0)
                return;

            for (ObservableList<String> row : sourceData) {
                if (row.size() > colIndex) {
                    String cell = row.get(colIndex);
                    if (cell != null && cell.toLowerCase().contains(keyword)) {
                        filtered.add(row);
                    }
                }
            }
        }

        table.setItems(filtered);
    }

    // Thêm method helper để lấy dữ liệu hiện tại (đã filter)
    private ObservableList<ObservableList<String>> getCurrentDisplayData(TableView<ObservableList<String>> table) {
        // Nếu là tabInformation thì lấy từ TableViewManager đã filter
        TableViewManager manager = getTableManager(table);

        if (manager != null) {
            FilteredList<ObservableList<String>> filteredData = manager.getFilteredData();

            if (filteredData != null && !filteredData.isEmpty()) {
                return filteredData;
            }
        }

        // Fallback: lấy từ items hiện tại của table
        ObservableList<ObservableList<String>> currentItems = table.getItems();
        if (currentItems != null && !currentItems.isEmpty()) {
            return currentItems;
        }

        // Nếu không có gì, trả về raw data
        if (table == tabInformationTable)
            return allData;
        if (table == tabRequestTable)
            return allDataRequest;
        if (table == tabProductIDMainTable)
            return allDataProductIDMain;

        return FXCollections.observableArrayList();
    }

    // Thêm method để khôi phục dữ liệu đã filter sau khi xóa search
    private void restoreFilteredData(TableView<ObservableList<String>> table) {
        TableViewManager manager = getTableManager(table);

        if (manager != null) {

            FilteredList<ObservableList<String>> filteredData = manager.getFilteredData();

            if (filteredData != null) {
                table.setItems(filteredData);
                return;
            }
        }

        // Fallback: dùng raw data
        if (table == tabInformationTable) {
            table.setItems(allData);
        } else if (table == tabRequestTable) {
            table.setItems(allDataRequest);
        } else if (table == tabProductIDMainTable) {
            table.setItems(allDataProductIDMain);
        }
    }

    private void setTableData(
            TableView<ObservableList<String>> table,
            ObservableList<ObservableList<String>> data) {

        if (data == null || data.isEmpty()) {
            System.err.println("⚠️ No data for table: " + table.getId());
            return;
        }

        TableViewManager manager = tableManagers.get(table);

        // chưa có manager
        if (manager == null) {

            manager = new TableViewManager();

            manager.setupTableView(table, data);

            tableManagers.put(table, manager);
        }
        // đã có manager
        else {

            System.out.println("🔄 Reload data for: " + table);

            manager.reloadData(data);
        }
    }

    private void startCartWatcher() {
        // Scheduler với thread daemon → tự chết khi app đóng
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            int count = dbInfoHelper.getCartCountFromDB();
            // System.out.println("Cart DB = " + count);

            Platform.runLater(() -> {
                // 🔥 Notification khi có đơn mới
                if (count > change) {
                    if (!notificationShowing) {
                        notificationShowing = true;

                        Notifications.create()
                                .title("Thông báo")
                                .text("Có đơn hàng mới")
                                .position(Pos.BOTTOM_RIGHT)
                                .hideAfter(Duration.seconds(3))
                                .onAction(e -> {
                                    // mở giỏ hàng khi click
                                    onCart();
                                })
                                .showInformation();

                        // reset flag sau khi hết thời gian hiển thị
                        PauseTransition delay = new PauseTransition(Duration.seconds(3));
                        delay.setOnFinished(e -> notificationShowing = false);
                        delay.play();
                    }
                }

                // cập nhật state
                change = count;
                CartState.getInstance().setCartCount(count);
            });
        }, 0, 10, TimeUnit.SECONDS);
    }

    @FXML
    private void onExportExcel() {
        try {
            if (selectedDrawerItem.getWareHouseCategory() == 0) {
                Stage stage = (Stage) jtable.getScene().getWindow();

                boolean result = functionHelper.exportExcel(tabInformationTable, stage, "sheet1");

                if (result) {
                    customDialogNotification.showDialog("Thành công", "Xuất Excel thành công",
                            Alert.AlertType.INFORMATION);
                } else {
                    System.out.println("Xuất thất bại!");
                    customDialogNotification.showDialog("Lỗi", "Xuất Excel thất bại", Alert.AlertType.ERROR);
                }
            } else {
                FXMLLoader loader = new FXMLLoader(
                        TabContextMenuHandler.class.getResource("/fxml/dialogExportExcelWareHouse.fxml"));
                Parent root = loader.load();

                Stage dialog = new Stage();
                dialog.setTitle("Thêm sản phẩm");
                dialog.setScene(new Scene(root));
                // dialog.initModality(Modality.WINDOW_MODAL);
                // dialog.initOwner(Main.getPrimaryStage());
                dialog.setResizable(true);
                // dialog.showAndWait();
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onOpenChart() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogStatistical.fxml"));

            Parent root = loader.load();
            DialogStatistical controller = loader.getController();
            controller.setInit(selectedDrawerItem);

            Stage dialog = new Stage();
            dialog.setTitle("Thống kê " + selectedDrawerItem.getNameWareHouse());
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(true);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onGuarantee() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogGuaranteeWareHouse.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.setTitle("Bảo hành");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(true);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCart() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCartWareHouse.fxml"));
            Parent root = loader.load();
            DialogCartWareHouse controller = loader.getController();
            Stage dialog = new Stage();
            dialog.setOnHidden(e -> {
                controller.cleanup();
            });
            dialog.setOnCloseRequest(e -> {
                controller.cleanup();
            }

            );

            dialog.setTitle("Giỏ hàng");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(true);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdownCartWatcher() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            System.out.println("Cart watcher stopped");
        }
    }

    private void resetTableManager() {
        tableManagers.clear();
    }

    private void openEditProductTable() {
        TabPane tabPane = new TabPane();
        Tab tabCreateProduct = new Tab("Thêm mới sản phẩm");
        Tab tabUpdateProduct = new Tab("Cập nhật sản phẩm");
        Tab tabDeleteProduct = new Tab("Xoá sản phẩm");

        tabCreateProduct.setClosable(false);
        tabUpdateProduct.setClosable(false);
        tabDeleteProduct.setClosable(false);
        // tabProduct.setContent(new EditableTableViewCreateProduct().getTable());
        EditableTableViewCreateProduct productCreate = new EditableTableViewCreateProduct();
        EditableTableViewUpdateProduct productUpdate = new EditableTableViewUpdateProduct();
        EditableTableViewDeleteProduct productDelete = new EditableTableViewDeleteProduct();

        BorderPane rootCreate = new BorderPane();
        rootCreate.setTop(productCreate.createToolbar());
        rootCreate.setCenter(productCreate.getTable());

        BorderPane rootUpdate = new BorderPane();
        rootUpdate.setTop(productUpdate.createToolbar());
        rootUpdate.setCenter(productUpdate.getTable());

        BorderPane rootDelete = new BorderPane();
        rootDelete.setTop(productDelete.createToolbar());
        rootDelete.setCenter(productDelete.getTable());

        tabCreateProduct.setContent(rootCreate);
        tabUpdateProduct.setContent(rootUpdate);
        tabDeleteProduct.setContent(rootDelete);

        tabPane.getTabs().addAll(tabCreateProduct, tabUpdateProduct, tabDeleteProduct);
        Stage dialog = new Stage();

        dialog.setScene(new Scene(tabPane, 1000, 600));
        dialog.setTitle("Nhập liệu sản phẩm");
        dialog.setResizable(true);

        dialog.show();
    }

    private void openEditHistoryTable() {
        TabPane tabPane = new TabPane();
        Tab tabCreateHistoryImport = new Tab("Nhập hàng");
        Tab tabCreateHistoryExport = new Tab("Xuất hàng");

        tabCreateHistoryImport.setClosable(false);
        tabCreateHistoryExport.setClosable(false);
        EditableTableViewCreateHistory historyImport = new EditableTableViewCreateHistory(selectedDrawerItem, "IMPORT");
        EditableTableViewCreateHistory historyExport = new EditableTableViewCreateHistory(selectedDrawerItem, "EXPORT");

        BorderPane roothistoryImport = new BorderPane();
        roothistoryImport.setTop(historyImport.createToolbar());
        roothistoryImport.setCenter(historyImport.getTable());

        BorderPane roothistoryExport = new BorderPane();
        roothistoryExport.setTop(historyExport.createToolbar());
        roothistoryExport.setCenter(historyExport.getTable());

        tabCreateHistoryImport.setContent(roothistoryImport);
        tabCreateHistoryExport.setContent(roothistoryExport);

        tabPane.getTabs().addAll(tabCreateHistoryImport, tabCreateHistoryExport);
        Stage dialog = new Stage();

        dialog.setScene(new Scene(tabPane, 1000, 600));
        dialog.setTitle("Nhập liệu lịch sử");
        dialog.setResizable(true);

        dialog.show();
    }

    private TableViewManager getTableManager(TableView<?> table) {
        return tableManagers.get(table);
    }

}
