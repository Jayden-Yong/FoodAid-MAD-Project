package com.example.foodaid_mad_project.HomeFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.foodaid_mad_project.Model.FoodBank;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.SharedViewModel;

public class MapPinItemFragment extends Fragment {
    private FoodBank foodBank;
    private SharedViewModel sharedViewModel;

    public MapPinItemFragment() {
        // Required empty public constructor
    }

    public MapPinItemFragment(FoodBank item) {
        this.foodBank = item;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_pin_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (foodBank == null)
            return; // Safety check

        // Initialize ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Find views
        TextView tvName = view.findViewById(R.id.selectedItemName);
        TextView tvLocation = view.findViewById(R.id.selectedItemLocation);
        TextView tvQuantity = view.findViewById(R.id.selectedItemQuantity);
        TextView tvDonator = view.findViewById(R.id.selectedItemDonator);
        ImageView ivImage = view.findViewById(R.id.selectedItemImage);
        ImageButton btnClose = view.findViewById(R.id.fragmentCloseButton);
        Button btnClaim = view.findViewById(R.id.selectedItemClaimButton);

        // Populate the views with FoodBank data
        tvName.setText(foodBank.getName());
        tvLocation.setText(foodBank.getLocation());
        tvQuantity.setText("Category: " + foodBank.getCategory()); // Using Category as Type legacy replacement
        tvDonator.setText("Hours: " + foodBank.getFormattedOperatingHours()); // Using Hours as Donator proxy

        // Handle image using Glide
        if (foodBank.getImageUrl() != null && !foodBank.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(foodBank.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivImage);
        } else {
            ivImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // Close button: hides the fragment
        btnClose.setOnClickListener(v -> {
            View containerView = getParentFragment() != null && getParentFragment().getView() != null
                    ? getParentFragment().getView().findViewById(R.id.MapPinFragment)
                    : null;
            if (containerView != null) {
                containerView.setVisibility(View.GONE);
            }
        });

        // Claim button: Use SharedViewModel to trigger navigation in HomeFragment
        btnClaim.setOnClickListener(v -> {
            // Select the item in ViewModel. HomeFragment observes this and will navigate.
            sharedViewModel.selectFoodBank(foodBank);
        });
    }
}
