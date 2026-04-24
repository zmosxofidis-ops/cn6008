package com.example.smartcityapp.models;

import com.google.gson.annotations.SerializedName;

public class CityReport {
    private final String title;
    private final String description;
    private final String category;
    
    @SerializedName("latitude")
    private double latitude;
    
    @SerializedName("longitude")
    private double longitude;
    
    @SerializedName("created_at")
    private String createdAt;

    public CityReport(String title, String description, String category, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
