package com.example.foodaid_mad_project.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class FoodItem implements Parcelable {
    private String id;
    private String name;
    private String location;
    private String quantity;
    private String donator;
    private int imageResId; // Using resource ID for mock data
    private double lat;
    private double lng;

    public FoodItem(String id, String name, String location, String quantity, String donator, int imageResId, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.quantity = quantity;
        this.donator = donator;
        this.imageResId = imageResId;
        this.lat = lat;
        this.lng = lng;
    }

    protected FoodItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        location = in.readString();
        quantity = in.readString();
        donator = in.readString();
        imageResId = in.readInt();
        lat = in.readDouble();
        lng = in.readDouble();
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
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(location);
        dest.writeString(quantity);
        dest.writeString(donator);
        dest.writeInt(imageResId);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    // Getters
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getQuantity() { return quantity; }
    public String getDonator() { return donator; }
    public int getImageResId() { return imageResId; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
}