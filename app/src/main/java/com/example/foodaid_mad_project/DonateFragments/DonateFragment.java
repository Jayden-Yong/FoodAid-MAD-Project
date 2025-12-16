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

        // --- View Binding ---
        RadioGroup toggleGroupDonationType = view.findViewById(R.id.toggleGroupDonationType);
        EditText etItemName = view.findViewById(R.id.etItemName);
        // quantity removed
        EditText etWeight = view.findViewById(R.id.etWeight);
        EditText etExpiryDate = view.findViewById(R.id.etExpiryDate);
        EditText etDescription = view.findViewById(R.id.etDescription);
        CardView cvUploadPhoto = view.findViewById(R.id.cvUploadPhoto);
        Spinner spinnerPickupMethod = view.findViewById(R.id.spinnerPickupMethod);
        etTimeFrom = view.findViewById(R.id.etTimeFrom);
        etTimeTo = view.findViewById(R.id.etTimeTo);
        CheckBox cbConfirm = view.findViewById(R.id.cbConfirm);

        // Time Picker Logic
        etTimeFrom.setOnClickListener(v -> showTimePickerDialog(etTimeFrom, true));
        etTimeTo.setOnClickListener(v -> showTimePickerDialog(etTimeTo, false));

        // Location Search Views
        etLocationSearch = view.findViewById(R.id.etLocationSearch);
        ImageButton btnSearchLocation = view.findViewById(R.id.btnSearchLocation);

        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText("Donate Food");

        ivSelectedPhoto = view.findViewById(R.id.ivSelectedPhoto);
        tvUploadPlaceholder = view.findViewById(R.id.tvUploadPlaceholder);

        // --- Map Initialization ---
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView = view.findViewById(R.id.mapView);
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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.Pickup_Method_List, R.layout.spinner_item_selected);
        adapter.setDropDownViewResource(R.layout.spinner_pickup_method);
        spinnerPickupMethod.setAdapter(adapter);

        TextView tvTimeLabelFrom = view.findViewById(R.id.tvTimeLabelFrom);
        TextView tvTimeLabelTo = view.findViewById(R.id.tvTimeLabelTo);

        spinnerPickupMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equalsIgnoreCase("Free Table")) {
                    tvTimeLabelFrom.setText("Drop Off:");
                    etTimeFrom.setHint("Time");
                    tvTimeLabelTo.setText("Expiry:");
                    etTimeTo.setHint("Time");
                } else {
                    // Default to Meet Up
                    tvTimeLabelFrom.setText("Start:");
                    etTimeFrom.setHint("Time");
                    tvTimeLabelTo.setText("End:");
                    etTimeTo.setHint("Time");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Navigation Logic
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
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
                String weightStr = etWeight.getText().toString();
                String desc = etDescription.getText().toString();

                // Validation
                if (toggleGroupDonationType.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getContext(), "Please select a donation type", Toast.LENGTH_SHORT).show();
                    return;
                }

                int selectedId = toggleGroupDonationType.getCheckedRadioButtonId();
                if (selectedId == R.id.radioGroceries)
                    categoryStr = "GROCERIES";
                else if (selectedId == R.id.radioMeals)
                    categoryStr = "MEALS";
                else
                    categoryStr = "OTHER";

                if (title.isEmpty() || weightStr.isEmpty() || desc.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedImageUri == null) {
                    Toast.makeText(getContext(), "Please upload a photo", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (startTime == 0 || endTime == 0) {
                    Toast.makeText(getContext(), "Please select start and end times", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (endTime <= startTime) {
                    Toast.makeText(getContext(), "End time must be after start time", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (endTime <= System.currentTimeMillis()) {
                    Toast.makeText(getContext(), "End time must be in the future", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!cbConfirm.isChecked()) {
                    Toast.makeText(getContext(), "You must confirm the donation details", Toast.LENGTH_SHORT).show();
                    return;
                }

                double weight = 0.0;
                try {
                    weight = Double.parseDouble(weightStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid weight format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get User Info
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    donator = user.getDisplayName();
                    donatorId = user.getUid();
                    if (donator == null || donator.isEmpty()) {
                        donator = user.getEmail().substring(0, user.getEmail().indexOf("@"));
                    }
                } else {
                    donator = "Anonymous";
                    donatorId = "anon";
                }

                pickupMethodStr = spinnerPickupMethod.getSelectedItem().toString();

                processImageAndSave(selectedImageUri, title, weight, desc, categoryStr, pickupMethodStr);
            });
        }
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
            // Ensure date is today (or handle date picker too, but for now assuming today
            // only per simplified requirement)
            // Ideally we should have a DatePicker too, but user req only mentioned
            // TimePicker inputs.
            // We'll assume the donation is for TODAY or TOMORROW if time is past?
            // Simple logic: Set to today.

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

    private void processImageAndSave(Uri imageUri, String title, double weight, String description, String category,
            String pickupMethod) {
        Toast.makeText(getContext(), "Processing image...", Toast.LENGTH_SHORT).show();

        try {
            // Database-Only approach: Convert to Base64
            // This is blocking UI thread slightly, ideally assume Async or use
            // Coroutines/Thread,
            // but for <500KB resize it's usually fast enough for a prototype.
            String base64Image = com.example.foodaid_mad_project.Utils.ImageUtil.uriToBase64(getContext(), imageUri);

            if (base64Image != null) {
                saveDonationToFirestore(base64Image, title, weight, description, category, pickupMethod);
            } else {
                Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDonationToFirestore(String base64Image, String title, double weight, String description,
            String category, String pickupMethod) {

        Map<String, Object> donation = new HashMap<>();
        donation.put("title", title);
        donation.put("weight", weight);
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
        // Store Base64 directly
        donation.put("imageUri", base64Image);

        db.collection("donations").add(donation)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Donation posted successfully!", Toast.LENGTH_LONG).show();

                    String[] timeArr = new String[] { etTimeFrom.getText().toString(), etTimeTo.getText().toString() };
                    int catId = (category.equals("GROCERIES")) ? R.id.radioGroceries : R.id.radioMeals;

                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.DonateFragmentContainer,
                                    new DonateNotifyFragment(title, timeArr, catId, weight, location, donator,
                                            base64Image))
                            .addToBackStack("DonateSuccess")
                            .commit();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save donation: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
    }
}