package com.example.foodaid_mad_project.Model;

/**
 * Badge
 *
 * Represents an achievement badge that a user can earn based on their contributions
 * (e.g., total weight of food donated).
 */
public class Badge {
    private String id; // Unique ID, e.g., "badge_10kg"
    private String name; // Display Name, e.g., "Food Saver"
    private String description; // Description, e.g., "Saved 10kg of food"
    private double requiredWeight; // Threshold to earn this badge
    private int iconResId; // Resource ID for the badge icon

    public Badge(String id, String name, String description, double requiredWeight, int iconResId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredWeight = requiredWeight;
        this.iconResId = iconResId;
    }

    // ============================================================================================
    // GETTERS
    // ============================================================================================

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
