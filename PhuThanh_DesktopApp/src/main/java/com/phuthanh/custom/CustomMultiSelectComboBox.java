package com.phuthanh.custom;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ComboBox;
import javafx.util.Callback;

public class CustomMultiSelectComboBox<T> extends ComboBox<T> {
    private final ObservableList<T> selectedItems = FXCollections.observableArrayList();

    public CustomMultiSelectComboBox() {
        super();

        // Hiển thị checkbox trong dropdown
        setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListCell<T> call(ListView<T> param) {
                return new ListCell<T>() {
                    private final CheckBox checkBox = new CheckBox();

                    {
                        checkBox.setOnAction(event -> {
                            T item = getItem();
                            if (item != null) {
                                if (checkBox.isSelected()) {
                                    if (!selectedItems.contains(item))
                                        selectedItems.add(item);
                                } else {
                                    selectedItems.remove(item);
                                }
                                updateComboDisplay();
                            }
                        });
                    }

                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            checkBox.setText(item.toString());
                            checkBox.setSelected(selectedItems.contains(item));
                            setGraphic(checkBox);
                        }
                    }
                };
            }
        });

        // Không cho phép chọn 1 item mặc định như combo thường
        setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                updateComboDisplay();
            }
        });
    }

    private void updateComboDisplay() {
        if (selectedItems.isEmpty()) {
            setPromptText("Chọn mục...");
        } else {
            setPromptText(String.join(", ",
                    selectedItems.stream().map(Object::toString).toList()));
        }
    }

    public ObservableList<T> getSelectedItems() {
        return selectedItems;
    }
}
