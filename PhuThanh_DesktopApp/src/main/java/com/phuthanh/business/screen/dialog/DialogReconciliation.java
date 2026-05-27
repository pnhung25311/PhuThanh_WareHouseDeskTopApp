package com.phuthanh.business.screen.dialog;

import java.time.LocalDate;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.model.warehouse.CCBdata;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DialogReconciliation {

    // =====================
    // SEARCH + FILTER
    // =====================
    @FXML
    private TextField txtSearch;

    @FXML
    private DatePicker dpTime;
    @FXML
    private ComboBox<CCBdata> cbType;

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

    private final DbTableHelper dbTableHelper = new DbTableHelper();
    private final FunctionHelper functionHelper = new FunctionHelper();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

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
        // int type = cbType.getSelectionModel().getSelectedIndex() == 0 ? 1 : 0; // 1:
        // Phiếu nhập, 2: Phiếu xuất
        tbvWareHouse.setItems(null);
        tbvBusiness.setItems(null);
        tbvReconciliation.setItems(null);

        allDataWareHouse = dbTableHelper.loadDataTable(tbvWareHouse,
                "select * from dbo.fn_GetStockSummaryByDate('" + dpTime.getValue() + "', " + cbType.getValue().getId()
                        + ")");
        allDataBusiness = dbTableHelper.loadDataTable(tbvBusiness,
                "select * from dbo.fn_GetCartSummaryByDate('" + dpTime.getValue() + "', " + cbType.getValue().getId()
                        + ")");
        allDataReconciliation = dbTableHelper.loadDataTable(tbvReconciliation,
                "select * from dbo.fn_GetReconciliationByDate('" + dpTime.getValue() + "', " + cbType.getValue().getId()
                        + ")");

        filteredDataWareHouse = new FilteredList<>(allDataWareHouse, p -> true);
        filteredDataBusiness = new FilteredList<>(allDataBusiness, p -> true);
        filteredDataReconciliation = new FilteredList<>(allDataReconciliation, p -> true);

        tbvWareHouse.setItems(filteredDataWareHouse);
        tbvBusiness.setItems(filteredDataBusiness);
        tbvReconciliation.setItems(filteredDataReconciliation);
        setupRowColor(tbvReconciliation);
    }

    private void loadComboBox() {
        cbType.getItems().addAll(
                new CCBdata(41, "Kho chính"),
                new CCBdata(43, "Kho 397"),
                new CCBdata(42, "Kho Khoáng Sản"),
                new CCBdata(44, "Kho Khe Dây"),
                new CCBdata(45, "Kho Làng Khánh"),
                new CCBdata(236, "Kho Xưởng Công Ty"));
        cbType.getSelectionModel().selectFirst();
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
        loadData();

    }

    @FXML
    private void onExportExcel() {
        System.out.println("Export Excel...");
        // TODO: export tbvReconciliation hoặc tab hiện tại
        Stage stage = (Stage) tabPane.getScene().getWindow();
        boolean result = functionHelper.exportExcel(tbvReconciliation, stage, "Đối soát");

        if (result) {
            customDialogNotification.showDialog("Thành công", "Xuất Excel thành công", Alert.AlertType.INFORMATION);
        } else {
            customDialogNotification.showDialog("Lỗi", "Xuất Excel thất bại", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onCancel() {
        System.out.println("Close dialog...");
        // TODO: đóng window
    }

    private void setupRowColor(TableView<ObservableList<String>> tableView) {

        tableView.setRowFactory(tv -> new TableRow<>() {

            @Override
            protected void updateItem(ObservableList<String> item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                    return;
                }

                try {

                    double qtyWarehouse = Double.parseDouble(item.get(4));
                    double qtyBusiness = Double.parseDouble(item.get(5));

                    double sum = qtyWarehouse - qtyBusiness;

                    // Row đang selected
                    if (isSelected()) {
                        setStyle("-fx-background-color: blue;");
                        return;
                    }

                    // Khớp dữ liệu
                    if (sum == 0) {

                        setStyle("-fx-background-color: #ffffff;");

                    } else if (sum != 0) {

                        // Lệch dữ liệu
                        setStyle("-fx-background-color: #ff0000;");

                    }else if(qtyWarehouse==0 &&qtyBusiness!=0){
                        setStyle("-fx-background-color: #5656ee;");

                    }
                    else if(qtyWarehouse!=0 &&qtyBusiness==0){
                        setStyle("-fx-background-color: #CCCC00;");

                    }

                } catch (Exception e) {

                    e.printStackTrace();
                    setStyle("");

                }
            }
        });
    }

}