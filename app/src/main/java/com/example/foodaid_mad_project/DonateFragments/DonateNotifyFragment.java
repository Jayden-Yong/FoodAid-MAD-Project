package com.example.foodaid_mad_project.DonateFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.HomeFragments.ItemDetailsFragment;
import com.example.foodaid_mad_project.Model.FoodItem;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.HomeFragments.HomeFragment;
import com.google.android.material.button.MaterialButton;

/**
 * <h1>DonateNotifyFragment</h1>
 * <p>
 * Displays a success message after a donation has been posted.
 * Allows the user to view the item they just created (simulated) or return to
 * Home.
 * </p>
 */
public class DonateNotifyFragment extends Fragment {

    private String title;
    private String[] pickupTime; // Legacy field, kept for compatibility if needed
    private int category;
    private double weight;
    private String location;
    private String donator;
    private String imageUri;
    private String description;
    private String dateString;

    public DonateNotifyFragment() {
        // Required empty public constructor
    }

    public DonateNotifyFragment(String title, String[] pickupTime, int category, double weight, String location,
            String donator, String imageUri, String description, String dateString) {
        this.title = title;
        this.pickupTime = pickupTime;
        this.category = category;
        this.weight = weight;
        this.location = location;
        this.donator = donator;
        this.imageUri = imageUri;
        this.description = description;
        this.dateString = dateString;
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

        MaterialButton btnViewItem = view.findViewById(R.id.btnViewItem);
        MaterialButton btnBackToHome = view.findViewById(R.id.btnBackToHome);
        TextView tvDonationDate = view.findViewById(R.id.tvDonationDate);

        // Display formatted date if valid
        if (dateString != null && tvDonationDate != null) {
            tvDonationDate.setText("Date: " + dateString);
            tvDonationDate.setVisibility(View.VISIBLE);
        } else if (tvDonationDate != null) {
            tvDonationDate.setVisibility(View.GONE);
        }

        // View Item: Reconstruct a transient FoodItem object to show details
        // immediately
        btnViewItem.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);

            String catString = (category == 1) ? "MEALS" : "GROCERIES"; // 1 was Meal ID in logic

            // NOTE: ID is null because we don't have the Document ID here from Firestore's
            // .add() result easily passed yet
            // This is a "preview" view. Real data is on the server.
            FoodItem item = new FoodItem(
                    null, null, donator, title, catString, "MEET_UP", location,
                    0, 0, imageUri, weight, 1, 0, 0, "AVAILABLE", null, System.currentTimeMillis(), description);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.coveringFragment, new ItemDetailsFragment(item))
                    .addToBackStack("ItemDetail")
                    .commit();
        });

        // Back to Home
        btnBackToHome.setOnClickListener(v -> {
            // Clear back stack to return to root
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);

            // Optionally ensure we are on HomeFragment (if not already handled by pop)
            // This logic relies on FragmentManager state, which should be correct.
        });
    }
}