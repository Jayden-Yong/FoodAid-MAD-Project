package com.example.foodaid_mad_project.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class FoodBank implements Parcelable {
    private String id;
    private String name;
    // New Listing Data
    private String category; // "Menu Rahmah", "Leftover", "Event"
    private double price; // For Rahmah/Leftover
    private Object quantity; // Can be int or String in Firestore
    private String status; // "AVAILABLE" or "COMPLETED"
    private String notes; // Description/Notes
    private long endTime; // Timestamp for auto-elimination
    private long timestamp; // Creation time/Notes
    private String ownerId;

    // Legacy mapping (Type -> Category)
    private java.util.Map<String, String> operatingHours;
    private float rating;
    private int ratingCount;
    private boolean isVerified;

    // Persisted fields required for Map/Display
    private String address;
    private double latitude;
    private double longitude;
    private String imageUrl;

    public FoodBank() {
    }

    public FoodBank(String id, String name, String category, double price, int quantity, String status, String notes,
            String ownerId, String address, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
        this.notes = notes;
        this.ownerId = ownerId;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = 0f;
        this.ratingCount = 0;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        if (quantity instanceof Number) {
            return ((Number) quantity).intValue();
        } else if (quantity instanceof String) {
            try {
                return Integer.parseInt((String) quantity);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public void setQuantity(Object quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @com.google.firebase.firestore.PropertyName("lat")
    public double getLatitude() {
        return latitude;
    }

    @com.google.firebase.firestore.PropertyName("lat")
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @com.google.firebase.firestore.PropertyName("lng")
    public double getLongitude() {
        return longitude;
    }

    @com.google.firebase.firestore.PropertyName("lng")
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @com.google.firebase.firestore.PropertyName("location")
    public String getAddress() {
        return address;
    }

    @com.google.firebase.firestore.PropertyName("location")
    public void setAddress(String address) {
        this.address = address;
    }

    public java.util.Map<String, String> getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(java.util.Map<String, String> operatingHours) {
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

    @com.google.firebase.firestore.PropertyName("imageUri")
    public String getImageUrl() {
        return imageUrl;
    }

    @com.google.firebase.firestore.PropertyName("imageUri")
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
        // New Fields
        category = in.readString();
        price = in.readDouble();
        quantity = in.readInt();
        status = in.readString();
        notes = in.readString();
        endTime = in.readLong();
        timestamp = in.readLong();

        latitude = in.readDouble();
        longitude = in.readDouble();
        address = in.readString();
        // Read Map
        int size = in.readInt();
        if (size >= 0) {
            operatingHours = new java.util.HashMap<>(size);
            for (int i = 0; i < size; i++) {
                String key = in.readString();
                String value = in.readString();
                operatingHours.put(key, value);
            }
        } else {
            operatingHours = null;
        }

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
        // New Fields
        dest.writeString(category);
        dest.writeDouble(price);
        dest.writeInt(getQuantity());
        dest.writeString(status);
        dest.writeString(notes);
        dest.writeLong(endTime);
        dest.writeLong(timestamp);

        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(address);
        // Write Map
        if (operatingHours == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(operatingHours.size());
            for (java.util.Map.Entry<String, String> entry : operatingHours.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }
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

    // Helper methods for compatibility
    @com.google.firebase.firestore.Exclude
    public double getLat() {
        return latitude;
    }

    @com.google.firebase.firestore.Exclude
    public double getLng() {
        return longitude;
    }

    @com.google.firebase.firestore.Exclude
    public String getLocation() {
        return address != null ? address : latitude + ", " + longitude;
    }

    // Helper to format operating hours for display
    @com.google.firebase.firestore.Exclude
    public String getFormattedOperatingHours() {
        if (operatingHours == null || operatingHours.isEmpty()) {
            return "Not Available";
        }
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<String, String> entry : operatingHours.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString().trim();
    }
}
