package com.phuthanh.helper;

import com.phuthanh.model.business.ProductBusiness;
import com.phuthanh.store.AppState;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

public class TabViewHelper {
    private   final SimpleStringProperty selectedAID = new SimpleStringProperty();
    private   final ObjectProperty<ProductBusiness> selectedProductBusiness = new SimpleObjectProperty<>();

    /**
     * Gán sự kiện click cho TableView
     * 
     * @param tableView TableView muốn gán
     */
    public void clickItemSaveAID(TableView<ObservableList<String>> tableView) {
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 1) {
                ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();
                if (selectedRow != null && !selectedRow.isEmpty()) {
                    selectedAID.set(selectedRow.get(0)); // ✅ Lưu phần tử đầu tiên
                    AppState.getInstance().set("selectedAID", selectedAID.get());
                    String _selectedAID = AppState.getInstance().get("selectedAID", String.class);
                    System.out.println("Đã chọn AID: " + _selectedAID);
                }
            }
        });
    }

    public void clickItemSaveAIDBusiness(TableView<ProductBusiness> tableView) {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {

                ProductBusiness selected = tableView.getSelectionModel().getSelectedItem();

                if (selected != null) {
                    selectedAID.set(selected.maVatTu); // ⭐ lấy từ model
                    selectedProductBusiness.set(selected);

                    AppState.getInstance().set("selectedAID", selectedAID.get());
                    AppState.getInstance().set("ProductBusinessSelected", selected);

                    String _selectedAID = AppState.getInstance().get("selectedAID", String.class);
                    System.out.println("Đã chọn AID: " + _selectedAID);
                }
            }
        });
    }

    // ✅ Hàm để lấy giá trị ở nơi khác
    public String getSelectedAID() {
        return selectedAID.get();
    }

    public ProductBusiness getSelectedProductBusiness() {
        return selectedProductBusiness.get();
    }
}
