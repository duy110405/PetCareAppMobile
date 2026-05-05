package com.example.petcareapp.data.model;
import com.google.firebase.Timestamp;
import java.util.Map;

public class PhieuKham {
    private String lichHenId;
    private String tenBacSi;
    private Timestamp thoiGianKham;

    // Bảng ChiSoSinhTon
    private double thanNhiet;
    private int nhipTim;
    private String huyetAp;
    private int nhipTho;

    // Bảng KhamLamSang (Rút gọn các trường chính, bạn có thể thêm các trường khác tương tự)
    private String tinhTrangTongQuat;
    private String trieuChungChinh;
    private String theTrang;

    // Kết luận & Điều trị
    private String ketLuan;
    private String huongDieuTri;
    private String ngayTaiKham;

    // Bảng Đơn Thuốc (Sử dụng String đa dòng hoặc List<Map> tùy độ phức tạp)
    private String donThuoc;

    public PhieuKham() {
        // Constructor rỗng bắt buộc cho Firestore
    }

    // --- GETTER & SETTER ---
    public String getLichHenId() { return lichHenId; }
    public void setLichHenId(String lichHenId) { this.lichHenId = lichHenId; }

    public String getTenBacSi() { return tenBacSi; }
    public void setTenBacSi(String tenBacSi) { this.tenBacSi = tenBacSi; }

    public Timestamp getThoiGianKham() { return thoiGianKham; }
    public void setThoiGianKham(Timestamp thoiGianKham) { this.thoiGianKham = thoiGianKham; }

    public double getThanNhiet() { return thanNhiet; }
    public void setThanNhiet(double thanNhiet) { this.thanNhiet = thanNhiet; }

    public int getNhipTim() { return nhipTim; }
    public void setNhipTim(int nhipTim) { this.nhipTim = nhipTim; }

    public String getHuyetAp() { return huyetAp; }
    public void setHuyetAp(String huyetAp) { this.huyetAp = huyetAp; }

    public int getNhipTho() { return nhipTho; }
    public void setNhipTho(int nhipTho) { this.nhipTho = nhipTho; }

    public String getTinhTrangTongQuat() { return tinhTrangTongQuat; }
    public void setTinhTrangTongQuat(String tinhTrangTongQuat) { this.tinhTrangTongQuat = tinhTrangTongQuat; }

    public String getTrieuChungChinh() { return trieuChungChinh; }
    public void setTrieuChungChinh(String trieuChungChinh) { this.trieuChungChinh = trieuChungChinh; }

    public String getTheTrang() { return theTrang; }
    public void setTheTrang(String theTrang) { this.theTrang = theTrang; }

    public String getKetLuan() { return ketLuan; }
    public void setKetLuan(String ketLuan) { this.ketLuan = ketLuan; }

    public String getHuongDieuTri() { return huongDieuTri; }
    public void setHuongDieuTri(String huongDieuTri) { this.huongDieuTri = huongDieuTri; }

    public String getNgayTaiKham() { return ngayTaiKham; }
    public void setNgayTaiKham(String ngayTaiKham) { this.ngayTaiKham = ngayTaiKham; }

    public String getDonThuoc() { return donThuoc; }
    public void setDonThuoc(String donThuoc) { this.donThuoc = donThuoc; }
}
