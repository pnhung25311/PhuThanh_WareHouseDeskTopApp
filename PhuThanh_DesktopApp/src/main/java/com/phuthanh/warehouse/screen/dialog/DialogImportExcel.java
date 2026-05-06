package com.phuthanh.warehouse.screen.dialog;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.model.warehouse.OptionAction;
// import com.phuthanh.store.AppState;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;

public class DialogImportExcel {

    @FXML
    private Button btnUpload;
    @FXML
    private Button btnOpenExcel;
    @FXML
    private Button btnOpenFolder;
    @FXML
    private Button btnDeleteExcel;
    @FXML
    private Button btnCheckExcel;
    @FXML
    private ComboBox<OptionAction> cbbChoseAction;
    @FXML
    private ProgressBar ProgressBarUpload;
    private Runnable callBack;

    private String excelFilePath;
    private static final FunctionHelper functionHelper = new FunctionHelper();
    DrawerItem selectedDrawer;

    // =============================
    // INIT
    // =============================
    public void initialize() {
        // loadComboBox();
        ProgressBarUpload.setProgress(0);
        // ProgressBarUpload.setVisible(false);
        setupActions();
    }

    private void loadComboBox() {

        OptionAction create = new OptionAction("CREATE", "Thêm mới");
        OptionAction update = new OptionAction("UPDATE", "Cập nhật");
        OptionAction delete = new OptionAction("DELETE", "Xóa");
        OptionAction export = new OptionAction("EXPORT", "Xuất hàng");
        OptionAction tranfer = new OptionAction("TRANSFER", "Xuất/Nhập điều chuyển");
        // OptionAction export_dynamic = new OptionAction("EXPORT_DYNAMIC", "Xuất thêm
        // kho");
        OptionAction import_ = new OptionAction("IMPORT", "Nhập hàng");
        // OptionAction import_dynamic = new OptionAction("IMPORT_DYNAMIC", "Nhập thêm
        // kho");

        if (selectedDrawer.getWareHouseCategory() > 0) {
            cbbChoseAction.getItems().addAll(create, update, delete, import_, export, tranfer);
        } else {
            cbbChoseAction.getItems().addAll(create, update, delete);
        }
    }

    public void initData(Runnable callback, DrawerItem item) {
        this.callBack = callback;
        this.selectedDrawer = item;
        loadComboBox();
    }

    private void setupActions() {
        btnUpload.setOnAction(e -> onUpload());
        btnOpenExcel.setOnAction(e -> onOpenExcel());
        btnOpenFolder.setOnAction(e -> onOpenFolder());
        btnDeleteExcel.setOnAction(e -> onDeleteExcel());
        btnCheckExcel.setOnAction(e -> onCheck());
    }

    // =============================
    // UPLOAD
    // =============================
    private void onUpload() {

        DrawerItem drawer = selectedDrawer;

        excelFilePath = drawer.getWareHouseCategory() > 0
                ? "C:\\PY\\fileExcel\\importExcelWareHouse.xlsx"
                : "C:\\PY\\fileExcel\\importExcelProduct.xlsx";

        OptionAction action = cbbChoseAction.getSelectionModel().getSelectedItem();
        if (action == null)
            return;

        Task<Void> task = buildTask(action.getId(), drawer);

        bindProgress(task);
        new Thread(task).start();
    }

    private void onCheck() {

        DrawerItem drawer = selectedDrawer;

        excelFilePath = drawer.getWareHouseCategory() > 0
                ? "C:\\PY\\fileExcel\\importExcelWareHouse.xlsx"
                : "C:\\PY\\fileExcel\\importExcelProduct.xlsx";

        OptionAction action = cbbChoseAction.getSelectionModel().getSelectedItem();
        if (action == null)
            return;

        Task<Void> task = buildTaskCheck(action.getId(), drawer);

        bindProgress(task);
        new Thread(task).start();
    }

    // =============================
    // TASK FACTORY
    // =============================
    private Task<Void> buildTask(String action, DrawerItem drawer) {
        btnUpload.setDisable(true);

        return new Task<>() {
            @Override
            protected Void call() throws Exception {

                updateProgress(0, 100);

                switch (action) {

                    case "CREATE":
                        if (drawer.getWareHouseCategory() > 0) {

                            functionHelper.importExcelWareHouse(
                                    drawer.getWareHouseDataBase(),
                                    excelFilePath,
                                    (current, total) -> updateProgress(current, total));
                        } else {
                            functionHelper.importExcelProduct(
                                    excelFilePath,
                                    (current, total) -> updateProgress(current, total));
                        }
                        break;

                    case "UPDATE":
                        if (drawer.getWareHouseCategory() > 0) {
                            functionHelper.importExcelWareHouseUpdate(
                                    drawer.getWareHouseDataBase(), excelFilePath,
                                    (current, total) -> updateProgress(current, total));
                        } else {
                            functionHelper.importUpdateProduct(
                                    excelFilePath,
                                    (current, total) -> updateProgress(current, total));
                        }
                        break;
                    case "DELETE":
                        if (drawer.getWareHouseCategory() > 0) {
                            functionHelper.insertFromWarehouseByProductID(excelFilePath,
                                    selectedDrawer.getWareHouseDataBase(),
                                    (current, total) -> updateProgress(current, total));
                        } else {
                            functionHelper.importDeleteProduct(excelFilePath,
                                    (current, total) -> updateProgress(current, total));
                        }
                        break;

                    case "EXPORT":
                        functionHelper.importExcelHistoryExport(excelFilePath,
                                (current, total) -> updateProgress(current, total));
                        break;
                    case "IMPORT":
                        functionHelper.importExcelHistoryImport(excelFilePath,
                                (current, total) -> updateProgress(current, total));
                        break;
                    case "IMPORT_DYNAMIC":
                        functionHelper.importExcelHistoryImportDynamic(excelFilePath,
                                (current, total) -> updateProgress(current, total));
                        break;
                    case "EXPORT_DYNAMIC":
                        functionHelper.importExcelHistoryExportDynamic(excelFilePath,
                                (current, total) -> updateProgress(current, total));
                        break;
                    case "TRANSFER":
                        functionHelper.importExcelHistoryTransferImport(excelFilePath,
                                (current, total) -> updateProgress(current, total));
                        break;
                }

                updateProgress(1, 1);
                return null;
            }
        };
    }

    private Task<Void> buildTaskCheck(String action, DrawerItem drawer) {
        btnUpload.setDisable(true);

        return new Task<>() {
            @Override
            protected Void call() throws Exception {

                updateProgress(0, 100);

                switch (action) {
                    case "EXPORT":
                        functionHelper.importExcelCheckData(excelFilePath, 3,
                                (current, total) -> updateProgress(current, total));
                        break;
                    case "IMPORT":
                        functionHelper.importExcelCheckData(excelFilePath, 4,
                                (current, total) -> updateProgress(current, total));
                        break;
                }

                updateProgress(1, 1);
                return null;
            }
        };
    }

    // =============================
    // PROGRESS HANDLER
    // =============================
    private void bindProgress(Task<?> task) {

        // ProgressBarUpload.setVisible(true);
        ProgressBarUpload.progressProperty().unbind();
        ProgressBarUpload.progressProperty().bind(task.progressProperty());
        // btnUpload.setDisable(true);

        task.setOnSucceeded(e -> {
            resetProgress();
            if (callBack != null) {
                callBack.run(); // ✅ GỌI CALLBACK Ở ĐÂY
            }

        });

        task.setOnFailed(e -> {
            resetProgress();
            task.getException().printStackTrace();

        });
    }

    private void resetProgress() {
        ProgressBarUpload.progressProperty().unbind();
        ProgressBarUpload.setProgress(0);
        // ProgressBarUpload.setVisible(false);
        btnUpload.setDisable(false);
    }

    // =============================
    // FILE ACTIONS
    // =============================
    private void onOpenExcel() {
        openFile(getExcelPath());
    }

    private void onOpenFolder() {
        File file = new File(getExcelPath());
        if (file.exists())
            openFile(file.getParent());
    }

    private void onDeleteExcel() {
        File file = new File(getExcelPath());
        if (!file.exists())
            return;

        try (FileInputStream fis = new FileInputStream(file);
                XSSFWorkbook wb = new XSSFWorkbook(fis)) {

            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                XSSFSheet sheet = wb.getSheetAt(s);

                for (int i = sheet.getLastRowNum(); i > 0; i--) {
                    Row row = sheet.getRow(i);
                    if (row != null)
                        sheet.removeRow(row);
                }

                // Reset AutoFilter
                if (sheet.getCTWorksheet().isSetAutoFilter()) {
                    sheet.getCTWorksheet().unsetAutoFilter();
                }

                Row header = sheet.getRow(0);
                if (header != null) {
                    sheet.setAutoFilter(new CellRangeAddress(
                            0, 0, 0, header.getLastCellNum() - 1));
                }
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================
    // UTIL
    // =============================
    private String getExcelPath() {
        DrawerItem drawer = selectedDrawer;
        return drawer.getWareHouseCategory() > 0
                ? "C:\\PY\\fileExcel\\importExcelWareHouse.xlsx"
                : "C:\\PY\\fileExcel\\importExcelProduct.xlsx";
    }

    private void openFile(String path) {
        try {
            File file = new File(path);
            if (file.exists())
                Desktop.getDesktop().open(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
