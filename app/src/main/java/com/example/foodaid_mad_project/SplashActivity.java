package com.example.foodaid_mad_project;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AnticipateInterpolator;

import androidx.core.splashscreen.SplashScreen;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodaid_mad_project.AuthFragments.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Task<DocumentSnapshot> fetchUserTask;
    private boolean isAnimationDone = false;
    private boolean isDataReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            fetchUserTask = db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            try {
                                User user = documentSnapshot.toObject(User.class);
                                UserManager.getInstance().setUser(user);
                            } catch (Exception e) {
                                Log.e("SplashActivity", "Error parsing user data", e);
                                // Continue without user data (or maybe basic data)
                                // Prevent crash loop
                            }
                        }
                        isDataReady = true;
                        checkAndNavigate();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SplashActivity", "Failed to fetch user data", e);
                        isDataReady = true; // Proceed anyway, handle null downstream
                        checkAndNavigate();
                    });
        } else {
            isDataReady = true;
        }

        long animationDuration = 4100;
        long startTime = System.currentTimeMillis();
        splashScreen.setKeepOnScreenCondition(() -> {
            long elapsedTime = System.currentTimeMillis() - startTime;
            // Keep splash screen until animation time catches up AND (data is ready OR we
            // time out waiting)
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

            // Run your animation.
            slideUp.start();
        });
    }

    private void checkAndNavigate() {
        // Only navigate if both the exit animation is done AND the data fetching is
        // complete
        if (!isAnimationDone)
            return;

        if (isDataReady) {
            navigateToNextScreen();
        } else {
            // If we are here, animation finished but data is pending.
            // The callback (addOnSuccessListener) will call checkAndNavigate again.
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
        overridePendingTransition(0, 0);
        finish();
    }
}
