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
                        .replace(R.id.ItemDetailsFragmentContainer, new ClaimNotifyFragment()) // Note: Ensure container
                                                                                               // ID is correct in main
                                                                                               // layout
                        .addToBackStack("ClaimSuccess")
                        .commit();
            });
        }
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
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
}
