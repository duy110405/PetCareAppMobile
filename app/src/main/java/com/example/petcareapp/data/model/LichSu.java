package com.example.petcareapp.data.model;

public class LichSu {
    private String maUser;
    private String emailUser;
    private String tenManHinh;
    private long thoiGian;

    public LichSu() {}

    public LichSu(String maUser, String emailUser, String tenManHinh, long thoiGian) {
        this.maUser = maUser;
        this.emailUser = emailUser;
        this.tenManHinh = tenManHinh;
        this.thoiGian = thoiGian;
    }


    public String getMaUser() { return maUser; }
    public void setMaUser(String maUser) { this.maUser = maUser; }
    public String getEmailUser() { return emailUser; }
    public void setEmailUser(String emailUser) { this.emailUser = emailUser; }
    public String getTenManHinh() { return tenManHinh; }
    public void setTenManHinh(String tenManHinh) { this.tenManHinh = tenManHinh; }
    public long getThoiGian() { return thoiGian; }
    public void setThoiGian(long thoiGian) { this.thoiGian = thoiGian; }
}