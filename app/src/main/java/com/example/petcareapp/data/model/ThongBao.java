package com.example.petcareapp.data.model;

import com.google.firebase.Timestamp;

public class ThongBao {

    private String id;
    private String userId;
    private String noiDung;
    private Timestamp thoiGian;
    private boolean daDoc;

    public ThongBao() {
    }

    public ThongBao(String userId, String noiDung) {
        this.userId = userId;
        this.noiDung = noiDung;
        this.thoiGian = Timestamp.now();
        this.daDoc = false;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public Timestamp getThoiGian() {
        return thoiGian;
    }

    public boolean isDaDoc() {
        return daDoc;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDaDoc(boolean daDoc) {
        this.daDoc = daDoc;
    }
}