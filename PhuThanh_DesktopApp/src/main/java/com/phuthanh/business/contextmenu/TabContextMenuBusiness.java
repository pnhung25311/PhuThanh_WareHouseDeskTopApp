package com.phuthanh.business.contextmenu;

import java.io.IOException;
import java.util.function.Supplier;

import com.phuthanh.business.screen.dialog.DialogHistoryBusiness;
import com.phuthanh.model.business.ProductBusiness;
import com.phuthanh.model.info.Account;
import com.phuthanh.store.AppState;
import com.phuthanh.warehouse.screen.dialog.DialogCreateCartExport;
import com.phuthanh.warehouse.screen.dialog.DialogCreateCartImport;
import com.phuthanh.warehouse.screen.dialog.DialogCreateCartImportExport;
import com.phuthanh.warehouse.screen.dialog.DialogCreateCartSynthetic;
import com.phuthanh.warehouse.screen.dialog.DialogCreateCartTransfer;

import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

public class TabContextMenuBusiness {
    public <S> void attachDefaultContextMenu(TableView<S> table, Supplier<String> aidSupplier,
            Supplier<ProductBusiness> aidSupplierBusiness,
            Runnable callbackSupplier) {
        table.setRowFactory(tv -> {
            TableRow<S> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();
            menu.setStyle(
                    // "-fx-font-size: 16px;" +
                    // "-fx-padding: 8px;" +
                    // "-fx-background-color: lightgray;" + // nếu muốn đổi màu nền
                    // "-fx-min-width: 200px;" // tăng rộng menu
                    "");

            MenuItem copy = new MenuItem("Sao chép");
            MenuItem viewHistory = new MenuItem("Xem nhập/xuất FAST");
            MenuItem addExCart = new MenuItem("Xuất kho");
            MenuItem addImCart = new MenuItem("Nhập kho");
            MenuItem addImExCart = new MenuItem("Nhập/Xuất kho");
            MenuItem addTransferCart = new MenuItem("Điều chuyển kho");
            MenuItem addSyntheticCart = new MenuItem("Nhập xuất tổng hợp");

            addExCart.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    ProductBusiness productBusiness = aidSupplierBusiness.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    // onConfirm(currentAID, callbackSupplier);
                    onCreateCartExport(currentAID, callbackSupplier, "CREATEEX", "Phiếu xuất kho", productBusiness);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });
            addImCart.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    ProductBusiness productBusiness = aidSupplierBusiness.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    System.out.println("Delete → AID hiện tại: " + productBusiness.maVatTu);

                    // Runnable cb = callbackSupplier.get();
                    // onConfirm(currentAID, callbackSupplier);
                    onCreateCartImport(currentAID, callbackSupplier, "CREATEIM", "Phiếu nhập kho", productBusiness);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });
            addImExCart.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    ProductBusiness productBusiness = aidSupplierBusiness.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    System.out.println("Delete → AID hiện tại: " + productBusiness.maVatTu);

                    // Runnable cb = callbackSupplier.get();
                    // onConfirm(currentAID, callbackSupplier);
                    onCreateCartImportExport(currentAID, callbackSupplier, currentAID, currentAID, productBusiness);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });
            viewHistory.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("HISTORY → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onHistory(currentAID);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });

            addTransferCart.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    ProductBusiness productBusiness = aidSupplierBusiness.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onCreateCartTransfer(currentAID, callbackSupplier, "CREATEIM", "Phiếu điều chuyển kho",
                            productBusiness);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });

            addSyntheticCart.setOnAction(e -> {
                try {
                    String currentAID = aidSupplier.get();
                    System.out.println("Delete → AID hiện tại: " + currentAID);
                    // Runnable cb = callbackSupplier.get();
                    onAddCartSynthetic(currentAID, callbackSupplier);
                } catch (Exception ex) {
                    // TODO: handle exception
                }
            });

            copy.setOnAction(e -> {

                var selectedIndices = table.getSelectionModel().getSelectedIndices();
                if (selectedIndices.isEmpty())
                    return;

                StringBuilder clipboardString = new StringBuilder();

                // ⭐ COPY DATA ROWS (không còn header)
                for (int rowIndex : selectedIndices) {

                    for (TableColumn<?, ?> column : table.getVisibleLeafColumns()) {
                        Object cellData = column.getCellData(rowIndex);
                        String value = cellData == null ? "" : cellData.toString();

                        // loại bỏ xuống dòng trong cell
                        value = value.replaceAll("[\\r\\n]+", " ");

                        clipboardString.append(value).append("\t");
                    }

                    // bỏ tab cuối dòng
                    clipboardString.setLength(clipboardString.length() - 1);
                    clipboardString.append("\n");
                }

                ClipboardContent content = new ClipboardContent();
                content.putString(clipboardString.toString());
                Clipboard.getSystemClipboard().setContent(content);

                System.out.println("Đã copy " + selectedIndices.size() + " dòng ra clipboard");
            });

            Account account = AppState.getInstance().get("Account", Account.class);
            if (account.getRole().equals("BACKOFFICE") || account.getRole().equals("BUSINESS") || account.getRole().equals("ADMIN")) {
                menu.getItems().addAll(copy, addExCart, addImCart, addTransferCart, viewHistory);
            } else {
                menu.getItems().addAll(copy, viewHistory);
            }

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

    private void onHistory(String aid) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxmlBusiness/dialogHistoryBusiness.fxml"));
            Parent root = loader.load();
            DialogHistoryBusiness controller = loader.getController();
            controller.initData(
                    "SELECT * FROM vwct90 WHERE ma_vt = '" + aid + "' AND sl_nhap > 0 ORDER BY ngay_ct DESC",
                    "SELECT * FROM vwct70y WHERE ma_vt = '" + aid + "' AND sl_nhap > 0 ORDER BY ngay_ct DESC");
            Stage dialog = new Stage();
            dialog.setTitle("Lịch sử giao dịch");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.setResizable(true);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onCreateCartImport(String codeAID, Runnable callback, String typeCreate, String titleDialog,
            ProductBusiness productBusiness) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCreateCartImport.fxml"));

            Parent root = loader.load();

            DialogCreateCartImport controller = loader.getController();
            controller.setInitialData(callback, typeCreate, null, codeAID, productBusiness);

            Stage dialog = new Stage();
            dialog.setTitle(titleDialog);
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onCreateCartImportExport(String codeAID, Runnable callback, String typeCreate, String titleDialog,
            ProductBusiness productBusiness) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCreateCartImportExport.fxml"));

            Parent root = loader.load();
            DialogCreateCartImportExport controller = loader.getController();
            controller.setInitialData(callback, typeCreate, null, codeAID,
                    productBusiness);

            Stage dialog = new Stage();
            dialog.setTitle(titleDialog);
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onCreateCartExport(String codeAID, Runnable callback, String typeCreate, String titleDialog,
            ProductBusiness productBusiness) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCreateCartExport.fxml"));

            Parent root = loader.load();

            DialogCreateCartExport controller = loader.getController();
            controller.setInitialData(callback, typeCreate, null, codeAID, productBusiness);

            Stage dialog = new Stage();
            dialog.setTitle(titleDialog);
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onCreateCartTransfer(String codeAID, Runnable callback, String typeCreate, String titleDialog,
            ProductBusiness productBusiness) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCreateCartTransfer.fxml"));

            Parent root = loader.load();

            DialogCreateCartTransfer controller = loader.getController();
            controller.setInitialData(callback, "TRANSFER", null, codeAID, productBusiness);

            Stage dialog = new Stage();
            dialog.setTitle(titleDialog);
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onAddCartSynthetic(String codeAID, Runnable callback) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("fxml/dialogCreateCartSynthetic.fxml"));

            Parent root = loader.load();

            DialogCreateCartSynthetic controller = loader.getController();
            controller.setInitialData(callback, "SYNTHETIC", codeAID);

            Stage dialog = new Stage();
            dialog.setTitle("Nhập xuất tổng hợp");
            dialog.setScene(new Scene(root));
            // dialog.initModality(Modality.WINDOW_MODAL);
            // dialog.initOwner(Main.getPrimaryStage());
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final PseudoClass PC_HIGHLIGHT = PseudoClass.getPseudoClass("highlight");
}
