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

public class ItemDetailsFragment extends Fragment {

    private String title;
    private String[] pickupTime;
    private int category;
    private int quantity;
    private String location;
    private String donator;
    private String imageUri;


    private TextView tvProductTitle, tvPickupTime, tvQuantity, tvLocationLabel, tvPostedBy;
    ImageView ivProductImage;
    private RadioGroup radioGroupCategory;


    public ItemDetailsFragment() {}

    // Updated Constructor
    public ItemDetailsFragment(String title, String[] pickupTime, int category, int quantity, String location, String donator, String imageUri){
        this.title = title;
        this.pickupTime = pickupTime;
        this.category = category;
        this.quantity = quantity;
        this.location = location;
        this.donator = donator;
        this.imageUri = imageUri;
    }

    public ItemDetailsFragment(String title, String[] pickupTime, int category, int quantity, String location, String donator){
        this(title, pickupTime, category, quantity, location, donator, null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvProductTitle = view.findViewById(R.id.tvProductTitle);
        tvPickupTime = view.findViewById(R.id.tvPickupTime);
        tvQuantity = view.findViewById(R.id.tvQuantity);
        tvLocationLabel = view.findViewById(R.id.tvLocationLabel);
        tvPostedBy = view.findViewById(R.id.tvPostedBy);
        ivProductImage = view.findViewById(R.id.ivProductImage);
        radioGroupCategory = view.findViewById(R.id.radioGroupCategory);

        tvProductTitle.setText(getString(R.string.Food_Name, title));
        if(pickupTime != null && pickupTime.length >= 2) {
            tvPickupTime.setText(getString(R.string.Pickup_Time, pickupTime[0], pickupTime[1]));
        }
        tvQuantity.setText(getString(R.string.Food_Quantity, quantity));
        tvLocationLabel.setText(getString(R.string.Food_Location, location));
        tvPostedBy.setText(getString(R.string.Food_Donator, donator));
        if (category != 0) radioGroupCategory.check(category);

        if (imageUri != null) {
            ivProductImage.setImageURI(Uri.parse(imageUri));
        }

        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText(getString(R.string.String, "Food Aid Details"));

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Manually pop the Donate Fragment
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("ItemDetail", FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
            btnClaim.setOnClickListener(v -> {
                //TODO: Save data to Firebase

                //TODO: Call Success Dialog
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.ItemDetailsFragmentContainer, new ClaimNotifyFragment())
                        .addToBackStack("ClaimSuccess")
                        .commit();
            });
        }
    }
}
