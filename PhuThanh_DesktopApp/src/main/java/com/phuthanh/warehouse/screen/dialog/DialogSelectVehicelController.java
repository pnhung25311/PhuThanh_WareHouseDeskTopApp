package com.phuthanh.warehouse.screen.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;

import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.model.info.Vehicle;

import java.util.List;
import java.util.stream.Collectors;

public class DialogSelectVehicelController {

    @FXML
    private TextField txtSearch;

    @FXML
    private ListView<VehicleTypeItem> listView;

    @FXML
    private Button btnConfirm;

    @FXML
    private Button btnCancel;

    private Stage dialogStage;

    private String selectedIds = "";
    private String selectedNames = "";

    private ObservableList<VehicleTypeItem> masterList = FXCollections.observableArrayList();
    private FilteredList<VehicleTypeItem> filteredList;

    private String vehicelIDs;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
    private   final DbInfoHelper dbInfoHelper = new DbInfoHelper();

    @FXML
    public void initialize() {
        try {
            List<Vehicle> vehicels = dbInfoHelper.getAllVehicels();

            for (Vehicle loc : vehicels) {
                masterList.add(new VehicleTypeItem(loc));
            }

            filteredList = new FilteredList<>(masterList, p -> true);
            listView.setItems(filteredList);

            // Hiển thị CheckBox trong ListView
            listView.setCellFactory(lv -> {
                CheckBoxListCell<VehicleTypeItem> cell = new CheckBoxListCell<>(VehicleTypeItem::selectedProperty);
                cell.setConverter(new javafx.util.StringConverter<VehicleTypeItem>() {
                    @Override
                    public String toString(VehicleTypeItem object) {
                        return object.getVehicel().getVehicleTypeName();
                    }

                    @Override
                    public VehicleTypeItem fromString(String string) {
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
                            .setPredicate(item -> item.getVehicel().getVehicleTypeName().toLowerCase().contains(filter));
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
        List<VehicleTypeItem> selectedItems = masterList.stream()
                .filter(VehicleTypeItem::isSelected)
                .collect(Collectors.toList());

        selectedIds = selectedItems.stream()
                .map(item -> String.valueOf(item.getVehicel().getVehicleID()))
                .collect(Collectors.joining(","));

        selectedNames = selectedItems.stream()
                .map(item -> item.getVehicel().getVehicleTypeName())
                .collect(Collectors.joining(", \n"));

        dialogStage.close();
    }

    private void onCancel() {
        // selectedIds = "";
        selectedNames = "";
        dialogStage.close();
    }

    public String getSelectedIds() {
        return selectedIds;
    }

    public String getSelectedNames() {
        return selectedNames;
    }

    public void initData(String vehicelID) {
        this.vehicelIDs = vehicelID;
        System.out.println("Init location ID: " + vehicelIDs);
        System.out.println("Init location ID: " + vehicelID);
        if (vehicelID != null && !vehicelID.isEmpty()) {
            loadItem();
        }
    }

    private void loadItem() {
        if (vehicelIDs == null || vehicelIDs.isEmpty()) {
            return;
        }

        // Tách chuỗi locationIDs thành mảng ID
        String[] ids = vehicelIDs.split(",");

        // Trim và chuyển sang Integer
        List<Integer> idList = java.util.Arrays.stream(ids)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // Duyệt masterList, nếu ID trùng với idList thì setSelected = true
        for (VehicleTypeItem item : masterList) {
            if (idList.contains(item.getVehicel().getVehicleID())) {
                item.setSelected(true);
            } else {
                item.setSelected(false);
            }
        }
    }

    // Wrapper class cho ListView
    public static  class VehicleTypeItem {
        private final Vehicle vehicle;
        private final javafx.beans.property.BooleanProperty selected = new javafx.beans.property.SimpleBooleanProperty(
                false);

        public VehicleTypeItem(Vehicle vehicle) {
            this.vehicle = vehicle;
        }

        public Vehicle getVehicel() {
            return vehicle;
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
