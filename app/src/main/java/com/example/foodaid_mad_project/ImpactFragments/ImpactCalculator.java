package com.example.foodaid_mad_project.ImpactFragments;

import com.example.foodaid_mad_project.Model.FoodItem;

import java.util.List;

public class ImpactCalculator {

    public double getWeightForRange(List<FoodItem> items, long startTime, long endTime) {
        double totalWeight = 0;
        for (FoodItem item : items) {
            // Using timestamp (posted time) or endTime (claim time)?
            // For claimed items, we might care when they were claimed, but FoodItem usually
            // stores posted timestamp.
            // Let's assume we filter by the item's creation timestamp for now, or 'endTime'
            // if it represents cleanup.
            // However, looking at FoodItem, 'timestamp' was added. Let's use 'timestamp'.
            if (item.getTimestamp() >= startTime && item.getTimestamp() <= endTime) {
                totalWeight += item.getWeight();
            }
        }
        return totalWeight;
    }

    public int getItemCountForRange(List<FoodItem> items, long startTime, long endTime) {
        int count = 0;
        for (FoodItem item : items) {
            if (item.getTimestamp() >= startTime && item.getTimestamp() <= endTime) {
                count++;
            }
        }
        return count;
    }

    public double getAllTimeWeight(List<FoodItem> items) {
        double totalWeight = 0;
        if (items != null) {
            for (FoodItem item : items) {
                totalWeight += item.getWeight();
            }
        }
        return totalWeight;
    }
}
