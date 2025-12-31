package com.example.foodaid_mad_project.ProfileFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.Utils.BadgeRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * AllBadgesFragment
 *
 * This fragment displays the user's earned badges in a popup dialog.
 * It shows a grid of available badges, highlighting the ones the user has earned with full color,
 * while unearned badges are shown in grayscale.
 */
public class AllBadgesFragment extends androidx.fragment.app.DialogFragment {

    private RecyclerView rvAllBadges;
    private AllBadgesAdapter adapter;
    private ImageButton btnBack;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration userListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_badges, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        loadUserBadges();

        btnBack.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Optional: Add margins logic if needed, but XML padding is easier
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) {
            userListener.remove();
        }
    }

    private void initializeViews(View view) {
        rvAllBadges = view.findViewById(R.id.rvAllBadges);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        // Grid with 3 columns
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        rvAllBadges.setLayoutManager(layoutManager);
        
        // Initial empty state
        adapter = new AllBadgesAdapter(BadgeRepository.getAllBadges(), new ArrayList<>());
        rvAllBadges.setAdapter(adapter);
    }

    private void loadUserBadges() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        userListener = db.collection("users").document(user.getUid())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("AllBadges", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        List<String> earnedIds = (List<String>) snapshot.get("earnedBadges");
                        adapter.updateData(BadgeRepository.getAllBadges(), earnedIds);
                    }
                });
    }
}
