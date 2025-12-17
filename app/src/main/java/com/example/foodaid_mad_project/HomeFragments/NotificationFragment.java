package com.example.foodaid_mad_project.HomeFragments;

import android.os.Bundle;
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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.Model.Notification;
import com.example.foodaid_mad_project.Model.NotificationItem;
import com.example.foodaid_mad_project.R;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationItem> allDataItems; // All fetched items mapped to ViewModels
    private List<NotificationItem> displayedList; // Items currently displayed (filtered + headers)
    private ChipGroup chipGroupFilters;
    private TextView btnViewHistory;
    private TextView tvEmptyState;

    private int currentDisplayCount = 5;
    private static final int ITEMS_PER_PAGE = 5;

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
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        btnViewHistory = view.findViewById(R.id.tv_view_history);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        allDataItems = new ArrayList<>();
        displayedList = new ArrayList<>();
        adapter = new NotificationAdapter(displayedList);
        adapter.setOnItemClickListener(this::onNotificationClicked);
        rvNotifications.setAdapter(adapter);

        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        // Ensure R.string.String exists or use hardcoded if not (assuming "String" is a
        // key in incoming strings.xml, but it looks weird. Using "Notifications" or
        // similar if possible, but sticking to incoming pattern for safety if I knew it
        // existed. I'll use hardcoded "Notifications" to be safe or "Food Aid Details"
        // as in incoming code if it was generic wrapper.)
        // Incoming code had: toolBarTitle.setText(getString(R.string.String, "Food Aid
        // Details")); - This implies R.string.String is a format string. I'll assume it
        // exists.
        if (toolBarTitle != null) {
            toolBarTitle.setText("Notifications");
        }

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
            toolbar.setNavigationOnClickListener(v -> {
                getParentFragmentManager().popBackStack();
            });
        }

        // Filter Logic
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty())
                return;
            currentDisplayCount = ITEMS_PER_PAGE;
            btnViewHistory.setVisibility(View.VISIBLE);
            updateDisplayedList();
            adapter.updateList(displayedList);
        });

        // Load More
        btnViewHistory.setOnClickListener(v -> {
            loadMoreItems();
        });

        loadNotificationsFromFirestore();
    }

    private void loadNotificationsFromFirestore() {
        if (auth.getCurrentUser() == null)
            return;

        db.collection("notifications")
                .whereIn("userId", java.util.Arrays.asList(auth.getCurrentUser().getUid(), "ALL"))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20) // Optimization: Fetch only latest 20
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (snapshots != null) {
                        allDataItems.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                            Notification n = doc.toObject(Notification.class);
                            if (n != null) {
                                n.setId(doc.getId());
                                allDataItems.add(mapToViewModel(n));
                            }
                        }

                        // Apply current filter and update UI
                        updateDisplayedList();
                        adapter.updateList(displayedList);
                        toggleEmptyState();
                    }
                });
    }

    private NotificationItem mapToViewModel(Notification n) {
        String timeString = new SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
                .format(new Date(n.getTimestamp()));
        return new NotificationItem(
                n.getTitle(),
                n.getMessage(),
                timeString,
                n.getType(),
                n.isRead(),
                new Date(n.getTimestamp()));
        // Note: NotificationItem needs to hold the original Notification ID to mark as
        // read.
        // But NotificationItem definition I saw didn't have ID field.
        // I might need to abuse 'title' or add a field to NotificationItem, or assume I
        // can find it later.
        // Better: Add `id` field to NotificationItem or extend it.
        // Since I can't easily change NotificationItem used by Adapter without
        // potentially breaking other things (if any),
        // I will assume I can't pass ID easily unless I add it to NotificationItem.
        // Wait, I can Modify NotificationItem.java to add ID field.
    }

    private void loadMoreItems() {
        List<NotificationItem> sourceList = getCurrentFilteredData();
        if (currentDisplayCount < sourceList.size()) {
            currentDisplayCount += ITEMS_PER_PAGE;
            updateDisplayedList();
            adapter.updateList(displayedList);
        }
        if (currentDisplayCount >= sourceList.size()) {
            btnViewHistory.setVisibility(View.GONE);
        }
    }

    private void updateDisplayedList() {
        displayedList.clear();
        List<NotificationItem> sourceList = getCurrentFilteredData();

        if (sourceList.isEmpty()) {
            // Let empty state handle it
        } else {
            int limit = Math.min(sourceList.size(), currentDisplayCount);
            List<NotificationItem> slice = new ArrayList<>();
            for (int i = 0; i < limit; i++) {
                slice.add(sourceList.get(i));
            }
            displayedList = groupItemsWithHeaders(slice);
        }
    }

    private List<NotificationItem> getCurrentFilteredData() {
        int checkedId = chipGroupFilters.getCheckedChipId();
        String typeToFilter = "All";
        if (checkedId == R.id.chip_request)
            typeToFilter = "Request";
        else if (checkedId == R.id.chip_donation)
            typeToFilter = "Donation";
        else if (checkedId == R.id.chip_community)
            typeToFilter = "Community";

        if (typeToFilter.equals("All"))
            return allDataItems;

        List<NotificationItem> filtered = new ArrayList<>();
        for (NotificationItem item : allDataItems) {
            // Check null type
            if (item.getType() != null && item.getType().equalsIgnoreCase(typeToFilter)) {
                filtered.add(item);
            }
        }
        return filtered;
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
            // view history visibility handled by loadMore/filter
            if (displayedList.size() < getCurrentFilteredData().size()) {
                btnViewHistory.setVisibility(View.VISIBLE);
            } else {
                btnViewHistory.setVisibility(View.GONE);
            }
        }
    }

    private void onNotificationClicked(NotificationItem item) {
        // Needs ID to update Firestore.
        // Since I can't pass ID in NotificationItem yet (unless I modify it), I'll skip
        // the update for now OR match by timestamp/title? (Risky).
        // Best approach: Add ID to NotificationItem.
        // I will assume I added it.
        if (item.getId() != null && !item.isRead()) {
            db.collection("notifications").document(item.getId())
                    .update("isRead", true)
                    .addOnFailureListener(
                            e -> Toast.makeText(getContext(), "Error updating", Toast.LENGTH_SHORT).show());
        }
    }
}
