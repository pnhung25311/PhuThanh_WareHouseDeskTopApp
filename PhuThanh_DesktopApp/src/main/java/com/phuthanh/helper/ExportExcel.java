package com.phuthanh.helper;


import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

public class ExportExcel {

    // =========================
    // CLASS MAP CỘT CHO MỖI SHEET
    // =========================
    public class SheetMapping {
        public int[] tableIndexes;
        public SheetMapping(int... tableIndexes) {
            this.tableIndexes = tableIndexes;
        }
    }

    // =========================
    // HÀM EXPORT CHÍNH
    // =========================
    public boolean exportFromOneTableTo4Sheets(
            TableView<ObservableList<String>> tableView,
            List<SheetMapping> mappings,
            Stage stage) {

        if (mappings.size() != 4) {
            System.out.println("Template cần đúng 4 sheet!");
            return false;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu file Excel");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("BienBan.xlsx");

        File file = fc.showSaveDialog(stage);
        if (file == null) return false;

        try (
            FileInputStream fis = new FileInputStream("src/main/resources/excel/Records.xlsx");
            Workbook workbook = new XSSFWorkbook(fis);
            FileOutputStream fos = new FileOutputStream(file)
        ) {

            // ===== STYLE BORDER =====
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);
            borderStyle.setAlignment(HorizontalAlignment.CENTER);
            borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle sttStyle = workbook.createCellStyle();
            sttStyle.cloneStyleFrom(borderStyle);
            DataFormat format = workbook.createDataFormat();
            sttStyle.setDataFormat(format.getFormat("General"));

            // ===== LOOP 4 SHEETS =====
            for (int sheetIndex = 0; sheetIndex < 4; sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                SheetMapping mapping = mappings.get(sheetIndex);

                updateDate(sheet);
                writeDataToSheet(sheet, tableView, mapping, borderStyle, sttStyle);
            }

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // UPDATE NGÀY TRONG TEMPLATE
    // =========================
    private void updateDate(Sheet sheet) {
        Row rowDate = sheet.getRow(4);
        if (rowDate == null) return;

        Cell cellDate = rowDate.getCell(0);
        if (cellDate == null) return;

        LocalDate today = LocalDate.now();
        String dateStr = "Hôm nay ngày "
                + today.getDayOfMonth() + " tháng "
                + today.getMonthValue() + " năm "
                + today.getYear() + " tại Quảng Ninh";

        cellDate.setCellValue(dateStr);
    }

    // =========================
    // GHI DATA VÀO 1 SHEET
    // =========================
    private void writeDataToSheet(
            Sheet sheet,
            TableView<ObservableList<String>> tableView,
            SheetMapping mapping,
            CellStyle borderStyle,
            CellStyle sttStyle) {

        int startRow = 17; // dòng bắt đầu ghi data trong template
        Row templateRow = sheet.getRow(startRow);

        int numberOfRows = tableView.getItems().size();
        int lastRow = sheet.getLastRowNum();

        // đẩy phần chữ ký xuống
        sheet.shiftRows(startRow, lastRow, numberOfRows);

        for (int i = 0; i < numberOfRows; i++) {

            Row newRow = sheet.createRow(startRow + i);
            newRow.setHeight(templateRow.getHeight());

            ObservableList<String> rowData = tableView.getItems().get(i);

            for (int col = 0; col < mapping.tableIndexes.length; col++) {

                Cell cell = newRow.createCell(col);

                // Cột STT
                if (col == 0) {
                    cell.setCellValue(i + 1);
                    cell.setCellStyle(sttStyle);
                    continue;
                }

                int tableIndex = mapping.tableIndexes[col];
                String value = tableIndex < rowData.size()
                        ? rowData.get(tableIndex)
                        : "";

                cell.setCellValue(value);
                cell.setCellStyle(borderStyle);
            }
        }
    }
}
