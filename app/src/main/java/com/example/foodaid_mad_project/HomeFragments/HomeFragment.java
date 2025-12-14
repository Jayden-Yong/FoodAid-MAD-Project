package com.example.foodaid_mad_project.HomeFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.AuthFragments.User;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.UserManager;

public class HomeFragment extends Fragment {

    private String email, username, welcomeDisplay;
    private TextView tvWelcomeUser;
    private androidx.recyclerview.widget.RecyclerView rvFoodBanks;
    private FoodBankAdapter foodBankAdapter;
    private com.example.foodaid_mad_project.SharedViewModel sharedViewModel;
    private com.google.android.material.chip.ChipGroup chipGroupFilters;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvWelcomeUser = view.findViewById(R.id.tvWelcomeUser);
        rvFoodBanks = view.findViewById(R.id.rvFoodBanks);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);

        // Setup ViewModel
        sharedViewModel = new androidx.lifecycle.ViewModelProvider(requireActivity())
                .get(com.example.foodaid_mad_project.SharedViewModel.class);

        // Setup RecyclerView
        // Setup RecyclerView
        rvFoodBanks.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        foodBankAdapter = new FoodBankAdapter(foodBank -> {
            // Handle Item Click -> Update ViewModel
            // This will trigger the observer below to navigate
            sharedViewModel.selectFoodBank(foodBank);
        });
        rvFoodBanks.setAdapter(foodBankAdapter);

        // Observe Data (List Updates)
        sharedViewModel.getFoodBanks().observe(getViewLifecycleOwner(), foodBanks -> {
            foodBankAdapter.setFoodBanks(foodBanks);
        });

        // Observe Selection (Navigation Logic)
        sharedViewModel.getSelectedFoodBank().observe(getViewLifecycleOwner(), foodBank -> {
            if (foodBank != null) {
                // Navigate to Details Fragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ItemDetailsFragment())
                        .addToBackStack("HomeToDetails")
                        .commit();

                // Note: We don't clear selection immediately so the details page can read it.
                // We rely on the ViewModel holding the state.
            }
        });

        // Setup Filter Chips
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            String filterType = "All";
            if (checkedId == R.id.chipPantry)
                filterType = "Food Pantry";
            else if (checkedId == R.id.chipLeftover)
                filterType = "Soup Kitchen"; // Mapping for now
            else if (checkedId == R.id.chipFreeTable)
                filterType = "Community Fridge"; // Mapping for now

            sharedViewModel.applyFilter(filterType);
        });

        // Setup User Greeting
        try {
            User user = UserManager.getInstance().getUser();
            if (user != null) {
                email = user.getEmail();

                if (user.getFullName() != null) {
                    username = user.getFullName();
                } else {
                    username = user.getDisplayName();
                }

                welcomeDisplay = username;
            }

        } catch (NullPointerException e) {
            welcomeDisplay = "Guest";
        }

        tvWelcomeUser.setText(getString(R.string.Welcome_User, "morning", welcomeDisplay));

        // Setup Switch View Button Logic
        setupSwitchView(view);
    }

    private android.widget.ImageButton btnSwitchView;
    private View mapContainer;
    private boolean isMapVisible = true; // Apps starts with Map by default

    private void setupSwitchView(View view) {
        btnSwitchView = view.findViewById(R.id.btnSwitchView);
        mapContainer = view.findViewById(R.id.MapFragment);

        // Initial State (Map Visible, List Gone - set in XML, but ensure here)
        mapContainer.setVisibility(View.VISIBLE);
        rvFoodBanks.setVisibility(View.GONE);
        if (btnSwitchView != null) {
            btnSwitchView.setImageResource(R.drawable.ic_list_view); // Icon implies "Click to see List"

            btnSwitchView.setOnClickListener(v -> toggleView());
        }
    }

    private void toggleView() {
        if (isMapVisible) {
            // SWITCH TO LIST MODE
            mapContainer.setVisibility(View.GONE);
            rvFoodBanks.setVisibility(View.VISIBLE);

            // Update Icon to show "Map" (so user knows they can go back)
            btnSwitchView.setImageResource(R.drawable.ic_map_view);
            isMapVisible = false;
        } else {
            // SWITCH TO MAP MODE
            mapContainer.setVisibility(View.VISIBLE);
            rvFoodBanks.setVisibility(View.GONE);

            // Update Icon to show "List"
            btnSwitchView.setImageResource(R.drawable.ic_list_view);
            isMapVisible = true;
        }
    }

}
