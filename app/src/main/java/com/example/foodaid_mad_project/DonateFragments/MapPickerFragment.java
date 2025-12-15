package com.example.foodaid_mad_project.DonateFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MapPickerFragment extends Fragment {

    private MapView mapView;
    private Button btnConfirmLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_picker, container, false);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapView = view.findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(new GeoPoint(3.1209, 101.6538)); // Default UM

        btnConfirmLocation = view.findViewById(R.id.btnConfirmLocation);
        btnConfirmLocation.setOnClickListener(v -> {
            GeoPoint center = (GeoPoint) mapView.getMapCenter();
            // Pass back result
            Bundle result = new Bundle();
            result.putDouble("lat", center.getLatitude());
            result.putDouble("lng", center.getLongitude());
            getParentFragmentManager().setFragmentResult("locationRequest", result);
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
