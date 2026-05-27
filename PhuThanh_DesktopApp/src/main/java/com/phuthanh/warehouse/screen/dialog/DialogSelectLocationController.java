package com.phuthanh.warehouse.screen.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;

import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.model.info.Location;

import java.util.List;
import java.util.stream.Collectors;

public class DialogSelectLocationController {

    @FXML
    private TextField txtSearch;

    @FXML
    private ListView<LocationItem> listView;

    @FXML
    private Button btnConfirm;

    @FXML
    private Button btnCancel;

    private Stage dialogStage;

    private String selectedIds = "";
    private String selectedNames = "";

    private ObservableList<LocationItem> masterList = FXCollections.observableArrayList();
    private FilteredList<LocationItem> filteredList;

    private String locationIDs;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
    private   final DbInfoHelper dbInfoHelper = new DbInfoHelper();

    @FXML
    public void initialize() {
        try {
            List<Location> locations = dbInfoHelper.getAllLocation();

            for (Location loc : locations) {
                masterList.add(new LocationItem(loc));
            }

            filteredList = new FilteredList<>(masterList, p -> true);
            listView.setItems(filteredList);

            // Hiển thị CheckBox trong ListView
            listView.setCellFactory(lv -> {
                CheckBoxListCell<LocationItem> cell = new CheckBoxListCell<>(LocationItem::selectedProperty);
                cell.setConverter(new javafx.util.StringConverter<LocationItem>() {
                    @Override
                    public String toString(LocationItem object) {
                        return object.getLocation().getNameLocation();
                    }

                    @Override
                    public LocationItem fromString(String string) {
                        return null;
                    }
                });
                return cell;
            });

            // Filter khi nhập text
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    String filter = newVal.toLowerCase().trim();
                    filteredList
                            .setPredicate(item -> item.getLocation().getNameLocation().toLowerCase().contains(filter));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // Button actions
            btnConfirm.setOnAction(e -> {
                try {
                    onConfirm();
                } catch (Exception ex) {
                    ex.printStackTrace(); // In ra nguyên nhân gốc
                }
            });

            btnCancel.setOnAction(e -> onCancel());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onConfirm() {
        List<LocationItem> selectedItems = masterList.stream()
                .filter(LocationItem::isSelected)
                .collect(Collectors.toList());

        selectedIds = selectedItems.stream()
                .map(item -> String.valueOf(item.getLocation().getLocationID()))
                .collect(Collectors.joining(","));

        selectedNames = selectedItems.stream()
                .map(item -> item.getLocation().getNameLocation())
                .collect(Collectors.joining(", \n"));

        dialogStage.close();
    }

    private void onCancel() {
        selectedIds = "";
        selectedNames = "";
        dialogStage.close();
    }

    public String getSelectedIds() {
        return selectedIds;
    }

    public String getSelectedNames() {
        return selectedNames;
    }

    public void initData(String LocationID) {
        this.locationIDs = LocationID;
        System.out.println("Init location ID: " + LocationID);
        System.out.println("Init location ID: " + locationIDs);
        if (LocationID != null && !LocationID.isEmpty()) {
            loadItem();
        }
    }

    private void loadItem() {
        if (locationIDs == null || locationIDs.isEmpty()) {
            return;
        }

        // Tách chuỗi locationIDs thành mảng ID
        String[] ids = locationIDs.split(",");

        // Trim và chuyển sang Integer
        List<Integer> idList = java.util.Arrays.stream(ids)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // Duyệt masterList, nếu ID trùng với idList thì setSelected = true
        for (LocationItem item : masterList) {
            if (idList.contains(item.getLocation().getLocationID())) {
                item.setSelected(true);
            } else {
                item.setSelected(false);
            }
        }
    }

    // Wrapper class cho ListView
    public   class LocationItem {
        private final Location location;
        private final javafx.beans.property.BooleanProperty selected = new javafx.beans.property.SimpleBooleanProperty(
                false);

        public LocationItem(Location location) {
            this.location = location;
        }

        public Location getLocation() {
            return location;
        }

        public javafx.beans.property.BooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }
    }
}
