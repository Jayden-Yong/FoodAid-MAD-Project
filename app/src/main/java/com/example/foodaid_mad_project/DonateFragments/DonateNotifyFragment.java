package com.example.foodaid_mad_project.DonateFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.HomeFragments.ItemDetailsFragment;
import com.example.foodaid_mad_project.R;
import com.google.android.material.button.MaterialButton;

public class DonateNotifyFragment extends Fragment {

    private String title;
    private String[] pickupTime;
    private int category;
    private int quantity;
    private String location;
    private String donator;
    private String imageUri;

    public DonateNotifyFragment() {}

    public DonateNotifyFragment(String title, String[] pickupTime, int category, int quantity, String location, String donator, String imageUri){
        this.title = title;
        this.pickupTime = pickupTime;
        this.category = category;
        this.quantity = quantity;
        this.location = location;
        this.donator = donator;
        this.imageUri = imageUri; // Save it
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donate_notify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnViewItem = view.findViewById(R.id.btnViewItem);
        MaterialButton btnBackToHome = view.findViewById(R.id.btnBackToHome);

        btnViewItem.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.coveringFragment, new ItemDetailsFragment(title, pickupTime, category, quantity, location, donator, imageUri))
                    .addToBackStack("ItemDetail")
                    .commit();
        });

        btnBackToHome.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });
    }
}