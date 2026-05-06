package com.phuthanh.components.tableview;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.transformation.FilteredList;

import java.util.HashMap;
import java.util.Map;

public class FilterManager<T> {

    private final TableView<T> tableView;
    private final Map<TableColumn<T, ?>, FilterPopup<T>> filterPopups = new HashMap<>();

    public FilterManager(TableView<T> tableView) {
        this.tableView = tableView;

        // Ensure tableView uses FilteredList
        if (!(tableView.getItems() instanceof FilteredList)) {
            // Create filtered list wrapper
            @SuppressWarnings("unchecked")
            FilteredList<T> filteredList = new FilteredList<>(tableView.getItems(), p -> true);
            tableView.setItems(filteredList);
        }
    }

    public void enableExcelLikeFilter() {
        for (TableColumn<T, ?> column : tableView.getColumns()) {
            @SuppressWarnings("unchecked")
            TableColumn<T, ?> typedColumn = (TableColumn<T, ?>) column;
            FilterPopup<T> filter = new FilterPopup<>(typedColumn, tableView);
            filterPopups.put(typedColumn, filter);
        }
    }

    public void clearAllFilters() {
        for (FilterPopup<T> filter : filterPopups.values()) {
            filter.clearFilter();
        }
    }

    public void refreshFilters() {
        for (FilterPopup<T> filter : filterPopups.values()) {
            // Rebuild filters if needed
        }
    }

    public boolean hasActiveFilters() {
        for (FilterPopup<T> filter : filterPopups.values()) {
            if (filter.isFilterActive()) {
                return true;
            }
        }
        return false;
    }

    // Thêm method này vào FilterManager.java (optional)
    public void disableFilters() {
        for (FilterPopup<T> filter : filterPopups.values()) {
            filter.clearFilter();
        }
    }

    public void removeAllFilters() {
        filterPopups.clear();
    }

    // Thêm method để refresh unique values khi data thay đổi
    public void refreshAllFilters() {
        for (FilterPopup<T> filter : filterPopups.values()) {
            filter.refreshUniqueValues(); // Cần thêm method này trong FilterPopup
        }
    }
}