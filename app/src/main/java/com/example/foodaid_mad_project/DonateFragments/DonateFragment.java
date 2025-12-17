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
import androidx.appcompat.content.res.AppCompatResources;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.views.overlay.MapEventsOverlay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.TimePickerDialog;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.foodaid_mad_project.Model.FoodItem;

// Implement OnMapReadyCallback
public class DonateFragment extends Fragment {

    private String title;
    private long startTime;
    private long endTime;
    private String categoryStr;
    private String pickupMethodStr;
    private String location; // Stores the selected address string
    private String donator;
    private String donatorId;

    private FirebaseFirestore db;
    private StorageReference storageReference;

    private ImageView ivSelectedPhoto;
    private TextView tvUploadPlaceholder;
    private Uri selectedImageUri;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    // Map Variables
    private MapView mapView;
    private GeoPoint selectedGeoPoint; // Stores the selected coordinates
    private Marker selectedMarker;
    private EditText etLocationSearch;
    private EditText etTimeFrom, etTimeTo;
    private Calendar calendar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        calendar = Calendar.getInstance();

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Initialize Views ---
        // Wait, setupMap() is not a standard method I see in the previous file content,
        // I will inline the map setup here to be safe, or call setupMap if I define it.
        // Looking at previous file (Step 861), checking if setupMap() exists.. NOT in
        // the visible lines.
        // It was called in the duplicate block (Line 246).
        // I will Inline the logic to be safe.

        // Global Variables
        etLocationSearch = view.findViewById(R.id.etLocationSearch);
        etTimeFrom = view.findViewById(R.id.etTimeFrom);
        etTimeTo = view.findViewById(R.id.etTimeTo);
        ivSelectedPhoto = view.findViewById(R.id.ivSelectedPhoto);
        tvUploadPlaceholder = view.findViewById(R.id.tvUploadPlaceholder);

        EditText etItemName = view.findViewById(R.id.etItemName);
        EditText etWeight = view.findViewById(R.id.etWeight);
        EditText etQuantity = view.findViewById(R.id.etQuantity);
        EditText etDescription = view.findViewById(R.id.etDescription);
        CardView cvUploadPhoto = view.findViewById(R.id.cvUploadPhoto);
        Spinner spinnerPickupMethod = view.findViewById(R.id.spinnerPickupMethod);
        RadioGroup radioGroupCategory = view.findViewById(R.id.toggleGroupDonationType);

        ImageButton btnSearchLocation = view.findViewById(R.id.btnSearchLocation);
        ImageButton btnMyLocation = view.findViewById(R.id.btnMyLocation);
        Button btnDonate = view.findViewById(R.id.btnDonate);
        CheckBox cbConfirm = view.findViewById(R.id.cbConfirm);
        TextView tvTimeLabelFrom = view.findViewById(R.id.tvTimeLabelFrom);
        TextView tvTimeLabelTo = view.findViewById(R.id.tvTimeLabelTo);
        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        if (toolBarTitle != null)
            toolBarTitle.setText("Donate Food");

        // --- Map Initialization ---
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView = view.findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.setMultiTouchControls(true);
            IMapController controller = mapView.getController();
            controller.setZoom(15.0);
            GeoPoint startPoint = new GeoPoint(3.1390, 101.6869); // Default KL
            controller.setCenter(startPoint);

            // Map Click Listener
            MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    handleMapClick(p);
                    return true;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    return false;
                }
            };
            MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
            mapView.getOverlays().add(0, mapEventsOverlay);
        }

        // --- Time Pickers ---
        etTimeFrom.setOnClickListener(v -> showTimePickerDialog(etTimeFrom, true));
        etTimeTo.setOnClickListener(v -> showTimePickerDialog(etTimeTo, false));

        // --- Spinner Setup ---
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.Pickup_Method_List, R.layout.spinner_item_selected);
        adapter.setDropDownViewResource(R.layout.spinner_pickup_method);
        spinnerPickupMethod.setAdapter(adapter);

        spinnerPickupMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equalsIgnoreCase("Free Table") || selected.contains("Free Table")) {
                    tvTimeLabelFrom.setText("Drop Off:");
                    etTimeFrom.setHint("Time");
                    tvTimeLabelTo.setText("Expiry:");
                    etTimeTo.setHint("Time");
                } else {
                    tvTimeLabelFrom.setText("Start:");
                    etTimeFrom.setHint("12:00 PM");
                    tvTimeLabelTo.setText("End:");
                    etTimeTo.setHint("02:00 PM");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // --- Location Logic ---
        com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient = com.google.android.gms.location.LocationServices
                .getFusedLocationProviderClient(requireActivity());

        // Auto-fetch location
        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(locationObj -> {
                if (locationObj != null && mapView != null) {
                    GeoPoint point = new GeoPoint(locationObj.getLatitude(), locationObj.getLongitude());
                    mapView.getController().setCenter(point);
                    mapView.getController().setZoom(18.0);
                    addMarker(point);
                    selectedGeoPoint = point;
                    getAddressFromGeoPoint(point);
                }
            });
        }

        if (btnMyLocation != null) {
            btnMyLocation.setOnClickListener(v -> {
                if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(locationObj -> {
                        if (locationObj != null && mapView != null) {
                            GeoPoint point = new GeoPoint(locationObj.getLatitude(), locationObj.getLongitude());
                            mapView.getController().animateTo(point);
                            mapView.getController().setZoom(18.0);
                            addMarker(point); // Defined below
                            selectedGeoPoint = point;
                            getAddressFromGeoPoint(point); // Defined below
                        } else {
                            Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Permission required", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnSearchLocation != null) {
            btnSearchLocation.setOnClickListener(v -> {
                String searchString = etLocationSearch.getText().toString();
                if (!searchString.isEmpty()) {
                    searchLocation(searchString);
                } else {
                    Toast.makeText(getContext(), "Please enter location", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // --- Photo Upload ---
        if (cvUploadPhoto != null) {
            cvUploadPhoto.setOnClickListener(v -> {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            });
        }

        // --- Donate Button ---
        if (btnDonate != null) {
            btnDonate.setOnClickListener(v -> {
                String donationTitle = etItemName.getText().toString().trim();
                String weightStr = etWeight.getText().toString().trim();
                String quantityStr = etQuantity.getText().toString().trim();
                String desc = etDescription.getText().toString().trim();
                String locationInput = etLocationSearch.getText().toString().trim();

                if (radioGroupCategory.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getContext(), "Select Donation Type", Toast.LENGTH_SHORT).show();
                    return;
                }

                int selectedId = radioGroupCategory.getCheckedRadioButtonId();
                if (selectedId == R.id.radioGroceries)
                    categoryStr = "GROCERIES";
                else
                    categoryStr = "MEALS";

                if (donationTitle.isEmpty() || weightStr.isEmpty() || quantityStr.isEmpty() || locationInput.isEmpty()
                        || selectedImageUri == null) {
                    Toast.makeText(getContext(), "Fill all fields & upload photo", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (startTime == 0 || endTime == 0) {
                    Toast.makeText(getContext(), "Select times", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (endTime <= startTime) {
                    Toast.makeText(getContext(), "End time must be after Start time", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!cbConfirm.isChecked()) {
                    Toast.makeText(getContext(), "Confirm donation details", Toast.LENGTH_SHORT).show();
                    return;
                }

                double weightVal = 0.0;
                try {
                    weightVal = Double.parseDouble(weightStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid weight", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    donator = user.getDisplayName();
                    donatorId = user.getUid();
                    if (donator == null || donator.isEmpty()) {
                        donator = "User";
                    }
                } else {
                    donator = "Anonymous";
                    donatorId = "anon";
                }

                int quantityVal = 1;
                try {
                    quantityVal = Integer.parseInt(quantityStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                    return;
                }

                pickupMethodStr = spinnerPickupMethod.getSelectedItem().toString();
                processImageAndSave(selectedImageUri, donationTitle, weightVal, quantityVal, desc, categoryStr,
                        pickupMethodStr);
            });
        }

        // Navigation Back Button
        Toolbar toolbar = view.findViewById(R.id.Toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            });
        }
    }

    // Helper to add marker (since I referenced it)
    private void addMarker(GeoPoint point) {
        if (mapView == null)
            return;
        if (selectedMarker != null) {
            mapView.getOverlays().remove(selectedMarker);
        }
        selectedMarker = new Marker(mapView);
        selectedMarker.setPosition(point);
        selectedMarker.setTitle("Selected Location");
        selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        selectedMarker.setIcon(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_custom_pin));
        mapView.getOverlays().add(selectedMarker);
        mapView.invalidate();
    }

    private void handleMapClick(GeoPoint point) {
        selectedGeoPoint = point;

        // Remove old marker
        if (selectedMarker != null) {
            mapView.getOverlays().remove(selectedMarker);
        }

        selectedMarker = new Marker(mapView);
        selectedMarker.setPosition(point);
        selectedMarker.setTitle("Selected Location");
        selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        selectedMarker.setIcon(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_custom_pin));
        mapView.getOverlays().add(selectedMarker);
        mapView.invalidate();

        getAddressFromGeoPoint(point);
    }

    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());

                IMapController controller = mapView.getController();
                controller.setCenter(point);
                controller.setZoom(18.0);

                handleMapClick(point);
                location = address.getAddressLine(0);
                etLocationSearch.setText(location);
            } else {
                Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error searching location", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAddressFromGeoPoint(GeoPoint point) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                location = addresses.get(0).getAddressLine(0);
                etLocationSearch.setText(location);
            } else {
                location = "Lat: " + point.getLatitude() + ", Lng: " + point.getLongitude();
                etLocationSearch.setText(location);
            }
        } catch (IOException e) {
            e.printStackTrace();
            location = "Unknown Location";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null)
            mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null)
            mapView.onPause();
    }

    private void showTimePickerDialog(EditText editText, boolean isStart) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minuteOfHour) -> {
            Calendar selectedTime = Calendar.getInstance();
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedTime.set(Calendar.MINUTE, minuteOfHour);

            // If time is in the past, assume it's for tomorrow
            if (selectedTime.before(Calendar.getInstance())) {
                selectedTime.add(Calendar.DAY_OF_YEAR, 1);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            editText.setText(sdf.format(selectedTime.getTime()));

            if (isStart) {
                startTime = selectedTime.getTimeInMillis();
            } else {
                endTime = selectedTime.getTimeInMillis();
            }
        }, hour, minute, false);
        timePickerDialog.show();
    }

    private void processImageAndSave(Uri imageUri, String title, double weight, int quantity, String description,
            String category,
            String pickupMethod) {
        Toast.makeText(getContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
        uploadImageToStorage(imageUri, title, weight, quantity, description, category, pickupMethod);
    }

    private void uploadImageToStorage(Uri imageUri, String title, double weight, int quantity, String description,
            String category, String pickupMethod) {
        if (imageUri == null) {
            // Should not happen as we check before calling, but handle gracefully
            saveDonationToFirestore(null, title, weight, quantity, description, category, pickupMethod);
            return;
        }

        String fileName = "donations/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storageReference.child(fileName);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    saveDonationToFirestore(downloadUrl, title, weight, quantity, description, category, pickupMethod);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Optional: Try saving without image or stop? For now we stop.
                });
    }

    private void saveDonationToFirestore(String imageUrl, String title, double weight, int quantity,
            String description,
            String category, String pickupMethod) {

        Map<String, Object> donation = new HashMap<>();
        donation.put("title", title);
        donation.put("weight", weight);
        donation.put("quantity", quantity);
        donation.put("description", description);
        donation.put("donatorId", donatorId);
        donation.put("donatorName", donator);
        donation.put("locationName", location != null ? location : "Unknown Location");
        donation.put("latitude", selectedGeoPoint != null ? selectedGeoPoint.getLatitude() : 0.0);
        donation.put("longitude", selectedGeoPoint != null ? selectedGeoPoint.getLongitude() : 0.0);
        donation.put("startTime", startTime);
        donation.put("endTime", endTime);
        donation.put("status", "AVAILABLE");
        donation.put("claimedBy", null);
        donation.put("category", category);
        donation.put("pickupMethod", pickupMethod);
        donation.put("timestamp", System.currentTimeMillis());
        // Store Storage URL instead of Base64
        donation.put("imageUri", imageUrl);

        db.collection("donations").add(donation)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Donation posted successfully!", Toast.LENGTH_LONG).show();

                    String[] timeArr = new String[] { etTimeFrom.getText().toString(), etTimeTo.getText().toString() };
                    int catId = (category.equals("GROCERIES")) ? R.id.radioGroceries : R.id.radioMeals;

                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.coveringFragment,
                                    new DonateNotifyFragment(title, timeArr, catId, weight, location, donator,
                                            imageUrl, description))
                            .addToBackStack("DonateSuccess")
                            .commit();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save donation: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
    }
}