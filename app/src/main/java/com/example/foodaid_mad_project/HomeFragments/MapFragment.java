package com.example.foodaid_mad_project.HomeFragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodaid_mad_project.Model.FoodBank;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.SharedViewModel;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;

public class MapFragment extends Fragment {

    private static final int LOCATION_REQUEST_CODE = 1001;
    private static final GeoPoint DEFAULT_LOCATION = new GeoPoint(3.1207, 101.6544); // Universiti Malaya

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private SharedViewModel sharedViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapView = view.findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);

        // Initial map view
        IMapController controller = mapView.getController();
        controller.setZoom(15.0); // Detailed zoom
        controller.setCenter(DEFAULT_LOCATION);

        checkAndSetupLocation();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Connect Sync (Our Code)
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getFoodBanks().observe(getViewLifecycleOwner(), this::updateMarkers);
    }

    private void updateMarkers(List<FoodBank> foodBanks) {
        if (mapView == null)
            return;

        // Clear old markers but KEEP the MyLocationOverlay
        // We iterate backwards to remove only Markers
        for (int i = mapView.getOverlays().size() - 1; i >= 0; i--) {
            if (mapView.getOverlays().get(i) instanceof Marker) {
                mapView.getOverlays().remove(i);
            }
        }

        // Add pins
        if (foodBanks != null) {
            for (FoodBank item : foodBanks) {
                addFoodMarker(item);
            }
        }

        mapView.invalidate(); // Refresh map
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
        if (getContext() == null)
            return;

        locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(requireContext()), mapView);
        locationOverlay.enableMyLocation();
        // locationOverlay.enableFollowLocation(); // Optional: Auto-follow
        mapView.getOverlays().add(locationOverlay);

        locationOverlay.runOnFirstFix(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    GeoPoint userPoint = locationOverlay.getMyLocation();
                    if (userPoint != null) {
                        IMapController controller = mapView.getController();
                        controller.animateTo(userPoint);
                    }
                });
            }
        });
    }

    private void addFoodMarker(FoodBank item) {
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(item.getLat(), item.getLng()));
        marker.setTitle(item.getName());
        marker.setIcon(AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_custom_pin // Ensure this drawable exists
        ));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        marker.setOnMarkerClickListener((m, mapView) -> {
            if (getParentFragment() instanceof HomeFragment) {
                ((HomeFragment) getParentFragment()).showMapPinDetails(item);
            }
            return true;
        });

        mapView.getOverlays().add(marker);
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
}
