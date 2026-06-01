package com.phuthanh.warehouse.contextmenu;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

// import com.phuthanh.Main;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.model.info.Account;
// import com.phuthanh.model.warehouse.Cart;
import com.phuthanh.model.warehouse.RequestCart;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;
import com.phuthanh.warehouse.screen.dialog.DialogCreateCartExportVAT;
import com.phuthanh.warehouse.screen.dialog.DialogCreateCartUpdate;
import com.phuthanh.warehouse.screen.dialog.DialogUpdateCartImport;
import com.phuthanh.warehouse.screen.dialog.DialogUpdateCartTransfer;
import com.phuthanh.warehouse.screen.dialog.DialogViewDataRequestCart;

import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
// import javafx.stage.Modality;
import javafx.stage.Stage;

public class TabContextMenuCart {
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final ArrayCRUD arrayCRUD = new ArrayCRUD();

    public <S> void attachDefaultContextMenu(TableView<S> table, Supplier<String> aidSupplier,
            Runnable callbackSupplier) {
        table.setRowFactory(tv -> {
            TableRow<S> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();
            // menu.setStyle(
            // "-fx-font-size: 16px;" +
            // "-fx-padding: 8px;" +
            // "-fx-background-color: lightgray;" + // nếu muốn đổi màu nền
            // "-fx-min-width: 200px;" // tăng rộng menu
            // );

            MenuItem confirmWarehouse = new MenuItem("Xác nhận của kho");
            MenuItem confirmAccountant = new MenuItem("Xác nhận của kế toán");
            MenuItem exportVAT = new MenuItem("Xuất VAT");
            MenuItem update = new MenuItem("Cập nhật");
            MenuItem requestUpdate = new MenuItem("Yêu cầu cập nhật");
            MenuItem delete = new MenuItem("Xóa");
            MenuItem recallConfirm = new MenuItem("Thu hồi xác nhận");

            // Dùng ContextMenuRequest thay vì MouseClicked

            confirmWarehouse.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onConfirmCart(currentAID, callbackSupplier, 1);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });

            recallConfirm.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onConfirmCart(currentAID, callbackSupplier, 0);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });

            confirmAccountant.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onConfirmCart(currentAID, callbackSupplier, 2);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });

            update.setOnAction(e -> {
                try {
                    int typeCart = dbInfoHelper.getCartByAID(Integer.parseInt(aidSupplier.get())).getTypeCartID();

                    String currentAID = aidSupplier.get();
                    System.out.println("Update → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    if (typeCart == 1) {
                        onUpdateImport(currentAID, callbackSupplier, false);
                    }
                    if (typeCart == 2) {
                        onUpdateExport(currentAID, callbackSupplier, false);
                    }
                    if (typeCart == 3) {
                        onUpdateTransfer(currentAID, callbackSupplier, false);
                    }

                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });

            delete.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onDelete(currentAID, callbackSupplier, false);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });

            requestUpdate.setOnAction(e -> {
                try {
                    int typeCart = dbInfoHelper.getCartByAID(Integer.parseInt(aidSupplier.get())).getTypeCartID();

                    String currentAID = aidSupplier.get();
                    System.out.println("Update → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    if (typeCart == 1) {
                        onUpdateImport(currentAID, callbackSupplier, true);
                    }
                    if (typeCart == 2) {
                        onUpdateExport(currentAID, callbackSupplier, true);
                    }
                    if (typeCart == 3) {
                        onUpdateTransfer(currentAID, callbackSupplier, true);
                    }

                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });

            exportVAT.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onInsertCartExport(currentAID, callbackSupplier, false);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });

            // menu.getItems().addAll(confirm, update, delete);

            row.setOnMousePressed(event -> {
                if (event.isSecondaryButtonDown() && !row.isEmpty()) {
                    table.getSelectionModel().select(row.getIndex());
                }
            });

            row.setOnContextMenuRequested(event -> {
                // if (!row.isEmpty()) {
                // menu.show(row, event.getScreenX(), event.getScreenY());
                // }
                if (row.isEmpty())
                    return;
                menu.getItems().clear();
                Account acc = AppState.getInstance().get("Account", Account.class);
                int currentAID = Integer.parseInt(aidSupplier.get());

                int statusCart = dbInfoHelper.getCartByAID(currentAID).getStatusID();

                if (acc != null && (acc.getRole().equals("WAREHOUSE"))) {
                    if (statusCart == 1) {
                        menu.getItems().addAll(requestUpdate, recallConfirm, delete);
                    }
                    if (statusCart == 0) {
                        menu.getItems().addAll(confirmWarehouse, update, delete);
                    }
                }
                if (acc.getRole().equals("ACCOUNTANT")) {
                    if (statusCart == 0) {
                        menu.getItems().addAll(confirmAccountant, update, delete);
                    }
                    if (statusCart == 1) {
                        menu.getItems().addAll(confirmAccountant, requestUpdate, delete);
                    }
                }
                if (acc.getRole().equals("ADMIN")) {
                    if (statusCart == 1) {
                        menu.getItems().addAll(confirmAccountant, recallConfirm, exportVAT, requestUpdate, delete);
                    }
                    if (statusCart == 0) {
                        menu.getItems().addAll(confirmWarehouse, confirmAccountant, exportVAT, update, delete);
                    }
                }
                if (acc.getRole().equals("BUSINESS")) {
                    if (statusCart == 1) {
                        menu.getItems().addAll(exportVAT, requestUpdate, delete);
                    }
                    if (statusCart == 0) {
                        menu.getItems().addAll(exportVAT, update, delete);
                    }
                }
                if (!row.isEmpty()) {
                    menu.show(row, event.getScreenX(), event.getScreenY());
                }

            });

            // ----- HIGHLIGHT ROW -----
            row.selectedProperty().addListener((obs, oldVal, newVal) -> {
                row.pseudoClassStateChanged(PC_HIGHLIGHT, newVal);
            });

            return row;
        });
    }

    public <S> void attachDefaultContextMenuRequest(TableView<S> table, Supplier<String> aidSupplier,
            Runnable callbackSupplier) {
        table.setRowFactory(tv -> {
            TableRow<S> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();

            MenuItem confirm = new MenuItem("Xác nhận yêu cầu");
            MenuItem delete = new MenuItem("Thu hồi yêu cầu");
            MenuItem viewDetails = new MenuItem("Xem chi tiết");

            // Account selectedItemFromState = AppState.getInstance().get("Account",
            // Account.class);

            // Dùng ContextMenuRequest thay vì MouseClicked

            viewDetails.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onViewDetails(currentAID, callbackSupplier);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });
            confirm.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("Confirm → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onConfirm(currentAID, callbackSupplier);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });
            delete.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onDeleteRequest(currentAID, callbackSupplier);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });

            row.setOnMousePressed(event -> {
                if (event.isSecondaryButtonDown() && !row.isEmpty()) {
                    table.getSelectionModel().select(row.getIndex());
                }
            });
            row.setOnContextMenuRequested(event -> {
                // if (!row.isEmpty()) {
                // menu.show(row, event.getScreenX(), event.getScreenY());
                // }
                if (row.isEmpty())
                    return;
                menu.getItems().clear();
                Account acc = AppState.getInstance().get("Account", Account.class);

                if (acc != null && (acc.getRole().equals("WAREHOUSE"))) {
                    menu.getItems().addAll(confirm, delete);
                }
                if (acc.getRole().equals("ACCOUNTANT")) {
                    menu.getItems().addAll(confirm, delete);

                }
                if (acc.getRole().equals("ADMIN")) {
                    menu.getItems().addAll(confirm, delete);

                }
                if (acc.getRole().equals("BUSINESS")) {
                    String currentAID = aidSupplier.get();
                    try {
                        String isconfirm = dbCRUDHelper.returnAID("RequestCart", "UserConfirm", "RequestAID",
                                currentAID);
                        if (isconfirm == null) {
                            menu.getItems().addAll(confirm, delete);

                        }

                    } catch (Exception e) {
                        // TODO: handle exception
                        System.out.println(e.getMessage());
                    }
                }
                if (!row.isEmpty()) {
                    menu.show(row, event.getScreenX(), event.getScreenY());
                }

            });

            // ----- HIGHLIGHT ROW -----
            row.selectedProperty().addListener((obs, oldVal, newVal) -> {
                row.pseudoClassStateChanged(PC_HIGHLIGHT, newVal);
            });

            return row;
        });
    }

    private final PseudoClass PC_HIGHLIGHT = PseudoClass.getPseudoClass("highlight");

    private void onViewDetails(String codeAID, Runnable callback) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("helperFxml/dialogViewDataRequestCart.fxml"));

            Parent root = loader.load();

            DialogViewDataRequestCart controller = loader.getController();
            controller.setData(codeAID, callback);

            Stage dialog = new Stage();
            dialog.setTitle("Chi tiết");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onConfirmCart(String codeAID, Runnable callback, int typeConfirm) {
        try {
            boolean confirm = customDialogNotification.showDialogConfirm("Xác nhận đơn hàng mã " + codeAID.toString(),
                    "Bạn có chắc muốn thu hồi xác nhận đơn hàng này không?",
                    "Hành động này không thể hoàn tác.", "Xác nhận", "Thoát");
            if (confirm) {

                int rowUpdated = dbCRUDHelper.update("Cart", Arrays.asList("Status"),
                        Arrays.asList(typeConfirm), "CartAID = ?", Arrays.asList(codeAID));
                if (rowUpdated > 0) {

                    customDialogNotification.showDialog("Xác nhận thành công",
                            "Yêu cầu đã được xác nhận và chuyển vào kho hàng.", Alert.AlertType.INFORMATION);
                    if (callback != null) {
                        callback.run();
                    }
                } else {
                    customDialogNotification.showDialog("Xác nhận thất bại",
                            "Đã có lỗi xảy ra khi xác nhận yêu cầu. Vui lòng thử lại.", Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onConfirm(String codeAID, Runnable callback) {
        try {
            boolean confirm = customDialogNotification.showDialogConfirm("Xác nhận yêu cầu",
                    "Bạn có chắc muốn xác nhận yêu cầu không?",
                    "Hành động này không thể hoàn tác.", "Xác nhận", "Thoát");
            if (confirm) {
                String getAction = dbCRUDHelper.returnAID(
                        "RequestCart", "Action", "RequestAID", codeAID);
                System.out.println("CurrentAID trong onConfirm: " + getAction);
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                List<String> cartColumns = new ArrayList<>(arrayCRUD.cartColumns);
                cartColumns.remove("CartAID");
                cartColumns.remove("CartID");
                Account account = AppState.getInstance().get("Account", Account.class);

                if (getAction.equals("1")) {
                    RequestCart rc = dbInfoHelper.getRequestCartByAID(Integer.parseInt(codeAID));
                    List<Object> values = Arrays.asList(
                            rc.getAccountID(), rc.getProductAID(), rc.getProductAIDVAT(), rc.getPartNo(),
                            rc.getNameProduct(), rc.getManufacturerID(), rc.getCountryID(), rc.getUnitID(),
                            rc.getVehicleTypeID(), rc.getParameter(), rc.getBusinessID(), rc.getQty(), rc.getPrice(),
                            rc.getTotal(), rc.getCogs(), rc.getPriceVAT(), rc.getGrossPriceVAT(), rc.getPaymentID(),
                            rc.getBillID(),
                            rc.getSourceID(), rc.getDeliveryID(), rc.getEmployeeID(), false, rc.getDeliveryTime(),
                            rc.getReportDate(),
                            rc.getStatusVAT(), rc.getContractID(), rc.getPriceCost(), rc.getInvoiceNumber(),
                            rc.getRemark(), rc.getTypeCartID(), now);
                    System.out.println("Giá trị lấy được từ RequestCart: " + values);
                    int rowUpdated = dbCRUDHelper.update("Cart", cartColumns, values, "CartAID = ?",
                            Arrays.asList(rc.getCartAID()));
                    System.out.println(rowUpdated + " row(s) updated in Cart table.");
                    if (rowUpdated > 0) {
                        dbCRUDHelper.update("RequestCart", Arrays.asList("UserConfirm", "TimeConfirm"),
                                Arrays.asList(account.getAccountID(), now), "RequestAID = ?", Arrays.asList(codeAID));
                        customDialogNotification.showDialog("Xác nhận thành công",
                                "Yêu cầu đã được xác nhận và chuyển vào kho hàng.", Alert.AlertType.INFORMATION);
                        if (callback != null) {
                            callback.run();
                        }
                    } else {
                        customDialogNotification.showDialog("Xác nhận thất bại",
                                "Đã có lỗi xảy ra khi xác nhận yêu cầu. Vui lòng thử lại.", Alert.AlertType.ERROR);
                    }
                } else {
                    RequestCart rc = dbInfoHelper.getRequestCartByAID(Integer.parseInt(codeAID));
                    int rowUpdated = dbCRUDHelper.delete("Cart", Arrays.asList("CartAID"),
                            Arrays.asList(rc.getCartAID()));
                    if (rowUpdated > 0) {
                        dbCRUDHelper.update("RequestCart", Arrays.asList("UserConfirm", "TimeConfirm"),
                                Arrays.asList(account.getAccountID(), now), "RequestAID = ?", Arrays.asList(codeAID));
                        customDialogNotification.showDialog("Xác nhận thành công",
                                "Yêu cầu đã được xác nhận và chuyển vào kho hàng.", Alert.AlertType.INFORMATION);
                        if (callback != null) {
                            callback.run();
                        }
                    } else {
                        customDialogNotification.showDialog("Xác nhận thất bại",
                                "Đã có lỗi xảy ra khi xác nhận yêu cầu. Vui lòng thử lại.", Alert.AlertType.ERROR);
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onUpdateExport(String codeAID, Runnable callback, boolean isRequest) {
        try {
            String screen = "fxml/dialogCreateCartUpdate.fxml";

            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource(screen));

            Parent root = loader.load();

            DialogCreateCartUpdate controller = loader.getController();
            controller.setInitialData(callback, "UPDATE", codeAID, isRequest);

            Stage dialog = new Stage();
            dialog.setTitle("Yêu cầu cập nhật đơn hàng");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onUpdateImport(String codeAID, Runnable callback, boolean isRequest) {
        try {
            String screen = "fxml/dialogUpdateCartImport.fxml";

            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource(screen));

            Parent root = loader.load();

            DialogUpdateCartImport controller = loader.getController();
            controller.setInitialData(callback, "UPDATE", codeAID, isRequest);

            Stage dialog = new Stage();
            dialog.setTitle("Yêu cầu cập nhật đơn hàng");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onInsertCartExport(String codeAID, Runnable callback, boolean isRequest) {
        try {
            String screen = "fxml/dialogCreateCartExportVAT.fxml";

            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource(screen));

            Parent root = loader.load();

            DialogCreateCartExportVAT controller = loader.getController();
            controller.setInitialData(callback, "INSERT", codeAID, isRequest);

            Stage dialog = new Stage();
            dialog.setTitle("Xuất VAT");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onUpdateTransfer(String codeAID, Runnable callback, boolean isRequest) {
        try {
            String screen = "fxml/dialogUpdateCartTransfer.fxml";

            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource(screen));

            Parent root = loader.load();

            DialogUpdateCartTransfer controller = loader.getController();
            controller.setInitialData(callback, "UPDATE", codeAID, isRequest);

            Stage dialog = new Stage();
            dialog.setTitle("Yêu cầu cập nhật đơn hàng");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onDelete(String codeAID, Runnable callback, boolean isRequest) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCreateCartUpdate.fxml"));

            Parent root = loader.load();

            DialogCreateCartUpdate controller = loader.getController();
            controller.setInitialData(callback, "DELETE", codeAID, isRequest);

            Stage dialog = new Stage();
            dialog.setTitle("Yêu cầu xóa đơn hàng");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onDeleteRequest(String codeAID, Runnable callback) {
        try {
            boolean confirm = customDialogNotification.showDialogConfirm("Xác nhận yêu cầu",
                    "Bạn có chắc muốn thu hồi yêu cầu không?",
                    "Hành động này không thể hoàn tác.", "Xác nhận", "Thoát");
            if (confirm) {
                int rowUpdated = dbCRUDHelper.delete("RequestCart", Arrays.asList("RequestAID"),
                        Arrays.asList(codeAID));
                if (rowUpdated > 0) {
                    customDialogNotification.showDialog("Thu hồi thành công",
                            "Yêu cầu đã được thu hồi thành công.", Alert.AlertType.INFORMATION);
                    if (callback != null) {
                        callback.run();
                    }
                } else {
                    customDialogNotification.showDialog("Thu hồi thất bại",
                            "Đã có lỗi xảy ra khi thu hồi yêu cầu. Vui lòng thử lại.", Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
