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
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MapFragment extends Fragment {

    private static final int LOCATION_REQUEST_CODE = 1001;
    private static final GeoPoint DEFAULT_LOCATION = new GeoPoint(3.1207, 101.6544); // Universiti Malaya

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;

    // Mock Food Items (replace with Firebase later)
    private List<FoodItem> foodItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapView = view.findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);

        // Initial map view
        IMapController controller = mapView.getController();
        controller.setZoom(20.0);
        controller.setCenter(DEFAULT_LOCATION);
        /*
         * // Setup mock data
         * generateMockData();
         * 
         * // Add pins
         * for (FoodItem item : foodItems) {
         * addFoodMarker(item);
         * }
         */
        // Listen to available donations from Firestore
        listenToAvailableDonations();

        checkAndSetupLocation();

        return view;
    }

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

    private void setupUserLocation() {
        locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(requireContext()), mapView);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        mapView.getOverlays().add(locationOverlay);

        locationOverlay.runOnFirstFix(() -> requireActivity().runOnUiThread(() -> {
            GeoPoint userPoint = locationOverlay.getMyLocation();

            IMapController controller = mapView.getController();
            controller.setZoom(20.0);

            if (userPoint != null) {
                controller.setCenter(userPoint);
                addMarker(userPoint, "You are here");
            }
        }));
    }

    private void addMarker(GeoPoint point, String title) {
        if (mapView == null)
            return;
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
    }

    private void addFoodMarker(FoodItem item) {
        if (mapView == null)
            return;
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(item.getLatitude(), item.getLongitude()));
        marker.setIcon(AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_custom_pin));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Remove default InfoWindow
        marker.setInfoWindow(null);

        marker.setOnMarkerClickListener((m, mapView) -> {
            if (getParentFragment() instanceof HomeFragment) {
                ((HomeFragment) getParentFragment()).showMapPinDetails(item);
            }
            return true;
        });

        mapView.getOverlays().add(marker);
    }

    /*
     * private void generateMockData() {
     * foodItems = new ArrayList<>();
     * foodItems.add(new FoodItem("1", "Tiger Biscuits", "Universiti Malaya",
     * "50 packs", "Student Council", R.drawable.ic_launcher_background, 3.1209,
     * 101.6538, 100));
     * foodItems.add(new FoodItem("2", "Leftover Catering", "Mid Valley", "20 kg",
     * "Grand Hotel", R.drawable.ic_launcher_background, 3.1176, 101.6776, 545));
     * foodItems.add(new FoodItem("3", "Canned Soup", "Jaya One", "100 cans",
     * "Community NGO", R.drawable.ic_launcher_background, 3.1180, 101.6360, 15));
     * }
     */

    private com.google.firebase.firestore.ListenerRegistration firestoreListener;

    private void listenToAvailableDonations() {
        foodItems = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long currentTime = System.currentTimeMillis();

        if (firestoreListener != null) {
            firestoreListener.remove();
        }

        firestoreListener = db.collection("donations")
                .whereEqualTo("status", "AVAILABLE")
                .whereGreaterThan("endTime", currentTime)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }

                    if (snapshots != null) {
                        foodItems.clear();

                        for (QueryDocumentSnapshot doc : snapshots) {
                            FoodItem item = doc.toObject(FoodItem.class);
                            item.setDonationId(doc.getId()); // Ensure ID is set
                            foodItems.add(item);
                        }

                        // Apply current filter
                        filterItems(currentCategory);
                    }
                });
    }

    private String currentCategory = "All";

    public void filterItems(String category) {
        this.currentCategory = category;
        if (mapView == null)
            return;

        // Clear existing food markers
        for (Overlay o : new ArrayList<>(mapView.getOverlays())) {
            if (o instanceof Marker) {
                Marker m = (Marker) o;
                if (!"You are here".equals(m.getTitle())) {
                    mapView.getOverlays().remove(o);
                }
            }
        }

        for (FoodItem item : foodItems) {
            boolean matches = false;
            if (category.equals("All")) {
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

    public void focusOnFoodItem(FoodItem item) {
        if (item == null || mapView == null)
            return;

        GeoPoint point = new GeoPoint(item.getLatitude(), item.getLongitude());

        // Temporarily stop following user location
        if (locationOverlay != null) {
            locationOverlay.disableFollowLocation();
        }

        mapView.post(() -> {
            if (mapView == null)
                return;
            IMapController controller = mapView.getController();
            controller.setZoom(20.0);
            controller.animateTo(point);

            // Show the info window of the marker
            for (Overlay overlay : mapView.getOverlays()) {
                if (overlay instanceof Marker) {
                    Marker marker = (Marker) overlay;
                    if (marker.getPosition().equals(point)) {
                        marker.showInfoWindow();
                        break;
                    }
                }
            }
        });

        // Show pin details in parent fragment
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
        // Properly allow MapView to detach
        if (mapView != null) {
            mapView.onDetach(); // Osmdroid cleanup
            mapView = null;
        }
        locationOverlay = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
        if (locationOverlay == null || locationOverlay.getMyLocationProvider() == null) {
            // Re-setup if needed, but be careful of duplication
            if (mapView != null)
                setupUserLocation();
        }

        if (locationOverlay != null) {
            locationOverlay.enableMyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        if (locationOverlay != null)
            locationOverlay.disableMyLocation();
    }
}
