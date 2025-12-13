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
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mainFragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
            NavigationUI.setupWithNavController(bottomNav, navController);

            User user = UserManager.getInstance().getUser();

            if (user != null && user.getFullName() == null) {
                // Navigate to CompleteProfileFragment if profile is incomplete
                navController.navigate(R.id.completeProfileFragment);
            }

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