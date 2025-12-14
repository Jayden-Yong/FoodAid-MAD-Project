package com.example.foodaid_mad_project.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class FoodBank implements Parcelable {
    private String id;
    private String name;
    private String type; // e.g., "Soup Kitchen", "Food Pantry"
    private double latitude;
    private double longitude;
    private String address;
    private String description;
    private String contactNumber;
    private String operatingHours;
    private float rating;
    private int ratingCount;
    private String imageUrl;
    private boolean isVerified;
    private String ownerId;

    // Default constructor required for calls to
    // DataSnapshot.getValue(FoodBank.class)
    public FoodBank() {
    }

    public FoodBank(String id, String name, String type, double latitude, double longitude,
            String address, String description, String contactNumber, String operatingHours,
            String imageUrl, String ownerId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.description = description;
        this.contactNumber = contactNumber;
        this.operatingHours = operatingHours;
        this.imageUrl = imageUrl;
        this.ownerId = ownerId;
        this.rating = 0.0f;
        this.ratingCount = 0;
        this.isVerified = false; // Default to pending
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    // Parcelable Implementation
    protected FoodBank(Parcel in) {
        id = in.readString();
        name = in.readString();
        type = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        address = in.readString();
        description = in.readString();
        contactNumber = in.readString();
        operatingHours = in.readString();
        rating = in.readFloat();
        ratingCount = in.readInt();
        imageUrl = in.readString();
        isVerified = in.readByte() != 0;
        ownerId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(address);
        dest.writeString(description);
        dest.writeString(contactNumber);
        dest.writeString(operatingHours);
        dest.writeFloat(rating);
        dest.writeInt(ratingCount);
        dest.writeString(imageUrl);
        dest.writeByte((byte) (isVerified ? 1 : 0));
        dest.writeString(ownerId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FoodBank> CREATOR = new Creator<FoodBank>() {
        @Override
        public FoodBank createFromParcel(Parcel in) {
            return new FoodBank(in);
        }

        @Override
        public FoodBank[] newArray(int size) {
            return new FoodBank[size];
        }
    };
}
