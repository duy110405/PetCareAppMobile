package com.example.petcareapp.data.model;

import com.google.firebase.Timestamp;
import java.util.List;

public class LichHen {

    private String id;
    private String userId;
    private String petId;
    private String chiNhanhId;
    private Timestamp thoiGianHen;

    private List<DichVu> danhSachDichVu;
    private int tongTien;

    private String ghiChu;
    private String lyDoTuChoi;

    private String trangThai;
    private String tenThuCung;
    private String tenChiNhanh;

    public LichHen() {
    }

    public LichHen(
            String id,
            String userId,
            String petId,
            String chiNhanhId,
            Timestamp thoiGianHen,
            List<DichVu> danhSachDichVu,
            int tongTien,
            String ghiChu,
            String lyDoTuChoi,
            String trangThai,
            String tenThuCung,
            String tenChiNhanh
    ) {
        this.id = id;
        this.userId = userId;
        this.petId = petId;
        this.chiNhanhId = chiNhanhId;
        this.thoiGianHen = thoiGianHen;
        this.danhSachDichVu = danhSachDichVu;
        this.tongTien = tongTien;
        this.ghiChu = ghiChu;
        this.lyDoTuChoi = lyDoTuChoi;
        this.trangThai = trangThai;
        this.tenThuCung = tenThuCung;
        this.tenChiNhanh = tenChiNhanh;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPetId() {
        return petId;
    }

    public String getChiNhanhId() {
        return chiNhanhId;
    }

    public Timestamp getThoiGianHen() {
        return thoiGianHen;
    }

    public List<DichVu> getDanhSachDichVu() {
        return danhSachDichVu;
    }

    public int getTongTien() {
        return tongTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public String getLyDoTuChoi() {
        return lyDoTuChoi;
    }

    public void setLyDoTuChoi(String lyDoTuChoi) {
        this.lyDoTuChoi = lyDoTuChoi;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public void setChiNhanhId(String chiNhanhId) {
        this.chiNhanhId = chiNhanhId;
    }

    public void setThoiGianHen(Timestamp thoiGianHen) {
        this.thoiGianHen = thoiGianHen;
    }

    public void setDanhSachDichVu(List<DichVu> danhSachDichVu) {
        this.danhSachDichVu = danhSachDichVu;
    }

    public void setTongTien(int tongTien) {
        this.tongTien = tongTien;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}