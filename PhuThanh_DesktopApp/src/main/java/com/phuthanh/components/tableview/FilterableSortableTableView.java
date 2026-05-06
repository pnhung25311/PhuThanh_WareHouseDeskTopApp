package com.phuthanh.components.tableview;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

import java.util.function.Predicate;

/**
 * Generic TableView với khả năng filter và sort
 * @param <T> Kiểu dữ liệu của object trong table
 */
public class FilterableSortableTableView<T> extends BorderPane {
    
    // Components
    private final TableView<T> tableView;
    private final ObservableList<T> masterData;
    private final FilteredList<T> filteredData;
    private final SortedList<T> sortedData;
    private final TextField searchField;
    private final ComboBox<String> filterColumnCombo;
    private final Label statusLabel;
    
    // Properties
    private final StringProperty searchText = new SimpleStringProperty("");
    private final ObjectProperty<Predicate<T>> customFilter = new SimpleObjectProperty<>(null);
    private String currentFilterColumn = "All";
    private Callback<T, String> customSearchExtractor;
    
    public FilterableSortableTableView() {
        this.masterData = FXCollections.observableArrayList();
        this.filteredData = new FilteredList<>(masterData, p -> true);
        this.sortedData = new SortedList<>(filteredData);
        this.tableView = new TableView<>();
        this.searchField = new TextField();
        this.filterColumnCombo = new ComboBox<>();
        this.statusLabel = new Label();
        
        setupUI();
        setupFiltering();
        setupSorting();
    }
    
    private void setupUI() {
        // Search Panel
        Label searchLabel = new Label("Search:");
        searchLabel.setStyle("-fx-font-weight: bold;");
        
        searchField.setPromptText("Type to search...");
        searchField.setPrefWidth(250);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        Label filterLabel = new Label("Filter by column:");
        filterLabel.setStyle("-fx-font-weight: bold;");
        
        filterColumnCombo.getItems().add("All");
        filterColumnCombo.setValue("All");
        filterColumnCombo.setPrefWidth(150);
        
        HBox searchBox = new HBox(10, searchLabel, searchField, filterLabel, filterColumnCombo);
        searchBox.setPadding(new Insets(10, 10, 10, 10));
        searchBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        
        // TableView
        tableView.setPlaceholder(new Label("No data available"));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        
        // Status bar
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
        statusLabel.setPadding(new Insets(5, 10, 5, 10));
        
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(searchBox, tableView, statusLabel);
        
        this.setCenter(mainContent);
        
        // Debounce cho search field
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            pause.setOnFinished(e -> {
                searchText.set(newVal.toLowerCase());
                applyFilter();
            });
            pause.playFromStart();
        });
        
        filterColumnCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentFilterColumn = newVal;
            applyFilter();
        });
    }
    
    private void setupFiltering() {
        searchText.addListener((obs, oldVal, newVal) -> applyFilter());
        customFilter.addListener((obs, oldVal, newVal) -> applyFilter());
    }
    
    private void applyFilter() {
        filteredData.setPredicate(item -> {
            // 1. Check custom filter
            if (customFilter.get() != null && !customFilter.get().test(item)) {
                return false;
            }
            
            // 2. Check search text
            String searchLower = searchText.get();
            if (searchLower == null || searchLower.isEmpty()) {
                return true;
            }
            
            // Handle different search modes
            if ("All".equals(currentFilterColumn)) {
                // Search through all text columns (using toString or custom extractor)
                if (customSearchExtractor != null) {
                    return customSearchExtractor.call(item).toLowerCase().contains(searchLower);
                }
                return item.toString().toLowerCase().contains(searchLower);
            } else {
                // Search specific column by extracting property value
                String value = extractColumnValue(item, currentFilterColumn);
                return value != null && value.toLowerCase().contains(searchLower);
            }
        });
        
        updateStatus();
    }
    
    private String extractColumnValue(T item, String columnName) {
        // Override this method or use customSearchExtractor
        if (customSearchExtractor != null) {
            return customSearchExtractor.call(item);
        }
        return item.toString();
    }
    
    private void setupSorting() {
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
    }
    
    private void updateStatus() {
        int filteredCount = filteredData.size();
        int totalCount = masterData.size();
        
        if (filteredCount == totalCount) {
            statusLabel.setText(String.format("Showing %d items", totalCount));
        } else {
            statusLabel.setText(String.format("Showing %d of %d items", filteredCount, totalCount));
        }
    }
    
    // ============ PUBLIC METHODS ============
    
    /**
     * Set dữ liệu cho table
     */
    public void setItems(ObservableList<T> items) {
        masterData.setAll(items);
        updateStatus();
    }
    
    /**
     * Thêm một item vào table
     */
    public void addItem(T item) {
        masterData.add(item);
        updateStatus();
    }
    
    /**
     * Xóa tất cả items
     */
    public void clearItems() {
        masterData.clear();
        updateStatus();
    }
    
    /**
     * Thêm column vào table
     */
    public void addColumn(TableColumn<T, ?> column) {
        tableView.getColumns().add(column);
        
        // Cập nhật dropdown filter
        if (!filterColumnCombo.getItems().contains(column.getText())) {
            filterColumnCombo.getItems().add(column.getText());
        }
    }
    
    /**
     * Thêm multiple columns
     */
    public void addColumns(@SuppressWarnings("unchecked") TableColumn<T, ?>... columns) {
        for (TableColumn<T, ?> column : columns) {
            addColumn(column);
        }
    }
    
    /**
     * Set custom search extractor (cho phép search custom logic)
     */
    public void setCustomSearchExtractor(Callback<T, String> extractor) {
        this.customSearchExtractor = extractor;
    }
    
    /**
     * Set custom filter predicate
     */
    public void setCustomFilter(Predicate<T> predicate) {
        this.customFilter.set(predicate);
    }
    
    /**
     * Clear filter
     */
    public void clearFilter() {
        searchField.clear();
        customFilter.set(null);
        filterColumnCombo.setValue("All");
    }
    
    /**
     * Lấy TableView (để custom thêm)
     */
    public TableView<T> getTableView() {
        return tableView;
    }
    
    /**
     * Lấy dữ liệu đã được sort và filter
     */
    public SortedList<T> getSortedData() {
        return sortedData;
    }
    
    /**
     * Lấy tất cả dữ liệu gốc
     */
    public ObservableList<T> getMasterData() {
        return masterData;
    }
    
    /**
     * Xóa column khỏi table
     */
    public void removeColumn(TableColumn<T, ?> column) {
        tableView.getColumns().remove(column);
        filterColumnCombo.getItems().remove(column.getText());
    }
}
