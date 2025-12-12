package com.example.foodaid_mad_project.DonateFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.R;

public class DonateFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText(getString(R.string.String, "Donate Food"));

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Manually pop the Donate Fragment
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });

        Toolbar toolbar = view.findViewById(R.id.Toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            });
        }

        // --- 3. Handle Donate Button ---
        Button btnDonate = view.findViewById(R.id.btnDonate);
        if (btnDonate != null) {
            btnDonate.setOnClickListener(v -> {
                //TODO: Save data to Firebase

                //TODO: Call Success Dialog
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.DonateFragmentContainer, new DonateNotifyFragment())
                        .addToBackStack("DonateSuccess")
                        .commit();

//                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
//                    getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
//                }
            });
        }
    }
}