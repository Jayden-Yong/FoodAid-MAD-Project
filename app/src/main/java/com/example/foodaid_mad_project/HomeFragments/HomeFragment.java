package com.example.foodaid_mad_project.HomeFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.AuthFragments.User;
import com.example.foodaid_mad_project.Model.FoodBank;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.SharedViewModel;
import com.example.foodaid_mad_project.UserManager;
import com.example.foodaid_mad_project.Adapter.FoodBankAdapter;
import com.google.android.material.chip.ChipGroup;

public class HomeFragment extends Fragment {

    // UI Elements
    private TextView tvWelcomeUser;
    private ImageButton btnToNotification;
    private ImageButton btnSwitchView;
    private RecyclerView recyclerView;
    private FragmentContainerView mapContainer;
    private ChipGroup chipGroupFilters;
    private FragmentContainerView mapPinContainer;

    // Logic
    private SharedViewModel sharedViewModel;
    private FoodBankAdapter adapter;
    private boolean isListView = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Setup Logic & ViewModel
        setupWelcomeMessage(view);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // 2. Setup Views
        btnToNotification = view.findViewById(R.id.btnToNotification);

        btnSwitchView = view.findViewById(R.id.btnSwitchView);
        recyclerView = view.findViewById(R.id.rvFoodBanks);
        mapContainer = view.findViewById(R.id.MapFragment);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        mapPinContainer = view.findViewById(R.id.MapPinFragment);

        // 3. Setup RecyclerView
        setupRecyclerView();

        // 4. Setup Map (Replace container with MapFragment OSM)
        getChildFragmentManager().beginTransaction()
                .replace(R.id.MapFragment, new MapFragment())
                .commit();

        // 5. Setup Listeners
        setupListeners();

        // 6. Observe Data
        sharedViewModel.getFoodBanks().observe(getViewLifecycleOwner(), list -> {
            adapter.setData(list);
            // MapFragment also observes this automatically
        });

        // Observe Selection for Navigation
        sharedViewModel.getSelectedFoodBank().observe(getViewLifecycleOwner(), foodBank -> {
            if (foodBank != null) {
                // Navigate to Full Details Page
                // Corrected constructor usage
                ItemDetailsFragment detailsFragment = new ItemDetailsFragment();
                // We don't have a constructor with FoodBank, passing via ViewModel is correct
                // SharedViewModel already holds SelectedFoodBank
                // But if we wanted to pass arguments (not recommended if using ViewModel):
                /*
                 * Bundle args = new Bundle();
                 * args.putParcelable("foodBank", foodBank);
                 * detailsFragment.setArguments(args);
                 */

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.coveringFragment, detailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void setupWelcomeMessage(View view) {
        tvWelcomeUser = view.findViewById(R.id.tvWelcomeUser);
        try {
            User user = UserManager.getInstance().getUser();
            String welcomeDisplay = "Guest";
            if (user != null) {
                String email = user.getEmail();
                if (user.getName() != null) {
                    welcomeDisplay = user.getName();
                } else if (email != null) {
                    welcomeDisplay = email.substring(0, email.indexOf("@")).toUpperCase();
                }
            }
            tvWelcomeUser.setText(getString(R.string.Welcome_User, welcomeDisplay));
        } catch (Exception e) {
            tvWelcomeUser.setText("Hello Guest");
        }
    }

    private void setupRecyclerView() {
        adapter = new FoodBankAdapter(getContext());
        // On Item Click in List -> Select it
        adapter.setOnItemClickListener(foodBank -> {
            sharedViewModel.selectFoodBank(foodBank);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {

        // Notification Button
        if (btnToNotification != null) {
            btnToNotification.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.coveringFragment, new NotificationFragment())
                        .addToBackStack("NotificationFragment")
                        .commit();
            });
        }

        // Toggle View Button
        btnSwitchView.setOnClickListener(v -> {
            isListView = !isListView;
            if (isListView) {
                // Show List, Hide Map
                mapContainer.setVisibility(View.GONE);
                mapPinContainer.setVisibility(View.GONE); // Hide overlay if switching to list
                recyclerView.setVisibility(View.VISIBLE);
                btnSwitchView.setImageResource(R.drawable.ic_map_view); // Change icon to map
            } else {
                // Show Map, Hide List
                recyclerView.setVisibility(View.GONE);
                mapContainer.setVisibility(View.VISIBLE);
                btnSwitchView.setImageResource(R.drawable.ic_list_view); // Change icon to list
            }
        });

        // Filter Chips
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            String filter = "All";
            if (checkedId == R.id.chipMenuRahmah)
                filter = "Menu Rahmah";
            else if (checkedId == R.id.chipCafeLeftover)
                filter = "Leftover";
            else if (checkedId == R.id.chipEventLeftover)
                filter = "Event";

            sharedViewModel.applyFilter(filter);
        });
    }

    // Called by MapFragment when a pin is clicked
    public void showMapPinDetails(FoodBank fb) {
        if (mapPinContainer != null) {
            mapPinContainer.setVisibility(View.VISIBLE);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.MapPinFragment, new MapPinItemFragment(fb))
                    .commit();
        }
    }
}
