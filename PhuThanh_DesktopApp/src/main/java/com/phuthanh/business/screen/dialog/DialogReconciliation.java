package com.phuthanh.business.screen.dialog;

import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
// import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.helper.TabViewHelper;
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
    private final TabViewHelper tabViewHelper = new TabViewHelper();
    private final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();

    // =====================
    // INIT
    // =====================
    @FXML
    public void initialize() {
        System.out.println("DialogReconciliation initialized");
        dpTime.setValue(LocalDate.now());
        tabViewHelper.clickItemSaveAID(tbvReconciliation);

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
            // TableRow<ObservableList<String>> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();

            MenuItem confirm = new MenuItem("Xác nhận đối soát");
            {
                confirm.setOnAction(e -> {
                    try {
                        ObservableList<String> item = getItem();

                        if (item == null) {
                            return;
                        }

                        String detailsWarehouse = item.get(3);
                        String detailsBusiness = item.get(4);
                        double sum = Double.parseDouble(item.get(7));

                        String str = item.get(0);
                        String[] arr = str.split(",");
                        System.out.println("arr = " + String.join(" | ", arr));
                        boolean warehouseEmpty = detailsWarehouse == null || detailsWarehouse.isBlank();

                        boolean businessEmpty = detailsBusiness == null || detailsBusiness.isBlank();

                        if (sum != 0 && businessEmpty && !warehouseEmpty) {
                            customDialogNotification.showDialog("Thông báo", "Kinh doanh chưa cập nhật",
                                    Alert.AlertType.WARNING);
                        } else if (sum != 0 && !businessEmpty && warehouseEmpty) {
                            customDialogNotification.showDialog("Thông báo", "Kho chưa cập nhật",
                                    Alert.AlertType.WARNING);

                        } else if (sum != 0 && !businessEmpty && !warehouseEmpty) {
                            customDialogNotification.showDialog("Thông báo", "Số liệu không khớp",
                                    Alert.AlertType.WARNING);

                        } else {
                            List<String> columns = List.of("Status");
                            List<List<Object>> rows = new ArrayList<>();
                            List<List<Object>> whereValuesList = new ArrayList<>();

                            for (String c : arr) {
                                rows.add(List.of(1));
                                whereValuesList.add(List.of(c.trim()));
                            }
                            int[] rowsAffected = dbCRUDHelper.updateBatch("Cart", columns, rows, List.of("CartAID"),
                                    whereValuesList);
                            boolean ok = Arrays.stream(rowsAffected)
                                    .noneMatch(r -> r == Statement.EXECUTE_FAILED);
                            if (ok) {
                                customDialogNotification.showDialog("Thành công", "Lưu thành công",
                                        Alert.AlertType.INFORMATION);

                            } else {
                                customDialogNotification.showDialog("Có dòng lưu thất bại", "Lưu thất bại",
                                        Alert.AlertType.ERROR);
                            }
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                this.setOnMousePressed(event -> {
                    if (event.isSecondaryButtonDown() && !this.isEmpty()) {
                        tableView.getSelectionModel().select(this.getIndex());
                    }
                });

                this.setOnContextMenuRequested(event -> {
                    menu.getItems().add(confirm);
                    if (!this.isEmpty()) {
                        menu.show(this, event.getScreenX(), event.getScreenY());
                    }
                });

                this.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    // row.pseudoClassStateChanged(PC_HIGHLIGHT, newVal);
                });

                // return row;
            }

            @Override
            protected void updateItem(ObservableList<String> item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                    return;
                }

                try {

                    String detailsWarehouse = item.get(3);
                    String detailsBusiness = item.get(4);
                    double sum = Double.parseDouble(item.get(7));

                    // System.out.println("detailsWarehouse = [" + detailsWarehouse + "]");
                    // System.out.println("detailsBusiness = [" + detailsBusiness + "]");
                    // System.out.println("sum = " + sum);
                    // System.out.println("warehouse null = " + (detailsWarehouse == null));
                    // System.out.println("warehouse blank = " + detailsWarehouse.isBlank());

                    // Row đang selected
                    if (isSelected()) {
                        setStyle("-fx-background-color: blue;");
                        return;
                    }
                    boolean warehouseEmpty = detailsWarehouse == null || detailsWarehouse.isBlank();

                    boolean businessEmpty = detailsBusiness == null || detailsBusiness.isBlank();

                    if (sum != 0 && businessEmpty && !warehouseEmpty) {
                        setStyle("-fx-background-color: #808080;");
                    } else if (sum != 0 && !businessEmpty && warehouseEmpty) {
                        setStyle("-fx-background-color: #33FFCC;");
                    } else if (sum != 0 && !businessEmpty && !warehouseEmpty) {
                        setStyle("-fx-background-color: #FF0000;");
                    } else {
                        setStyle("-fx-background-color: #ffffff;");
                    }
                    // Khớp dữ liệu
                    // if (sum == 0 && detailsWarehouse != null && detailsBusiness != null) {
                    // setStyle("-fx-background-color: #ffffff;");
                    // } else
                    // if (sum != 0 && detailsBusiness == null && detailsWarehouse != null) {
                    // setStyle("-fx-background-color: #808080;");
                    // } else if (sum != 0 && detailsBusiness != null && detailsWarehouse == null) {
                    // setStyle("-fx-background-color: #33FFCC;");
                    // }else{
                    // setStyle("-fx-background-color: #ffffff;");
                    // }

                } catch (Exception e) {

                    e.printStackTrace();
                    setStyle("");

                }
            }

        });
    }

}