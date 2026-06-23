package com.phuthanh.helper;

import com.phuthanh.model.business.ProductBusiness;
import com.phuthanh.store.AppState;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

public class TabViewHelper {
    
    private final SimpleStringProperty selectedAID = new SimpleStringProperty();
    private final ObjectProperty<ProductBusiness> selectedProductBusiness = new SimpleObjectProperty<>();

    /**
     * Gán sự kiện click cho TableView (Dữ liệu động ObservableList dạng Chuỗi)
     * Đã tối ưu chống ghi đè Handler và chống lỗi sai lệch khi kéo thả vị trí cột.
     */
    public void clickItemSaveAID(TableView<ObservableList<String>> tableView) {
        if (tableView == null) return;

        // TỐI ƯU AN TOÀN: Sử dụng addEventHandler để gom hành vi click, KHÔNG dùng setOnMouseClicked gây ghi đè/mất logic cũ
        tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 1) {
                ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();
                if (selectedRow != null && !selectedRow.isEmpty()) {
                    
                    // SỬA LỖI ĐỔI THỨ TỰ CỘT: Tìm chỉ mục thực tế của cột mang định danh "ProductAID" hoặc "AID"
                    int aidIndex = findRealColumnIndexById(tableView, "AID");
                    if (aidIndex == -1) {
                        aidIndex = findRealColumnIndexById(tableView, "AID"); // Dự phòng trường hợp viết tắt
                    }
                    
                    // Nếu không tìm thấy cột theo ID, fallback an toàn về vị trí đầu tiên (0)
                    if (aidIndex == -1 || aidIndex >= selectedRow.size()) {
                        aidIndex = 0;
                    }

                    String aidValue = selectedRow.get(aidIndex);
                    if (aidValue != null) {
                        aidValue = aidValue.trim();
                        selectedAID.set(aidValue);
                        
                        // Đẩy lên Reactive State Management tập trung
                        AppState.getInstance().set("selectedAID", aidValue);
                        System.out.println("🎯 [Dynamic Table] Đã chọn AID thực tế: " + aidValue);
                    }
                }
            }
        });
    }

    /**
     * Gán sự kiện click cho TableView (Dữ liệu dạng Object Model cụ thể)
     */
    public void clickItemSaveAIDBusiness(TableView<ProductBusiness> tableView) {
        if (tableView == null) return;

        tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 1) {
                ProductBusiness selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selectedAID.set(selected.maVatTu);
                    selectedProductBusiness.set(selected);

                    AppState.getInstance().set("selectedAID", selected.maVatTu);
                    AppState.getInstance().set("ProductBusinessSelected", selected);

                    System.out.println("🎯 [Model Table] Đã chọn AID: " + selected.maVatTu);
                }
            }
        });
    }

    /**
     * GIẢI PHÁP SỬA LỖI UI: 
     * Duyệt qua danh sách cấu trúc gốc để tìm chính xác index vị trí dữ liệu trong Database, 
     * bất chấp việc người dùng kéo thả, đổi thứ tự hiển thị của các cột trên màn hình JavaFX.
     */
    private int findRealColumnIndexById(TableView<ObservableList<String>> table, String targetId) {
        ObservableList<TableColumn<ObservableList<String>, ?>> columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            TableColumn<?, ?> col = columns.get(i);
            // Kiểm tra qua ID cột được gán từ DbTableHelper.createColumns()
            if (col.getId() != null && col.getId().equalsIgnoreCase(targetId)) {
                return i;
            }
            // Dự phòng: Kiểm tra qua tiêu đề hiển thị nếu dev quên gán ID cột
            if (col.getText() != null && (col.getText().equalsIgnoreCase("Mã Vật Tư") || col.getText().equalsIgnoreCase("Mã AID"))) {
                return i;
            }
        }
        return -1;
    }

    /* ================= GETTER TỐI ƯU ================= */

    public String getSelectedAID() {
        return selectedAID.get();
    }

    public SimpleStringProperty selectedAIDProperty() {
        return selectedAID;
    }

    public ProductBusiness getSelectedProductBusiness() {
        return selectedProductBusiness.get();
    }

    public ObjectProperty<ProductBusiness> selectedProductBusinessProperty() {
        return selectedProductBusiness;
    }
}