package com.example.petcareapp.data.model;

import java.util.List;


public class User {
    private String id;
    private String username;
    private String email;
    private String phone;
    private boolean locked;
    private int petCount;
    private String role;
    private int tongDiem;

    public User() {}

    public User(String id, String username, String email, String phone, boolean locked, int petCount, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.locked = locked;
        this.petCount = petCount;
        this.role = role;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isLocked() { return locked; }
    public int getPetCount() { return petCount; }
    public String getRole() { return role; }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    public void setId(String id) { this.id = id; }

    public int getTongDiem() {
        return tongDiem;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPetCount(int petCount) {
        this.petCount = petCount;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setTongDiem(int tongDiem) {
        this.tongDiem = tongDiem;
    }
}


