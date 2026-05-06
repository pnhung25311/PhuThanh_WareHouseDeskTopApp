package com.phuthanh.custom;

import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.util.StringConverter;

public class CustomCombobox1 {

    /* =========================================================
       SETUP SEARCHABLE COMBOBOX (KHÔNG CÓ SELECTED ITEM)
       ========================================================= */
    public static <T> void setupComboBox(
            ComboBox<T> comboBox,
            List<T> items,
            IdExtractor<T> idExtractor,
            NameExtractor<T> nameExtractor) {

        ObservableList<T> list = FXCollections.observableArrayList(items);
        FilteredList<T> filteredItems = new FilteredList<>(list, p -> true);
        comboBox.setItems(filteredItems);

        comboBox.setEditable(true);
        comboBox.setVisibleRowCount(10);

        /* 🔥 biến ComboBox thành SearchBox */
        comboBox.setSelectionModel(null);
        comboBox.setValue(null);

        /* ⭐ FLAG để code cũ nhận diện */
        comboBox.getProperties().put("IS_SEARCH_COMBOBOX", true);

        /* render dropdown */
        comboBox.setCellFactory(lv -> {
            ListCell<T> cell = new ListCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : nameExtractor.getName(item));
                }
            };

            /* click item → chỉ fill text */
            cell.setOnMouseClicked(e -> {
                if (!cell.isEmpty()) {
                    e.consume();
                    comboBox.getEditor().setText(cell.getText());
                    comboBox.hide();
                }
            });

            return cell;
        });

        /* không hiển thị selected item */
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
            }
        });

        comboBox.setConverter(new StringConverter<T>() {
            @Override public String toString(T object) { return ""; }
            @Override public T fromString(String string) { return null; }
        });

        /* FILTER khi gõ */
        comboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal.toLowerCase().trim();

            filteredItems.setPredicate(item -> {
                if (filter.isEmpty()) return true;
                return nameExtractor.getName(item).toLowerCase().contains(filter);
            });

            if (!comboBox.isShowing()) comboBox.show();
            fixPopupHeight(comboBox);
        });

        /* chặn ENTER commit */
        comboBox.getEditor().setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER -> {
                    e.consume();
                    comboBox.hide();
                }
                default -> {}
            }
        });
    }

    /* ========================================================= */
    public static <T> String getText(ComboBox<T> cb) {
        return cb.getEditor().getText().trim();
    }

    public static <T> void clear(ComboBox<T> cb) {
        cb.getEditor().clear();
    }

    public static <T> void setTextById(
            ComboBox<T> cb,
            List<T> items,
            int id,
            IdExtractor<T> idExtractor,
            NameExtractor<T> nameExtractor) {

        for (T item : items) {
            if (idExtractor.getId(item) == id) {
                cb.getEditor().setText(nameExtractor.getName(item));
                return;
            }
        }
    }

    public static <T> Optional<T> findItemByText(
            ComboBox<T> cb,
            List<T> items,
            NameExtractor<T> nameExtractor) {

        String text = getText(cb).toLowerCase();

        return items.stream()
                .filter(i -> nameExtractor.getName(i).toLowerCase().equals(text))
                .findFirst();
    }

    /* fix bug popup JavaFX */
    @SuppressWarnings("unchecked")
    private static <T> void fixPopupHeight(ComboBox<T> comboBox) {
        Platform.runLater(() -> {
            if (comboBox.getSkin() instanceof ComboBoxListViewSkin<?> skin) {
                ListView<T> listView = (ListView<T>) skin.getPopupContent();
                listView.refresh();
                listView.requestLayout();
            }
        });
    }

    public interface IdExtractor<T> {
        int getId(T item);
    }

    public interface NameExtractor<T> {
        String getName(T item);
    }
}