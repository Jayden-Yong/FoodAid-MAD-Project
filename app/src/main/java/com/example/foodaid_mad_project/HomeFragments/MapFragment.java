package com.example.foodaid_mad_project.HomeFragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapFragment extends Fragment {

    private static final int LOCATION_REQUEST_CODE = 1001;
    private static final GeoPoint DEFAULT_LOCATION = new GeoPoint(3.1207, 101.6544); // Universiti Malaya

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapView = view.findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);

        checkAndSetupLocation();

        return view;
    }

    private void checkAndSetupLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE
            );
        } else {
            setupUserLocation();
        }
    }

    private void setupUserLocation() {
        locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(requireContext()), mapView
        );
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        mapView.getOverlays().add(locationOverlay);

        locationOverlay.runOnFirstFix(() -> requireActivity().runOnUiThread(() -> {
            GeoPoint userPoint = locationOverlay.getMyLocation();

            IMapController controller = mapView.getController();
            controller.setZoom(20.0);

            if (userPoint != null) {
                // User location available
                controller.setCenter(userPoint);
                addMarker(userPoint, "You are here");
            } else {
                // Fallback to Universiti Malaya
                controller.setCenter(DEFAULT_LOCATION);
                addMarker(DEFAULT_LOCATION, "Universiti Malaya");
            }
        }));
    }

    private void addMarker(GeoPoint point, String title) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupUserLocation();
            } else {
                // Permission denied -> fallback to default
                mapView.getController().setZoom(20.0);
                mapView.getController().setCenter(DEFAULT_LOCATION);
                addMarker(DEFAULT_LOCATION, "Universiti Malaya");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (locationOverlay != null) {
            locationOverlay.enableMyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
        }
    }
}


