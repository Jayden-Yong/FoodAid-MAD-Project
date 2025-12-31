package com.example.foodaid_mad_project;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import com.example.foodaid_mad_project.AuthFragments.LoginFragment;
import com.example.foodaid_mad_project.AuthFragments.RegisterFragment;
import com.google.android.material.button.MaterialButton;

/**
 * AuthActivity
 *
 * The authentication entry point.
 * Handles navigation between:
 * - Landing Page (Login/Register buttons)
 * - Login Fragment
 * - Register Fragment
 *
 * Also manages back navigation to return to the landing page from fragments.
 */
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

    /**
     * Navigates to the Login Fragment.
     */
    public void showLoginFragment() {
        hideLandingPage();
        authFragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.authFragmentContainer, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * Navigates to the Register Fragment.
     */
    public void showRegisterFragment() {
        hideLandingPage();
        authFragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.authFragmentContainer, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * Hides the landing page elements to display the fragment container.
     */
    private void hideLandingPage() {
        authLogoTop.setVisibility(View.GONE);
        tvSlogan1.setVisibility(View.GONE);
        tvSlogan2.setVisibility(View.GONE);
        btnToLogin.setVisibility(View.GONE);
        btnToSignUp.setVisibility(View.GONE);
        tvFooter.setVisibility(View.GONE);
    }

    /**
     * Restores the landing page view state.
     */
    public void showLandingPage() {
        authLogoTop.setVisibility(View.VISIBLE);
        tvSlogan1.setVisibility(View.VISIBLE);
        tvSlogan2.setVisibility(View.VISIBLE);
        btnToLogin.setVisibility(View.VISIBLE);
        btnToSignUp.setVisibility(View.VISIBLE);
        tvFooter.setVisibility(View.VISIBLE);

        authFragmentContainer.setVisibility(View.GONE);
        getSupportFragmentManager().popBackStack(); // Clear back stack
    }
}
