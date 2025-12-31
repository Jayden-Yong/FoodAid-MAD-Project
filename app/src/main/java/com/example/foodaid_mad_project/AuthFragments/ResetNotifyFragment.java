package com.example.foodaid_mad_project.AuthFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.R;

/**
 * ResetNotifyFragment
 *
 * Displays a confirmation screen telling the user to check their email.
 * Tapping the screen or back button returns the user to the Login screen
 * by popping the back stack twice (skipping the "Forgot Password" input screen).
 */
public class ResetNotifyFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_link_notify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ConstraintLayout notifyBackground = view.findViewById(R.id.notifyBackground);

        // Tap anywhere to go back to Login
        notifyBackground.setOnClickListener(v -> navigateBackToLogin());

        // Handle Hardware Back Button
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateBackToLogin();
                    }
                });
    }

    /**
     * Pops the back stack twice to return to LoginFragment,
     * bypassing the ResetPasswordFragment input screen.
     */
    private void navigateBackToLogin() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            // Pop once to remove this fragment
            getParentFragmentManager().popBackStack();
            // Pop again to remove ResetPasswordFragment and show LoginFragment
            // (Assuming LoginFragment is the base or previous on stack)
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        }
    }
}
