package com.example.foodaid_mad_project;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnticipateInterpolator;

import androidx.core.splashscreen.SplashScreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodaid_mad_project.AuthFragments.CompleteProfileFragment;
import com.example.foodaid_mad_project.AuthFragments.User;
import com.example.foodaid_mad_project.HomeFragments.HomeFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        User user = UserManager.getInstance().getUser();
        // TODO: replace with navigation
        // Set mainFragment
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Create HomeFragment instance, add HomeFragment to the mainFragment and run
            HomeFragment homeFragment = new HomeFragment();
            CompleteProfileFragment completeProfileFragment = new CompleteProfileFragment();

            if (user.getFullName() == null) {
                fragmentTransaction.add(R.id.mainFragment, completeProfileFragment);
            } else {
                fragmentTransaction.add(R.id.mainFragment, homeFragment);
            }

            fragmentTransaction.commit();
        }
    }
}