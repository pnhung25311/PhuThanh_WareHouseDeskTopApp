package com.phuthanh.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
// import java.time.LocalDateTime;
// import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
// import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
// import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
// import java.util.Random;
// import java.util.stream.Collectors;
import java.util.stream.Collectors;

import com.phuthanh.core.ConvertHTMLtoPDF;
import com.phuthanh.custom.CustomCombobox.IdExtractor;
import com.phuthanh.custom.CustomDialogNotification;
import com.phuthanh.model.helper.ExcelColumn;
import com.phuthanh.model.info.Account;
import com.phuthanh.model.info.Location;
import com.phuthanh.model.info.Vehicle;
import com.phuthanh.model.warehouse.DrawerItem;
import com.phuthanh.model.warehouse.Product;
import com.phuthanh.model.warehouse.WareHouse;
import com.phuthanh.store.AppState;
import com.phuthanh.utils.ArrayCRUD;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
// import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
// import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
// import java.util.Iterator;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;

@SuppressWarnings("unchecked")
public class FunctionHelper {
    private final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private final CustomDialogNotification customDialogNotification = new CustomDialogNotification();
    private final ConvertHTMLtoPDF convertHTMLtoPDF = new ConvertHTMLtoPDF();
    private final ArrayCRUD arrayCRUD = new ArrayCRUD();

    /**
     * Chọn item trong ComboBox dựa trên id
     *
     * @param comboBox    ComboBox đã setup bằng CustomCombobox
     * @param id          id muốn chọn
     * @param idExtractor IdExtractor dùng để lấy id của item
     * @param <T>         Kiểu item trong ComboBox
     */
    public <T> void selectComboBoxItemById(
            ComboBox<T> comboBox,
            int id,
            IdExtractor<T> extractor) {

        // ⭐ nếu là SearchComboBox → bỏ qua
        if (Boolean.TRUE.equals(comboBox.getProperties().get("IS_SEARCH_COMBOBOX")))
            return;

        if (comboBox.getSelectionModel() == null)
            return;

        comboBox.getSelectionModel().clearSelection();

        for (T item : comboBox.getItems()) {
            if (extractor.getId(item) == id) {
                comboBox.getSelectionModel().select(item);
                break;
            }
        }
    }

    public <T> int getComboBoxItemById(
            ComboBox<T> cb,
            Function<T, Integer> idGetter,
            Function<T, String> nameGetter) {

        // Object value = cb.getValue();
        Object value = cb.getValue();
        String text = cb.getEditor().getText();
        System.out.println("value = " + value);
        System.out.println("text = " + text);

        // Nếu user đang gõ nhưng chưa commit
        if (text != null && !text.isBlank()) {
            value = text;
        }

        if (value == null) {
            // System.out.println("value null thật");
            return 0;
        }

        // Nếu đã là object đúng kiểu
        if (!cb.getItems().isEmpty() && value.getClass() == cb.getItems().get(0).getClass()) {
            T item = (T) value;
            return idGetter.apply(item);
        }

        // Nếu là String (do search / input)
        if (value instanceof String) {
            String input = normalizeVietnamese((String) value);

            for (T item : cb.getItems()) {
                Integer id = idGetter.apply(item);
                String name = nameGetter.apply(item);

                String itemName = normalizeVietnamese(name);
                String itemId = String.valueOf(id).trim();

                if (itemName.equals(input) || itemId.equals(input)) {
                    System.out.println(itemName + " só sánh " + input + " " + itemName.equals(input));
                    cb.setValue(item);
                    return id;
                }
            }
        }

        return 0;
    }

    private String normalizeVietnamese(String s) {
        if (s == null)
            return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        temp = temp.replaceAll("\\p{M}", ""); // remove dấu
        return temp.toLowerCase().trim();
    }

    /**
     * Chỉ cho phép nhập số trong TextField
     *
     * @param tf TextField cần áp dụng
     */
    public void allowOnlyNumber(TextField tf) {
        tf.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("-?\\d*(\\.\\d*)?")) {
                return change;
            }
            return null;
        }));
    }

    public void setupMoneyField(TextField tf) {

        TextFormatter<String> formatter = new TextFormatter<>(change -> {

            String newText = change.getControlNewText();

            if (newText.isEmpty())
                return change;

            String cleaned = newText
                    .replace(",", "")
                    .replaceAll("[\\r\\n\\t ]", "");

            if (!cleaned.matches("\\d*(\\.\\d*)?")) {
                return null;
            }

            // đồng thời thay luôn text được paste
            if (!cleaned.equals(newText.replace(",", ""))) {
                change.setText(change.getText().replaceAll("[\\r\\n\\t ]", ""));
            }

            return change;
        });

        tf.setTextFormatter(formatter);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');

        DecimalFormat df = new DecimalFormat("#,##0.###", symbols);

        tf.textProperty().addListener((obs, oldValue, newValue) -> {

            if (newValue == null || newValue.isEmpty())
                return;

            String raw = newValue.replace(",", "");

            if (raw.equals("."))
                return;

            try {

                double value = Double.parseDouble(raw);

                String formatted = df.format(value);

                if (!formatted.equals(newValue)) {

                    int caret = formatted.length();

                    tf.setText(formatted);
                    tf.positionCaret(caret);
                }

            } catch (NumberFormatException ignored) {
            }
        });
    }

    /**
     * Chỉ cho phép nhập số trong TextField
     *
     * @param locationIDs String chứa các ID vị trí, phân tách bằng dấu phẩy
     * @param locationMap Bản đồ ánh xạ từ ID vị trí sang tên vị
     */
    public String convertLocation(String locationIDs, List<Location> locations) {
        if (locationIDs == null || locationIDs.isBlank()) {
            return "";
        }

        List<String> names = new ArrayList<>();

        for (String idStr : locationIDs.split(",")) {
            String idTrim = idStr.trim();

            for (Location loc : locations) {
                if (String.valueOf(loc.getLocationID()).equals(idTrim)) {
                    names.add(loc.getNameLocation().trim());
                    break;
                }
            }
        }

        return String.join(", ", names);
    }

    public String convertVehicle(String vehicle, List<Vehicle> vehicles) {
        if (vehicle == null || vehicle.isBlank()) {
            return "";
        }

        List<String> names = new ArrayList<>();

        for (String idStr : vehicle.split(",")) {
            String idTrim = idStr.trim();

            for (Vehicle vehi : vehicles) {
                if (String.valueOf(vehi.getVehicleID()).equals(idTrim)) {
                    names.add(vehi.getVehicleTypeName().trim());
                    break;
                }
            }
        }

        return String.join(", ", names);
    }

    public String convertVehicleOptimized(String vehicle, java.util.Map<Integer, String> vehicleMap) {
        if (vehicle == null || vehicle.isBlank()) {
            return "";
        }

        List<String> names = new ArrayList<>();

        for (String idStr : vehicle.split(",")) {
            try {
                Integer vehicleId = Integer.parseInt(idStr.trim());
                String vehicleName = vehicleMap.get(vehicleId);
                if (vehicleName != null) {
                    names.add(vehicleName);
                }
            } catch (NumberFormatException e) {
                // Skip invalid IDs
            }
        }

        return String.join(", ", names);
    }

    public String toLower(String text) {
        if (text == null)
            return null;
        return text.toLowerCase();
    }

    public String convertDate(LocalDate date) {
        if (date == null)
            return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateStr = date.format(formatter);
        return dateStr;
    }

    /**
     * Export TableView data to Excel (.xlsx)
     *
     * @param tableView TableView cần export
     * @param stage     Stage để mở FileChooser
     * @param sheetName Tên Sheet trong Excel
     * @return true nếu xuất thành công, false nếu thất bại
     */
    public <T> boolean exportExcel(TableView<T> tableView, Stage stage, String sheetName) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(sheetName);

            // Lấy danh sách cột đang được hiển thị (visible)
            List<TableColumn<T, ?>> visibleColumns = tableView.getColumns().stream()
                    .filter(TableColumn::isVisible) // chỉ lấy visible
                    .toList();

            // Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < visibleColumns.size(); i++) {
                TableColumn<T, ?> column = visibleColumns.get(i);
                headerRow.createCell(i).setCellValue(column.getText());
                sheet.autoSizeColumn(i);
            }

            // Data
            for (int rowIndex = 0; rowIndex < tableView.getItems().size(); rowIndex++) {
                Row excelRow = sheet.createRow(rowIndex + 1);
                T row = tableView.getItems().get(rowIndex);

                for (int col = 0; col < visibleColumns.size(); col++) {
                    TableColumn<T, ?> column = visibleColumns.get(col);
                    Object cellValue = column.getCellData(row);
                    excelRow.createCell(col)
                            .setCellValue(cellValue == null ? "" : cellValue.toString());
                }
            }

            // Save file
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

    /**
     * Import dữ liệu từ file Excel vào bảng trong cơ sở dữ liệu.
     *
     * @param columnsProduct Danh sách tên cột tương ứng với thứ tự cột trong Excel.
     * @param excelPath      Đường dẫn tới file Excel (.xlsx).
     */
    public void importExcelProduct(
            String excelPath,
            BiConsumer<Integer, Integer> progress) {

        Connection conn = null;
        PreparedStatement ps = null;
        Workbook workbook = null;
        FileInputStream fis = null;

        StringBuilder errorMsg = new StringBuilder();
        int errorCount = 0;

        try {
            // ---------- EXCEL ----------
            fis = new FileInputStream(excelPath);
            workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            // ---------- DB ----------
            conn = DbHelper.getConnection();
            conn.setAutoCommit(false); // 🔥 BẮT BUỘC

            String sql = """
                        INSERT INTO %s (
                            ProductIDMain,
                            ProductID,
                            ID_Keeton,
                            ID_Industrial,
                            ID_PartNo,
                            ID_ReplacedPartNo,
                            NameProduct,
                            Parameter,
                            VehicleTypeID,
                            VehicleDetail,
                            VehicleCluster,
                            ManufacturerID,
                            CountryID,
                            SupplierActualID,
                            SupplierID,
                            UnitID,
                            "SegmentID",
                            "PurposeID",
                            Img1,
                            Img2,
                            Img3,
                            Remark,
                            LastTime
                        )
                        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """.formatted("Product");

            ps = conn.prepareStatement(sql);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                // ---------- READ EXCEL ----------
                Cell c0 = row.getCell(0);
                Cell c1 = row.getCell(1);
                Cell c2 = row.getCell(2);
                Cell c3 = row.getCell(3);
                Cell c4 = row.getCell(4);
                Cell c5 = row.getCell(5);
                Cell c6 = row.getCell(6);
                Cell c7 = row.getCell(7);
                Cell c8 = row.getCell(8);
                Cell c9 = row.getCell(9);
                Cell c10 = row.getCell(10);
                Cell c11 = row.getCell(11);
                Cell c12 = row.getCell(12);
                Cell c13 = row.getCell(13);
                Cell c14 = row.getCell(14);
                Cell c15 = row.getCell(15);
                Cell c16 = row.getCell(16);
                Cell c17 = row.getCell(17);
                Cell c18 = row.getCell(18);
                Cell c19 = row.getCell(19);
                Cell c20 = row.getCell(20);

                String productID = toString(c0);

                if (productID == null || productID.isBlank()) {
                    continue;
                }

                if (dbCRUDHelper.isProductIdExists(productID)) {
                    errorMsg.append("Mã ").append(productID).append(" đã tồn tại");
                    errorCount++;
                    break;
                }

                // ---------- VALIDATE ----------
                // if (!isValidCode(productID)) {
                // errorMsg.append("Mã ").append(productID).append(" không đúng định dạng");
                // errorCount++;
                // break;
                // }

                String condition = toString(c3) == null ? toString(c6).trim() : toString(c3).trim();
                if (condition.isEmpty() || condition.isBlank() || condition == null || condition.equals("")) {
                    System.out.println("condition = " + condition);
                    errorMsg.append("Mã ").append(productID)
                            .append(" Bạn phải nhập Mã danh điểm hoặc Thông số kỹ thuật!");

                    errorCount++;
                    break;
                }
                System.out.println("condition = " + condition);

                if (toString(c13) == null || toString(c12) == null || toString(c10) == null || toString(c11) == null
                        || toString(c14) == null) {
                    errorMsg.append("Mã ").append(productID).append(" Bạn phải nhập đầy đủ thông tin các tiêu chí!");

                    errorCount++;
                    break;

                }

                if (dbInfoHelper.checkCriteriaProduct(condition, toInt(c13), toInt(c12), toInt(c10), toInt(c11),
                        toInt(c14))) {
                    System.out.println("checkCriteriaProduct = " + condition + " " + toInt(c13) + " " + toInt(c12) + " "
                            + toInt(c10) + " " + toInt(c11) + " " + toInt(c14));
                    errorMsg.append("Mã ").append(productID).append(
                            " Với các tiêu chí đã nhập đã tồn tại sản phẩm nào trong hệ thống, vui lòng kiểm tra lại!");
                    errorCount++;
                    break;

                }

                String productIDMain = removePrefixLetters(productID);

                if (dbCRUDHelper.isProductIdExists(productID)) {
                    errorMsg.append("Mã ").append(productID).append(" đã tồn tại");
                    errorCount++;
                    break;
                }

                // ---------- SET PARAM ----------
                int i = 1;
                ps.setString(i++, productIDMain);
                setNullableString(ps, i++, c0); // ProductID
                setNullableString(ps, i++, c1); // ID_Keton
                setNullableString(ps, i++, c2); // ID_Industrial
                setNullableString(ps, i++, c3); // ID_PartNo
                setNullableString(ps, i++, c4); // ID_ReplacedPartNo
                setNullableString(ps, i++, c5); // NameProduct
                setNullableString(ps, i++, c6); // Parameter
                setNullableString(ps, i++, c7); // VehicleTypeID
                setNullableString(ps, i++, c8); // VehicleDetail
                setNullableString(ps, i++, c9); // VehicleCluster

                setNullableInt(ps, i++, c10); // ManufacturerID
                setNullableInt(ps, i++, c11); // CountryID
                setNullableInt(ps, i++, c12); // SupplierActualID
                setNullableInt(ps, i++, c13); // SupplierID
                setNullableInt(ps, i++, c14); // UnitID
                setNullableInt(ps, i++, c15); // Mảng kinh doanh
                setNullableInt(ps, i++, c16); // mục đích

                setNullableString(ps, i++, c17); // Img1
                setNullableString(ps, i++, c18); // Img2
                setNullableString(ps, i++, c19); // Img3
                setNullableString(ps, i++, c20); // Remark

                ps.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
                ps.addBatch();

                currentRow++;
                progress.accept(currentRow, totalRows);

                // ---------- BATCH ----------
                if (currentRow % 500 == 0) {
                    ps.executeBatch();
                    ps.clearBatch();
                }
            }

            // ---------- RESULT ----------
            if (errorCount == 0) {
                ps.executeBatch();
                conn.commit();

                Platform.runLater(() -> {
                    customDialogNotification.showDialog(
                            "Thành công",
                            "Nhập excel sản phẩm thành công",
                            Alert.AlertType.INFORMATION);
                });
            } else {
                conn.rollback();

                Platform.runLater(() -> {
                    customDialogNotification.showDialog(
                            "Thất bại",
                            errorMsg.toString(),
                            Alert.AlertType.ERROR);
                });
            }

        } catch (Exception ex) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                customDialogNotification.showDialog(
                        "Lỗi hệ thống",
                        ex.getMessage(),
                        Alert.AlertType.ERROR);
            });

        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
                if (workbook != null)
                    workbook.close();
                if (fis != null)
                    fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void importUpdateProduct(
            String excelPath,
            BiConsumer<Integer, Integer> progress) {

        final int BATCH_SIZE = 500;

        // StringBuilder error = new StringBuilder();

        try (Connection conn = DbHelper.getConnection();
                FileInputStream fis = new FileInputStream(excelPath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            conn.setAutoCommit(false);

            Sheet sheet = workbook.getSheetAt(1);
            Row headerRow = sheet.getRow(0);

            Map<Integer, String> columnMap = new LinkedHashMap<>();
            int productIdColumn = -1;

            // ===== READ HEADER =====
            // ===== READ HEADER =====
            for (Cell cell : headerRow) {

                String rawHeader = toString(cell);

                String header = normalizeHeader(rawHeader);

                System.out.println("Excel Header = [" + header + "]");

                if (header.equals("ma san pham")) {
                    productIdColumn = cell.getColumnIndex();
                    continue;
                }

                if (arrayCRUD.HEADER_MAPPING.containsKey(header)) {

                    columnMap.put(
                            cell.getColumnIndex(),
                            arrayCRUD.HEADER_MAPPING.get(header));

                    System.out.println(
                            "Mapped: " + header +
                                    " -> " +
                                    arrayCRUD.HEADER_MAPPING.get(header));
                }
            }
            if (productIdColumn == -1)
                throw new RuntimeException("Thiếu cột Mã sản phẩm");

            // ===== BUILD SQL =====
            StringBuilder sql = new StringBuilder("UPDATE Product SET ");

            for (String col : columnMap.values()) {
                sql.append(col)
                        .append(" = COALESCE(?, ")
                        .append(col)
                        .append("),");
            }

            sql.append("LastTime = GETDATE()");
            sql.append(
                    " WHERE LTRIM(RTRIM(UPPER(ProductID))) = ?");
            System.out.println(sql.toString());

            PreparedStatement psUpdate = conn.prepareStatement(sql.toString());

            int totalRows = sheet.getLastRowNum();
            int batchCount = 0;
            int currentRow = 0;

            // ===== LOOP =====
            for (int i = 1; i <= totalRows; i++) {

                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                String productID = normalizeProductID(
                        toString(
                                row.getCell(productIdColumn)));

                if (productID.isBlank())
                    continue;

                int paramIndex = 1;

                for (Integer colIndex : columnMap.keySet()) {

                    String value = toString(row.getCell(colIndex));

                    if (value.isBlank())
                        psUpdate.setNull(
                                paramIndex++, Types.VARCHAR);
                    else
                        psUpdate.setString(
                                paramIndex++, value);
                }

                psUpdate.setString(paramIndex, productID);

                // ⭐ ADD BATCH
                psUpdate.addBatch();
                batchCount++;

                // ⭐ EXECUTE EACH 500 ROW
                if (batchCount % BATCH_SIZE == 0) {

                    psUpdate.executeBatch();
                    psUpdate.clearBatch();

                    System.out.println(
                            "Executed batch: " + batchCount);
                }

                progress.accept(++currentRow, totalRows);
            }

            // ===== LAST BATCH =====
            psUpdate.executeBatch();

            conn.commit();

            Platform.runLater(() -> customDialogNotification.showDialog(
                    "Thành công",
                    "Import Excel thành công",
                    Alert.AlertType.INFORMATION));

        } catch (Exception e) {

            e.printStackTrace();

            Platform.runLater(() -> customDialogNotification.showDialog(
                    "Lỗi",
                    "Import thất bại",
                    Alert.AlertType.ERROR));
        }
    }

    public void importDeleteProduct(
            String excelPath,
            BiConsumer<Integer, Integer> progress) {

        Connection conn = null;
        FileInputStream fis = null;
        Workbook workbook = null;

        try {
            conn = DbHelper.getConnection();
            conn.setAutoCommit(false);

            fis = new FileInputStream(excelPath);
            workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(2);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;
            StringBuilder str = new StringBuilder();

            Account acc = AppState.getInstance().get("Account", Account.class);

            // 🔴 chuẩn bị câu INSERT 1 lần duy nhất
            String sqlInsert = """
                        INSERT INTO RequestProduct (
                            ProductAID, ProductID, ID_Keeton, ID_Industrial, ID_PartNo,
                            ID_ReplacedPartNo, NameProduct, Parameter, VehicleTypeID,
                            VehicleDetail, VehicleCluster, ManufacturerID, CountryID,
                            SupplierActualID, SupplierID, UnitID,
                            Img1, Img2, Img3, RemarkOfProduct, LastTimeOfProduct,
                            UserRequest, TimeRequest
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            PreparedStatement psInsert = conn.prepareStatement(sqlInsert);

            int batchSize = 500; // số record mỗi lần đẩy
            int batchCount = 0;
            boolean hasError = false;

            for (Row row : sheet) {

                if (row.getRowNum() == 0)
                    continue;

                Cell cell = row.getCell(0);
                if (cell == null) {
                    currentRow++;
                    progress.accept(currentRow, totalRows);
                    continue;
                }

                String productID = cell.getStringCellValue().trim();
                if (productID.isEmpty()) {
                    currentRow++;
                    progress.accept(currentRow, totalRows);
                    continue;
                }

                // 🔎 kiểm tra Product tồn tại
                String sqlProduct = "SELECT * FROM Product WHERE ProductID = ?";
                try (PreparedStatement psCheck = conn.prepareStatement(sqlProduct)) {
                    psCheck.setString(1, productID);
                    ResultSet rs = psCheck.executeQuery();

                    if (!rs.next()) {
                        currentRow++;
                        progress.accept(currentRow, totalRows);
                        continue;
                    }

                    String productAID = rs.getString("ProductAID");

                    // ❌ đã có trong RequestProduct
                    if (dbCRUDHelper.isWareHouseExists(productAID, "RequestProduct")) {
                        str.append("Mã ").append(productID).append(" đã có trong yêu cầu\n");
                        hasError = true;
                        break;
                    }

                    // 🔴 add batch thay vì executeUpdate
                    psInsert.setString(1, rs.getString("ProductAID"));
                    psInsert.setString(2, rs.getString("ProductID"));
                    psInsert.setString(3, rs.getString("ID_Keeton"));
                    psInsert.setString(4, rs.getString("ID_Industrial"));
                    psInsert.setString(5, rs.getString("ID_PartNo"));
                    psInsert.setString(6, rs.getString("ID_ReplacedPartNo"));
                    psInsert.setString(7, rs.getString("NameProduct"));
                    psInsert.setString(8, rs.getString("Parameter"));
                    psInsert.setString(9, rs.getString("VehicleTypeID"));
                    psInsert.setString(10, rs.getString("VehicleDetail"));
                    psInsert.setString(11, rs.getString("VehicleCluster"));
                    psInsert.setString(12, rs.getString("ManufacturerID"));
                    psInsert.setString(13, rs.getString("CountryID"));
                    psInsert.setString(14, rs.getString("SupplierActualID"));
                    psInsert.setString(15, rs.getString("SupplierID"));
                    psInsert.setString(16, rs.getString("UnitID"));
                    psInsert.setString(17, rs.getString("Img1"));
                    psInsert.setString(18, rs.getString("Img2"));
                    psInsert.setString(19, rs.getString("Img3"));
                    psInsert.setString(20, rs.getString("Remark"));
                    psInsert.setTimestamp(21, rs.getTimestamp("LastTime"));
                    psInsert.setString(22, acc != null ? acc.getUserName() : "");
                    psInsert.setTimestamp(23, new Timestamp(System.currentTimeMillis()));

                    psInsert.addBatch();
                    batchCount++;

                    // 🚀 đủ batch → đẩy 1 lần
                    if (batchCount % batchSize == 0) {
                        psInsert.executeBatch();
                        psInsert.clearBatch();
                    }
                }

                currentRow++;
                progress.accept(currentRow, totalRows);
            }

            // 🔴 đẩy phần còn dư
            psInsert.executeBatch();
            psInsert.close();

            // 🔴 commit / rollback
            if (!hasError) {
                conn.commit();
                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thành công",
                        "Nhập excel thành công",
                        Alert.AlertType.INFORMATION));
            } else {
                conn.rollback();
                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thất bại",
                        str.toString(),
                        Alert.AlertType.ERROR));
            }

        } catch (Exception e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ignored) {
            }
            e.printStackTrace();

            Platform.runLater(() -> customDialogNotification.showDialog(
                    "Lỗi",
                    "Xử lý Excel thất bại",
                    Alert.AlertType.ERROR));
        } finally {
            try {
                if (workbook != null)
                    workbook.close();
            } catch (Exception ignored) {
            }
            try {
                if (fis != null)
                    fis.close();
            } catch (Exception ignored) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void importExcelWareHouse(
            String tableName,
            String excelPath,
            BiConsumer<Integer, Integer> progress) {

        Connection conn = null;
        PreparedStatement ps = null;
        Workbook workbook = null;
        FileInputStream fis = null;

        StringBuilder str = new StringBuilder();
        int currentSuccsess = 0;

        try {
            // ---------- EXCEL ----------
            fis = new FileInputStream(excelPath);
            workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            // ---------- DB ----------
            conn = DbHelper.getConnection();
            conn.setAutoCommit(false); // 🔥 BẮT BUỘC

            Account accountFromState = AppState.getInstance().get("Account", Account.class);

            String insertSQL = """
                        INSERT INTO %s (
                            ProductAID, Qty, Qty_Expected,
                            ID_Bill, LocationID, LastTime, LastUser, Remark
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """.formatted(tableName);

            ps = conn.prepareStatement(insertSQL);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                String excelProductID = toString(row.getCell(0)).trim();
                // double qtyExpected = toDouble(row.getCell(1));
                String idBill = toString(row.getCell(2));
                String excelLocation = toString(row.getCell(3));
                String remark = toString(row.getCell(4));

                // ❌ VALIDATE
                // if (!isValidCode(excelProductID)) {
                // str.append("Mã ").append(excelProductID).append(" không đúng định dạng");
                // currentSuccsess++;
                // break;
                // }

                String productAID = dbCRUDHelper.returnAID(
                        "Product", "ProductAID", "ProductID", excelProductID);

                if (dbCRUDHelper.isWareHouseExists(productAID, tableName)) {
                    str.append("Mã ").append(excelProductID).append(" đã tồn tại trong kho");
                    currentSuccsess++;
                    break;
                }

                Timestamp now = new Timestamp(System.currentTimeMillis());

                int i = 1;
                ps.setString(i++, productAID);
                ps.setInt(i++, 0);
                // ps.setDouble(i++, qtyExpected);
                setNullableDouble(ps, i++, row.getCell(1));
                // setNullableDouble(ps, i++, null);
                ps.setString(i++, idBill);
                ps.setString(i++, excelLocation);
                ps.setTimestamp(i++, now);
                ps.setString(i++, accountFromState.getUserName());
                ps.setString(i++, remark);
                ps.addBatch();

                currentRow++;
                progress.accept(currentRow, totalRows);

                // 🚀 batch mỗi 500 dòng
                if (currentRow % 500 == 0) {
                    ps.executeBatch();
                    ps.clearBatch();
                }
            }

            // ---------- KẾT QUẢ ----------
            if (currentSuccsess == 0) {
                ps.executeBatch();
                conn.commit(); // ✅ CHỈ COMMIT KHI KHÔNG LỖI

                Platform.runLater(() -> {
                    customDialogNotification.showDialog(
                            "Thành công",
                            "Nhập excel thành công",
                            Alert.AlertType.INFORMATION);
                });
            } else {
                conn.rollback(); // 🔥 HỦY TOÀN BỘ

                Platform.runLater(() -> {
                    customDialogNotification.showDialog(
                            "Thất bại",
                            str.toString(),
                            Alert.AlertType.ERROR);
                });
            }

        } catch (Exception ex) {
            // 🔥 LỖI BẤT KỲ → ROLLBACK
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ex.printStackTrace();

            Platform.runLater(() -> {
                customDialogNotification.showDialog(
                        "Lỗi hệ thống",
                        ex.getMessage(),
                        Alert.AlertType.ERROR);
            });

        } finally {
            // ---------- ĐÓNG TÀI NGUYÊN ----------
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
                if (workbook != null)
                    workbook.close();
                if (fis != null)
                    fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void importExcelWareHouseUpdate(
            String tableName,
            String excelPath,
            BiConsumer<Integer, Integer> progress) {
        try {
            FileInputStream fis = new FileInputStream(excelPath);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(1); // sheet update

            Connection conn = DbHelper.getConnection();
            conn.setAutoCommit(false);

            Account accountFromState = AppState.getInstance().get("Account", Account.class);
            DrawerItem selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);

            String updateSQL = """
                        UPDATE %s
                        SET
                            Qty_Expected = COALESCE(?, Qty_Expected),
                            ID_Bill      = COALESCE(?, ID_Bill),
                            LocationID   = COALESCE(?, LocationID),
                            Remark       = COALESCE(?, Remark),
                            LastTime     = ?,
                            LastUser     = ?
                        WHERE DataWareHouseAID = ?
                    """.formatted(tableName);

            PreparedStatement psUpdate = conn.prepareStatement(updateSQL);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;
            int batchSize = 300;
            int batchCount = 0;
            int currentSuccsess = 0;
            StringBuilder str = new StringBuilder();

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                String excelProductID = toString(row.getCell(0));
                double qtyExpected = toDouble(row.getCell(1));
                String idBill = toString(row.getCell(2));
                String excelLocation = toString(row.getCell(3));
                String remark = toString(row.getCell(4));

                // if (!isValidCode(excelProductID)) {
                // str.append("Mã ").append(excelProductID).append(" không đúng định dạng");
                // currentSuccsess++;
                // conn.rollback();
                // break;
                // }

                // ❌ Product không tồn tại
                if (!dbCRUDHelper.isProductIdExists(excelProductID)) {
                    currentRow++;
                    progress.accept(currentRow, totalRows);
                    str.append("Mã ").append(excelProductID).append(" không tồn tại");
                    currentSuccsess++;
                    conn.rollback();
                    break;
                }

                String productAID = dbCRUDHelper.returnAID(
                        "Product", "ProductAID", "ProductID", excelProductID);

                // ❌ Chưa có trong kho
                if (!dbCRUDHelper.isWareHouseExists(productAID, tableName)) {
                    currentRow++;
                    progress.accept(currentRow, totalRows);
                    str.append("Mã ").append(excelProductID).append(" không tồn tại trong kho");
                    currentSuccsess++;
                    conn.rollback();
                    break;
                }

                String wareHouseAID = dbCRUDHelper.returnAID(
                        selectedDrawerItem.getWareHouseTable(),
                        "DataWareHouseAID",
                        "ProductAID",
                        productAID);

                Timestamp now = new Timestamp(System.currentTimeMillis());
                int lastUser = accountFromState.getAccountID();

                int u = 1;
                psUpdate.setDouble(u++, qtyExpected);
                psUpdate.setString(u++, idBill);
                psUpdate.setString(u++, excelLocation);
                psUpdate.setString(u++, remark);
                psUpdate.setTimestamp(u++, now);
                psUpdate.setInt(u++, lastUser);
                psUpdate.setString(u++, wareHouseAID);

                psUpdate.addBatch();
                batchCount++;
                currentRow++;

                // 🔥 update ProgressBar
                progress.accept(currentRow, totalRows);

                if (batchCount % batchSize == 0) {
                    psUpdate.executeBatch();
                    psUpdate.clearBatch();
                }
            }

            if (currentSuccsess == 0) {
                psUpdate.executeBatch();
                conn.commit();

                psUpdate.close();
                conn.close();
                workbook.close();
                fis.close();

                Platform.runLater(() -> {
                    customDialogNotification.showDialog(
                            "Thành công",
                            "Nhập excel thành công",
                            Alert.AlertType.INFORMATION);
                });
            } else {
                conn.rollback();
                Platform.runLater(() -> {
                    customDialogNotification.showDialog(
                            "Thất bại",
                            str.toString(),
                            Alert.AlertType.ERROR);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                customDialogNotification.showDialog(
                        "Lỗi",
                        "Xử lý Excel thất bại",
                        Alert.AlertType.ERROR);
            });
        }
    }

    public void insertFromWarehouseByProductID(
            String excelPath,
            String table,
            BiConsumer<Integer, Integer> progress) {
        try {
            Connection conn = DbHelper.getConnection();
            conn.setAutoCommit(false);

            FileInputStream fis = new FileInputStream(excelPath);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(2); // sheet delete

            DrawerItem selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            Account accountFromState = AppState.getInstance().get("Account", Account.class);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;
            int currentSuccsess = 0;
            StringBuilder str = new StringBuilder();

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                Cell cell = row.getCell(0);
                if (cell == null) {
                    currentRow++;
                    progress.accept(currentRow, totalRows);
                    continue;
                }

                String productID = cell.getStringCellValue().trim();
                if (productID.isEmpty()) {
                    currentRow++;
                    progress.accept(currentRow, totalRows);
                    continue;
                }
                // if (!isValidCode(productID)) {
                // str.append("Mã ").append(productID).append(" không đúng định dạng");
                // currentSuccsess++;
                // conn.rollback();
                // break;
                // }

                String productAID = dbCRUDHelper.returnAID(
                        "Product", "ProductAID", "ProductID", productID);

                // ❌ đã có trong thùng rác
                if (dbCRUDHelper.isWareHouseExists(productAID, table)) {
                    currentRow++;
                    progress.accept(currentRow, totalRows);
                    str.append("Mã ").append(productID).append(" đã có trong yêu cầu");
                    currentSuccsess++;
                    conn.rollback();
                    break;
                }

                String whAID = dbCRUDHelper.returnAID(
                        selectedDrawerItem.getWareHouseDataBase(),
                        "DataWareHouseAID",
                        "ProductAID",
                        productAID);

                // ❌ không tồn tại trong kho
                if (whAID == null) {
                    currentRow++;
                    progress.accept(currentRow, totalRows);
                    str.append("Mã ").append(productID).append(" không có trong kho");
                    currentSuccsess++;
                    conn.rollback();
                    break;
                }

                WareHouse wh = dbInfoHelper.getWareHouseByAID(whAID);

                String sqlInsert = """
                            INSERT INTO %s (
                                DataWareHouseAID, ProductAID,
                                Qty, Qty_Expected, ID_Bill, LocationID,
                                LastTimeOfWareHouse, LastUser, RemarkOfWareHouse,
                                UserRequest, TimeRequest, UserConfirm, TimeConfirm,
                                RemarkOfRequest, LastTimeOfRequest
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(),
                                    NULL, NULL, NULL, GETDATE())
                        """.formatted(selectedDrawerItem.getWareHouseRequestDataBase());

                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    int i = 1;
                    ps.setString(i++, wh.getDataWareHouseAID());
                    ps.setString(i++, wh.getProductAID());
                    ps.setDouble(i++, wh.getQty());
                    ps.setDouble(i++, wh.getQty_Expected());
                    ps.setString(i++, wh.getID_Bill());
                    ps.setString(i++, wh.getLocationID());
                    ps.setString(i++, wh.getLastTime().toString());
                    ps.setString(i++, wh.getLastUser());
                    ps.setString(i++, wh.getRemark());
                    ps.setString(i++, accountFromState.getUserName());
                    ps.executeUpdate();
                }

                currentRow++;
                progress.accept(currentRow, totalRows);
            }
            if (currentSuccsess == 0) {
                conn.commit();
                conn.close();
                workbook.close();
                fis.close();

                // ✅ GIỮ DIALOG THÀNH CÔNG
                Platform.runLater(() -> {
                    customDialogNotification.showDialog(
                            "Thành công",
                            "Xử lý Excel thành công",
                            Alert.AlertType.INFORMATION);
                });
            } else {
                conn.rollback();
                Platform.runLater(() -> {
                    customDialogNotification.showDialog(
                            "Thất bại",
                            str.toString(),
                            Alert.AlertType.ERROR);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                customDialogNotification.showDialog(
                        "Lỗi",
                        "Xử lý Excel thất bại",
                        Alert.AlertType.ERROR);
            });
        }
    }

    public void importExcelDetailProduct(
            String excelPath,
            BiConsumer<Integer, Integer> progressCallback) {

        try (FileInputStream fis = new FileInputStream(excelPath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            Connection conn = DbHelper.getConnection();
            conn.setAutoCommit(false);

            String insertSQL = """
                        INSERT INTO DetailsProduct (
                            ProductID,
                            ID_PartNo,
                            PartNoID,
                            NameEnglish,
                            NameVietNamese,
                            PartNoQty,
                            Parameter,
                            Remark,
                            LastTime
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            PreparedStatement ps = conn.prepareStatement(insertSQL);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                currentRow++;

                String excelProductID = toString(row.getCell(0));
                String idpartNo = toString(row.getCell(1));
                String partNoID = toString(row.getCell(2));
                String nameEN = toString(row.getCell(3));
                String nameVN = toString(row.getCell(4));
                int partNoQty = toInt(row.getCell(5));
                String parameter = toString(row.getCell(6));
                String remark = toString(row.getCell(7));

                int i = 1;
                ps.setString(i++, excelProductID);
                ps.setString(i++, idpartNo);
                ps.setString(i++, partNoID);
                ps.setString(i++, nameEN);
                ps.setString(i++, nameVN);
                ps.setInt(i++, partNoQty);
                ps.setString(i++, parameter);
                ps.setString(i++, remark);
                ps.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));

                ps.addBatch();

                // ✅ UPDATE PROGRESS
                if (progressCallback != null) {
                    progressCallback.accept(currentRow, totalRows);
                }
            }

            ps.executeBatch();
            conn.commit();

            ps.close();
            conn.close();

            Platform.runLater(() -> {
                customDialogNotification.showDialog(
                        "Thành công",
                        "Đã import dòng",
                        Alert.AlertType.INFORMATION);
            });

        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                customDialogNotification.showDialog(
                        "Lỗi",
                        "Import Excel thất bại",
                        Alert.AlertType.ERROR);
            });
        }
    }

    public void importExcelHistoryExport(
            String excelPath,
            BiConsumer<Integer, Integer> progress) throws Exception {

        Connection conn = null;
        PreparedStatement psInsert = null;
        PreparedStatement psUpdate = null;

        // 🔥 Cache qty theo AID để xử lý mã trùng
        Map<String, Double> qtyCache = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(excelPath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(3);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;
            boolean hasError = false;

            DrawerItem selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            Account user = AppState.getInstance().get("Account", Account.class);

            StringBuilder errorMsg = new StringBuilder();

            conn = DbHelper.getConnection();
            conn.setAutoCommit(false); // 🔴 TRANSACTION

            // ---------- INSERT HISTORY ----------
            String insertHistorySQL = """
                    INSERT INTO %s (
                        DataWareHouseAID,
                        Qty,
                        ID_Employee,
                        Partner,
                        Remark,
                        Time,
                        LastUser,
                        LastTime
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())
                    """.formatted(selectedDrawerItem.getWareHouseDataBaseHistory());

            // ---------- UPDATE WAREHOUSE ----------
            String updateWarehouseSQL = """
                    UPDATE %s
                    SET Qty = ?,
                        LocationID = COALESCE(?, LocationID),
                        LastUser = ?,
                        LastTime = GETDATE()
                    WHERE DataWareHouseAID = ?
                    """.formatted(selectedDrawerItem.getWareHouseDataBase());

            psInsert = conn.prepareStatement(insertHistorySQL);
            psUpdate = conn.prepareStatement(updateWarehouseSQL);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                // ---------- READ EXCEL ----------
                String productId = toString(row.getCell(0));
                if (productId == null || productId.isBlank() || productId.isBlank()) {
                    continue;
                }
                if (productId != null) {
                    productId = productId.replace("\u00A0", "")
                            .replaceAll("\\s+", "")
                            .trim();
                }

                // if (!isValidCode(productId)) {
                // errorMsg.append("Mã ").append(productId).append(" không đúng định dạng");
                // hasError = true;
                // break;
                // }

                double qty = toDouble(row.getCell(1));
                if (qty <= 0) {
                    errorMsg.append("Mã ").append(productId)
                            .append(" số lượng xuất phải lớn hơn 0");
                    hasError = true;
                    break;
                }

                LocalDateTime now = LocalDateTime.now();
                Timestamp timestamp = Timestamp.valueOf(now);

                int idEmployee = toInt(row.getCell(2));
                int partner = toInt(row.getCell(3));
                String remark = toString(row.getCell(4));
                Timestamp time = toTimestampOrNow(row.getCell(5));
                String location = toString(row.getCell(6));

                String aid = dbCRUDHelper.returnAID(
                        selectedDrawerItem.getWareHouseTable(),
                        "DataWareHouseAID",
                        "ProductID",
                        productId);

                if (aid == null || aid.isEmpty()) {
                    // errorMsg.append("Mã ").append(productId).append(" chưa có trong kho");
                    // hasError = true;
                    // break;

                    String tableWh = selectedDrawerItem.getWareHouseDataBase();
                    List<String> columnsWarehouse = new ArrayList<>(arrayCRUD.warehouseColumns);
                    columnsWarehouse.remove("DataWareHouseAID");
                    String proaid = dbCRUDHelper.returnAID(
                            "Product",
                            "ProductAID",
                            "ProductID",
                            productId);
                    if (proaid == null || proaid.isEmpty()) {
                        errorMsg.append("Mã ").append(productId).append(" chưa có trong trong danh mục sản phẩm");
                        hasError = true;
                        break;
                    }
                    List<Object> values = Arrays.asList(
                            proaid, 0, 0,
                            "", "",
                            timestamp, user.getUserName(), remark);
                    dbCRUDHelper.insert(tableWh, columnsWarehouse, values);

                    aid = dbCRUDHelper.returnAID(
                            selectedDrawerItem.getWareHouseTable(),
                            "DataWareHouseAID",
                            "ProductID",
                            productId);
                } else {
                    if (selectedDrawerItem.getWareHouseSupplierID() == 41) {
                        String qtyCheck = dbCRUDHelper.returnAID(
                                selectedDrawerItem.getWareHouseTable(),
                                "Qty",
                                "DataWareHouseAID",
                                aid);
                        System.out.println("qtyCheck: " + qtyCheck);
                        if (qtyCheck == null || qtyCheck.isEmpty() || Double.parseDouble(qtyCheck) <= 0) {
                            // result = "Số lượng trong kho đang không còn, ";
                            errorMsg.append("Mã ").append(productId).append(" Số lượng trong kho đang không còn");
                            hasError = true;
                            break;
                        }
                    }
                }

                double qtyExport = qty * -1;

                // ---------- INSERT HISTORY ----------
                int i = 1;
                psInsert.setString(i++, aid);
                psInsert.setDouble(i++, qtyExport);
                psInsert.setInt(i++, idEmployee);
                psInsert.setInt(i++, partner);
                psInsert.setString(i++, remark);
                psInsert.setTimestamp(i++, time);
                psInsert.setString(i++, user != null ? user.getUserName() : "");
                psInsert.addBatch();

                // ---------- UPDATE QTY (CACHE) ----------
                double currentQty;

                if (qtyCache.containsKey(aid)) {
                    currentQty = qtyCache.get(aid);
                } else {
                    currentQty = dbCRUDHelper.sumQtyHistory(
                            selectedDrawerItem.getWareHouseDataBaseHistory(),
                            Integer.parseInt(aid));
                }

                double newQty = currentQty + qtyExport;
                qtyCache.put(aid, newQty);

                psUpdate.setDouble(1, newQty);
                psUpdate.setString(2, location);
                psUpdate.setString(3, user != null ? user.getUserName() : "");
                psUpdate.setString(4, aid);
                psUpdate.addBatch();

                currentRow++;
                progress.accept(currentRow, totalRows);

                // 🔥 batch mỗi 500 dòng
                if (currentRow % 500 == 0) {
                    psInsert.executeBatch();
                    psUpdate.executeBatch();
                    psInsert.clearBatch();
                    psUpdate.clearBatch();
                }
            }

            // ---------- COMMIT / ROLLBACK ----------
            if (!hasError) {
                psInsert.executeBatch();
                psUpdate.executeBatch();
                conn.commit();

                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thành công",
                        "Nhập excel thành công",
                        Alert.AlertType.INFORMATION));
            } else {
                conn.rollback();
                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thất bại",
                        errorMsg.toString(),
                        Alert.AlertType.ERROR));
            }

        } catch (Exception e) {
            if (conn != null)
                conn.rollback();
            Platform.runLater(() -> customDialogNotification.showDialog(
                    "Lỗi",
                    "Xử lý Excel thất bại: " + e.getMessage(),
                    Alert.AlertType.ERROR));
            throw e;
        } finally {
            if (psInsert != null)
                psInsert.close();
            if (psUpdate != null)
                psUpdate.close();
            if (conn != null)
                conn.close();
        }
    }

    public void importExcelHistoryImport(
            String excelPath,
            BiConsumer<Integer, Integer> progress) throws Exception {

        Connection conn = null;
        PreparedStatement psInsert = null;
        PreparedStatement psUpdate = null;

        // 🔥 Cache qty để xử lý mã trùng
        Map<String, Double> qtyCache = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(excelPath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(4);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;
            boolean hasError = false;

            DrawerItem selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            Account user = AppState.getInstance().get("Account", Account.class);

            StringBuilder errorMsg = new StringBuilder();

            conn = DbHelper.getConnection();
            conn.setAutoCommit(false); // 🔴 TRANSACTION

            // ---------- INSERT HISTORY ----------
            String insertHistorySQL = """
                    INSERT INTO %s (
                        DataWareHouseAID,
                        Qty,
                        ID_Employee,
                        Partner,
                        Remark,
                        Time,
                        LastUser,
                        LastTime
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())
                    """.formatted(selectedDrawerItem.getWareHouseDataBaseHistory());

            // ---------- UPDATE WAREHOUSE ----------
            String updateWarehouseSQL = """
                    UPDATE %s
                    SET Qty = ?,
                        LocationID = COALESCE(?, LocationID),
                        LastUser = ?,
                        LastTime = GETDATE()
                    WHERE DataWareHouseAID = ?
                    """.formatted(selectedDrawerItem.getWareHouseDataBase());

            psInsert = conn.prepareStatement(insertHistorySQL);
            psUpdate = conn.prepareStatement(updateWarehouseSQL);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                // ---------- READ EXCEL ----------
                String productId = toString(row.getCell(0));
                if (productId == null || productId.isBlank() || productId.isBlank()) {
                    continue;
                }
                if (productId != null) {
                    productId = productId
                            // .replace("\u00A0", "")
                            // .replaceAll("\\s+", "")
                            .trim();
                }

                // if (!isValidCode(productId)) {
                // errorMsg.append("Mã ").append(productId).append(" không đúng định dạng");
                // hasError = true;
                // break;
                // }

                double qty = toDouble(row.getCell(1));
                if (qty <= 0) {
                    errorMsg.append("Mã ").append(productId)
                            .append(" số lượng nhập phải lớn hơn 0");
                    hasError = true;
                    break;
                }
                LocalDateTime now = LocalDateTime.now();
                Timestamp timestamp = Timestamp.valueOf(now);

                int idEmployee = toInt(row.getCell(2));
                int partner = toInt(row.getCell(3));
                String remark = toString(row.getCell(4));
                Timestamp time = toTimestampOrNow(row.getCell(5));
                String location = toString(row.getCell(6));

                String aid = dbCRUDHelper.returnAID(
                        selectedDrawerItem.getWareHouseTable(),
                        "DataWareHouseAID",
                        "ProductID",
                        productId);

                if (aid == null || aid.isEmpty()) {
                    // errorMsg.append("Mã ").append(productId).append(" chưa có trong kho");
                    // hasError = true;
                    // break;

                    String tableWh = selectedDrawerItem.getWareHouseDataBase();
                    List<String> columnsWarehouse = new ArrayList<>(arrayCRUD.warehouseColumns);
                    columnsWarehouse.remove("DataWareHouseAID");
                    String proaid = dbCRUDHelper.returnAID(
                            "Product",
                            "ProductAID",
                            "ProductID",
                            productId);
                    if (proaid == null || proaid.isEmpty()) {
                        errorMsg.append("Mã ").append(productId).append(" chưa có trong trong danh mục sản phẩm");
                        hasError = true;
                        break;
                    }
                    // System.out.println(proaid + "của mã ProductID" + productId);
                    List<Object> values = Arrays.asList(
                            proaid, 0, 0,
                            "", "",
                            timestamp, user.getUserName(), remark);
                    dbCRUDHelper.insert(tableWh, columnsWarehouse, values);

                    aid = dbCRUDHelper.returnAID(
                            selectedDrawerItem.getWareHouseTable(),
                            "DataWareHouseAID",
                            "ProductID",
                            productId);
                }

                // ---------- INSERT HISTORY ----------
                int i = 1;
                psInsert.setString(i++, aid);
                psInsert.setDouble(i++, qty);
                psInsert.setInt(i++, idEmployee);
                psInsert.setInt(i++, partner);
                psInsert.setString(i++, remark);
                psInsert.setTimestamp(i++, time);
                psInsert.setString(i++, user != null ? user.getUserName() : "");
                psInsert.addBatch();

                // ---------- UPDATE QTY (CACHE) ----------
                double currentQty;

                if (qtyCache.containsKey(aid)) {
                    currentQty = qtyCache.get(aid);
                } else {
                    currentQty = dbCRUDHelper.sumQtyHistory(
                            selectedDrawerItem.getWareHouseDataBaseHistory(),
                            Integer.parseInt(aid));
                }
                // System.out.println(aid + "aid của mã WH AID" + productId);

                double newQty = currentQty + qty;
                qtyCache.put(aid, newQty);

                psUpdate.setDouble(1, newQty);
                psUpdate.setString(2, location);
                psUpdate.setString(3, user != null ? user.getUserName() : "");
                psUpdate.setString(4, aid);
                psUpdate.addBatch();

                currentRow++;
                progress.accept(currentRow, totalRows);

                // 🔥 batch mỗi 500 dòng
                if (currentRow % 500 == 0) {
                    psInsert.executeBatch();
                    psUpdate.executeBatch();
                    psInsert.clearBatch();
                    psUpdate.clearBatch();
                }
            }

            // ---------- COMMIT / ROLLBACK ----------
            if (!hasError) {
                psInsert.executeBatch();
                psUpdate.executeBatch();
                conn.commit();

                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thành công",
                        "Nhập excel thành công",
                        Alert.AlertType.INFORMATION));
            } else {
                conn.rollback();
                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thất bại",
                        errorMsg.toString(),
                        Alert.AlertType.ERROR));
            }

        } catch (Exception e) {
            if (conn != null)
                conn.rollback();
            Platform.runLater(() -> customDialogNotification.showDialog(
                    "Lỗi",
                    "Xử lý Excel thất bại: " + e.getMessage(),
                    Alert.AlertType.ERROR));
            throw e;
        } finally {
            if (psInsert != null)
                psInsert.close();
            if (psUpdate != null)
                psUpdate.close();
            if (conn != null)
                conn.close();
        }
    }

    public void importExcelHistoryTransferImport(
            String excelPath,
            BiConsumer<Integer, Integer> progress) throws Exception {

        Connection conn = null;
        PreparedStatement psInsertFrom = null;
        PreparedStatement psUpdateFrom = null;
        PreparedStatement psInsertTo = null;
        PreparedStatement psUpdateTo = null;

        Map<String, Double> qtyCache = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(excelPath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(5);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;
            boolean hasError = false;
            StringBuilder errorMsg = new StringBuilder();

            DrawerItem drawerFrom = AppState.getInstance()
                    .get("selectedDrawerItem", DrawerItem.class);

            Account user = AppState.getInstance()
                    .get("Account", Account.class);

            Map<Integer, DrawerItem> drawerBySupplier = dbInfoHelper.getWareHouseDataBase().stream()
                    .collect(Collectors.toMap(
                            DrawerItem::getWareHouseSupplierID,
                            d -> d,
                            (a, b) -> a));

            conn = DbHelper.getConnection();
            conn.setAutoCommit(false);

            psInsertFrom = conn.prepareStatement("""
                    INSERT INTO %s (
                        DataWareHouseAID, Qty, ID_Employee, Partner,
                        Remark, Time, LastUser, LastTime, TransferGroupID
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), ?)
                    """.formatted(drawerFrom.getWareHouseDataBaseHistory()));

            psUpdateFrom = conn.prepareStatement("""
                    UPDATE %s
                    SET Qty = ?, LastUser = ?, LastTime = GETDATE(), LocationID = COALESCE(?, LocationID)
                    WHERE DataWareHouseAID = ?
                    """.formatted(drawerFrom.getWareHouseDataBase()));

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;
                LocalDateTime now = LocalDateTime.now();
                Timestamp timestamp = Timestamp.valueOf(now);
                String productId = toString(row.getCell(0));
                double qty = toDouble(row.getCell(1));
                int idEmployee = toInt(row.getCell(2));
                int partner = toInt(row.getCell(3));
                String remark = toString(row.getCell(4));
                Timestamp time = toTimestampOrNow(row.getCell(5));
                String locationID = toString(row.getCell(6));

                if (productId == null || productId.isBlank() || productId.isBlank()) {
                    continue;
                }

                // if (!isValidCode(productId)) {
                // hasError = true;
                // errorMsg.append("Mã ").append(productId).append("Dữ liệu không hợp lệ tại
                // dòng ")
                // .append(row.getRowNum() + 1);
                // break;
                // }

                DrawerItem drawerTo = drawerBySupplier.get(partner);
                if (drawerTo == null) {
                    hasError = true;
                    errorMsg.append("Không tìm thấy kho đến cho mã: ")
                            .append(productId);
                    break;
                }

                if (drawerFrom.getWareHouseID()
                        .equals(drawerTo.getWareHouseID())) {
                    hasError = true;
                    errorMsg.append("Kho nguồn và kho đến trùng nhau");
                    break;
                }

                // ---------- AID FROM ----------
                String aid = dbCRUDHelper.returnAID(
                        drawerFrom.getWareHouseTable(),
                        "DataWareHouseAID",
                        "ProductID",
                        productId);

                if (aid == null || aid.trim().isEmpty()) {
                    // hasError = true;
                    // errorMsg.append("Không lấy được AID kho nguồn: ")
                    // .append(productId);
                    // break;
                    String tableWh = drawerFrom.getWareHouseDataBase();
                    List<String> columnsWarehouse = new ArrayList<>(arrayCRUD.warehouseColumns);
                    columnsWarehouse.remove("DataWareHouseAID");
                    String proaid = dbCRUDHelper.returnAID(
                            "Product",
                            "ProductAID",
                            "ProductID",
                            productId);
                    List<Object> values = Arrays.asList(
                            proaid, 0, 0,
                            "", "",
                            timestamp, user.getUserName(), remark);
                    dbCRUDHelper.insert(tableWh, columnsWarehouse, values);

                    aid = dbCRUDHelper.returnAID(
                            drawerFrom.getWareHouseTable(),
                            "DataWareHouseAID",
                            "ProductID",
                            productId);
                }

                int aidFromInt = Integer.parseInt(aid);

                String keyFrom = "FROM_" + aid;
                double currentQtyFrom = qtyCache.containsKey(keyFrom)
                        ? qtyCache.get(keyFrom)
                        : dbCRUDHelper.sumQtyHistory(
                                drawerFrom.getWareHouseDataBaseHistory(),
                                aidFromInt);

                double newQtyFrom = currentQtyFrom + qty;
                String transferGroupID = generateTransferGroupID(drawerFrom.getWareHouseID(),
                        drawerTo.getWareHouseID());
                qtyCache.put(keyFrom, newQtyFrom);
                double qtyFrom = qty * -1;

                psInsertFrom.setString(1, aid);
                psInsertFrom.setDouble(2, qtyFrom);
                psInsertFrom.setInt(3, idEmployee);
                psInsertFrom.setInt(4, partner);
                psInsertFrom.setString(5, remark);
                psInsertFrom.setTimestamp(6, time);
                psInsertFrom.setString(7, user.getUserName());
                psInsertFrom.setString(8, transferGroupID);
                psInsertFrom.addBatch();

                psUpdateFrom.setDouble(1, newQtyFrom);
                psUpdateFrom.setString(2, user.getUserName());
                psUpdateFrom.setString(3, locationID);
                psUpdateFrom.setString(4, aid);
                psUpdateFrom.addBatch();

                // ---------- PREPARE TO ----------
                if (psInsertTo == null) {
                    psInsertTo = conn.prepareStatement("""
                            INSERT INTO %s (
                                DataWareHouseAID, Qty, ID_Employee, Partner,
                                Remark, Time, LastUser, LastTime, TransferGroupID
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), ?)
                            """.formatted(drawerTo.getWareHouseDataBaseHistory()));

                    psUpdateTo = conn.prepareStatement("""
                            UPDATE %s
                            SET Qty = ?, LastUser = ?, LastTime = GETDATE(), locationID = COALESCE(?, locationID)
                            WHERE DataWareHouseAID = ?
                            """.formatted(drawerTo.getWareHouseDataBase()));
                }

                // ---------- AID TO ----------
                String aidTO = dbCRUDHelper.returnAID(
                        drawerTo.getWareHouseTable(),
                        "DataWareHouseAID",
                        "ProductID",
                        productId);

                if (aidTO == null || aidTO.trim().isEmpty()) {
                    // hasError = true;
                    // errorMsg.append("Không lấy được AID kho đến: ")
                    // .append(productId);
                    // break;
                    String tableWh = drawerTo.getWareHouseDataBase();
                    List<String> columnsWarehouse = new ArrayList<>(arrayCRUD.warehouseColumns);
                    columnsWarehouse.remove("DataWareHouseAID");
                    String proaid = dbCRUDHelper.returnAID(
                            "Product",
                            "ProductAID",
                            "ProductID",
                            productId);
                    List<Object> values = Arrays.asList(
                            proaid, 0, 0,
                            "", "",
                            timestamp, user.getUserName(), remark);
                    dbCRUDHelper.insert(tableWh, columnsWarehouse, values);

                    aidTO = dbCRUDHelper.returnAID(
                            drawerTo.getWareHouseTable(),
                            "DataWareHouseAID",
                            "ProductID",
                            productId);
                }

                int aidToInt = Integer.parseInt(aidTO);

                double qtyTo = qty;
                String keyTo = "TO_" + aidTO;

                double currentQtyTo = qtyCache.containsKey(keyTo)
                        ? qtyCache.get(keyTo)
                        : dbCRUDHelper.sumQtyHistory(
                                drawerTo.getWareHouseDataBaseHistory(),
                                aidToInt);

                double newQtyTo = currentQtyTo + qtyTo;
                qtyCache.put(keyTo, newQtyTo);

                psInsertTo.setString(1, aidTO);
                psInsertTo.setDouble(2, qtyTo);
                psInsertTo.setInt(3, idEmployee);
                psInsertTo.setInt(4, drawerFrom.getWareHouseSupplierID());
                psInsertTo.setString(5, remark);
                psInsertTo.setTimestamp(6, time);
                psInsertTo.setString(7, user.getUserName());
                psInsertTo.setString(8, transferGroupID);
                psInsertTo.addBatch();

                // 🔥 FIX BUG: update đúng aidTO
                psUpdateTo.setDouble(1, newQtyTo);
                psUpdateTo.setString(2, user.getUserName());
                psUpdateTo.setString(3, locationID);
                psUpdateTo.setString(4, aidTO);

                psUpdateTo.addBatch();

                currentRow++;
                progress.accept(currentRow, totalRows);

                if (currentRow % 500 == 0) {
                    psInsertFrom.executeBatch();
                    psUpdateFrom.executeBatch();
                    psInsertTo.executeBatch();
                    psUpdateTo.executeBatch();
                }
            }

            if (!hasError) {
                psInsertFrom.executeBatch();
                psUpdateFrom.executeBatch();
                psInsertTo.executeBatch();
                psUpdateTo.executeBatch();
                conn.commit();

                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thành công",
                        "Điều chuyển kho thành công",
                        Alert.AlertType.INFORMATION));
            } else {
                conn.rollback();
                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thất bại",
                        errorMsg.toString(),
                        Alert.AlertType.ERROR));
            }

        } catch (Exception e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            if (psInsertFrom != null)
                psInsertFrom.close();
            if (psUpdateFrom != null)
                psUpdateFrom.close();
            if (psInsertTo != null)
                psInsertTo.close();
            if (psUpdateTo != null)
                psUpdateTo.close();
            if (conn != null)
                conn.close();
        }
    }

    public void importExcelHistoryExportDynamic(
            String excelPath,
            BiConsumer<Integer, Integer> progress) throws Exception {

        Connection conn = null;
        PreparedStatement psInsert = null;
        PreparedStatement psUpdate = null;

        // 🔥 Cache qty theo AID để xử lý mã trùng
        Map<String, Double> qtyCache = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(excelPath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(5);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;
            boolean hasError = false;

            DrawerItem selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            Account user = AppState.getInstance().get("Account", Account.class);

            StringBuilder errorMsg = new StringBuilder();

            conn = DbHelper.getConnection();
            conn.setAutoCommit(false); // 🔴 TRANSACTION

            // ---------- INSERT HISTORY ----------
            String insertHistorySQL = """
                    INSERT INTO %s (
                        DataWareHouseAID,
                        Qty,
                        ID_Employee,
                        Partner,
                        Remark,
                        Time,
                        LastUser,
                        LastTime
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())
                    """.formatted(selectedDrawerItem.getWareHouseDataBaseHistory());

            // ---------- UPDATE WAREHOUSE ----------
            String updateWarehouseSQL = """
                    UPDATE %s
                    SET Qty = ?,
                        LastUser = ?,
                        LastTime = GETDATE()
                    WHERE DataWareHouseAID = ?
                    """.formatted(selectedDrawerItem.getWareHouseDataBase());

            psInsert = conn.prepareStatement(insertHistorySQL);
            psUpdate = conn.prepareStatement(updateWarehouseSQL);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                // ---------- READ EXCEL ----------
                String productId = toString(row.getCell(0));
                if (productId != null) {
                    productId = productId.replace("\u00A0", "")
                            .replaceAll("\\s+", "")
                            .trim();
                }

                // if (!isValidCode(productId)) {
                // errorMsg.append("Mã ").append(productId).append(" không đúng định dạng");
                // hasError = true;
                // break;
                // }

                double qty = toDouble(row.getCell(5));
                if (qty <= 0) {
                    errorMsg.append("Mã ").append(productId)
                            .append(" số lượng xuất phải lớn hơn 0");
                    hasError = true;
                    break;
                }

                LocalDateTime now = LocalDateTime.now();
                Timestamp timestamp = Timestamp.valueOf(now);
                double qtyExpected = toDouble(row.getCell(1));
                String idBill = toString(row.getCell(2));
                String excelLocation = toString(row.getCell(3));
                String remark = toString(row.getCell(4));
                // double qty = toDouble(row.getCell(5));
                int idEmployee = toInt(row.getCell(6));
                int partner = toInt(row.getCell(7));
                String remarkOfHistory = toString(row.getCell(8));
                Timestamp time = toTimestampOrNow(row.getCell(9));

                String aid = dbCRUDHelper.returnAID(
                        selectedDrawerItem.getWareHouseTable(),
                        "DataWareHouseAID",
                        "ProductID",
                        productId);

                if (aid == null || aid.isEmpty()) {
                    // errorMsg.append("Mã ").append(productId).append(" chưa có trong kho");
                    // hasError = true;
                    // break;
                    String tableWh = selectedDrawerItem.getWareHouseDataBase();
                    List<String> columnsWarehouse = new ArrayList<>(arrayCRUD.warehouseColumns);
                    columnsWarehouse.remove("DataWareHouseAID");
                    String proaid = dbCRUDHelper.returnAID(
                            "Product",
                            "ProductAID",
                            "ProductID",
                            productId);
                    List<Object> values = Arrays.asList(
                            proaid, 0, qtyExpected,
                            idBill, excelLocation,
                            timestamp, user.getUserName(), remark);
                    dbCRUDHelper.insert(tableWh, columnsWarehouse, values);

                    aid = dbCRUDHelper.returnAID(
                            selectedDrawerItem.getWareHouseTable(),
                            "DataWareHouseAID",
                            "ProductID",
                            productId);

                }

                double qtyExport = qty * -1;

                // ---------- INSERT HISTORY ----------
                int i = 1;
                psInsert.setString(i++, aid);
                psInsert.setDouble(i++, qtyExport);
                psInsert.setInt(i++, idEmployee);
                psInsert.setInt(i++, partner);
                psInsert.setString(i++, remarkOfHistory);
                psInsert.setTimestamp(i++, time);
                psInsert.setString(i++, user != null ? user.getUserName() : "");
                psInsert.addBatch();

                // ---------- UPDATE QTY (CACHE) ----------
                double currentQty;

                if (qtyCache.containsKey(aid)) {
                    currentQty = qtyCache.get(aid);
                } else {
                    currentQty = dbCRUDHelper.sumQtyHistory(
                            selectedDrawerItem.getWareHouseDataBaseHistory(),
                            Integer.parseInt(aid));
                }

                double newQty = currentQty + qtyExport;
                qtyCache.put(aid, newQty);

                psUpdate.setDouble(1, newQty);
                psUpdate.setString(2, user != null ? user.getUserName() : "");
                psUpdate.setString(3, aid);
                psUpdate.addBatch();

                currentRow++;
                progress.accept(currentRow, totalRows);

                // 🔥 batch mỗi 500 dòng
                if (currentRow % 500 == 0) {
                    psInsert.executeBatch();
                    psUpdate.executeBatch();
                    psInsert.clearBatch();
                    psUpdate.clearBatch();
                }
            }

            // ---------- COMMIT / ROLLBACK ----------
            if (!hasError) {
                psInsert.executeBatch();
                psUpdate.executeBatch();
                conn.commit();

                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thành công",
                        "Nhập excel thành công",
                        Alert.AlertType.INFORMATION));
            } else {
                conn.rollback();
                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thất bại",
                        errorMsg.toString(),
                        Alert.AlertType.ERROR));
            }

        } catch (Exception e) {
            if (conn != null)
                conn.rollback();
            Platform.runLater(() -> customDialogNotification.showDialog(
                    "Lỗi",
                    "Xử lý Excel thất bại: " + e.getMessage(),
                    Alert.AlertType.ERROR));
            throw e;
        } finally {
            if (psInsert != null)
                psInsert.close();
            if (psUpdate != null)
                psUpdate.close();
            if (conn != null)
                conn.close();
        }
    }

    public void importExcelHistoryImportDynamic(
            String excelPath,
            BiConsumer<Integer, Integer> progress) throws Exception {

        Connection conn = null;
        PreparedStatement psInsert = null;
        PreparedStatement psUpdate = null;

        // 🔥 Cache qty theo AID để xử lý mã trùng
        Map<String, Double> qtyCache = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(excelPath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(6);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;
            boolean hasError = false;

            DrawerItem selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);
            Account user = AppState.getInstance().get("Account", Account.class);

            StringBuilder errorMsg = new StringBuilder();

            conn = DbHelper.getConnection();
            conn.setAutoCommit(false); // 🔴 TRANSACTION

            // ---------- INSERT HISTORY ----------
            String insertHistorySQL = """
                    INSERT INTO %s (
                        DataWareHouseAID,
                        Qty,
                        ID_Employee,
                        Partner,
                        Remark,
                        Time,
                        LastUser,
                        LastTime
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())
                    """.formatted(selectedDrawerItem.getWareHouseDataBaseHistory());

            // ---------- UPDATE WAREHOUSE ----------
            String updateWarehouseSQL = """
                    UPDATE %s
                    SET Qty = ?,
                        LastUser = ?,
                        LastTime = GETDATE()
                    WHERE DataWareHouseAID = ?
                    """.formatted(selectedDrawerItem.getWareHouseDataBase());

            psInsert = conn.prepareStatement(insertHistorySQL);
            psUpdate = conn.prepareStatement(updateWarehouseSQL);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                // ---------- READ EXCEL ----------
                String productId = toString(row.getCell(0));
                if (productId != null) {
                    productId = productId.replace("\u00A0", "")
                            .replaceAll("\\s+", "")
                            .trim();
                }

                // if (!isValidCode(productId)) {
                // errorMsg.append("Mã ").append(productId).append(" không đúng định dạng");
                // hasError = true;
                // break;
                // }

                double qty = toDouble(row.getCell(5));
                if (qty <= 0) {
                    errorMsg.append("Mã ").append(productId)
                            .append(" số lượng xuất phải lớn hơn 0");
                    hasError = true;
                    break;
                }

                LocalDateTime now = LocalDateTime.now();
                Timestamp timestamp = Timestamp.valueOf(now);
                double qtyExpected = toDouble(row.getCell(1));
                String idBill = toString(row.getCell(2));
                String excelLocation = toString(row.getCell(3));
                String remark = toString(row.getCell(4));
                // double qty = toDouble(row.getCell(5));
                int idEmployee = toInt(row.getCell(6));
                int partner = toInt(row.getCell(7));
                String remarkOfHistory = toString(row.getCell(8));
                Timestamp time = toTimestampOrNow(row.getCell(9));

                String aid = dbCRUDHelper.returnAID(
                        selectedDrawerItem.getWareHouseTable(),
                        "DataWareHouseAID",
                        "ProductID",
                        productId);

                if (aid == null || aid.isEmpty()) {
                    // errorMsg.append("Mã ").append(productId).append(" chưa có trong kho");
                    // hasError = true;
                    // break;
                    String tableWh = selectedDrawerItem.getWareHouseDataBase();
                    List<String> columnsWarehouse = new ArrayList<>(arrayCRUD.warehouseColumns);
                    columnsWarehouse.remove("DataWareHouseAID");
                    String proaid = dbCRUDHelper.returnAID(
                            "Product",
                            "ProductAID",
                            "ProductID",
                            productId);
                    List<Object> values = Arrays.asList(
                            proaid, 0, qtyExpected,
                            idBill, excelLocation,
                            timestamp, user.getUserName(), remark);
                    dbCRUDHelper.insert(tableWh, columnsWarehouse, values);

                    aid = dbCRUDHelper.returnAID(
                            selectedDrawerItem.getWareHouseTable(),
                            "DataWareHouseAID",
                            "ProductID",
                            productId);

                }

                double qtyExport = qty * 1;

                // ---------- INSERT HISTORY ----------
                int i = 1;
                psInsert.setString(i++, aid);
                psInsert.setDouble(i++, qtyExport);
                psInsert.setInt(i++, idEmployee);
                psInsert.setInt(i++, partner);
                psInsert.setString(i++, remarkOfHistory);
                psInsert.setTimestamp(i++, time);
                psInsert.setString(i++, user != null ? user.getUserName() : "");
                psInsert.addBatch();

                // ---------- UPDATE QTY (CACHE) ----------
                double currentQty;

                if (qtyCache.containsKey(aid)) {
                    currentQty = qtyCache.get(aid);
                } else {
                    currentQty = dbCRUDHelper.sumQtyHistory(
                            selectedDrawerItem.getWareHouseDataBaseHistory(),
                            Integer.parseInt(aid));
                }

                double newQty = currentQty + qtyExport;
                qtyCache.put(aid, newQty);

                psUpdate.setDouble(1, newQty);
                psUpdate.setString(2, user != null ? user.getUserName() : "");
                psUpdate.setString(3, aid);
                psUpdate.addBatch();

                currentRow++;
                progress.accept(currentRow, totalRows);

                // 🔥 batch mỗi 500 dòng
                if (currentRow % 500 == 0) {
                    psInsert.executeBatch();
                    psUpdate.executeBatch();
                    psInsert.clearBatch();
                    psUpdate.clearBatch();
                }
            }

            // ---------- COMMIT / ROLLBACK ----------
            if (!hasError) {
                psInsert.executeBatch();
                psUpdate.executeBatch();
                conn.commit();

                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thành công",
                        "Nhập excel thành công",
                        Alert.AlertType.INFORMATION));
            } else {
                conn.rollback();
                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thất bại",
                        errorMsg.toString(),
                        Alert.AlertType.ERROR));
            }

        } catch (Exception e) {
            if (conn != null)
                conn.rollback();
            Platform.runLater(() -> customDialogNotification.showDialog(
                    "Lỗi",
                    "Xử lý Excel thất bại: " + e.getMessage(),
                    Alert.AlertType.ERROR));
            throw e;
        } finally {
            if (psInsert != null)
                psInsert.close();
            if (psUpdate != null)
                psUpdate.close();
            if (conn != null)
                conn.close();
        }
    }

    public void importExcelCheckData(
            String excelPath,
            int sheetIndex,
            BiConsumer<Integer, Integer> progress) {

        final int ERROR_COL_INDEX = 7;
        final int LAST_DATA_COL = 5; // A → G

        File file = new File(excelPath);
        if (!file.exists())
            return;

        try (FileInputStream fis = new FileInputStream(file);
                XSSFWorkbook wb = new XSSFWorkbook(fis)) {

            if (sheetIndex < 0 || sheetIndex >= wb.getNumberOfSheets()) {
                throw new IllegalArgumentException("Sheet index không hợp lệ");
            }

            XSSFSheet sheet = wb.getSheetAt(sheetIndex);

            /* ================= HEADER ================= */

            Row header = sheet.getRow(0);
            if (header == null)
                header = sheet.createRow(0);

            Cell errorHeader = header.getCell(ERROR_COL_INDEX);
            if (errorHeader == null)
                errorHeader = header.createCell(ERROR_COL_INDEX);
            errorHeader.setCellValue("Lý do lỗi");

            int totalRows = sheet.getLastRowNum();

            /* ================= STYLE CACHE ================= */

            // cache style lỗi theo dataFormat
            Map<Short, CellStyle> errorStyleCache = new HashMap<>();

            /* ================= PROCESS ================= */

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                // clear cột lỗi
                Cell errorCell = row.getCell(ERROR_COL_INDEX);
                if (errorCell == null)
                    errorCell = row.createCell(ERROR_COL_INDEX);
                errorCell.setCellValue("");

                /* ===== VALIDATE ===== */
                String error = validateRow(row);

                /* ===== TÔ LỖI ===== */
                if (error != null && !error.isEmpty()) {

                    for (int c = 0; c <= LAST_DATA_COL; c++) {
                        Cell cell = row.getCell(c);
                        if (cell == null)
                            continue;

                        CellStyle origin = cell.getCellStyle();
                        short fmt = origin.getDataFormat();

                        CellStyle errorStyle = errorStyleCache.computeIfAbsent(fmt, f -> {
                            CellStyle cs = wb.createCellStyle();
                            cs.cloneStyleFrom(origin);
                            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            cs.setFillForegroundColor(IndexedColors.RED.getIndex());
                            return cs;
                        });

                        cell.setCellStyle(errorStyle);
                    }

                    errorCell.setCellValue(error);
                }

                /* ===== PROGRESS ===== */
                if (progress != null) {
                    progress.accept(i, totalRows);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateExcelDetailProduct(
            String excelPath,
            BiConsumer<Integer, Integer> progressCallback) {

        try (
                FileInputStream fis = new FileInputStream(excelPath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(1);

            Connection conn = DbHelper.getConnection();
            conn.setAutoCommit(false);

            String updateSQL = """
                        UPDATE DetailsProduct
                        SET
                            ProductID = ?,
                            ID_PartNo = ?,
                            PartNoID = ?,
                            NameEnglish = ?,
                            NameVietNamese = ?,
                            PartNoQty = ?,
                            Parameter = ?,
                            Remark = ?,
                            LastTime = GETDATE()
                        WHERE PartNoAID = ?
                    """;

            PreparedStatement ps = conn.prepareStatement(updateSQL);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;

            // StringBuilder notify = new StringBuilder();

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                currentRow++;
                String partNoAID = toString(row.getCell(0));
                String excelProductID = toString(row.getCell(1));
                String idpartNo = toString(row.getCell(2));
                String partNoID = toString(row.getCell(3));
                String nameEN = toString(row.getCell(4));
                String nameVN = toString(row.getCell(5));
                int partNoQty = toInt(row.getCell(6));
                String parameter = toString(row.getCell(7));
                String remark = toString(row.getCell(8));

                int i = 1;
                ps.setString(i++, excelProductID);
                ps.setString(i++, idpartNo);
                ps.setString(i++, partNoID);
                ps.setString(i++, nameEN);
                ps.setString(i++, nameVN);
                ps.setInt(i++, partNoQty);
                ps.setString(i++, parameter);
                ps.setString(i++, remark);
                ps.setString(i++, partNoAID);

                ps.addBatch();

                // ✅ UPDATE PROGRESS
                if (progressCallback != null) {
                    progressCallback.accept(currentRow, totalRows);
                }
            }

            ps.executeBatch();
            conn.commit();

            ps.close();
            conn.close();

            Platform.runLater(() -> {
                customDialogNotification.showDialog(
                        "Thành công",
                        "Đã cập nhật thành công",
                        Alert.AlertType.INFORMATION);
            });

        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                customDialogNotification.showDialog(
                        "Lỗi",
                        "Cập nhật Excel thất bại",
                        Alert.AlertType.ERROR);
            });
        }
    }

    public void deleteExcelDetailProduct(
            String excelPath,
            BiConsumer<Integer, Integer> progressCallback) {

        try (
                FileInputStream fis = new FileInputStream(excelPath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(2);

            Connection conn = DbHelper.getConnection();
            conn.setAutoCommit(false);

            String deleteSQL = """
                        DELETE FROM DetailsProduct
                        WHERE PartNoAID = ?
                    """;

            PreparedStatement ps = conn.prepareStatement(deleteSQL);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;

            // StringBuilder notify = new StringBuilder();

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                currentRow++;

                String partNoAID = toString(row.getCell(0));

                ps.setString(1, partNoAID);

                ps.addBatch();

                // ✅ UPDATE PROGRESS
                if (progressCallback != null) {
                    progressCallback.accept(currentRow, totalRows);
                }
            }

            ps.executeBatch();
            conn.commit();

            ps.close();
            conn.close();

            Platform.runLater(() -> {
                customDialogNotification.showDialog(
                        "Thành công",
                        "Đã xóa thành công",
                        Alert.AlertType.INFORMATION);
            });

        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                customDialogNotification.showDialog(
                        "Lỗi",
                        "Xóa Excel thất bại",
                        Alert.AlertType.ERROR);
            });
        }
    }

    public void importExcelCart(
            String excelPath,
            BiConsumer<Integer, Integer> progress,
            String typeCart,
            int locationSheet) throws Exception {

        Connection conn = null;
        PreparedStatement psInsert = null;

        try (FileInputStream fis = new FileInputStream(excelPath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(locationSheet);
            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;
            boolean hasError = false;

            Account user = AppState.getInstance().get("Account", Account.class);
            long accountId = user.getAccountID();

            StringBuilder errorMsg = new StringBuilder();

            conn = DbHelper.getConnection();
            conn.setAutoCommit(false);

            String insertSQL = "INSERT INTO Cart (" +
                    "AccountID, ProductAID, ProductAIDVAT, ID_PartNo, NameProduct," +
                    "ManufacturerID, CountryID, UnitID, VehicleTypeID, BusinessID, Qty, PriceNET," +
                    "Total, Cogs, PriceVAT, PaymentID, BillID," +
                    "SourceID, DeliveryID, EmployeeID, Status," +
                    "DeliveryTime, StatusVAT, ContractID, Remark, TypeCartID, LastTime" +
                    ") VALUES (" +
                    "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,GETDATE()" +
                    ")";

            psInsert = conn.prepareStatement(insertSQL);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Bỏ header

                // ===== READ PRODUCT =====
                String productId = toString(row.getCell(0));
                if (productId != null) {
                    productId = productId.replace("\u00A0", "").replaceAll("\\s+", "").trim();
                }

                // if (!isValidCode(productId)) {
                // errorMsg.append("Mã ").append(productId).append(" không đúng định dạng\n");
                // hasError = true;
                // break;
                // }

                Product product = dbInfoHelper.getProductByID(productId);
                if (product == null || product.getProductAID() == null) {
                    errorMsg.append("Mã ").append(productId).append(" chưa tồn tại\n");
                    hasError = true;
                    break;
                }

                long productAID = Long.parseLong(product.getProductAID());

                String productIdVAT = toString(row.getCell(1));
                String productAidVATStr = dbCRUDHelper.returnAID(
                        "Product", "ProductAID", "ProductID", productIdVAT);

                long productAidVAT = 0;
                if (productAidVATStr != null && !productAidVATStr.isBlank()) {
                    productAidVAT = Long.parseLong(productAidVATStr);
                }

                // ===== GIỮ NGUYÊN LOGIC FALLBACK =====
                String partNo = toString(row.getCell(2));
                partNo = (partNo != null ? partNo.trim() : product.getID_PartNo());

                String nameProduct = toString(row.getCell(3));
                nameProduct = (nameProduct != null ? nameProduct.trim() : product.getNameProduct());

                int manufacturerId = toInt(row.getCell(4));
                manufacturerId = (manufacturerId != 0 ? manufacturerId : product.getManufacturerID());

                int countryId = toInt(row.getCell(5));
                countryId = (countryId != 0 ? countryId : product.getCountryID());

                int unitId = toInt(row.getCell(6));
                unitId = (unitId != 0 ? unitId : product.getUnitID());
                String vehicleTypeID = toString(row.getCell(7));
                vehicleTypeID = (vehicleTypeID != null ? vehicleTypeID.trim() : product.getVehicleTypeID());
                int businessID = toInt(row.getCell(8));

                double qty = toDouble(row.getCell(9));
                double price = toDouble(row.getCell(10));
                double total = toDouble(row.getCell(11));
                double cogs = toDouble(row.getCell(12));
                double priceVAT = toDouble(row.getCell(13));
                int paymentId = toInt(row.getCell(14));
                int billId = toInt(row.getCell(15));
                int sourceId = toInt(row.getCell(16));
                int deliveryId = toInt(row.getCell(17));
                int employeeId = toInt(row.getCell(18));
                Timestamp time = toTimestampOrNow(row.getCell(19));
                int statusVAT = toInt(row.getCell(20));
                int contractId = toInt(row.getCell(21));
                String remark = toString(row.getCell(22));
                int typeCartID = 0;

                // ===== VALIDATE =====
                if (billId == 0 || sourceId == 0 || deliveryId == 0 || qty <= 0) {
                    errorMsg.append("Dữ liệu bắt buộc bị thiếu tại mã ").append(productId).append("\n");
                    hasError = true;
                    break;
                }

                if (typeCart.equals("EXPORT")) {
                    qty *= -1;
                    typeCartID = 2;
                }
                if (typeCart.equals("IMPORT")) {
                    typeCartID = 1;
                }
                if (typeCart.equals("TRANSFER")) {
                    typeCartID = 3;
                }

                // ===== INSERT =====
                int i = 1;
                psInsert.setLong(i++, accountId);
                psInsert.setLong(i++, productAID);
                psInsert.setLong(i++, productAidVAT);
                psInsert.setString(i++, partNo);
                psInsert.setString(i++, nameProduct);

                psInsert.setInt(i++, manufacturerId);
                psInsert.setInt(i++, countryId);
                psInsert.setInt(i++, unitId);
                psInsert.setString(i++, vehicleTypeID);

                psInsert.setInt(i++, businessID);
                psInsert.setDouble(i++, qty);
                psInsert.setDouble(i++, price);

                psInsert.setDouble(i++, total);
                psInsert.setDouble(i++, cogs);
                psInsert.setDouble(i++, priceVAT);
                psInsert.setInt(i++, paymentId);
                psInsert.setInt(i++, billId);

                psInsert.setInt(i++, sourceId);
                psInsert.setInt(i++, deliveryId);
                psInsert.setInt(i++, employeeId);
                psInsert.setInt(i++, 0); // Status
                psInsert.setTimestamp(i++, time);
                psInsert.setInt(i++, statusVAT);
                psInsert.setInt(i++, contractId);
                psInsert.setString(i++, remark);
                psInsert.setInt(i++, typeCartID);

                psInsert.addBatch();
                currentRow++;

                progress.accept(currentRow, totalRows);

                // Execute batch mỗi 500 dòng để tránh memory overload
                if (currentRow % 500 == 0) {
                    psInsert.executeBatch();
                    psInsert.clearBatch();
                }
            }

            // ==================== XỬ LÝ SAU VÒNG LẶP ====================
            if (hasError) {
                conn.rollback();
                throw new Exception(errorMsg.toString());
            }

            // Execute batch còn lại
            if (currentRow > 0) {
                int[] batchResults = psInsert.executeBatch();
                System.out.println("✅ Import hoàn tất. Số dòng insert: " + currentRow);
                System.out.println("Batch result: " + Arrays.toString(batchResults));
            } else {
                System.out.println("⚠️ Không có dữ liệu nào được import!");
            }

            conn.commit();
            System.out.println("🎉 IMPORT CART SUCCESS - Total rows: " + currentRow);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Đã rollback transaction do lỗi.");
                } catch (Exception rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            throw e;
        } finally {
            // Đóng tài nguyên an toàn
            try {
                if (psInsert != null)
                    psInsert.close();
                if (conn != null)
                    conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void deleteExcelCart(
            String excelPath,
            BiConsumer<Integer, Integer> progress,
            int locationSheet) throws Exception {

        Connection conn = null;
        PreparedStatement psDelete = null;

        try (FileInputStream fis = new FileInputStream(excelPath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(locationSheet);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;
            boolean hasError = false;

            StringBuilder errorMsg = new StringBuilder();

            conn = DbHelper.getConnection();
            conn.setAutoCommit(false); // 🔴 TRANSACTION

            // 🔥 DELETE SQL
            String deleteSQL = """
                    DELETE FROM Cart
                    WHERE CartAID = ?
                    """;

            psDelete = conn.prepareStatement(deleteSQL);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                // ---------- READ EXCEL ----------
                String cardAID = toString(row.getCell(0));

                if (cardAID != null) {
                    cardAID = cardAID.replace("\u00A0", "")
                            .replaceAll("\\s+", "")
                            .trim();
                }

                String statusOfCart = dbCRUDHelper.returnAID("Cart", "Status", "CartAID", cardAID);
                if (statusOfCart.equals("1")) {
                    errorMsg.append("Mã ").append(cardAID).append(" đã được xác nhận, không thể xóa");
                    hasError = true;
                    break;
                }

                // ---------- ADD BATCH DELETE ----------
                psDelete.setString(1, cardAID);
                psDelete.addBatch();

                currentRow++;
                progress.accept(currentRow, totalRows);

                // 🔥 batch mỗi 500 dòng
                if (currentRow % 500 == 0) {
                    psDelete.executeBatch();
                    psDelete.clearBatch();
                }
            }

            // ---------- COMMIT / ROLLBACK ----------
            if (!hasError) {
                psDelete.executeBatch();
                conn.commit();

                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thành công",
                        "Xóa dữ liệu từ Excel thành công",
                        Alert.AlertType.INFORMATION));

            } else {
                conn.rollback();
                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thất bại",
                        errorMsg.toString(),
                        Alert.AlertType.ERROR));
            }

        } catch (Exception e) {
            if (conn != null)
                conn.rollback();

            Platform.runLater(() -> customDialogNotification.showDialog(
                    "Lỗi",
                    "Xử lý Excel thất bại: " + e.getMessage(),
                    Alert.AlertType.ERROR));

            throw e;
        } finally {
            if (psDelete != null)
                psDelete.close();
            if (conn != null)
                conn.close();
        }
    }

    public void confirmExcelCart(
            String excelPath,
            BiConsumer<Integer, Integer> progress,
            int locationSheet) throws Exception {

        Connection conn = null;
        PreparedStatement psUpdate = null;

        try (FileInputStream fis = new FileInputStream(excelPath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(locationSheet);

            int totalRows = sheet.getLastRowNum();
            int currentRow = 0;
            boolean hasError = false;

            StringBuilder errorMsg = new StringBuilder();

            conn = DbHelper.getConnection();
            conn.setAutoCommit(false); // 🔴 TRANSACTION

            // 🔥 UPDATE STATUS ONLY
            String updateSQL = """
                        UPDATE Cart
                        SET Status = 1, LastTime = GETDATE()
                        WHERE CartAID = ?
                    """;

            psUpdate = conn.prepareStatement(updateSQL);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                // ---------- READ EXCEL ----------
                String cardAID = toString(row.getCell(0));

                if (cardAID != null) {
                    cardAID = cardAID.replace("\u00A0", "")
                            .replaceAll("\\s+", "")
                            .trim();
                }
                String statusOfCart = dbCRUDHelper.returnAID("Cart", "Status", "CartAID", cardAID);
                if (statusOfCart.equals("1")) {
                    errorMsg.append("Mã ").append(cardAID).append(" đã được xác nhận, không thể xóa");
                    hasError = true;
                    break;
                }

                // ---------- ADD BATCH ----------
                psUpdate.setString(1, cardAID);
                psUpdate.addBatch();

                currentRow++;
                progress.accept(currentRow, totalRows);

                if (currentRow % 500 == 0) {
                    psUpdate.executeBatch();
                    psUpdate.clearBatch();
                }
            }

            // ---------- COMMIT / ROLLBACK ----------
            if (!hasError) {
                psUpdate.executeBatch();
                conn.commit();

                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thành công",
                        "Cập nhật Status từ Excel thành công",
                        Alert.AlertType.INFORMATION));
            } else {
                conn.rollback();
                Platform.runLater(() -> customDialogNotification.showDialog(
                        "Thất bại",
                        errorMsg.toString(),
                        Alert.AlertType.ERROR));
            }

        } catch (Exception e) {
            if (conn != null)
                conn.rollback();

            Platform.runLater(() -> customDialogNotification.showDialog(
                    "Lỗi",
                    "Update Status thất bại: " + e.getMessage(),
                    Alert.AlertType.ERROR));

            throw e;
        } finally {
            if (psUpdate != null)
                psUpdate.close();
            if (conn != null)
                conn.close();
        }
    }

    public boolean exportExcelFromTemplate(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu file Excel");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("BienBan.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;

        try (
                FileInputStream fis = new FileInputStream("src/main/resources/excel/Records.xlsx");
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {
            Sheet sheet = workbook.getSheetAt(0);

            // ===== STYLE BORDER DÙNG CHUNG =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // style riêng cho STT (không bị %)
            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);
            DataFormat format = workbook.createDataFormat();
            sttStyle.setDataFormat(format.getFormat("General"));

            // ===== UPDATE NGÀY =====
            Row rowDate = sheet.getRow(4);
            if (rowDate != null) {
                Cell cellDate = rowDate.getCell(0);
                LocalDate today = LocalDate.now();

                String dateStr = "Hôm nay ngày "
                        + today.getDayOfMonth() + " tháng "
                        + today.getMonthValue() + " năm "
                        + today.getYear() + " tại Quảng Ninh";

                cellDate.setCellValue(dateStr);
            }

            int startRow = 17;
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            // đẩy chữ ký xuống
            sheet.shiftRows(startRow, lastRow, numberOfRows);

            // ===== GHI DATA =====
            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell newCell = newRow.createCell(j);

                    // ===== STT =====
                    if (j == 0) {
                        newCell.setCellValue(i + 1);
                        newCell.setCellStyle(sttStyle);
                        continue;
                    }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size()) {
                        value = rowData.get(mapCol.columnIndex);
                    }

                    newCell.setCellValue(value);
                    newCell.setCellStyle(borderStyle);
                }
            }

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportPdfFromTemplate(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu file PDF");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fc.setInitialFileName("BienBan.pdf");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;

        try {
            // ===== LOAD HTML TEMPLATE FROM RESOURCES =====
            FileInputStream is = new FileInputStream("src/main/resources/pdf/bienbanDeoNai.html");

            // ⭐ DEBUG QUAN TRỌNG

            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            // ===== DATE =====
            // ===== DATE =====
            LocalDate today = LocalDate.now();
            String dateStr = "Hôm nay ngày "
                    + today.getDayOfMonth() + " tháng "
                    + today.getMonthValue() + " năm "
                    + today.getYear() + " tại Quảng Ninh";

            // ⭐ replace đúng biến template mới
            html = html.replace("${dateLocation}", dateStr);
            html = html.replace("${companyDeliver}", "CÔNG TY CỔ PHẦN VIỆT Ý QN");
            html = html.replace("${companyReceive}", "CÔNG CP THAN ĐÈO NAI - CỌC SÁU - TKV");
            html = html.replace("${deliverPerson}", "Nguyễn Đình Mạnh");
            html = html.replace("${deliverRole}", "CB Cty");

            // ===== HEADER + ROWS =====
            html = html.replace("${headers}",
                    convertHTMLtoPDF.buildHeaders(columns));

            html = html.replace("${rows}",
                    convertHTMLtoPDF.buildRows(tableView, columns));

            // ===== EXPORT PDF =====
            convertHTMLtoPDF.htmlToPdf(html, file);

            System.out.println("Export PDF thành công!");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** --- Lấy danh sách cột được hiển thị (ShowHide = 1) --- */
    public List<String> getColumnListShowhide() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT NameColumn FROM NameColumns WHERE ShowHide = 1";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(rs.getString("NameColumn"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** --- Lấy column Map để đổi tên hiển thị --- */
    public Map<String, String> getColumnMap() {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT NameColumn, NameShow FROM NameColumns";
        try (Connection conn = DbHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                map.put(rs.getString("NameColumn"), rs.getString("NameShow"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean isValidCode(String text) {

        if (text == null)
            return false;

        // Chuẩn hoá dữ liệu từ Excel / TextField
        text = text
                .replace("\u00A0", "")
                .replaceAll("\\s+", "")
                .trim();

        // Chỉ cần đủ 7 ký tự
        return text.length() == 7;
    }

    private final DataFormatter FORMATTER = new DataFormatter();

    private final String[] DATE_PATTERNS = {
            "dd/MM/yyyy",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy HH:mm:ss"
    };

    // ========================= STRING =========================
    public String toString(Cell cell) {
        if (cell == null)
            return null;

        String value = FORMATTER.formatCellValue(cell)
                .replace("\u00A0", "")
                .trim();

        return value.isEmpty() ? null : value;
    }

    // ========================= INTEGER =========================
    public int toInt(Cell cell) {
        try {
            String value = toString(cell);
            if (value == null)
                return 0;

            value = value.replace(",", "")
                    .replaceAll("[^0-9\\-]", "");

            return value.isEmpty() ? 0 : Integer.parseInt(value);

        } catch (Exception e) {
            return 0;
        }
    }

    // ========================= DOUBLE =========================
    public Double toDouble(Cell cell) {
        if (cell == null)
            return 0.0;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    String v = cell.getStringCellValue().trim();
                    if (v.isEmpty())
                        return 0.0;
                    return Double.parseDouble(v);
                default:
                    return 0.0;
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    // ========================= TIMESTAMP =========================
    public Timestamp toTimestamp(Cell cell) {
        if (cell == null)
            return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC
                    && DateUtil.isCellDateFormatted(cell)) {

                return new Timestamp(cell.getDateCellValue().getTime());
            }

            String value = toString(cell);
            if (value == null)
                return null;

            for (String pattern : DATE_PATTERNS) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    sdf.setLenient(false);
                    Date date = sdf.parse(value);
                    return new Timestamp(date.getTime());
                } catch (Exception ignore) {
                }
            }
            return null;

        } catch (Exception e) {
            return null;
        }
    }

    public Timestamp toTimestampOrNow(Cell cell) {
        Timestamp ts = toTimestamp(cell);
        return ts != null ? ts : new Timestamp(System.currentTimeMillis());
    }

    // ==========================================================
    // =================== JDBC SAFE SETTERS ====================
    // ==========================================================

    public void setNullableInt(
            PreparedStatement ps, int index, Cell cell) throws SQLException {

        ps.setInt(index, toInt(cell));
    }

    public void setNullableDouble(
            PreparedStatement ps, int index, Cell cell) throws SQLException {

        Double v = toDouble(cell);
        if (v == null)
            ps.setNull(index, Types.DOUBLE);
        else
            ps.setDouble(index, v);
    }

    public void setNullableString(
            PreparedStatement ps, int index, Cell cell) throws SQLException {

        String v = toString(cell);
        if (v == null)
            ps.setNull(index, Types.NVARCHAR);
        else
            ps.setString(index, v);
    }

    public void setNullableTimestamp(
            PreparedStatement ps, int index, Cell cell) throws SQLException {

        Timestamp v = toTimestamp(cell);
        if (v == null)
            ps.setNull(index, Types.TIMESTAMP);
        else
            ps.setTimestamp(index, v);
    }

    private String validateRow(Row row) {
        try {
            DrawerItem selectedDrawerItem = AppState.getInstance().get("selectedDrawerItem", DrawerItem.class);

            String code = row.getCell(0) == null
                    ? null
                    : row.getCell(0).toString().trim();
            String result = "";
            // if (!isValidCode(code)) {
            // result += "Mã sản phẩm sai định dạng (VD: AB12345678), ";
            // }
            if (selectedDrawerItem.getWareHouseSupplierID() == 41) {
                String qty = dbCRUDHelper.returnAID(
                        selectedDrawerItem.getWareHouseTable(),
                        "Qty",
                        "ProductID",
                        code);
                if (qty == null || qty.isEmpty() || Double.parseDouble(qty) <= 0) {
                    result += "Số lượng trong kho đang không còn, ";
                }
            }

            if (!isValidQuantity(row.getCell(1))) {
                result += "Số lượng phải > 0, ";
            }

            return result.isEmpty() ? "" : result;
        } catch (Exception e) {
            // TODO: handle exception
            return "Lỗi định dạng dữ liệu";
        }

    }

    private boolean isValidQuantity(Cell cell) {
        double qty = toDouble(cell);
        return qty > 0;
    }

    public String safeTrim(TextField tf) {
        return (tf == null || tf.getText() == null) ? "" : tf.getText().trim();
    }

    public String safeTrim(TextArea ta) {
        return (ta == null || ta.getText() == null) ? "" : ta.getText().trim();
    }

    public String generateTransferGroupID(String conditionFrom, String conditionTo) {

        LocalDateTime now = LocalDateTime.now();

        // ddMMyy
        String datePart = String.format("%02d%02d%02d",
                now.getDayOfMonth(),
                now.getMonthValue(),
                now.getYear() % 100);

        // ssmmHH
        String timePart = String.format("%02d%02d%02d",
                now.getSecond(),
                now.getMinute(),
                now.getHour());

        // random 6 số
        Random random = new Random();
        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            randomPart.append(random.nextInt(10));
        }

        // chỉ số
        return conditionFrom + datePart + timePart + randomPart.toString() + conditionTo;
    }

    private String normalizeProductID(String value) {

        if (value == null)
            return "";

        return value
                .replace("\u00A0", "") // space ẩn excel
                .replaceAll("\\s+", "") // mọi khoảng trắng
                .trim()
                .toUpperCase(); // đồng bộ DB
    }

    private String normalizeHeader(String header) {

        if (header == null)
            return "";

        return header
                .replace("\u00A0", " ") // NBSP
                .replace("\n", "")
                .replace("\r", "")
                .replace("\t", "")
                .trim()
                .replaceAll("\\s+", " ") // nhiều space -> 1 space
                .toLowerCase();
    }

    public String generateCodeBH() {
        LocalDateTime now = LocalDateTime.now();

        String day = String.format("%02d", now.getDayOfMonth());
        String month = String.format("%02d", now.getMonthValue());
        String year = String.valueOf(now.getYear());
        String sec = String.format("%02d", now.getSecond());
        String min = String.format("%02d", now.getMinute());
        String hour = String.format("%02d", now.getHour());

        // return "BH-" + day + month + year + hour + min + sec;
        return day + month + year + hour + min + sec;
    }

    public void printRowColumns(ObservableList<String> row) {
        System.out.println("===== DEBUG ROW COLUMN INDEX =====");
        for (int i = 0; i < row.size(); i++) {
            System.out.println("Index " + i + " = " + row.get(i));
        }
        System.out.println("==================================");
    }

    public String removePrefixLetters(String input) {
        if (input == null)
            return null;
        return input.replaceFirst("^[A-Za-z]+", "");
    }

    // Thêm các thư viện này ở đầu file FunctionHelper.java nếu chưa có

    // Thêm 2 hàm này vào bên trong class FunctionHelper
    public String formatCurrency(String value) {
        if (value == null || value.isBlank())
            return "0";
        try {
            double amount = Double.parseDouble(value);
            // Format theo chuẩn tiền tệ phân cách dấu phẩy hàng nghìn (hoặc chỉnh theo
            // format cũ của bạn)
            DecimalFormat formatter = new DecimalFormat("#,###");
            return formatter.format(amount);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    public String formatQuantity(String value) {
        if (value == null || value.isBlank())
            return "0";
        try {
            double qty = Double.parseDouble(value);
            // Nếu là số nguyên thì bỏ phần thập phân .0, nếu có số lẻ thì giữ lại
            if (qty == (long) qty) {
                return String.valueOf((long) qty);
            } else {
                return String.valueOf(qty);
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }
}
