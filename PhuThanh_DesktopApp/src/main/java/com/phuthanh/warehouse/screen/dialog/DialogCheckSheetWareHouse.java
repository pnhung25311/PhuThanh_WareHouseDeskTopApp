package com.phuthanh.warehouse.screen.dialog;

import java.io.IOException;

// import com.phuthanh.Main;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.helper.TabViewHelper;
import com.phuthanh.manager.TableViewManager;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.store.AppState;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
// import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogCheckSheetWareHouse {

    /* ===== SEARCH AREA ===== */
    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnReload;

    /* ===== TAB PANE ===== */
    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tabSheetCheck;

    @FXML
    private Tab tabDataCheck;

    /* ===== TABLES ===== */
    @FXML
    private TableView<ObservableList<String>> tabSheetCheckTable;

    @FXML
    private TableView<ObservableList<String>> tabDataCheckTable;

    /* ===== FOOTER BUTTONS ===== */
    @FXML
    private Button btnExportExcel;
    @FXML
    private Button btnSheet;
    @FXML
    private Button btnDataCheck;

    @FXML
    private Button btnCancel;
    private DrawerItem selectedDrawerItem;
    private   final DbTableHelper dbTableHelper = new DbTableHelper();
    private   final TabViewHelper tabViewHelper = new TabViewHelper();
    private TableViewManager tableViewManager = new TableViewManager();
    private   final FunctionHelper functionHelper = new FunctionHelper();
    private   final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    private ObservableList<ObservableList<String>> allDataSheet;
    private ObservableList<ObservableList<String>> dataCheck;

    private String sheetAID;

    /* ===== INITIALIZE ===== */
    @FXML
    private void initialize() {
        System.out.println("DialogCheckSheetWareHouse loaded");

        tabViewHelper.clickItemSaveAID(tabSheetCheckTable);
        btnDataCheck.setVisible(false);

        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldTab, newTab) -> {
                    btnDataCheck.setVisible(newTab == tabDataCheck);
                });

        tabSheetCheckTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldRow, newRow) -> {
                    if (newRow != null) {
                        onSheetRowSelected(newRow);
                    }
                });

        txtSearch.textProperty().addListener((obs, oldText, newText) -> {
            handleSearch(newText);
        });
    }

    private void onSheetRowSelected(ObservableList<String> row) {
        btnDataCheck.setVisible(true);

        sheetAID = row.get(0);
        AppState.getInstance().set("sheetAID", sheetAID);

        tabPane.getSelectionModel().select(tabDataCheck);

        dataCheck = dbTableHelper.loadTableDetails(
                tabDataCheckTable,
                selectedDrawerItem.getWareHouseDataCheck(),
                "SheetAID",
                sheetAID);

        // ⭐ setup table tại đây
        tableViewManager.setupTableView(tabDataCheckTable, dataCheck);

        tabDataCheckTable.setItems(dataCheck);
    }

    public void initData(DrawerItem item) {
        this.selectedDrawerItem = item;

        loadData(); // load DB trước

        // ⭐ setup table SAU khi đã có data
        tableViewManager.setupTableView(tabSheetCheckTable, allDataSheet);
    }

    private void handleSearch(String keyword) {
        if (keyword == null)
            keyword = "";
        String searchText = keyword.toLowerCase().trim();

        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        if (selectedTab == tabSheetCheck) {
            filterTable(tabSheetCheckTable, allDataSheet, searchText);
        } else if (selectedTab == tabDataCheck) {
            filterTable(tabDataCheckTable, dataCheck, searchText);
        }
    }

    private void filterTable(
            TableView<ObservableList<String>> table,
            ObservableList<ObservableList<String>> sourceData,
            String keyword) {

        if (sourceData == null)
            return;

        // Nếu search rỗng → trả về data gốc
        if (keyword.isEmpty()) {
            table.setItems(sourceData);
            return;
        }

        ObservableList<ObservableList<String>> filteredData = javafx.collections.FXCollections.observableArrayList();

        for (ObservableList<String> row : sourceData) {
            for (String cell : row) {
                if (cell != null && cell.toLowerCase().contains(keyword)) {
                    filteredData.add(row);
                    break;
                }
            }
        }

        table.setItems(filteredData);
    }

    private void loadData() {
        if (selectedDrawerItem == null) {
            System.out.println("selectedDrawerItem is null");
            return;
        }

        // chỉ load tab Sheet khi mở dialog
        allDataSheet = dbTableHelper.loadTableConvertSheet(
                tabSheetCheckTable,
                selectedDrawerItem.getWareHouseSheet());

        tabSheetCheckTable.setItems(allDataSheet);

        // tab DataCheck để trống ban đầu
        tabDataCheckTable.setItems(FXCollections.observableArrayList());
    }

    /* ===== ACTIONS ===== */
    @FXML
    private void onReload() {
        System.out.println("Reload clicked");
        // reload data
        loadData();
    }

    @FXML
    private void onExportExcel() {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        TableView<ObservableList<String>> tableToExport = null;

        if (selectedTab == tabSheetCheck) {
            tableToExport = tabSheetCheckTable;
        } else if (selectedTab == tabDataCheck) {
            tableToExport = tabDataCheckTable;
        }

        if (tableToExport == null || tableToExport.getItems().isEmpty()) {
            System.out.println("Không có dữ liệu để export");
            return;
        }
        boolean result = functionHelper.exportExcel(tableToExport, stage, "sheet1");

        if (result) {
            customDialogNotification.showDialog("Thành công", "Xuất Excel thành công",
                    Alert.AlertType.INFORMATION);
        } else {
            System.out.println("Xuất thất bại!");
            customDialogNotification.showDialog("Lỗi", "Xuất Excel thất bại", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onClose() {
        System.out.println("Close dialog");
        btnCancel.getScene().getWindow().hide();
    }

    @FXML
    private void onSheet() {
        try {
            // FXMLLoader loader = new
            // FXMLLoader(getClass().getResource("/fxml/dialogCreateProduct.fxml"));
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogSheet.fxml"));

            Parent root = loader.load();
            DialogSheet controller = loader.getController();
            controller.initData(selectedDrawerItem, null, () -> loadData());

            Stage dialog = new Stage();
            dialog.setTitle("Tạo phiếu");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.APPLICATION_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(false);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDataCheck() {
        try {
            // FXMLLoader loader = new
            // FXMLLoader(getClass().getResource("/fxml/dialogCreateProduct.fxml"));
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCheckSheet.fxml"));

            Parent root = loader.load();
            DialogCheckSheet controller = loader.getController();
            controller.initData(selectedDrawerItem, sheetAID, () -> loadData());

            Stage dialog = new Stage();
            dialog.setTitle("Thêm kiểm kê");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.APPLICATION_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(false);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
