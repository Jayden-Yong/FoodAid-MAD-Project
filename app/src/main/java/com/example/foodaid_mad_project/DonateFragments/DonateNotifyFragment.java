package com.example.foodaid_mad_project.DonateFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.HomeFragments.HomeFragment;
import com.example.foodaid_mad_project.HomeFragments.ItemDetailsFragment;
import com.example.foodaid_mad_project.MainActivity;
import com.example.foodaid_mad_project.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class DonateNotifyFragment extends Fragment {

    private MaterialButton btnViewItem, btnBackToHome;

    private ItemDetailsFragment itemDetailsFragment;
    private String title;
    private String[] pickupTime;
    private int category;
    private int quantity;
    private String location;
    private String donator;

    public DonateNotifyFragment() {
    }

    public DonateNotifyFragment(String title, String[] pickupTime, int category, int quantity, String location,
            String donator) {
        this.title = title;
        this.pickupTime = pickupTime;
        this.category = category;
        this.quantity = quantity;
        this.location = location;
        this.donator = donator;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donate_notify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnViewItem = view.findViewById(R.id.btnViewItem);
        btnBackToHome = view.findViewById(R.id.btnBackToHome);

        // Initialize ViewModel
        com.example.foodaid_mad_project.SharedViewModel sharedViewModel = new androidx.lifecycle.ViewModelProvider(
                requireActivity()).get(com.example.foodaid_mad_project.SharedViewModel.class);

        btnViewItem.setOnClickListener(v -> {

            // Create a temporary FoodBank object to represent this donation
            com.example.foodaid_mad_project.Model.FoodBank tempFoodBank = new com.example.foodaid_mad_project.Model.FoodBank();
            tempFoodBank.setId("temp_" + System.currentTimeMillis());
            tempFoodBank.setName(title);
            tempFoodBank.setAddress(location);
            tempFoodBank.setOwnerId(donator);

            // Join pickup times
            StringBuilder hours = new StringBuilder();
            if (pickupTime != null) {
                for (String time : pickupTime)
                    hours.append(time).append(" ");
            }
            java.util.Map<String, String> hoursMap = new java.util.HashMap<>();
            hoursMap.put("Hours", hours.toString().trim());
            tempFoodBank.setOperatingHours(hoursMap);

            // Map Category (assuming int maps to something meaningful or just use generic)
            // Map Category (assuming int maps to something meaningful or just use generic)
            tempFoodBank.setCategory(category == 1 ? "Halal" : "Non-Halal");
            tempFoodBank.setNotes("Quantity: " + quantity); // Store quantity in description for now

            // Set selection in ViewModel
            sharedViewModel.selectFoodBank(tempFoodBank);

            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.coveringFragment, new ItemDetailsFragment())
                    .addToBackStack("ItemDetail")
                    .commit();
        });

        btnBackToHome.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });
    }
}
