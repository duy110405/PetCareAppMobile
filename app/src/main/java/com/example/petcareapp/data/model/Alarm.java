package com.example.petcareapp.data.model;

public class Alarm {
    public String id;
    public String name;
    public String description;
    public String time; // "19:00"

    public String type;

    public Alarm() {}

    public Alarm(String id, String name, String time, String type, String description) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.type = type;
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getTime() { return time; }
    public String getType() { return type; }
    public String getDescription() { return description; }
}
