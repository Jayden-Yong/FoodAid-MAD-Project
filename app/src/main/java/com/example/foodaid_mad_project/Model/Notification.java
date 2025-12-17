package com.example.foodaid_mad_project.Model;

public class Notification {
    private String id;
    private String title;
    private String message;
    private String type; // e.g., "SYSTEM", "CLAIM", "DONATION"
    private String relatedId; // e.g., donationId or userId
    private long timestamp;
    private boolean isRead;

    public Notification() {
        // Required for Firestore
    }

    public Notification(String id, String title, String message, String type, String relatedId, long timestamp,
            boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedId = relatedId;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
