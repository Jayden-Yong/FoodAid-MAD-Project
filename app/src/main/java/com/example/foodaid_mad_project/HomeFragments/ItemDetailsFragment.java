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

        // Rate Button Logic
        com.google.android.material.button.MaterialButton btnRate = view.findViewById(R.id.btnRate);
        if (btnRate != null) {
            btnRate.setOnClickListener(v -> showRatingDialog(sharedViewModel.getSelectedFoodBank().getValue()));
        }
    }

    private void showRatingDialog(FoodBank foodBank) {
        if (foodBank == null)
            return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null); // We will create this layout
                                                                                     // dynamically if needed or just
                                                                                     // build it programmatically to
                                                                                     // avoid creating new file

        // Simpler approach: Build view programmatically to avoid creating multiple
        // files
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final android.widget.RatingBar ratingBar = new android.widget.RatingBar(getContext());
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1);
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = android.view.Gravity.CENTER;
        ratingBar.setLayoutParams(lp);

        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Leave a review...");
        layout.addView(ratingBar);
        layout.addView(input);

        builder.setView(layout);
        builder.setTitle("Rate " + foodBank.getName());
        builder.setPositiveButton("Submit", (dialog, which) -> {
            float rating = ratingBar.getRating();
            String comment = input.getText().toString();
            submitReview(foodBank.getId(), rating, comment);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void submitReview(String foodBankId, float rating, String comment) {
        String userId = "anonymous";
        if (com.example.foodaid_mad_project.UserManager.getInstance().getUser() != null) {
            userId = com.example.foodaid_mad_project.UserManager.getInstance().getUser().getUid();
        }

        java.util.Map<String, Object> review = new java.util.HashMap<>();
        review.put("userId", userId);
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("foodbanks").document(foodBankId).collection("reviews")
                .add(review)
                .addOnSuccessListener(docRef -> android.widget.Toast
                        .makeText(getContext(), "Review Submitted!", android.widget.Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> android.widget.Toast
                        .makeText(getContext(), "Error: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show());
    }

    private void populateUI(FoodBank foodBank) {
        tvProductTitle.setText(getString(R.string.Food_Name, foodBank.getName()));

        // Map "Operating Hours" -> "Pickup Time"
        // If string formatting is strict in strings.xml, we use basic setting or try to
        // match
        // Assuming strings.xml uses format args, we pass values.
        // If OperatingHours is a single string, we might need to conform to "From: %s
        // To: %s" if that's what XML expects.
        // For now, I'll assume passing the whole string or split if possible.
        // Simpler: Just set text directly to avoid format errors if strict.
        tvPickupTime.setText("Time: " + foodBank.getOperatingHours());

        // Mapping Type to Category Radio Button (Visual only)
        if (foodBank.getType().equalsIgnoreCase("Food Pantry")) {
            radioGroupCategory.check(R.id.radioButtonHalal); // Example mapping
        } else {
            radioGroupCategory.check(R.id.radioButtonNonHalal);
        }

        // Quantity: FoodBanks don't have "Quantity" in the same way as single items.
        // We'll show "Available" or a generic message.
        tvQuantity.setText("Status: Open");

        tvLocationLabel.setText(getString(R.string.Food_Location, foodBank.getAddress()));
        tvPostedBy.setText(getString(R.string.Food_Donator, foodBank.getOwnerId()));

        // Setup Bookmarking
        if (getView() != null) {
            setupFavouriteButton(getView(), foodBank);
        }
    }

    private void setupFavouriteButton(View view, FoodBank foodBank) {
        android.widget.ImageButton btnFavourite = view.findViewById(R.id.btnFavourite);
        if (btnFavourite == null)
            return;

        // Get Current User
        com.example.foodaid_mad_project.AuthFragments.User user = com.example.foodaid_mad_project.UserManager
                .getInstance().getUser();

        if (user == null || foodBank.getId() == null)
            return;

        // 1. Initial State Check
        boolean isFav = false;
        if (user.getFavourites() != null && user.getFavourites().contains(foodBank.getId())) {
            isFav = true;
            btnFavourite.setImageResource(R.drawable.ic_favourite_check);
        } else {
            btnFavourite.setImageResource(R.drawable.ic_favourite_uncheck);
        }

        // 2. Click Listener
        btnFavourite.setOnClickListener(v -> {
            boolean currentFavState = false;
            if (user.getFavourites() != null && user.getFavourites().contains(foodBank.getId())) {
                currentFavState = true;
            }

            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore
                    .getInstance();
            if (currentFavState) {
                // Remove
                db.collection("users").document(user.getUid())
                        .update("favourites", com.google.firebase.firestore.FieldValue.arrayRemove(foodBank.getId()));

                // Update Local User Object immediatley for responsiveness
                if (user.getFavourites() != null)
                    user.getFavourites().remove(foodBank.getId());
                btnFavourite.setImageResource(R.drawable.ic_favourite_uncheck);
            } else {
                // Add
                db.collection("users").document(user.getUid())
                        .update("favourites", com.google.firebase.firestore.FieldValue.arrayUnion(foodBank.getId()));

                // Update Local User Object
                if (user.getFavourites() == null)
                    user.setFavourites(new java.util.ArrayList<>());
                user.getFavourites().add(foodBank.getId());
                btnFavourite.setImageResource(R.drawable.ic_favourite_check);
            }
        });
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
}
