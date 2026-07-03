package com.phuthanh.manager;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.awt.Desktop;
import java.net.URI;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class TableViewManager {
    private TableView<ObservableList<String>> currentTable;
    private ObservableList<ObservableList<String>> rawData;
    private FilteredList<ObservableList<String>> filteredData;
    private EventHandler<MouseEvent> currentRightClickHandler;

    private final Map<Integer, Set<String>> columnFilters = new HashMap<>();
    private final Map<Integer, Function<ObservableList<String>, String>> columnMapperByVisibleIndex = new HashMap<>();
    private final Map<TableColumn<ObservableList<String>, ?>, Integer> columnToVisibleIndex = new HashMap<>();
    private ContextMenu currentFilterMenu;
    private final Map<TableColumn<?, ?>, String> originalHeaders = new HashMap<>();

    // 1. Thay thế hoàn toàn hàm setupTableView cũ của bạn
    public void setupTableView(TableView<ObservableList<String>> table, ObservableList<ObservableList<String>> data) {
        this.currentTable = table;
        this.rawData = data;
        this.filteredData = new FilteredList<>(data, p -> true);
        table.setItems(filteredData);

        Platform.runLater(() -> {
            if (!table.getItems().isEmpty() && !table.getColumns().isEmpty()) {
                table.getSelectionModel().clearAndSelect(0, table.getColumns().get(0));
                table.getFocusModel().focus(0, table.getColumns().get(0));
            }
        });

        table.getSelectionModel().clearSelection();
        table.getFocusModel().focus(-1);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        table.setRowFactory(tv -> {
            TableRow<ObservableList<String>> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateRowStyle(row, table));
            return row;
        });

        table.getFocusModel().focusedCellProperty().addListener((obs, o, n) -> table.refresh());

        // ❌ XÓA BỎ LẮNG NGHE CŨ TẠI ĐÂY (Dòng table.getColumns().addListener cũ gây lỗi
        // ghi đè)

        initMapper(table);
        attachHeaderRightClick(table);

        if (!columnFilters.isEmpty())
            applyFilter();
        enableCopy(table);
        enableCellTextSelection(table);
        setStyleTableView(table);
    }

    // 2. Thay thế hoàn toàn hàm reloadData cũ của bạn
    public void reloadData(ObservableList<ObservableList<String>> newData) {
        this.rawData = newData;
        this.filteredData = new FilteredList<>(rawData, p -> true);

        if (currentTable != null) {
            currentTable.setItems(filteredData);
            Platform.runLater(() -> {

                attachHeaderRightClick(currentTable);
                initMapper(currentTable);
                enableCellTextSelection(currentTable);
                currentTable.refresh();
                if (!columnFilters.isEmpty())
                    applyFilter();
            });
        }
    }

    private void initMapper(TableView<ObservableList<String>> table) {
        columnMapperByVisibleIndex.clear();
        columnToVisibleIndex.clear();
        int visibleIndex = 0;

        for (TableColumn<ObservableList<String>, ?> col : table.getColumns()) {
            originalHeaders.putIfAbsent(col, col.getText());
            if (col.isVisible()) {
                final int dataIndex = findDataIndexForColumn(table, col);
                if (dataIndex == -1)
                    continue;

                final int currentVisibleIndex = visibleIndex;
                columnMapperByVisibleIndex.put(currentVisibleIndex,
                        row -> (row == null || dataIndex >= row.size() || row.get(dataIndex) == null) ? ""
                                : row.get(dataIndex));
                columnToVisibleIndex.put(col, currentVisibleIndex);
                visibleIndex++;
            }
        }
    }

    private int findDataIndexForColumn(TableView<ObservableList<String>> table,
            TableColumn<ObservableList<String>, ?> targetColumn) {
        Object userData = targetColumn.getUserData();
        if (userData instanceof Integer)
            return (Integer) userData;

        int dataIndex = 0;
        for (TableColumn<ObservableList<String>, ?> col : table.getColumns()) {
            if (col == targetColumn)
                return dataIndex;
            dataIndex++;
        }
        return -1;
    }

    private void attachHeaderRightClick(TableView<ObservableList<String>> table) {
        if (currentRightClickHandler != null) {
            table.removeEventFilter(MouseEvent.MOUSE_CLICKED, currentRightClickHandler);
        }

        currentRightClickHandler = event -> {
            if (event.getButton() != MouseButton.SECONDARY)
                return;

            Node node = event.getPickResult().getIntersectedNode();
            TableColumn<?, ?> column = findColumnFromNode(node);
            if (column == null)
                return;

            int visibleIndex = getVisibleIndexByColumnText(table, column.getText());
            if (visibleIndex != -1) {
                showFilterPopup(table, visibleIndex, event.getScreenX(), event.getScreenY());
                event.consume();
            }
        };
        table.addEventFilter(MouseEvent.MOUSE_CLICKED, currentRightClickHandler);
    }

    private int getVisibleIndexByColumnText(TableView<ObservableList<String>> table, String columnText) {
        int visibleIndex = 0;
        for (TableColumn<ObservableList<String>, ?> col : table.getColumns()) {
            if (col.isVisible()) {
                if (col.getText().equals(columnText))
                    return visibleIndex;
                visibleIndex++;
            }
        }
        return -1;
    }

    private TableColumn<?, ?> findColumnFromNode(Node node) {
        while (node != null && !(node instanceof TableColumnHeader)) {
            node = node.getParent();
        }
        return (node instanceof TableColumnHeader header && header.getTableColumn() instanceof TableColumn)
                ? (TableColumn<?, ?>) header.getTableColumn()
                : null;
    }

    private void showFilterPopup(TableView<ObservableList<String>> table, int visibleIndex, double x, double y) {
        if (currentFilterMenu != null && currentFilterMenu.isShowing()) {
            currentFilterMenu.hide();
        }

        Function<ObservableList<String>, String> mapper = columnMapperByVisibleIndex.get(visibleIndex);
        if (mapper == null)
            return;

        ContextMenu menu = new ContextMenu();
        menu.setOnHidden(e -> currentFilterMenu = null);

        List<ObservableList<String>> dataSource = rawData.stream()
                .filter(row -> {
                    for (var entry : columnFilters.entrySet()) {
                        int filterColumn = entry.getKey();
                        if (filterColumn == visibleIndex)
                            continue;

                        Set<String> allowed = entry.getValue();
                        Function<ObservableList<String>, String> filterMapper = columnMapperByVisibleIndex
                                .get(filterColumn);
                        if (filterMapper == null || !allowed.contains(filterMapper.apply(row)))
                            return false;
                    }
                    return true;
                }).toList();

        Set<String> masterSet = new TreeSet<>();
        for (ObservableList<String> row : dataSource) {
            String value = mapper.apply(row);
            masterSet.add(value.isEmpty() ? "(Empty)" : value);
        }
        if (masterSet.isEmpty())
            masterSet.add("(No data available)");

        Set<String> selected = new HashSet<>();
        Set<String> rawSelected = columnFilters.getOrDefault(visibleIndex, new HashSet<>());
        for (String s : rawSelected) {
            selected.add(s.isEmpty() ? "(Empty)" : s);
        }

        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        ObservableList<String> displayList = FXCollections.observableArrayList(masterSet);
        ListView<String> listView = new ListView<>(displayList);
        listView.setPrefHeight(250);
        listView.setPrefWidth(280);

        listView.setCellFactory(lv -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(e -> {
                    String item = getItem();
                    if (item == null)
                        return;
                    if (checkBox.isSelected())
                        selected.add(item);
                    else
                        selected.remove(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    checkBox.setText(item);
                    checkBox.setSelected(selected.contains(item));
                    setGraphic(checkBox);
                }
            }
        });

        searchField.textProperty().addListener((obs, old, newVal) -> {
            String searchText = newVal == null ? "" : newVal.toLowerCase();
            displayList.setAll(masterSet.stream().filter(v -> v.toLowerCase().contains(searchText)).toList());
        });

        Button btnSelectAll = new Button("Select All"), btnClearAll = new Button("Clear All"),
                btnApply = new Button("Apply"), btnCancel = new Button("Cancel");
        btnSelectAll.setOnAction(e -> {
            selected.clear();
            selected.addAll(displayList);
            listView.refresh();
        });
        btnClearAll.setOnAction(e -> {
            selected.clear();
            listView.refresh();
        });

        btnApply.setOnAction(e -> {
            Set<String> filterValues = new HashSet<>();
            for (String s : selected) {
                if ("(Empty)".equals(s))
                    filterValues.add("");
                else if (!"(No data available)".equals(s))
                    filterValues.add(s);
            }

            if (filterValues.isEmpty() || filterValues.size() == masterSet.size()
                    || (masterSet.size() == 1 && masterSet.contains("(No data available)"))) {
                columnFilters.remove(visibleIndex);
            } else {
                columnFilters.put(visibleIndex, filterValues);
            }
            updateHeaderText(table);
            applyFilter();
            menu.hide();
        });
        btnCancel.setOnAction(e -> menu.hide());

        HBox buttonBar = new HBox(10, btnSelectAll, btnClearAll, btnApply, btnCancel);
        buttonBar.setStyle("-fx-alignment: center; -fx-padding: 5 0 0 0;");
        VBox content = new VBox(10, searchField, listView, buttonBar);
        content.setStyle(
                "-fx-padding: 10; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        CustomMenuItem menuItem = new CustomMenuItem(content, false);
        menuItem.setHideOnClick(false);
        menu.getItems().add(menuItem);
        menu.show(table, x, y);
        currentFilterMenu = menu;
    }

    // private void applyFilter() {
    //     if (filteredData == null)
    //         return;
    //     if (columnFilters.isEmpty()) {
    //         filteredData.setPredicate(p -> true);
    //         return;
    //     }

    //     filteredData.setPredicate(row -> {
    //         for (var entry : columnFilters.entrySet()) {
    //             int visibleIndex = entry.getKey();
    //             Set<String> allowedValues = entry.getValue();
    //             if (allowedValues == null || allowedValues.isEmpty())
    //                 continue;

    //             Function<ObservableList<String>, String> mapper = columnMapperByVisibleIndex.get(visibleIndex);
    //             if (mapper == null || !allowedValues.contains(mapper.apply(row)))
    //                 return false;
    //         }
    //         return true;
    //     });
    // }

    public void clearAllFilters() {
        columnFilters.clear();
        columnMapperByVisibleIndex.clear();
        columnToVisibleIndex.clear();

        if (currentFilterMenu != null) {
            currentFilterMenu.getItems().clear();
            currentFilterMenu = null; // Đánh dấu hủy thực thể Menu đồ họa cũ
        }

        // Ép dữ liệu filter (FilteredList) quay về trạng thái ban đầu không ràng buộc
        // điều kiện
        if (filteredData != null) {
            filteredData.setPredicate(null);
        }
        // 3. Hủy bỏ thực thể Menu đồ họa cũ và các item để giải phóng RAM UI
        if (currentFilterMenu != null) {
            currentFilterMenu.getItems().clear();
            currentFilterMenu = null;
        }

        // 4. Ép giải phóng bộ nhớ rác của thực thể bảng hiện tại đang quản lý
        if (rawData != null) {
            rawData.clear();
        }
    }

    public FilteredList<ObservableList<String>> getFilteredData() {
        return filteredData;
    }

    public void dispose() {
        if (currentTable != null && currentRightClickHandler != null) {
            currentTable.removeEventFilter(MouseEvent.MOUSE_CLICKED, currentRightClickHandler);
        }
        if (currentFilterMenu != null)
            currentFilterMenu.hide();
        columnFilters.clear();
        columnMapperByVisibleIndex.clear();
        columnToVisibleIndex.clear();
        originalHeaders.clear();
        rawData = null;
        filteredData = null;
        currentTable = null;
    }

    public void enableCopy(TableView<ObservableList<String>> table) {
        table.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                var selectedCells = table.getSelectionModel().getSelectedCells();
                if (selectedCells.isEmpty())
                    return;
                boolean singleCell = selectedCells.size() == 1;

                Map<Integer, Map<Integer, String>> sortedRowData = new TreeMap<>();
                for (TablePosition<?, ?> pos : selectedCells) {
                    int row = pos.getRow();
                    int col = table.getColumns().indexOf(pos.getTableColumn());
                    Object value = pos.getTableColumn().getCellData(row);
                    String text = value == null ? "" : value.toString();
                    if (singleCell)
                        text = text.replace("\n", " ").replace("\t", " ").replaceAll("\\s+", " ").trim();
                    sortedRowData.computeIfAbsent(row, k -> new TreeMap<>()).put(col, text);
                }

                StringBuilder sb = new StringBuilder();
                for (Map<Integer, String> row : sortedRowData.values()) {
                    for (String value : row.values())
                        sb.append(value).append("\t");
                    if (sb.length() > 0)
                        sb.setLength(sb.length() - 1);
                    sb.append("\n");
                }

                ClipboardContent content = new ClipboardContent();
                content.putString(sb.toString());
                Clipboard.getSystemClipboard().setContent(content);
                event.consume();
            }
        });
    }

    private void enableCellTextSelection(TableView<ObservableList<String>> table) {
        for (TableColumn<ObservableList<String>, ?> column : table.getColumns()) {
            if ("SELECT_COL".equals(column.getId()))
                continue;

            TableColumn<ObservableList<String>, String> col = (TableColumn<ObservableList<String>, String>) column;
            col.setCellFactory(tc -> new TableCell<>() {
                private final TextField textField = new TextField();
                {
                    textField.setEditable(false);
                    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal)
                            cancelEdit();
                    });
                    textField.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.ESCAPE)
                            cancelEdit();
                    });

                    addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                        if (isEmpty())
                            return;
                        String value = getItem();
                        if (e.isControlDown() && e.getButton() == MouseButton.PRIMARY && isImageUrl(value)) {
                            openInBrowser(value);
                            e.consume();
                            return;
                        }
                        if (e.getClickCount() == 2) {
                            Platform.runLater(() -> {
                                startEdit();
                                textField.requestFocus();
                                textField.deselect();
                            });
                        }
                    });
                    setStyle("-fx-cursor: text;");
                }

                @Override
                public void startEdit() {
                    super.startEdit();
                    if (getItem() != null) {
                        textField.setText(getItem());
                        setGraphic(textField);
                        setText(null);
                    }
                }

                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setGraphic(null);
                    setText(getItem());
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                        return;
                    }

                    int myRow = getIndex();
                    int myCol = getVisibleColumnIndex(getTableColumn());
                    TablePosition<?, ?> focused = getTableView().getFocusModel().getFocusedCell();

                    boolean currentCell = focused != null && focused.getRow() == myRow && focused.getColumn() == myCol;
                    boolean rowSelected = getTableView().getSelectionModel().getSelectedCells().stream()
                            .anyMatch(p -> p.getRow() == myRow);
                    boolean cellSelected = getTableView().getSelectionModel().isSelected(myRow, getTableColumn());

                    if (currentCell)
                        setStyle(
                                "-fx-background-color:#19d238;-fx-text-fill:white;-fx-font-weight:bold;-fx-border-color: red;-fx-border-width: 2;");
                    else if (cellSelected)
                        setStyle("-fx-background-color:#52bd2b;-fx-text-fill:black;");
                    else if (rowSelected)
                        setStyle("-fx-background-color:#b3a227;-fx-text-fill:black;");
                    else
                        setStyle("");

                    if (isEditing()) {
                        textField.setText(item);
                        setGraphic(textField);
                        setText(null);
                    } else {
                        setText(item);
                        setGraphic(null);
                    }
                }
            });
        }
    }

    private void openInBrowser(String rawUrl) {
        try {
            Desktop.getDesktop().browse(new URI(rawUrl.replace(" ", "%20")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isImageUrl(String text) {
        return text != null && text.matches("(?i)^(https?:\\/\\/.*\\.(png|jpg|jpeg|gif|webp|bmp|svg))$");
    }

    private void setStyleTableView(TableView<?> table) {
        Platform.runLater(() -> {
            for (Node node : table.lookupAll(".scroll-bar")) {
                if (node instanceof ScrollBar sb) {
                    if (sb.getOrientation() == Orientation.VERTICAL) {
                        sb.setPrefWidth(22);
                        sb.setMinWidth(22);
                        sb.setMaxWidth(22);
                    } else {
                        sb.setPrefHeight(22);
                        sb.setMinHeight(22);
                        sb.setMaxHeight(22);
                    }
                }
            }
        });
    }

    private int getVisibleColumnIndex(TableColumn<?, ?> targetColumn) {
        int visibleIndex = 0;
        for (TableColumn<?, ?> col : targetColumn.getTableView().getColumns()) {
            if (!col.isVisible())
                continue;
            if (col == targetColumn)
                return visibleIndex;
            visibleIndex++;
        }
        return -1;
    }

    private void updateRowStyle(TableRow<ObservableList<String>> row, TableView<ObservableList<String>> tableView) {
        if (row.isEmpty()) {
            row.setStyle("");
            return;
        }
        if (row.getIndex() == tableView.getFocusModel().getFocusedCell().getRow()) {
            row.setStyle("-fx-background-color: #ca8e0c;");
        } else {
            row.setStyle("");
        }
    }

    private void updateHeaderText(TableView<ObservableList<String>> table) {
        int visibleIndex = 0;
        for (TableColumn<ObservableList<String>, ?> col : table.getColumns()) {
            if (!col.isVisible())
                continue;
            String originalText = originalHeaders.getOrDefault(col, col.getText());
            col.setText(columnFilters.containsKey(visibleIndex) ? originalText + " 🔽" : originalText);
            visibleIndex++;
        }
    }

private String currentSearchKeyword = "";
private int currentSearchColIndex = -1;
private boolean isSearchQuantityCol = false;

public void updateSearchPredicate(String keyword, int colIndex, boolean isQuantityCol) {
    this.currentSearchKeyword = keyword.toLowerCase();
    this.currentSearchColIndex = colIndex;
    this.isSearchQuantityCol = isQuantityCol;
    applyFilter(); // Gọi lại hàm này để nó tính toán lại toàn bộ điều kiện
}

private void applyFilter() {
    if (filteredData == null) return;

    filteredData.setPredicate(row -> {
        // 1. Kiểm tra Filter cột (Header click)
        for (var entry : columnFilters.entrySet()) {
            int visibleIndex = entry.getKey();
            Set<String> allowedValues = entry.getValue();
            Function<ObservableList<String>, String> mapper = columnMapperByVisibleIndex.get(visibleIndex);
            if (mapper != null && !allowedValues.contains(mapper.apply(row)))
                return false;
        }

        // 2. Kiểm tra Search Keyword (đang gõ ở ô tìm kiếm)
        if (currentSearchKeyword.isEmpty()) return true;

        if (currentSearchColIndex != -1) {
            // Tìm theo cột cụ thể
            String cellVal = row.get(currentSearchColIndex);
            return checkCellMatch(cellVal, currentSearchKeyword, isSearchQuantityCol);
        } else {
            // Tìm trên toàn dòng
            for (String cell : row) {
                if (checkCellMatch(cell, currentSearchKeyword, isSearchQuantityCol)) return true;
            }
            return false;
        }
    });
}

// Hàm hỗ trợ kiểm tra cell
private boolean checkCellMatch(String cell, String keyword, boolean isQuantityCol) {
    if (cell == null || cell.isEmpty()) return false;
    String val = cell.toLowerCase();
    if (!isQuantityCol) val = val.replace("-", "").replace(".", "");
    return val.contains(keyword);
}

}