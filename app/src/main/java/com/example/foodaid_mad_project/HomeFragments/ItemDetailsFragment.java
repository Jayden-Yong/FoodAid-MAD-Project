package com.example.foodaid_mad_project.HomeFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodaid_mad_project.DonateFragments.DonateNotifyFragment;
import com.example.foodaid_mad_project.Model.FoodBank;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.SharedViewModel;

public class ItemDetailsFragment extends Fragment {

    private TextView tvProductTitle, tvPickupTime, tvQuantity, tvLocationLabel, tvPostedBy;
    private RadioGroup radioGroupCategory;
    private SharedViewModel sharedViewModel;

    // Default constructor is sufficient now
    public ItemDetailsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Views
        tvProductTitle = view.findViewById(R.id.tvProductTitle);
        tvPickupTime = view.findViewById(R.id.tvPickupTime);
        tvQuantity = view.findViewById(R.id.tvQuantity);
        tvLocationLabel = view.findViewById(R.id.tvLocationLabel);
        tvPostedBy = view.findViewById(R.id.tvPostedBy);
        radioGroupCategory = view.findViewById(R.id.radioGroupCategory);
        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        Toolbar toolbar = view.findViewById(R.id.Toolbar);
        Button btnClaim = view.findViewById(R.id.btnClaim);

        // Setup Toolbar
        toolBarTitle.setText(getString(R.string.String, "Food Aid Details"));

        // Setup ViewModel Observation
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getSelectedFoodBank().observe(getViewLifecycleOwner(), foodBank -> {
            if (foodBank != null) {
                populateUI(foodBank);
            }
        });

        // Backend Navigation (Handle Hardware Back Button)
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateBack();
                    }
                });

        // Toolbar Navigation (Handle Toolbar Back Arrow)
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> navigateBack());
        }

        // Claim Button Logic
        if (btnClaim != null) {
            btnClaim.setOnClickListener(v -> {
                // TODO: Save data to Firebase (Claim Action)

                // Show Success Notification
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.ItemDetailsFragmentContainer, new ClaimNotifyFragment())
                        .addToBackStack("ClaimSuccess")
                        .commit();
            });
        }

    }

    private void populateUI(FoodBank foodBank) {
        tvProductTitle.setText(foodBank.getName()); // Just Name

        // Show Operating Hours or Pickup Times
        String timeInfo = "Time: ";
        if (foodBank.getOperatingHours() != null && foodBank.getOperatingHours().containsKey("Time")) {
            timeInfo += foodBank.getOperatingHours().get("Time");
        } else {
            timeInfo += "Flexible";
        }
        tvPickupTime.setText(timeInfo);

        // Category mapping
        if (foodBank.getCategory() != null) {
            if (foodBank.getCategory().equalsIgnoreCase("Menu Rahmah"))
                radioGroupCategory.check(R.id.radioPantry); // Reuse ID for now or need to update XML IDs to match if I
                                                            // care about visual toggle
            else if (foodBank.getCategory().equalsIgnoreCase("Event"))
                radioGroupCategory.check(R.id.radioFreeTable);
            else
                radioGroupCategory.check(R.id.radioLeftover);
        }

        // Quantity logic
        String status = "Quantity: " + foodBank.getQuantity();
        if (foodBank.getPrice() > 0) {
            status += String.format(" | RM %.2f", foodBank.getPrice());
        }
        tvQuantity.setText(status);

        tvLocationLabel.setText(foodBank.getAddress());
        // Show Notes/Desc
        if (foodBank.getNotes() != null && !foodBank.getNotes().isEmpty()) {
            tvPostedBy.setText("Notes: " + foodBank.getNotes());
        } else {
            tvPostedBy.setText("Owner: " + foodBank.getOwnerId());
        }

        // Setup Bookmarking
        // Bookmarking removed as per requirements

        // Update Claim Button Text
        Button btnClaim = getView().findViewById(R.id.btnClaim);
        if (btnClaim != null) {
            btnClaim.setText("Collect Request");
            btnClaim.setOnClickListener(v -> showCollectDialog(foodBank));
        }
    }

    private void showCollectDialog(FoodBank foodBank) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Collect Item");
        builder.setMessage("Enter quantity to collect (Max: " + foodBank.getQuantity() + ")");

        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String qtyStr = input.getText().toString();
            if (!qtyStr.isEmpty()) {
                int qtyToCollect = Integer.parseInt(qtyStr);
                processCollection(foodBank, qtyToCollect);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void processCollection(FoodBank foodBank, int qtyToCollect) {
        if (qtyToCollect <= 0 || qtyToCollect > foodBank.getQuantity()) {
            android.widget.Toast.makeText(getContext(), "Invalid Quantity", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        int newQuantity = foodBank.getQuantity() - qtyToCollect;
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore
                .getInstance();

        if (newQuantity == 0) {
            // Item finished -> Prompt Review -> Delete
            showRatingDialog(foodBank, db);
        } else {
            // Update Quantity
            db.collection("foodbanks").document(foodBank.getId())
                    .update("quantity", newQuantity)
                    .addOnSuccessListener(aVoid -> {
                        android.widget.Toast
                                .makeText(getContext(), "Collected Successfully!", android.widget.Toast.LENGTH_SHORT)
                                .show();
                        getParentFragmentManager().popBackStack(); // Go back
                    });
        }
    }

    private void showRatingDialog(FoodBank foodBank, com.google.firebase.firestore.FirebaseFirestore db) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Rate this Item");

        // Simple Layout for Rating
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        android.widget.RatingBar ratingBar = new android.widget.RatingBar(getContext());
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1);
        ratingBar.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));

        layout.addView(ratingBar);
        builder.setView(layout);

        builder.setPositiveButton("Submit & Finish", (dialog, which) -> {
            // Delete Item regardless of rating for now (as per requirement to delete on 0)
            // Ideally we save the rating to the User/Owner profile but for this scope we
            // just acknowledge it.
            float rating = ratingBar.getRating();
            android.widget.Toast.makeText(getContext(), "Function Rating: " + rating, android.widget.Toast.LENGTH_SHORT)
                    .show();

            // Delete
            db.collection("foodbanks").document(foodBank.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        android.widget.Toast
                                .makeText(getContext(), "Item Completed & Removed!", android.widget.Toast.LENGTH_SHORT)
                                .show();
                        getParentFragmentManager().popBackStack();
                    });
        });

        builder.setCancelable(false); // Must rate or acknowledge
        builder.show();
    }

    // setupFavouriteButton removed as per requirements

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
}
