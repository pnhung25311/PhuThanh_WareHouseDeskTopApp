package com.phuthanh.warehouse.screen.dialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.phuthanh.helper.FunctionHelper;
// import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.model.warehouse.OptionAction;
// import com.phuthanh.store.AppState;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;

// import javafx.stage.FileChooser;
import java.awt.Desktop;

public class DialogImportExcelDetailsProduct {

    @FXML
    private Button btnUpload;

    @FXML
    private Button btnOpenExcel;

    @FXML
    private Button btnOpenFolder;

    @FXML
    private Button btnDeleteExcel;

    @FXML
    private ComboBox<OptionAction> cbbChoseAction;

    @FXML
    private ProgressBar ProgressBarUpload;
    private String excelFilePath;
    private Runnable callBack;
    private static final FunctionHelper functionHelper = new FunctionHelper();

    public void initialize() {
        // Load dữ liệu cho ComboBox
        loadComboBox();

        // Set giá trị ProgressBar mặc định
        ProgressBarUpload.setProgress(0);

        // Gán các sự kiện cho các nút
        setupActions();
        System.err.println("DialogImportExcelDetailsProduct initialized");
        // excelFilePath =
        // "C:\\project\\PhuThanh_WareHouseDeskTopApp\\PhuThanh_DesktopApp\\src\\main\\java\\com\\phuthanh\\fileExcel\\importExcelProduct.xlsx";
    }

    private void loadComboBox() {
        // cbbChoseAction.getSelectionModel().selectFirst();
        // DrawerItem selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);

        OptionAction add = new OptionAction("CREATE", "Thêm mới");
        OptionAction update = new OptionAction("UPDATE", "Cập nhật");
        OptionAction delete = new OptionAction("DELETE", "Xóa");
        // OptionAction export = new OptionAction("DELETE", "Xuất hàng");
        // OptionAction _import = new OptionAction("DELETE", "Nhập hàng");
        // if (selectedDrawerItem.getWareHouseCategory() > 0) {
            cbbChoseAction.getItems().addAll(add, update, delete);
        // } else {
        //     cbbChoseAction.getItems().addAll(add, update, delete);
        // }

        // cbbChoseAction.getSelectionModel().selectFirst();
    }

    private void setupActions() {
        btnUpload.setOnAction(e -> onUpload());
        btnOpenExcel.setOnAction(e -> onOpenExcel());
        btnOpenFolder.setOnAction(e -> onOpenFolder());
        btnDeleteExcel.setOnAction(e -> onDeleteExcel());
    }

    public void initData(Runnable callback) {
        this.callBack = callback;
    }

    // =============================
    // HANDLING FUNCTIONS
    // =============================
    private void onUpload() {
        System.out.println("Upload Excel...");
        excelFilePath = "C:\\PY\\fileExcel\\importExcelDetailsProduct.xlsx";

        OptionAction selected = cbbChoseAction.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        String actionId = selected.getId();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {

                updateProgress(0, 100);

                switch (actionId) {
                    case "CREATE":
                        System.out.println("Bạn chọn: Thêm mới");
                        functionHelper.importExcelDetailProduct(
                                excelFilePath,
                                (current, total) -> updateProgress(current, total));
                        break;
                    case "UPDATE":
                        System.out.println("Bạn chọn: Cập nhật");
                        functionHelper.updateExcelDetailProduct(excelFilePath,
                                (current, total) -> updateProgress(current, total));
                        break;
                    case "DELETE":
                        System.out.println("Bạn chọn: Xóa");
                        functionHelper.deleteExcelDetailProduct(excelFilePath,
                                (current, total) -> updateProgress(current, total));
                        break;
                }

                updateProgress(100, 100);
                return null;
            }
        };

        // Bind ProgressBar
        ProgressBarUpload.progressProperty().bind(task.progressProperty());

        // Disable button khi đang chạy
        btnUpload.setDisable(true);

        // Khi xong
        task.setOnSucceeded(e -> {
            ProgressBarUpload.progressProperty().unbind();
            ProgressBarUpload.setProgress(1);
            btnUpload.setDisable(false);
            if (callBack != null) {
                System.out.println("=========================1");
                callBack.run();
            }
            System.out.println("Upload hoàn tất!");
        });

        // Khi lỗi
        task.setOnFailed(e -> {
            ProgressBarUpload.progressProperty().unbind();
            ProgressBarUpload.setProgress(0);
            btnUpload.setDisable(false);
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private void onOpenExcel() {
        System.out.println("Mở file Excel...");
        excelFilePath = "C:\\PY\\fileExcel\\importExcelDetailsProduct.xlsx";

        // TODO: mở file excel

        if (excelFilePath == null || excelFilePath.isEmpty()) {
            System.out.println("Chưa có file Excel để mở!");
            return;
        }

        File file = new File(excelFilePath);

        if (!file.exists()) {
            System.out.println("File không tồn tại: " + file.getAbsolutePath());
            return;
        }

        try {
            Desktop.getDesktop().open(file);
            System.out.println("Đã mở file: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onOpenFolder() {
        System.out.println("Mở thư mục...");
        // TODO: mở folder chứa file

        if (excelFilePath == null || excelFilePath.isEmpty()) {
            System.out.println("Chưa có file Excel!");
            return;
        }

        File file = new File(excelFilePath);

        if (!file.exists()) {
            System.out.println("File không tồn tại: " + file.getAbsolutePath());
            return;
        }

        // Lấy thư mục cha
        File folder = file.getParentFile();

        if (folder == null || !folder.exists()) {
            System.out.println("Không tìm thấy thư mục chứa file!");
            return;
        }

        try {
            Desktop.getDesktop().open(folder); // Mở thư mục
            System.out.println("Đã mở thư mục: " + folder.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
private void onDeleteExcel() {
    System.out.println("Xóa dữ liệu Excel nhưng giữ header...");
    excelFilePath = "C:\\PY\\fileExcel\\importExcelDetailsProduct.xlsx";

    if (excelFilePath == null || excelFilePath.isEmpty()) {
        System.out.println("Chưa có file Excel để xóa!");
        return;
    }

    File file = new File(excelFilePath);
    if (!file.exists()) {
        System.out.println("File Excel không tồn tại!");
        return;
    }

    try (FileInputStream fis = new FileInputStream(file);
         XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

        // 🔥 Lặp qua tất cả sheet
        for (int s = 0; s < workbook.getNumberOfSheets(); s++) {

            XSSFSheet sheet = workbook.getSheetAt(s);
            int lastRow = sheet.getLastRowNum();

            // 👉 XÓA từ row 1 trở đi (giữ row 0 = header)
            for (int i = lastRow; i > 0; i--) {
                Row row = sheet.getRow(i);
                if (row != null) sheet.removeRow(row);
            }
        }

        // Lưu file lại
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }

        System.out.println("Đã xoá toàn bộ dữ liệu, giữ nguyên header tất cả sheet!");

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
