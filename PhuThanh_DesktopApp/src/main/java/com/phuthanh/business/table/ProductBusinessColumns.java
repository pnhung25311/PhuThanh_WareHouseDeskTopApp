package com.phuthanh.business.table;

import java.util.List;
import com.phuthanh.business.service.StockComputeService;

public class ProductBusinessColumns {

    private static final StockComputeService stockService = new StockComputeService();

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static int toInt(String s) {
        if (s == null || s.isBlank())
            return 0;

        try {
            s = s.trim().replace(",", ""); // chỉ bỏ dấu phân cách

            // parse đúng số thập phân
            double d = Double.parseDouble(s);

            return (int) Math.round(d); // làm tròn đúng
        } catch (Exception e) {
            return 0;
        }
    }

    public static List<ColumnConfig> getColumns() {

        return List.of(

                // ===== IDENTIFY =====
                new ColumnConfig("Mã vật tư", p -> nvl(p.maVatTu), false),
                new ColumnConfig("Danh điểm", p -> nvl(p.danhDiem), false),
                new ColumnConfig("Bộ danh điểm tương đương", p -> nvl(p.boDanhDiem), false),
                new ColumnConfig("Tên hàng hóa", p -> nvl(p.tenHangHoa), false),
                new ColumnConfig("Tên Việt Ý VAT", p -> nvl(p.tenVietYVAT), false),
                new ColumnConfig("Tên Phú Thành VAT", p -> nvl(p.tenPhuThanhVAT), false),
                new ColumnConfig("Hãng SX", p -> nvl(p.hangSX), false),
                new ColumnConfig("Nước SX", p -> nvl(p.nuocSX), false),
                new ColumnConfig("Nhà cung cấp hợp đồng", p -> nvl(p.nccHopDong), false),
                new ColumnConfig("Nhà cung cấp thực tế", p -> nvl(p.nccThucTe), false),
                new ColumnConfig("ĐVT", p -> nvl(p.donViTinh), false),

                // ===== SỐ LƯỢNG =====
                new ColumnConfig("Số lượng dự kiến", p -> nvl(p.soLuongDuKien), true),

                // ===== KHO =====
                new ColumnConfig("Kho chính", p -> nvl(p.khoChinh), true),
                new ColumnConfig("Kho Khoáng Sản", p -> nvl(p.khoKhoangSan), true),
                new ColumnConfig("Kho 397", p -> nvl(p.kho397), true),
                new ColumnConfig("Kho Làng Khánh", p -> nvl(p.khoLangKhanh), true),
                new ColumnConfig("Kho Khe Dây", p -> nvl(p.khoKheDay), true),

                new ColumnConfig("Tổng số lượng tồn", p -> String.valueOf(
                        toInt(p.khoChinh) + toInt(p.khoKhoangSan) + toInt(p.kho397) + toInt(p.khoLangKhanh) +
                                toInt(p.khoKheDay)),
                        true),

                // ===== VAT PHÚ THÀNH =====
                new ColumnConfig("VAT PT K1", p -> {
                    var r = stockService.compute(p).get("VAT Phú Thành");
                    return r == null ? "0" : String.valueOf(r.co);
                }, true),

                new ColumnConfig("VAT PT K0", p -> {
                    var r = stockService.compute(p).get("VAT Phú Thành");
                    return r == null ? "0" : String.valueOf(r.ko);
                }, true),

                new ColumnConfig("Co/Cq Phú Thành", p -> nvl(p.cocqPhuThanh), false),

                // ===== VAT VIỆT Ý =====
                new ColumnConfig("VAT VY K1", p -> {
                    var r = stockService.compute(p).get("VAT Việt Ý");
                    return r == null ? "0" : String.valueOf(r.co);
                }, true),

                new ColumnConfig("VAT VY K0", p -> {
                    var r = stockService.compute(p).get("VAT Việt Ý");
                    return r == null ? "0" : String.valueOf(r.ko);
                }, true),

                new ColumnConfig("Co/Cq Việt Ý", p -> nvl(p.cocqVietY), false),

                new ColumnConfig("Ghi chú VAT", p -> nvl(p.ghiChuVAT), false),

                // ===== HKD =====
                new ColumnConfig("HKD Dũng Fast", p -> nvl(p.khoDung), true),
                new ColumnConfig("HKD Dũng K2", p -> "", true),
                new ColumnConfig("HKD Dũng K3", p -> "", true),

                new ColumnConfig("HKD Thiện Fast", p -> nvl(p.khoThien), true),
                new ColumnConfig("HKD Thiện K2", p -> "", true),
                new ColumnConfig("HKD Thiện K4", p -> "", true),

                // ===== GIÁ =====
                new ColumnConfig("Giá vốn 1", p -> nvl(p.giaVon1), true),
                new ColumnConfig("Giá vốn 2", p -> nvl(p.giaVon2), true),

                // ===== XE =====
                new ColumnConfig("Hãng xe", p -> nvl(p.hangXe), false),
                new ColumnConfig("Dòng xe", p -> nvl(p.dongXe), false),

                // ===== KHÁC =====
                new ColumnConfig("Thông số kĩ thuật", p -> nvl(p.thongSo), false),
                new ColumnConfig("Vị trí", p -> nvl(p.viTri), false),
                new ColumnConfig("Mã số hóa đơn", p -> nvl(p.maHoaDon), false),
                new ColumnConfig("Mảng kinh doanh", p -> "", false),
                new ColumnConfig("Mục đích", p -> "", false),
                new ColumnConfig("Ghi chú", p -> nvl(p.ghiChu), false),

                // ===== HÌNH =====
                new ColumnConfig("Hình ảnh 1", p -> nvl(p.hinh1), false),
                new ColumnConfig("Hình ảnh 2", p -> nvl(p.hinh2), false),
                new ColumnConfig("Hình ảnh 3", p -> nvl(p.hinh3), false),

                // ===== BÁN =====
                new ColumnConfig("Tổng số lượng bán ra", p -> nvl(p.tongBan), true)

        );
    }
}