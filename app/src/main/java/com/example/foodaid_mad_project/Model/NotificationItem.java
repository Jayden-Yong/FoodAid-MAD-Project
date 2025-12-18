package com.example.foodaid_mad_project.Model;

import java.util.Date;

/**
 * <h1>NotificationItem</h1>
 * <p>
 * A View Model Wrapper class for displaying notifications in the RecyclerView.
 * It can represent either:
 * <ul>
 * <li>A <b>Header</b> (e.g., "Today", "Yesterday")</li>
 * <li>A <b>Data Item</b> (actual notification content)</li>
 * </ul>
 * </p>
 */
public class NotificationItem {
    // Data Item Fields
    private String title;
    private String description;
    private String timeString; // Display text (e.g. "10:00 AM")
    private String type;
    private boolean isRead;
    private String id;
    private Date timestamp;
    private String userId;

    // Header State
    private boolean isHeader;

    // Constructor for Data Item (Simple)
    public NotificationItem(String title, String description, String timeString, String type, boolean isRead,
            Date timestamp) {
        this(title, description, timeString, type, isRead, timestamp, null, null);
    }

    // Constructor for Data Item (With ID)
    public NotificationItem(String title, String description, String timeString, String type, boolean isRead,
            Date timestamp, String id) {
        this(title, description, timeString, type, isRead, timestamp, id, null);
    }

    // Constructor for Data Item (Full)
    public NotificationItem(String title, String description, String timeString, String type, boolean isRead,
            Date timestamp, String id, String userId) {
        this.title = title;
        this.description = description;
        this.timeString = timeString;
        this.type = type;
        this.isRead = isRead;
        this.timestamp = timestamp;
        this.isHeader = false;
        this.id = id;
        this.userId = userId;
    }

    // Constructor for Header
    public NotificationItem(String headerTitle) {
        this.title = headerTitle; // Reuse title field for header text
        this.isHeader = true;
        this.timestamp = new Date(); // Default to now (won't used for sorting typically)
    }

    // ============================================================================================
    // GETTERS
    // ============================================================================================

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

    public String getUserId() {
        return userId;
    }
}