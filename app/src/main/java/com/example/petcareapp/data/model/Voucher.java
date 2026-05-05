package com.example.petcareapp.data.model;

public class Voucher {
    private String idVoucher;
    private String tenVoucher;
    private int diemYeuCau;
    private int soTienGiam;
    public Voucher(){}

    public Voucher(String idVoucher, String tenVoucher, int diemYeuCau, int soTienGiam) {
        this.idVoucher = idVoucher;
        this.tenVoucher = tenVoucher;
        this.diemYeuCau = diemYeuCau;
        this.soTienGiam = soTienGiam;
    }

    public String getIdVoucher() {
        return idVoucher;
    }

    public String getTenVoucher() {
        return tenVoucher;
    }

    public int getDiemYeuCau() {
        return diemYeuCau;
    }

    public int getSoTienGiam() {
        return soTienGiam;
    }

    public void setIdVoucher(String idVoucher) {
        this.idVoucher = idVoucher;
    }

    public void setTenVoucher(String tenVoucher) {
        this.tenVoucher = tenVoucher;
    }

    public void setDiemYeuCau(int diemYeuCau) {
        this.diemYeuCau = diemYeuCau;
    }

    public void setSoTienGiam(int soTienGiam) {
        this.soTienGiam = soTienGiam;
    }
}

