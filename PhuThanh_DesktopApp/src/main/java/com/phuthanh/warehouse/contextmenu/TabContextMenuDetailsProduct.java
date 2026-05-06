package com.phuthanh.warehouse.contextmenu;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;

// import com.phuthanh.Main;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.custom.TabContextMenuHandler;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.warehouse.screen.dialog.DialogCreateDetailsProduct;

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

public class TabContextMenuDetailsProduct {
        private static final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    public <S> void attachDefaultContextMenu(TableView<S> table, Supplier<String> aidSupplier,
            Runnable callbackSupplier) {
        table.setRowFactory(tv -> {
            TableRow<S> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();
            menu.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-padding: 8px;" +
                            "-fx-background-color: lightgray;" + // nếu muốn đổi màu nền
                            "-fx-min-width: 200px;" // tăng rộng menu
            );

            MenuItem update = new MenuItem("Chỉnh sửa");
            MenuItem delete = new MenuItem("Xóa");
            // MenuItem requestDeleteProduct = new MenuItem("Yêu cầu xóa");
            // MenuItem addHistory = new MenuItem("Thêm nhập xuất");
            // MenuItem requestDeleteWareHouse = new MenuItem("Yêu cầu xóa");
            // MenuItem exportExcel = new MenuItem("Xuất Excel");

            // DrawerItem selectedItemFromState =
            // AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);

            // Dùng ContextMenuRequest thay vì MouseClicked

            update.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onUpdate(currentAID, callbackSupplier);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });
            delete.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onDelete(currentAID, callbackSupplier);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });
            menu.getItems().addAll(update, delete);

            row.setOnMousePressed(event -> {
                if (event.isSecondaryButtonDown() && !row.isEmpty()) {
                    table.getSelectionModel().select(row.getIndex());
                }
            });

            row.setOnContextMenuRequested(event -> {
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

    private static final PseudoClass PC_HIGHLIGHT = PseudoClass.getPseudoClass("highlight");

    private static void onUpdate(String codeAID, Runnable callback) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    TabContextMenuHandler.class.getResource("/fxml/dialogCreateDetailsProduct.fxml"));
            Parent root = loader.load();
            DialogCreateDetailsProduct controller = loader.getController();
            controller.setOnCreateSuccess(callback);
            // System.out.println("Selected AID for update: " + selectedAID);
            controller.setCodeAID(codeAID);
            controller.setEditMode(true);

            Stage dialog = new Stage();
            dialog.setTitle("Thêm mới chi tiết sản phẩm");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void onDelete(String codeAID, Runnable callback) {
        try {
            DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
            int row = dbCRUDHelper.delete("DetailsProduct", Arrays.asList("PartNoAID"),
                    Arrays.asList(codeAID));
            if (row > 0) {
                customDialogNotification.showDialog("Thành công", "Xóa chi tiết sản phẩm thành công",
                        Alert.AlertType.INFORMATION);
                if (callback != null) {
                    callback.run();
                }
            } else {
                System.out.println("Xóa thất bại!");
                customDialogNotification.showDialog("Lỗi", "Xóa chi tiết sản phẩm thất bại",
                        Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}
