package com.example.gurukul;

public class Course {
    private String courseId;
    private String courseName;
    private String description;
    private String imageUrl;
    private String duration;
    private String rating;
    private String instructor;
    private String organizationId;

    public Course() {
        // Empty constructor required for Firestore
    }

    public Course(String courseId, String courseName, String description, String imageUrl, String duration, String rating) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.duration = duration;
        this.rating = rating;
    }

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}