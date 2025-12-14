package com.example.foodaid_mad_project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.foodaid_mad_project.Model.FoodBank;
import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends ViewModel {

    // Source of Truth (All data)
    private final List<FoodBank> allFoodBanks = new ArrayList<>();

    // Observed Data (Filtered list for UI)
    private final MutableLiveData<List<FoodBank>> filteredFoodBanks = new MutableLiveData<>();

    // Selecting an item on Map/List
    private final MutableLiveData<FoodBank> selectedFoodBank = new MutableLiveData<>();

    public SharedViewModel() {
        // Initialize with Mock Data for now (Project Requirement: UM integration first)
        loadMockData();
    }

    public LiveData<List<FoodBank>> getFoodBanks() {
        return filteredFoodBanks;
    }

    public LiveData<FoodBank> getSelectedFoodBank() {
        return selectedFoodBank;
    }

    public void selectFoodBank(FoodBank foodBank) {
        selectedFoodBank.setValue(foodBank);
    }

    // UC6: Filter Logic
    public void applyFilter(String type) {
        if (type == null || type.equals("All") || type.isEmpty()) {
            filteredFoodBanks.setValue(new ArrayList<>(allFoodBanks));
        } else {
            List<FoodBank> filteredList = new ArrayList<>();
            for (FoodBank fb : allFoodBanks) {
                if (fb.getType().equalsIgnoreCase(type)) {
                    filteredList.add(fb);
                }
            }
            filteredFoodBanks.setValue(filteredList);
        }
    }

    private void loadMockData() {
        allFoodBanks.clear();

        // 1. KK12 (Residential College)
        allFoodBanks.add(new FoodBank(
                "fb_kk12",
                "KK12 Food Pantry",
                "Food Pantry",
                3.1256, 101.6525,
                "Raja Dr. Nazrin Shah Residential College",
                "Free dry foods for students.",
                "03-12345678",
                "24 Hours",
                "https://example.com/kk12.jpg",
                "admin_1"));

        // 2. Cafe d'Rimba (FCSIT) - Requested Replacement
        allFoodBanks.add(new FoodBank(
                "fb_rimba",
                "Cafe d'Rimba (FCSIT)",
                "Soup Kitchen",
                3.1282, 101.6507,
                "Faculty of Computer Science and Information Technology",
                "Hot meals available during lunch hours.",
                "011-98765432",
                "12:00 PM - 2:00 PM",
                "https://example.com/rimba.jpg",
                "admin_2"));

        // 3. Main Library
        allFoodBanks.add(new FoodBank(
                "fb_library",
                "Main Library Food Bank",
                "Food Bank",
                3.1215, 101.6545,
                "Foyer, Main Library",
                "Community shared food shelf.",
                "03-55556666",
                "8:00 AM - 10:00 PM",
                "https://example.com/lib.jpg",
                "admin_3"));

        // 4. API (Academy of Islamic Studies)
        allFoodBanks.add(new FoodBank(
                "fb_api",
                "API Community Fridge",
                "Community Fridge",
                3.1180, 101.6560,
                "Academy of Islamic Studies Entrace",
                "Chilled foods and drinks.",
                "03-77778888",
                "9:00 AM - 5:00 PM",
                "https://example.com/api.jpg",
                "admin_4"));

        // Initial state: Show all
        filteredFoodBanks.setValue(new ArrayList<>(allFoodBanks));
    }
}
