package com.phuthanh.business.table;

import java.util.List;
import com.phuthanh.business.service.StockComputeService;

public class ProductBusinessColumns {

    private final StockComputeService stockService = new StockComputeService();

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private int toInt(String s) {
        if (s == null || s.isBlank())
            return 0;

        try {
            s = s.trim().replace(",", ""); // chỉ bỏ dấu phân cách

            // parse đúng số thập phân
            double d = Double.parseDouble(s);
            if (d < 0) {
                return 0;
            }

            return (int) Math.round(d); // làm tròn đúng
        } catch (Exception e) {
            return 0;
        }
    }

    public List<ColumnConfig> getColumns() {
        

        return List.of(

                // ===== IDENTIFY =====
                new ColumnConfig("Mã vật tư", p -> nvl(p.maVatTu), false, 150),
                new ColumnConfig("Danh điểm", p -> nvl(p.danhDiem), false, 150),
                new ColumnConfig("Bộ danh điểm tương đương", p -> nvl(p.boDanhDiem), false, 250),
                new ColumnConfig("Tên hàng hóa", p -> nvl(p.tenHangHoa), false, 250),
                new ColumnConfig("Tên Việt Ý VAT", p -> nvl(p.tenVietYVAT), false, 150),
                new ColumnConfig("Tên Phú Thành VAT", p -> nvl(p.tenPhuThanhVAT), false, 150),
                new ColumnConfig("Hãng SX", p -> nvl(p.hangSX), false, 150),
                new ColumnConfig("Nước SX", p -> nvl(p.nuocSX), false, 150),
                new ColumnConfig("Nhà cung cấp hợp đồng", p -> nvl(p.nccHopDong), false, 150),
                new ColumnConfig("Nhà cung cấp thực tế", p -> nvl(p.nccThucTe), false, 150),
                new ColumnConfig("ĐVT", p -> nvl(p.donViTinh), false, 150),

                // ===== SỐ LƯỢNG =====
                new ColumnConfig("Số lượng dự kiến", p -> nvl(p.soLuongDuKien), true, 100),

                // ===== KHO =====
                new ColumnConfig("Kho chính", p -> nvl(p.khoChinh), true, 100),
                new ColumnConfig("Kho Khoáng Sản", p -> nvl(p.khoKhoangSan), true, 100),
                new ColumnConfig("Kho 397", p -> nvl(p.kho397), true, 100),
                new ColumnConfig("Kho Làng Khánh", p -> nvl(p.khoLangKhanh), true, 100),
                new ColumnConfig("Kho Khe Dây", p -> nvl(p.khoKheDay), true, 100),

                new ColumnConfig("Tổng số lượng tồn", p -> String.valueOf(
                        toInt(p.khoChinh) + toInt(p.khoKhoangSan) + toInt(p.kho397) + toInt(p.khoLangKhanh) +
                                toInt(p.khoKheDay)),
                        true, 100),

                // ===== VAT PHÚ THÀNH =====
                new ColumnConfig("VAT PT K1\n(Có VAT có hàng)", p -> {
                    var r = stockService.compute(p).get("VAT Phú Thành");
                    return r == null ? "0" : String.valueOf(r.co);
                }, true, 200),

                new ColumnConfig("VAT PT K0\n(Có VAT không hàng)", p -> {
                    var r = stockService.compute(p).get("VAT Phú Thành");
                    return r == null ? "0" : String.valueOf(r.ko);
                }, true, 200),

                // ===== VAT VIỆT Ý =====
                new ColumnConfig("VAT VY K1\n(Có VAT có hàng)", p -> {
                    var r = stockService.compute(p).get("VAT Việt Ý");
                    return r == null ? "0" : String.valueOf(r.co);
                }, true, 200),

                new ColumnConfig("VAT VY K0\n(Có VAT không hàng)", p -> {
                    var r = stockService.compute(p).get("VAT Việt Ý");
                    return r == null ? "0" : String.valueOf(r.ko);
                }, true, 200),

                // new ColumnConfig("VAT Phú Thành", p -> nvl(p.vatPhuThanh), true),
                // new ColumnConfig("VAT Việt Ý", p -> nvl(p.vatVietY), true, 100),
                new ColumnConfig("Co/Cq Phú Thành", p -> nvl(p.cocqPhuThanh), false, 100),

                new ColumnConfig("Co/Cq Việt Ý", p -> nvl(p.cocqVietY), false, 100),

                new ColumnConfig("Ghi chú VAT", p -> nvl(p.ghiChuVAT), false, 100),

                // ===== HKD =====
                new ColumnConfig("HKD Dũng Fast", p -> nvl(p.khoDung), true, 100),
                new ColumnConfig("HKD Dũng K2", p -> "", true, 100),
                new ColumnConfig("HKD Dũng K3", p -> "", true, 100),

                new ColumnConfig("HKD Thiện Fast", p -> nvl(p.khoThien), true, 100),
                new ColumnConfig("HKD Thiện K2", p -> "", true, 100),
                new ColumnConfig("HKD Thiện K4", p -> "", true, 100),

                // ===== GIÁ =====
                new ColumnConfig("Giá vốn 1", p -> nvl(p.giaVon1), true, 100),
                new ColumnConfig("Giá vốn 2", p -> nvl(p.giaVon2), true, 100),

                // ===== XE =====
                new ColumnConfig("Hãng xe", p -> nvl(p.hangXe), false, 100),
                new ColumnConfig("Dòng xe", p -> nvl(p.dongXe), false, 150),

                // ===== KHÁC =====
                new ColumnConfig("Thông số kĩ thuật", p -> nvl(p.thongSo), false, 100),
                new ColumnConfig("Vị trí", p -> nvl(p.viTri), false, 100),
                new ColumnConfig("Mã số hóa đơn", p -> nvl(p.maHoaDon), false, 100),
                new ColumnConfig("Mảng kinh doanh", p -> nvl(p.mangKinhDoanh), false, 100),
                new ColumnConfig("Mục đích", p -> nvl(p.mucDich), false, 100),
                new ColumnConfig("Ghi chú", p -> nvl(p.ghiChu), false, 100),

                // ===== HÌNH =====
                new ColumnConfig("Hình ảnh 1", p -> nvl(p.hinh1), false, 250),
                new ColumnConfig("Hình ảnh 2", p -> nvl(p.hinh2), false, 250),
                new ColumnConfig("Hình ảnh 3", p -> nvl(p.hinh3), false, 250),

                // ===== BÁN =====
                new ColumnConfig("Tổng số lượng bán ra", p -> nvl(p.tongBan), true, 100)

        );
    }
}