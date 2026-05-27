package com.phuthanh.manager;

import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.model.warehouse.DrawerItem;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class SearchManager {
    private   final DbTableHelper dbTableHelper = new DbTableHelper();

    public void performSearch(TextField txtSearch, TableView<ObservableList<String>> table,
            DrawerItem selectedDrawerItem) {
        String keyword = txtSearch.getText().trim();

        if (keyword.isEmpty()) {
            // Nếu không có keyword, load lại bảng gốc theo drawer chọn
            if (selectedDrawerItem != null) {
                dbTableHelper.loadTable(table, selectedDrawerItem.getWareHouseTable());
            }
        } else {
            // Tìm kiếm trong dữ liệu đang có của TableView
            dbTableHelper.searchTable(table, keyword);
        }
    }
}
