package com.example.foodaid_mad_project.ImpactFragments;

import com.example.foodaid_mad_project.Model.FoodItem;

import java.util.List;

/**
 * ImpactCalculator
 *
 * Utility class for calculating aggregation statistics for Impact reports.
 * Used to sum up counts and weights of food items over specific time ranges.
 */
public class ImpactCalculator {

    /**
     * Calculates the total weight of items within a time range.
     * 
     * @param items     List of food items
     * @param startTime Start timestamp
     * @param endTime   End timestamp
     * @return Total weight in kg
     */
    public double getWeightForRange(List<FoodItem> items, long startTime, long endTime) {
        double totalWeight = 0;
        for (FoodItem item : items) {
            if (item.getTimestamp() >= startTime && item.getTimestamp() <= endTime) {
                totalWeight += item.getWeight();
            }
        }
        return totalWeight;
    }

    /**
     * Calculates the count of items within a time range.
     * 
     * @param items     List of food items
     * @param startTime Start timestamp
     * @param endTime   End timestamp
     * @return Item count
     */
    public int getItemCountForRange(List<FoodItem> items, long startTime, long endTime) {
        int count = 0;
        for (FoodItem item : items) {
            if (item.getTimestamp() >= startTime && item.getTimestamp() <= endTime) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calculates the total weight of all items in the list.
     * 
     * @param items List of food items
     * @return Total weight in kg
     */
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
