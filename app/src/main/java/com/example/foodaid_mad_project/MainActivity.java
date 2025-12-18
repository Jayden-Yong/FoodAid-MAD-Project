package com.example.foodaid_mad_project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.foodaid_mad_project.DonateFragments.DonateFragment;
import com.example.foodaid_mad_project.Utils.NotificationWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.TimeUnit;

/**
 * <h1>MainActivity</h1>
 * <p>
 * The main container for the application after authentication.
 * Manages:
 * <ul>
 * <li>Bottom Navigation (Home, Impact, Donate, Community, Profile).</li>
 * <li>Floating Action Button for Quick Donation.</li>
 * <li>Global Notification subscriptions (FCM & Worker).</li>
 * <li>Permission requests (Notifications).</li>
 * </ul>
 * </p>
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupPermissions();
        setupNotifications();
        setupNavigation();
    }

    private void setupPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.POST_NOTIFICATIONS }, 101);
            }
        }
    }

    private void setupNotifications() {
        // 1. Background Worker for Polling Global Notifs
        PeriodicWorkRequest notificationWork = new PeriodicWorkRequest.Builder(NotificationWorker.class, 15,
                TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "FoodAidGlobalNotifications",
                ExistingPeriodicWorkPolicy.KEEP,
                notificationWork);

        // 2. FCM Topic Subscription
        FirebaseMessaging.getInstance().subscribeToTopic("donations")
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("MainActivity", "FCM Subscribe failed", task.getException());
                    } else {
                        Log.d("MainActivity", "Subscribed to donations topic");
                    }
                });
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mainFragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
            NavigationUI.setupWithNavController(bottomNav, navController);

            // FAB Logic: Opens Donation Overlay
            FloatingActionButton fab = findViewById(R.id.fabNewDonation);
            fab.setOnClickListener(v -> {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.coveringFragment, new DonateFragment())
                        .addToBackStack("Donate")
                        .commit();
            });
        }
    }
}