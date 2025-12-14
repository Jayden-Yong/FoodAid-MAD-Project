package com.example.foodaid_mad_project.Model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class DonationItem {
    private String id;
    private String donatorId; // From UserManager
    private String itemName;
    private int quantity;
    private String weight; // e.g. "500g"
    private String expiryDate;
    private String pickupMethod;
    private String location;
    private String pickupTimeFrom;
    private String pickupTimeTo;
    private String description;
    private String status; // "Available", "Claimed"

    @ServerTimestamp
    private Timestamp timestamp;

    // Empty constructor for Firebase
    public DonationItem() {
    }

    public DonationItem(String donatorId, String itemName, int quantity, String weight,
            String expiryDate, String pickupMethod, String location, String description,
            String pickupTimeFrom, String pickupTimeTo) {
        this.donatorId = donatorId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.weight = weight;
        this.expiryDate = expiryDate;
        this.pickupMethod = pickupMethod;
        this.location = location;
        this.description = description;
        this.pickupTimeFrom = pickupTimeFrom;
        this.pickupTimeTo = pickupTimeTo;
        this.status = "Available";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDonatorId() {
        return donatorId;
    }

    public void setDonatorId(String donatorId) {
        this.donatorId = donatorId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getPickupMethod() {
        return pickupMethod;
    }

    public void setPickupMethod(String pickupMethod) {
        this.pickupMethod = pickupMethod;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getPickupTimeFrom() {
        return pickupTimeFrom;
    }

    public void setPickupTimeFrom(String pickupTimeFrom) {
        this.pickupTimeFrom = pickupTimeFrom;
    }

    public String getPickupTimeTo() {
        return pickupTimeTo;
    }

    public void setPickupTimeTo(String pickupTimeTo) {
        this.pickupTimeTo = pickupTimeTo;
    }
}
