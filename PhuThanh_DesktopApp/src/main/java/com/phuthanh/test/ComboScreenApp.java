package com.phuthanh.test;

import java.util.List;
import java.util.Locale;

import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.model.info.Supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ComboScreenApp {

    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();

    public void show() {

        Stage stage = new Stage();

        Label title = new Label("Chọn nhà cung cấp");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        // ===== COMBOBOX =====
        ComboBox<Supplier> combo = new ComboBox<>();
        combo.setPrefWidth(250);
        combo.setPromptText("Gõ để tìm supplier...");
        combo.setEditable(true);

        // load dữ liệu
        List<Supplier> suppliers = dbInfoHelper.getAllSuppliers();
        ObservableList<Supplier> originalList = FXCollections.observableArrayList(suppliers);
        FilteredList<Supplier> filteredList = new FilteredList<>(originalList, s -> true);
        combo.setItems(filteredList);

        // hiển thị name thay vì toString()
        combo.setConverter(new StringConverter<Supplier>() {
            @Override
            public String toString(Supplier s) {
                return s == null ? "" : s.getName();
            }
            @Override
            public Supplier fromString(String string) { return null; }
        });

        TextField editor = combo.getEditor();
        boolean[] isTyping = {false}; // flag chống crash

        // ===== CHẶN SPACE + ENTER KHÔNG CHO AUTO SELECT =====
        editor.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if ((e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) && isTyping[0]) {
                e.consume();
            }
        });

        // ===== USER BẮT ĐẦU GÕ =====
        editor.setOnKeyPressed(e -> isTyping[0] = true);

        // ===== SEARCH REALTIME =====
        editor.textProperty().addListener((obs, oldVal, newVal) -> {

            if (!isTyping[0]) return;

            String search = newVal.toLowerCase(Locale.ROOT);

            filteredList.setPredicate(s -> {
                if (search.isEmpty()) return true;
                return s.getName().toLowerCase(Locale.ROOT).contains(search);
            });

            combo.show();
        });

        // ===== USER CHỌN ITEM =====
        combo.setOnAction(e -> {
            Supplier selected = combo.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            isTyping[0] = false;              // ⭐ quan trọng
            editor.setText(selected.getName()); // hiển thị text
        });

        // ===== LISTENER DEBUG =====
        combo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("===== SELECTED =====");
                System.out.println("ID   = " + newVal.getSupplierID());
                System.out.println("Name = " + newVal.getName());
                System.out.println("====================");
            }
        });

        // ===== BUTTON TEST =====
        Button btnGet = new Button("Lấy supplier đã chọn");
        Label resultLabel = new Label();

        btnGet.setOnAction(e -> {
            Supplier s = combo.getValue();
            if (s == null) resultLabel.setText("Chưa chọn!");
            else resultLabel.setText("Bạn đã chọn: " + s.getName());
        });

        VBox root = new VBox(15, title, combo, btnGet, resultLabel);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 420, 260));
        stage.setTitle("ComboBox Search Demo");
        stage.show();
    }
}