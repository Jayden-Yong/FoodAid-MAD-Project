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
public class DonateFragment extends Fragment
// Vibe Coded map location search using Google Map
// implements OnMapReadyCallback
{

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
    private GoogleMap mMap;
    private LatLng selectedLatLng; // Stores the selected coordinates
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

        // Vibe Coded map location search using Google Map
        // --- Map Initialization ---
        // Get the map fragment from the container
        // Fragment mapFragment =
        // getChildFragmentManager().findFragmentById(R.id.mapFragmentContainer);
        // if (mapFragment == null) {
        // mapFragment = SupportMapFragment.newInstance();
        // getChildFragmentManager().beginTransaction()
        // .add(R.id.mapFragmentContainer, mapFragment)
        // .commit();
        // }
        // // Load the map asynchronously
        // if (mapFragment instanceof SupportMapFragment) {
        // ((SupportMapFragment) mapFragment).getMapAsync(this);
        // }

        // --- Search Button Logic ---
        btnSearchLocation.setOnClickListener(v -> {
            String searchString = etLocationSearch.getText().toString();
            if (!searchString.isEmpty()) {
                // searchLocation(searchString);
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

                uploadImageToStorage(selectedImageUri, title, weight, desc, categoryStr, pickupMethodStr);
            });
        }
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

    private void uploadImageToStorage(Uri imageUri, String title, double weight, String description, String category,
            String pickupMethod) {
        // Show Loading?
        Toast.makeText(getContext(), "Uploading donation...", Toast.LENGTH_SHORT).show();

        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storageReference.child("food_images/" + filename);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveDonationToFirestore(uri.toString(), title, weight, description, category, pickupMethod);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveDonationToFirestore(String imageUrl, String title, double weight, String description,
            String category, String pickupMethod) {
        // Create FoodItem
        // String donationId = db.collection("donations").document().getId(); //
        // Auto-gen later or now?
        // Let's use auto-gen ID from add()

        Map<String, Object> donation = new HashMap<>();
        donation.put("title", title);
        donation.put("weight", weight);
        donation.put("description", description); // FoodItem doesn't have desc field in Phase 1 plan?
        // Wait, Phase 1 Plan Refactor FoodItem.java: id, donatorId, donatorName, title,
        // weight, locationName, latitude, longitude, startTime, endTime, status,
        // claimedBy, category, pickupMethod, timestamp.
        // Description was NOT in FoodItem.java in Phase 1 plan?
        // Let's check FoodItem.java content if possible, or just add it to map.
        // It's better to add it.

        donation.put("donatorId", donatorId);
        donation.put("donatorName", donator);
        donation.put("locationName", location != null ? location : "Unknown Location");
        donation.put("latitude", selectedLatLng != null ? selectedLatLng.latitude : 0.0);
        donation.put("longitude", selectedLatLng != null ? selectedLatLng.longitude : 0.0);
        donation.put("startTime", startTime);
        donation.put("endTime", endTime);
        donation.put("status", "AVAILABLE");
        donation.put("claimedBy", null);
        donation.put("category", category);
        donation.put("pickupMethod", pickupMethod);
        donation.put("timestamp", System.currentTimeMillis());
        donation.put("imageUri", imageUrl);

        db.collection("donations").add(donation)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Donation posted successfully!", Toast.LENGTH_LONG).show();

                    // Navigate to Success / Notify Fragment
                    // Re-using DonateNotifyFragment logic but adapting args?
                    // Or just clear stack and go Home?
                    // Use DonateNotifyFragment as requested in Plan.
                    // It needs: title, pickupTime[], category(int), weight, location, donator,
                    // imageUri

                    String[] timeArr = new String[] { etTimeFrom.getText().toString(), etTimeTo.getText().toString() };
                    int catId = (category.equals("GROCERIES")) ? R.id.radioGroceries : R.id.radioMeals; // mapping back
                                                                                                        // for display

                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.DonateFragmentContainer,
                                    new DonateNotifyFragment(title, timeArr, catId, weight, location, donator,
                                            imageUrl))
                            .addToBackStack("DonateSuccess")
                            .commit();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save donation: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
    }
}