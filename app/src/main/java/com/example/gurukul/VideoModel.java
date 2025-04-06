package com.example.gurukul;

public class VideoModel {
    private String title;
    private String description;
    private String thumbnailUrl;
    private String videoUrl;
    private String courseId;

    public VideoModel(String title, String description, String thumbnailUrl, String videoUrl, String courseId) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.videoUrl = videoUrl;
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getCourseId() {
        return courseId;
    }
}
