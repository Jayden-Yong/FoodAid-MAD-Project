package com.example.foodaid_mad_project.AuthFragments;

import java.util.List;
import java.util.Map;

public class User {
    private String uid;
    private String email;
    private String fullName;
    private String displayName;
    private String userType;
    private String faculty;
    private String residentialCollege;
    private long createdAt;
    private List<String> dietaryPreferences;


    public User() {}

    public User(String uid, String email, String fullName, String displayName, String userType, String faculty, String residentialCollege, long createdAt, List<String> dietaryPreferences) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.userType = userType;
        this.createdAt = createdAt;
        this.dietaryPreferences = dietaryPreferences;
        this.faculty = faculty;
        this.residentialCollege = residentialCollege;
        this.fullName = fullName;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFaculty() { return faculty; }

    public String getResidentialCollege() { return residentialCollege; }

    public String getFullName() { return fullName; }

    public String getUserType() {
        return userType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public List<String> getDiet() {
        return dietaryPreferences;
    }

    public void addAdditionalData(Map<String, Object> additionalData) {
        this.fullName = (String) additionalData.get("fullName");
        this.faculty = (String) additionalData.get("faculty");
        this.residentialCollege = (String) additionalData.get("residentialCollege");
        this.dietaryPreferences = (List<String>) additionalData.get("dietaryPreferences");
    }
}
