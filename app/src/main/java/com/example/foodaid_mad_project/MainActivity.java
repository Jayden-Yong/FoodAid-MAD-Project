package com.example.foodaid_mad_project;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.foodaid_mad_project.DonateFragments.DonateFragment; // Import DonateFragment
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodaid_mad_project.AuthFragments.User;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for authentication
        com.google.firebase.auth.FirebaseUser firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance()
                .getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new android.content.Intent(this, AuthActivity.class));
            finish();
            return;
        } else if (UserManager.getInstance().getUser() == null) {
            // User is logged in but data is missing.
            // Try to recover by creating a temporary user object from Auth data
            // instead of redirecting to Splash loop.
            User tempUser = new User();
            // We can't set fields easily without constructor, so we just set what we can if
            // possible
            // or just rely on the fact that Auth is valid.
            // Ideally, we re-fetch here, but for now let's just NOT restart Splash to avoid
            // loop.
            android.util.Log.w("MainActivity",
                    "User is logged in but UserManager data is null. Proceeding with caution.");
        }

        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mainFragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
            NavigationUI.setupWithNavController(bottomNav, navController);

            User user = UserManager.getInstance().getUser();

            FloatingActionButton fab = findViewById(R.id.fabNewDonation);
            fab.setOnClickListener(v -> {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.coveringFragment, new DonateFragment())
                        .addToBackStack("Donate")
                        .commit();
            });

            // Manage Bottom Nav and FAB Visibility
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                if (id == R.id.privacyFragment ||
                        id == R.id.helpFAQFragment) {
                    bottomNav.setVisibility(android.view.View.GONE);
                    fab.setVisibility(android.view.View.GONE);
                } else {
                    bottomNav.setVisibility(android.view.View.VISIBLE);
                    fab.setVisibility(android.view.View.VISIBLE);
                }
            });
        }
    }
}