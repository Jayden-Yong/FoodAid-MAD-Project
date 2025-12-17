package com.example.foodaid_mad_project.Model;

import java.util.Date;

public class NotificationItem {
    private String title;
    private String description;
    private String timeString; // Display text (e.g. "10:00 AM")
    private String type;
    private boolean isRead;

    // New fields for sorting/headers
    private String id;
    private Date timestamp;
    private boolean isHeader;

    // Constructor for Data Item
    public NotificationItem(String title, String description, String timeString, String type, boolean isRead,
            Date timestamp) {
        this(title, description, timeString, type, isRead, timestamp, null);
    }

    public NotificationItem(String title, String description, String timeString, String type, boolean isRead,
            Date timestamp, String id) {
        this.title = title;
        this.description = description;
        this.timeString = timeString;
        this.type = type;
        this.isRead = isRead;
        this.timestamp = timestamp;
        this.isHeader = false;
        this.id = id;
    }

    // Constructor for Header
    public NotificationItem(String headerTitle) {
        this.title = headerTitle; // Reuse title field for header text
        this.isHeader = true;
        this.timestamp = new Date(); // Default to now (won't be used for sorting header itself usually)
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTimeString() {
        return timeString;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return isRead;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public String getId() {
        return id;
    }
}