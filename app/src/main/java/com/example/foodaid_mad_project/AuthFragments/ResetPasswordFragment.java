package com.example.foodaid_mad_project.AuthFragments;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

/**
 * <h1>ResetPasswordFragment</h1>
 * <p>
 * This fragment allows users to request a password reset email.
 * It validates the email input and triggers the Firebase reset flow.
 * Upon success, it navigates to the {@link ResetNotifyFragment}.
 * </p>
 */
public class ResetPasswordFragment extends Fragment {

    private EditText etEmailToReset;
    private final FirebaseAuth auth;

    /**
     * Constructor required to inject FirebaseAuth instance.
     * Note: In production apps, Dependency Injection (Hilt/Dagger) is preferred,
     * but passing via constructor is acceptable for simple fragments if properly
     * handled.
     * However, the default constructor is required for Fragment recreation.
     * Consider removing this constructor and using FirebaseAuth.getInstance()
     * inside onCreate.
     */
    public ResetPasswordFragment(FirebaseAuth auth) {
        this.auth = auth;
    }

    // Default constructor for system recreation
    public ResetPasswordFragment() {
        this.auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI Setup
        etEmailToReset = view.findViewById(R.id.etEmailToReset);
        MaterialButton btnReset = view.findViewById(R.id.btnReset);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Listeners
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnReset.setOnClickListener(v -> sendPasswordResetEmail());

        // Handle Hardware Back Button
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack();
                        }
                    }
                });
    }

    /**
     * Validates email and sends a password reset link via Firebase.
     */
    private void sendPasswordResetEmail() {
        String email = etEmailToReset.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your email first", Toast.LENGTH_LONG).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_LONG).show();
            return;
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Navigate to confirmation screen
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.authFragmentContainer, new ResetNotifyFragment())
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getContext(), "Failed to send password reset email", Toast.LENGTH_LONG).show();
            }
        });
    }
}
