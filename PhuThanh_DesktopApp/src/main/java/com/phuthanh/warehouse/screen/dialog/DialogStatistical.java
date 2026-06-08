package com.phuthanh.warehouse.screen.dialog;

import java.io.IOException;
import java.time.LocalDate;

// import com.phuthanh.Main;
import com.phuthanh.helper.DbTableHelper;
// import com.phuthanh.helper.FunctionHelper;
// import com.phuthanh.helper.TabViewHelper;
// import com.phuthanh.manager.TableViewManager;
import com.phuthanh.model.warehouse.DrawerItem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
// import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogStatistical {

    // ===== FILTER CHUNG =====
    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private TextField txtSearch;
    @FXML
    private Label txtLabelSearch;

    // ===== TAB PANE =====
    @FXML
    private TabPane tabPane;

    // ===== TABLES =====
    @FXML
    private TableView<ObservableList<String>> tableViewEmployeeExport, tableViewEmployeeExportDetails;
    @FXML
    private TableView<ObservableList<String>> tableViewEmployeeImport, tableViewEmployeeImportDetails;
    @FXML
    private TableView<ObservableList<String>> tableViewProductExport, tableViewProductImport;
    @FXML
    private TableView<ObservableList<String>> tableViewProductExportDetails, tableViewProductImportDetails;
    @FXML
    private TableView<ObservableList<String>> tableViewPartnerExport, tableViewPartnerImport;
    @FXML
    private TableView<ObservableList<String>> tableViewPartnerExportDetails, tableViewPartnerImportDetails;
    @FXML
    private TableView<ObservableList<String>> tableViewEmployeeWareHouseExport, tableViewEmployeeWareHouseExportDetails;
    @FXML
    private TableView<ObservableList<String>> tableViewEmployeeWareHouseImport, tableViewEmployeeWareHouseImportDetails;

    @FXML
    private TableView<ObservableList<String>> tableView2;

    private final DbTableHelper dbTableHelper = new DbTableHelper();
    // private final FunctionHelper functionHelper = new FunctionHelper();
    // private final TableViewManager tableViewManager = new TableViewManager();
    // private final TabViewHelper tabViewHelper = new TabViewHelper();

    private ObservableList<ObservableList<String>> allDataEmployeeExport;
    private ObservableList<ObservableList<String>> allDataEmployeeExportDetails;

    private ObservableList<ObservableList<String>> allDataEmployeeImport;
    private ObservableList<ObservableList<String>> allDataEmployeeImportDetails;

    private ObservableList<ObservableList<String>> allDataProductExport;
    private ObservableList<ObservableList<String>> allDataProductExportDetails;

    private ObservableList<ObservableList<String>> allDataProductImport;
    private ObservableList<ObservableList<String>> allDataProductImportDetails;

    private ObservableList<ObservableList<String>> allDataPartnerExport;
    private ObservableList<ObservableList<String>> allDataPartnerExportDetails;

    private ObservableList<ObservableList<String>> allDataPartnerImport;
    private ObservableList<ObservableList<String>> allDataPartnerImportDetails;

    private ObservableList<ObservableList<String>> allDataEmployeeWareHouseExport;
    private ObservableList<ObservableList<String>> allDataEmployeeWareHouseExportDetails;

    private ObservableList<ObservableList<String>> allDataEmployeeWareHouseImport;
    private ObservableList<ObservableList<String>> allDataEmployeeWareHouseImportDetails;

    private DrawerItem selectedDrawerItem;
    private String wareHouseID;

    @FXML
    public void initialize() {
        // set giá trị mặc định nếu cần
        // fromDatePicker.setValue(LocalDate.now().minusDays(7));
        // toDatePicker.setValue(LocalDate.now());
        txtSearch.setVisible(false);
        txtSearch.setManaged(false);
        txtLabelSearch.setVisible(false);
        txtLabelSearch.setManaged(false);
        setCurrentMonth(fromDatePicker, toDatePicker);

        // selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem",
        // DrawerItem.class);

        // lắng nghe filter chung
        fromDatePicker.valueProperty().addListener((obs, o, n) -> reloadCurrentTab());
        toDatePicker.valueProperty().addListener((obs, o, n) -> reloadCurrentTab());
        txtSearch.textProperty().addListener((obs, o, n) -> reloadCurrentTab());
        // setWareHouseID();
        tabPane.getSelectionModel().selectFirst(); // 👈 QUAN TRỌNG
        // đổi tab thì reload
        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, o, n) -> reloadCurrentTab());
        // setupTablieView();
        init();
    }

    // ===== LOAD THEO TAB =====
    private void reloadCurrentTab() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null)
            return;

        int index = tabPane.getSelectionModel().getSelectedIndex();

        if (index == 0) {
            loadTopEmployeeExport(); // Top nhân viên
        } else if (index == 1) {
            loadTopEmployeeImport();
        } else if (index == 2) {
            loadTopProductExport();
        } else if (index == 3) {
            loadTopProductImport();
        } else if (index == 4) {
            loadTopPartnerExport();
        } else if (index == 5) {
            loadTopPartnerImport();
        } else if (index == 6) {
            loadTopEmployeeWareHouseExport();
        } else if (index == 7) {
            loadTopEmployeeWareHouseImport();
        }
    }

    private void loadTopEmployeeExport() {
        ObservableList<ObservableList<String>> allDataEx = dbTableHelper.loadDataTable(
                tableViewEmployeeExport,
                "SELECT f.ID_Employee, e.NameEmployee, f.Qty, f.TotalCount FROM dbo.fnDataWareHouseHistoryEmployee('"
                        + fromDatePicker.getValue().toString() + "', '" + toDatePicker.getValue().toString() + "', "
                        + wareHouseID
                        + ", 'EXPORT') AS f LEFT JOIN dbo.Employee e ON f.ID_Employee = e.EmployeeID ORDER BY f.Qty DESC");

        // System.out.println(allDataEx);
        allDataEmployeeExport = allDataEx;
        tableViewEmployeeExport.setItems(allDataEmployeeExport);
    }

    private void loadTopEmployeeImport() {
        ObservableList<ObservableList<String>> allDataIm = dbTableHelper.loadDataTable(
                tableViewEmployeeImport,
                "SELECT f.ID_Employee, e.NameEmployee, f.Qty, f.TotalCount FROM dbo.fnDataWareHouseHistoryEmployee('"
                        + fromDatePicker.getValue().toString() + "', '" + toDatePicker.getValue().toString() + "', "
                        + wareHouseID
                        + ", 'IMPORT') AS f LEFT JOIN dbo.Employee e ON f.ID_Employee = e.EmployeeID ORDER BY f.Qty DESC");
        // System.out.println(allDataIm);
        allDataEmployeeImport = allDataIm;
        tableViewEmployeeImport.setItems(allDataEmployeeImport);
    }

    private void loadTopProductExport() {
        ObservableList<ObservableList<String>> allDataEx = dbTableHelper.loadDataTable(
                tableViewProductExport,
                "SELECT f.ProductAID, p.ProductID, f.Qty, f.TotalCount FROM dbo.fnDataWareHouseHistoryProduct('"
                        + fromDatePicker.getValue().toString() + "', '" + toDatePicker.getValue().toString() + "', "
                        + wareHouseID
                        + ", 'EXPORT') AS f LEFT OUTER JOIN dbo.Product AS p ON f.ProductAID = p.ProductAID ORDER BY f.Qty DESC");
        // System.out.println(allDataIm);
        allDataProductExport = allDataEx;
        tableViewProductExport.setItems(allDataProductExport);
    }

    private void loadTopProductImport() {
        ObservableList<ObservableList<String>> allDataIm = dbTableHelper.loadDataTable(
                tableViewProductImport,
                "SELECT f.ProductAID, p.ProductID, f.Qty, f.TotalCount FROM dbo.fnDataWareHouseHistoryProduct('"
                        + fromDatePicker.getValue().toString() + "', '" + toDatePicker.getValue().toString() + "', "
                        + wareHouseID
                        + ", 'IMPORT') AS f LEFT OUTER JOIN dbo.Product AS p ON f.ProductAID = p.ProductAID ORDER BY f.Qty DESC");
        // System.out.println(allDataIm);
        allDataProductImport = allDataIm;
        tableViewProductImport.setItems(allDataProductImport);
    }

    private void loadTopPartnerExport() {
        ObservableList<ObservableList<String>> allDataEx = dbTableHelper.loadStatisticalPartner(
                tableViewPartnerExport,
                fromDatePicker.getValue().toString(), toDatePicker.getValue().toString(),
                wareHouseID,
                "EXPORT");
        // System.out.println(allDataEx);
        allDataPartnerExport = allDataEx;
        tableViewPartnerExport.setItems(allDataPartnerExport);
    }

    private void loadTopPartnerImport() {
        ObservableList<ObservableList<String>> allDataIm = dbTableHelper.loadStatisticalPartner(
                tableViewPartnerImport,
                fromDatePicker.getValue().toString(), toDatePicker.getValue().toString(),
                wareHouseID,
                "IMPORT");
        // System.out.println(allDataIm);
        allDataPartnerImport = allDataIm;
        tableViewPartnerImport.setItems(allDataPartnerImport);
    }

    private void loadTopEmployeeWareHouseExport() {
        ObservableList<ObservableList<String>> allDataEx = dbTableHelper.loadStatisticalEmpolyeeWareHouse(
                tableViewEmployeeWareHouseExport,
                fromDatePicker.getValue().toString(), toDatePicker.getValue().toString(),
                wareHouseID,
                "EXPORT");
        // System.out.println(allDataEx);
        allDataEmployeeWareHouseExport = allDataEx;
        tableViewEmployeeWareHouseExport.setItems(allDataEmployeeWareHouseExport);
    }

    private void loadTopEmployeeWareHouseImport() {
        ObservableList<ObservableList<String>> allDataEx = dbTableHelper.loadStatisticalEmpolyeeWareHouse(
                tableViewEmployeeWareHouseImport,
                fromDatePicker.getValue().toString(), toDatePicker.getValue().toString(),
                wareHouseID,
                "IMPORT");
        // System.out.println(allDataEx);
        allDataEmployeeWareHouseImport = allDataEx;
        tableViewEmployeeWareHouseImport.setItems(allDataEmployeeWareHouseImport);
    }

    private void loadTopEmployeeExportDetails(String coeAID) {
        ObservableList<ObservableList<String>> allDataIm = dbTableHelper.loadStatisticalEmployeeDetails(
                tableViewEmployeeExportDetails,
                fromDatePicker.getValue().toString(), toDatePicker.getValue().toString(), coeAID, "EXPORT",
                wareHouseID);
        allDataEmployeeExportDetails = allDataIm;

        tableViewEmployeeExportDetails.setItems(allDataEmployeeExportDetails);
    }

    private void loadTopEmployeeImportDetails(String coeAID) {
        ObservableList<ObservableList<String>> allDataIm = dbTableHelper.loadStatisticalEmployeeDetails(
                tableViewEmployeeImportDetails,
                fromDatePicker.getValue().toString(), toDatePicker.getValue().toString(), coeAID, "IMPORT",
                wareHouseID);
        allDataEmployeeImportDetails = allDataIm;
        tableViewEmployeeImportDetails.setItems(allDataEmployeeImportDetails);
    }

    private void loadTopProductExportDetails(String coeAID) {
        ObservableList<ObservableList<String>> allDataIm = dbTableHelper.loadStatisticalProductDetails(
                tableViewProductExportDetails,
                fromDatePicker.getValue().toString(), toDatePicker.getValue().toString(), coeAID, "EXPORT",
                wareHouseID);
        allDataProductExportDetails = allDataIm;

        tableViewProductExportDetails.setItems(allDataProductExportDetails);
    }

    private void loadTopProductImportDetails(String coeAID) {
        ObservableList<ObservableList<String>> allDataIm = dbTableHelper.loadStatisticalProductDetails(
                tableViewProductImportDetails,
                fromDatePicker.getValue().toString(), toDatePicker.getValue().toString(), coeAID, "IMPORT",
                wareHouseID);
        allDataProductImportDetails = allDataIm;
        tableViewProductImportDetails.setItems(allDataProductImportDetails);
    }

    private void loadTopPartnerExportDetails(String coeAID) {
        ObservableList<ObservableList<String>> allDataIm = dbTableHelper.loadStatisticalPartnerDetails(
                tableViewPartnerExportDetails,
                fromDatePicker.getValue().toString(), toDatePicker.getValue().toString(), coeAID, "EXPORT",
                wareHouseID);
        allDataPartnerExportDetails = allDataIm;
        tableViewPartnerExportDetails.setItems(allDataPartnerExportDetails);
    }

    private void loadTopPartnerImportDetails(String coeAID) {
        ObservableList<ObservableList<String>> allDataIm = dbTableHelper.loadStatisticalPartnerDetails(
                tableViewPartnerImportDetails,
                fromDatePicker.getValue().toString(), toDatePicker.getValue().toString(), coeAID, "IMPORT",
                wareHouseID);
        allDataPartnerImportDetails = allDataIm;
        tableViewPartnerImportDetails.setItems(allDataPartnerImportDetails);
    }

    private void loadTopEmployeeWareHouseExportDetails(String coeAID) {
        ObservableList<ObservableList<String>> allDataIm = dbTableHelper.loadStatisticalEmployeeWareHouseDetails(
                tableViewEmployeeWareHouseExportDetails,
                fromDatePicker.getValue().toString(), toDatePicker.getValue().toString(), coeAID, "EXPORT",
                wareHouseID);
        allDataEmployeeWareHouseExportDetails = allDataIm;
        tableViewEmployeeWareHouseExportDetails.setItems(allDataEmployeeWareHouseExportDetails);
    }

    private void loadTopEmployeeWareHouseImportDetails(String coeAID) {
        ObservableList<ObservableList<String>> allDataIm = dbTableHelper.loadStatisticalEmployeeWareHouseDetails(
                tableViewEmployeeWareHouseImportDetails,
                fromDatePicker.getValue().toString(), toDatePicker.getValue().toString(), coeAID, "IMPORT",
                wareHouseID);
        allDataEmployeeWareHouseImportDetails = allDataIm;
        tableViewEmployeeWareHouseImportDetails.setItems(allDataEmployeeWareHouseImportDetails);
    }

    private void setCurrentMonth(DatePicker fromDate, DatePicker toDate) {
        LocalDate now = LocalDate.now();

        fromDate.setValue(now.withDayOfMonth(1));
        toDate.setValue(now.withDayOfMonth(now.lengthOfMonth()));
    }

    // private void setupTablieView() {
    // tableViewManager.setupTableView(tableViewEmployeeExport, );
    // tableViewManager.setupTableView(tableViewEmployeeExportDetails);

    // tableViewManager.setupTableView(tableViewEmployeeImport);
    // tableViewManager.setupTableView(tableViewEmployeeImportDetails);

    // tableViewManager.setupTableView(tableViewProductExport);
    // tableViewManager.setupTableView(tableViewProductExportDetails);

    // tableViewManager.setupTableView(tableViewProductImport);
    // tableViewManager.setupTableView(tableViewProductImportDetails);

    // tableViewManager.setupTableView(tableViewPartnerExport);
    // tableViewManager.setupTableView(tableViewPartnerExportDetails);

    // tableViewManager.setupTableView(tableViewPartnerImport);
    // tableViewManager.setupTableView(tableViewPartnerImportDetails);

    // tableViewManager.setupTableView(tableViewEmployeeWareHouseExport);
    // tableViewManager.setupTableView(tableViewEmployeeWareHouseExportDetails);

    // tableViewManager.setupTableView(tableViewEmployeeWareHouseImport);
    // tableViewManager.setupTableView(tableViewEmployeeWareHouseImportDetails);
    // // ==========================================
    // tabViewHelper.clickItemSaveAID(tableViewEmployeeExport);
    // tabViewHelper.clickItemSaveAID(tableViewEmployeeExportDetails);

    // tabViewHelper.clickItemSaveAID(tableViewEmployeeImport);
    // tabViewHelper.clickItemSaveAID(tableViewEmployeeImportDetails);

    // tabViewHelper.clickItemSaveAID(tableViewProductExport);
    // tabViewHelper.clickItemSaveAID(tableViewProductExportDetails);

    // tabViewHelper.clickItemSaveAID(tableViewProductImport);
    // tabViewHelper.clickItemSaveAID(tableViewProductImportDetails);

    // tabViewHelper.clickItemSaveAID(tableViewPartnerExport);
    // tabViewHelper.clickItemSaveAID(tableViewPartnerExportDetails);

    // tabViewHelper.clickItemSaveAID(tableViewPartnerImport);
    // tabViewHelper.clickItemSaveAID(tableViewPartnerImportDetails);

    // tabViewHelper.clickItemSaveAID(tableViewEmployeeWareHouseExport);
    // tabViewHelper.clickItemSaveAID(tableViewEmployeeWareHouseExportDetails);

    // tabViewHelper.clickItemSaveAID(tableViewEmployeeWareHouseImport);
    // tabViewHelper.clickItemSaveAID(tableViewEmployeeWareHouseImportDetails);
    // }

    private void init() {
        tableViewEmployeeExport.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {

                    if (newItem == null)
                        return;

                    String aid = newItem.get(0); // cột ID_Employee
                    if (aid == null || aid.isBlank())
                        return;

                    System.out.println("AID từ row: " + aid);
                    loadTopEmployeeExportDetails(aid);
                });
        tableViewEmployeeImport.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {

                    if (newItem == null)
                        return;

                    String aid = newItem.get(0); // cột ID_Employee
                    if (aid == null || aid.isBlank())
                        return;

                    System.out.println("AID từ row: " + aid);
                    loadTopEmployeeImportDetails(aid);
                });
        tableViewProductExport.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {

                    if (newItem == null)
                        return;

                    String aid = newItem.get(0); // cột ID_Employee
                    if (aid == null || aid.isBlank())
                        return;

                    System.out.println("AID từ row: " + aid);
                    loadTopProductExportDetails(aid);
                });
        tableViewPartnerExport.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {

                    if (newItem == null)
                        return;

                    String aid = newItem.get(0); // cột ID_Employee
                    if (aid == null || aid.isBlank())
                        return;

                    System.out.println("AID từ row: " + aid);
                    loadTopPartnerExportDetails(aid);
                });
        tableViewProductImport.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {

                    if (newItem == null)
                        return;

                    String aid = newItem.get(0); // cột ID_Employee
                    if (aid == null || aid.isBlank())
                        return;

                    System.out.println("AID từ row: " + aid);
                    loadTopProductImportDetails(aid);
                });
        tableViewPartnerImport.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {

                    if (newItem == null)
                        return;

                    String aid = newItem.get(0); // cột ID_Employee
                    if (aid == null || aid.isBlank())
                        return;

                    System.out.println("AID từ row: " + aid);
                    loadTopPartnerImportDetails(aid);
                });
        tableViewEmployeeWareHouseExport.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {

                    if (newItem == null)
                        return;

                    String aid = newItem.get(0); // cột ID_Employee
                    if (aid == null || aid.isBlank())
                        return;

                    System.out.println("AID từ row: " + aid);
                    loadTopEmployeeWareHouseExportDetails(aid);
                });
        tableViewEmployeeWareHouseImport.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {

                    if (newItem == null)
                        return;

                    String aid = newItem.get(0); // cột ID_Employee
                    if (aid == null || aid.isBlank())
                        return;

                    System.out.println("AID từ row: " + aid);
                    loadTopEmployeeWareHouseImportDetails(aid);
                });
    }

    private ObservableList<ObservableList<String>> getFirstItems(
            ObservableList<ObservableList<String>> source, int limit) {

        if (source == null)
            return null;

        ObservableList<ObservableList<String>> result = FXCollections.observableArrayList();

        int _limit = Math.min(limit, source.size());

        for (int i = 0; i < _limit; i++) {
            result.add(source.get(i));
        }

        return result;
    }

    private ObservableList<ObservableList<String>> getCurrentTabData() {

        int index = tabPane.getSelectionModel().getSelectedIndex();

        switch (index) {
            case 0:
                return allDataEmployeeExport;
            case 1:
                return allDataEmployeeImport;
            case 2:
                return getFirstItems(allDataProductExport, 20);
            case 3:
                return getFirstItems(allDataProductImport, 20);
            case 4:
                return allDataPartnerExport;
            case 5:
                return allDataPartnerImport;
            case 6:
                return allDataEmployeeWareHouseExport;
            case 7:
                return allDataEmployeeWareHouseImport;
            default:
                return null;
        }
    }

    @FXML
    private void onOpenChart() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogChart.fxml"));

            Parent root = loader.load();
            DialogChart chartController = loader.getController();
            chartController.setData(getCurrentTabData());
            Stage dialog = new Stage();
            dialog.setTitle("Thống kê biểu đồ");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(true);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setWareHouseID() {
        if (selectedDrawerItem.getWareHouseCategory() > 0) {
            this.wareHouseID = selectedDrawerItem.getWareHouseID();
        } else {
            this.wareHouseID = null;
        }
    }

    public void setInit(DrawerItem item) {
        this.selectedDrawerItem = item;
        setWareHouseID();
        reloadCurrentTab();
    }

    public void cleanup() {
        if (allDataEmployeeExport != null) allDataEmployeeExport.clear();
        if (allDataEmployeeExportDetails != null) allDataEmployeeExportDetails.clear();
        if (allDataEmployeeImport != null) allDataEmployeeImport.clear();
        if (allDataEmployeeImportDetails != null) allDataEmployeeImportDetails.clear();
        if (allDataProductExport != null) allDataProductExport.clear();
        if (allDataProductExportDetails != null) allDataProductExportDetails.clear();
        if (allDataProductImport != null) allDataProductImport.clear();
        if (allDataProductImportDetails != null) allDataProductImportDetails.clear();
        if (allDataPartnerExport != null) allDataPartnerExport.clear();
        if (allDataPartnerExportDetails != null) allDataPartnerExportDetails.clear();
        if (allDataPartnerImport != null) allDataPartnerImport.clear();
        if (allDataPartnerImportDetails != null) allDataPartnerImportDetails.clear();
        if (allDataEmployeeWareHouseExport != null) allDataEmployeeWareHouseExport.clear();
        if (allDataEmployeeWareHouseExportDetails != null) allDataEmployeeWareHouseExportDetails.clear();
        if (allDataEmployeeWareHouseImport != null) allDataEmployeeWareHouseImport.clear();
        if (allDataEmployeeWareHouseImportDetails != null) allDataEmployeeWareHouseImportDetails.clear();
    }
}
