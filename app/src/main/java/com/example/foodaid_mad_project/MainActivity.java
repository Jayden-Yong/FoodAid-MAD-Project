package com.example.foodaid_mad_project;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnticipateInterpolator;

import androidx.core.splashscreen.SplashScreen;

import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        long animationDuration = 2100;
        long startTime = System.currentTimeMillis();
        splashScreen.setKeepOnScreenCondition(() -> {
            long elapsedTime = System.currentTimeMillis() - startTime;
            return elapsedTime < animationDuration;
        });

        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {
            final View splashScreenView = splashScreenViewProvider.getView();
            final ObjectAnimator slideUp = ObjectAnimator.ofFloat(
                    splashScreenView,
                    View.TRANSLATION_Y,
                    0f,
                    -splashScreenView.getHeight()
            );
            slideUp.setInterpolator(new AnticipateInterpolator());
            slideUp.setDuration(300L);

            slideUp.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    splashScreenViewProvider.remove();
                }
            });

            // Run your animation.
            slideUp.start();
        });
    }
}