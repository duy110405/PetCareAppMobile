package com.example.petcareapp.data.model;

import com.google.firebase.Timestamp;


public class Pet {
    private String id;
    private String name;
    private String breed;
    private Timestamp dob; // 🔥 lưu ngày sinh;
    private double weight;
    private String color;
    private String status;
    private String imageUrl;
    private int reminderCount;

    public Pet() {}

    public Pet(String id, String name, String breed, Timestamp dob,
               double weight, String color, String status,
               String imageUrl, int reminderCount) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.dob = dob;
        this.weight = weight;
        this.color = color;
        this.status = status;
        this.imageUrl = imageUrl;
        this.reminderCount = reminderCount;
    }


    public String getId() { return id; }
    public String getName() { return name; }
    public String getBreed() { return breed; }
    public Timestamp getDob() {return dob;}
    public double getWeight() { return weight; }
    public String getColor() { return color; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public int getReminderCount() { return reminderCount; }
}
