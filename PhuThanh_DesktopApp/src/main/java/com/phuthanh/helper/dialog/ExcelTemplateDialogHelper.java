package com.phuthanh.helper.dialog;

import java.time.LocalDate;
// import java.time.ZoneId;
// import java.util.Date;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
// import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
// import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;

import com.phuthanh.model.enums.ExcelTemplateType;
import com.phuthanh.model.helper.ModelExportExcelCart;

public class ExcelTemplateDialogHelper {
    public  Optional<ModelExportExcelCart> chooseTemplateDialog() {

        Dialog<ModelExportExcelCart> dialog = new Dialog<>();
        dialog.setTitle("Xuất Excel");
        dialog.setHeaderText("Chọn thông tin xuất file Excel");

        ButtonType okButtonType = new ButtonType("Xuất Excel", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // ===== UI =====
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        DatePicker datePicker = new DatePicker(LocalDate.now());

        ComboBox<ExcelTemplateType> cbTemplate = new ComboBox<>();
        cbTemplate.getItems().addAll(ExcelTemplateType.values());
        cbTemplate.setPrefWidth(260);
        cbTemplate.getSelectionModel().selectFirst(); // chọn mặc định

        grid.add(new Label("Chọn ngày:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Biểu mẫu:"), 0, 1);
        grid.add(cbTemplate, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // ===== VALIDATE không cho bấm OK nếu thiếu =====
        Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        okButton.disableProperty().bind(
                datePicker.valueProperty().isNull()
                        .or(cbTemplate.valueProperty().isNull()));

        // ===== Convert result -> Model =====
        dialog.setResultConverter(button -> {
            if (button == okButtonType) {
                ModelExportExcelCart model = new ModelExportExcelCart();

                LocalDate localDate = datePicker.getValue();
                // Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                model.setDate(localDate);
                model.setExcelTemplateType(cbTemplate.getValue());

                return model;
            }
            return null;
        });

        return dialog.showAndWait();
    }
}