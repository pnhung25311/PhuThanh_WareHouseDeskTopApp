package com.phuthanh.components.tableview;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class TableColumnHelper {
    
    /**
     * Tạo column đơn giản với StringProperty
     */
    public <T> TableColumn<T, String> createStringColumn(
            String title, 
            Callback<T, String> propertyExtractor) {
        
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> {
            String value = propertyExtractor.call(cellData.getValue());
            return new SimpleStringProperty(value);
        });
        return column;
    }
    
    /**
     * Tạo column với custom cell factory
     */
    public <T, U> TableColumn<T, U> createColumn(
            String title,
            Callback<T, U> propertyExtractor) {
        
        TableColumn<T, U> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> {
            U value = propertyExtractor.call(cellData.getValue());
            return new SimpleObjectProperty<>(value);
        });
        return column;
    }
    
    /**
     * Tạo column với style tùy chỉnh
     */
    public <T> TableColumn<T, String> createStyledStringColumn(
            String title,
            Callback<T, String> propertyExtractor,
            String styleClass) {
        
        TableColumn<T, String> column = createStringColumn(title, propertyExtractor);
        column.setCellFactory(tc -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().remove(styleClass);
                } else {
                    setText(item);
                    if (!getStyleClass().contains(styleClass)) {
                        getStyleClass().add(styleClass);
                    }
                }
            }
        });
        return column;
    }
}
