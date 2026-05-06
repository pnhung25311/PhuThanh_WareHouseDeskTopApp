package com.phuthanh.components.tableview;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.beans.value.ObservableValue;

import java.util.*;
import java.util.function.Predicate;

public class FilterPopup<T> {

    private final TableColumn<T, ?> column;
    private final TableView<T> tableView;
    private final ContextMenu contextMenu;
    private final ObservableList<String> uniqueValues = FXCollections.observableArrayList();
    private Predicate<T> currentFilter = t -> true;
    private final Set<String> selectedValues = new HashSet<>();
    private Label filterIcon;

    public FilterPopup(TableColumn<T, ?> column, TableView<T> tableView) {
        this.column = column;
        this.tableView = tableView;
        this.contextMenu = new ContextMenu();

        buildFilterMenu();
        addFilterIconToHeader();
    }

    private void addFilterIconToHeader() {
        column.setGraphic(null);

        Label columnLabel = new Label(column.getText());
        filterIcon = new Label(" ▼");
        filterIcon.setStyle("-fx-text-fill: #666; -fx-font-size: 10px; -fx-cursor: hand;");

        HBox headerBox = new HBox(5, columnLabel, filterIcon);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        column.setGraphic(headerBox);

        filterIcon.setOnMouseClicked(e -> showFilterMenu());
        columnLabel.setOnMouseClicked(e -> sortColumn());
    }

    private void buildFilterMenu() {
        contextMenu.getItems().clear();

        Label headerLabel = new Label("Filter: " + column.getText());
        headerLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(200);

        ListView<String> valueListView = new ListView<>();
        valueListView.setPrefHeight(250);
        valueListView.setPrefWidth(220);
        valueListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Button selectAllBtn = new Button("Select All");
        Button clearAllBtn = new Button("Clear All");
        Button okBtn = new Button("OK");
        Button cancelBtn = new Button("Cancel");

        HBox buttonBox = new HBox(10, selectAllBtn, clearAllBtn);
        buttonBox.setAlignment(Pos.CENTER);

        HBox actionBox = new HBox(10, okBtn, cancelBtn);
        actionBox.setAlignment(Pos.CENTER);

        VBox content = new VBox(10, headerLabel, searchField, valueListView, buttonBox, actionBox);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: white; -fx-border-color: #ccc;");

        CustomMenuItem menuItem = new CustomMenuItem(content, false);
        contextMenu.getItems().add(menuItem);

        loadUniqueValues();
        valueListView.setItems(uniqueValues);

        // Select all initially
        for (int i = 0; i < uniqueValues.size(); i++) {
            valueListView.getSelectionModel().select(i);
        }
        selectedValues.clear();
        selectedValues.addAll(uniqueValues);

        // Search filter
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                valueListView.setItems(uniqueValues);
            } else {
                ObservableList<String> filtered = FXCollections.observableArrayList();
                for (String val : uniqueValues) {
                    if (val != null && val.toLowerCase().contains(newVal.toLowerCase())) {
                        filtered.add(val);
                    }
                }
                valueListView.setItems(filtered);
            }
        });

        selectAllBtn.setOnAction(e -> {
            for (int i = 0; i < valueListView.getItems().size(); i++) {
                valueListView.getSelectionModel().select(i);
            }
            updateSelectedValues(valueListView);
            updateFilter();
        });

        clearAllBtn.setOnAction(e -> {
            valueListView.getSelectionModel().clearSelection();
            selectedValues.clear();
            updateFilter();
        });

        okBtn.setOnAction(e -> {
            updateSelectedValues(valueListView);
            updateFilter();
            contextMenu.hide();
        });

        cancelBtn.setOnAction(e -> contextMenu.hide());

        valueListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            updateSelectedValues(valueListView);
            updateFilter();
        });
    }

    private void updateSelectedValues(ListView<String> listView) {
        selectedValues.clear();
        selectedValues.addAll(listView.getSelectionModel().getSelectedItems());
    }

    private void loadUniqueValues() {
        uniqueValues.clear();
        Set<String> values = new HashSet<>();

        for (T item : tableView.getItems()) {
            String value = getCellValueAsString(item);
            if (value != null && !value.isEmpty()) {
                values.add(value);
            } else {
                values.add("(blank)");
            }
        }

        ObservableList<String> sortedValues = FXCollections.observableArrayList(values);
        FXCollections.sort(sortedValues);
        uniqueValues.addAll(sortedValues);
    }

    // FIXED: Use raw type to avoid generic issues
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String getCellValueAsString(T item) {
        try {
            if (column.getCellValueFactory() != null) {
                // Create CellDataFeatures with raw type
                TableColumn.CellDataFeatures cellDataFeatures = new TableColumn.CellDataFeatures(tableView, column,
                        item);

                // Use raw type callback
                Callback callback = column.getCellValueFactory();
                ObservableValue<?> observable = (ObservableValue<?>) callback.call(cellDataFeatures);

                if (observable != null) {
                    Object value = observable.getValue();
                    return value != null ? value.toString() : "";
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }

    private void updateFilter() {
        if (selectedValues.isEmpty()) {
            currentFilter = t -> true;
        } else {
            currentFilter = item -> {
                String value = getCellValueAsString(item);
                if (value == null || value.isEmpty()) {
                    value = "(blank)";
                }
                return selectedValues.contains(value);
            };
        }

        applyFilter();
        updateFilterIcon();
    }

    @SuppressWarnings("unchecked")
    private void applyFilter() {
        if (tableView.getItems() instanceof FilteredList) {
            FilteredList<T> filteredData = (FilteredList<T>) tableView.getItems();
            filteredData.setPredicate(currentFilter);
        }
    }

    private void updateFilterIcon() {
        if (filterIcon != null) {
            boolean hasActiveFilter = selectedValues.size() != uniqueValues.size() && !selectedValues.isEmpty();
            if (hasActiveFilter) {
                filterIcon.setStyle(
                        "-fx-text-fill: #2196F3; -fx-font-size: 10px; -fx-cursor: hand; -fx-font-weight: bold;");
                filterIcon.setText(" 🔍");
            } else {
                filterIcon.setStyle("-fx-text-fill: #666; -fx-font-size: 10px; -fx-cursor: hand;");
                filterIcon.setText(" ▼");
            }
        }
    }

    private void showFilterMenu() {
        loadUniqueValues();
        contextMenu.show(tableView, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void sortColumn() {
        tableView.getSortOrder().clear();
        tableView.getSortOrder().add(column);
        column.setSortType(TableColumn.SortType.ASCENDING);
    }

    public void clearFilter() {
        selectedValues.clear();
        selectedValues.addAll(uniqueValues);
        updateFilter();
    }

    public boolean isFilterActive() {
        return selectedValues.size() != uniqueValues.size() && !selectedValues.isEmpty();
    }

    // Thêm vào FilterPopup.java
    public void refreshUniqueValues() {
        loadUniqueValues();
        // Reset selection
        selectedValues.clear();
        selectedValues.addAll(uniqueValues);
        updateFilter();
    }
}