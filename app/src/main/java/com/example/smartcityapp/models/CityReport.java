package com.example.smartcityapp.models;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.Timestamp;

public class CityReport {
    private String title;
    private String description;
    private String category;
    private GeoPoint location;
    private Timestamp timestamp;

    public CityReport() {}

    public CityReport(String title, String description, String category, GeoPoint location, Timestamp timestamp) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.timestamp = timestamp;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public GeoPoint getLocation() { return location; }
    public Timestamp getTimestamp() { return timestamp; }
}
