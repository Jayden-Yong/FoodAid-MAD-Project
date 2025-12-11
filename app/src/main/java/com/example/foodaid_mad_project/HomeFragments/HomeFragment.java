package com.example.foodaid_mad_project.HomeFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {


    private TextView tvWelcomeUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Testing for fragment and string formatting
        String email, username, welcomeDisplay;

        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();
            username = currentUser.getDisplayName();
            email = currentUser.getEmail();
            welcomeDisplay = username.isEmpty() ? email : username;
        } catch (NullPointerException e){
            Log.e("HomeFragment", "NullPointerException: onViewCreated()", e);
            welcomeDisplay = "Guest";
        }

        tvWelcomeUser = view.findViewById(R.id.tvWelcomeUser);
        tvWelcomeUser.setText(getString(R.string.Welcome_User, "morning", welcomeDisplay));
    }

}
