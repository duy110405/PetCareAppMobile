package com.example.petcareapp.data.model;

public class ChiNhanh {
    private String id; // ID tự tạo của Firebase
    private String tenChiNhanh;
    private String diaChi;
    private String soDienThoai;
    private String gioLamViec;
    private double viDo;
    private double kinhDo;

    // Firebase BẮT BUỘC phải có constructor rỗng
    public ChiNhanh() {
    }

    public ChiNhanh(String tenChiNhanh, String diaChi, String soDienThoai, String gioLamViec, double viDo, double kinhDo) {
        this.tenChiNhanh = tenChiNhanh;
        this.diaChi = diaChi;
        this.soDienThoai = soDienThoai;
        this.gioLamViec = gioLamViec;
        this.viDo = viDo;
        this.kinhDo = kinhDo;
    }

    // --- CÁC HÀM GETTER / SETTER ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getGioLamViec() { return gioLamViec; }
    public void setGioLamViec(String gioLamViec) { this.gioLamViec = gioLamViec; }

    public double getViDo() { return viDo; }
    public void setViDo(double viDo) { this.viDo = viDo; }

    public double getKinhDo() { return kinhDo; }
    public void setKinhDo(double kinhDo) { this.kinhDo = kinhDo; }
}