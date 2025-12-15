package com.example.foodaid_mad_project.ProfileFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.AuthActivity;
import com.example.foodaid_mad_project.AuthFragments.User;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.UserManager;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserId;
    private Button btnLogout;

    private long lastClickTime = 0;

    private boolean isSafeToClick() {
        if (System.currentTimeMillis() - lastClickTime < 1000) {
            return false;
        }
        lastClickTime = System.currentTimeMillis();
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserId = view.findViewById(R.id.tvUserId);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Fetch User Data from UserManager
        User currentUser = UserManager.getInstance().getUser();

        if (currentUser != null) {
            tvUserName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "User");
            // Using Email as the ID/Matric placeholder for now as per plan
            tvUserId.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "N/A");
        }

        // Logout Functionality
        btnLogout.setOnClickListener(v -> {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Clear UserManager
            UserManager.getInstance().clear();

            // Navigate back to AuthActivity (Login)
            Intent intent = new Intent(requireActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Navigation to Settings Fragments
        view.findViewById(R.id.btnPrivacySettings).setOnClickListener(v -> {
            if (!isSafeToClick())
                return;
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.action_profile_to_privacy);
        });

        view.findViewById(R.id.btnHelpFAQ).setOnClickListener(v -> {
            if (!isSafeToClick())
                return;
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.action_profile_to_helpFAQ);
        });

        view.findViewById(R.id.btnContactReport).setOnClickListener(v -> {
            if (!isSafeToClick())
                return;
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.action_profile_to_contactReport);
        });
    }
}
