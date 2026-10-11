package com.example.myapplication;

import java.io.Serializable;

public class EventModel implements Serializable {
    private String name;
    private String place;
    private String date;
    private String time;
    private boolean isEnabled;

    public EventModel(String name, String place, String date, String time, boolean isEnabled) {
        this.name = name;
        this.place = place;
        this.date = date;
        this.time = time;
        this.isEnabled = isEnabled;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
}