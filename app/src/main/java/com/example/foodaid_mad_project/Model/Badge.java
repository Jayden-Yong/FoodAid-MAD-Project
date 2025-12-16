package com.example.foodaid_mad_project.Model;

public class Badge {
    private String id; // "badge_10kg"
    private String name; // "Food Saver"
    private String description; // "Saved 10kg of food"
    private double requiredWeight; // 10.0
    private int iconResId; // R.drawable.ic_badge_something

    public Badge(String id, String name, String description, double requiredWeight, int iconResId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredWeight = requiredWeight;
        this.iconResId = iconResId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getRequiredWeight() {
        return requiredWeight;
    }

    public int getIconResId() {
        return iconResId;
    }
}
