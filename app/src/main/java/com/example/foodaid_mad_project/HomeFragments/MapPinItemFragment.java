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

        // Contextually combine
        String qtyText = "Qty: " + foodItem.getQuantity() + " • " + foodItem.getWeight() + " kg";
        tvQuantity.setText(qtyText);

        // tvWeight & tvDonator removed from UI to save space

        if (foodItem.getImageUri() != null && !foodItem.getImageUri().isEmpty()) {
            String imageStr = foodItem.getImageUri();
            if (imageStr.startsWith("http")) {
                com.bumptech.glide.Glide.with(getContext())
                        .load(imageStr)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivImage);
            } else {
                try {
                    byte[] imageBytes = com.example.foodaid_mad_project.Utils.ImageUtil.base64ToBytes(imageStr);
                    com.bumptech.glide.Glide.with(getContext())
                            .asBitmap()
                            .load(imageBytes)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .into(ivImage);
                } catch (Exception e) {
                    ivImage.setImageResource(R.drawable.ic_launcher_background);
                }
            }
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
            // Create new fragment with the FoodItem object
            ItemDetailsFragment detailsFragment = new ItemDetailsFragment(foodItem);

            // Replace current fragment in container
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.coveringFragment, detailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Setup Realtime Listener
        setupRealtimeUpdates(tvQuantity, btnClaim);
    }

    private com.google.firebase.firestore.ListenerRegistration pinListener;

    private void setupRealtimeUpdates(TextView tvQuantity, Button btnClaim) {
        if (foodItem == null || foodItem.getDonationId() == null)
            return;

        pinListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("donations")
                .document(foodItem.getDonationId())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null)
                        return;
                    if (snapshot != null && snapshot.exists()) {
                        FoodItem updatedItem = snapshot.toObject(FoodItem.class);
                        if (updatedItem != null) {
                            updatedItem.setDonationId(snapshot.getId());
                            this.foodItem = updatedItem;

                            // Update UI
                            String qtyText = "Qty: " + foodItem.getQuantity() + " • " + foodItem.getWeight() + " kg";
                            tvQuantity.setText(qtyText);

                            if (foodItem.getQuantity() <= 0) {
                                btnClaim.setEnabled(false);
                                tvQuantity.setText("Status: CLAIMED");
                            }
                        }
                    } else {
                        // Removed
                        if (getView() != null)
                            getView().setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pinListener != null) {
            pinListener.remove();
        }
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