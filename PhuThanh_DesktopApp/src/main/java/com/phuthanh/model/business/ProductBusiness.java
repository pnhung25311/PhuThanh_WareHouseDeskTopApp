package com.phuthanh.model.business;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductBusiness {

    // ===== IDENTIFY =====
    @JsonProperty("Mã vật tư")
    public String maVatTu;

    @JsonProperty("Mã Keeton")
    public String maKeeton;

    @JsonProperty("Mã Công Nghiệp")
    public String maCongNghiep;

    @JsonProperty("Danh điểm")
    public String danhDiem;

    @JsonProperty("Bộ danh điểm tương đương")
    public String boDanhDiem;

    // ===== PRODUCT INFO =====
    @JsonProperty("Tên hàng hóa")
    public String tenHangHoa;

    @JsonProperty("Thông số kĩ thuật")
    public String thongSo;

    @JsonProperty("Hãng Xe")
    public String hangXe;

    @JsonProperty("Dòng xe")
    public String dongXe;

    @JsonProperty("Cụm xe")
    public String cumXe;

    @JsonProperty("Hãng Sản Xuất")
    public String hangSX;

    @JsonProperty("Nước Sản Xuất")
    public String nuocSX;

    // ⭐ NEW FIELDS
    @JsonProperty("Mục đích")
    public String mucDich;

    @JsonProperty("Mảng kinh doanh")
    public String mangKinhDoanh;

    // ===== SUPPLIER =====
    @JsonProperty("Nhà cung cấp hợp đồng")
    public String nccHopDong;

    @JsonProperty("Nhà cung cấp thực tế")
    public String nccThucTe;

    @JsonProperty("Đơn vị tính")
    public String donViTinh;

    // ===== IMAGES =====
    @JsonProperty("Hình ảnh 1")
    public String hinh1;

    @JsonProperty("Hình ảnh 2")
    public String hinh2;

    @JsonProperty("Hình ảnh 3")
    public String hinh3;

    @JsonProperty("Ghi chú")
    public String ghiChu;

    // ===== PRICE =====
    @JsonProperty("Giá vốn 1")
    public String giaVon1;

    @JsonProperty("Giá vốn 2")
    public String giaVon2;

    @JsonProperty("VAT Việt Ý")
    public String vatVietY;

    @JsonProperty("VAT Phú Thành")
    public String vatPhuThanh;

    @JsonProperty("Ghi chu VAT")
    public String ghiChuVAT;

    @JsonProperty("Tên Việt Ý VAT")
    public String tenVietYVAT;

    @JsonProperty("Tên Phú Thành VAT")
    public String tenPhuThanhVAT;

    // ===== WAREHOUSE =====
    @JsonProperty("Kho chính")
    public String khoChinh;

    @JsonProperty("Kho 397")
    public String kho397;

    @JsonProperty("Kho Khe Dây")
    public String khoKheDay;

    @JsonProperty("Kho Khoáng Sản")
    public String khoKhoangSan;

    @JsonProperty("Kho Làng Khánh")
    public String khoLangKhanh;

    @JsonProperty("HKD Hoàng Văn Dũng")
    public String khoDung;

    @JsonProperty("HKD Lê Văn Thiện")
    public String khoThien;

    // ===== CERT =====
    @JsonProperty("CoCq Việt Ý")
    public String cocqVietY;

    @JsonProperty("CoCq Phú Thành")
    public String cocqPhuThanh;

    // ===== BUSINESS =====
    @JsonProperty("Số lượng dự kiến")
    public String soLuongDuKien;

    @JsonProperty("Mã số hóa đơn")
    public String maHoaDon;

    @JsonProperty("Vị trí")
    public String viTri;

    @JsonProperty("Tổng số lượng bán ra")
    public String tongBan;

    @JsonProperty("Số lượng bán ra gần nhất")
    public String banGanNhat;

    @JsonProperty("Thời gian bán ra gần nhất")
    public String ngayBanGanNhat;

    public ProductBusiness() {}

    // ===== COPY CONSTRUCTOR =====
    public ProductBusiness(ProductBusiness o) {
        if (o == null) return;

        this.maVatTu = o.maVatTu;
        this.maKeeton = o.maKeeton;
        this.maCongNghiep = o.maCongNghiep;
        this.danhDiem = o.danhDiem;
        this.boDanhDiem = o.boDanhDiem;
        this.tenHangHoa = o.tenHangHoa;
        this.thongSo = o.thongSo;
        this.hangXe = o.hangXe;
        this.dongXe = o.dongXe;
        this.cumXe = o.cumXe;
        this.hangSX = o.hangSX;
        this.nuocSX = o.nuocSX;
        this.mucDich = o.mucDich;
        this.mangKinhDoanh = o.mangKinhDoanh;
        this.nccHopDong = o.nccHopDong;
        this.nccThucTe = o.nccThucTe;
        this.donViTinh = o.donViTinh;
        this.hinh1 = o.hinh1;
        this.hinh2 = o.hinh2;
        this.hinh3 = o.hinh3;
        this.ghiChu = o.ghiChu;
        this.giaVon1 = o.giaVon1;
        this.giaVon2 = o.giaVon2;
        this.vatVietY = o.vatVietY;
        this.vatPhuThanh = o.vatPhuThanh;
        this.ghiChuVAT = o.ghiChuVAT;
        this.tenVietYVAT = o.tenVietYVAT;
        this.tenPhuThanhVAT = o.tenPhuThanhVAT;
        this.khoChinh = o.khoChinh;
        this.kho397 = o.kho397;
        this.khoKheDay = o.khoKheDay;
        this.khoKhoangSan = o.khoKhoangSan;
        this.khoLangKhanh = o.khoLangKhanh;
        this.khoDung = o.khoDung;
        this.khoThien = o.khoThien;
        this.cocqVietY = o.cocqVietY;
        this.cocqPhuThanh = o.cocqPhuThanh;
        this.soLuongDuKien = o.soLuongDuKien;
        this.maHoaDon = o.maHoaDon;
        this.viTri = o.viTri;
        this.tongBan = o.tongBan;
        this.banGanNhat = o.banGanNhat;
        this.ngayBanGanNhat = o.ngayBanGanNhat;
    }

    public ProductBusiness copy() {
        return new ProductBusiness(this);
    }
}