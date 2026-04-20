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
}


