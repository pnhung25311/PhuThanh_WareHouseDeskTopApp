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
import com.phuthanh.model.info.Account;
import com.phuthanh.model.warehouse.OptionAction;
import com.phuthanh.store.AppState;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;

public class DialogImportExcelCart {

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

    private static final FunctionHelper functionHelper = new FunctionHelper();

    // =============================
    // INIT
    // =============================
    public void initialize() {
        // loadComboBox();
        ProgressBarUpload.setProgress(0);
        // ProgressBarUpload.setVisible(false);
        setupActions();
        loadComboBox();

    }

    private void loadComboBox() {

        OptionAction delete = new OptionAction("DELETE", "Xóa");
        OptionAction export = new OptionAction("EXPORT", "Xuất hàng");
        OptionAction transfer = new OptionAction("TRANSFER", "Điều chuyển");
        OptionAction import_ = new OptionAction("IMPORT", "Nhập hàng");
        OptionAction confirm_ = new OptionAction("CONFIRM", "Xác nhận");

        Account user = AppState.getInstance().get("Account", Account.class);
        if (user.getRole().equals("ADMIN")) {
            cbbChoseAction.getItems().addAll(import_, export, transfer, delete, confirm_);
        }
        if (user.getRole().equals("BUSINESS")) {
            cbbChoseAction.getItems().addAll(import_, export, transfer, delete);
        }
        if (user.getRole().equals("WAREHOUSE")) {
            cbbChoseAction.getItems().addAll(delete, confirm_);
        }
    }

    public void initData(Runnable callback) {
        this.callBack = callback;
    }

    private void setupActions() {
        btnUpload.setOnAction(e -> onUpload());
        btnOpenExcel.setOnAction(e -> onOpenExcel());
        btnOpenFolder.setOnAction(e -> onOpenFolder());
        btnDeleteExcel.setOnAction(e -> onDeleteExcel());
    }

    // =============================
    // UPLOAD
    // =============================
    private void onUpload() {

        OptionAction action = cbbChoseAction.getSelectionModel().getSelectedItem();
        if (action == null)
            return;

        Task<Void> task = buildTask(action.getId());

        bindProgress(task);
        new Thread(task).start();
    }

    // =============================
    // TASK FACTORY
    // =============================
    private Task<Void> buildTask(String action) {
        btnUpload.setDisable(true);

        return new Task<>() {
            @Override
            protected Void call() throws Exception {

                updateProgress(0, 100);

                switch (action) {

                    case "UPDATE":
                        break;
                    case "CONFIRM":
                        functionHelper.confirmExcelCart(getExcelPath(),
                                (current, total) -> updateProgress(current, total), 4);
                        break;
                    case "DELETE":
                        functionHelper.deleteExcelCart(getExcelPath(),
                                (current, total) -> updateProgress(current, total), 3);
                        break;
                    case "EXPORT":
                        functionHelper.importExcelCart(getExcelPath(),
                                (current, total) -> updateProgress(current, total), "EXPORT", 1);
                        break;
                    case "IMPORT":
                        System.out.println("IMPORT");
                        functionHelper.importExcelCart(getExcelPath(),
                                (current, total) -> updateProgress(current, total), "IMPORT", 0);
                        break;
                    case "IMPORT_DYNAMIC":
                        break;
                    case "EXPORT_DYNAMIC":
                        break;
                    case "TRANSFER":
                        System.out.println("TRANSFER");
                        functionHelper.importExcelCart(getExcelPath(),
                                (current, total) -> updateProgress(current, total), "TRANSFER", 2);
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
        return "C:\\PY\\fileExcel\\importExcelCart.xlsx";
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
