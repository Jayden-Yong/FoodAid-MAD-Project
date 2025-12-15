package com.example.foodaid_mad_project.DonateFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.Model.FoodBank;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.UserManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Locale;
import java.io.IOException;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Geocoder;
import android.location.Address;
import android.widget.ImageButton;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;

public class DonateFragment extends Fragment {

    private EditText etItemName, etQuantity, etWeight, etDescription, etPrice;
    private TextView tvLocationAddress; // Changed from EditText to TextView
    private EditText etTimeFrom, etTimeTo;
    private RadioGroup toggleGroupDonationType;
    private LinearLayout containerPrice;
    private CheckBox cbConfirm;

    private FusedLocationProviderClient fusedLocationClient;
    private ImageButton btnSearchLocation, btnPinLocation; // Added btnPinLocation
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // Stored Location
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;

    private android.widget.ImageView ivFoodImage;
    private android.net.Uri selectedImageUri;
    private ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> pickMedia;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Image Picker Registration
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                ivFoodImage.setImageURI(uri);
                view.findViewById(R.id.tvAddPhotoHint).setVisibility(View.GONE);
                ivFoodImage.setPadding(0, 0, 0, 0);
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        getLocation();
                    } else {
                        Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        // Listen for MapPicker Result
        getParentFragmentManager().setFragmentResultListener("locationRequest", getViewLifecycleOwner(),
                (requestKey, result) -> {
                    double lat = result.getDouble("lat");
                    double lng = result.getDouble("lng");
                    selectedLat = lat;
                    selectedLng = lng;
                    getAddressFromCoords(lat, lng);
                });

        // Bind Views
        toggleGroupDonationType = view.findViewById(R.id.toggleGroupDonationType);
        etItemName = view.findViewById(R.id.etItemName);
        etQuantity = view.findViewById(R.id.etQuantity);
        etWeight = view.findViewById(R.id.etWeight);
        etDescription = view.findViewById(R.id.etDescription); // Notes
        etPrice = view.findViewById(R.id.etPrice);
        containerPrice = view.findViewById(R.id.containerPrice);
        ivFoodImage = view.findViewById(R.id.ivFoodImage);

        tvLocationAddress = view.findViewById(R.id.tvLocationAddress); // Replaces etLocationSearch
        btnSearchLocation = view.findViewById(R.id.btnSearchLocation); // GPS Button
        btnPinLocation = view.findViewById(R.id.btnPinLocation); // Pin Button

        ivFoodImage.setOnClickListener(v -> {
            pickMedia.launch(new androidx.activity.result.PickVisualMediaRequest.Builder()
                    .setMediaType(
                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        btnSearchLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        btnPinLocation.setOnClickListener(v -> {
            // Open MapPickerFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.DonateFragmentContainer, new MapPickerFragment()) // Overlay or Replace
                    .addToBackStack("MapPicker")
                    .commit();
        });

        etTimeFrom = view.findViewById(R.id.etTimeFrom);
        etTimeTo = view.findViewById(R.id.etTimeTo);
        cbConfirm = view.findViewById(R.id.cbConfirm);

        // Category Toggle Logic
        toggleGroupDonationType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioEventLeftover) {
                containerPrice.setVisibility(View.GONE);
                etPrice.setText("0.00");
            } else {
                containerPrice.setVisibility(View.VISIBLE);
            }
        });
        // Default selection
        if (toggleGroupDonationType.getCheckedRadioButtonId() == -1) {
            toggleGroupDonationType.check(R.id.radioMenuRahmah);
        }

        // Toolbar
        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText(getString(R.string.String, "Donate / List Food"));

        Toolbar toolbar = view.findViewById(R.id.Toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> handleBackPress());
        }
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        handleBackPress();
                    }
                });

        // Submit Button
        Button btnDonate = view.findViewById(R.id.btnDonate);
        btnDonate.setOnClickListener(v -> submitDonation());

        // Initial Location Attempt (Silent)
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }

        setupTimePickers();
    }

    private void setupTimePickers() {
        etTimeFrom.setOnClickListener(v -> showTimePicker(etTimeFrom));
        etTimeTo.setOnClickListener(v -> showTimePicker(etTimeTo));
    }

    private void showTimePicker(EditText targetEditText) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(getContext(),
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    targetEditText.setText(time);
                }, hour, minute, true); // true for 24-hour format
        timePickerDialog.show();
    }

    private void handleBackPress() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void submitDonation() {
        // 1. Inputs
        String itemName = etItemName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String location = tvLocationAddress.getText().toString().trim();
        String notes = etDescription.getText().toString().trim();
        String timeFrom = etTimeFrom.getText().toString().trim();
        String timeTo = etTimeTo.getText().toString().trim();

        // Determine Category
        int selectedId = toggleGroupDonationType.getCheckedRadioButtonId();
        String category = "Menu Rahmah";
        if (selectedId == R.id.radioCafeLeftover)
            category = "Leftover";
        else if (selectedId == R.id.radioEventLeftover)
            category = "Event";

        // Validation
        if (itemName.isEmpty()) {
            etItemName.setError("Required");
            return;
        }
        if (quantityStr.isEmpty()) {
            etQuantity.setError("Required");
            return;
        }
        if (!category.equals("Event") && priceStr.isEmpty()) {
            etPrice.setError("Required");
            return;
        }
        if (!cbConfirm.isChecked()) {
            Toast.makeText(getContext(), "Please confirm details", Toast.LENGTH_SHORT).show();
            return;
        }
        if (location.isEmpty() || location.equals("Detecting Location...")
                || location.equals("Tap GPS or Pin to set location")) {
            Toast.makeText(getContext(), "Please set a location", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityStr);
        double price = 0.0;
        try {
            if (!priceStr.isEmpty())
                price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            // 0.0
        }

        // 2. Prepare FoodBank Object (ID needed for image name)
        String newId = UUID.randomUUID().toString();
        FoodBank listing = new FoodBank();
        listing.setId(newId);
        listing.setName(itemName);
        listing.setCategory(category);
        listing.setPrice(price);
        listing.setQuantity(quantity);
        listing.setStatus("AVAILABLE");
        listing.setNotes(notes);
        listing.setAddress(location);
        listing.setLatitude(selectedLat);
        listing.setLongitude(selectedLng);
        listing.setTimestamp(System.currentTimeMillis());

        // Calculate EndTime
        long endTime = 0;
        if (!timeTo.isEmpty()) {
            endTime = parseEndTime(timeTo);
        }
        listing.setEndTime(endTime);

        // Owner
        String userId = "anonymous";
        if (UserManager.getInstance().getUser() != null) {
            userId = UserManager.getInstance().getUser().getUid();
        }
        listing.setOwnerId(userId);

        // Operating Hours for Display
        Map<String, String> hours = new HashMap<>();
        if (!timeFrom.isEmpty() || !timeTo.isEmpty()) {
            hours.put("Time", timeFrom + " - " + timeTo);
        }
        listing.setOperatingHours(hours);

        // Legacy Fields Defaults
        listing.setRating(0f);
        listing.setRatingCount(0);
        listing.setVerified(true);
        listing.setImageUrl(null); // Default

        // 3. Upload Image then Save
        if (selectedImageUri != null) {
            Toast.makeText(getContext(), "Uploading Image...", Toast.LENGTH_SHORT).show();
            uploadImageAndSave(listing, userId, timeFrom, timeTo, itemName, quantity, location);
        } else {
            saveToFirestore(listing, userId, timeFrom, timeTo, itemName, quantity, location);
        }
    }

    private void uploadImageAndSave(FoodBank listing, String userId, String timeFrom, String timeTo, String itemName,
            int quantity, String location) {
        try {
            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media
                    .getBitmap(requireContext().getContentResolver(), selectedImageUri);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, baos); // Compress to 60%
            byte[] data = baos.toByteArray();

            com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage
                    .getInstance();
            com.google.firebase.storage.StorageReference storageRef = storage.getReference()
                    .child("foodbank_images/" + listing.getId() + ".jpg");

            storageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        listing.setImageUrl(uri.toString());
                        saveToFirestore(listing, userId, timeFrom, timeTo, itemName, quantity, location);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                        // Proceed without image
                        saveToFirestore(listing, userId, timeFrom, timeTo, itemName, quantity, location);
                    });

        } catch (IOException e) {
            Toast.makeText(getContext(), "Image Processing Failed", Toast.LENGTH_SHORT).show();
            saveToFirestore(listing, userId, timeFrom, timeTo, itemName, quantity, location);
        }
    }

    private void saveToFirestore(FoodBank listing, String userId, String timeFrom, String timeTo, String itemName,
            int quantity, String location) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("foodbanks").document(listing.getId()).set(listing)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Listing Posted!", Toast.LENGTH_SHORT).show();

                    // Navigate to Success
                    String[] times = new String[] { timeFrom, timeTo };
                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.DonateFragmentContainer,
                                    new DonateNotifyFragment(itemName, times, 0, quantity,
                                            location, "Me"))
                            .addToBackStack("DonateSuccess")
                            .commit();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private long parseEndTime(String timeTo) {
        try {
            // Assuming format HH:mm
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = sdf.parse(timeTo);
            if (date == null)
                return 0;

            Calendar cal = Calendar.getInstance();
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(date);

            cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, 0);

            return cal.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    private void getLocation() {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && androidx.core.app.ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        tvLocationAddress.setText("Detecting Location...");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        selectedLat = location.getLatitude();
                        selectedLng = location.getLongitude();
                        getAddressFromCoords(selectedLat, selectedLng);
                    } else {
                        tvLocationAddress.setText("GPS unavailable. Try Pin.");
                        Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getAddressFromCoords(double lat, double lng) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                tvLocationAddress.setText(address);
            } else {
                tvLocationAddress.setText("Lat: " + lat + ", Lng: " + lng);
            }
        } catch (IOException e) {
            tvLocationAddress.setText("Lat: " + lat + ", Lng: " + lng);
        }
    }
}