package com.rentease.app.model;

import java.io.Serializable;

public class Property implements Serializable {
    public String id, name, location, type, rent, description, ownerId, imageUrl, status;

    public Property() {
        this.status = "Active";
    }

    public Property(String id, String name, String location, String type, String rent,
                    String description, String ownerId, String imageUrl, String status) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.type = type;
        this.rent = rent;
        this.description = description;
        this.ownerId = ownerId;
        this.imageUrl = imageUrl;
        this.status = (status != null && !status.isEmpty()) ? status : "Active";
    }

    public Property(String id, String name, String location, String type, String rent,
                    String description, String ownerId, String imageUrl) {
        this(id, name, location, type, rent, description, ownerId, imageUrl, "Active");
    }

    public Property(String name, String location, String type, String rent,
                    String description, String ownerId, String imageUrl) {
        this(null, name, location, type, rent, description, ownerId, imageUrl, "Active");
    }

    public boolean isExpired() {
        return "Expired".equalsIgnoreCase(status);
    }

    public boolean isBooked() {
        return "Booked".equalsIgnoreCase(status);
    }
}
