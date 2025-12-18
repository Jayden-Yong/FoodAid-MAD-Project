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

import com.bumptech.glide.Glide;
import com.example.foodaid_mad_project.Model.FoodItem;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.Utils.ImageUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * <h1>MapPinItemFragment</h1>
 * <p>
 * Displays a summary card for a selected map pin (FoodItem).
 * Appears as an overlay on the map.
 * Allows the user to view basic details (Qty, Duration) and click "Claim" to
 * see full details.
 * </p>
 */
public class MapPinItemFragment extends Fragment {

    private FoodItem foodItem;
    private ListenerRegistration pinListener;

    public MapPinItemFragment() {
        // Required empty public constructor
    }

    public MapPinItemFragment(FoodItem item) {
        this.foodItem = item;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_pin_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (foodItem == null)
            return;

        // 1. Initialize Views
        TextView tvName = view.findViewById(R.id.selectedItemName);
        TextView tvLocation = view.findViewById(R.id.selectedItemLocation);
        TextView tvQuantity = view.findViewById(R.id.selectedItemQuantity);
        ImageView ivImage = view.findViewById(R.id.selectedItemImage);
        TextView tvDuration = view.findViewById(R.id.selectedItemPostDuration);
        ImageButton btnClose = view.findViewById(R.id.fragmentCloseButton);
        Button btnClaim = view.findViewById(R.id.selectedItemClaimButton);

        // 2. Bind Data
        tvName.setText(foodItem.getTitle());
        tvLocation.setText(foodItem.getLocationName());

        String qtyText = "Qty: " + foodItem.getQuantity() + " • " + foodItem.getWeight() + " kg";
        tvQuantity.setText(qtyText);

        // Duration Logic (e.g. "2 hours ago")
        long diff = System.currentTimeMillis() - foodItem.getTimestamp();
        long mins = diff / (1000 * 60);
        String durationText = mins >= 60
                ? (mins / 60) + " hrs ago"
                : mins + " mins ago";
        tvDuration.setText(durationText);

        // Image Loading (URL or Base64)
        loadImage(ivImage, foodItem.getImageUri());

        // 3. Setup Actions
        // Close Button
        btnClose.setOnClickListener(v -> closeFragment());

        // Claim/View Button
        btnClaim.setOnClickListener(v -> openItemDetails());

        // 4. Real-time updates (in case item gets claimed while looking)
        setupRealtimeUpdates(tvQuantity, btnClaim);
    }

    private void loadImage(ImageView ivImage, String imageStr) {
        if (imageStr != null && !imageStr.isEmpty()) {
            if (imageStr.startsWith("http")) {
                // URL Loading
                Glide.with(this)
                        .load(imageStr)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivImage);
            } else {
                // Base 64 Loading
                try {
                    byte[] imageBytes = ImageUtil.base64ToBytes(imageStr);
                    Glide.with(this)
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
            ivImage.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private void openItemDetails() {
        ItemDetailsFragment detailsFragment = new ItemDetailsFragment(foodItem);
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.coveringFragment, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void closeFragment() {
        if (getParentFragment() != null && getParentFragment().getView() != null) {
            View containerView = getParentFragment().getView().findViewById(R.id.MapPinFragment);
            if (containerView != null) {
                containerView.setVisibility(View.GONE);
            }
        }
    }

    private void setupRealtimeUpdates(TextView tvQuantity, Button btnClaim) {
        if (foodItem == null || foodItem.getDonationId() == null)
            return;

        pinListener = FirebaseFirestore.getInstance()
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

                            if (foodItem.getQuantity() <= 0 || !"AVAILABLE".equals(foodItem.getStatus())) {
                                btnClaim.setEnabled(false);
                                btnClaim.setText("Claimed");
                                tvQuantity.setText("Status: CLAIMED");
                            }
                        }
                    } else {
                        // Document deleted -> Hide self
                        closeFragment();
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
}