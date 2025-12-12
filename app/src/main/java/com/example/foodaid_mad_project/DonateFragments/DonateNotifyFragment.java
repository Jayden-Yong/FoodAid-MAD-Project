package com.example.foodaid_mad_project.DonateFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.HomeFragments.HomeFragment;
import com.example.foodaid_mad_project.HomeFragments.ItemDetailsFragment;
import com.example.foodaid_mad_project.MainActivity;
import com.example.foodaid_mad_project.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class DonateNotifyFragment extends DialogFragment {

    private MaterialButton btnViewItem, btnBackToHome;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donate_notify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnViewItem = view.findViewById(R.id.btnViewItem);
        btnBackToHome = view.findViewById(R.id.btnBackToHome);

        //TODO: Fix the code. The button only goes back to home and not continue to the item details fragment.
        btnViewItem.setOnClickListener(v -> {
            dismiss();
            getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.coveringFragment, new ItemDetailsFragment())
                    .addToBackStack("ItemDetails")
                    .commit();
        });

        btnBackToHome.setOnClickListener(v -> {
            dismiss();
            getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });
    }
}
