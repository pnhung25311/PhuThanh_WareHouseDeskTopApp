package com.phuthanh.warehouse.screen.dialog;

import java.io.IOException;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.helper.TabViewHelper;
import com.phuthanh.manager.TableViewManager;
import com.phuthanh.warehouse.contextmenu.TabContextMenuGuarantee;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.TableView;

public class DialogGuaranteeWareHouse {
    // =========================
    // SEARCH AREA
    // =========================
    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnReload;

    @FXML
    private Button btnCreate;

    // =========================
    // TAB AREA
    // =========================
    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tabSheetCheck;

    @FXML
    private TableView<ObservableList<String>> tabGuarantee;

    // =========================
    // FOOTER AREA
    // =========================
    @FXML
    private Button btnExportExcel;

    @FXML
    private Button btnCancel;

    private static final TabViewHelper tabViewHelper = new TabViewHelper();
    private static final TableViewManager tableViewManager = new TableViewManager();
    private static final DbTableHelper dbTableHelper = new DbTableHelper();
    private static final FunctionHelper functionHelper = new FunctionHelper();
    private static final TabContextMenuGuarantee tabContextMenuGuarantee = new TabContextMenuGuarantee();
    private static final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    private ObservableList<ObservableList<String>> allDataGuarantee;
    private FilteredList<ObservableList<String>> filteredDataGuarantee;

    // =========================
    // INITIALIZE
    // =========================

    public void initialize() {
        loadDataGuarantee();
        System.out.println("DialogCheckSheetWareHouse initialized");
        tabViewHelper.clickItemSaveAID(tabGuarantee);
        tableViewManager.setupTableView(tabGuarantee, allDataGuarantee);
        tabContextMenuGuarantee.attachDefaultContextMenu(tabGuarantee, () -> tabViewHelper.getSelectedAID(),
                () -> loadDataGuarantee());

        setupSearch();
    }

    private void loadDataGuarantee() {
        ObservableList<ObservableList<String>> allData = dbTableHelper.loadTableGuaranteeConvert(tabGuarantee,
                "vwGuarantee");
        allDataGuarantee = allData;
        // 🔥 TẠO FILTERED LIST
        filteredDataGuarantee = new FilteredList<>(allDataGuarantee, p -> true);
        tabGuarantee.setItems(filteredDataGuarantee);
    }

    // =========================
    // ACTION EVENTS
    // =========================

    @FXML
    private void onReload() {
        System.out.println("Reload clicked");
        // TODO: load lại dữ liệu
        loadDataGuarantee();

    }

    @FXML
    private void onCreate() {
        System.out.println("Create clicked");
        // TODO: mở form thêm mới
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCreateGuarantee.fxml"));
            Parent root = loader.load();
            DialogCreateGuarantee controller = loader.getController();
            controller.setInitialData(() -> loadDataGuarantee(), false, null);
            Stage dialog = new Stage();
            dialog.setTitle("Thêm mới phiếu bảo hành");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onExportExcel() {
        System.out.println("Export Excel clicked");
        // TODO: xử lý xuất excel
        Stage stage = (Stage) tabPane.getScene().getWindow();

        boolean result = functionHelper.exportExcel(tabGuarantee, stage, "sheet1");

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
        System.out.println("Close clicked");

        // đóng cửa sổ
        btnCancel.getScene().getWindow().hide();
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applySearchFilter(newVal));
    }

    private void applySearchFilter(String keyword) {

        if (filteredDataGuarantee == null)
            return;

        String kw = keyword == null ? "" : keyword.toLowerCase().trim();

        filteredDataGuarantee.setPredicate(row -> matchRow(row, kw));
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

}
