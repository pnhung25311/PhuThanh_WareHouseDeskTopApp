package com.phuthanh.warehouse.EditableTableView.helper;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.phuthanh.warehouse.EditableTableView.modelTable.CartFX;
import com.phuthanh.custom.CustomDialogNotification;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TableUtils {

    // Tối ưu RAM: Dùng chung một Instance Dialog/Notification cho toàn bộ các màn hình Cart
    private static final CustomDialogNotification dialogHelper = new CustomDialogNotification();

    public static CustomDialogNotification getDialogHelper() {
        return dialogHelper;
    }

    /**
     * Cấu hình các thuộc tính chỉnh sửa cơ bản cho TableView độc lập với kiểu dữ liệu
     */
    public static <T> void configureEditableTable(TableView<T> table) {
        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);
    }

    /**
     * Hàm dịch chuyển con trỏ ô dùng chung (Ủy quyền hành vi thêm dòng mới qua Runnable)
     */
    public static void moveToCell(TableView<CartFX> table, int row, int col, Runnable addNewRowTask) {
        int maxCol = table.getColumns().size() - 1;
        if (col < 0) col = 0;
        if (col > maxCol) col = maxCol;
        if (row < 0) row = 0;

        if (row >= table.getItems().size()) {
            if (addNewRowTask != null) {
                addNewRowTask.run(); // Gọi hàm addNewRow(1) của Class nghiệp vụ tương ứng
            }
        }

        final int targetRow = row;
        final int targetCol = col;

        Platform.runLater(() -> {
            if (targetRow < table.getItems().size()) {
                TableColumn<CartFX, ?> column = table.getColumns().get(targetCol);
                table.getSelectionModel().clearAndSelect(targetRow, column);
                table.getFocusModel().focus(targetRow, column);
                table.scrollTo(targetRow);
            }
        });
    }

    /**
     * Dịch chuyển ô chuyên biệt cho màn hình Xóa (Sử dụng TableView<StringProperty>)
     */
    public static void moveToCellSimple(TableView<StringProperty> table, int row, Runnable addNewRowTask) {
        if (row < 0) row = 0;
        if (row >= table.getItems().size() && addNewRowTask != null) {
            addNewRowTask.run();
        }
        int targetRow = row;
        Platform.runLater(() -> {
            if (targetRow < table.getItems().size()) {
                table.getSelectionModel().clearAndSelect(targetRow);
                table.getFocusModel().focus(targetRow);
                table.scrollTo(targetRow);
                if (!table.getColumns().isEmpty()) {
                    table.edit(targetRow, table.getColumns().get(0));
                }
            }
        });
    }

    /**
     * Tối ưu RAM: Định dạng số tập trung sử dụng một vùng bộ nhớ tĩnh
     */
    public static String formatNumber(Object value) {
        if (value == null) return "0";
        try {
            double num;
            if (value instanceof StringProperty sp) {
                num = Double.parseDouble(sp.get().replace(",", "").replace(".", ""));
            } else {
                num = Double.parseDouble(value.toString().replace(",", "").replace(".", ""));
            }
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            DecimalFormat df = new DecimalFormat("#,###", symbols);
            return df.format(num);
        } catch (Exception e) {
            return "0";
        }
    }

    public static Double parseNumber(Object valueObj) {
        if (valueObj == null) return 0.0;
        String value = (valueObj instanceof StringProperty sp) ? sp.get() : valueObj.toString();
        if (value == null || value.isBlank()) return 0.0;
        try {
            return Double.parseDouble(value.replace(",", "").replace(".", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Áp dụng Style CSS ScrollBar dùng chung cho TableView
     * Cơ chế giải phóng và tái sử dụng bộ nhớ String CSS
     */
    public static void applyTableStyle(TableView<?> table) {
        if (table.getScene() == null) {
            table.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) injectCss(newScene);
            });
        } else {
            injectCss(table.getScene());
        }
    }

    private static void injectCss(javafx.scene.Scene scene) {
        String css = """
            .scroll-bar:vertical, .scroll-bar:horizontal { -fx-background-color: transparent; }
            .scroll-bar .thumb { -fx-background-color: #C1C1C1; -fx-background-radius: 4; }
            .scroll-bar .thumb:hover { -fx-background-color: #A8A8A8; }
            .scroll-bar .thumb:pressed { -fx-background-color: #8E8E8E; }
            .scroll-bar .increment-button, .scroll-bar .decrement-button { -fx-background-color: transparent; -fx-padding: 0; }
        """.replace("\n", "");
        
        String dataUrl = "data:text/css," + css;
        if (!scene.getStylesheets().contains(dataUrl)) {
            scene.getStylesheets().add(dataUrl);
        }
    }
}