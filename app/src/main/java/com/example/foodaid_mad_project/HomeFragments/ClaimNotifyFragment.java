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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Reuse generic notify layout
        return inflater.inflate(R.layout.fragment_donate_notify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Views from fragment_donate_notify.xml
        android.widget.TextView tvTitle = view.findViewById(R.id.tvClaimTitle);
        android.widget.TextView tvGuide = view.findViewById(R.id.tvClaimGuide);
        MaterialButton btnViewItem = view.findViewById(R.id.btnViewItem);
        MaterialButton btnBackToHome = view.findViewById(R.id.btnBackToHome);

        // Customize Text for Claim
        if (tvTitle != null)
            tvTitle.setText("Claim Successful!");
        if (tvGuide != null)
            tvGuide.setText("Please collect your item within the time window. Check details in 'My Claims'.");

        // "View Item" used for "My Claims"
        if (btnViewItem != null) {
            btnViewItem.setVisibility(View.VISIBLE);
            btnViewItem.setText("My Claims");
            btnViewItem.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.coveringFragment,
                                new com.example.foodaid_mad_project.ImpactFragments.ImpactFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        if (btnBackToHome != null) {
            btnBackToHome.setText("Back to Home");
            btnBackToHome.setOnClickListener(v -> {
                // Clear back stack to Home
                getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            });
        }
    }
}
