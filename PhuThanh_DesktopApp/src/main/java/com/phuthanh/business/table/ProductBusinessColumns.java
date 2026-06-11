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
                new ColumnConfig("maVatTu", "Mã vật tư", p -> nvl(p.maVatTu), false, 150),
                new ColumnConfig("danhDiem", "Danh điểm", p -> nvl(p.danhDiem), false, 150),
                new ColumnConfig("boDanhDiem", "Bộ danh điểm tương đương", p -> nvl(p.boDanhDiem), false, 250),
                new ColumnConfig("tenHangHoa", "Tên hàng hóa", p -> nvl(p.tenHangHoa), false, 250),
                new ColumnConfig("tenVietYVAT", "Tên Việt Ý VAT", p -> nvl(p.tenVietYVAT), false, 150),
                new ColumnConfig("tenPhuThanhVAT", "Tên Phú Thành VAT", p -> nvl(p.tenPhuThanhVAT), false, 150),
                new ColumnConfig("hangSX", "Hãng sản xuất", p -> nvl(p.hangSX), false, 150),
                new ColumnConfig("nuocSX", "Nước sản xuất", p -> nvl(p.nuocSX), false, 150),
                new ColumnConfig("nccHopDong", "Nhà cung cấp hợp đồng", p -> nvl(p.nccHopDong), false, 150),
                new ColumnConfig("nccThucTe", "Nhà cung cấp thực tế", p -> nvl(p.nccThucTe), false, 150),
                new ColumnConfig("donViTinh", "Đơn vị tính", p -> nvl(p.donViTinh), false, 150),

                // ===== SỐ LƯỢNG =====
                new ColumnConfig("soLuongDuKien", "Số lượng dự kiến", p -> nvl(p.soLuongDuKien), true, 100),

                // ===== KHO =====
                new ColumnConfig("khoChinh", "Kho chính", p -> nvl(p.khoChinh), true, 100),
                new ColumnConfig("khoKhoangSan", "Kho Khoáng Sản", p -> nvl(p.khoKhoangSan), true, 100),
                new ColumnConfig("kho397", "Kho 397", p -> nvl(p.kho397), true, 100),
                new ColumnConfig("khoLangKhanh", "Kho Làng Khánh", p -> nvl(p.khoLangKhanh), true, 100),
                new ColumnConfig("khoKheDay", "Kho Khe Dây", p -> nvl(p.khoKheDay), true, 100),

                new ColumnConfig("tongSoLuongTon", "Tổng số lượng tồn", p -> String.valueOf(
                        toInt(p.khoChinh) + toInt(p.khoKhoangSan) + toInt(p.kho397) + toInt(p.khoLangKhanh) +
                                toInt(p.khoKheDay)),
                        true, 100),

                // ===== VAT PHÚ THÀNH =====
                new ColumnConfig("ptK1","VAT PT K1\n(Có VAT có hàng)", p -> {
                    var r = stockService.compute(p).get("VAT Phú Thành");
                    return r == null ? "0" : String.valueOf(r.co);
                }, true, 200),

                new ColumnConfig("ptK0","VAT PT K0\n(Có VAT không hàng)", p -> {
                    var r = stockService.compute(p).get("VAT Phú Thành");
                    return r == null ? "0" : String.valueOf(r.ko);
                }, true, 200),

                // ===== VAT VIỆT Ý =====
                new ColumnConfig("vyK1", "VAT VY K1\n(Có VAT có hàng)", p -> {
                    var r = stockService.compute(p).get("VAT Việt Ý");
                    return r == null ? "0" : String.valueOf(r.co);
                }, true, 200),

                new ColumnConfig("vyK0", "VAT VY K0\n(Có VAT không hàng)", p -> {
                    var r = stockService.compute(p).get("VAT Việt Ý");
                    return r == null ? "0" : String.valueOf(r.ko);
                }, true, 200),

                // new ColumnConfig("VAT Phú Thành", p -> nvl(p.vatPhuThanh), true),
                // new ColumnConfig("VAT Việt Ý", p -> nvl(p.vatVietY), true, 100),
                new ColumnConfig("cocqPhuThanh", "Co/Cq Phú Thành", p -> nvl(p.cocqPhuThanh), false, 100),

                new ColumnConfig("cocqVietY", "Co/Cq Việt Ý", p -> nvl(p.cocqVietY), false, 100),

                new ColumnConfig("ghiChuVAT", "Ghi chú VAT", p -> nvl(p.ghiChuVAT), false, 100),

                // ===== HKD =====
                new ColumnConfig("hkdDungFast", "HKD Dũng Fast", p -> nvl(p.khoDung), true, 100),
                new ColumnConfig("hkdDungK2", "HKD Dũng K2", p -> "", true, 100),
                new ColumnConfig("hkdDungK3", "HKD Dũng K3", p -> "", true, 100),

                new ColumnConfig("hkdThienFast", "HKD Thiện Fast", p -> nvl(p.khoThien), true, 100),
                new ColumnConfig("hkdThienK2", "HKD Thiện K2", p -> "", true, 100),
                new ColumnConfig("hkdThienK4", "HKD Thiện K4", p -> "", true, 100),

                // ===== GIÁ =====
                new ColumnConfig("giaVon1", "Giá vốn 1", p -> nvl(p.giaVon1), true, 100),
                new ColumnConfig("giaVon2", "Giá vốn 2", p -> nvl(p.giaVon2), true, 100),

                // ===== XE =====
                new ColumnConfig("hangXe", "Hãng xe", p -> nvl(p.hangXe), false, 100),
                new ColumnConfig("dongXe", "Dòng xe", p -> nvl(p.dongXe), false, 150),

                // ===== KHÁC =====
                new ColumnConfig("thongSo", "Thông số kĩ thuật", p -> nvl(p.thongSo), false, 100),
                new ColumnConfig("viTri", "Vị trí", p -> nvl(p.viTri), false, 100),
                new ColumnConfig("maHoaDon", "Mã số hóa đơn", p -> nvl(p.maHoaDon), false, 100),
                new ColumnConfig("mangKinhDoanh", "Mảng kinh doanh", p -> nvl(p.mangKinhDoanh), false, 100),
                new ColumnConfig("mucDich", "Mục đích", p -> nvl(p.mucDich), false, 100),
                new ColumnConfig("ghiChu", "Ghi chú", p -> nvl(p.ghiChu), false, 100),

                // ===== HÌNH =====
                new ColumnConfig("hinh1", "Hình ảnh 1", p -> nvl(p.hinh1), false, 250),
                new ColumnConfig("hinh2", "Hình ảnh 2", p -> nvl(p.hinh2), false, 250),
                new ColumnConfig("hinh3", "Hình ảnh 3", p -> nvl(p.hinh3), false, 250),

                // ===== BÁN =====
                new ColumnConfig("tongBan", "Tổng số lượng bán ra", p -> nvl(p.tongBan), true, 100)

        );
    }
}