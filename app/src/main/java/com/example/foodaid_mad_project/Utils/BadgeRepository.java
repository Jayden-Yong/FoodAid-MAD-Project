package com.example.foodaid_mad_project.Utils;

import com.example.foodaid_mad_project.Model.Badge;
import com.example.foodaid_mad_project.R;

import java.util.ArrayList;
import java.util.List;

public class BadgeRepository {

    public static List<Badge> getAllBadges() {
        List<Badge> allBadges = new ArrayList<>();
        allBadges.add(new Badge("badge_10kg", "10kg Saved", "Saved 10kg of food", 10.0, R.drawable.ic_launcher_foreground));
        allBadges.add(new Badge("badge_50kg", "50kg Saved", "Saved 50kg of food", 50.0, R.drawable.ic_launcher_foreground));
        allBadges.add(new Badge("badge_100kg", "100kg Saved", "Saved 100kg of food", 100.0, R.drawable.ic_launcher_foreground));
        return allBadges;
    }

    public static Badge getBadgeById(String id) {
        for (Badge badge : getAllBadges()) {
            if (badge.getId().equals(id)) {
                return badge;
            }
        }
        return null;
    }
}
