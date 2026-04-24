package com.example.petcareapp.data.model;

public class DichVu {
    private String ten;
    private int gia;

    public DichVu() {}

    public DichVu(String ten, int gia) {
        this.ten = ten;
        this.gia = gia;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public int getGia() {
        return gia;
    }

    public void setGia(int gia) {
        this.gia = gia;
    }
}