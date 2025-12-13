package com.example.foodaid_mad_project.HomeFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.AuthFragments.User;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.UserManager;

public class HomeFragment extends Fragment {

    private String email, username, welcomeDisplay;
    private TextView tvWelcomeUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvWelcomeUser = view.findViewById(R.id.tvWelcomeUser);

        try {
            User user = UserManager.getInstance().getUser();
            if (user != null) {
                email = user.getEmail();

                if (user.getFullName() != null) {
                    username = user.getFullName();
                } else {
                    username = user.getDisplayName();
                }

                welcomeDisplay = username;
            }

        } catch (NullPointerException e) {
            welcomeDisplay = "Guest";
        }

        tvWelcomeUser.setText(getString(R.string.Welcome_User, "morning", welcomeDisplay));
    }

}
