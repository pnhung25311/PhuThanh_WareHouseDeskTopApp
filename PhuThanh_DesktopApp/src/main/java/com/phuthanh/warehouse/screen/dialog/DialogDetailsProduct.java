package com.phuthanh.warehouse.screen.dialog;

import java.io.IOException;
import java.util.Arrays;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.TabViewHelper;
import com.phuthanh.manager.TableViewManager;
import com.phuthanh.warehouse.contextmenu.TabContextMenuDetailsProduct;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import javafx.stage.Stage;

public class DialogDetailsProduct {
    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnReload;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tabAllRowHistory;

    @FXML
    private Tab tabRowHistory;

    @FXML
    private TableView<ObservableList<String>> tabAllRowHistoryTable;

    // Khởi tạo với danh sách rỗng để tránh NullPointerException
    private ObservableList<ObservableList<String>> baseAllData = FXCollections.observableArrayList();

    private   final DbTableHelper dbTableHelper = new DbTableHelper();
    private   final TabViewHelper tabViewHelper = new TabViewHelper();
    private   final TableViewManager tableViewManager = new TableViewManager();
    private   final TabContextMenuDetailsProduct tabContextMenuDetailsProduct = new TabContextMenuDetailsProduct();
    private   final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    private   final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();

    // ============================
    // SỰ KIỆN
    // ============================
    @FXML
    private void onReload() {
        loadProductTable("1900-01-01", "2100-12-31");
        applySearchFilter(txtSearch.getText());
    }

    // ============================
    // KHỞI TẠO
    // ============================
    @FXML
    public void initialize() {

        System.out.println("DialogDetailsProduct initialized");

        baseAllData = FXCollections.observableArrayList();

        loadProductTable("1900-01-01", "2100-12-31");

        setupSearch();

        tabViewHelper.clickItemSaveAID(tabAllRowHistoryTable);

        tabContextMenuDetailsProduct.attachDefaultContextMenu(
                tabAllRowHistoryTable,
                () -> tabViewHelper.getSelectedAID(),
                () -> loadProductTable("1900-01-01", "2100-12-31"));
        tableViewManager.setupTableView(tabAllRowHistoryTable, baseAllData);
    }

    public void loadProductTable(String fromDate, String toDate) {

        ObservableList<ObservableList<String>> loadedAllData = dbTableHelper.loadDataTable(
                tabAllRowHistoryTable,
                "SELECT * FROM vwDetailsProduct ORDER BY LastTime DESC");

        baseAllData = (loadedAllData != null)
                ? loadedAllData
                : FXCollections.observableArrayList();

        tabAllRowHistoryTable.setItems(baseAllData);
    }

    private FilteredList<ObservableList<String>> filteredList;

    private void setupSearch() {

        filteredList = new FilteredList<>(baseAllData, p -> true);

        SortedList<ObservableList<String>> sorted = new SortedList<>(filteredList);
        sorted.comparatorProperty().bind(tabAllRowHistoryTable.comparatorProperty());

        tabAllRowHistoryTable.setItems(sorted);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            applySearchFilter(newVal);
        });
    }

    private void applySearchFilter(String keyword) {

        String kw = (keyword == null) ? "" : keyword.trim().toLowerCase();

        filteredList.setPredicate(row -> {

            if (kw.isEmpty())
                return true;

            for (String cell : row) {
                if (cell != null && cell.toLowerCase().contains(kw)) {
                    return true;
                }
            }
            return false;
        });
    }

    @FXML
    private void onCreate() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCreateDetailsProduct.fxml"));
            Parent root = loader.load();
            DialogCreateDetailsProduct controller = loader.getController();
            controller.setOnCreateSuccess(() -> loadProductTable("1900-01-01", "2100-12-31"));
            Stage dialog = new Stage();
            dialog.setTitle("Thêm mới chi tiết sản phẩm");
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ImportExcel() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogImportExcelDetailsProduct.fxml"));
            Parent root = loader.load();
            DialogImportExcelDetailsProduct controller = loader.getController();
            controller.initData(() -> loadProductTable("1900-01-01", "2100-12-31"));

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
    private void onUpdate() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCreateDetailsProduct.fxml"));
            Parent root = loader.load();
            DialogCreateDetailsProduct controller = loader.getController();
            controller.setOnCreateSuccess(() -> loadProductTable("1900-01-01", "2100-12-31"));

            controller.setCodeAID(tabViewHelper.getSelectedAID());
            controller.setEditMode(true);

            Stage dialog = new Stage();
            dialog.setTitle("Sửa chi tiết sản phẩm");
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDelete() {
        try {
            String selectedAID = tabViewHelper.getSelectedAID();
            if (selectedAID == null || selectedAID.isEmpty()) {
                customDialogNotification.showDialog("Thông báo", "Vui lòng chọn dòng cần xóa",
                        Alert.AlertType.WARNING);
                return;
            }

            int row = dbCRUDHelper.delete("DetailsProduct", Arrays.asList("PartNoAID"),
                    Arrays.asList(selectedAID));
            if (row > 0) {
                customDialogNotification.showDialog("Thành công", "Xóa chi tiết sản phẩm thành công",
                        Alert.AlertType.INFORMATION);
                loadProductTable("1900-01-01", "2100-12-31");
            } else {
                System.out.println("Xóa thất bại!");
                customDialogNotification.showDialog("Lỗi", "Xóa chi tiết sản phẩm thất bại",
                        Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi", "Có lỗi xảy ra: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }
}