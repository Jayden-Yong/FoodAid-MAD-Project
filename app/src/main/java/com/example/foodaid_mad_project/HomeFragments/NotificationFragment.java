package com.example.foodaid_mad_project.HomeFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.Model.Notification;
import com.example.foodaid_mad_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private TextView tvEmptyState;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        setupRecyclerView();
        loadNotifications();
    }

    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList, this::onNotificationClicked);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        if (auth.getCurrentUser() == null)
            return;

        // Query: collection "notifications", where userId == currentUser, sort by
        // timestamp desc
        db.collection("notifications")
                .whereEqualTo("userId", auth.getCurrentUser().getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (snapshots != null) {
                        notificationList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                            Notification n = doc.toObject(Notification.class);
                            if (n != null) {
                                n.setId(doc.getId());
                                notificationList.add(n);
                            }
                        }

                        adapter.notifyDataSetChanged();
                        toggleEmptyState();
                    }
                });
    }

    private void toggleEmptyState() {
        if (notificationList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
        }
    }

    private void onNotificationClicked(Notification notification) {
        if (!notification.isRead()) {
            // Mark as read in Firestore
            db.collection("notifications").document(notification.getId())
                    .update("isRead", true)
                    .addOnFailureListener(
                            e -> Toast.makeText(getContext(), "Error updating status", Toast.LENGTH_SHORT).show());
        }

        // Navigation logic can be expanded here based on notification type
        // For now, simpler is better + we are in Home flow
        if ("CLAIM".equals(notification.getType())) {
            // Maybe navigate to Impact or details?
        }
    }
}
