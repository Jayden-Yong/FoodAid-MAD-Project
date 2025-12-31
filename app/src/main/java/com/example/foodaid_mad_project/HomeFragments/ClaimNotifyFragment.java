package com.example.foodaid_mad_project.HomeFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.R;
import com.google.android.material.button.MaterialButton;

/**
 ClaimNotifyFragment
 Displays a success message after a user claims a food item.
 Reuses the layout {@code fragment_donate_notify.xml} but customizes text for claims.
 */
public class ClaimNotifyFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Reuse generic notify layout
        return inflater.inflate(R.layout.fragment_donate_notify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Views
        TextView tvTitle = view.findViewById(R.id.tvClaimTitle);
        TextView tvGuide = view.findViewById(R.id.tvClaimGuide);
        TextView tvDonationDate = view.findViewById(R.id.tvDonationDate);
        MaterialButton btnViewItem = view.findViewById(R.id.btnViewItem);
        MaterialButton btnBackToHome = view.findViewById(R.id.btnBackToHome);

        // Customize Text for Claim Logic
        if (tvTitle != null) {
            tvTitle.setText("Claim Successful!");
        }
        if (tvGuide != null) {
            tvGuide.setText(
                    "Please collect your item within the time window. You can view your claims in the Impact page.");
        }
        if(tvDonationDate != null){
            tvDonationDate.setVisibility(View.GONE);
        }

        // Hide "View Item" as per design request (claims are viewed in Impact/History)
        if (btnViewItem != null) {
            btnViewItem.setVisibility(View.GONE);
        }

        // Configure Back Button
        if (btnBackToHome != null) {
            btnBackToHome.setText("Back to Home");
            btnBackToHome.setOnClickListener(v -> {
                // Clear entire back stack to return to HomeFragment fresh
                getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            });
        }
    }
}
