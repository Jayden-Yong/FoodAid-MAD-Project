package com.example.foodaid_mad_project;

import android.app.Application;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

public class FoodAidApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable offline persistence for Firestore
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                        .build())
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
    }
}
