package com.example.gurukul;

public class Organization {
    private String uid;
    private String title;
    private String description;
    private String imageUrl;
    private String ownerId;

    public Organization() {
        // Empty constructor required for Firestore
    }

    public Organization(String uid, String title, String description, String imageUrl, String ownerId) {
        this.uid = uid;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.ownerId = ownerId;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}