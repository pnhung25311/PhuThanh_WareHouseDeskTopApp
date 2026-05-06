package com.phuthanh.business.screen.dialog;

import java.time.LocalDate;

import com.phuthanh.helper.DbTableHelper;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DialogReconciliation {

    // =====================
    // SEARCH + FILTER
    // =====================
    @FXML
    private TextField txtSearch;

    @FXML
    private DatePicker dpTime;
    @FXML
    private ComboBox<String> cbType;

    // =====================
    // BUTTONS
    // =====================
    @FXML
    private Button btnReload;

    @FXML
    private Button btnExportExcel;

    @FXML
    private Button btnCancel;

    // =====================
    // TABPANE
    // =====================
    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tabAllRowHistory; // Kho

    @FXML
    private Tab tabRowHistory; // Kinh doanh

    @FXML
    private Tab tabRowHistory1; // Đối soát

    // =====================
    // TABLES
    // =====================
    @FXML
    private TableView<ObservableList<String>> tbvWareHouse;

    @FXML
    private TableView<ObservableList<String>> tbvBusiness;

    @FXML
    private TableView<ObservableList<String>> tbvReconciliation;

    private ObservableList<ObservableList<String>> allDataWareHouse;
    private FilteredList<ObservableList<String>> filteredDataWareHouse;

    private ObservableList<ObservableList<String>> allDataBusiness;
    private FilteredList<ObservableList<String>> filteredDataBusiness;

    private ObservableList<ObservableList<String>> allDataReconciliation;
    private FilteredList<ObservableList<String>> filteredDataReconciliation;

    private static final DbTableHelper dbTableHelper = new DbTableHelper();

    // =====================
    // INIT
    // =====================
    @FXML
    public void initialize() {
        System.out.println("DialogReconciliation initialized");
        dpTime.setValue(LocalDate.now());

        setupSearch();
        setupDatePicker();
        setupComboBox();
        loadComboBox();
        loadData();
    }

    private void loadData() {
        // int type = cbType.getSelectionModel().getSelectedIndex() == 0 ? 1 : 0; // 1: Phiếu nhập, 2: Phiếu xuất
        allDataWareHouse = dbTableHelper.loadDataTable(tbvWareHouse,
                "select * from dbo.fn_GetStockSummaryByDate('" + dpTime.getValue() + "')");
        allDataBusiness = dbTableHelper.loadDataTable(tbvBusiness,
                "select * from dbo.fn_GetCartSummaryByDate('" + dpTime.getValue() + "')");
        allDataReconciliation = dbTableHelper.loadDataTable(tbvReconciliation,
                "select * from dbo.fn_GetReconciliationByDate('" + dpTime.getValue() + "')");

        filteredDataWareHouse = new FilteredList<>(allDataWareHouse, p -> true);
        filteredDataBusiness = new FilteredList<>(allDataBusiness, p -> true);
        filteredDataReconciliation = new FilteredList<>(allDataReconciliation, p -> true);

        tbvWareHouse.setItems(filteredDataWareHouse);
        tbvBusiness.setItems(filteredDataBusiness);
        tbvReconciliation.setItems(filteredDataReconciliation);
    }

    private void loadComboBox() {

        cbType.getItems().addAll("Phiếu nhập", "Phiếu xuất");
        if (!cbType.getItems().isEmpty()) {
            cbType.getSelectionModel().selectFirst();
        }
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Search: " + newVal);
            // TODO: filter theo tab hiện tại

        });
    }

    private void setupComboBox() {
        cbType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Loại phiếu: " + newVal);
            // TODO: reload dữ liệu theo loại phiếu
            loadData();
        });
    }

    private void setupDatePicker() {
        dpTime.valueProperty().addListener((obs, oldValue, newValue) -> {
            System.out.println("Ngày cũ: " + oldValue);
            System.out.println("Ngày mới: " + newValue);

            // TODO: reload dữ liệu theo ngày mới
            loadData();
        });
    }

    // =====================
    // ACTIONS
    // =====================

    @FXML
    private void onReload() {
        System.out.println("Reload data...");
        // TODO: load lại dữ liệu cho 3 bảng
    }

    @FXML
    private void onExportExcel() {
        System.out.println("Export Excel...");
        // TODO: export tbvReconciliation hoặc tab hiện tại
    }

    @FXML
    private void onCancel() {
        System.out.println("Close dialog...");
        // TODO: đóng window
    }
}