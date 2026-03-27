package com.example.smartcityapp.models;

import com.google.firebase.firestore.GeoPoint;

public class ParkingSpot {
    private String id;
    private String name;
    private GeoPoint location;
    private boolean isAvailable;

    public ParkingSpot() {} // Required for Firestore

    public ParkingSpot(String id, String name, GeoPoint location, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.isAvailable = isAvailable;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public GeoPoint getLocation() { return location; }
    public boolean isAvailable() { return isAvailable; }
}
