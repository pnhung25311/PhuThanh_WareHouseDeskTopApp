package com.phuthanh.warehouse.screen.dialog;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.phuthanh.custom.CustomCombobox;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.model.warehouse.Sheet;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DialogSheet {
    @FXML
    private TextField txtSheetID;

    @FXML
    private TextField txtRemark;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnExit;
    private DrawerItem selecDrawerItem;
    private String CodeAID;
    private Runnable CallBack;
    private   final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private   final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private   final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
            private final ArrayCRUD arrayCRUD = new ArrayCRUD();

    // private   final ArrayCRUD arrayCRUD = new ArrayCRUD();

    // Xử lý khi bấm nút Lưu
    @FXML
    private void onSave() {
        try {

            Account accountFromState = AppState.getInstance().get("Account", Account.class);
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            String sheetId = txtSheetID.getText();
            String remark = txtRemark.getText();
            List<Object> values = Arrays.asList(sheetId.trim(), 0, remark.trim(), accountFromState.getUserName(),
                    timestamp);
            List<String> columnsSheet = new ArrayList<>(arrayCRUD.sheetColumns);
            columnsSheet.remove("SheetAID");

            int row = dbCRUDHelper.insert(selecDrawerItem.getWareHouseSheetDataBase(), columnsSheet, values);
            if (row > 0) {
                customDialogNotification.showDialog("Thành công", "Tạo phiếu kiểm kho thành công",
                        Alert.AlertType.INFORMATION);
                if (CallBack != null) {
                    CallBack.run();
                }
                onExit();
            } else {
                customDialogNotification.showDialog("Thành công", "Tạo phiếu kiểm kho thất bại",
                        Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        // TODO: handle exception
    }

    public void initData(DrawerItem item, String codeAID, Runnable callBack) {
        this.selecDrawerItem = item;
        this.CodeAID = codeAID;
        this.CallBack = callBack;
        loadData();
    }

    private void loadData() {
        try {
            Sheet sheet = dbInfoHelper.getSheetByAID(selecDrawerItem.getWareHouseSheetDataBase(), CodeAID);
            if (sheet != null) {
                txtSheetID.setText(sheet.getSheetID());
                txtRemark.setText(sheet.getRemark());
            }
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e.getMessage());
        }
    }

    // Xử lý khi bấm nút Thoát
    @FXML
    private void onExit() {
        Stage stage = (Stage) btnExit.getScene().getWindow();
        stage.close();
    }
}
