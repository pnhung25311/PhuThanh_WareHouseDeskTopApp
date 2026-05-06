package com.phuthanh.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.phuthanh.helper.dialog.ExcelTemplateDialogHelper;
import com.phuthanh.model.enums.ExcelTemplateType;
import com.phuthanh.model.helper.ExcelColumn;
import com.phuthanh.model.helper.ModelExportExcelCart;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormat;

public class FunctionExportExcel {
    // Định nghĩa các cột tĩnh để dùng chung
    public static final ExcelColumn COL_STT = new ExcelColumn("STT", -1);
    public static final ExcelColumn COL_MA_SP = new ExcelColumn("Mã sản phẩm", 5);
    public static final ExcelColumn COL_DANH_DIEM = new ExcelColumn("Danh điểm", 6);
    public static final ExcelColumn COL_TEN_SP = new ExcelColumn("Tên sản phẩm", 7);
    public static final ExcelColumn COL_HANG_SX = new ExcelColumn("Hãng Sản Xuất", 9);
    public static final ExcelColumn COL_XUAT_XU = new ExcelColumn("Xuất xứ", 11);
    public static final ExcelColumn COL_DVT = new ExcelColumn("ĐVT", 13);
    public static final ExcelColumn COL_DONG_XE = new ExcelColumn("Dòng xe", 14);
    public static final ExcelColumn COL_XUAT_KHO = new ExcelColumn("Xuất kho", 15);
    public static final ExcelColumn COL_MA_VAT = new ExcelColumn("Mã sản phẩm VAT", 16);
    public static final ExcelColumn COL_DV_NHAP = new ExcelColumn("DV nhập", 18);
    public static final ExcelColumn COL_SL = new ExcelColumn("SL", 19);
    public static final ExcelColumn COL_DON_GIA = new ExcelColumn("Đơn giá", 20);
    public static final ExcelColumn COL_THANH_TIEN = new ExcelColumn("Thành tiền", 21);
    public static final ExcelColumn COL_GIA_VON = new ExcelColumn("Giá vốn", 22);
    public static final ExcelColumn COL_GIA_VAT = new ExcelColumn("Giá VAT", 23);
    public static final ExcelColumn COL_PAY_24 = new ExcelColumn("Tình trạng thanh toán", 24);
    public static final ExcelColumn COL_PAY_25 = new ExcelColumn("Tình trạng thanh toán", 25);
    public static final ExcelColumn COL_HOA_DON = new ExcelColumn("Hóa đơn", 27);
    public static final ExcelColumn COL_NOI_LAY = new ExcelColumn("Nơi lấy", 29);
    public static final ExcelColumn COL_NOI_GIAO = new ExcelColumn("Nơi giao", 31);
    public static final ExcelColumn COL_NHAN_VIEN = new ExcelColumn("Nhân viên", 33);
    public static final ExcelColumn COL_NGAY_VC = new ExcelColumn("Ngày vận chuyển", 36);
    public static final ExcelColumn COL_NOTE_37 = new ExcelColumn("Ghi chú", 37);
    public static final ExcelColumn COL_NOTE_38 = new ExcelColumn("Ghi chú", 38);
    public static final ExcelColumn COL_SO_HD = new ExcelColumn("Số HĐ", 41);
    public static final ExcelColumn COL_NOTE_100 = new ExcelColumn("Ghi chú", 100);

    // Một bộ cột cơ bản mà hầu như case nào cũng có
    public static List<ExcelColumn> getBaseCols() {
        return List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL);
    }

    public boolean exportExcelByTemplate(
            TableView<ObservableList<String>> tableView,
            Stage stage) {

        // B1: chọn template
        Optional<ModelExportExcelCart> result =
        new ExcelTemplateDialogHelper().chooseTemplateDialog();
        if (result == null||result.isEmpty())
            return false;
        ModelExportExcelCart template = result.get();
        LocalDate selectDate = template.getDate();
        ExcelTemplateType selectTemplate = template.getExcelTemplateType();

        // B2: mapping cột theo từng template
        switch (selectTemplate) {

            case RECORDSDEONAI:
                return exportExcelFromTemplateDEONAI(tableView, List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_HANG_SX,
                        COL_XUAT_XU, COL_DVT, COL_SL, COL_SO_HD, COL_NOTE_38, COL_MA_SP), stage, selectDate);

            case RECORDSKTKS:
                return exportExcelFromTemplateKTKS(tableView, List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU,
                        COL_DVT, COL_SL, COL_DONG_XE, COL_NOTE_100, COL_MA_SP, COL_NOTE_38), stage, selectDate);

            case RECORDSIMPORTWH:
                return exportExcelFromTemplateIMPORTWH(tableView,
                        List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                                COL_MA_SP, COL_DON_GIA, COL_GIA_VAT, COL_NOI_LAY, COL_NOI_GIAO, COL_PAY_25, COL_HOA_DON,
                                COL_MA_VAT, COL_NOTE_37, COL_NHAN_VIEN, COL_NGAY_VC),
                        stage, selectDate);

            case RECORDSEXPORTWH:
                return exportExcelFromTemplateEXPORTWH(tableView,
                        List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_XUAT_KHO,
                                COL_MA_SP, COL_DON_GIA, COL_GIA_VAT, COL_NOI_LAY, COL_NOI_GIAO, COL_PAY_24, COL_HOA_DON,
                                COL_MA_VAT, COL_NOTE_38, COL_NHAN_VIEN, COL_NGAY_VC),
                        stage, selectDate);

            case RECORDSTHL:
                return exportExcelFromTemplateTHL(
                        tableView, List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL,
                                COL_DONG_XE, COL_MA_SP, COL_NOTE_38, COL_GIA_VON, COL_DON_GIA, COL_SL, COL_HOA_DON),
                        stage, selectDate);

            case RECORDSCBT:
                return exportExcelFromTemplateCBT(tableView,
                        List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                                COL_MA_SP, COL_DON_GIA, COL_THANH_TIEN, COL_NOTE_38, COL_NGAY_VC, COL_GIA_VON,
                                COL_HOA_DON, COL_NOTE_100),
                        stage, selectDate);

            case RECORDSCNMLK:
                return exportExcelFromTemplateCNMLK(tableView,
                        List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                                COL_MA_SP, COL_DON_GIA, COL_THANH_TIEN, COL_NOTE_38, COL_NGAY_VC, COL_GIA_VON,
                                COL_DV_NHAP, COL_HOA_DON, COL_NOTE_100),
                        stage, selectDate);

            case RECORDSCNMKD:
                return exportExcelFromTemplateCNMKD(tableView,
                        List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                                COL_MA_SP, COL_DON_GIA, COL_THANH_TIEN, COL_NOTE_38, COL_NGAY_VC, COL_THANH_TIEN,
                                COL_NOTE_100, COL_HOA_DON, COL_NOTE_100, COL_NOTE_100),
                        stage, selectDate);

            case RECORDSDONGBAC:
                return exportExcelFromTemplateDONGBAC(tableView,
                        List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                                COL_MA_SP, COL_DON_GIA, COL_THANH_TIEN, COL_NOTE_38, COL_NGAY_VC, COL_GIA_VON,
                                COL_DV_NHAP, COL_HOA_DON),
                        stage, selectDate);

            case RECORDSPHUONGSON:
                return exportExcelFromTemplatePHUONGSON(tableView,
                        List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                                COL_MA_SP, COL_DON_GIA, COL_THANH_TIEN, COL_NOTE_38, COL_NGAY_VC, COL_GIA_VON,
                                COL_DV_NHAP, COL_HOA_DON),
                        stage, selectDate);

            case RECORDSUONGBI:
                return exportExcelFromTemplateUONGBI(tableView, List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU,
                        COL_DVT, COL_SL, COL_NOTE_38, COL_MA_SP), stage, selectDate);

            case RECORDSCAOSON:
                return exportExcelFromTemplateCAOSON(tableView,
                        List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL,
                                COL_DONG_XE, COL_MA_SP, COL_NOTE_38),
                        stage, selectDate);
            case RECORDSKHESIM:
                return exportExcelFromTemplateKHESIM(tableView,
                        List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL,
                                COL_DONG_XE, COL_MA_SP, COL_NOTE_38),
                        stage, selectDate);

            case RECORDSVIETBAC:
                return exportExcelFromTemplateVIETBAC(tableView,
                        List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL,
                                COL_NOTE_38),
                        stage, selectDate);
            case RECORDSAPLUC:
                return exportExcelFromTemplateAPLUC(tableView,
                        List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL,
                                COL_NOTE_38),
                        stage, selectDate);

            case RECORDSBANLE:
                return exportExcelFromTemplateBANLE(tableView, List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU,
                        COL_DVT, COL_SL, COL_DONG_XE, COL_MA_SP, COL_DON_GIA, COL_THANH_TIEN, COL_NOTE_38), stage, selectDate);
        }

        return false;
    }

    public boolean exportExcelFromTemplateKTKS(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu biên bản giao nhận KTKS");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_giao_hang_KTKS.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;
        String templatePath = "C:\\PY\\fileExcel\\RecordsKTKS.xlsx";
        try (FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ================= STYLE =================
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            borderStyle.setWrapText(true);

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);
            sttStyle.setDataFormat(workbook.createDataFormat().getFormat("General"));

            // ================= UPDATE NGÀY =================
            Row rowDate = sheet.getRow(4);
            LocalDate today = date;

            String dateStr = "Hôm nay ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear()
                    + " tại Quảng Ninh";

            rowDate.getCell(0).setCellValue(dateStr);

            // ================= UPDATE TÊN CÔNG TY =================
            sheet.getRow(6).getCell(0)
                    .setCellValue("ĐẠI DIỆN BÊN GIAO : CÔNG TY CỔ PHẦN VIỆT Ý QN");

            sheet.getRow(9).getCell(0)
                    .setCellValue("ĐẠI DIỆN BÊN NHẬN BÀN GIAO: CÔNG TY KHAI THÁC KHOÁNG SẢN");

            // ================= INSERT TABLE =================
            int startRow = 15; // dòng bắt đầu bảng
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            // 👉 đẩy phần chữ ký xuống
            sheet.shiftRows(startRow, lastRow, numberOfRows);

            // ================= GHI DATA =================
            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                for (int j = 0; j < columns.size(); j++) {

                    Cell newCell = newRow.createCell(j);

                    // STT
                    if (j == 0) {
                        newCell.setCellValue(i + 1);
                        newCell.setCellStyle(sttStyle);
                        continue;
                    }

                    String value = "";
                    if (columns.get(j).columnIndex < rowData.size()) {
                        value = rowData.get(columns.get(j).columnIndex);
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

    public boolean exportExcelFromTemplateDEONAI(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu file Excel");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_giao_hang_DEONAI.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;
        String templatePath = "C:\\PY\\fileExcel\\RecordsDEONAI.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
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
                LocalDate today = date;

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

    public boolean exportExcelFromTemplateIMPORTWH(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Phiếu Nhập Kho");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("phieu_nhap_kho.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;
        String templatePath = "C:\\PY\\fileExcel\\RecordsIMPORTWH.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ================= STYLE BORDER =================
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);
            DataFormat format = workbook.createDataFormat();
            sttStyle.setDataFormat(format.getFormat("General"));

            // ================= UPDATE HEADER =================
            LocalDate today = date;
            String dateStr = "Ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear();

            sheet.getRow(1).getCell(0).setCellValue(dateStr);
            sheet.getRow(2).getCell(0).setCellValue("Người đề nghị : ");
            sheet.getRow(3).getCell(0).setCellValue("Lý do nhập: ");

            // ================= VỊ TRÍ TABLE =================
            int startRow = 5; // dòng bắt đầu data (sau header)
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            // 👉 Đẩy phần chữ ký xuống
            sheet.shiftRows(startRow, lastRow, numberOfRows);

            // ================= GHI DATA =================
            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // ===== STT =====
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    // ===== DATA =====
                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExcelFromTemplateEXPORTWH(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Phiếu Nhập Kho");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("phieu_nhap_kho.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;
        String templatePath = "C:\\PY\\fileExcel\\RecordsEXPORTWH.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ================= STYLE BORDER =================
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);
            DataFormat format = workbook.createDataFormat();
            sttStyle.setDataFormat(format.getFormat("General"));

            // ================= UPDATE HEADER =================
            LocalDate today = date;
            String dateStr = "Ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear();

            sheet.getRow(1).getCell(0).setCellValue(dateStr);
            sheet.getRow(2).getCell(0).setCellValue("Người đề nghị : ");
            sheet.getRow(3).getCell(0).setCellValue("Lý do nhập: ");

            // ================= VỊ TRÍ TABLE =================
            int startRow = 5; // dòng bắt đầu data (sau header)
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            // 👉 Đẩy phần chữ ký xuống
            sheet.shiftRows(startRow, lastRow, numberOfRows);

            // ================= GHI DATA =================
            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // ===== STT =====
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    // ===== DATA =====
                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExcelFromTemplateTHL(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Biên Bản THL");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_thl.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;
        String templatePath = "C:\\PY\\fileExcel\\RecordsTHL.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ================= STYLE BORDER =================
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);
            DataFormat format = workbook.createDataFormat();
            sttStyle.setDataFormat(format.getFormat("General"));

            // ================= UPDATE HEADER =================
            LocalDate today = date;
            String dateStr = "Hôm nay ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear()
                    + " tại Cẩm Phả Quảng Ninh";

            Row rowDate = sheet.getRow(4);
            if (rowDate != null)
                rowDate.getCell(0).setCellValue(dateStr);

            // ================= TABLE START =================
            int startRow = 12; // dòng bắt đầu table (theo file bạn gửi)
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            // đẩy chữ ký xuống
            sheet.shiftRows(startRow, lastRow, numberOfRows);

            // ================= WRITE DATA =================
            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // STT
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExcelFromTemplateCBT(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Biên Bản CBT");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_cbt.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;
        String templatePath = "C:\\PY\\fileExcel\\RecordsCBT.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ===== STYLE =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.cloneStyleFrom(borderStyle);
            DataFormat df = workbook.createDataFormat();
            moneyStyle.setDataFormat(df.getFormat("#,##0"));

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);

            // ===== UPDATE DATE =====
            LocalDate today = date;
            String dateStr = "Hôm nay ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear()
                    + " tại Cẩm Phả Quảng Ninh";

            sheet.getRow(4).getCell(0).setCellValue(dateStr);

            // ===== TABLE =====
            int startRow = 13;
            // int totalMoneyRowIndex;
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            sheet.shiftRows(startRow, lastRow, numberOfRows);

            // double totalMoney = 0;

            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                // double qty = parseDoubleSafe(rowData.get(8));
                // double price = parseDoubleSafe(rowData.get(18));
                // double rowTotal = qty * price;
                // totalMoney += rowTotal;

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // STT
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    // Thành tiền (cột 9)
                    // if (j == 9) {
                    // cell.setCellValue(rowTotal);
                    // cell.setCellStyle(moneyStyle);
                    // continue;
                    // }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            // ===== WRITE TOTAL ROW =====
            // totalMoneyRowIndex = startRow + numberOfRows;

            // Row totalRow = sheet.createRow(totalMoneyRowIndex);
            // Cell labelCell = totalRow.createCell(8);
            // labelCell.setCellValue("Tổng cộng");

            // Cell totalCell = totalRow.createCell(9);
            // totalCell.setCellValue(totalMoney);
            // totalCell.setCellStyle(moneyStyle);

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExcelFromTemplateCNMLK(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Biên Bản CNMLK");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_cnmlk.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;
        String templatePath = "C:\\PY\\fileExcel\\RecordsCNMLK.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ===== STYLE =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.cloneStyleFrom(borderStyle);
            DataFormat df = workbook.createDataFormat();
            moneyStyle.setDataFormat(df.getFormat("#,##0"));

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);

            // ===== UPDATE DATE =====
            LocalDate today = date;
            String dateStr = "Hôm nay ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear()
                    + " tại Cẩm Phả Quảng Ninh";

            sheet.getRow(4).getCell(0).setCellValue(dateStr);

            // ===== TABLE =====
            int startRow = 12;
            // int totalMoneyRowIndex;
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            sheet.shiftRows(startRow, lastRow, numberOfRows);

            // double totalMoney = 0;

            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                // double qty = parseDoubleSafe(rowData.get(8));
                // double price = parseDoubleSafe(rowData.get(18));
                // double rowTotal = qty * price;
                // totalMoney += rowTotal;

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // STT
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    // Thành tiền (cột 9)
                    // if (j == 9) {
                    // cell.setCellValue(rowTotal);
                    // cell.setCellStyle(moneyStyle);
                    // continue;
                    // }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            // ===== WRITE TOTAL ROW =====
            // totalMoneyRowIndex = startRow + numberOfRows;

            // Row totalRow = sheet.createRow(totalMoneyRowIndex);
            // Cell labelCell = totalRow.createCell(8);
            // labelCell.setCellValue("Tổng cộng");

            // Cell totalCell = totalRow.createCell(9);
            // totalCell.setCellValue(totalMoney);
            // totalCell.setCellStyle(moneyStyle);

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExcelFromTemplateCNMKD(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Biên Bản CNMKD");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_cnmkd.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;
        String templatePath = "C:\\PY\\fileExcel\\RecordsCNMKD.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ===== STYLE =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.cloneStyleFrom(borderStyle);
            DataFormat df = workbook.createDataFormat();
            moneyStyle.setDataFormat(df.getFormat("#,##0"));

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);

            // ===== UPDATE DATE =====
            LocalDate today = date;
            String dateStr = "Hôm nay ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear()
                    + " tại Cẩm Phả Quảng Ninh";

            sheet.getRow(4).getCell(0).setCellValue(dateStr);

            // ===== TABLE =====
            int startRow = 12;
            // int totalMoneyRowIndex;
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            sheet.shiftRows(startRow, lastRow, numberOfRows);

            // double totalMoney = 0;

            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                // double qty = parseDoubleSafe(rowData.get(8));
                // double price = parseDoubleSafe(rowData.get(18));
                // double rowTotal = qty * price;
                // totalMoney += rowTotal;

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // STT
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    // Thành tiền (cột 9)
                    // if (j == 9) {
                    // cell.setCellValue(rowTotal);
                    // cell.setCellStyle(moneyStyle);
                    // continue;
                    // }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            // ===== WRITE TOTAL ROW =====
            // totalMoneyRowIndex = startRow + numberOfRows;

            // Row totalRow = sheet.createRow(totalMoneyRowIndex);
            // Cell labelCell = totalRow.createCell(8);
            // labelCell.setCellValue("Tổng cộng");

            // Cell totalCell = totalRow.createCell(9);
            // totalCell.setCellValue(totalMoney);
            // totalCell.setCellStyle(moneyStyle);

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExcelFromTemplateDONGBAC(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Biên Bản Đông Bắc");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_dongbac.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;
        String templatePath = "C:\\PY\\fileExcel\\RecordsDONGBAC.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ===== STYLE =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.cloneStyleFrom(borderStyle);
            DataFormat df = workbook.createDataFormat();
            moneyStyle.setDataFormat(df.getFormat("#,##0"));

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);

            // ===== UPDATE DATE =====
            LocalDate today = date;
            String dateStr = "Hôm nay ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear()
                    + " tại Cẩm Phả Quảng Ninh";

            sheet.getRow(4).getCell(0).setCellValue(dateStr);

            // ===== TABLE =====
            int startRow = 12;
            // int totalMoneyRowIndex;
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            sheet.shiftRows(startRow, lastRow, numberOfRows);

            // double totalMoney = 0;

            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                // double qty = parseDoubleSafe(rowData.get(8));
                // double price = parseDoubleSafe(rowData.get(18));
                // double rowTotal = qty * price;
                // totalMoney += rowTotal;

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // STT
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    // Thành tiền (cột 9)
                    // if (j == 9) {
                    // cell.setCellValue(rowTotal);
                    // cell.setCellStyle(moneyStyle);
                    // continue;
                    // }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            // ===== WRITE TOTAL ROW =====
            // totalMoneyRowIndex = startRow + numberOfRows;

            // Row totalRow = sheet.createRow(totalMoneyRowIndex);
            // Cell labelCell = totalRow.createCell(8);
            // labelCell.setCellValue("Tổng cộng");

            // Cell totalCell = totalRow.createCell(9);
            // totalCell.setCellValue(totalMoney);
            // totalCell.setCellStyle(moneyStyle);

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExcelFromTemplatePHUONGSON(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Biên Bản Phương Sơn");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_phuongson.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;
        String templatePath = "C:\\PY\\fileExcel\\RecordsPHUONGSON.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ===== STYLE =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.cloneStyleFrom(borderStyle);
            DataFormat df = workbook.createDataFormat();
            moneyStyle.setDataFormat(df.getFormat("#,##0"));

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);

            // ===== UPDATE DATE =====
            LocalDate today = date;
            String dateStr = "Hôm nay ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear()
                    + " tại Cẩm Phả Quảng Ninh";

            sheet.getRow(4).getCell(0).setCellValue(dateStr);

            // ===== TABLE =====
            int startRow = 12;
            // int totalMoneyRowIndex;
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            sheet.shiftRows(startRow, lastRow, numberOfRows);

            // double totalMoney = 0;

            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                // double qty = parseDoubleSafe(rowData.get(8));
                // double price = parseDoubleSafe(rowData.get(18));
                // double rowTotal = qty * price;
                // totalMoney += rowTotal;

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // STT
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    // Thành tiền (cột 9)
                    // if (j == 9) {
                    // cell.setCellValue(rowTotal);
                    // cell.setCellStyle(moneyStyle);
                    // continue;
                    // }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            // ===== WRITE TOTAL ROW =====
            // totalMoneyRowIndex = startRow + numberOfRows;

            // Row totalRow = sheet.createRow(totalMoneyRowIndex);
            // Cell labelCell = totalRow.createCell(8);
            // labelCell.setCellValue("Tổng cộng");

            // Cell totalCell = totalRow.createCell(9);
            // totalCell.setCellValue(totalMoney);
            // totalCell.setCellStyle(moneyStyle);

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // private double parseDoubleSafe(String s) {
    // try {
    // return Double.parseDouble(s.replace(",", "").trim());
    // } catch (Exception e) {
    // return 0;
    // }
    // }

    public boolean exportExcelFromTemplateUONGBI(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Biên Bản Uông bí");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_uong_bi.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;

        String templatePath = "C:\\PY\\fileExcel\\RecordsUONGBI.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ===== STYLE =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);

            // ===== UPDATE DATE =====
            LocalDate today = date;
            String dateStr = "Hôm nay: Ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear()
                    + " tại Cẩm Phả Quảng Ninh";

            sheet.getRow(3).getCell(0).setCellValue(dateStr);

            // ===== TABLE =====
            int startRow = 12;
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            sheet.shiftRows(startRow, lastRow, numberOfRows);

            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // STT
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExcelFromTemplateCAOSON(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Biên Bản Giao Nhận");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_cao_son.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;

        String templatePath = "C:\\PY\\fileExcel\\RecordsCAOSON.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ===== STYLE =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);

            // ===== UPDATE DATE =====
            LocalDate today = date;
            String dateStr = "Hôm nay ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear()
                    + " tại Cẩm Phả Quảng Ninh";

            sheet.getRow(4).getCell(0).setCellValue(dateStr);

            // ===== TABLE =====
            int startRow = 12;
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            sheet.shiftRows(startRow, lastRow, numberOfRows);

            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // STT
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExcelFromTemplateKHESIM(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Biên Bản Giao Nhận");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_khe_sim.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;

        String templatePath = "C:\\PY\\fileExcel\\RecordsKheSim.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ===== STYLE =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);

            // ===== UPDATE DATE =====
            LocalDate today = date;
            String dateStr = "Hôm nay, ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear()
                    + " tại Quảng Ninh";

            sheet.getRow(4).getCell(0).setCellValue(dateStr);

            // ===== TABLE =====
            int startRow = 15; // bảng của mẫu này nằm thấp hơn mẫu trước
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            sheet.shiftRows(startRow, lastRow, numberOfRows);

            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // STT
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExcelFromTemplateVIETBAC(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Biên Bản Giao Nhận");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_viet_bac.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;

        String templatePath = "C:\\PY\\fileExcel\\RecordsVIETBAC.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ===== STYLE =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);

            // ===== UPDATE DATE =====
            LocalDate today = date;
            String dateStr = "Hôm nay, ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear()
                    + " tại Quảng Ninh";

            sheet.getRow(3).getCell(0).setCellValue(dateStr);

            // ===== TABLE =====
            int startRow = 12;
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            sheet.shiftRows(startRow, lastRow, numberOfRows);

            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // STT
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExcelFromTemplateAPLUC(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage,LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Biên Bản Giao Nhận");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_ap_luc.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;

        String templatePath = "C:\\PY\\fileExcel\\RecordsAPLUC.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ===== STYLE =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);

            // ===== UPDATE DATE =====
            LocalDate today = date;
            String dateStr = "Hôm nay, ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear();

            sheet.getRow(4).getCell(0).setCellValue(dateStr);

            // ===== TABLE =====
            int startRow = 15;
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            sheet.shiftRows(startRow, lastRow, numberOfRows);

            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // STT
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExcelFromTemplateBANLE(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage, LocalDate date) {

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Biên Bản Giao Nhận");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("bien_ban_ban_le.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null)
            return false;

        String templatePath = "C:\\PY\\fileExcel\\RecordsBANLE.xlsx";

        try (
                FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis);
                FileOutputStream fos = new FileOutputStream(file);) {

            Sheet sheet = workbook.getSheetAt(0);

            // ===== STYLE =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.cloneStyleFrom(borderStyle);
            DataFormat df = workbook.createDataFormat();
            moneyStyle.setDataFormat(df.getFormat("#,##0"));

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);

            // ===== UPDATE DATE =====
            LocalDate today = date;
            String dateStr = "Hôm nay ngày " + today.getDayOfMonth()
                    + " tháng " + today.getMonthValue()
                    + " năm " + today.getYear()
                    + " tại Cẩm Phả Quảng Ninh";

            sheet.getRow(1).getCell(0).setCellValue(dateStr);

            // ===== TABLE =====
            int startRow = 8; // bảng của mẫu này nằm cao hơn CNMLK
            // int totalMoneyRowIndex;
            Row templateRow = sheet.getRow(startRow);

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            sheet.shiftRows(startRow, lastRow, numberOfRows);

            // double totalMoney = 0;

            for (int i = 0; i < numberOfRows; i++) {

                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight(templateRow.getHeight());

                ObservableList<String> rowData = tableView.getItems().get(i);

                // double qty = parseDoubleSafe(rowData.get(8));
                // double price = parseDoubleSafe(rowData.get(18));
                // double rowTotal = qty * price;
                // totalMoney += rowTotal;

                for (int j = 0; j < columns.size(); j++) {

                    ExcelColumn mapCol = columns.get(j);
                    Cell cell = newRow.createCell(j);

                    // STT
                    if (mapCol.columnIndex == -1) {
                        cell.setCellValue(i + 1);
                        cell.setCellStyle(sttStyle);
                        continue;
                    }

                    // Thành tiền (cột 9)
                    // if (j == 9) {
                    // cell.setCellValue(rowTotal);
                    // cell.setCellStyle(moneyStyle);
                    // continue;
                    // }

                    String value = "";
                    if (mapCol.columnIndex < rowData.size())
                        value = rowData.get(mapCol.columnIndex);

                    cell.setCellValue(value);
                    cell.setCellStyle(borderStyle);
                }
            }

            // ===== WRITE TOTAL ROW =====
            // totalMoneyRowIndex = startRow + numberOfRows;

            // Row totalRow = sheet.createRow(totalMoneyRowIndex);
            // Cell labelCell = totalRow.createCell(8);
            // labelCell.setCellValue("Tổng cộng");

            // Cell totalCell = totalRow.createCell(9);
            // totalCell.setCellValue(totalMoney);
            // totalCell.setCellStyle(moneyStyle);

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
