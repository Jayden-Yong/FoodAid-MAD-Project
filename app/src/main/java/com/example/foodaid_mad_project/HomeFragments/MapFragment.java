package com.example.foodaid_mad_project.HomeFragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodaid_mad_project.Model.FoodBank;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.SharedViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SharedViewModel sharedViewModel;
    private static final int PERMISSION_REQUEST_CODE = 1001;

    // Center of University of Malaya
    private static final LatLng UM_LOCATION = new LatLng(3.1219, 101.6570);
    private static final float ZOOM_LEVEL = 15.0f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 1. Move Camera to UM
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UM_LOCATION, ZOOM_LEVEL));

        // 2. Enable My Location (Blue Dot)
        enableMyLocation();

        // 3. Observe Data changes
        sharedViewModel.getFoodBanks().observe(getViewLifecycleOwner(), this::updateMarkers);

        // 4. Handle Marker Clicks
        mMap.setOnMarkerClickListener(marker -> {
            FoodBank foodBank = (FoodBank) marker.getTag();
            if (foodBank != null) {
                // For now, show a simple Toast/Info Window
                // Future: Open BottomSheet or Details
                marker.showInfoWindow();
                sharedViewModel.selectFoodBank(foodBank);
            }
            return false; // Return false to allow default behavior (center map + open info window)
        });
    }

    private void updateMarkers(List<FoodBank> foodBanks) {
        if (mMap == null)
            return;

        mMap.clear(); // Clear old markers

        for (FoodBank fb : foodBanks) {
            LatLng position = new LatLng(fb.getLatitude(), fb.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(fb.getName())
                    .snippet(fb.getType())); // Show Type in the bubble

            if (marker != null) {
                marker.setTag(fb); // Store object for click retrieval
            }
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            // Request permission
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(getContext(), "Location permission needed to show your position", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
