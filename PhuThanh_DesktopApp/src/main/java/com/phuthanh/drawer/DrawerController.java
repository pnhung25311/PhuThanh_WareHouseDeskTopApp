package com.phuthanh.drawer;

import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.model.warehouse.DrawerItem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;

public class DrawerController {
    private final DbTableHelper dbTableHelper = new DbTableHelper();


    // ❗ Sửa ở đây: ListView<DrawerItem>, không phải String
    @FXML
    private ListView<DrawerItem> drawerListView;

    private DrawerActionListener listener;

    public void initialize() {
        // --- Lấy danh sách item từ SQL Server ---
        List<DrawerItem> drawerItems = getDrawerItemsFromDB();
        System.out.println("Drawer items from DB: " + drawerItems);

        // Chuyển danh sách sang ObservableList
        ObservableList<DrawerItem> observableItems = FXCollections.observableArrayList(drawerItems);
        drawerListView.setItems(observableItems);

        // --- Chọn phần tử đầu tiên sau khi load ---
        if (!observableItems.isEmpty()) {
            drawerListView.getSelectionModel().select(0);

            DrawerItem firstItem = observableItems.get(0);

            // Gọi listener để load dữ liệu tabview ngay từ đầu
            if (listener != null) {
                listener.onDrawerItemClicked(firstItem);
            }

            System.out.println("Auto selected: " + firstItem.getWareHouseID() + " - " + firstItem.getNameWareHouse());
        }

        // ✅ Hiển thị name nhưng giữ object DrawerItem
        drawerListView.setCellFactory(lv -> new ListCell<DrawerItem>() {
            @Override
            protected void updateItem(DrawerItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNameWareHouse());
                    setStyle("-fx-text-fill: #5f3bffff; -fx-font-size: 16px; -fx-font-weight: 600;");
                }
            }
        });

        // ✅ Khi click → gửi object ra listener
        drawerListView.setOnMouseClicked(event -> {
            DrawerItem selected = drawerListView.getSelectionModel().getSelectedItem();
            if (selected != null && listener != null) {
                System.out
                        .println("Clicked drawer: " + selected.getWareHouseID() + " - " + selected.getNameWareHouse());
                listener.onDrawerItemClicked(selected); // Gửi object, không gửi String
            }
        });
    }

    public void setListener(DrawerActionListener listener) {
        this.listener = listener;
    }

    // --- Lấy danh sách DrawerItem từ DB ---
    private List<DrawerItem> getDrawerItemsFromDB() {
        return dbTableHelper.getDrawerItemsFromDB();
    }
}
