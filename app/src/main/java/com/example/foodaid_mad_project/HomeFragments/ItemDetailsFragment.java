package com.example.foodaid_mad_project.HomeFragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.DonateFragments.DonateNotifyFragment;
import com.example.foodaid_mad_project.Model.FoodItem;
import com.example.foodaid_mad_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.Toast;

public class ItemDetailsFragment extends Fragment {

    private FoodItem foodItem;

    private TextView tvProductTitle, tvPickupTime, tvQuantity, tvLocationLabel, tvPostedBy;
    private ImageView ivProductImage;
    private RadioGroup radioGroupCategory;

    public ItemDetailsFragment() {
        // Required empty public constructor
    }

    public ItemDetailsFragment(FoodItem item) {
        this.foodItem = item;
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

        if (foodItem == null)
            return;

        tvProductTitle = view.findViewById(R.id.tvProductTitle);
        tvPickupTime = view.findViewById(R.id.tvPickupTime);
        tvQuantity = view.findViewById(R.id.tvQuantity);
        tvLocationLabel = view.findViewById(R.id.tvLocationLabel);
        tvPostedBy = view.findViewById(R.id.tvPostedBy);
        ivProductImage = view.findViewById(R.id.ivProductImage);
        radioGroupCategory = view.findViewById(R.id.radioGroupCategory);

        tvProductTitle.setText(getString(R.string.Food_Name, foodItem.getTitle()));

        // Convert timestamps to readable time if needed, or just show duration/range
        // For simplicity, showing a generic message or formatting dates.
        // Assuming user wants start/end time formatted.
        // Simple placeholder for now as per previous logic which used string array.
        // Let's format the start/end time.
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String startStr = sdf.format(new Date(foodItem.getStartTime()));
        String endStr = sdf.format(new Date(foodItem.getEndTime()));
        tvPickupTime.setText(getString(R.string.Pickup_Time, startStr, endStr));

        tvQuantity.setText("Weight: " + foodItem.getWeight() + " kg");
        tvLocationLabel.setText(getString(R.string.Food_Location, foodItem.getLocationName()));
        tvPostedBy.setText(getString(R.string.Food_Donator, foodItem.getDonatorName()));

        // Category check
        if (foodItem.getCategory() != null) {
            // Check based on string
            if (foodItem.getCategory().equalsIgnoreCase("GROCERIES")) {
                radioGroupCategory.check(R.id.radioGroceries);
            } else if (foodItem.getCategory().equalsIgnoreCase("MEALS")) {
                radioGroupCategory.check(R.id.radioMeals);
            }
        }

        if (foodItem.getImageUri() != null) {
            ivProductImage.setImageURI(Uri.parse(foodItem.getImageUri()));
        }

        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText(getString(R.string.String, "Food Aid Details"));

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack("ItemDetail",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        }
                    }
                });

        Toolbar toolbar = view.findViewById(R.id.Toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("ItemDetail", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            });
        }

        Button btnClaim = view.findViewById(R.id.btnClaim);
        if (btnClaim != null) {
            btnClaim.setOnClickListener(v -> claimDonation());
        }
    }

    private void claimDonation() {
        if (foodItem.getDonationId() == null) {
            Toast.makeText(getContext(), "Error: Invalid Item ID", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("donations").document(foodItem.getDonationId());

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef);
            String status = snapshot.getString("status");
            Long endTime = snapshot.getLong("endTime");
            long currentTime = System.currentTimeMillis();

            if (status != null && status.equals("AVAILABLE") &&
                    endTime != null && endTime > currentTime) {

                transaction.update(docRef, "status", "CLAIMED");

                String uid = FirebaseAuth.getInstance().getUid();
                transaction.update(docRef, "claimedBy", uid);

                return null; // Success
            } else {
                throw new FirebaseFirestoreException("Item unavailable or expired",
                        FirebaseFirestoreException.Code.ABORTED);
            }
        }).addOnSuccessListener(result -> {
            // Navigate to Success
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.ItemDetailsFragmentContainer, new ClaimNotifyFragment())
                    .addToBackStack("ClaimSuccess")
                    .commit();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Claim failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
