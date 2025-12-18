package com.example.foodaid_mad_project.HomeFragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.Model.FoodItem;
import com.example.foodaid_mad_project.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * <h1>MapFragment</h1>
 * <p>
 * Displays an OpenStreetMap view showing available food banks and donations.
 * Features:
 * <ul>
 * <li>Real-time updates from "donations" collection in Firestore.</li>
 * <li>User location tracking (with permission).</li>
 * <li>Filtering by category (Groceries/Meals).</li>
 * <li>Interactive pins that open details in {@link MapPinItemFragment}.</li>
 * </ul>
 * </p>
 */
public class MapFragment extends Fragment {

    private static final int LOCATION_REQUEST_CODE = 1001;
    // Default Location: Universiti Malaya
    private static final GeoPoint DEFAULT_LOCATION = new GeoPoint(3.1207, 101.6544);

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private ListenerRegistration firestoreListener;
    private List<FoodItem> foodItems = new ArrayList<>();
    private String currentCategory = "All";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // 1. Configure Osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(new java.io.File(requireContext().getFilesDir(), "osmdroid"));
        Configuration.getInstance()
                .setOsmdroidTileCache(new java.io.File(requireContext().getFilesDir(), "osmdroid/tiles"));

        // 2. Setup MapView
        mapView = view.findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);

        IMapController controller = mapView.getController();
        controller.setZoom(16.0);
        controller.setCenter(DEFAULT_LOCATION);

        // 3. Start Data Listener
        listenToAvailableDonations();

        // 4. Setup User Location
        checkAndSetupLocation();

        return view;
    }

    /**
     * Checks for location permission. If granted, enables location overlay.
     * Otherwise requests permission.
     */
    private void checkAndSetupLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_REQUEST_CODE);
        } else {
            setupUserLocation();
        }
    }

    /**
     * Enables the "My Location" blue dot and fly-to-location behavior.
     */
    private void setupUserLocation() {
        if (mapView == null)
            return;

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        mapView.getOverlays().add(locationOverlay);

        // Fly to user location on first fix
        locationOverlay.runOnFirstFix(() -> requireActivity().runOnUiThread(() -> {
            GeoPoint userPoint = locationOverlay.getMyLocation();
            if (userPoint != null && mapView != null) {
                IMapController controller = mapView.getController();
                controller.setZoom(16.5);
                controller.setCenter(userPoint);
                addLocationMarker(userPoint, "You are here");
            }
        }));
    }

    /**
     * Adds a generic marker for user location (optional explicit marker).
     */
    private void addLocationMarker(GeoPoint point, String title) {
        if (mapView == null)
            return;
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
    }

    /**
     * Listens for available donations in Firestore that haven't expired.
     */
    private void listenToAvailableDonations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long currentTime = System.currentTimeMillis();

        if (firestoreListener != null) {
            firestoreListener.remove();
        }

        firestoreListener = db.collection("donations")
                .whereEqualTo("status", "AVAILABLE")
                .whereGreaterThan("endTime", currentTime) // Server-side filter for future expiry
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }

                    if (snapshots != null) {
                        foodItems.clear();

                        for (QueryDocumentSnapshot doc : snapshots) {
                            FoodItem item = doc.toObject(FoodItem.class);
                            item.setDonationId(doc.getId());

                            // Double check expiration manually (Safety belt)
                            if (item.getEndTime() > 0 && item.getEndTime() < System.currentTimeMillis()) {
                                continue;
                            }
                            foodItems.add(item);
                        }
                        // Refresh map with current data and filter
                        filterItems(currentCategory);
                    }
                });
    }

    /**
     * Adds a donation pin to the map.
     */
    private void addFoodMarker(FoodItem item) {
        if (mapView == null)
            return;

        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(item.getLatitude(), item.getLongitude()));
        marker.setIcon(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_custom_pin));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Disable default info window to use custom bottom sheet
        marker.setInfoWindow(null);

        marker.setOnMarkerClickListener((m, mapView) -> {
            if (getParentFragment() instanceof HomeFragment) {
                ((HomeFragment) getParentFragment()).showMapPinDetails(item);
            }
            return true;
        });

        mapView.getOverlays().add(marker);
    }

    /**
     * Filters displayed items by category ("All", "Groceries", "Meals").
     */
    public void filterItems(String category) {
        this.currentCategory = category;
        if (mapView == null)
            return;

        // Clear existing food markers (Keep "You are here" marker if possible, or just
        // rebuild overlays)
        // Ideally we differentiate by some usage ID, but checking title is a safe
        // heuristic here.
        List<Overlay> overlaysToRemove = new ArrayList<>();
        for (Overlay o : mapView.getOverlays()) {
            if (o instanceof Marker) {
                Marker m = (Marker) o;
                if (!"You are here".equals(m.getTitle())) {
                    overlaysToRemove.add(o);
                }
            }
        }
        mapView.getOverlays().removeAll(overlaysToRemove);

        // Re-add matching markers
        for (FoodItem item : foodItems) {
            boolean matches = false;
            if ("All".equals(category)) {
                matches = true;
            } else if (item.getCategory() != null) {
                if (category.equalsIgnoreCase("Groceries") &&
                        (item.getCategory().equalsIgnoreCase("GROCERIES")
                                || item.getCategory().equalsIgnoreCase("Pantry"))) {
                    matches = true;
                } else if (category.equalsIgnoreCase("Meals") &&
                        (item.getCategory().equalsIgnoreCase("MEALS")
                                || item.getCategory().equalsIgnoreCase("Leftover"))) {
                    matches = true;
                }
            }

            if (matches) {
                addFoodMarker(item);
            }
        }
        mapView.invalidate();
    }

    /**
     * Centers map on a specific food item and zooms in.
     */
    public void focusOnFoodItem(FoodItem item) {
        if (item == null || mapView == null)
            return;

        GeoPoint point = new GeoPoint(item.getLatitude(), item.getLongitude());

        // Temporarily stop auto-following user to allow manual look
        if (locationOverlay != null) {
            locationOverlay.disableFollowLocation();
        }

        mapView.post(() -> {
            if (mapView == null)
                return;
            IMapController controller = mapView.getController();
            controller.setZoom(20.0);
            controller.animateTo(point);

            // Find matching marker and simulate click (optional visual cue)
            // Note: markers don't have IDs easily set here, so we match by position
        });

        // Trigger the details view in HomeFragment
        if (getParentFragment() instanceof HomeFragment) {
            ((HomeFragment) getParentFragment()).showMapPinDetails(item);
        }
    }

    public List<FoodItem> getFoodItems() {
        return foodItems != null ? foodItems : new ArrayList<>();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupUserLocation();
            }
        }
    }

    // Lifecycle Management for MapView and Listeners

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null)
            mapView.onResume();
        if (locationOverlay != null)
            locationOverlay.enableMyLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null)
            mapView.onPause();
        if (locationOverlay != null)
            locationOverlay.disableMyLocation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
        if (mapView != null) {
            mapView.onDetach();
            mapView = null;
        }
        locationOverlay = null;
    }
}
