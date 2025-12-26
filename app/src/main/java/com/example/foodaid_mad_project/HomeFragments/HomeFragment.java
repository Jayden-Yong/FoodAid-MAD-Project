package com.example.foodaid_mad_project.HomeFragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.example.foodaid_mad_project.AuthFragments.User;
import com.example.foodaid_mad_project.Model.FoodItem;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.UserManager;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <h1>HomeFragment</h1>
 * <p>
 * The main landing fragment of the application.
 * Features:
 * <ul>
 * <li>Displays a welcome message to the user.</li>
 * <li>Provides a Search Bar and Category Filters (Groceries vs Meals).</li>
 * <li>Embeds the {@link MapFragment} to show donation locations.</li>
 * <li>Handles Notification Badge updates by listening to Firestore.</li>
 * <li>Navigates to {@link NotificationFragment} and shows
 * {@link MapPinItemFragment} details.</li>
 * </ul>
 * </p>
 */
public class HomeFragment extends Fragment {

    private String email, username, welcomeDisplay;
    private TextView tvWelcomeUser;
    private ListenerRegistration notificationListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Setup Header & User Info
        setupHeader(view);

        // 2. Setup Search & Filters
        setupSearchAndFilters(view);

        // 3. Load Map Fragment
        loadMapFragment();

        // 4. Setup Notification Button & Badge
        setupNotifications(view);
    }

    /**
     * initializes welcome text based on the current user session.
     */
    private void setupHeader(View view) {
        tvWelcomeUser = view.findViewById(R.id.tvWelcomeUser);

        try {
            User user = UserManager.getInstance().getUser();
            if (user != null) {
                email = user.getEmail();

                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    username = user.getDisplayName();
                } else if (email != null && email.contains("@")) {
                    username = email.substring(0, email.indexOf("@")).toUpperCase();
                } else {
                    username = "User";
                }
                welcomeDisplay = username;
            }
        } catch (NullPointerException e) {
            welcomeDisplay = "Guest";
        }
        tvWelcomeUser.setText(getString(R.string.Welcome_User, "morning", welcomeDisplay));
    }

    /**
     * sets up the search bar listener and chip group filters for the map.
     */
    private void setupSearchAndFilters(View view) {
        // Chip Group (Filtering)
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupFilters);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String category = "All";
            if (checkedIds.contains(R.id.chipGroceries)) {
                category = "Groceries";
            } else if (checkedIds.contains(R.id.chipMeals)) {
                category = "Meals";
            }

            MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.MapFragment);
            if (mapFragment != null) {
                mapFragment.filterItems(category);
            }
        });

        // Search Bar
        EditText etSearch = view.findViewById(R.id.etSearch);
        // Real-time search listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.MapFragment);
                if (mapFragment != null) {
                    // Get current category from chips or default to "All"
                    String currentCategory = "All";
                    ChipGroup chipGroup = view.findViewById(R.id.chipGroupFilters);
                    if (chipGroup.getCheckedChipId() == R.id.chipGroceries) currentCategory = "Groceries";
                    else if (chipGroup.getCheckedChipId() == R.id.chipMeals) currentCategory = "Meals";

                    // Filter map immediately
                    mapFragment.filterItems(currentCategory, s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listen Keyboard Enter, but addTextChanged above already handles everything
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchLocation(query);
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Dynamically replaces the placeholder with the MapFragment instance.
     */
    private void loadMapFragment() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.MapFragment, new MapFragment())
                .commit();
    }

    /**
     * Wired up the navigation to NotificationFragment and starts the badge
     * listener.
     */
    private void setupNotifications(View view) {
        ImageButton btnToNotification = view.findViewById(R.id.btnToNotification);
        View notificationBadge = view.findViewById(R.id.notificationBadge);

        btnToNotification.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.coveringFragment, new NotificationFragment())
                    .addToBackStack("NotificationFragment")
                    .commit();
        });

        // Listen for unread notifications to toggle red dot
        setupNotificationListener(notificationBadge);
    }

    /**
     * Real-time listener for "notifications" collection to show/hide the red badge.
     * Checks both personal notifications and Global ("ALL") ones.
     */
    private void setupNotificationListener(View badge) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return;

        notificationListener = FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereIn("userId", Arrays.asList(FirebaseAuth.getInstance().getCurrentUser().getUid(), "ALL"))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20) // Limit check to 20 latest for performance
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null)
                        return;

                    boolean hasUnread = false;
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            boolean isRead = Boolean.TRUE.equals(doc.getBoolean("isRead"));
                            String docUserId = doc.getString("userId");
                            String docId = doc.getId();

                            if ("ALL".equals(docUserId)) {
                                // Global notification: Check SharedPreferences for read status
                                if (!isGlobalReadLocally(docId)) {
                                    hasUnread = true;
                                    break;
                                }
                            } else {
                                // Personal notification: Check Firestore field
                                if (!isRead) {
                                    hasUnread = true;
                                    break;
                                }
                            }
                        }
                    }

                    badge.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                });
    }

    /**
     * Checks SharedPreferences to see if a Global Notification ID has been marked
     * as read.
     */
    private boolean isGlobalReadLocally(String notificationId) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null || getContext() == null)
            return false;

        SharedPreferences prefs = requireContext().getSharedPreferences(
                "foodaid_prefs_" + FirebaseAuth.getInstance().getCurrentUser().getUid(),
                android.content.Context.MODE_PRIVATE);
        Set<String> readSet = prefs.getStringSet("read_global_ids", new HashSet<>());
        return readSet.contains(notificationId);
    }

    /**
     * Called by MapFragment when a pin is clicked. Shows the mini-detail fragment.
     */
    public void showMapPinDetails(FoodItem item) {
        FragmentContainerView mapPinContainer = getView().findViewById(R.id.MapPinFragment);
        if (mapPinContainer == null)
            return;

        mapPinContainer.setVisibility(View.VISIBLE);

        MapPinItemFragment pinFragment = new MapPinItemFragment(item);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.MapPinFragment, pinFragment)
                .commit();
    }

    /**
     * Searches for a FoodItem in the MapFragment based on title or location name.
     */
    private void searchLocation(String query) {
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.MapFragment);
        if (mapFragment == null)
            return;

        List<FoodItem> items = mapFragment.getFoodItems();
        if (items.isEmpty()) {
            showFoodbankNotFoundMessage();
            return;
        }

        FoodItem foundItem = null;
        String lowerQuery = query.toLowerCase().trim();
        for (FoodItem item : items) {
            boolean matchLocation = item.getLocationName() != null
                    && item.getLocationName().toLowerCase().contains(lowerQuery);
            boolean matchTitle = item.getTitle() != null && item.getTitle().toLowerCase().contains(lowerQuery);

            if (matchLocation || matchTitle) {
                foundItem = item;
                break;
            }
        }

        if (foundItem != null) {
            mapFragment.focusOnFoodItem(foundItem);
        } else {
            showFoodbankNotFoundMessage();
        }
    }

    private void showFoodbankNotFoundMessage() {
        Toast toast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);

        TextView tv = new TextView(getContext());
        tv.setText("Foodbank not found");
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setBackgroundColor(android.graphics.Color.parseColor("#AA000000")); // semi-transparent black
        tv.setPadding(30, 20, 30, 20);
        tv.setTextSize(16);
        tv.setGravity(android.view.Gravity.CENTER);

        toast.setView(tv);
        toast.setGravity(android.view.Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }
}