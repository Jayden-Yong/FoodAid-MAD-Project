package com.example.foodaid_mad_project.DonateFragments;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

// Implement OnMapReadyCallback
public class DonateFragment extends Fragment implements OnMapReadyCallback {

    private String title;
    private String[] pickupTime;
    private int category;
    private String quantityStr;
    private String location; // Stores the selected address string
    private String donator;

    private ImageView ivSelectedPhoto;
    private TextView tvUploadPlaceholder;
    private Uri selectedImageUri;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    // Map Variables
    private GoogleMap mMap;
    private LatLng selectedLatLng; // Stores the selected coordinates
    private EditText etLocationSearch;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                ivSelectedPhoto.setImageURI(uri);
                ivSelectedPhoto.setVisibility(View.VISIBLE);
                tvUploadPlaceholder.setVisibility(View.GONE);
            } else {
                Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- View Binding ---
        RadioGroup toggleGroupDonationType = view.findViewById(R.id.toggleGroupDonationType);
        EditText etItemName = view.findViewById(R.id.etItemName);
        EditText etQuantity = view.findViewById(R.id.etQuantity);
        EditText etWeight = view.findViewById(R.id.etWeight);
        EditText etExpiryDate = view.findViewById(R.id.etExpiryDate);
        EditText etDescription = view.findViewById(R.id.etDescription);
        CardView cvUploadPhoto = view.findViewById(R.id.cvUploadPhoto);
        Spinner spinnerPickupMethod = view.findViewById(R.id.spinnerPickupMethod);
        EditText etTimeFrom = view.findViewById(R.id.etTimeFrom);
        EditText etTimeTo = view.findViewById(R.id.etTimeTo);
        CheckBox cbConfirm = view.findViewById(R.id.cbConfirm);

        // Location Search Views
        etLocationSearch = view.findViewById(R.id.etLocationSearch);
        ImageButton btnSearchLocation = view.findViewById(R.id.btnSearchLocation);

        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText("Donate Food");

        ivSelectedPhoto = view.findViewById(R.id.ivSelectedPhoto);
        tvUploadPlaceholder = view.findViewById(R.id.tvUploadPlaceholder);

        // --- Map Initialization ---
        // Get the map fragment from the container
        Fragment mapFragment = getChildFragmentManager().findFragmentById(R.id.mapFragmentContainer);
        if (mapFragment instanceof SupportMapFragment) {
            ((SupportMapFragment) mapFragment).getMapAsync(this);
        }

        // --- Search Button Logic ---
        btnSearchLocation.setOnClickListener(v -> {
            String searchString = etLocationSearch.getText().toString();
            if (!searchString.isEmpty()) {
                searchLocation(searchString);
            } else {
                Toast.makeText(getContext(), "Please enter a location to search", Toast.LENGTH_SHORT).show();
            }
        });

        // Photo Upload Logic
        if (cvUploadPhoto != null) {
            cvUploadPhoto.setOnClickListener(v -> {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            });
        }

        // Spinner Setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.Pickup_Method_List, R.layout.spinner_item_selected);
        adapter.setDropDownViewResource(R.layout.spinner_pickup_method);
        spinnerPickupMethod.setAdapter(adapter);

        // Navigation Logic
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });

        Toolbar toolbar = view.findViewById(R.id.Toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            });
        }

        // --- Donate Button Logic ---
        Button btnDonate = view.findViewById(R.id.btnDonate);
        if (btnDonate != null) {
            btnDonate.setOnClickListener(v -> {
                // Input Gathering
                title = etItemName.getText().toString();
                quantityStr = etQuantity.getText().toString();
                String weight = etWeight.getText().toString();
                String expiry = etExpiryDate.getText().toString();
                String desc = etDescription.getText().toString();
                String timeFrom = etTimeFrom.getText().toString();
                String timeTo = etTimeTo.getText().toString();

                // Validation
                if(toggleGroupDonationType.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getContext(), "Please select a donation type", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (title.isEmpty() || quantityStr.isEmpty() || weight.isEmpty() || expiry.isEmpty() || desc.isEmpty()){
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedImageUri == null) {
                    Toast.makeText(getContext(), "Please upload a photo", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (location == null || location.isEmpty()) {
                    Toast.makeText(getContext(), "Please search or select a location on the map", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (etTimeFrom.getText().toString().isEmpty() || etTimeTo.getText().toString().isEmpty()){
                    Toast.makeText(getContext(), "Please fill in all time fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!cbConfirm.isChecked()) {
                    Toast.makeText(getContext(), "You must confirm the donation details", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save Logic (Mocked)
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                donator = (user != null) ? user.getEmail().substring(0, user.getEmail().indexOf("@")).toUpperCase() : "Anonymous";

                pickupTime = new String[]{timeFrom, timeTo};
                category = toggleGroupDonationType.getCheckedRadioButtonId();

                String imageUriString = selectedImageUri.toString();
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.DonateFragmentContainer, new DonateNotifyFragment(title, pickupTime, category, Integer.parseInt(quantityStr), location, donator, imageUriString))
                        .addToBackStack("DonateSuccess")
                        .commit();
            });
        }
    }

    // --- Map Callback ---
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Default: Kuala Lumpur
        LatLng defaultLocation = new LatLng(3.1390, 101.6869);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        // Map Click Listener: Allow user to pin point manually
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear(); // Remove old marker
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            selectedLatLng = latLng;

            // Reverse Geocode: Get address from LatLng
            getAddressFromLatLng(latLng);
        });
    }

    // Helper: Search Location by Text
    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title(locationName));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                // Save data
                selectedLatLng = latLng;
                location = address.getAddressLine(0); // Full address string
                // Update EditText to show full resolved address
                etLocationSearch.setText(location);
            } else {
                Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error searching location", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper: Get Address from Pin Point
    private void getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                location = addresses.get(0).getAddressLine(0);
                etLocationSearch.setText(location); // Update UI
            } else {
                location = "Selected Coordinates: " + latLng.latitude + ", " + latLng.longitude;
                etLocationSearch.setText(location);
            }
        } catch (IOException e) {
            e.printStackTrace();
            location = "Unknown Location";
        }
    }
}