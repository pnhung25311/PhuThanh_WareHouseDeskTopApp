package com.phuthanh.custom;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.text.Normalizer;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;

public class SearchableComboBox<T> extends ComboBox<T> {

    private ObservableList<T> originalItems;
    private FilteredList<T> filteredItems;

    private boolean isAdjusting = false;
    private boolean isSelecting = false;
    private boolean firstOpen = true;

    private StringConverter<T> converter;

    // ❌ KHÔNG override setItems nữa
    public void setSearchableItems(List<T> items, StringConverter<T> converter) {
        System.out.println("====call");
        this.converter = converter;
        setConverter(converter);
        setEditable(true);

        originalItems = FXCollections.observableArrayList(items);
        filteredItems = new FilteredList<>(originalItems, s -> true);

        // dùng setItems của ComboBox gốc
        super.setItems(filteredItems);

        TextField editor = getEditor();

        // gõ -> filter
        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            safeFilter();
            System.out.println(newVal + "kkk");
        });

        // mở dropdown lần đầu -> filter
        showingProperty().addListener((obs, oldVal, isShowing) -> {
            if (isShowing && firstOpen) {
                firstOpen = false;
                safeFilter();
            }
        });

        setOnAction(e -> {
            T value = getValue();
            System.out.println("User picked: " + value);
        });

        // ⭐ FIX QUAN TRỌNG: khi chọn item phải commit value
        getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // System.out.println(newVal.toString() + "=======================");
            System.out.println(newVal + " SELECTED");

            if (newVal == null)
                return;

            isSelecting = true;
            Platform.runLater(() -> {
                // commit value cho ComboBox editable
                setValue(newVal);
                editor.setText(converter.toString(newVal));
                // editor.positionCaret(getEditor().getText().length());
                isSelecting = false;
                System.out.println(newVal + "=======================");
            });
        });
        setCellFactory(lv -> {
            javafx.scene.control.ListCell<T> cell = new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : converter.toString(item));
                }
            };

            // ⭐ QUAN TRỌNG NHẤT: force select khi click item
            cell.setOnMousePressed(e -> {
                if (!cell.isEmpty()) {
                    T item = cell.getItem();
                    System.out.println(item);
                    getSelectionModel().select(item); // ← đây là thứ bị thiếu
                    setValue(item);
                    hide();
                }
            });

            return cell;
        });

    }

    private void safeFilter() {
        if (isAdjusting || isSelecting)
            return;
        Platform.runLater(this::filter);
    }

    private void filter() {
        if (isAdjusting || isSelecting)
            return;
        isAdjusting = true;

        String keyword = normalize(getEditor().getText());

        filteredItems.setPredicate(item -> normalize(converter.toString(item)).contains(keyword));

        if (!filteredItems.isEmpty())
            show();

        isAdjusting = false;
    }

    private String normalize(String s) {
        if (s == null)
            return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }
}