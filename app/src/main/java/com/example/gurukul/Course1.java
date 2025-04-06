package com.example.gurukul;


public class Course1 {
    private String courseName;
    private String instructorName;
    private String duration;
    private int imageResId;
    private int progress;

    public Course1(String courseName, String instructorName, String duration, int imageResId, int progress) {
        this.courseName = courseName;
        this.instructorName = instructorName;
        this.duration = duration;
        this.imageResId = imageResId;
        this.progress = progress;
    }

    public String getCourseName() { return courseName; }
    public String getInstructorName() { return instructorName; }
    public String getDuration() { return duration; }
    public int getImageResId() { return imageResId; }
    public int getProgress() { return progress; }
}