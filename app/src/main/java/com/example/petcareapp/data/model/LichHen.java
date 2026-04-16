package com.example.petcareapp.data.model;


import com.google.firebase.Timestamp;

public class LichHen {
    private String id;
    private String userId;
    private String petId;
    private String chiNhanhId;
    private Timestamp thoiGianHen; // Dùng Timestamp giống biến dob của Pet
    private String lyDo;
    private String trangThai;
    private String tenThuCung;
    private String tenChiNhanh;

    // 1. Constructor rỗng (Bắt buộc cho Firebase)
    public LichHen() {}

    // 2. Constructor đầy đủ
    public LichHen(String id, String userId, String petId, String chiNhanhId,
                   Timestamp thoiGianHen, String lyDo, String trangThai,
                   String tenThuCung, String tenChiNhanh) {
        this.id = id;
        this.userId = userId;
        this.petId = petId;
        this.chiNhanhId = chiNhanhId;
        this.thoiGianHen = thoiGianHen;
        this.lyDo = lyDo;
        this.trangThai = trangThai;
        this.tenThuCung = tenThuCung;
        this.tenChiNhanh = tenChiNhanh;
    }

    // 3. Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }

    public String getChiNhanhId() { return chiNhanhId; }
    public void setChiNhanhId(String chiNhanhId) { this.chiNhanhId = chiNhanhId; }

    public Timestamp getThoiGianHen() { return thoiGianHen; }
    public void setThoiGianHen(Timestamp thoiGianHen) { this.thoiGianHen = thoiGianHen; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getTenThuCung() { return tenThuCung; }
    public void setTenThuCung(String tenThuCung) { this.tenThuCung = tenThuCung; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }
}