package com.example.foodaid_mad_project.DonateFragments;

import android.app.TimePickerDialog;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.Utils.ImageUtil;
import com.example.foodaid_mad_project.Utils.LoadingDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * DonateFragment
 *
 * Allows users to donate food items by filling out a form.
 * Features:
 * - Form inputs: Title, Weight, Quantity, Description, Category.
 * - Location Selection: Map View (OpenStreetMap), Auto-detect GPS, or Manual Search.
 * - Image Upload: Converts selected image to Base64 String for Firestore storage.
 * - Notifications: Triggers Firestore notifications for the donor (confirmation) and others (global alert).
 */
public class DonateFragment extends Fragment {

    // Inputs
    private String categoryStr;
    private String pickupMethodStr;
    private String location; // Address string
    private String donator;
    private String donatorId;
    private long startTime;
    private long endTime;

    // Firebase
    private FirebaseFirestore db;

    // UI Elements
    private ImageView ivSelectedPhoto, transparentImage;
    private TextView tvUploadPlaceholder;
    private Uri selectedImageUri;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private NestedScrollView scrollView;

    // Map Elements
    private MapView mapView;
    private GeoPoint selectedGeoPoint;
    private Marker selectedMarker;
    private EditText etLocationSearch;
    private EditText etTimeFrom, etTimeTo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // Register Image Picker
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

        TextView toolbarTitle = view.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Donate");

        // 1. Initialize UI Groups
        initializeInputs(view);
        initializeMap(view);
        initializeLocationServices(view);

        // 2. Initialize User Info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            donatorId = user.getUid();
            donator = user.getDisplayName();
            if (donator == null || donator.isEmpty()) {
                donator = "Anonymous Donor";
            }
        }

        // 3. Setup Actions
        setupTimePickers();
        setupSubmitButton(view);
        setupNavigation(view);
        fixMapScrolling();
    }

    private void initializeInputs(View view) {
        scrollView = view.findViewById(R.id.scrollView);
        etLocationSearch = view.findViewById(R.id.etLocationSearch);
        etTimeFrom = view.findViewById(R.id.etTimeFrom);
        etTimeTo = view.findViewById(R.id.etTimeTo);
        ivSelectedPhoto = view.findViewById(R.id.ivSelectedPhoto);
        tvUploadPlaceholder = view.findViewById(R.id.tvUploadPlaceholder);
        transparentImage = view.findViewById(R.id.transparentImage);

        CardView cvUploadPhoto = view.findViewById(R.id.cvUploadPhoto);
        if (cvUploadPhoto != null) {
            cvUploadPhoto.setOnClickListener(v -> pickMedia.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()));
        }

        Spinner spinnerPickupMethod = view.findViewById(R.id.spinnerPickupMethod);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.Pickup_Method_List, R.layout.spinner_item_selected);
        adapter.setDropDownViewResource(R.layout.spinner_pickup_method);
        spinnerPickupMethod.setAdapter(adapter);

        // Dynamic hint updates based on pickup method
        TextView tvTimeLabelFrom = view.findViewById(R.id.tvTimeLabelFrom);
        TextView tvTimeLabelTo = view.findViewById(R.id.tvTimeLabelTo);
        spinnerPickupMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tvTimeLabelFrom.setText("Start:");
                etTimeFrom.setHint("12:00 PM");
                tvTimeLabelTo.setText("End:");
                etTimeTo.setHint("02:00 PM");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initializeMap(View view) {
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView = view.findViewById(R.id.mapView);

        if (mapView != null) {
            mapView.setMultiTouchControls(true);
            IMapController controller = mapView.getController();
            controller.setZoom(16.0);
            GeoPoint startPoint = new GeoPoint(3.1390, 101.6869); // Default KL
            controller.setCenter(startPoint);

            // Tap Listener
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
    }

    private void initializeLocationServices(View view) {
        ImageButton btnSearchLocation = view.findViewById(R.id.btnSearchLocation);
        ImageButton btnMyLocation = view.findViewById(R.id.btnMyLocation);
        FusedLocationProviderClient fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(requireActivity());

        // 1. Check permission & auto-locate on start
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(locationObj -> {
                        if (locationObj != null && mapView != null) {
                            GeoPoint point = new GeoPoint(locationObj.getLatitude(), locationObj.getLongitude());
                            updateMapLocation(point);
                        }
                    });
        }

        // 2. Button Listeners
        if (btnMyLocation != null) {
            btnMyLocation.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(requireContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Getting location...", Toast.LENGTH_SHORT).show();
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener(locationObj -> {
                                if (locationObj != null && mapView != null) {
                                    GeoPoint point = new GeoPoint(locationObj.getLatitude(),
                                            locationObj.getLongitude());
                                    updateMapLocation(point);
                                } else {
                                    Toast.makeText(getContext(), "Location not found, try outside.", Toast.LENGTH_LONG)
                                            .show();
                                }
                            })
                            .addOnFailureListener(e -> Toast
                                    .makeText(getContext(), "Loc Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    requestPermissions(new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION }, 100);
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
    }

    private void updateMapLocation(GeoPoint point) {
        if (mapView == null)
            return;
        mapView.getController().animateTo(point);
        mapView.getController().setZoom(16.5);
        addMarker(point);
        selectedGeoPoint = point;
        getAddressFromGeoPoint(point);
    }

    private void setupTimePickers() {
        etTimeFrom.setOnClickListener(v -> showTimePickerDialog(etTimeFrom, true));
        etTimeTo.setOnClickListener(v -> showTimePickerDialog(etTimeTo, false));
    }

    private void showTimePickerDialog(EditText editText, boolean isStart) {
        Calendar initCal = Calendar.getInstance();

        // Try to parse existing text
        String currentText = editText.getText().toString();
        if (!currentText.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                java.util.Date date = sdf.parse(currentText);
                if (date != null)
                    initCal.setTime(date);
            } catch (Exception ignored) {
            }
        }

        new TimePickerDialog(getContext(), (view, hourOfDay, minuteOfHour) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedCal.set(Calendar.MINUTE, minuteOfHour);
            selectedCal.set(Calendar.SECOND, 0);

            // Validation Logic
            if (isStart) {
                if (selectedCal.getTimeInMillis() < System.currentTimeMillis()) {
                    selectedCal.add(Calendar.DAY_OF_YEAR, 1); // If past time, likely means tomorrow
                }
                startTime = selectedCal.getTimeInMillis();
            } else {
                if (startTime != 0) {
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTimeInMillis(startTime);
                    // Match date
                    selectedCal.set(Calendar.YEAR, startCal.get(Calendar.YEAR));
                    selectedCal.set(Calendar.DAY_OF_YEAR, startCal.get(Calendar.DAY_OF_YEAR));

                    if (selectedCal.getTimeInMillis() <= startTime) {
                        selectedCal.add(Calendar.DAY_OF_YEAR, 1); // Overnight pick-up
                    }
                } else {
                    if (selectedCal.getTimeInMillis() < System.currentTimeMillis()) {
                        selectedCal.add(Calendar.DAY_OF_YEAR, 1);
                    }
                }
                endTime = selectedCal.getTimeInMillis();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            editText.setText(sdf.format(selectedCal.getTime()));

        }, initCal.get(Calendar.HOUR_OF_DAY), initCal.get(Calendar.MINUTE), false).show();
    }

    private void setupSubmitButton(View view) {
        Button btnDonate = view.findViewById(R.id.btnDonate);
        if (btnDonate == null)
            return;

        EditText etItemName = view.findViewById(R.id.etItemName);
        EditText etWeight = view.findViewById(R.id.etWeight);
        EditText etQuantity = view.findViewById(R.id.etQuantity);
        EditText etDescription = view.findViewById(R.id.etDescription);
        RadioGroup radioGroupCategory = view.findViewById(R.id.toggleGroupDonationType);
        CheckBox cbConfirm = view.findViewById(R.id.cbConfirm);
        Spinner spinnerPickupMethod = view.findViewById(R.id.spinnerPickupMethod);

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
            categoryStr = (selectedId == R.id.radioGroceries) ? "GROCERIES" : "MEALS";

            if (donationTitle.isEmpty() || weightStr.isEmpty() || quantityStr.isEmpty() || locationInput.isEmpty()
                    || selectedImageUri == null) {
                Toast.makeText(getContext(), "Fill all fields & upload photo", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startTime == 0 || endTime == 0 || endTime <= startTime) {
                Toast.makeText(getContext(), "Invalid Start/End times", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbConfirm.isChecked()) {
                Toast.makeText(getContext(), "Confirm donation details", Toast.LENGTH_SHORT).show();
                return;
            }

            double weightVal;
            try {
                weightVal = Double.parseDouble(weightStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid weight", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantityVal;
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

    private void processImageAndSave(Uri imageUri, String title, double weight, int quantity, String description,
            String category, String pickupMethod) {

        LoadingDialog loadingDialog = new LoadingDialog(requireContext());
        loadingDialog.show();

        new Thread(() -> {
            try {
                String base64Image = ImageUtil.uriToBase64(requireContext(), imageUri);

                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    if (base64Image == null) {
                        Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveDonationToFirestore(base64Image, title, weight, quantity, description, category, pickupMethod);
                });

            } catch (Exception e) {
                Log.e("Donate", "Image Error", e);
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(getContext(), "Image Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveDonationToFirestore(String base64Image, String title, double weight, int quantity,
            String description, String category, String pickupMethod) {

        Map<String, Object> donation = new HashMap<>();
        donation.put("title", title);
        donation.put("weight", weight);
        donation.put("quantity", quantity);
        donation.put("initialQuantity", quantity);
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
        donation.put("imageUri", base64Image); // Saving as Base64 String directly

        db.collection("donations").add(donation)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Donation posted successfully!", Toast.LENGTH_LONG).show();
                    triggerNotifications(title, location); // Notify user and others
                    navigateToSuccess(title, category, weight, base64Image, description);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save donation: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
    }

    private void triggerNotifications(String title, String locationName) {
        db.collection("users").document(donatorId).get().addOnSuccessListener(userSnap -> {
            boolean pushEnabled = true;
            if (userSnap.exists() && userSnap.contains("pushNotificationEnabled")) {
                pushEnabled = Boolean.TRUE.equals(userSnap.getBoolean("pushNotificationEnabled"));
            }

            if (pushEnabled) {
                // 1. Personal Notification
                Map<String, Object> personalNotif = new HashMap<>();
                personalNotif.put("title", "Donation Successful");
                personalNotif.put("message",
                        "Thank you for donating " + title + "! Your contribution helps the community.");
                personalNotif.put("timestamp", System.currentTimeMillis());
                personalNotif.put("isRead", false);
                personalNotif.put("type", "Donation");
                personalNotif.put("userId", donatorId);
                db.collection("notifications").add(personalNotif);

                // 2. Global Notification
                Map<String, Object> globalNotif = new HashMap<>();
                globalNotif.put("title", "New Donation Available");
                globalNotif.put("message",
                        donator + " is donating " + title + " at " + (locationName != null ? locationName : "Unknown"));
                globalNotif.put("timestamp", System.currentTimeMillis());
                globalNotif.put("isRead", false);
                globalNotif.put("type", "Donation");
                globalNotif.put("userId", "ALL");
                db.collection("notifications").add(globalNotif);
            }
        });
    }

    private void navigateToSuccess(String title, String category, double weight, String base64Image,
            String description) {
        String[] timeArr = new String[] { etTimeFrom.getText().toString(), etTimeTo.getText().toString() };
        int catId = (category.equals("GROCERIES")) ? R.id.radioGroceries : R.id.radioMeals;

        String dateString = "-";
        if (startTime > 0) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
            dateString = sdfDate.format(new java.util.Date(startTime));
        }

        if (isAdded()) {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.coveringFragment,
                            new DonateNotifyFragment(title, timeArr, catId, weight, location, donator,
                                    base64Image, description, dateString))
                    .addToBackStack("DonateSuccess")
                    .commit();
        }
    }

    private void setupNavigation(View view) {
        Toolbar toolbar = view.findViewById(R.id.Toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            });
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    // Map Helper Methods -> Logic identical to previous impl
    private void addMarker(GeoPoint point) {
        if (mapView == null)
            return;
        if (selectedMarker != null)
            mapView.getOverlays().remove(selectedMarker);

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
        addMarker(point);
        getAddressFromGeoPoint(point);
    }

    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());
                updateMapLocation(point);
            } else {
                Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
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

    private void fixMapScrolling() {
        if (transparentImage == null || scrollView == null) return;

        transparentImage.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Disallow ScrollView to intercept touch events.
                    scrollView.requestDisallowInterceptTouchEvent(true);
                    // Disable touch on transparent view
                    return false;
                case MotionEvent.ACTION_UP:
                    // Allow ScrollView to intercept touch events.
                    scrollView.requestDisallowInterceptTouchEvent(false);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    scrollView.requestDisallowInterceptTouchEvent(true);
                    return false;
                default:
                    return true;
            }
        });
    }
}