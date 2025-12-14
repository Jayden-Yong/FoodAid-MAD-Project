package com.example.foodaid_mad_project.HomeFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.example.foodaid_mad_project.AuthFragments.User;
import com.example.foodaid_mad_project.Model.FoodItem;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.UserManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// import com.google.firebase.firestore.FirebaseFirestore;
// import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment
//        implements OnMapReadyCallback
{

    private String email, username, welcomeDisplay;
    private TextView tvWelcomeUser;

    //My Map
//    private GoogleMap mMap;
    // private FirebaseFirestore db; // Commented out for now
//    private FragmentContainerView mapPinContainer;
    private ImageButton btnToQR;

    // List to hold Mock Data
    private List<FoodItem> mockFoodItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvWelcomeUser = view.findViewById(R.id.tvWelcomeUser);

        try {
            User user = UserManager.getInstance().getUser();
            if (user != null) {
                email = user.getEmail();

                if (user.getFullName() != null) {
                    username = user.getFullName();
                } else if(user.getDisplayName() != null){
                    username = user.getDisplayName();
                } else {
                    username = email.substring(0, email.indexOf("@")).toUpperCase();
                }

                welcomeDisplay = username;
            }

        } catch (NullPointerException e) {
            welcomeDisplay = "Guest";
        }
        tvWelcomeUser.setText(getString(R.string.Welcome_User, "morning", welcomeDisplay));


        // --- Load MapFragment into FragmentContainerView ---
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.MapFragment, new MapFragment())
                .commit();

//        Vibe Coded map using Google Map
//        // db = FirebaseFirestore.getInstance(); // Commented out
//
//        // --- MOCK DATA GENERATION (Temporary) ---
//        generateMockData();
//        // ----------------------------------------
//
//        mapPinContainer = view.findViewById(R.id.MapPinFragment);
//        mapPinContainer.setVisibility(View.GONE);


//        // Initialize Map
//        Fragment mapFragment = getChildFragmentManager().findFragmentById(R.id.MapFragment);
//        if (mapFragment == null) {
//            mapFragment = new SupportMapFragment();
//            getChildFragmentManager().beginTransaction()
//                    .replace(R.id.MapFragment, mapFragment)
//                    .commit();
//        }
//
//        if (mapFragment instanceof SupportMapFragment) {
//            ((SupportMapFragment) mapFragment).getMapAsync(this);
//        }

        // To QR Page
        btnToQR = view.findViewById(R.id.btnToQR);
        btnToQR.setOnClickListener(v -> {
            // Use requireActivity().getSupportFragmentManager() because coveringFragment
            // belongs to MainActivity, not the NavHostFragment.
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.coveringFragment, new QRFragment())
                    .addToBackStack("QRFragment")
                    .commit();
        });
    }

    public void showMapPinDetails(FoodItem item) {
        // Get the container
        FragmentContainerView mapPinContainer = getView().findViewById(R.id.MapPinFragment);
        if (mapPinContainer == null) return;

        // Make container visible
        mapPinContainer.setVisibility(View.VISIBLE);

        // Create the fragment with the selected FoodItem
        MapPinItemFragment pinFragment = new MapPinItemFragment(item);

        // Replace the container with this fragment
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.MapPinFragment, pinFragment)
                .commit();
    }

//    private void generateMockData() {
//        mockFoodItems = new ArrayList<>();
//        // Using local drawables for testing
//        mockFoodItems.add(new FoodItem("1", "Tiger Biscuits", "Universiti Malaya", "50 packs", "Student Council", R.drawable.ic_launcher_background, 3.1209, 101.6538));
//        mockFoodItems.add(new FoodItem("2", "Leftover Catering", "Mid Valley", "20 kg", "Grand Hotel", R.drawable.ic_launcher_background, 3.1176, 101.6776));
//        mockFoodItems.add(new FoodItem("3", "Canned Soup", "Jaya One", "100 cans", "Community NGO", R.drawable.ic_launcher_background, 3.1180, 101.6360));
//    }
//
//    @Override
//    public void onMapReady(@NonNull GoogleMap googleMap) {
//        mMap = googleMap;
//
//        // Default Camera Position
//        LatLng startLocation = new LatLng(3.1209, 101.6538);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 12));
//
//        // --- REAL DATABASE CODE (Commented Out) ---
//        /*
//        db.collection("donations").get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    FoodItem item = document.toObject(FoodItem.class);
//                    if (item != null) {
//                        LatLng loc = new LatLng(item.getLat(), item.getLng());
//                        Marker marker = mMap.addMarker(new MarkerOptions().position(loc).title(item.getName()));
//                        if (marker != null) marker.setTag(item);
//                    }
//                }
//            } else {
//                Toast.makeText(getContext(), "Failed to load map pins", Toast.LENGTH_SHORT).show();
//            }
//        });
//        */
//
//        // --- MOCK DATA CODE (Temporary) ---
//        for (FoodItem item : mockFoodItems) {
//            LatLng position = new LatLng(item.getLat(), item.getLng());
//            Marker marker = mMap.addMarker(new MarkerOptions()
//                    .position(position)
//                    .title(item.getName()));
//
//            if (marker != null) {
//                marker.setTag(item);
//            }
//        }
//        // ----------------------------------
//
//        mMap.setOnMarkerClickListener(marker -> {
//            FoodItem selectedItem = (FoodItem) marker.getTag();
//            if (selectedItem != null) {
//                showMapPinDetails(selectedItem);
//                return true;
//            }
//            return false;
//        });
//
//        mMap.setOnMapClickListener(latLng -> {
//            mapPinContainer.setVisibility(View.GONE);
//        });
//    }
//
//    private void showMapPinDetails(FoodItem item) {
//        mapPinContainer.setVisibility(View.VISIBLE);
//        MapPinItemFragment pinFragment = new MapPinItemFragment(item);
//
//        getChildFragmentManager().beginTransaction()
//                .replace(R.id.MapPinFragment, pinFragment)
//                .commit();
//    }
}