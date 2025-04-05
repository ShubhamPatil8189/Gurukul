package com.example.gurukul;
public class Organization {
    private String uid;
    private String title;
    private String description;
    private String imageUrl;

    public Organization(String uid, String title, String description, String imageUrl) {
        this.uid = uid;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getUid() { return uid; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
}
