package com.example.foodaid_mad_project.CommunityFragments;

import com.google.firebase.Timestamp;

public class Report {
    private String reporterId;
    private String issueType;
    private String description;
    private Timestamp timestamp;
    private String status;

    // Empty constructor needed for Firestore serialization
    public Report() {
    }

    public Report(String reporterId, String issueType, String description, Timestamp timestamp, String status) {
        this.reporterId = reporterId;
        this.issueType = issueType;
        this.description = description;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
