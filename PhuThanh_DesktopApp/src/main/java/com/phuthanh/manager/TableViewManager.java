package com.phuthanh.manager;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableView.TableViewSelectionModel;
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

    // ================= SETUP =================
    public void setupTableView(TableView<ObservableList<String>> table,
            ObservableList<ObservableList<String>> data) {

        this.currentTable = table;
        this.rawData = data;
        this.filteredData = new FilteredList<>(data, p -> true);
        table.setItems(filteredData);

        Platform.runLater(() -> {

            if (!table.getItems().isEmpty()
                    && !table.getColumns().isEmpty()) {

                table.getSelectionModel()
                        .clearAndSelect(
                                0,
                                table.getColumns().get(0));

                table.getFocusModel()
                        .focus(
                                0,
                                table.getColumns().get(0));
            }
        });

        table.getSelectionModel().clearSelection();
        table.getFocusModel().focus(-1);

        table.getSelectionModel().setCellSelectionEnabled(true);
        TableViewSelectionModel<ObservableList<String>> sm = table.getSelectionModel();

        sm.setCellSelectionEnabled(true);
        sm.setSelectionMode(SelectionMode.MULTIPLE);

        table.setRowFactory(tv -> {
            TableRow<ObservableList<String>> row = new TableRow<>();

            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                updateRowStyle(row, table);
            });

            return row;
        });
        table.getFocusModel().focusedCellProperty().addListener((obs, o, n) -> {
            table.refresh();
        });
        initMapper(table);
        attachHeaderRightClick(table);

        if (!columnFilters.isEmpty()) {
            applyFilter();
        }
        enableCopy(table);
        enableCellTextSelection(table);
        setStyleTableView(table);
    }

    // ================= RELOAD DATA =================
    public void reloadData(ObservableList<ObservableList<String>> newData) {
        // System.out.println("🔄 Reloading data...");

        this.rawData = newData;
        this.filteredData = new FilteredList<>(rawData, p -> true);
        // System.out.println("reloadData: rawData size = " + (rawData != null ?
        // rawData.size() : 0));

        if (currentTable != null) {
            currentTable.setItems(filteredData);

            Platform.runLater(() -> {
                attachHeaderRightClick(currentTable);
                initMapper(currentTable);
                enableCellTextSelection(currentTable);
                currentTable.refresh();
                if (!columnFilters.isEmpty()) {
                    applyFilter();
                }
            });
        }
    }

    // ================= INIT MAPPER - CHỈ LẤY COLUMN VISIBLE =================
    private void initMapper(TableView<ObservableList<String>> table) {
        columnMapperByVisibleIndex.clear();
        columnToVisibleIndex.clear();

        // System.out.println("🔧 Initializing mapper for visible columns only...");

        int visibleIndex = 0;

        for (TableColumn<ObservableList<String>, ?> col : table.getColumns()) {
            originalHeaders.putIfAbsent(col, col.getText());
            if (col.isVisible()) {
                // ✅ Tìm đúng chỉ số trong data cho column này
                final int dataIndex = findDataIndexForColumn(table, col);

                if (dataIndex == -1) {
                    // System.err.println(" ❌ Cannot find data index for column: " + col.getText());
                    continue;
                }

                final int currentVisibleIndex = visibleIndex;

                columnMapperByVisibleIndex.put(currentVisibleIndex, row -> {
                    if (row == null || dataIndex >= row.size()) {
                        return "";
                    }
                    return row.get(dataIndex) == null ? "" : row.get(dataIndex);
                });

                columnToVisibleIndex.put(col, currentVisibleIndex);

                // System.out.println(" Visible column " + currentVisibleIndex + ": '" +
                // col.getText()
                // + "' (data index: " + dataIndex + ")");
                visibleIndex++;
            } else {
                // System.out.println(" ⚠️ Hidden column skipped: '" + col.getText() + "'");
            }
        }

        // System.out.println("✅ Total visible columns: " + visibleIndex);
    }

    // ✅ Thêm method này để tìm đúng data index
    private int findDataIndexForColumn(TableView<ObservableList<String>> table,
            TableColumn<ObservableList<String>, ?> targetColumn) {
        // Kiểm tra xem column có user data lưu data index không
        Object userData = targetColumn.getUserData();
        if (userData instanceof Integer) {
            return (Integer) userData;
        }

        // Nếu không, tìm theo thứ tự các column trong table
        // (giả sử data có cùng thứ tự với columns, bỏ qua column ẩn)
        int dataIndex = 0;
        for (TableColumn<ObservableList<String>, ?> col : table.getColumns()) {
            if (col == targetColumn) {
                return dataIndex;
            }
            dataIndex++;
        }

        return -1;
    }

    // ================= HEADER RIGHT CLICK =================
    private void attachHeaderRightClick(TableView<ObservableList<String>> table) {
        // System.out.println("✔ Attaching header right-click handler");

        if (currentRightClickHandler != null) {
            table.removeEventFilter(MouseEvent.MOUSE_CLICKED, currentRightClickHandler);
        }

        currentRightClickHandler = event -> {
            if (event.getButton() != MouseButton.SECONDARY)
                return;

            // System.out.println("\n================ RIGHT CLICK HEADER ================");

            Node node = event.getPickResult().getIntersectedNode();
            TableColumn<?, ?> column = findColumnFromNode(node);

            if (column == null) {
                // System.out.println("❌ COLUMN NOT FOUND");
                return;
            }
            // column.setText(column.getText() + " " + "🔽");

            // ✅ Cách 1: Tìm visible index bằng text của column
            int visibleIndex = getVisibleIndexByColumnText(table, column.getText());

            // System.out.println("📌 Visible Index = " + visibleIndex);
            showFilterPopup(table, visibleIndex, event.getScreenX(), event.getScreenY());
            event.consume();
        };

        table.addEventFilter(MouseEvent.MOUSE_CLICKED, currentRightClickHandler);
    }

    private int getVisibleIndexByColumnText(TableView<ObservableList<String>> table, String columnText) {
        int visibleIndex = 0;
        for (TableColumn<ObservableList<String>, ?> col : table.getColumns()) {
            if (col.isVisible()) {
                if (col.getText().equals(columnText)) {
                    return visibleIndex;
                }
                visibleIndex++;
            }
        }
        return -1;
    }

    // ================= FIND COLUMN FROM NODE =================
    private TableColumn<?, ?> findColumnFromNode(Node node) {
        while (node != null && !(node instanceof TableColumnHeader)) {
            node = node.getParent();
        }

        if (node instanceof TableColumnHeader header) {
            TableColumnBase<?, ?> base = header.getTableColumn();
            if (base instanceof TableColumn) {
                return (TableColumn<?, ?>) base;
            }
        }
        return null;
    }

    // ================= FILTER POPUP - DÙNG VISIBLE INDEX =================
    private void showFilterPopup(TableView<ObservableList<String>> table,
            int visibleIndex,
            double x, double y) {
        debugColumnData(visibleIndex);
        // 🔴 NEW: nếu đang có popup mở -> đóng nó trước
        if (currentFilterMenu != null && currentFilterMenu.isShowing()) {
            currentFilterMenu.hide();
        }

        // System.out.println("📦 OPEN POPUP FOR VISIBLE INDEX: " + visibleIndex);

        Function<ObservableList<String>, String> mapper = columnMapperByVisibleIndex.get(visibleIndex);
        // System.out.println(mapper);

        if (mapper == null) {
            // System.err.println("❌ No mapper for visible index: " + visibleIndex);
            // System.out.println("Available mappers: " +
            // columnMapperByVisibleIndex.keySet());
            return;
        }

        ContextMenu menu = new ContextMenu();
        menu.setOnHidden(e -> currentFilterMenu = null);

        // Lấy dữ liệu từ rawData
        List<ObservableList<String>> dataSource = rawData.stream()
                .filter(row -> {

                    for (var entry : columnFilters.entrySet()) {

                        int filterColumn = entry.getKey();

                        // bỏ qua filter của chính cột đang mở
                        if (filterColumn == visibleIndex)
                            continue;

                        Set<String> allowed = entry.getValue();

                        Function<ObservableList<String>, String> filterMapper = columnMapperByVisibleIndex
                                .get(filterColumn);

                        if (filterMapper == null)
                            continue;

                        String value = filterMapper.apply(row);

                        if (!allowed.contains(value)) {
                            return false;
                        }
                    }

                    return true;
                })
                .toList();
        // System.out.println("dataSource");
        // System.out.println(rawData);

        if (dataSource == null || dataSource.isEmpty()) {
            // System.err.println("❌ No data source available!");
            return;
        }

        // System.out.println("🔍 Data source size: " + dataSource.size());

        // Thu thập các giá trị unique
        Set<String> masterSet = new TreeSet<>();

        // for (ObservableList<String> row : dataSource) {
        // if (row != null) {
        // String value = mapper.apply(row);
        // String displayValue = value.isEmpty() ? "(Empty)" : value;
        // masterSet.add(displayValue);
        // }
        // }

        for (ObservableList<String> row : dataSource) {
            String value = mapper.apply(row);
            masterSet.add(value.isEmpty() ? "(Empty)" : value);
        }

        // System.out.println("📊 Total unique values found: " + masterSet.size());

        if (masterSet.isEmpty()) {
            masterSet.add("(No data available)");
        }

        // Lấy các giá trị đã được chọn trước đó
        Set<String> selected = new HashSet<>(columnFilters.getOrDefault(visibleIndex, new HashSet<>()));

        // Chuyển đổi (Empty) trong selected
        Set<String> selectedForDisplay = new HashSet<>();
        for (String s : selected) {
            if (s.isEmpty()) {
                selectedForDisplay.add("(Empty)");
            } else {
                selectedForDisplay.add(s);
            }
        }
        selected.clear();
        selected.addAll(selectedForDisplay);

        // System.out.println("📌 Current selected values: " + selected);

        // Tạo UI Components
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        ObservableList<String> displayList = FXCollections.observableArrayList(masterSet);
        ListView<String> listView = new ListView<>(displayList);
        listView.setPrefHeight(250);
        listView.setPrefWidth(280);

        listView.setCellFactory(lv -> new ListCell<String>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    String item = getItem();
                    if (item == null)
                        return;

                    if (checkBox.isSelected()) {
                        selected.add(item);
                    } else {
                        selected.remove(item);
                    }
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
            List<String> filtered = masterSet.stream()
                    .filter(v -> v.toLowerCase().contains(searchText))
                    .toList();
            displayList.setAll(filtered);
        });

        Button btnSelectAll = new Button("Select All");
        Button btnClearAll = new Button("Clear All");
        Button btnApply = new Button("Apply");
        Button btnCancel = new Button("Cancel");

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
            // System.out.println("🚀 APPLY FILTER - Selected: " + selected.size());

            // Chuyển đổi (Empty) về chuỗi rỗng
            Set<String> filterValues = new HashSet<>();
            for (String s : selected) {
                if ("(Empty)".equals(s)) {
                    filterValues.add("");
                } else if ("(No data available)".equals(s)) {
                    // Bỏ qua, không lọc
                } else {
                    filterValues.add(s);
                }
            }

            if (filterValues.isEmpty() || filterValues.size() == masterSet.size() ||
                    (masterSet.size() == 1 && masterSet.contains("(No data available)"))) {
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
        content.setStyle("""
                -fx-padding: 10;
                -fx-background-color: white;
                -fx-border-color: #cccccc;
                -fx-border-radius: 5;
                -fx-background-radius: 5;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);
                """);

        CustomMenuItem menuItem = new CustomMenuItem(content, false);
        menuItem.setHideOnClick(false);
        menu.getItems().add(menuItem);

        menu.show(table, x, y);
        currentFilterMenu = menu;
    }

    // ================= APPLY FILTER =================
    private void applyFilter() {
        if (filteredData == null) {
            // System.err.println("⚠️ filteredData is null!");
            return;
        }

        // System.out.println("🔁 APPLY FILTER - Active filters: " +
        // columnFilters.size());

        if (columnFilters.isEmpty()) {
            filteredData.setPredicate(p -> true);
            // System.out.println("✅ All filters cleared. Row count: " +
            // filteredData.size());
            return;
        }

        filteredData.setPredicate(row -> {
            for (var entry : columnFilters.entrySet()) {
                int visibleIndex = entry.getKey();
                Set<String> allowedValues = entry.getValue();

                if (allowedValues == null || allowedValues.isEmpty())
                    continue;

                // Lấy mapper cho visible index này
                Function<ObservableList<String>, String> mapper = columnMapperByVisibleIndex.get(visibleIndex);
                if (mapper == null)
                    continue;

                String cellValue = mapper.apply(row);

                if (!allowedValues.contains(cellValue)) {
                    return false;
                }
            }
            return true;
        });

        // System.out.println("✅ Filter applied. Row count: " + filteredData.size());
    }

    // ================= UTILITIES =================
    public void clearAllFilters() {
        columnFilters.clear();
        if (currentTable != null) {
            updateHeaderText(currentTable);
        }
        applyFilter();
    }

    public void removeFilter(int visibleIndex) {
        columnFilters.remove(visibleIndex);
        if (currentTable != null) {
            updateHeaderText(currentTable);
        }
        applyFilter();
    }

    public boolean hasFilter(int visibleIndex) {
        return columnFilters.containsKey(visibleIndex);
    }

    public FilteredList<ObservableList<String>> getFilteredData() {
        return filteredData;
    }

    public void refresh() {
        if (filteredData != null) {
            applyFilter();
        }
    }

    public int getVisibleColumnCount() {
        return columnMapperByVisibleIndex.size();
    }

    public void dispose() {
        if (currentTable != null && currentRightClickHandler != null) {
            currentTable.removeEventFilter(MouseEvent.MOUSE_CLICKED, currentRightClickHandler);
        }
        if (currentFilterMenu != null) {
            currentFilterMenu.hide();
            currentFilterMenu = null;
        }

        columnFilters.clear();
        columnMapperByVisibleIndex.clear();
        columnToVisibleIndex.clear();
        originalHeaders.clear();

        rawData = null;
        filteredData = null;
        currentTable = null;
        currentRightClickHandler = null;
    }

    // ================= COPY =================
    public void enableCopy(TableView<ObservableList<String>> table) {
        table.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                var selectedCells = table.getSelectionModel().getSelectedCells();
                if (selectedCells.isEmpty())
                    return;
                boolean singleCell = selectedCells.size() == 1; // ⭐ check 1 ô

                Map<Integer, Map<Integer, String>> rowData = new TreeMap<>();
                for (TablePosition<?, ?> pos : selectedCells) {
                    int row = pos.getRow();
                    int col = table.getColumns().indexOf(pos.getTableColumn());
                    Object value = pos.getTableColumn().getCellData(row);
                    String text = value == null ? "" : value.toString();
                    if (singleCell) {
                        text = text
                                .replace("\n", " ")
                                .replace("\t", " ")
                                .replaceAll("\\s+", " ")
                                .trim();
                    }
                    rowData.computeIfAbsent(row, k -> new TreeMap<>())
                            .put(col, value == null ? "" : value.toString());
                }

                StringBuilder sb = new StringBuilder();
                for (Map<Integer, String> row : rowData.values()) {
                    for (String value : row.values()) {
                        sb.append(value).append("\t");
                        // sb.append(value);
                    }
                    if (sb.length() > 0) {
                        sb.setLength(sb.length() - 1);
                    }
                    sb.append("\n");
                }

                ClipboardContent content = new ClipboardContent();
                content.putString(sb.toString());
                Clipboard.getSystemClipboard().setContent(content);
                event.consume();
            }
        });
    }

    // Thêm method kiểm tra
    public void debugColumnData(int visibleIndex) {
        Function<ObservableList<String>, String> mapper = columnMapperByVisibleIndex.get(visibleIndex);
        if (mapper == null) {
            // System.out.println("No mapper for index " + visibleIndex);
            return;
        }

        Set<String> values = new HashSet<>();
        for (ObservableList<String> row : rawData) {
            values.add(mapper.apply(row));
        }

        // System.out.println("All values in column " + visibleIndex + ": " + values);
        // System.out.println("Contains CA00000001: " + values.contains("CA00000001"));
    }

    private void enableCellTextSelection(TableView<ObservableList<String>> table) {

        for (TableColumn<ObservableList<String>, ?> column : table.getColumns()) {
            if ("SELECT_COL".equals(column.getId())) {
                continue;
            }

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
                        if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.ESCAPE) {
                            cancelEdit();
                        }
                    });

                    addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {

                        if (isEmpty())
                            return;

                        String value = getItem();

                        if (e.isControlDown()
                                && e.getButton() == MouseButton.PRIMARY
                                && isImageUrl(value)) {

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
                    if (getItem() == null)
                        return;

                    textField.setText(getItem());
                    setGraphic(textField);
                    setText(null);
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

                    TablePosition<?, ?> focused = getTableView()
                            .getFocusModel()
                            .getFocusedCell();

                    boolean currentCell = focused != null
                            && focused.getRow() == myRow
                            && focused.getColumn() == myCol;

                    boolean rowSelected = getTableView()
                            .getSelectionModel()
                            .getSelectedCells()
                            .stream()
                            .anyMatch(p -> p.getRow() == myRow);
                    boolean cellSelected = getTableView()
                            .getSelectionModel()
                            .isSelected(myRow, getTableColumn());

                    if (currentCell) {
                        setStyle("""
                                    -fx-background-color:#19d238;
                                    -fx-text-fill:white;
                                    -fx-font-weight:bold;
                                    -fx-border-color: red;
                                    -fx-border-width: 2;
                                """);

                    } else if (cellSelected) {
                        setStyle("""
                                    -fx-background-color:#52bd2b;
                                    -fx-text-fill:black;
                                """);

                    } else if (rowSelected) {
                        setStyle("""
                                    -fx-background-color:#b3a227;
                                    -fx-text-fill:black;
                                """);

                    } else {
                        setStyle("");
                    }

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
            URI uri = new URI(rawUrl);
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            try {
                // encode fallback
                String fixed = rawUrl.replace(" ", "%20");
                Desktop.getDesktop().browse(new URI(fixed));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean isImageUrl(String text) {
        if (text == null)
            return false;

        // nhận diện URL + đuôi file ảnh
        return text.matches("(?i)^(https?:\\/\\/.*\\.(png|jpg|jpeg|gif|webp|bmp|svg))$");
    }

    private void setStyleTableView(TableView<?> table) {

        Platform.runLater(() -> {

            // 🔵 Làm to ScrollBar
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

            // 🔥 CSS Excel viết trực tiếp trong code
            String css = """
                        .scroll-bar:vertical {
                            -fx-pref-width: 22;
                        }
                        .scroll-bar:horizontal {
                            -fx-pref-height: 22;
                        }

                        .scroll-bar .track {
                            -fx-background-color: #F1F1F1;
                            -fx-background-radius: 10;
                        }

                        .scroll-bar .thumb {
                            -fx-background-color: #C1C1C1;
                            -fx-background-radius: 10;
                        }

                        .scroll-bar .thumb:hover {
                            -fx-background-color: #A8A8A8;
                        }

                        .scroll-bar .thumb:pressed {
                            -fx-background-color: #8E8E8E;
                        }

                        .scroll-bar .increment-button,
                        .scroll-bar .decrement-button {
                            -fx-background-color: transparent;
                            -fx-padding: 0;
                        }
                    """;

            // 👉 Inject CSS trực tiếp vào Scene
            table.getScene().getStylesheets().add(
                    "data:text/css," + css.replace("\n", ""));
        });
    }

    // private void setupRowHighlight(TableView<ObservableList<String>> table) {

    //     table.setRowFactory(tv -> {

    //         TableRow<ObservableList<String>> row = new TableRow<>();

    //         table.getFocusModel().focusedCellProperty().addListener((obs, oldVal, newVal) -> {

    //             if (row.isEmpty())
    //                 return;

    //             if (row.getIndex() == newVal.getRow()) {
    //                 row.setStyle("-fx-background-color: #23dd12;");
    //             } else {
    //                 row.setStyle("");
    //             }
    //         });

    //         return row;
    //     });
    // }

    private int getVisibleColumnIndex(TableColumn<?, ?> targetColumn) {

        int visibleIndex = 0;

        for (TableColumn<?, ?> col : targetColumn.getTableView().getColumns()) {

            if (!col.isVisible()) {
                continue;
            }

            if (col == targetColumn) {
                return visibleIndex;
            }

            visibleIndex++;
        }

        return -1;
    }

    private void updateRowStyle(
            TableRow<ObservableList<String>> row,
            TableView<ObservableList<String>> tableView) {

        if (row.isEmpty()) {
            row.setStyle("");
            return;
        }

        int focusedRow = tableView.getFocusModel()
                .getFocusedCell()
                .getRow();

        if (row.getIndex() == focusedRow) {
            row.setStyle("-fx-background-color: #ca8e0c;");
        } else {
            row.setStyle("");
        }
    }

    // public void dispose() {
    // if (currentTable != null && currentRightClickHandler != null) {
    // currentTable.removeEventFilter(MouseEvent.MOUSE_CLICKED,
    // currentRightClickHandler);
    // }
    // columnFilters.clear();
    // columnMapperByVisibleIndex.clear();
    // columnToVisibleIndex.clear();
    // originalHeaders.clear();
    // }
    // TableView<ObservableList<String>> table) {

    // return table.getRowFactory();
    // }

    private void updateHeaderText(TableView<ObservableList<String>> table) {

        int visibleIndex = 0;

        for (TableColumn<ObservableList<String>, ?> col : table.getColumns()) {

            if (!col.isVisible()) {
                continue;
            }

            String originalText = originalHeaders.getOrDefault(
                    col,
                    col.getText());

            if (columnFilters.containsKey(visibleIndex)) {
                col.setText(originalText + " " + "🔽");
            } else {
                col.setText(originalText);
            }

            visibleIndex++;
        }
    }
}