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
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.AuthActivity;
import com.example.foodaid_mad_project.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordFragment extends Fragment {
    private EditText etEmailToReset;
    private FirebaseAuth auth;

    public ResetPasswordFragment(FirebaseAuth auth){
        this.auth = auth;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            }
        });

        etEmailToReset = view.findViewById(R.id.etEmailToReset);
        MaterialButton btnReset = view.findViewById(R.id.btnReset);

        btnReset.setOnClickListener(v -> sendPasswordResetEmail());
    }

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
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.ResetNotifyFragmentContainer, new ResetNotifyFragment())
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getContext(), "Failed to send password reset email", Toast.LENGTH_LONG).show();
            }
        });
    }
}
