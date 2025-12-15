package com.example.foodaid_mad_project.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class FoodItem implements Parcelable {
    private String id;
    private String name;

    // --- FIELDS FROM HEAD (Target dynamic impact logic) ---
    private String donorId;
    private String claimeeId; // Null if not claimed yet
    private String status; // "AVAILABLE", "CLAIMED"
    private double weightKg;
    private long timestamp;
    private String imageUrl;
    private String description;
    private String expiryDate;

    // --- FIELDS FROM MAIN (Existing/Map logic) ---
    private String location;
    private String quantity;
    private String donator; // Conflict with donorId? Keeping both to be safe
    private int imageResId; // For Mock Data
    private String imageUri; // Duplicate of imageUrl? Keeping both.
    private double lat;
    private double lng;
    private long postDurationMins;

    // Empty constructor for Firestore
    public FoodItem() {
    }

    // Constructor for HEAD (Dynamic Impact)
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

    // Constructor for MAIN (Mock Data/Map)
    public FoodItem(String id, String name, String location, String quantity, String donator, int imageResId,
            double lat, double lng, long postDurationMins) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.quantity = quantity;
        this.donator = donator;
        this.imageResId = imageResId;
        this.lat = lat;
        this.lng = lng;
        this.postDurationMins = postDurationMins;
    }

    // Constructor for MAIN (Real Data Map?)
    public FoodItem(String id, String name, String location, String quantity, String donator, String imageUri,
            double lat, double lng, long postDurationMins) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.quantity = quantity;
        this.donator = donator;
        this.imageUri = imageUri;
        this.lat = lat;
        this.lng = lng;
        this.postDurationMins = postDurationMins;
    }

    protected FoodItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        // HEAD
        donorId = in.readString();
        claimeeId = in.readString();
        status = in.readString();
        weightKg = in.readDouble();
        timestamp = in.readLong();
        imageUrl = in.readString();
        description = in.readString();
        expiryDate = in.readString();
        // MAIN
        location = in.readString();
        quantity = in.readString();
        donator = in.readString();
        imageResId = in.readInt();
        imageUri = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        postDurationMins = in.readLong();
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
        // HEAD
        dest.writeString(donorId);
        dest.writeString(claimeeId);
        dest.writeString(status);
        dest.writeDouble(weightKg);
        dest.writeLong(timestamp);
        dest.writeString(imageUrl);
        dest.writeString(description);
        dest.writeString(expiryDate);
        // MAIN
        dest.writeString(location);
        dest.writeString(quantity);
        dest.writeString(donator);
        dest.writeInt(imageResId);
        dest.writeString(imageUri);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeLong(postDurationMins);
    }

    // Getters and Setters - HEAD
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

    // Merge image logic? If imageUrl is set, return it. If imageUri is set, return
    // it.
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

    // Getters and Setters - MAIN
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDonator() {
        return donator;
    }

    public void setDonator(String donator) {
        this.donator = donator;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getPostDurationMins() {
        return postDurationMins;
    }

    public void setPostDurationMins(long postDurationMins) {
        this.postDurationMins = postDurationMins;
    }
}
