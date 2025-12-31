package com.example.foodaid_mad_project.AuthFragments;

import java.util.List;

/**
 * User
 *
 * Represents the User model for Authentication and Profile purposes.
 * This class corresponds to the documents in the "users" collection in Firestore.
 */
public class User {

    private String uid;
    private String email;
    private String displayName; // Populated from Registration or Google
    private String photoUrl; // Firebase Storage URL
    private java.util.List<String> earnedBadges = new java.util.ArrayList<>(); // List of Badge IDs (e.g., "badge_10kg")
    private long createdAt; // Timestamp
    private long lastLogin; // Timestamp

    // Required empty public constructor for Firestore serialization
    public User() {
    }

    public User(String uid, String email, String displayName, String photoUrl, List<String> earnedBadges,
            long createdAt, long lastLogin) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.earnedBadges = earnedBadges;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // ============================================================================================
    // GETTERS & SETTERS
    // ============================================================================================

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

    /**
     * Helper to get the display name, aliased as getName for convenience.
     */
    public String getName() {
        return displayName;
    }
}
