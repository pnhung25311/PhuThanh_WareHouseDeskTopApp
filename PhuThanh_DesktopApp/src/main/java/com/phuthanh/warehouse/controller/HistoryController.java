package com.phuthanh.warehouse.controller;

import java.time.LocalDate;
import java.util.Map;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.helper.TabViewHelper;
import com.phuthanh.manager.TableViewManager;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.warehouse.contextmenu.TabContextMenuHistory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
// import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class HistoryController {

    // ============================================
    // ÁNH XẠ TỪ FXML
    // ============================================

    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnReload;

    @FXML
    private DatePicker FromDate;

    @FXML
    private DatePicker ToDate;

    @FXML
    private TabPane tabPane;

    @FXML
    private TableView<ObservableList<String>> tabAllRowHistoryTable;

    @FXML
    private TableView<ObservableList<String>> tabRequestHistoryTable;
    @FXML
    private Tab tabAllRowHistory;
    @FXML
    private Tab tabRowHistory;
    @FXML
    private Tab tabRequestHistory;

    @FXML
    private Button btnExportExcel;

    @FXML
    private Button btnCancel;
    @FXML
    private Label lblTotalImport;

    @FXML
    private Label lblTotalExport;

    // private String tableLoadData;
    private DrawerItem SelectedDrawerItem;
    Runnable callback;

    private ObservableList<ObservableList<String>> allHistoryData;
    private ObservableList<ObservableList<String>> allRequestHistoryData;

    private FilteredList<ObservableList<String>> filteredAllHistory;
    private FilteredList<ObservableList<String>> filteredRequestHistory;

    // private ObservableList<ObservableList<String>> allRowDataHistory;
    private String codeAID;
    private Map<String, Double> sumHistory;
    // private String selectedAID;
    // ============================================
    private   final DbTableHelper dbTableHelper = new DbTableHelper();
    private   final FunctionHelper functionHelper = new FunctionHelper();
    private   final TabViewHelper tabViewHelper = new TabViewHelper();
    private   final TableViewManager tableViewManager = new TableViewManager();
    private   final TabContextMenuHistory tabContextMenuHistory = new TabContextMenuHistory();
    private   final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private   final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    public void initialize() {
        setCurrentMonth(FromDate, ToDate);
        // datePickerInitialize();
        if (SelectedDrawerItem != null) {
            loadProductTable(functionHelper.convertDate(FromDate.getValue()),
                    functionHelper.convertDate(ToDate.getValue()));
        }

        // tableViewManager.setupTableView(tabAllRowHistoryTable, allHistoryData);
        // tableViewManager.setupTableView(tabRequestHistoryTable,
        // allRequestHistoryData);

        tabViewHelper.clickItemSaveAID(tabAllRowHistoryTable);
        tabViewHelper.clickItemSaveAID(tabRequestHistoryTable);
        // tabRequestHistory.setOnSelectionChanged(
        // event -> TabContentManager.loadTabHistoryUpdateToRow(tabRequestHistory,
        // tabRequestHistoryTable,
        // SelectedDrawerItem));
        loadRequestHistoryTable(
                functionHelper.convertDate(FromDate.getValue()),
                functionHelper.convertDate(ToDate.getValue()));
        tabRequestHistory.setOnSelectionChanged(event -> {
            if (tabRequestHistory.isSelected()) {
                loadRequestHistoryTable(
                        functionHelper.convertDate(FromDate.getValue()),
                        functionHelper.convertDate(ToDate.getValue()));
                applySearchFilter(txtSearch.getText());
            }
        });
        // setupSearchAll();

    }

    public void loadProductTable(String fromDate, String toDate) {
        allHistoryData = FXCollections.observableArrayList();
        if (SelectedDrawerItem == null)
            return;
        if (codeAID != null) {
            allHistoryData = dbTableHelper.loadTableDetailsHistory(
                    tabAllRowHistoryTable,
                    SelectedDrawerItem.getWareHouseHistory(), "DataWareHouseAID", codeAID);
            sumHistory = dbInfoHelper.getSumaryHistory(codeAID, SelectedDrawerItem.getWareHouseDataBaseHistory(), null,
                    null);
        } else {
            allHistoryData = dbTableHelper.loadTableHistoryConvert(
                    tabAllRowHistoryTable,
                    SelectedDrawerItem.getWareHouseHistory(),
                    fromDate, toDate);
            sumHistory = dbInfoHelper.getSumaryHistory(null, SelectedDrawerItem.getWareHouseDataBaseHistory(), fromDate,
                    toDate);
        }
        double importQty = sumHistory.get("import");
        double exportQty = sumHistory.get("export");

        lblTotalImport.setText(String.format(" %.2f", importQty));
        lblTotalExport.setText(String.format(" %.2f", exportQty));

        filteredAllHistory = new FilteredList<>(allHistoryData, p -> true);
        SortedList<ObservableList<String>> sorted = new SortedList<>(filteredAllHistory);
        sorted.comparatorProperty().bind(tabAllRowHistoryTable.comparatorProperty());

        tabAllRowHistoryTable.setItems(sorted);
    }

    private void loadRequestHistoryTable(String fromDate, String toDate) {
        allRequestHistoryData = FXCollections.observableArrayList();

        if (SelectedDrawerItem == null)
            return;

        allRequestHistoryData = dbTableHelper.loadTableDetails(
                tabRequestHistoryTable,
                SelectedDrawerItem.getWareHouseUpdateHistory(),
                null,
                null);

        filteredRequestHistory = new FilteredList<>(allRequestHistoryData, p -> true);
        SortedList<ObservableList<String>> sorted = new SortedList<>(filteredRequestHistory);
        sorted.comparatorProperty().bind(tabRequestHistoryTable.comparatorProperty());

        tabRequestHistoryTable.setItems(sorted);
    }

    public void initData(DrawerItem item, String codeaid, Runnable callBack) {
        this.SelectedDrawerItem = item;
        this.callback = callBack;
        this.codeAID = codeaid;
        if (callback != null) {
            System.out.println("ko null ở history");
        } else {
            System.out.println("bị nul ở history");
        }
        // this.selectedAID = selectedAID;
        loadProductTable(functionHelper.convertDate(FromDate.getValue()),
                functionHelper.convertDate(ToDate.getValue()));
        loadRequestHistoryTable(functionHelper.convertDate(FromDate.getValue()),
                functionHelper.convertDate(ToDate.getValue()));

        tableViewManager.setupTableView(tabAllRowHistoryTable, allHistoryData);
        tableViewManager.setupTableView(tabRequestHistoryTable, allRequestHistoryData);
        tabContextMenuHistory.attachDefaultContextMenu(tabAllRowHistoryTable, () -> tabViewHelper.getSelectedAID());
        tabContextMenuHistory.attachDefaultContextMenuRequest(tabRequestHistoryTable,
                () -> tabViewHelper.getSelectedAID(),()->()-> loadRequestHistoryTable(
                functionHelper.convertDate(FromDate.getValue()),
                functionHelper.convertDate(ToDate.getValue())),
                () -> loadProductTable(functionHelper.convertDate(FromDate.getValue()),
                functionHelper.convertDate(ToDate.getValue())));
        setupSearch();
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applySearchFilter(newVal));

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTab, newTab) -> applySearchFilter(txtSearch.getText()));
    }

    private void applySearchFilter(String keyword) {
        String kw = keyword == null ? "" : keyword.toLowerCase().trim();

        if (tabPane.getSelectionModel().getSelectedItem() == tabAllRowHistory) {
            if (filteredAllHistory == null)
                return;
            filteredAllHistory.setPredicate(row -> matchRow(row, kw));
        } else if (tabPane.getSelectionModel().getSelectedItem() == tabRequestHistory) {
            if (filteredRequestHistory == null)
                return;
            filteredRequestHistory.setPredicate(row -> matchRow(row, kw));
        }
    }

    private boolean matchRow(ObservableList<String> row, String kw) {
        if (kw.isBlank())
            return true;

        for (String cell : row) {
            if (cell != null && cell.toLowerCase().contains(kw)) {
                return true;
            }
        }
        return false;
    }

    @FXML
    private void onExportExcel() {
        TableView<?> tableToExport = null;

        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == tabAllRowHistory) {
            tableToExport = tabAllRowHistoryTable;
        } else if (selectedTab == tabRequestHistory) {
            tableToExport = tabRequestHistoryTable;
        }

        if (tableToExport != null) {
            Stage stage = (Stage) tabPane.getScene().getWindow();
            boolean result = functionHelper.exportExcel(tableToExport, stage, "Export Data");

            if (result) {
                customDialogNotification.showDialog("Thành công", "Xuất Excel thành công",
                        Alert.AlertType.INFORMATION);
            } else {
                System.out.println("Xuất thất bại!");
                customDialogNotification.showDialog("Lỗi", "Xuất Excel thất bại", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onSearch() {
        loadProductTable(functionHelper.convertDate(FromDate.getValue()),
                functionHelper.convertDate(ToDate.getValue()));
    }

    @FXML
    private void onReload() {
        setCurrentMonth(FromDate, ToDate);
        loadProductTable(functionHelper.convertDate(FromDate.getValue()),
                functionHelper.convertDate(ToDate.getValue()));
        loadRequestHistoryTable(functionHelper.convertDate(FromDate.getValue()),
                functionHelper.convertDate(ToDate.getValue()));

    }

    // private void datePickerInitialize() {
    // FromDate.setValue(LocalDate.of(1900, 1, 1));
    // ToDate.setValue(LocalDate.now().plusDays(1));
    // }

    private void setCurrentMonth(DatePicker fromDate, DatePicker toDate) {
        LocalDate now = LocalDate.now();

        fromDate.setValue(now.withDayOfMonth(1));
        toDate.setValue(now.withDayOfMonth(now.lengthOfMonth()));
    }

}
