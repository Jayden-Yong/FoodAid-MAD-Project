package com.example.foodaid_mad_project.AuthFragments;

import java.util.List;

public class User {
    private String uid;
    private String email;
    private String displayName; // Populated from Registration or Google
    private String photoUrl; // Firebase Storage URL
    private java.util.List<String> earnedBadges = new java.util.ArrayList<>(); // List of Badge IDs (e.g., "badge_10kg")
    private String userType; // "student"
    private long createdAt; // Timestamp
    private long lastLogin; // Timestamp

    public User() {
    }

    public User(String uid, String email, String displayName, String photoUrl, List<String> earnedBadges,
            String userType, long createdAt, long lastLogin) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.earnedBadges = earnedBadges;
        this.userType = userType;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public List<String> getEarnedBadges() {
        return earnedBadges;
    }

    public void setEarnedBadges(List<String> earnedBadges) {
        this.earnedBadges = earnedBadges;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void addAdditionalData(java.util.Map<String, Object> data) {
        // This is a helper for local updates, though ideally User object should have
        // these fields
        // For now, we rely on Firestore for these extra fields if they aren't in the
        // model
        // or we add them to the model.
        // Let's add them to the model for completeness if we want strong typing
    }

    public String getName() {
        return displayName;
    }
}
