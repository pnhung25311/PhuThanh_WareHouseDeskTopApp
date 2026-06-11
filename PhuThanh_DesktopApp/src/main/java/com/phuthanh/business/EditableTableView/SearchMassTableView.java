package com.phuthanh.business.EditableTableView;

import java.io.File;
import java.io.FileOutputStream;
import com.phuthanh.business.table.ColumnConfig;
import com.phuthanh.business.table.ProductBusinessColumns;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.model.business.ProductBusiness;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormat;

public class SearchMassTableView {
    private final TableView<StringProperty> table = new TableView<>();
    private final ObservableList<StringProperty> data = FXCollections.observableArrayList();
    private final ProductBusinessColumns productBusinessColumns = new ProductBusinessColumns();
    private final Stage stage;

    private final int typeSearch;
    private final ObservableList<ProductBusiness> items;
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();

    public SearchMassTableView(int typeSearch, Stage stage, ObservableList<ProductBusiness> items) {
        this.typeSearch = typeSearch;
        this.stage = stage;
        this.items = items;
        createTable();
    }

    public TableView<StringProperty> getTable() {
        return table;
    }

    public ObservableList<StringProperty> getData() {
        return data;
    }

    // ================= TABLE =================
    private void createTable() {

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);
        String header = switch (typeSearch) {
            case 1 -> "Mã sản phẩm";
            case 2 -> "Danh điểm";
            case 3 -> "Mã vạch";
            default -> "Value";
        };

        TableColumn<StringProperty, String> col = new TableColumn<>(header);

        col.setCellValueFactory(c -> c.getValue());

        col.setCellFactory(tc -> new TableCell<>() {

            private TextField textField;

            @Override
            public void startEdit() {
                if (isEmpty())
                    return;

                super.startEdit();
                createTextField();

                setText(null);
                setGraphic(textField);

                textField.selectAll();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem() == null ? "" : getItem());
                setGraphic(null);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    if (textField != null)
                        textField.setText(item);
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }

            private void createTextField() {

                textField = new TextField(getItem());

                textField.setOnAction(e -> commitEdit(textField.getText()));

                textField.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (!newV) {
                        commitEdit(textField.getText());
                    }
                });

                textField.setOnKeyPressed(e -> {

                    // TableView<StringProperty> tv = getTableView();

                    int row = getIndex();
                    // int col = tv.getColumns().indexOf(getTableColumn());

                    switch (e.getCode()) {

                        case ENTER -> {
                            commitEdit(textField.getText());
                            moveToCell(row + 1);
                            e.consume();
                        }

                        case DOWN -> {
                            commitEdit(textField.getText());
                            moveToCell(row + 1);
                            e.consume();
                        }

                        case UP -> {
                            commitEdit(textField.getText());
                            moveToCell(Math.max(0, row - 1));
                            e.consume();
                        }

                        default -> {
                        }
                    }
                });
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);

                StringProperty row = getTableView().getItems().get(getIndex());
                row.set(newValue);
            }
        });

        col.setPrefWidth(300);

        table.getColumns().add(col);
        table.setItems(data);

        enablePaste();
    }

    // ================= TOOLBAR =================
    public ToolBar createToolbar() {

        Button btnAdd = new Button("➕ Thêm dòng");
        Button btnDelete = new Button("🗑 Xóa dòng");
        Button btnClear = new Button("Clear");
        Button btnPrint = new Button("📤 Print Data");

        btnAdd.setOnAction(e -> addNewRow());
        btnDelete.setOnAction(e -> deleteSelectedRow());
        btnClear.setOnAction(e -> data.clear());
        btnPrint.setOnAction(e -> printAllData());

        return new ToolBar(btnAdd, btnDelete, btnClear, btnPrint);
    }

    // ================= ACTIONS =================
    public void addNewRow() {
        data.add(new SimpleStringProperty(""));
    }

    public void deleteSelectedRow() {
        int index = table.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            data.remove(index);
        }
    }

    // ================= PASTE EXCEL =================
    private void enablePaste() {

        table.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.V) {
                paste();
            }
        });
    }

    private void paste() {

        Clipboard cb = Clipboard.getSystemClipboard();
        if (!cb.hasString())
            return;

        String[] rows = cb.getString().split("\\r?\\n");

        // TablePosition<StringProperty, ?> focused =
        // table.getFocusModel().getFocusedCell();

        // int startRow = focused.getRow();
        int startRow = table.getFocusModel().getFocusedIndex();

        // ===== chưa focus cell → append =====
        if (startRow < 0) {
            for (String r : rows) {
                if (!r.isBlank()) {
                    data.add(new SimpleStringProperty(r));
                }
            }
            table.refresh();
            return;
        }

        // ===== paste theo vị trí =====
        for (int i = 0; i < rows.length; i++) {

            int targetRow = startRow + i;

            if (targetRow >= data.size()) {
                data.add(new SimpleStringProperty(""));
            }

            data.get(targetRow).set(rows[i]);
        }

        table.refresh();
    }

    // ================= MOVE CELL =================
    private void moveToCell(int row) {

        if (row < 0)
            row = 0;

        if (row >= table.getItems().size()) {
            addNewRow();
        }

        int targetRow = row;

        Platform.runLater(() -> {
            table.getSelectionModel().clearAndSelect(targetRow);
            table.getFocusModel().focus(targetRow);
            table.scrollTo(targetRow);
            table.edit(targetRow, table.getColumns().get(0));
        });
    }

    public void createBusinessColumns(TableView<ProductBusiness> table) {

        table.getColumns().clear();

        for (ColumnConfig cfg : productBusinessColumns.getColumns()) {

            TableColumn<ProductBusiness, String> col = new TableColumn<>(cfg.header);
            col.setCellValueFactory(cell -> new SimpleStringProperty(cfg.mapper.apply(cell.getValue())));
            col.setPrefWidth(cfg.width);
            col.setId(cfg.id);
            table.getColumns().add(col);
        }
    }

    public class SearchResultRow {

        private final String keyword;
        private final ProductBusiness product;
        private final int groupIndex;
        private final boolean showKeyword;

        public SearchResultRow(String keyword, ProductBusiness product, int groupIndex, boolean showKeyword) {
            this.keyword = keyword;
            this.product = product;
            this.groupIndex = groupIndex;
            this.showKeyword = showKeyword;
        }

        public String getKeyword() {
            return keyword;
        }

        public ProductBusiness getProduct() {
            return product;
        }

        public int getGroupIndex() {
            return groupIndex;
        }

        public boolean isShowKeyword() {
            return showKeyword;
        }
    }

    private void printAllData() {

        TableView<SearchResultRow> tv = new TableView<>();

        ObservableList<SearchResultRow> searchResult = FXCollections.observableArrayList();

        int groupIndex = 0;

        for (StringProperty property : data) {

            String keyword = property.get().trim();

            if (keyword.isEmpty()) {
                continue;
            }

            ObservableList<ProductBusiness> matched;

            if (typeSearch == 2) {

                matched = FXCollections.observableArrayList(
                        items.stream()
                                .filter(item -> (item.danhDiem != null
                                        && !item.danhDiem.isEmpty()
                                        && item.danhDiem.contains(keyword))
                                        ||
                                        (item.boDanhDiem != null
                                                && !item.boDanhDiem.isEmpty()
                                                && item.boDanhDiem.contains(keyword)))
                                .toList());

            } else {

                matched = FXCollections.observableArrayList(
                        items.stream()
                                .filter(item -> item.maVatTu != null
                                        && item.maVatTu.trim().equals(keyword))
                                .toList());
            }

            /*
             * Không tìm thấy
             */
            if (matched.isEmpty()) {

                searchResult.add(
                        new SearchResultRow(
                                keyword,
                                null,
                                groupIndex,
                                true));

            } else {

                /*
                 * Có kết quả
                 */
                for (int i = 0; i < matched.size(); i++) {

                    searchResult.add(
                            new SearchResultRow(
                                    keyword,
                                    matched.get(i),
                                    groupIndex,
                                    i == 0));
                }
            }

            groupIndex++;
        }

        try {

            tv.getColumns().clear();

            /*
             * Enter Part No.
             */
            String columnName = typeSearch == 2 ? "Enter Part No." : "Enter ProductID.";
            TableColumn<SearchResultRow, String> searchColumn = new TableColumn<>(columnName);

            searchColumn.setCellValueFactory(cell -> {

                SearchResultRow row = cell.getValue();

                return new SimpleStringProperty(
                        row.isShowKeyword()
                                ? row.getKeyword()
                                : "");
            });

            searchColumn.setPrefWidth(150);

            tv.getColumns().add(searchColumn);

            /*
             * Các cột ProductBusiness
             */
            for (ColumnConfig cfg : productBusinessColumns.getColumns()) {

                TableColumn<SearchResultRow, String> col = new TableColumn<>(cfg.header);

                col.setCellValueFactory(cell -> {

                    ProductBusiness product = cell.getValue().getProduct();

                    /*
                     * Không tìm thấy sản phẩm
                     */
                    if (product == null) {

                        /*
                         * Hiện thông báo ở cột mã vật tư
                         */
                        if ("maVatTu".equals(cfg.id)) {
                            return new SimpleStringProperty(" ");
                        }

                        return new SimpleStringProperty("");
                    }

                    return new SimpleStringProperty(
                            cfg.mapper.apply(product));
                });

                col.setPrefWidth(cfg.width);

                col.setId(cfg.id);

                tv.getColumns().add(col);
            }

            tv.setItems(searchResult);

            /*
             * Tô màu xen kẽ
             */
            tv.setRowFactory(table -> new TableRow<>() {

                @Override
                protected void updateItem(SearchResultRow item,
                        boolean empty) {

                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setStyle("");
                        return;
                    }

                    if (item.getGroupIndex() % 2 == 0) {

                        setStyle("""
                                -fx-background-color: #DCEEFF;
                                """);

                    } else {

                        setStyle("""
                                -fx-background-color: white;
                                """);
                    }
                }
            });
            String sheetName = typeSearch == 2 ? "Danh điểm" : "Mã sản phẩm";
            boolean success = exportExcel(tv, stage, sheetName);

            customDialogNotification.showDialog(
                    success ? "Thành công" : "Lỗi",
                    success
                            ? "Xuất Excel thành công"
                            : "Xuất Excel thất bại",
                    success
                            ? Alert.AlertType.INFORMATION
                            : Alert.AlertType.ERROR);

        } catch (Exception e) {

            e.printStackTrace();

            customDialogNotification.showDialog(
                    "Lỗi",
                    e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    public <T> boolean exportExcel(TableView<T> tableView, Stage stage, String sheetName) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(sheetName);

            // =========================
            // STYLE HEADER
            // =========================
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            headerStyle.setBorderBottom(BorderStyle.THIN);
            // =========================
            // MONEY STYLE (CHỈ DÙNG CHO CỘT TIỀN)
            // =========================
            CellStyle moneyStyle = workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            moneyStyle.setDataFormat(df.getFormat("#,##0"));
            

            // =========================
            // STYLE ROW EVEN / ODD
            // =========================
            CellStyle evenStyle = workbook.createCellStyle();
            evenStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            evenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle oddStyle = workbook.createCellStyle();
            oddStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            oddStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle evenMoneyStyle = workbook.createCellStyle();
evenMoneyStyle.cloneStyleFrom(evenStyle);
evenMoneyStyle.setDataFormat(df.getFormat("#,##0"));

CellStyle oddMoneyStyle = workbook.createCellStyle();
oddMoneyStyle.cloneStyleFrom(oddStyle);
oddMoneyStyle.setDataFormat(df.getFormat("#,##0"));

            // =========================
            // GET VISIBLE COLUMNS
            // =========================
            List<TableColumn<T, ?>> columns = tableView.getColumns().stream()
                    .filter(TableColumn::isVisible)
                    .toList();

            // =========================
            // HEADER ROW
            // =========================
            Row headerRow = sheet.createRow(0);

            for (int col = 0; col < columns.size(); col++) {
                TableColumn<T, ?> column = columns.get(col);

                Cell cell = headerRow.createCell(col);
                cell.setCellValue(column.getText());
                cell.setCellStyle(headerStyle);

                sheet.autoSizeColumn(col);
            }

            // =========================
            // DATA ROWS
            // =========================
            ObservableList<T> items = tableView.getItems();

            for (int rowIndex = 0; rowIndex < items.size(); rowIndex++) {

                T rowData = items.get(rowIndex);

                Row row = sheet.createRow(rowIndex + 1);

                // nếu bạn có class SearchResultRow
                int groupIndex = 0;
                if (rowData instanceof SearchResultRow sr) {
                    groupIndex = sr.getGroupIndex();
                }

                CellStyle rowStyle = (groupIndex % 2 == 0) ? evenStyle : oddStyle;

                for (int col = 0; col < columns.size(); col++) {

                    TableColumn<T, ?> column = columns.get(col);
                    Object value = column.getCellData(rowData);

                    Cell cell = row.createCell(col);

                    String colName = column.getText();

                    // =========================
                    // CHỈ FORMAT CỘT TIỀN
                    // =========================
boolean isMoney = isMoneyColumn(colName);

if (isMoney) {

    double num = 0;

    if (value == null || value.toString().trim().isEmpty()) {
        num = 0;
    } else if (value instanceof Number n) {
        num = n.doubleValue();
    } else {
        try {
            num = Double.parseDouble(value.toString().replace(",", "").trim());
        } catch (Exception e) {
            num = 0;
        }
    }

    cell.setCellValue(num);

    // 👇 QUAN TRỌNG: giữ màu row + format tiền
    cell.setCellStyle((groupIndex % 2 == 0) ? evenMoneyStyle : oddMoneyStyle);

} else {
                        cell.setCellValue(value == null ? "" : value.toString());
                        cell.setCellStyle(rowStyle);
                    }
                }
            }

            // =========================
            // SAVE FILE
            // =========================
            FileChooser fc = new FileChooser();
            fc.setTitle("Save Excel File");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

            File file = fc.showSaveDialog(stage);
            if (file == null)
                return false;

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isMoneyColumn(String colName) {
        return colName != null && (colName.equalsIgnoreCase("Giá") ||
                colName.equalsIgnoreCase("Thành tiền") ||
                colName.equalsIgnoreCase("Tổng tiền") ||
                colName.equalsIgnoreCase("Tiền") ||
                colName.toLowerCase().contains("giá") ||
                colName.toLowerCase().contains("tiền"));
    }

}
