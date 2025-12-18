package com.example.foodaid_mad_project.HomeFragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.Model.Notification;
import com.example.foodaid_mad_project.Model.NotificationItem;
import com.example.foodaid_mad_project.R;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * <h1>NotificationFragment</h1>
 * <p>
 * Displays a list of notifications for the user.
 * Supports:
 * <ul>
 * <li>Real-time updates from Firestore.</li>
 * <li>Filtering by "All", "Donation", or "Community".</li>
 * <li>Handling "Global" notifications (system-wide) with local read-status
 * tracking.</li>
 * <li>Grouping notifications by date (Today, Yesterday, etc.).</li>
 * </ul>
 * </p>
 */
public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationItem> allDataItems; // Raw fetched items mapped to ViewModels
    private List<NotificationItem> displayedList; // Items currently displayed (filtered + headers)

    // UI Elements
    private ChipGroup chipGroupFilters;
    private TextView btnViewHistory;
    private TextView tvEmptyState;

    // Pagination / Display Logic
    private int currentDisplayCount = 5;
    private static final int ITEMS_PER_PAGE = 5;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration notificationListener;

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

        // 1. Initialize Views
        initializeViews(view);

        // 2. Setup RecyclerView
        setupRecyclerView();

        // 3. Setup Actions (Back, Filter, Load More)
        setupActions(view);

        // 4. Load Data
        loadNotificationsFromFirestore();
    }

    private void initializeViews(View view) {
        rvNotifications = view.findViewById(R.id.rvNotifications);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        btnViewHistory = view.findViewById(R.id.tv_view_history);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        if (toolBarTitle != null) {
            toolBarTitle.setText("Notifications");
        }
    }

    private void setupRecyclerView() {
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        allDataItems = new ArrayList<>();
        displayedList = new ArrayList<>();
        adapter = new NotificationAdapter(displayedList);
        adapter.setOnItemClickListener(this::onNotificationClicked);
        rvNotifications.setAdapter(adapter);
    }

    private void setupActions(View view) {
        // Back Navigation
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack();
                        }
                    }
                });

        Toolbar toolbar = view.findViewById(R.id.Toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Filter Logic
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> loadNotificationsFromFirestore());

        // Load More Logic
        btnViewHistory.setOnClickListener(v -> loadMoreItems());
    }

    /**
     * Setups the Firestore listener based on the selected filter chip.
     * Uses efficient server-side filtering logic.
     */
    private void loadNotificationsFromFirestore() {
        if (auth.getCurrentUser() == null)
            return;

        if (notificationListener != null) {
            notificationListener.remove();
        }

        int checkedId = chipGroupFilters.getCheckedChipId();
        Query query;

        if (checkedId == R.id.chip_community) {
            // Community: All posts of type "Community"
            query = db.collection("notifications")
                    .whereEqualTo("type", "Community")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(20);

        } else if (checkedId == R.id.chip_donation) {
            // Donation: Explicitly for User OR Global, but type must be "Donation"
            query = db.collection("notifications")
                    .whereIn("userId", Arrays.asList(auth.getCurrentUser().getUid(), "ALL"))
                    .whereEqualTo("type", "Donation")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(20);

        } else {
            // All: (User OR Global) OR (type == Community)
            // Uses Filter.or for composite query
            query = db.collection("notifications")
                    .where(Filter.or(
                            Filter.inArray("userId", Arrays.asList(auth.getCurrentUser().getUid(), "ALL")),
                            Filter.equalTo("type", "Community")))
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(20);
        }

        notificationListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e("Notify", "Listen failed.", e);
                return;
            }

            if (snapshots != null) {
                allDataItems.clear();
                for (DocumentSnapshot doc : snapshots) {
                    Notification n = doc.toObject(Notification.class);
                    if (n != null) {
                        n.setId(doc.getId());
                        allDataItems.add(mapToViewModel(n));
                    }
                }

                // Update UI
                updateDisplayedList();
                adapter.updateList(displayedList);
                toggleEmptyState();
            }
        });
    }

    /**
     * Maps the raw Firestore model to the View Model.
     * Handles local read status for Global notifications.
     */
    private NotificationItem mapToViewModel(Notification n) {
        String timeString = new SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
                .format(new Date(n.getTimestamp()));

        boolean isRead = n.isRead();

        // Check local read state for Global items
        if ("ALL".equals(n.getUserId())) {
            if (isGlobalReadLocally(n.getId())) {
                isRead = true;
            }
        }

        return new NotificationItem(
                n.getTitle(),
                n.getMessage(),
                timeString,
                n.getType(),
                isRead,
                new Date(n.getTimestamp()),
                n.getId(),
                n.getUserId());
    }

    /**
     * Handles "Mark as Read" logic.
     */
    private void onNotificationClicked(NotificationItem item) {
        if (item.getId() != null && !item.isRead()) {
            Toast.makeText(getContext(), "Marking as read...", Toast.LENGTH_SHORT).show();

            if ("ALL".equals(item.getUserId())) {
                // Global: Save to SharedPreferences
                markGlobalReadLocally(item.getId());
                // Reload to reflect change (simplest way to re-run mapToViewModel)
                loadNotificationsFromFirestore();
            } else {
                // Personal: Update Firestore
                db.collection("notifications").document(item.getId())
                        .update("isRead", true)
                        .addOnFailureListener(e -> Toast
                                .makeText(getContext(), "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT)
                                .show());
            }
        }
    }

    // ============================================================================================
    // DISPLAY & PAGINATION
    // ============================================================================================

    private void loadMoreItems() {
        if (currentDisplayCount < allDataItems.size()) {
            currentDisplayCount += ITEMS_PER_PAGE;
            updateDisplayedList();
            adapter.updateList(displayedList);
        }
        // Check visibility again after update
        if (currentDisplayCount >= allDataItems.size()) {
            btnViewHistory.setVisibility(View.GONE);
        }
    }

    private void updateDisplayedList() {
        displayedList.clear();
        if (allDataItems.isEmpty())
            return;

        int limit = Math.min(allDataItems.size(), currentDisplayCount);
        List<NotificationItem> slice = new ArrayList<>(allDataItems.subList(0, limit));
        displayedList = groupItemsWithHeaders(slice);
    }

    private List<NotificationItem> groupItemsWithHeaders(List<NotificationItem> items) {
        List<NotificationItem> grouped = new ArrayList<>();
        if (items.isEmpty())
            return grouped;

        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Calendar thisWeek = Calendar.getInstance();
        thisWeek.add(Calendar.DAY_OF_YEAR, -7);
        Calendar lastWeek = Calendar.getInstance();
        lastWeek.add(Calendar.DAY_OF_YEAR, -14);

        String currentHeader = "";

        for (NotificationItem item : items) {
            String itemHeader = getHeaderForDate(item.getTimestamp(), today, yesterday, thisWeek, lastWeek);
            if (!itemHeader.equals(currentHeader)) {
                grouped.add(new NotificationItem(itemHeader));
                currentHeader = itemHeader;
            }
            grouped.add(item);
        }
        return grouped;
    }

    private String getHeaderForDate(Date date, Calendar today, Calendar yesterday, Calendar thisWeek,
            Calendar lastWeek) {
        if (date == null)
            return "Unknown Date";
        Calendar target = Calendar.getInstance();
        target.setTime(date);

        if (isSameDay(target, today))
            return "Today";
        if (isSameDay(target, yesterday))
            return "Yesterday";
        if (target.after(thisWeek))
            return "This week";
        if (target.after(lastWeek))
            return "Last week";
        return "A long time ago";
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void toggleEmptyState() {
        if (allDataItems.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
            btnViewHistory.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            if (displayedList.size() < allDataItems.size()) {
                btnViewHistory.setVisibility(View.VISIBLE);
            } else {
                btnViewHistory.setVisibility(View.GONE);
            }
        }
    }

    // ============================================================================================
    // LOCAL STORAGE HELPERS
    // ============================================================================================

    private boolean isGlobalReadLocally(String notificationId) {
        if (auth.getCurrentUser() == null || getContext() == null)
            return false;
        SharedPreferences prefs = requireContext().getSharedPreferences(
                "foodaid_prefs_" + auth.getCurrentUser().getUid(), android.content.Context.MODE_PRIVATE);
        Set<String> readSet = prefs.getStringSet("read_global_ids", new HashSet<>());
        return readSet.contains(notificationId);
    }

    private void markGlobalReadLocally(String notificationId) {
        if (auth.getCurrentUser() == null || getContext() == null)
            return false;
        SharedPreferences prefs = requireContext().getSharedPreferences(
                "foodaid_prefs_" + auth.getCurrentUser().getUid(), android.content.Context.MODE_PRIVATE);
        Set<String> readSet = new HashSet<>(prefs.getStringSet("read_global_ids", new HashSet<>()));
        readSet.add(notificationId);
        prefs.edit().putStringSet("read_global_ids", readSet).apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }
}
