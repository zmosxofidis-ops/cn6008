package com.example.smartcityapp.models;

import com.google.gson.annotations.SerializedName;

/**
 * Μοντέλο δεδομένων για μια αναφορά προβλήματος στην πόλη.
 */
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

    // Νέο πεδίο για την αποθήκευση της διαδρομής της φωτογραφίας στη συσκευή
    private String imagePath;

    /**
     * Constructor για τη δημιουργία αναφοράς χωρίς αρχικό imagePath.
     */
    public CityReport(String title, String description, String category, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Πλήρης Constructor που περιλαμβάνει και τη διαδρομή της εικόνας.
     */
    public CityReport(String title, String description, String category, double latitude, double longitude, String imagePath) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imagePath = imagePath;
    }

    // Getters και Setters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
