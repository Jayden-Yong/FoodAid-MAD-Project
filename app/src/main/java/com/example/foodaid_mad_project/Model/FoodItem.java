package com.example.foodaid_mad_project.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class FoodItem implements Parcelable {
    private String id;
    private String name;
    private String donorId;
    private String claimeeId; // Null if not claimed yet
    private String status; // "AVAILABLE", "CLAIMED"
    private double weightKg;
    private long timestamp;
    private String imageUrl;
    private String description;
    private String expiryDate;

    // Empty constructor for Firestore
    public FoodItem() {
    }

    public FoodItem(String id, String name, String donorId, String status, double weightKg, long timestamp,
            String imageUrl) {
        this.id = id;
        this.name = name;
        this.donorId = donorId;
        this.status = status;
        this.weightKg = weightKg;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
    }

    protected FoodItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        donorId = in.readString();
        claimeeId = in.readString();
        status = in.readString();
        weightKg = in.readDouble();
        timestamp = in.readLong();
        imageUrl = in.readString();
        description = in.readString();
        expiryDate = in.readString();
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
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(donorId);
        dest.writeString(claimeeId);
        dest.writeString(status);
        dest.writeDouble(weightKg);
        dest.writeLong(timestamp);
        dest.writeString(imageUrl);
        dest.writeString(description);
        dest.writeString(expiryDate);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getClaimeeId() {
        return claimeeId;
    }

    public void setClaimeeId(String claimeeId) {
        this.claimeeId = claimeeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
