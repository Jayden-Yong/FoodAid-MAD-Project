package com.example.foodaid_mad_project.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class FoodItem implements Parcelable {
    private String donationId; // Document ID
    private String donatorId; // User UID
    private String donatorName; // For display
    private String title;
    private String category; // "GROCERIES" or "MEALS"
    private String pickupMethod; // "MEET_UP" or "FREE_TABLE"
    private String locationName; // Address text
    private double latitude;
    private double longitude;
    private String imageUri; // Storage URL

    // Impact & Time Logic
    private double weight; // TOTAL Weight in KG
    private long startTime; // Pick-up start (Unix Millis)
    private long endTime; // Expiry time (Unix Millis)

    // Status Logic
    private String status; // "AVAILABLE" or "CLAIMED"
    private String claimedBy; // UID of claimer (null if available)
    private long timestamp; // Posted time

    // Empty constructor needed for Firebase Firestore
    public FoodItem() {
    }

    // Full Constructor
    public FoodItem(String donationId, String donatorId, String donatorName, String title,
            String category, String pickupMethod, String locationName,
            double latitude, double longitude, String imageUri,
            double weight, long startTime, long endTime,
            String status, String claimedBy, long timestamp) {
        this.donationId = donationId;
        this.donatorId = donatorId;
        this.donatorName = donatorName;
        this.title = title;
        this.category = category;
        this.pickupMethod = pickupMethod;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUri = imageUri;
        this.weight = weight;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.claimedBy = claimedBy;
        this.timestamp = timestamp;
    }

    protected FoodItem(Parcel in) {
        donationId = in.readString();
        donatorId = in.readString();
        donatorName = in.readString();
        title = in.readString();
        category = in.readString();
        pickupMethod = in.readString();
        locationName = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        imageUri = in.readString();
        weight = in.readDouble();
        startTime = in.readLong();
        endTime = in.readLong();
        status = in.readString();
        claimedBy = in.readString();
        timestamp = in.readLong();
    }

    public static final Creator<FoodItem> CREATOR = new Creator<FoodItem>() {
        @Override
        public FoodItem createFromParcel(Parcel in) {
            return new FoodItem(in);
        }

        @Override
        public FoodItem[] newArray(int size) {
            return new FoodItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(donationId);
        dest.writeString(donatorId);
        dest.writeString(donatorName);
        dest.writeString(title);
        dest.writeString(category);
        dest.writeString(pickupMethod);
        dest.writeString(locationName);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(imageUri);
        dest.writeDouble(weight);
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeString(status);
        dest.writeString(claimedBy);
        dest.writeLong(timestamp);
    }

    // Getters and Setters

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public String getDonatorId() {
        return donatorId;
    }

    public void setDonatorId(String donatorId) {
        this.donatorId = donatorId;
    }

    public String getDonatorName() {
        return donatorName;
    }

    public void setDonatorName(String donatorName) {
        this.donatorName = donatorName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPickupMethod() {
        return pickupMethod;
    }

    public void setPickupMethod(String pickupMethod) {
        this.pickupMethod = pickupMethod;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(String claimedBy) {
        this.claimedBy = claimedBy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}