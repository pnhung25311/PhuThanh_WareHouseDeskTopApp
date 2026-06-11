package com.phuthanh.manager;

import com.phuthanh.business.table.ColumnConfig;
import com.phuthanh.business.table.ProductBusinessColumns;
import com.phuthanh.helper.function.NumberFormatter;
import com.phuthanh.model.business.ProductBusiness;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class TableViewManagerBusiness {

    private final NumberFormatter numberFormatter = new NumberFormatter();
    private final ProductBusinessColumns productBusinessColumns = new ProductBusinessColumns();

    private FilteredList<ProductBusiness> filteredData;
    private final Map<TableColumn<ProductBusiness, ?>, String> columnFilters = new HashMap<>();
    private ContextMenu activeFilterMenu;
    private final Map<TableColumn<ProductBusiness, ?>, String> originalHeaders = new HashMap<>();
    private TableView<ProductBusiness> currentTable;

    // ================= SETUP =================
    public void setupTableView(TableView<ProductBusiness> table,
            FilteredList<ProductBusiness> filteredData) {

        this.filteredData = filteredData;

        createBusinessColumns(table);
        table.setItems(filteredData);
        this.currentTable = table;
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> table.refresh());
        Platform.runLater(() -> enableHeaderRightClickFilter(table));

        enableCopy(table);
        highlightRows(table);
        setStyleTableView(table);
    }

    // ================= HEADER FILTER =================
    private void enableHeaderRightClickFilter(TableView<ProductBusiness> table) {

        table.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {

            if (e.getButton() != MouseButton.SECONDARY)
                return;

            Node node = e.getPickResult().getIntersectedNode();

            while (node != null && !(node instanceof TableColumnHeader)) {
                node = node.getParent();
            }

            if (node == null)
                return;

            TableColumnHeader header = (TableColumnHeader) node;
            TableColumn<ProductBusiness, ?> col = (TableColumn<ProductBusiness, ?>) header.getTableColumn();

            if (col == null)
                return;

            columnFilters.putIfAbsent(col, "");

            // ⭐ NEW: đóng popup cũ nếu đang mở
            if (activeFilterMenu != null && activeFilterMenu.isShowing()) {
                activeFilterMenu.hide();
            }

            // mở popup mới
            activeFilterMenu = showFilterPopup(table, col, e.getScreenX(), e.getScreenY());

            e.consume();
        });
    }

    // ================= FILTER POPUP (IMPROVED) =================
    private ContextMenu showFilterPopup(TableView<ProductBusiness> table,
            TableColumn<ProductBusiness, ?> column,
            double x, double y) {

        ContextMenu menu = new ContextMenu();

        // ================= DATA =================
        ObservableList<String> masterList = FXCollections.observableArrayList();

        for (ProductBusiness item : filteredData) {
            Object v = column.getCellData(item);
            if (v != null && !masterList.contains(v.toString())) {
                masterList.add(v.toString());
            }
        }

        FilteredList<String> filteredList = new FilteredList<>(masterList, p -> true);

        // ================= SEARCH =================
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        // ================= SELECTED VALUES =================
        java.util.Set<String> selectedValues = new java.util.HashSet<>();

        // ================= LISTVIEW WITH CHECKBOX =================
        ListView<String> listView = new ListView<>(filteredList);
        listView.setPrefHeight(200);

        listView.setCellFactory(lv -> new ListCell<>() {

            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    String item = getItem();
                    if (item == null)
                        return;

                    if (checkBox.isSelected()) {
                        selectedValues.add(item);
                    } else {
                        selectedValues.remove(item);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                checkBox.setText(item);
                checkBox.setSelected(selectedValues.contains(item));
                setGraphic(checkBox);
            }
        });

        // ================= SEARCH FILTER =================
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {

            String keyword = newVal == null ? "" : newVal.toLowerCase();

            filteredList.setPredicate(item -> item.toLowerCase().contains(keyword));
        });

        // ================= BUTTONS =================
        Button apply = new Button("Apply");
        Button clear = new Button("Clear");

        apply.setOnAction(e -> {

            if (selectedValues.isEmpty()) {
                columnFilters.remove(column);
            } else {
                columnFilters.put(column, String.join("||", selectedValues).toLowerCase());
            }

            updateHeaderText(table);

            applyFilter();
            menu.hide();
        });

        clear.setOnAction(e -> {
            selectedValues.clear();
            columnFilters.remove(column);
            updateHeaderText(table);
            applyFilter();
            menu.hide();
        });

        HBox buttons = new HBox(10, apply, clear);

        VBox box = new VBox(10, searchField, listView, buttons);
        box.setStyle("""
                -fx-padding: 12;
                -fx-background-color: white;
                -fx-border-color: #ddd;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
                """);

        menu.getItems().add(new CustomMenuItem(box, false));

        // menu.show(table, x, y);
        menu.setOnHidden(e -> activeFilterMenu = null); // reset khi đóng
        menu.show(table, x, y);
        return menu;
    }

    public void clearAllFilters() {
        columnFilters.clear();
        if (currentTable != null) {
            updateHeaderText(currentTable);
        }
        applyFilter();
    }

    // ================= APPLY FILTER =================
    private void applyFilter() {

        if (filteredData == null)
            return;

        filteredData.setPredicate(product -> {

            // header filters
            for (var entry : columnFilters.entrySet()) {

                String filter = entry.getValue();
                if (filter == null || filter.isEmpty())
                    continue;

                Object value = entry.getKey().getCellData(product);
                String text = value == null ? "" : value.toString().toLowerCase();

                String[] parts = filter.split("\\|\\|");

                boolean match = false;
                for (String p : parts) {
                    if (text.contains(p)) {
                        match = true;
                        break;
                    }
                }

                if (!match)
                    return false;
            }

            // ⭐ combine với search textbox
            return externalPredicate.test(product);
        });
    }

    // ================= CREATE COLUMNS =================
    public void createBusinessColumns(TableView<ProductBusiness> table) {

        table.getColumns().clear();

        for (ColumnConfig cfg : productBusinessColumns.getColumns()) {

            TableColumn<ProductBusiness, String> col = new TableColumn<>(cfg.header);
            originalHeaders.put(col, cfg.header);
            col.setCellValueFactory(cell -> new SimpleStringProperty(cfg.mapper.apply(cell.getValue())));
            col.setPrefWidth(cfg.width);
            col.setId(cfg.id);

            col.setCellFactory(tc -> new TableCell<ProductBusiness, String>() {

                private final TextField textField = new TextField();

                {
                    // không cho sửa
                    textField.setEditable(false);

                    // khi mất focus thì quay lại mode bình thường
                    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal)
                            cancelEdit();
                    });

                    // Enter/Escape thoát edit mode
                    textField.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.ESCAPE) {
                            cancelEdit();
                        }
                    });

                    // double click -> vào chế độ select text
                    setOnMouseClicked(e -> {
                        if (isEmpty())
                            return;

                        String value = getItem();
                        if (e.isControlDown() && e.getButton() == MouseButton.PRIMARY) {
                            if (isImageUrl(value)) {
                                openInBrowser(value);
                                return;
                            }
                        }
                        if (e.getClickCount() == 2 && !isEmpty()) {
                            startEdit();
                            textField.requestFocus();
                            textField.selectAll(); // optional
                        }
                    });
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
                    setText(getItem());
                    setGraphic(null);
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

                    // =====================
                    // Highlight Row + Cell
                    // =====================

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

                        // ô hiện tại
                        setStyle("""
                                    -fx-background-color:#19d238;
                                    -fx-text-fill:white;
                                    -fx-font-weight:bold;
                                    -fx-border-color: red;
                                    -fx-border-width: 2;
                                """);

                    } else if (cellSelected) {

                        // vùng đang chọn
                        setStyle("""
                                    -fx-background-color:#52bd2b;
                                    -fx-text-fill:black;
                                """);

                    } else if (rowSelected) {

                        // cùng hàng với vùng chọn
                        setStyle("""
                                    -fx-background-color:#b3a227;
                                    -fx-text-fill:black;
                                """);

                    } else {

                        setStyle("");

                    }

                    setText(cfg.isNumber
                            ? numberFormatter.formatIfNumber(item)
                            : item);
                }
            });

            table.getColumns().add(col);
        }
        restoreColumnOrder(table);
        table.getColumns().addListener(
                (ListChangeListener<TableColumn<ProductBusiness, ?>>) c -> {
                    saveColumnOrder(table);
                });
    }

    // ================= COPY =================
    private void enableCopy(TableView<ProductBusiness> table) {

        table.setOnKeyPressed(event -> {

            if (event.isControlDown() && event.getCode() == KeyCode.C) {

                var selectedCells = table.getSelectionModel().getSelectedCells();
                if (selectedCells.isEmpty())
                    return;

                boolean singleCell = selectedCells.size() == 1;

                // ⭐ Gom dữ liệu theo row -> col
                Map<Integer, Map<Integer, String>> rowData = new TreeMap<>();

                for (TablePosition<?, ?> pos : selectedCells) {
                    int row = pos.getRow();
                    int col = table.getColumns().indexOf(pos.getTableColumn());

                    Object value = pos.getTableColumn().getCellData(row);
                    String text = value == null ? "" : value.toString();

                    // ⭐ chỉ clean khi copy 1 ô
                    if (singleCell) {
                        text = text
                                .replace("\n", " ")
                                .replace("\t", " ")
                                .replaceAll("\\s+", " ")
                                .trim();
                    }

                    rowData.computeIfAbsent(row, r -> new TreeMap<>())
                            .put(col, text);
                }

                // ⭐ Build text theo dạng bảng
                StringBuilder sb = new StringBuilder();

                for (var row : rowData.values()) {
                    for (var value : row.values()) {
                        sb.append(value).append("\t");
                    }
                    if (sb.length() > 0)
                        sb.setLength(sb.length() - 1); // bỏ tab cuối row
                    sb.append("\n");
                }

                if (sb.length() > 0)
                    sb.setLength(sb.length() - 1); // bỏ newline cuối

                ClipboardContent content = new ClipboardContent();
                content.putString(sb.toString());
                Clipboard.getSystemClipboard().setContent(content);

                event.consume();
            }
        });
    }

    // ================= ROW MENU =================
    private void highlightRows(TableView<ProductBusiness> table) {

        table.setRowFactory(tv -> {

            TableRow<ProductBusiness> row = new TableRow<>();

            ContextMenu menu = new ContextMenu();
            MenuItem edit = new MenuItem("Edit");
            MenuItem delete = new MenuItem("Delete");

            menu.getItems().addAll(edit, delete);

            row.setOnMouseClicked(e -> {
                if (e.isSecondaryButtonDown() && !row.isEmpty())
                    menu.show(row, e.getScreenX(), e.getScreenY());
                else
                    menu.hide();
            });

            return row;
        });
    }

    private Predicate<ProductBusiness> externalPredicate = p -> true;

    public void setExternalPredicate(Predicate<ProductBusiness> predicate) {
        this.externalPredicate = predicate;
        applyFilter();
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

    private void updateHeaderText(TableView<ProductBusiness> table) {

        for (TableColumn<ProductBusiness, ?> col : table.getColumns()) {

            String original = originalHeaders.getOrDefault(col, col.getText());

            if (columnFilters.containsKey(col)
                    && columnFilters.get(col) != null
                    && !columnFilters.get(col).isEmpty()) {

                col.setText(original + " 🔽");

            } else {

                col.setText(original);

            }
        }
    }

    private void saveColumnOrder(TableView<ProductBusiness> table) {

        try {

            String order = table.getColumns()
                    .stream()
                    .map(TableColumn::getId)
                    .collect(Collectors.joining(","));

            Path path = Paths.get("config", "product_business_columns.txt");

            Files.createDirectories(path.getParent());

            Files.writeString(
                    path,
                    order,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreColumnOrder(TableView<ProductBusiness> table) {

        try {

            Path path = Paths.get("config", "product_business_columns.txt");

            if (!Files.exists(path)) {
                return;
            }

            String order = Files.readString(path);

            if (order == null || order.isBlank()) {
                return;
            }

            Map<String, TableColumn<ProductBusiness, ?>> columnMap = table.getColumns()
                    .stream()
                    .collect(Collectors.toMap(
                            TableColumn::getId,
                            c -> c));

            List<TableColumn<ProductBusiness, ?>> sortedColumns = new ArrayList<>();

            for (String id : order.split(",")) {

                TableColumn<ProductBusiness, ?> col = columnMap.get(id.trim());

                if (col != null) {
                    sortedColumns.add(col);
                }
            }

            // thêm các cột mới chưa có trong file
            for (TableColumn<ProductBusiness, ?> col : table.getColumns()) {

                if (!sortedColumns.contains(col)) {
                    sortedColumns.add(col);
                }
            }

            table.getColumns().setAll(sortedColumns);
            columnMap.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}