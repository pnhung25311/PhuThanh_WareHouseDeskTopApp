package com.phuthanh.custom;

import java.util.List;

import org.controlsfx.control.textfield.TextFields;

// import com.phuthanh.custom.CustomCombobox.IdExtractor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
// import javafx.css.converter.StringConverter;
import javafx.scene.control.ComboBox;
// import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

public class CustomCombobox {

    public static <T> void setupComboBox(
            ComboBox<T> comboBox,
            List<T> items,
            IdExtractor<T> idExtractor,
            NameExtractor<T> nameExtractor) {

        ObservableList<T> list = FXCollections.observableArrayList(items);
        FilteredList<T> filteredItems = new FilteredList<>(list, p -> true);
        // comboBox.setItems(list);
        comboBox.setItems(filteredItems);
        comboBox.setEditable(true);

        // Hiển thị NAME nhưng giữ OBJECT
        // comboBox.setCellFactory(lv -> new ListCell<>() {
        // @Override
        // protected void updateItem(T item, boolean empty) {
        // super.updateItem(item, empty);
        // setText(empty || item == null ? null : nameExtractor.getName(item));
        // }
        // });

        // comboBox.setButtonCell(new ListCell<>() {
        // @Override
        // protected void updateItem(T item, boolean empty) {
        // super.updateItem(item, empty);
        // setText(empty || item == null ? null : nameExtractor.getName(item));
        // }
        // });
        TextFields.bindAutoCompletion(comboBox.getEditor(), comboBox.getItems());

        // Converter để editable ComboBox không crash
        comboBox.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : nameExtractor.getName(object);
            }

            @Override
            public T fromString(String string) {
                // Nếu người dùng gõ text → trả về item hiện tại, không crash
                return comboBox.getValue();
            }
        });

        // comboBox.setOnAction(event -> {
        // T selected = comboBox.getSelectionModel().getSelectedItem();
        // if (selected != null) {
        // // System.out.println("Selected ID: " + idExtractor.getId(selected));
        // }
        // });

        // Cho phép nhập chữ để filter
        // comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) ->
        // {
        // final String filter = newValue.toLowerCase().trim();

        // filteredItems.setPredicate(item -> {
        // if (filter.isEmpty())
        // return true;
        // return nameExtractor.getName(item).toLowerCase().contains(filter);
        // });

        // comboBox.show(); // tự mở dropdown khi gõ
        // });

        // Khi chọn một item
        // comboBox.valueProperty().addListener((obs, old, selected) -> {
        // if (selected != null) {
        // // System.out.println("Selected ID: " + idExtractor.getId(selected));
        // }
        // });
    }

    public interface IdExtractor<T> {
        int getId(T item);
    }

    public interface NameExtractor<T> {
        String getName(T item);
    }

    public <T> void clearComboBox(ComboBox<T> cb) {
        cb.getSelectionModel().clearSelection(); // xoá selected index
        cb.setValue(null); // xoá value
        cb.getEditor().clear(); // xoá text editor (rất quan trọng)
    }

    public static <T> ComboBox<T> create(
            List<T> items,
            NameExtractor<T> nameExtractor) {

        ComboBox<T> comboBox = new ComboBox<>();

        ObservableList<T> list = FXCollections.observableArrayList(items);
        FilteredList<T> filtered = new FilteredList<>(list, p -> true);

        comboBox.setItems(filtered);
        comboBox.setEditable(true);

        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : nameExtractor.getName(object);
            }

            @Override
            public T fromString(String string) {
                return comboBox.getValue();
            }
        });

        comboBox.getEditor().textProperty().addListener((obs, old, text) -> {

            javafx.application.Platform.runLater(() -> {
                filtered.setPredicate(item -> {
                    if (text == null || text.isEmpty())
                        return true;

                    String name = nameExtractor.getName(item);
                    return name != null &&
                            name.toLowerCase().contains(text.toLowerCase());
                });

                if (!comboBox.isShowing()) {
                    comboBox.show();
                }
            });
        });

        return comboBox;
    }

}
