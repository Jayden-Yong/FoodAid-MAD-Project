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
        // Initialize with Real Data from Firestore
        fetchFoodBanks();
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
    public void applyFilter(String filter) {
        if (filter == null || filter.equals("All") || filter.isEmpty()) {
            filteredFoodBanks.setValue(new ArrayList<>(allFoodBanks));
        } else {
            List<FoodBank> filteredList = new ArrayList<>();
            for (FoodBank fb : allFoodBanks) {
                // Auto-Elimination: Check if expired
                if (fb.getEndTime() > 0 && fb.getEndTime() < System.currentTimeMillis()) {
                    continue; // Skip expired items
                }

                // Check Category first (New), then Type (Legacy)
                // Check Category
                boolean matchCategory = fb.getCategory() != null && fb.getCategory().equalsIgnoreCase(filter);

                if (matchCategory) {
                    filteredList.add(fb);
                }
            }
            filteredFoodBanks.setValue(filteredList);
        }
    }

    private void fetchFoodBanks() {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("foodbanks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        allFoodBanks.clear();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                            FoodBank fb = doc.toObject(FoodBank.class);
                            // Ensure ID is set from Document ID if not in model
                            if (fb.getId() == null || fb.getId().isEmpty()) {
                                fb.setId(doc.getId());
                            }
                            allFoodBanks.add(fb);
                        }
                        // Refresh the view with new data
                        filteredFoodBanks.setValue(new ArrayList<>(allFoodBanks));
                    }
                });
    }
}
