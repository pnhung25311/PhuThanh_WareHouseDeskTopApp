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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormat;

import com.phuthanh.helper.dialog.ExcelTemplateDialogHelper;
import com.phuthanh.model.enums.ExcelTemplateType;
import com.phuthanh.model.helper.ExcelColumn;
import com.phuthanh.model.helper.ModelExportExcelCart;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FunctionExportExcel {
    // Định nghĩa các cột tĩnh dùng chung
    public final ExcelColumn COL_STT = new ExcelColumn("STT", -1);
    public final ExcelColumn COL_MA_SP = new ExcelColumn("Mã sản phẩm", 5);
    public final ExcelColumn COL_DANH_DIEM = new ExcelColumn("Danh điểm", 6);
    public final ExcelColumn COL_TEN_SP = new ExcelColumn("Tên sản phẩm", 7);
    public final ExcelColumn COL_HANG_SX = new ExcelColumn("Hãng Sản Xuất", 9);
    public final ExcelColumn COL_XUAT_XU = new ExcelColumn("Xuất xứ", 11);
    public final ExcelColumn COL_DVT = new ExcelColumn("ĐVT", 13);
    public final ExcelColumn COL_DONG_XE = new ExcelColumn("Dòng xe", 14);
    public final ExcelColumn COL_XUAT_KHO = new ExcelColumn("Xuất kho", 15);
    public final ExcelColumn COL_MA_VAT = new ExcelColumn("Mã sản phẩm VAT", 17);
    public final ExcelColumn COL_DV_NHAP = new ExcelColumn("DV nhập", 19);
    public final ExcelColumn COL_SL = new ExcelColumn("SL", 20);
    public final ExcelColumn COL_DON_GIA = new ExcelColumn("Đơn giá", 21);
    public final ExcelColumn COL_THANH_TIEN = new ExcelColumn("Thành tiền", 22);
    public final ExcelColumn COL_GIA_VON = new ExcelColumn("Giá vốn", 23);
    public final ExcelColumn COL_GIA_VAT = new ExcelColumn("Giá VAT", 24);
    public final ExcelColumn COL_PAY_24 = new ExcelColumn("Tình trạng thanh toán", 29);
    public final ExcelColumn COL_PAY_25 = new ExcelColumn("Tình trạng thanh toán", 31);
    public final ExcelColumn COL_HOA_DON = new ExcelColumn("Hóa đơn", 31);
    public final ExcelColumn COL_NOI_LAY = new ExcelColumn("Nơi lấy", 33);
    public final ExcelColumn COL_NOI_GIAO = new ExcelColumn("Nơi giao", 35);
    public final ExcelColumn COL_NHAN_VIEN = new ExcelColumn("Nhân viên", 3);
    public final ExcelColumn COL_NGAY_VC = new ExcelColumn("Ngày vận chuyển", 40);
    public final ExcelColumn COL_NGAY_XGH = new ExcelColumn("Ngày xuất/giao hàng", 41);
    public final ExcelColumn COL_NOTE_37 = new ExcelColumn("Vị trí", 42);
    public final ExcelColumn COL_NOTE_38 = new ExcelColumn("Ghi chú", 43);
    public final ExcelColumn COL_SO_HD = new ExcelColumn("Số HĐ", 46);
    public final ExcelColumn COL_NOTE_100 = new ExcelColumn("Ghi chú", 100);

    // Lớp cấu hình nội bộ để tùy biến các mẫu Excel khác nhau
    private static class TemplateConfig {
        String dialogTitle;
        String defaultFileName;
        String templateName;
        int startRow;
        int dateRow;
        int dateCell = 0;
        String datePrefix;
        String dateSuffix;
        boolean hasCompanyNames = false;

        public TemplateConfig(String dialogTitle, String defaultFileName, String templateName, int startRow,
                int dateRow, String datePrefix, String dateSuffix) {
            this.dialogTitle = dialogTitle;
            this.defaultFileName = defaultFileName;
            this.templateName = templateName;
            this.startRow = startRow;
            this.dateRow = dateRow;
            this.datePrefix = datePrefix;
            this.dateSuffix = dateSuffix;
        }
    }

    public boolean exportExcelByTemplate(TableView<ObservableList<String>> tableView, Stage stage) {
        Optional<ModelExportExcelCart> result = new ExcelTemplateDialogHelper().chooseTemplateDialog();
        if (result == null || result.isEmpty()) {
            return false;
        }

        ModelExportExcelCart template = result.get();
        LocalDate selectDate = template.getDate();
        ExcelTemplateType selectTemplate = template.getExcelTemplateType();

        List<ExcelColumn> columns;
        TemplateConfig config;

        switch (selectTemplate) {
            case RECORDSDEONAI:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_HANG_SX, COL_XUAT_XU, COL_DVT, COL_SL,
                        COL_SO_HD, COL_NOTE_38, COL_MA_SP);
                config = new TemplateConfig("Lưu file Excel", "bien_ban_giao_hang_DEONAI.xlsx", "RecordsDEONAI.xlsx",
                        17, 4, "Hôm nay ngày ", " tại Quảng Ninh");
                break;

            case RECORDSKTKS:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                        COL_NOTE_100, COL_MA_SP, COL_NOTE_38);
                config = new TemplateConfig("Lưu biên bản giao nhận KTKS", "bien_ban_giao_hang_KTKS.xlsx",
                        "RecordsKTKS.xlsx", 15, 4, "Hôm nay ngày ", " tại Quảng Ninh");
                config.hasCompanyNames = true; // Trường hợp đặc biệt của KTKS
                break;

            case RECORDSIMPORTWH:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                        COL_MA_SP, COL_DON_GIA, COL_NOI_LAY, COL_NOI_GIAO, COL_NOTE_38, COL_HOA_DON, COL_NHAN_VIEN,
                        COL_NGAY_VC);
                config = new TemplateConfig("Lưu Phiếu Nhập Kho", "phieu_nhap_kho.xlsx", "RecordsIMPORTWH.xlsx", 9, 4,
                        "Ngày ", "");
                break;

            case RECORDSEXPORTWH:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                        COL_MA_SP, COL_DON_GIA, COL_NOI_LAY, COL_NOI_GIAO, COL_NOTE_38, COL_HOA_DON, COL_NHAN_VIEN,
                        COL_NGAY_VC);
                config = new TemplateConfig("Lưu Phiếu Xuất Kho", "phieu_xuat_kho.xlsx", "RecordsEXPORTWH.xlsx", 9, 4,
                        "Ngày ", "");
                break;

            case RECORDSTHL:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                        COL_MA_SP, COL_NOTE_38, COL_GIA_VON, COL_DON_GIA, COL_SL, COL_HOA_DON);
                config = new TemplateConfig("Lưu Biên Bản THL", "bien_ban_thl.xlsx", "RecordsTHL.xlsx", 12, 4,
                        "Hôm nay ngày ", " tại Cẩm Phả Quảng Ninh");
                break;

            case RECORDSCBT:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                        COL_MA_SP, COL_DON_GIA, COL_THANH_TIEN, COL_NOTE_38, COL_NGAY_VC, COL_GIA_VON, COL_HOA_DON,
                        COL_NOTE_100);
                config = new TemplateConfig("Lưu Biên Bản CBT", "bien_ban_cbt.xlsx", "RecordsCBT.xlsx", 13, 4,
                        "Hôm nay ngày ", " tại Cẩm Phả Quảng Ninh");
                break;

            case RECORDSCNMLK:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                        COL_MA_SP, COL_DON_GIA, COL_THANH_TIEN, COL_NOTE_38, COL_NGAY_VC, COL_GIA_VON, COL_DV_NHAP,
                        COL_HOA_DON, COL_NOTE_100);
                config = new TemplateConfig("Lưu Biên Bản CNMLK", "bien_ban_cnmlk.xlsx", "RecordsCNMLK.xlsx", 12, 4,
                        "Hôm nay ngày ", " tại Cẩm Phả Quảng Ninh");
                break;

            case RECORDSCNMKD:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                        COL_MA_SP, COL_DON_GIA, COL_THANH_TIEN, COL_NOTE_38, COL_NGAY_VC, COL_THANH_TIEN, COL_NOTE_100,
                        COL_HOA_DON, COL_NOTE_100, COL_NOTE_100);
                config = new TemplateConfig("Lưu Biên Bản CNMKD", "bien_ban_cnmkd.xlsx", "RecordsCNMKD.xlsx", 12, 4,
                        "Hôm nay ngày ", " tại Cẩm Phả Quảng Ninh");
                break;

            case RECORDSDONGBAC:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                        COL_MA_SP, COL_DON_GIA, COL_THANH_TIEN, COL_NOTE_38, COL_NGAY_VC, COL_GIA_VON, COL_DV_NHAP,
                        COL_HOA_DON);
                config = new TemplateConfig("Lưu Biên Bản Đông Bắc", "bien_ban_dongbac.xlsx", "RecordsDONGBAC.xlsx", 12,
                        4, "Hôm nay ngày ", " tại Cẩm Phả Quảng Ninh");
                break;

            case RECORDSPHUONGSON:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                        COL_MA_SP, COL_DON_GIA, COL_THANH_TIEN, COL_NOTE_38, COL_NGAY_VC, COL_GIA_VON, COL_DV_NHAP,
                        COL_HOA_DON);
                config = new TemplateConfig("Lưu Biên Bản Phương Sơn", "bien_ban_phuongson.xlsx",
                        "RecordsPHUONGSON.xlsx", 12, 4, "Hôm nay ngày ", " tại Cẩm Phả Quảng Ninh");
                break;

            case RECORDSUONGBI:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_NOTE_38,
                        COL_MA_SP);
                config = new TemplateConfig("Lưu Biên Bản Uông bí", "bien_ban_uong_bi.xlsx", "RecordsUONGBI.xlsx", 12,
                        3, "Hôm nay: Ngày ", " tại Cẩm Phả Quảng Ninh");
                break;

            case RECORDSCAOSON:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                        COL_MA_SP, COL_NOTE_38);
                config = new TemplateConfig("Lưu Biên Bản Giao Nhận", "bien_ban_cao_son.xlsx", "RecordsCAOSON.xlsx", 12,
                        4, "Hôm nay ngày ", " tại Cẩm Phả Quảng Ninh");
                break;

            case RECORDSKHESIM:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                        COL_MA_SP, COL_NOTE_38);
                config = new TemplateConfig("Lưu Biên Bản Giao Nhận", "bien_ban_khe_sim.xlsx", "RecordsKheSim.xlsx", 15,
                        4, "Hôm nay, ngày ", " tại Quảng Ninh");
                break;

            case RECORDSVIETBAC:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_NOTE_38);
                config = new TemplateConfig("Lưu Biên Bản Giao Nhận", "bien_ban_viet_bac.xlsx", "RecordsVIETBAC.xlsx",
                        12, 3, "Hôm nay, ngày ", " tại Quảng Ninh");
                break;

            case RECORDSAPLUC:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_NOTE_38);
                config = new TemplateConfig("Lưu Biên Bản Giao Nhận", "bien_ban_ap_luc.xlsx", "RecordsAPLUC.xlsx", 15,
                        4, "Hôm nay, ngày ", "");
                break;

            case RECORDSBANLE:
                columns = List.of(COL_STT, COL_TEN_SP, COL_DANH_DIEM, COL_XUAT_XU, COL_DVT, COL_SL, COL_DONG_XE,
                        COL_MA_SP, COL_DON_GIA, COL_THANH_TIEN, COL_NOTE_38);
                config = new TemplateConfig("Lưu Biên Bản Giao Nhận", "bien_ban_ban_le.xlsx", "RecordsBANLE.xlsx", 8, 1,
                        "Hôm nay ngày ", " tại Cẩm Phả Quảng Ninh");
                break;

            default:
                return false;
        }

        return exportExcelMaster(tableView, columns, stage, selectDate, config);
    }

    /**
     * Hàm master gộp tất cả các xử lý xuất Excel logic cũ, tối ưu RAM và cực kỳ
     * sạch.
     */
    private boolean exportExcelMaster(
            TableView<ObservableList<String>> tableView,
            List<ExcelColumn> columns,
            Stage stage,
            LocalDate date,
            TemplateConfig config) {

        FileChooser fc = new FileChooser();
        fc.setTitle(config.dialogTitle);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName(config.defaultFileName);

        File file = fc.showSaveDialog(stage);
        if (file == null) {
            return false;
        }

        String templatePath = "C:\\PY\\fileExcel\\" + config.templateName;

        // Try-with-resources lồng nhau giải phóng RAM tức thì cho Stream và Workbook
        try (FileInputStream fis = new FileInputStream(templatePath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // ================= STYLE (Tạo một lần duy nhất) =================
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
            DataFormat format = workbook.createDataFormat();
            sttStyle.setDataFormat(format.getFormat("General"));

            // ================= UPDATE NGÀY THÁNG =================
            Row rowDate = sheet.getRow(config.dateRow);
            if (rowDate != null) {
                Cell cellDate = rowDate.getCell(config.dateCell);
                if (cellDate != null) {
                    String dateStr = config.datePrefix + date.getDayOfMonth()
                            + " tháng " + date.getMonthValue()
                            + " năm " + date.getYear()
                            + config.dateSuffix;
                    cellDate.setCellValue(dateStr);
                }
            }

            // Xử lý riêng cho trường hợp đặc biệt của KTKS
            if (config.hasCompanyNames) {
                Row row6 = sheet.getRow(6);
                if (row6 != null && row6.getCell(0) != null)
                    row6.getCell(0).setCellValue("ĐẠI DIỆN BÊN GIAO : CÔNG TY CỔ PHẦN VIỆT Ý QN");
                Row row9 = sheet.getRow(9);
                if (row9 != null && row9.getCell(0) != null)
                    row9.getCell(0).setCellValue("ĐẠI DIỆN BÊN NHẬN BÀN GIAO: CÔNG TY KHAI THÁC KHOÁNG SẢN");
            }

            // ================= ĐẨY CHỮ KÝ XUỐNG & GHI DỮ LIỆU =================
            int startRow = config.startRow;
            Row templateRow = sheet.getRow(startRow);
            int templateHeight = (templateRow != null) ? templateRow.getHeight() : 400;

            int numberOfRows = tableView.getItems().size();
            int lastRow = sheet.getLastRowNum();

            if (numberOfRows > 0 && startRow <= lastRow) {
                sheet.shiftRows(startRow, lastRow, numberOfRows);
            }

            // Ghi Data vào bảng
            for (int i = 0; i < numberOfRows; i++) {
                Row newRow = sheet.createRow(startRow + i);
                newRow.setHeight((short) templateHeight);

                ObservableList<String> rowData = tableView.getItems().get(i);

                for (int j = 0; j < columns.size(); j++) {
                    ExcelColumn mapCol = columns.get(j);
                    Cell newCell = newRow.createCell(j);

                    // Xử lý cột STT
                    if (mapCol.columnIndex == -1) {
                        newCell.setCellValue(i + 1);
                        newCell.setCellStyle(sttStyle);
                        continue;
                    }

                    // Điền data từ TableView
                    String value = "";
                    if (mapCol.columnIndex < rowData.size()) {
                        value = rowData.get(mapCol.columnIndex);
                    }

                    newCell.setCellValue(value != null ? value : "");
                    newCell.setCellStyle(borderStyle);
                }
            }

            // Ghi dữ liệu ra file đầu ra
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
// List<List<Object>> whereValuesList = new ArrayList<>();
// List<List<Object>> updateValuesList = new ArrayList<>();

// if (resultExport) {

// for (ObservableList<String> row : tableView.getItems()) {

// String cartAID = row.get(0);
// System.out.println(cartAID);

// // WHERE cho từng row
// whereValuesList.add(List.of(cartAID));

// // VALUE update cho từng row
// updateValuesList.add(List.of(selectDate.toString()));
// }

// try {
// db.updateBatch(
// "Cart",
// List.of("ReportDate"),
// updateValuesList,
// List.of("CartAID"),
// whereValuesList);

// } catch (SQLException e) {
// e.printStackTrace();
// }
// }