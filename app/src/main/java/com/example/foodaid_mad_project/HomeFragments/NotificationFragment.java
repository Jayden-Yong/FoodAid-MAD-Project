package com.example.foodaid_mad_project.HomeFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.Model.NotificationItem;
import com.example.foodaid_mad_project.R;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationItem> allDataItems; // Raw data only
    private List<NotificationItem> displayedList; // Data + Headers
    private ChipGroup chipGroupFilters;
    private TextView btnViewHistory;

    private int currentDisplayCount = 5;
    private static final int ITEMS_PER_PAGE = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvNotifications = view.findViewById(R.id.rv_notifications);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        btnViewHistory = view.findViewById(R.id.tv_view_history);

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        // 1. Generate Data with timestamps
        generateMockData();

        // 2. Sort Data (Newest first)
        Collections.sort(allDataItems, (o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));

        // 3. Initial Display
        displayedList = new ArrayList<>();
        updateDisplayedList();

        adapter = new NotificationAdapter(displayedList);
        rvNotifications.setAdapter(adapter);

        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText(getString(R.string.String, "Food Aid Details"));

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Manually pop the Donate Fragment
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("ItemDetail", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });

        Toolbar toolbar = view.findViewById(R.id.Toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("ItemDetail", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            });
        }

        // Filter Logic
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            currentDisplayCount = ITEMS_PER_PAGE;
            btnViewHistory.setVisibility(View.VISIBLE);
            updateDisplayedList();
            adapter.updateList(displayedList);
        });

        // Load More
        btnViewHistory.setOnClickListener(v -> {
            loadMoreItems();
        });
    }

    private void loadMoreItems() {
        // We filter the raw data first to know the total available for this filter
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
        int limit = Math.min(sourceList.size(), currentDisplayCount);

        // Slice the list
        List<NotificationItem> slice = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            slice.add(sourceList.get(i));
        }

        // Insert Headers into the slice
        displayedList = groupItemsWithHeaders(slice);
    }

    private List<NotificationItem> getCurrentFilteredData() {
        int checkedId = chipGroupFilters.getCheckedChipId();
        String typeToFilter = "All";
        if (checkedId == R.id.chip_request) typeToFilter = "Request";
        else if (checkedId == R.id.chip_donation) typeToFilter = "Donation";
        else if (checkedId == R.id.chip_community) typeToFilter = "Community";

        if (typeToFilter.equals("All")) return allDataItems;

        List<NotificationItem> filtered = new ArrayList<>();
        for (NotificationItem item : allDataItems) {
            if (item.getType().equalsIgnoreCase(typeToFilter)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private List<NotificationItem> groupItemsWithHeaders(List<NotificationItem> items) {
        List<NotificationItem> grouped = new ArrayList<>();

        if (items.isEmpty()) return grouped;

        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance(); yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Calendar thisWeek = Calendar.getInstance(); thisWeek.add(Calendar.DAY_OF_YEAR, -7);
        Calendar lastWeek = Calendar.getInstance(); lastWeek.add(Calendar.DAY_OF_YEAR, -14);

        String currentHeader = "";

        for (NotificationItem item : items) {
            String itemHeader = getHeaderForDate(item.getTimestamp(), today, yesterday, thisWeek, lastWeek);

            if (!itemHeader.equals(currentHeader)) {
                grouped.add(new NotificationItem(itemHeader)); // Add Header
                currentHeader = itemHeader;
            }
            grouped.add(item); // Add Item
        }
        return grouped;
    }

    private String getHeaderForDate(Date date, Calendar today, Calendar yesterday, Calendar thisWeek, Calendar lastWeek) {
        Calendar target = Calendar.getInstance();
        target.setTime(date);

        if (isSameDay(target, today)) return "Today";
        if (isSameDay(target, yesterday)) return "Yesterday";
        if (target.after(thisWeek)) return "This week";
        if (target.after(lastWeek)) return "Last week";
        return "A long time ago";
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void generateMockData() {
        allDataItems = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        // 1. Today
        allDataItems.add(createItem("Donation Approved", "Tiger Biscuits approved", "10:00 AM", "Donation", cal.getTime()));

        // 2. Yesterday
        cal.add(Calendar.DATE, -1);
        allDataItems.add(createItem("Pickup Reminder", "Collect at UM", "2:00 PM", "Pickup", cal.getTime()));
        allDataItems.add(createItem("Request Declined", "Stock unavailable", "4:00 PM", "Request", cal.getTime()));

        // 3. This Week (e.g., -3 days)
        cal = Calendar.getInstance(); cal.add(Calendar.DATE, -3);
        allDataItems.add(createItem("Community Post", "5 new comments", "Mon", "Community", cal.getTime()));
        cal.add(Calendar.DATE, -1); // -4 days
        allDataItems.add(createItem("Impact Update", "Saved 5kg food", "Sun", "Impact", cal.getTime()));

        // 4. Last Week (e.g., -9 days)
        cal = Calendar.getInstance(); cal.add(Calendar.DATE, -9);
        allDataItems.add(createItem("Donation Received", "Canned Soup", "Last Mon", "Donation", cal.getTime()));
        allDataItems.add(createItem("Pickup Completed", "Rice donation picked up", "Last Sun", "Pickup", cal.getTime()));

        // 5. A Long Time Ago (e.g., -20 days)
        cal = Calendar.getInstance(); cal.add(Calendar.DATE, -20);
        allDataItems.add(createItem("Welcome", "Account created", "Nov 20", "System", cal.getTime()));

        // Add more bulk items for scrolling...
        for(int i=0; i<15; i++) {
            cal.add(Calendar.DATE, -1);
            allDataItems.add(createItem("Old Activity " + i, "Description " + i, "Date", "System", cal.getTime()));
        }
    }

    private NotificationItem createItem(String title, String desc, String time, String type, Date date) {
        return new NotificationItem(title, desc, time, type, false, date);
    }
}