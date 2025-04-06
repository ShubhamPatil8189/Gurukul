package com.example.gurukul;


public class StudentModel {
    private String name;
    private String email;

    public StudentModel(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
}