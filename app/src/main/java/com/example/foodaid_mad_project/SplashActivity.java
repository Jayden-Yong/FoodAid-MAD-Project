package com.example.foodaid_mad_project;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnticipateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.foodaid_mad_project.AuthFragments.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * SplashActivity
 *
 * The initial launch screen.
 * Handles:
 * - Displaying the splash screen animation.
 * - Checking current Authentication state.
 * - Pre-fetching User data from Firestore if logged in.
 * - Routing to MainActivity (if logged in) or AuthActivity (if not).
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private boolean isAnimationDone = false;
    private boolean isDataReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Start Data Fetching
        if (currentUser != null) {
            fetchUserData(currentUser);
        } else {
            isDataReady = true;
        }

        setupSplashScreenAnimation(splashScreen);
    }

    private void fetchUserData(FirebaseUser currentUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            User user = documentSnapshot.toObject(User.class);
                            UserManager.getInstance().setUser(user);
                        } catch (Exception e) {
                            Log.e("SplashActivity", "Error parsing user data", e);
                        }
                    }
                    isDataReady = true;
                    checkAndNavigate();
                })
                .addOnFailureListener(e -> {
                    Log.e("SplashActivity", "Failed to fetch user data", e);
                    isDataReady = true; // Proceed anyway, likely offline
                    checkAndNavigate();
                });
    }

    private void setupSplashScreenAnimation(SplashScreen splashScreen) {
        long animationDuration = 1000;
        long startTime = System.currentTimeMillis();

        // Keep splash screen on screen until animation time passes AND data is ready
        splashScreen.setKeepOnScreenCondition(() -> {
            long elapsedTime = System.currentTimeMillis() - startTime;
            // Wait for both animation time AND data ready state?
            // Actually, keep condition is just for the "holding" phase.
            // Let's hold until animation duration passes. Logic for combined wait is better
            // handled in exit listener.
            return elapsedTime < animationDuration;
        });

        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {
            final View splashScreenView = splashScreenViewProvider.getView();
            final ObjectAnimator slideUp = ObjectAnimator.ofFloat(
                    splashScreenView,
                    View.TRANSLATION_Y,
                    0f,
                    -splashScreenView.getHeight());
            slideUp.setInterpolator(new AnticipateInterpolator());
            slideUp.setDuration(300L);

            slideUp.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    splashScreenViewProvider.remove();
                    isAnimationDone = true;
                    checkAndNavigate();
                }
            });

            slideUp.start();
        });
    }

    private void checkAndNavigate() {
        // Wait for both Animation AND Data
        if (!isAnimationDone)
            return;

        // If data isn't ready yet (e.g. slow network), we might have already finished
        // animation.
        // In that case, we should wait?
        // Current logic: If animation finishes but data isn't ready, we return.
        // Then when data finishes (addOnSuccess), it calls checkAndNavigate() again and
        // succeeds.
        if (isDataReady) {
            navigateToNextScreen();
        }
    }

    private void navigateToNextScreen() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Intent intent;
        if (currentUser != null) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, AuthActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(0, 0); // No transition animation
        finish();
    }
}
