package com.example.petcareapp.data.model;

public class KhoVoucher {
    private String id;
    private String idVoucher;
    private String idUser;
    private String trangThai;

    public KhoVoucher() {
    }

    public KhoVoucher(String id, String idVoucher, String idUser, String trangThai) {
        this.id = id;
        this.idVoucher = idVoucher;
        this.idUser = idUser;
        this.trangThai = trangThai;
    }

    public String getId() {
        return id;
    }

    public String getIdVoucher() {
        return idVoucher;
    }

    public String getIdUser() {
        return idUser;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIdVoucher(String idVoucher) {
        this.idVoucher = idVoucher;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}