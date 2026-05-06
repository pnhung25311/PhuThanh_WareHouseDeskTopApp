package com.phuthanh.warehouse.contextmenu;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.custom.TabContextMenuHandler;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.model.warehouse.RequestHistoryWareHouse;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;
import com.phuthanh.warehouse.screen.dialog.DialogUpdateHistory;

import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

// import com.phuthanh.custom.customDialogNotification;
// import com.phuthanh.helper.FunctionHelper;
// import com.phuthanh.model.DrawerItem;
// import com.phuthanh.store.AppState;

// import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
// import javafx.stage.Stage;
import javafx.stage.Stage;

public class TabContextMenuHistory {
    private static final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    /**
     * Gắn ContextMenu mặc định cho bất kỳ TableView nào
     * 
     * @param table TableView cần gắn menu
     */
    public <S> void attachDefaultContextMenu(TableView<S> table, Supplier<String> aidSupplier) {
        table.setRowFactory(tv -> {
            TableRow<S> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();
            menu.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-padding: 8px;" +
                            "-fx-background-color: lightgray;" + // nếu muốn đổi màu nền
                            "-fx-min-width: 200px;" // tăng rộng menu
            );

            MenuItem edit = new MenuItem("Yêu cầu chỉnh sửa");
            MenuItem delete = new MenuItem("Yêu cầu xóa");

            edit.setOnAction(e -> {
                String currentAID = aidSupplier.get();
                System.out.println("Delete → AID hiện tại: " + currentAID);
                openDialogHistory(currentAID, 1);
            });

            delete.setOnAction(e -> {
                String currentAID = aidSupplier.get();
                System.out.println("Delete → AID hiện tại: " + currentAID);
                openDialogHistory(currentAID, 0);
            });

            // Dùng ContextMenuRequest thay vì MouseClicked
            row.setOnMousePressed(event -> {
                if (event.isSecondaryButtonDown() && !row.isEmpty()) {
                    table.getSelectionModel().select(row.getIndex());
                }
            });

            row.setOnContextMenuRequested(event -> {
                if (row.isEmpty())
                    return;
                boolean userRole = Boolean.TRUE.equals(
                        AppState.getInstance().get("UserRole", Boolean.class));
                if (userRole) {
                    menu.getItems().addAll(edit, delete);
                }
                table.getSelectionModel().select(row.getIndex());

                menu.show(row, event.getScreenX(), event.getScreenY());

            });

            // ----- HIGHLIGHT ROW -----
            row.selectedProperty().addListener((obs, oldVal, newVal) -> {
                row.pseudoClassStateChanged(PC_HIGHLIGHT, newVal);
            });

            return row;
        });
    }

    public <S> void attachDefaultContextMenuRequest(TableView<S> table, Supplier<String> aidSupplier,
            Supplier<Runnable> callbackSupplier, Runnable callbackSupplierHistory) {
        table.setRowFactory(tv -> {
            TableRow<S> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();
            menu.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-padding: 8px;" +
                            "-fx-background-color: lightgray;" + // nếu muốn đổi màu nền
                            "-fx-min-width: 200px;" // tăng rộng menu
            );

            MenuItem confirmRequest = new MenuItem("Xác nhận yêu cầu");
            MenuItem deleteRequest = new MenuItem("Thu hồi yêu cầu");

            // DrawerItem selectedItemFromState =
            // AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);

            confirmRequest.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    Runnable cb = callbackSupplier.get();
                    DeleteHistoryRequest(currentAID, cb);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });
            deleteRequest.setOnAction(e -> {
                String currentAID = aidSupplier.get();
                System.out.println("Delete → AID hiện tại: " + currentAID);
                Runnable cb = callbackSupplier.get();
                // Runnable cbhis = callbackSupplierHistory.get();

                DeleteRequest(currentAID, cb, callbackSupplierHistory);
            });

            menu.getItems().addAll(confirmRequest, deleteRequest);

            // Dùng ContextMenuRequest thay vì MouseClicked
            row.setOnMousePressed(event -> {
                if (event.isSecondaryButtonDown() && !row.isEmpty()) {
                    table.getSelectionModel().select(row.getIndex());
                }
            });

            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    menu.show(row, event.getScreenX(), event.getScreenY());
                }
                if (row.isEmpty())
                    return;

                menu.getItems().clear(); // ⚠️ rất quan trọng

                boolean userRole = Boolean.TRUE.equals(
                        AppState.getInstance().get("UserRole", Boolean.class));
                if (userRole) {
                    String currentAID = aidSupplier.get();
                    boolean isOwnerConfirm = checkUserConfirm(currentAID); // ✅ CHỈ GỌI KHI CLICK PHẢI
                    if (isOwnerConfirm) {
                        boolean isOwner = checkUser(currentAID); // ✅ CHỈ GỌI KHI CLICK PHẢI
                        if (isOwner) {
                            menu.getItems().addAll(confirmRequest, deleteRequest);
                        } else {
                            menu.getItems().addAll(confirmRequest, deleteRequest);
                        }
                    }
                }
                menu.show(row, event.getScreenX(), event.getScreenY());
            });

            // ----- HIGHLIGHT ROW -----
            row.selectedProperty().addListener((obs, oldVal, newVal) -> {
                row.pseudoClassStateChanged(PC_HIGHLIGHT, newVal);
            });

            return row;
        });
    }

    /*
     * true là chưa xác nhập
     * false là đã xác nhận
     */
    private static boolean checkUserConfirm(String codeAID) {
        try {
            DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            String nameConfirm = dbCRUDHelper.returnAID(selectedItemFromState.getWareHouseUpdateHistoryDataBase(),
                    "UserConfirm", "RequestAID", codeAID);
            if (nameConfirm != null) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e.getMessage());
            return true;
        }
    }

    private static boolean checkUser(String codeAID) {
        try {
            DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            Account accountFromState = AppState.getInstance().get("Account", Account.class);

            String name = dbCRUDHelper.returnAID(selectedItemFromState.getWareHouseUpdateHistoryDataBase(),
                    "UserRequest", "RequestAID", codeAID);
            System.out.println("name: " + name);
            if (name.toLowerCase().equals(accountFromState.getUserName().toLowerCase())) {
                // customDialogNotification.showDialog("Lỗi", "Không thể xác nhận vì bạn là
                // người tạo yêu cầu",
                // Alert.AlertType.WARNING);
                return true;
            }
            return false;
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e.getMessage());
            return false;
        }
    }

    private static final PseudoClass PC_HIGHLIGHT = PseudoClass.getPseudoClass("highlight");

    private static void openDialogHistory(String currentAID, int status) {
        try {
            // DrawerItem selectedDrawerItem =
            // AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            FXMLLoader loader = new FXMLLoader(
                    TabContextMenuHandler.class.getResource("/fxml/dialogUpdateHistory.fxml"));
            // FXMLLoader loader = new
            // FXMLLoader(getClass().getClassLoader().getResource("fxml/dialogUpdateHistory.fxml"));

            Parent root = loader.load();
            DialogUpdateHistory controller = loader.getController();
            controller.initData(currentAID, status);
            // controller.setOnCreateSuccess(this::loadProductTable("1900-01-01",
            // FunctionHelper.convertDate(LocalDate.now())));

            Stage dialog = new Stage();
            dialog.setTitle("Chỉnh sửa lịch sử nhập xuất");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void DeleteRequest(String codeAID, Runnable callback, Runnable callbackHis) {
        try {
            DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            String checkRequest = dbCRUDHelper.returnAID(selectedItemFromState.getWareHouseUpdateHistoryDataBase(),
                    "UserConfirm", "RequestAID", codeAID);
            if (checkRequest != null) {
                customDialogNotification.showDialog("Lỗi", "Không thể xóa vì đã xác nhận", Alert.AlertType.WARNING);
            } else {
                String checkAccountID = dbCRUDHelper.returnAID(
                        selectedItemFromState.getWareHouseUpdateHistoryDataBase(),
                        "UserRequest", "RequestAID", codeAID);
                Account accountFromState = AppState.getInstance().get("Account", Account.class);
                if (checkAccountID.equals(accountFromState.getUserName())) {
                    boolean confirm = customDialogNotification.showDialogConfirm("Thu hồi yêu cầu",
                            "Bạn có chắc muốn thu hồi yêu cầu này không?", "Hành động này không thể hoàn tác.",
                            "Thu hồi",
                            "Thoát");
                    if (confirm) {
                        int result = dbCRUDHelper.delete(selectedItemFromState.getWareHouseUpdateHistoryDataBase(),
                                List.of("RequestAID"),
                                List.of(codeAID));
                        if (result > 0) {
                            customDialogNotification.showDialog("Thành công", "Xóa thành công",
                                    Alert.AlertType.INFORMATION);
                            // GỌI RELOAD
                            if (callback != null) {
                                callback.run();
                                System.out.println("====================");
                            }
                            if (callbackHis != null) {
                                callbackHis.run();
                                System.out.println("====================");
                            }
                        } else {
                            customDialogNotification.showDialog("Lỗi", "Xóa thất bại", Alert.AlertType.ERROR);
                        }
                    }
                } else {
                    customDialogNotification.showDialog("Lỗi", "Bạn không phải người yêu cầu",
                            Alert.AlertType.WARNING);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi", "Đã có lỗi xảy ra: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private static void DeleteHistoryRequest(String codeAID, Runnable callback) {
        try {
            DbCRUDHelper db = new DbCRUDHelper();
            DbInfoHelper dbInfo = new DbInfoHelper();

            DrawerItem selectedDrawer = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            Account account = AppState.getInstance().get("Account", Account.class);

            boolean confirm = customDialogNotification.showDialogConfirm(
                    "Xác nhận yêu cầu",
                    "Bạn có chắc muốn xác nhận yêu cầu không?",
                    "Hành động này không thể hoàn tác.",
                    "Xác nhận", "Thoát");

            if (!confirm)
                return;

            String tableRequest = selectedDrawer.getWareHouseUpdateHistoryDataBase();
            String codeAIDDele = db.returnAID(tableRequest, "HistoryAID", "RequestAID", codeAID);

            if (isBlank(codeAIDDele)) {
                customDialogNotification.showDialog("Lỗi", "Không tìm thấy HistoryAID", Alert.AlertType.ERROR);
                return;
            }

            RequestHistoryWareHouse request = dbInfo.getRequestHistoryWareHouse(codeAID);
            if (request == null) {
                customDialogNotification.showDialog("Lỗi", "Không tìm thấy dữ liệu request", Alert.AlertType.ERROR);
                return;
            }

            String nameCreator = db.returnAID(selectedDrawer.getWareHouseRequestDataBase(),
                    "UserRequest", "RequestAID", codeAIDDele);

            if (!isBlank(nameCreator) &&
                    nameCreator.equalsIgnoreCase(account.getUserName())) {
                customDialogNotification.showDialog("Lỗi",
                        "Không thể xác nhận vì bạn là người tạo yêu cầu",
                        Alert.AlertType.WARNING);
                return;
            }

            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());

            List<String> columnsHistory = new ArrayList<>(ArrayCRUD.historyColumns);
            columnsHistory.remove("HistoryAID");
            columnsHistory.remove("DataWareHouseAID");

            // =========================================================
            // 🔁 XỬ LÝ KHO ĐÍCH (TRANSFER GROUP)
            // =========================================================
            String transferGroupID = db.returnAID(
                    selectedDrawer.getWareHouseDataBaseHistory(),
                    "TransferGroupID", "HistoryAID", codeAIDDele);

            if (!isBlank(transferGroupID)) {
                String lastTwo = transferGroupID.substring(transferGroupID.length() - 2);

                DrawerItem targetDrawer = dbInfo.getWareHouseDataBase()
                        .stream()
                        .filter(i -> lastTwo.equals(i.getWareHouseID()))
                        .findFirst()
                        .orElse(null);

                if (targetDrawer != null) {
                    String returnAID = db.returnAID(
                            targetDrawer.getWareHouseDataBaseHistory(),
                            "DataWareHouseAID", "TransferGroupID", transferGroupID);

                    int dataWarehouseAID = safeParseInt(returnAID);

                    if (dataWarehouseAID > 0) {
                        if (request.getAction()) {
                            // đảo chiều qty
                            db.update(targetDrawer.getWareHouseDataBaseHistory(),
                                    columnsHistory,
                                    List.of(request.getQty() * -1,
                                            request.getIdEmployee(),
                                            request.getPartner(),
                                            request.getRemark(),
                                            request.getTime(),
                                            request.getTransferGroupID(),
                                            request.getUserRequest(),
                                            timestamp),
                                    "TransferGroupID = ?", List.of(transferGroupID));
                        } else {
                            db.delete(targetDrawer.getWareHouseDataBaseHistory(),
                                    List.of("TransferGroupID"),
                                    List.of(transferGroupID));
                        }

                        recalcWarehouseQty(db,
                                targetDrawer.getWareHouseDataBaseHistory().toString(),
                                targetDrawer.getWareHouseDataBase().toString(),
                                dataWarehouseAID);
                    }
                }
            }

            // =========================================================
            // 🔁 XỬ LÝ KHO NGUỒN
            // =========================================================
            if (request.getAction()) {
                // CONFIRM
                int row = db.update(selectedDrawer.getWareHouseDataBaseHistory(),
                        columnsHistory,
                        List.of(request.getQty(),
                                request.getIdEmployee(),
                                request.getPartner(),
                                request.getRemark(),
                                request.getTime(),
                                request.getTransferGroupID() == null ? "" : request.getTransferGroupID(),
                                request.getUserRequest(),
                                timestamp),
                        "HistoryAID = ?", List.of(codeAIDDele));

                if (row > 0) {
                    recalcWarehouseQty(db,
                            selectedDrawer.getWareHouseDataBaseHistory().toString(),
                            selectedDrawer.getWareHouseDataBase().toString(),
                            request.getDataWareHouseAID());

                    db.update(tableRequest,
                            List.of("UserConfirm", "TimeConfirm"),
                            List.of(account.getUserName(), timestamp),
                            "RequestAID = ?", List.of(codeAID));

                    customDialogNotification.showDialog("Thành công",
                            "Xác nhận yêu cầu thành công",
                            Alert.AlertType.INFORMATION);
                }

            } else {
                // DELETE REQUEST
                db.delete(selectedDrawer.getWareHouseDataBaseHistory(),
                        List.of("HistoryAID"),
                        List.of(codeAIDDele));

                recalcWarehouseQty(db,
                        selectedDrawer.getWareHouseDataBaseHistory().toString(),
                        selectedDrawer.getWareHouseDataBase().toString(),
                        request.getDataWareHouseAID());

                db.update(tableRequest,
                        List.of("UserConfirm", "TimeConfirm"),
                        List.of(account.getUserName(), timestamp),
                        "RequestAID = ?", List.of(request.getRequestAID()));

                customDialogNotification.showDialog("Thành công",
                        "Xóa thành công",
                        Alert.AlertType.INFORMATION);
            }

            if (callback != null)
                callback.run();

        } catch (Exception e) {
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi",
                    "Đã có lỗi xảy ra: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private static int safeParseInt(String value) {
        if (value == null)
            return 0;
        value = value.trim();
        if (value.isEmpty())
            return 0;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static void recalcWarehouseQty(DbCRUDHelper db, String historyTable, String warehouseTable,
            int dataWarehouseAID) {
        try {
            double totalQty = db.sumQtyHistory(historyTable, dataWarehouseAID);
            db.update(warehouseTable,
                    List.of("Qty"),
                    List.of(totalQty),
                    "DataWareHouseAID = ?", List.of(dataWarehouseAID));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }
}
