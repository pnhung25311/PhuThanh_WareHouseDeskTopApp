package com.phuthanh.helper.dialog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.phuthanh.model.helper.ColumnConfig;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfigDialog extends Stage {
    private HBox container;
    private ObservableList<ColumnConfig> observableList;
    // private String configFileName;
    private final String FOLDER_NAME = "config";

    public ConfigDialog() {
        // Set modality once during construction
        this.initModality(Modality.APPLICATION_MODAL);
    }

    public void showDialog(String fileName, List<ColumnConfig> dbColumns) {
        // this.configFileName = fileName;
        this.setTitle("Tùy chỉnh cột");

        List<ColumnConfig> sortedList = loadSortedList(fileName, dbColumns);
        this.observableList = FXCollections.observableArrayList(sortedList);

        container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(10));
        renderItems();

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setPrefHeight(100);
        scrollPane.setFitToHeight(true);

        Button btnSave = new Button("Lưu cấu hình");
        btnSave.setOnAction(e -> {
            saveToFile(fileName, observableList);
            this.close();
        });

        VBox root = new VBox(10, new Label("Kéo thả ngang để sắp xếp:"), scrollPane, btnSave);
        root.setPadding(new Insets(15));
        this.setScene(new Scene(root, 600, 200));
        this.showAndWait();
    }

    private void renderItems() {
        container.getChildren().clear();
        for (ColumnConfig config : observableList) {
            Label label = new Label(config.getLabel());
            label.setStyle(
                    "-fx-border-color: #999; -fx-padding: 8px; -fx-background-color: #f0f0f0; -fx-cursor: hand;");

            // Kéo
            label.setOnDragDetected(event -> {
                Dragboard db = label.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(config.getId());
                db.setContent(cc);
                event.consume();
            });

            // Thả
            label.setOnDragOver(event -> {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            });

            label.setOnDragDropped(event -> {
                String draggedId = event.getDragboard().getString();
                ColumnConfig draggedItem = observableList.stream()
                        .filter(c -> c.getId().equals(draggedId)).findFirst().orElse(null);

                if (draggedItem != null) {
                    int targetIndex = container.getChildren().indexOf(label);
                    observableList.remove(draggedItem);
                    observableList.add(targetIndex, draggedItem);
                    renderItems(); // Vẽ lại HBox với thứ tự mới
                }
                event.setDropCompleted(true);
                event.consume();
            });
            container.getChildren().add(label);
        }
    }

    private List<ColumnConfig> loadSortedList(String fileName, List<ColumnConfig> dbColumns) {
        // Tạo đường dẫn: config/tên_file
        Path path = Paths.get(FOLDER_NAME, fileName);

        if (!Files.exists(path))
            return new ArrayList<>(dbColumns);

        try {
            List<String> savedIds = Files.readAllLines(path);
            // ... (phần code xử lý danh sách giữ nguyên như cũ)
            List<ColumnConfig> result = new ArrayList<>();
            for (String id : savedIds) {
                dbColumns.stream().filter(c -> c.getId().equals(id)).findFirst().ifPresent(result::add);
            }
            for (ColumnConfig col : dbColumns) {
                if (!result.contains(col))
                    result.add(col);
            }
            return result;
        } catch (IOException e) {
            return new ArrayList<>(dbColumns);
        }
    }

    // Khi lưu file
// Trong ConfigDialog.java
private void saveToFile(String fileName, List<ColumnConfig> list) {
    try {
        Path path = Paths.get("config", fileName);
        
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        // ĐẢM BẢO DÒNG NÀY LẤY ID:
        List<String> ids = list.stream()
                               .map(ColumnConfig::getId) // Đảm bảo gọi getId()
                               .collect(Collectors.toList());
        
        Files.write(path, ids, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    public String getJoinedColumnIds(String fileName) {
        Path path = Paths.get("config", fileName);

        // Nếu file không tồn tại hoặc không thể đọc, trả về chuỗi rỗng
        // (hoặc một danh sách ID mặc định để chương trình vẫn chạy được)
        if (!Files.exists(path) || !Files.isReadable(path)) {
            System.err.println("Cảnh báo: Không thể đọc file " + fileName + ". Đang sử dụng cấu hình mặc định.");
            return ""; // Hoặc trả về danh sách cột mặc định của bạn ở đây
        }

        try {
            System.out.println(Files.readAllLines(path).stream()
                    .filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.joining(",")));
            return Files.readAllLines(path).stream()
                    .filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.joining(","));
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
            return "";
        }
    }
}