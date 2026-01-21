package com.example.foodaid_mad_project.HomeFragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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

import com.example.foodaid_mad_project.Utils.ImageUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * MapFragment
 *
 * Displays an OpenStreetMap view showing available food banks and donations.
 * Features:
 * - Real-time updates from "donations" collection in Firestore.
 * - User location tracking (with permission).
 * - Filtering by category (Groceries/Meals).
 * - Interactive pins that open details in MapPinItemFragment.
 */
public class MapFragment extends Fragment {

    private static final int LOCATION_REQUEST_CODE = 1001;
    // Default Location: Universiti Malaya
    private static final GeoPoint DEFAULT_LOCATION = new GeoPoint(3.1207, 101.6544);
    private static final float PIN_SCALE_FACTOR = 1.0f;

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private ListenerRegistration firestoreListener;
    private List<FoodItem> foodItems = new ArrayList<>();
    private String currentCategory = "All";
    private FoodItem singleItemModeItem; // If set, map shows only this item
    private String currentSearchQuery = ""; // For real-time search

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

        mapView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false; // Perform default map actions
        });

        IMapController controller = mapView.getController();
        controller.setZoom(16.0);
        controller.setCenter(DEFAULT_LOCATION);

        // 3. Start Data Listener
        if (singleItemModeItem != null) {
            setupSingleItemMap();
        } else {
            listenToAvailableDonations();
        }

        // 4. Setup User Location
        checkAndSetupLocation();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If in single item mode, don't listen to all donations, just show one
        if (singleItemModeItem != null) {
            setupSingleItemMap();
        }
        // Logic will be handled in listenToAvailableDonations if singleItemModeItem is null
    }

    /**
     * Checks for location permission. If granted, enables location overlay.
     * Otherwise requests permission.
     */
    private void checkAndSetupLocation() {
        if (getContext() == null) return;
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
        if (mapView == null || getContext() == null) return;
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        locationOverlay.enableMyLocation();

        if (singleItemModeItem == null) {
            locationOverlay.enableFollowLocation();

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

        mapView.getOverlays().add(locationOverlay);
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
        if (singleItemModeItem != null) return; // Don't listen to global updates in single mode

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long currentTime = System.currentTimeMillis();

        if (firestoreListener != null) firestoreListener.remove();

        firestoreListener = db.collection("donations")
                .whereEqualTo("status", "AVAILABLE")
                .whereGreaterThan("endTime", currentTime)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    foodItems.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        FoodItem item = doc.toObject(FoodItem.class);
                        item.setDonationId(doc.getId());
                        if (item.getEndTime() > System.currentTimeMillis()) {
                            foodItems.add(item);
                        }
                    }
                    refreshMapMarkers();
                });
    }

    private void setupSingleItemMap() {
        if (mapView == null || singleItemModeItem == null) return;

        // Clear old overlays
        mapView.getOverlays().clear();

        // Center map on item
        GeoPoint point = new GeoPoint(singleItemModeItem.getLatitude(), singleItemModeItem.getLongitude());
        mapView.getController().setZoom(18.0);
        mapView.getController().setCenter(point);

        // Add specific marker
        addFoodMarker(singleItemModeItem);
    }

    /**
     * Adds a donation pin to the map.
     */
    private void addFoodMarker(FoodItem item) {
        if (mapView == null || getContext() == null) return;

        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(item.getLatitude(), item.getLongitude()));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setInfoWindow(null);

        marker.setOnMarkerClickListener((m, mapView) -> {
            if (getParentFragment() instanceof HomeFragment) {
                ((HomeFragment) getParentFragment()).showMapPinDetails(item);
            }
            return true;
        });

        // 1. Set Scaled Default Icon
        // We scale the default icon too so it matches the size of the image-loaded icon
        marker.setIcon(new BitmapDrawable(getResources(), getScaledDefaultPin()));
        mapView.getOverlays().add(marker);

//        // 2. Load Image & Composite
//        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
//            CustomTarget<Bitmap> target = new CustomTarget<Bitmap>() {
//                @Override
//                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                    if (isAdded() && mapView != null) {
//                        marker.setIcon(new BitmapDrawable(getResources(), createCustomPin(resource)));
//                        mapView.invalidate();
//                    }
//                }
//                @Override
//                public void onLoadCleared(@Nullable Drawable placeholder) {}
//            };
//
//            if (!item.getImageUri().startsWith("http")) {
//                try {
//                    byte[] imageBytes = ImageUtil.base64ToBytes(item.getImageUri());
//                    Glide.with(requireContext()).asBitmap().load(imageBytes).into(target);
//                } catch (Exception e) { /* keep default */ }
//            } else {
//                Glide.with(requireContext()).asBitmap().load(item.getImageUri()).into(target);
//            }
//        }
    }

    /**
     * Creates a scaled-up default pin without an image.
     */
    private Bitmap getScaledDefaultPin() {
        Drawable pinDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_custom_pin);
        Bitmap originalPin = ((BitmapDrawable) pinDrawable).getBitmap();
        int width = (int) (originalPin.getWidth() * PIN_SCALE_FACTOR);
        int height = (int) (originalPin.getHeight() * PIN_SCALE_FACTOR);
        return Bitmap.createScaledBitmap(originalPin, width, height, true);
    }

    /**
     * Creates a scaled-up pin with the food image composited inside.
     */
    private Bitmap createCustomPin(Bitmap foodImage) {
        // 1. Get Base Pin & Scale It
        Drawable pinDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_custom_pin);
        Bitmap originalPin = ((BitmapDrawable) pinDrawable).getBitmap();

        int width = (int) (originalPin.getWidth() * PIN_SCALE_FACTOR);
        int height = (int) (originalPin.getHeight() * PIN_SCALE_FACTOR);

        Bitmap scaledPin = Bitmap.createScaledBitmap(originalPin, width, height, true);

        // 2. Create Canvas
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // 3. Draw Background Pin
        canvas.drawBitmap(scaledPin, 0, 0, null);

        // 4. Calculate Window for Food Image (Relative to NEW size)
        // Adjust these ratios if the hole isn't perfectly centered
        int diameter = (int) (width * 0.76);
        int xOffset = (width - diameter) / 2;
        int yOffset = (int) (width * 0.12);

        // 5. Crop & Draw Food Image
        Bitmap scaledFood = Bitmap.createScaledBitmap(foodImage, diameter, diameter, false);
        Bitmap circularFood = getCircularBitmap(scaledFood);

        canvas.drawBitmap(circularFood, xOffset, yOffset, null);

        return result;
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xFF000000);
        canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getWidth() / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * Filters displayed items by category ("All", "Groceries", "Meals").
     */
    public void filterItems(String category, String searchQuery) {
        this.currentCategory = category;
        this.currentSearchQuery = searchQuery != null ? searchQuery.toLowerCase().trim() : "";
        refreshMapMarkers();
    }

    // Backwards compatibility
    public void filterItems(String category) {
        filterItems(category, "");
    }

    private void refreshMapMarkers() {
        if (mapView == null) return;

        // Clear existing food markers (Keep "You are here" marker if possible, or just
        // rebuild overlays)
        // Ideally we differentiate by some usage ID, but checking title is a safe
        // heuristic here.
        List<Overlay> overlaysToRemove = new ArrayList<>();
        for (Overlay o : mapView.getOverlays()) {
            if (o instanceof Marker && !"You are here".equals(((Marker) o).getTitle())) {
                overlaysToRemove.add(o);
            }
        }
        mapView.getOverlays().removeAll(overlaysToRemove);

        // Re-add matching markers
        for (FoodItem item : foodItems) {
            boolean categoryMatch = "All".equals(currentCategory) ||
                    (item.getCategory() != null &&
                            (currentCategory.equalsIgnoreCase("Groceries") ?
                                    (item.getCategory().equalsIgnoreCase("GROCERIES") || item.getCategory().equalsIgnoreCase("Pantry")) :
                                    (item.getCategory().equalsIgnoreCase("MEALS") || item.getCategory().equalsIgnoreCase("Leftover"))));

            boolean searchMatch = currentSearchQuery.isEmpty() ||
                    item.getTitle().toLowerCase().contains(currentSearchQuery) ||
                    item.getLocationName().toLowerCase().contains(currentSearchQuery);

            if (categoryMatch && searchMatch) {
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

    public void setSingleItemMode(FoodItem item) {
        this.singleItemModeItem = item;
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
