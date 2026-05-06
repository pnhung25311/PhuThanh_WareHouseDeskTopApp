package com.phuthanh.custom;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

public class CustomDialogNotification {
    /**
     * Hiển thị dialog thông báo
     * 
     * @param title   Tiêu đề dialog
     * @param message Nội dung thông báo
     * @param type    Loại thông báo (INFORMATION, WARNING, ERROR, CONFIRMATION)
     */
    public void showDialog(String title, String message, AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null); // bỏ header nếu muốn
        alert.showAndWait();
    }

    /**
     * Hiển thị dialog Confirm
     * 
     * @param setTitle       Tiêu đề dialog
     * @param setHeaderText  Tiều đề đầu trang
     * @param setContentText Nội dung của dialog
     * @param btnConfirm     Text của btn confirm
     * @param btnClose       Text của btn close
     * @return Kết quả trả về là true hoặc false nếu true là đã xác nhận và false là
     *         ngược lại
     */

    public boolean showDialogConfirm(String setTitle, String setHeaderText, String setContentText, String btnConfirm,
            String btnClose) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(setTitle);
        alert.setHeaderText(setHeaderText);
        alert.setContentText(setContentText);

        ButtonType yesBtn = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesBtn, cancelBtn);

        return alert.showAndWait().orElse(cancelBtn) == yesBtn;
    }

}
