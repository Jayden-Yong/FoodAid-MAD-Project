package com.example.foodaid_mad_project.HomeFragments;

import android.net.Uri;
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

import com.example.foodaid_mad_project.Model.FoodItem;
import com.example.foodaid_mad_project.R;

public class MapPinItemFragment extends Fragment {
    private FoodItem foodItem;

    public MapPinItemFragment() {
        // Required empty public constructor
    }

    public MapPinItemFragment(FoodItem item) {
        this.foodItem = item;
    }

    //
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_pin_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (foodItem == null)
            return; // Safety check

        // Find views
        TextView tvName = view.findViewById(R.id.selectedItemName);
        TextView tvLocation = view.findViewById(R.id.selectedItemLocation);
        TextView tvQuantity = view.findViewById(R.id.selectedItemQuantity);
        TextView tvDonator = view.findViewById(R.id.selectedItemDonator);
        ImageView ivImage = view.findViewById(R.id.selectedItemImage);
        ImageButton btnClose = view.findViewById(R.id.fragmentCloseButton);
        Button btnClaim = view.findViewById(R.id.selectedItemClaimButton);
        TextView tvDuration = view.findViewById(R.id.selectedItemPostDuration);

        // Calculate duration logic
        long diff = System.currentTimeMillis() - foodItem.getTimestamp();
        long mins = diff / (1000 * 60);
        String durationText = mins >= 60
                ? (mins / 60) + " hrs ago"
                : mins + " mins ago";

        tvDuration.setText(durationText);

        // Populate the views with food item data
        tvName.setText(foodItem.getTitle());
        tvLocation.setText(foodItem.getLocationName());
        tvQuantity.setText("Weight: " + foodItem.getWeight() + " kg");
        tvDonator.setText("By: " + foodItem.getDonatorName());

        // Handle image (URI or resource)
        if (foodItem.getImageUri() != null && !foodItem.getImageUri().isEmpty()) {
            ivImage.setImageURI(Uri.parse(foodItem.getImageUri()));
        } else {
            ivImage.setImageResource(R.drawable.ic_launcher_background); // fallback
        }

        // Close button: hides the fragment
        btnClose.setOnClickListener(v -> {
            View containerView = getParentFragment() != null
                    ? getParentFragment().getView().findViewById(R.id.MapPinFragment)
                    : null;
            if (containerView != null) {
                containerView.setVisibility(View.GONE);
            }
        });

        // Claim button: opens ItemDetailsFragment
        btnClaim.setOnClickListener(v -> {
            // Mock pickup times
            String[] mockTimes = { "10:00 AM", "6:00 PM" };

            // Create new fragment
            ItemDetailsFragment detailsFragment = new ItemDetailsFragment(
                    foodItem.getTitle(),
                    mockTimes,
                    R.id.radioPantry, // This ID might be wrong, passing int category
                    foodItem.getWeight(),
                    foodItem.getLocationName(),
                    foodItem.getDonatorName());

            // Replace current fragment in container
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.coveringFragment, detailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    // Vibe Coded map pin to use with Google Map
    // @Override
    // public void onViewCreated(@NonNull View view, @Nullable Bundle
    // savedInstanceState) {
    // super.onViewCreated(view, savedInstanceState);
    //
    // if (foodItem == null) return;
    //
    // TextView tvName = view.findViewById(R.id.selectedItemName);
    // TextView tvLocation = view.findViewById(R.id.selectedItemLocation);
    // TextView tvQuantity = view.findViewById(R.id.selectedItemQuantity);
    // TextView tvDonator = view.findViewById(R.id.selectedItemDonator);
    // ImageView ivImage = view.findViewById(R.id.selectedItemImage);
    // ImageButton btnClose = view.findViewById(R.id.fragmentCloseButton);
    // Button btnClaim = view.findViewById(R.id.selectedItemClaimButton);
    //
    // tvName.setText(foodItem.getName());
    // tvLocation.setText(foodItem.getLocation());
    // tvQuantity.setText("Qty: " + foodItem.getQuantity());
    // tvDonator.setText("By: " + foodItem.getDonator());
    //
    // // Handle Image: Check if it's Mock (ResID) or Real (URI)
    // if (foodItem.getImageUri() != null && !foodItem.getImageUri().isEmpty()) {
    // ivImage.setImageURI(Uri.parse(foodItem.getImageUri()));
    // } else if (foodItem.getImageResId() != 0) {
    // ivImage.setImageResource(foodItem.getImageResId());
    // } else {
    // ivImage.setImageResource(R.drawable.ic_launcher_background); // Fallback
    // }
    //
    // btnClose.setOnClickListener(v -> {
    // if (getParentFragment() != null && getParentFragment().getView() != null) {
    // View containerView =
    // getParentFragment().getView().findViewById(R.id.MapPinFragment);
    // if (containerView != null) containerView.setVisibility(View.GONE);
    // }
    // });
    //
    // btnClaim.setOnClickListener(v -> {
    // String[] mockTimes = {"10:00 AM", "6:00 PM"};
    //
    // // Try to parse quantity to integer safely
    // int qty = 0;
    // try {
    // String qtyString = foodItem.getQuantity().replaceAll("[^0-9]", ""); //
    // Extract numbers
    // if(!qtyString.isEmpty()) qty = Integer.parseInt(qtyString);
    // } catch (Exception e) { qty = 1; }
    //
    // ItemDetailsFragment detailsFragment = new ItemDetailsFragment(
    // foodItem.getName(),
    // mockTimes,
    // R.id.radioPantry,
    // qty,
    // foodItem.getLocation(),
    // foodItem.getDonator()
    // );
    //
    // // Use the correct container ID for your Activity
    // if (getActivity() != null) {
    // getActivity().getSupportFragmentManager().beginTransaction()
    // .replace(R.id.coveringFragment, detailsFragment)
    // .addToBackStack(null)
    // .commit();
    // }
    // });
    // }
}