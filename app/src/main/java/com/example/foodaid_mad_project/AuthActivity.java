package com.example.foodaid_mad_project;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import com.example.foodaid_mad_project.AuthFragments.LoginFragment;
import com.example.foodaid_mad_project.AuthFragments.RegisterFragment;
import com.google.android.material.button.MaterialButton;

public class AuthActivity extends AppCompatActivity {

    private MaterialButton btnToLogin, btnToSignUp;
    private FragmentContainerView authFragmentContainer;
    private View authLogoTop, tvSlogan1, tvSlogan2, tvFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Initialize views
        btnToLogin = findViewById(R.id.btnToLogin);
        btnToSignUp = findViewById(R.id.btnToSignUp);
        authFragmentContainer = findViewById(R.id.authFragmentContainer);
        authLogoTop = findViewById(R.id.authLogoTop);
        tvSlogan1 = findViewById(R.id.tvSlogan1);
        tvSlogan2 = findViewById(R.id.tvSlogan2);
        tvFooter = findViewById(R.id.tvFooter);

        // Set click listeners
        btnToLogin.setOnClickListener(v -> showLoginFragment());
        btnToSignUp.setOnClickListener(v -> showRegisterFragment());

        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    showLandingPage();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    public void showLoginFragment() {
        // Hide landing page views
        hideLandingPage();

        // Show fragment container and load LoginFragment
        authFragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.authFragmentContainer, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }

    public void showRegisterFragment() {
        // Hide landing page views
        hideLandingPage();

        // Show fragment container and load RegisterFragment
        authFragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.authFragmentContainer, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }

    private void hideLandingPage() {
        authLogoTop.setVisibility(View.GONE);
        tvSlogan1.setVisibility(View.GONE);
        tvSlogan2.setVisibility(View.GONE);
        btnToLogin.setVisibility(View.GONE);
        btnToSignUp.setVisibility(View.GONE);
        tvFooter.setVisibility(View.GONE);
    }

    public void showLandingPage() {
        // Show landing page views
        authLogoTop.setVisibility(View.VISIBLE);
        tvSlogan1.setVisibility(View.VISIBLE);
        tvSlogan2.setVisibility(View.VISIBLE);
        btnToLogin.setVisibility(View.VISIBLE);
        btnToSignUp.setVisibility(View.VISIBLE);
        tvFooter.setVisibility(View.VISIBLE);

        // Hide fragment container
        authFragmentContainer.setVisibility(View.GONE);

        // Clear back stack
        getSupportFragmentManager().popBackStack();
    }
}
