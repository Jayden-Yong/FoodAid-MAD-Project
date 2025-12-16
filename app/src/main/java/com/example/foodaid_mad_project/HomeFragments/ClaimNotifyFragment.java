package com.example.foodaid_mad_project.HomeFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.R;
import com.google.android.material.button.MaterialButton;

public class ClaimNotifyFragment extends Fragment {
    private MaterialButton btnViewQr, btnBackToHome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_claim_successful_notify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnViewQr = view.findViewById(R.id.btnViewQr);
        btnBackToHome = view.findViewById(R.id.btnBackToHome);

        btnViewQr.setOnClickListener(v -> {
            // QR Functionality removed, redirecting to home/details or showing toast
            android.widget.Toast.makeText(getContext(), "QR Feature Disabled", android.widget.Toast.LENGTH_SHORT)
                    .show();
            // Optional: Auto-close
            getParentFragmentManager().popBackStack("ItemDetail", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });

        btnBackToHome.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack("ItemDetail", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });
    }
}
