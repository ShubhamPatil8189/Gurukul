package com.example.gurukul;

public class EnrolledCourse {
    private String courseName;
    private String instructorName;
    private String duration;
    private int imageResId;

    public EnrolledCourse(String courseName, String instructorName, String duration, int imageResId) {
        this.courseName = courseName;
        this.instructorName = instructorName;
        this.duration = duration;
        this.imageResId = imageResId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public String getDuration() {
        return duration;
    }

    public int getImageResId() {
        return imageResId;
    }
}