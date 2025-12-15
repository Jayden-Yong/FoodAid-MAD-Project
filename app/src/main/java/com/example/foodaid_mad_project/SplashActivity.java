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
import com.google.firebase.firestore.Source;

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
                        User user = documentSnapshot.toObject(User.class);
                        UserManager.getInstance().setUser(user);
                        isDataReady = true;
                        checkAndNavigate();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SplashActivity", "Failed to fetch user data from server, trying cache.", e);
                        // Try to fetch from cache if server fails
                        db.collection("users").document(currentUser.getUid()).get(Source.CACHE)
                                .addOnSuccessListener(documentSnapshot -> {
                                    User user = documentSnapshot.toObject(User.class);
                                    UserManager.getInstance().setUser(user);
                                    isDataReady = true;
                                    checkAndNavigate();
                                })
                                .addOnFailureListener(e2 -> {
                                    Log.e("SplashActivity",
                                            "Failed to fetch user data from cache too. Creating fallback user.", e2);
                                    // Fallback to a minimal user object deriving from FirebaseAuth
                                    // Fallback to a minimal user object deriving from FirebaseAuth
                                    User fallbackUser = new User(
                                            currentUser.getUid(),
                                            currentUser.getEmail(),
                                            currentUser.getDisplayName() != null ? currentUser.getDisplayName()
                                                    : "User");
                                    UserManager.getInstance().setUser(fallbackUser);
                                    isDataReady = true;
                                    checkAndNavigate();
                                });
                    });
        } else {
            isDataReady = true;
        }

        long animationDuration = 2000;
        long startTime = System.currentTimeMillis();
        splashScreen.setKeepOnScreenCondition(() -> {
            long elapsedTime = System.currentTimeMillis() - startTime;
            // Keep splash screen until animation time catches up AND (data is ready OR we
            // time out waiting)
            return elapsedTime < animationDuration;
        });

        // Safety Timeout: If data takes too long (e.g. 8 seconds), force logout and
        // retry
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isDataReady && !isFinishing()) {
                Log.w("SplashActivity", "Data fetch timed out. Forcing logout.");
                mAuth.signOut();
                isAnimationDone = true; // Force allowing navigation
                checkAndNavigate();
            }
        }, 8000);

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
