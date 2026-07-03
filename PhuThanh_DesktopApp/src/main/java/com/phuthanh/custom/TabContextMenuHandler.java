package com.phuthanh.custom;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

// import com.phuthanh.Main;
import com.phuthanh.helper.DbCRUDHelper;
import com.phuthanh.helper.FunctionHelper;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.store.AppState;
import com.phuthanh.warehouse.controller.HistoryController;
import com.phuthanh.warehouse.screen.dialog.DialogCreateHistoryController;
import com.phuthanh.warehouse.screen.dialog.DialogCreateProductController;
import com.phuthanh.warehouse.screen.dialog.DialogRequestHistoryController;
import com.phuthanh.warehouse.screen.dialog.DialogRequestProduct;
import com.phuthanh.warehouse.screen.dialog.DialogTransferHistoryWareHouse;

import javafx.css.PseudoClass;
// import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
// import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Supplier;
import javafx.util.Callback;

/**
 * ContextMenu nâng cao cho TabPane
 */
public class TabContextMenuHandler {
    private final FunctionHelper functionHelper = new FunctionHelper();
    // private final Stage primaryStage = Main.getPrimaryStage();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    /**
     * Gắn ContextMenu mặc định cho bất kỳ TableView nào
     * 
     * @param table TableView cần gắn menu
     */
    public <S> void attachDefaultContextMenu(TableView<S> table, Supplier<String> aidSupplier,
            Runnable callback) {
        System.out.println("Attach Context Menu");
        Callback<TableView<S>, TableRow<S>> oldFactory = table.getRowFactory();

        table.setRowFactory(tv -> {

            TableRow<S> row;

            if (oldFactory != null) {
                row = oldFactory.call(tv);
            } else {
                row = new TableRow<>();
            }
            ContextMenu menu = new ContextMenu();
            // menu.setStyle(
            // "-fx-font-size: 16px;" +
            // "-fx-padding: 8px;" +
            // "-fx-background-color: lightgray;" + // nếu muốn đổi màu nền
            // "-fx-min-width: 200px;" // tăng rộng menu
            // );

            MenuItem edit = new MenuItem("Chỉnh sửa");
            MenuItem requestDeleteProduct = new MenuItem("Yêu cầu xóa");
            MenuItem addHistory = new MenuItem("Thêm nhập xuất");
            MenuItem requestDeleteWareHouse = new MenuItem("Yêu cầu xóa");
            MenuItem exportExcel = new MenuItem("Xuất Excel");
            MenuItem viewDetails = new MenuItem("Xem chi tiết");
            MenuItem viewRowHistory = new MenuItem("Xem lịch sử");
            MenuItem transferWareHouse = new MenuItem("Nhập/xuất điều chuyển");
            MenuItem copy = new MenuItem("Sao chép");

            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            // boolean userRole = Boolean.TRUE.equals(
            // AppState.getInstance().get("UserRole", Boolean.class));

            edit.setOnAction(e -> {
                if (selectedItemFromState.getWareHouseCategory() > 0) {
                    System.out.println("Edit → AID hiện tại: " + aidSupplier.get());
                    String currentAID = aidSupplier.get();
                    openDialogAddToWareHouse(currentAID, false, false, true, callback);
                } else {
                    System.out.println("Edit → AID hiện tại: " + aidSupplier.get());
                    String currentAID = aidSupplier.get();
                    openDialogUpdate(currentAID, callback);
                }
            });

            requestDeleteProduct.setOnAction(e -> {
                String currentAID = aidSupplier.get();
                openDialogRequestProduct(currentAID);
            });

            addHistory.setOnAction(e -> {
                String currentAID = aidSupplier.get();
                System.out.println("Add to WareHouse → AID hiện tại: " + currentAID);
                openDialogAddToWareHouse(currentAID, false, true, true, callback);
            });

            requestDeleteWareHouse.setOnAction(e -> {
                String currentAID = aidSupplier.get();
                System.out.println("Delete → AID hiện tại: " + currentAID);
                openDialogRequestWareHouse(currentAID, false, false, false);
            });

            exportExcel.setOnAction(e -> {

                onOpenExportExcel();
            });

            viewDetails.setOnAction(e -> {
                String currentAID = aidSupplier.get();
                System.out.println("View Details → AID hiện tại: " + currentAID);
                openDialogDetailsProduct(currentAID);
            });

            viewRowHistory.setOnAction(e -> {
                String currentAID = aidSupplier.get();
                String codeAID = currentAID;
                System.out.println("View Row History → AID hiện tại: " + currentAID);
                openDialogDetailsHistory(codeAID, callback);
            });

            transferWareHouse.setOnAction(e -> {
                String currentAID = aidSupplier.get();
                transferWareHouse(currentAID, callback);
            });

            copy.setOnAction(e -> {
                int rowIndex = table.getSelectionModel().getSelectedIndex();
                if (rowIndex < 0)
                    return;

                StringBuilder rowData = new StringBuilder();

                for (TableColumn<?, ?> column : table.getVisibleLeafColumns()) {
                    Object cellData = column.getCellData(rowIndex);

                    String value = cellData == null ? "" : cellData.toString();

                    // 🔥 FIX: loại bỏ xuống dòng trong cell
                    value = value.replaceAll("[\\r\\n]+", " ");

                    rowData.append(value).append("\t");
                }

                // bỏ tab dư ở cuối
                if (rowData.length() > 0) {
                    rowData.setLength(rowData.length() - 1);
                }

                ClipboardContent content = new ClipboardContent();
                content.putString(rowData.toString());
                Clipboard.getSystemClipboard().setContent(content);
            });
            // Dùng ContextMenuRequest thay vì MouseClicked

            row.setOnMousePressed(event -> {
                if (event.isSecondaryButtonDown() && !row.isEmpty()) {
                    table.getSelectionModel().select(row.getIndex());
                }
            });

            // row.setOnContextMenuRequested(event -> {
            if (row.isEmpty()) {
                // return;
            }

            // 👉 LẤY STATE MỚI NHẤT TẠI THỜI ĐIỂM CLICK
            DrawerItem selectedItemFromState1 = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);

            // boolean userRole = Boolean.TRUE.equals(
            //         AppState.getInstance().get("UserRole", Boolean.class));

            // System.out.println("RIGHT CLICK CATEGORY = "
            // + selectedItemFromState1.getWareHouseCategory());

            // 👉 XOÁ MENU CŨ
            menu.getItems().clear();

            // 👉 BUILD LẠI MENU THEO STATE MỚI
            // if (userRole) {
            //     if (selectedItemFromState1.getWareHouseCategory() > 0) {
            //         menu.getItems().addAll(
            //                 edit, addHistory, requestDeleteWareHouse,
            //                 viewRowHistory, transferWareHouse, copy);
            //     } else {
            //         menu.getItems().addAll(edit, requestDeleteProduct, copy);
            //     }
            // }
            Account acc = AppState.getInstance().get("Account", Account.class);
            if (acc.getRole().trim().equals("WAREHOUSE")) {
                if (selectedItemFromState1.getWareHouseCategory() > 0) {
                    menu.getItems().addAll(
                            edit, addHistory, requestDeleteWareHouse,
                            viewRowHistory, transferWareHouse, copy);
                } else {
                    menu.getItems().addAll(edit, requestDeleteProduct, copy);
                }
            }
            if (acc.getRole().trim().equals("ACCOUNTANT")) {
                if (selectedItemFromState1.getWareHouseCategory() > 0) {
                    menu.getItems().addAll(viewRowHistory, copy);
                } else {
                    menu.getItems().add(copy);
                }
            }
            if (acc.getRole().trim().equals("ADMIN")) {
                if (selectedItemFromState1.getWareHouseCategory() > 0) {
                    menu.getItems().addAll(
                            edit, addHistory, requestDeleteWareHouse,
                            viewRowHistory, transferWareHouse, copy);
                } else {
                    menu.getItems().addAll(edit, requestDeleteProduct, copy);
                }
            }
            if (acc.getRole().trim().equals("BACKOFFICE")) {
                if (selectedItemFromState1.getWareHouseCategory() > 0) {
                    menu.getItems().addAll(viewRowHistory, copy);
                } else {
                    menu.getItems().addAll(edit, requestDeleteProduct, copy);
                }
            }
            if (acc.getRole().trim().equals("BUSINESS")
                    || acc.getRole().trim().equals("IMPORT")) {
                if (selectedItemFromState1.getWareHouseCategory() > 0) {
                    menu.getItems().addAll(viewRowHistory, copy);
                } else {
                    menu.getItems().addAll(edit, requestDeleteProduct, copy);
                }
            }

            // 👉 CHỌN ROW
            table.getSelectionModel().select(row.getIndex());

            // 👉 SHOW MENU
            // menu.show(row, event.getScreenX(), event.getScreenY());
            row.setContextMenu(menu);
            // });

            // ----- HIGHLIGHT ROW -----
            // row.selectedProperty().addListener((obs, oldVal, newVal) -> {
            // row.pseudoClassStateChanged(PC_HIGHLIGHT, newVal);
            // });

            return row;
        });
    }

    public <S> void attachDetailsContextMenu(TableView<S> table, Supplier<String> aidSupplier,
            Runnable callback) {
        table.setRowFactory(tv -> {
            TableRow<S> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();
            // menu.setStyle(
            // "-fx-font-size: 16px;" +
            // "-fx-padding: 8px;" +
            // "-fx-background-color: lightgray;" + // nếu muốn đổi màu nền
            // "-fx-min-width: 200px;" // tăng rộng menu
            // );

            MenuItem edit = new MenuItem("Yêu cầu chỉnh sửa");
            // MenuItem addWareHouse = new MenuItem("Thêm vào kho");
            MenuItem addHistory = new MenuItem("Thêm nhập xuất");
            MenuItem delete = new MenuItem("Yêu cầu xóa");
            MenuItem exportExcel = new MenuItem("Xuất Excel");

            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            edit.setOnAction(e -> {
                if (selectedItemFromState.getWareHouseCategory() > 0) {
                    System.out.println("Edit → AID hiện tại: " + aidSupplier.get());
                    String currentAID = aidSupplier.get();
                    openDialogAddToWareHouse(currentAID, false, false, true, callback);
                } else {
                    System.out.println("Edit → AID hiện tại: " + aidSupplier.get());
                    String currentAID = aidSupplier.get();
                    openDialogUpdate(currentAID, callback);
                }
            });

            addHistory.setOnAction(e -> {
                String currentAID = aidSupplier.get();
                System.out.println("Add to WareHouse → AID hiện tại: " + currentAID);
                openDialogAddToWareHouse(currentAID, false, true, false, callback);
            });

            delete.setOnAction(e -> {
                String currentAID = aidSupplier.get();
                System.out.println("Delete → AID hiện tại: " + currentAID);
            });
            exportExcel.setOnAction(e -> {
                Stage stage = (Stage) table.getScene().getWindow();
                boolean result = functionHelper.exportExcel(table,
                        stage,
                        "Sheet1");

                if (result) {
                    customDialogNotification.showDialog("Thành công", "Xuất Excel thành công",
                            Alert.AlertType.INFORMATION);
                } else {
                    System.out.println("Xuất thất bại!");
                    customDialogNotification.showDialog("Lỗi", "Xuất Excel thất bại", Alert.AlertType.ERROR);

                }
            });

            if (selectedItemFromState.getWareHouseCategory() > 0) {
                menu.getItems().addAll(edit, addHistory, delete, exportExcel);
            } else {
                menu.getItems().addAll(edit, delete, exportExcel);
            }
            // menu.getItems().addAll(edit, addWareHouse, delete);

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

    private final ContextMenu requestMenu = new ContextMenu();

    public <S> void attachRequestContextMenu(TableView<S> table,
            Supplier<String> aidSupplier) {

        table.setOnContextMenuRequested(event -> {

            requestMenu.hide();
            requestMenu.getItems().clear();

            Node target = event.getPickResult().getIntersectedNode();

            TableRow<?> row = null;

            while (target != null) {
                if (target instanceof TableRow<?> tr) {
                    row = tr;
                    break;
                }
                target = target.getParent();
            }

            if (row == null || row.isEmpty()) {
                return;
            }

            table.getSelectionModel().select(row.getIndex());

            MenuItem confirmDelete = new MenuItem("Xác nhận yêu cầu");
            MenuItem deleteRequest = new MenuItem("Thu hồi yêu cầu");
            MenuItem exportExcel = new MenuItem("Xuất Excel");

            DrawerItem selectedItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);

            confirmDelete.setOnAction(e -> {
                String currentAID = aidSupplier.get();

                if (selectedItem.getWareHouseCategory() > 0) {
                    DeleteWareHouseRequest(currentAID);
                } else {
                    DeleteProductRequest(currentAID);
                }
            });

            deleteRequest.setOnAction(e -> {
                String currentAID = aidSupplier.get();
                DeleteRequest(currentAID);
            });

            exportExcel.setOnAction(e -> {
                Stage stage = (Stage) table.getScene().getWindow();

                boolean result = functionHelper.exportExcel(
                        table,
                        stage,
                        "Sheet1");

                if (result) {
                    customDialogNotification.showDialog(
                            "Thành công",
                            "Xuất Excel thành công",
                            Alert.AlertType.INFORMATION);
                } else {
                    customDialogNotification.showDialog(
                            "Lỗi",
                            "Xuất Excel thất bại",
                            Alert.AlertType.ERROR);
                }
            });

            Account acc = AppState.getInstance().get("Account", Account.class);

            if (acc != null &&
                    ("WAREHOUSE".equals(acc.getRole().trim())
                            || "ADMIN".equals(acc.getRole().trim()))) {

                requestMenu.getItems().addAll(
                        confirmDelete,
                        deleteRequest,
                        exportExcel);
            }

            if (!requestMenu.getItems().isEmpty()) {
                requestMenu.show(
                        table,
                        event.getScreenX(),
                        event.getScreenY());

                event.consume();
            }
        });
    }
    /*
     * true là chưa xác nhập
     * false là đã xác nhận
     */
    // private boolean checkUserConfirm(String codeAID) {
    // try {
    // DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();

    // DrawerItem selectedItemFromState =
    // AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
    // String nameConfirm =
    // dbCRUDHelper.returnAID(selectedItemFromState.getWareHouseRequestDataBase(),
    // "UserConfirm", "RequestAID", codeAID);
    // if (nameConfirm != null) {
    // return false;
    // } else {
    // return true;
    // }
    // } catch (Exception e) {
    // // TODO: handle exception
    // System.out.println(e.getMessage());
    // return true;
    // }
    // }

    // private boolean checkUser(String codeAID) {
    // try {
    // DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();

    // DrawerItem selectedItemFromState =
    // AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
    // Account accountFromState = AppState.getInstance().get("Account",
    // Account.class);
    // String name =
    // dbCRUDHelper.returnAID(selectedItemFromState.getWareHouseRequestDataBase(),
    // "UserRequest", "RequestAID", codeAID);
    // System.out.println("name: " + name);
    // if (name.toLowerCase().equals(accountFromState.getUserName().toLowerCase()))
    // {
    // // customDialogNotification.showDialog("Lỗi", "Không thể xác nhận vì bạn là
    // // người tạo yêu cầu",
    // // Alert.AlertType.WARNING);
    // return true;

    // }
    // return false;
    // } catch (Exception e) {
    // // TODO: handle exception
    // System.out.println(e.getMessage());
    // return false;
    // }

    // }

    private void openDialogUpdate(String productID, Runnable callback) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    TabContextMenuHandler.class.getResource("/fxml/dialogCreateProduct.fxml"));
            Parent root = loader.load();
            DialogCreateProductController controller = loader.getController();
            controller.initData(productID);
            controller.setOnCreateSuccess(callback);

            Stage dialog = new Stage();
            dialog.setTitle("Chỉnh sửa sản phẩm");
            dialog.setScene(new Scene(root));
            // dialog.initOwner(primaryStage);
            // dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDialogRequestProduct(String productID) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    TabContextMenuHandler.class.getResource("/fxml/dialogRequestProduct.fxml"));
            Parent root = loader.load();
            DialogRequestProduct controller = loader.getController();
            controller.initData(productID);

            Stage dialog = new Stage();
            dialog.setTitle("Yêu cầu xóa");
            dialog.setScene(new Scene(root));
            // dialog.initOwner(primaryStage);
            // dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDialogRequestWareHouse(String WareHouseAID, boolean isCreate, boolean isAddHistory,
            boolean isUpdate) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    TabContextMenuHandler.class.getResource("/fxml/dialogRequestWareHouse.fxml"));
            Parent root = loader.load();
            DialogRequestHistoryController controller = loader.getController();
            controller.initData(WareHouseAID, isCreate, isAddHistory, isUpdate);
            controller.setProductAID(WareHouseAID, isCreate, isAddHistory, isUpdate);

            Stage dialog = new Stage();
            dialog.setTitle("Yêu cầu xóa");
            dialog.setScene(new Scene(root));
            // dialog.initOwner(primaryStage);
            // dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDialogAddToWareHouse(String WareHouseAID, boolean isCreate, boolean isAddHistory,
            boolean isUpdate, Runnable cb) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    TabContextMenuHandler.class.getResource("/fxml/dialogCreateHistory.fxml"));
            Parent root = loader.load();
            DialogCreateHistoryController controller = loader.getController();
            controller.initData(WareHouseAID, isCreate, isAddHistory, isUpdate);
            controller.setProductAID(WareHouseAID, isCreate, isAddHistory, isUpdate);
            controller.setOnCreateSuccess(cb);

            Stage dialog = new Stage();
            dialog.setTitle("Thêm nhập xuất");
            dialog.setScene(new Scene(root));
            // dialog.initOwner(primaryStage);
            // dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void DeleteProductRequest(String codeAID) {
        try {
            DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();

            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            boolean confirm = customDialogNotification.showDialogConfirm("Xác nhận yêu cầu",
                    "Bạn có chắc muốn xác nhận yêu cầu không?",
                    "Hành động này không thể hoàn tác.", "Xác nhận", "Thoát");
            String tableRequest = selectedItemFromState.getWareHouseRequestDataBase();
            Account accountFromState = AppState.getInstance().get("Account", Account.class);
            String codeAIDDele = dbCRUDHelper.returnAID(tableRequest,
                    "ProductAID", "RequestAID", codeAID);
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            String name = dbCRUDHelper.returnAID(selectedItemFromState.getWareHouseRequestDataBase(),
                    "UserRequest", "RequestAID", codeAID);
            System.out.println("name: " + name);
            // if (name.toLowerCase().equals(accountFromState.getUserName().toLowerCase()))
            // {
            // customDialogNotification.showDialog("Lỗi", "Không thể xác nhận vì bạn là
            // người tạo yêu cầu",
            // Alert.AlertType.WARNING);
            // return;

            // }
            if (confirm) {
                int result = dbCRUDHelper.delete(selectedItemFromState.getWareHouseDataBase(),
                        List.of("ProductAID"),
                        List.of(codeAIDDele));
                if (result > 0) {
                    customDialogNotification.showDialog("Thành công", "Xóa thành công", Alert.AlertType.INFORMATION);
                    // GỌI RELOAD
                    int updateRequest = dbCRUDHelper.update(tableRequest, List.of("UserConfirm", "TimeConfirm"),
                            List.of(accountFromState.getUserName(), timestamp), "RequestAID = ?", List.of(codeAID));
                    System.out.println(updateRequest);
                    if (onReloadCallback != null) {
                        onReloadCallback.run();
                    }
                } else {
                    customDialogNotification.showDialog("Lỗi", "Xóa thất bại", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi", "Đã có lỗi xảy ra: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void DeleteWareHouseRequest(String codeAID) {
        try {
            DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();

            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            boolean confirm = customDialogNotification.showDialogConfirm("Xác nhận yêu cầu",
                    "Bạn có chắc muốn xác nhận yêu cầu không?",
                    "Hành động này không thể hoàn tác.", "Xác nhận", "Thoát");
            String tableRequest = selectedItemFromState.getWareHouseRequestDataBase();
            Account accountFromState = AppState.getInstance().get("Account", Account.class);
            String codeAIDDele = dbCRUDHelper.returnAID(tableRequest,
                    "DataWareHouseAID", "RequestAID", codeAID);
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            String name = dbCRUDHelper.returnAID(selectedItemFromState.getWareHouseRequestDataBase(),
                    "UserRequest", "RequestAID", codeAIDDele);
            System.out.println(name);
            // if (name.toLowerCase().equals(accountFromState.getUserName().toLowerCase()))
            // {
            // customDialogNotification.showDialog("Lỗi", "Không thể xác nhận vì bạn là
            // người tạo yêu cầu",
            // Alert.AlertType.WARNING);
            // return;

            // }

            if (confirm) {
                int result = dbCRUDHelper.delete(selectedItemFromState.getWareHouseDataBase(),
                        List.of("DataWareHouseAID"),
                        List.of(codeAIDDele));
                if (result > 0) {
                    customDialogNotification.showDialog("Thành công", "Xóa thành công", Alert.AlertType.INFORMATION);
                    // GỌI RELOAD
                    int updateRequest = dbCRUDHelper.update(tableRequest, List.of("UserConfirm", "TimeConfirm"),
                            List.of(accountFromState.getAccountID(), timestamp), "RequestAID = ?", List.of(codeAID));
                    System.out.println(updateRequest);
                    if (onReloadCallback != null) {
                        onReloadCallback.run();
                    }
                } else {
                    customDialogNotification.showDialog("Lỗi", "Xóa thất bại", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            customDialogNotification.showDialog("Lỗi", "Đã có lỗi xảy ra: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void DeleteRequest(String codeAID) {
        try {
            DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();

            DrawerItem selectedItemFromState = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            String checkRequest = dbCRUDHelper.returnAID(selectedItemFromState.getWareHouseRequestDataBase(),
                    "UserConfirm", "RequestAID", codeAID);
            if (checkRequest != null) {
                customDialogNotification.showDialog("Lỗi", "Không thể xóa vì đã xác nhận", Alert.AlertType.WARNING);
            } else {
                String checkAccountID = dbCRUDHelper.returnAID(selectedItemFromState.getWareHouseRequestDataBase(),
                        "UserRequest", "RequestAID", codeAID);
                Account accountFromState = AppState.getInstance().get("Account", Account.class);
                if (checkAccountID.equals(accountFromState.getUserName())) {
                    boolean confirm = customDialogNotification.showDialogConfirm("Thu hồi yêu cầu",
                            "Bạn có chắc muốn thu hồi yêu cầu không?",
                            "Hành động này không thể hoàn tác.", "Thu hồi", "Thoát");
                    if (confirm) {
                        int result = dbCRUDHelper.delete(selectedItemFromState.getWareHouseRequestDataBase(),
                                List.of("RequestAID"),
                                List.of(codeAID));
                        if (result > 0) {
                            customDialogNotification.showDialog("Thành công", "Xóa thành công",
                                    Alert.AlertType.INFORMATION);
                            // GỌI RELOAD
                            if (onReloadCallback != null) {
                                onReloadCallback.run();
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

    private Runnable onReloadCallback;

    public void setOnReloadCallback(Runnable callback) {
        onReloadCallback = callback;
    }

    private void onOpenExportExcel() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    TabContextMenuHandler.class.getResource("/fxml/dialogExportExcelWareHouse.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Thêm sản phẩm");
            dialog.setScene(new Scene(root));
            // dialog.initOwner(primaryStage);
            // dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDialogDetailsProduct(String productID) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    TabContextMenuHandler.class.getResource("/fxml/dialogDetailsProduct.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Chi tiết sản phẩm");
            dialog.setScene(new Scene(root));
            // dialog.initOwner(primaryStage);
            // dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDialogDetailsHistory(String productID, Runnable callback) {
        try {

            DrawerItem selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            FXMLLoader loader = new FXMLLoader(
                    TabContextMenuHandler.class.getResource("/fxml/dialogHistoryWareHouse.fxml"));
            Parent root = loader.load();
            HistoryController controller = loader.getController();
            controller.initData(selectedDrawerItem, productID, callback);
            Stage dialog = new Stage();
            dialog.setTitle("Lịch sử nhập xuất");
            dialog.setScene(new Scene(root));
            // dialog.initOwner(primaryStage);
            // dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void transferWareHouse(String codeAID, Runnable cb) {
        try {
            DrawerItem selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            FXMLLoader loader = new FXMLLoader(
                    TabContextMenuHandler.class.getResource("/fxml/dialogTransferHistoryWareHouse.fxml"));
            Parent root = loader.load();
            DialogTransferHistoryWareHouse controller = loader.getController();
            controller.initData(selectedDrawerItem, codeAID, cb);
            Stage dialog = new Stage();
            dialog.setTitle("Xuất điều chuyển");
            dialog.setScene(new Scene(root));
            // dialog.initOwner(primaryStage);
            // dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(true);
            dialog.showAndWait();
            // dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final PseudoClass PC_HIGHLIGHT = PseudoClass.getPseudoClass("highlight");

}
