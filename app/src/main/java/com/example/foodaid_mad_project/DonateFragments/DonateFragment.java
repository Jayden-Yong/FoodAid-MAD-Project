package com.example.foodaid_mad_project.DonateFragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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

import com.example.foodaid_mad_project.Model.FoodItem; // Import your model
import com.example.foodaid_mad_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// import com.google.firebase.firestore.FirebaseFirestore;
// import java.util.UUID;

public class DonateFragment extends Fragment {

    private String title;
    private String[] pickupTime;
    private int category;
    private String quantityStr;
    private String location;
    private String donator;

    private ImageView ivSelectedPhoto;
    private TextView tvUploadPlaceholder;
    private Uri selectedImageUri;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    // private FirebaseFirestore db; // Commented out

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // db = FirebaseFirestore.getInstance(); // Commented out

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

        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText("Donate Food");

        ivSelectedPhoto = view.findViewById(R.id.ivSelectedPhoto);
        tvUploadPlaceholder = view.findViewById(R.id.tvUploadPlaceholder);
        if (cvUploadPhoto != null) {
            cvUploadPhoto.setOnClickListener(v -> {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            });
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.Pickup_Method_List, R.layout.spinner_item_selected);
        adapter.setDropDownViewResource(R.layout.spinner_pickup_method);
        spinnerPickupMethod.setAdapter(adapter);

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

        Button btnDonate = view.findViewById(R.id.btnDonate);
        if (btnDonate != null) {
            btnDonate.setOnClickListener(v -> {
                // Validation
                if(toggleGroupDonationType.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getContext(), "Please select a donation type", Toast.LENGTH_SHORT).show();
                    return;
                }

                title = etItemName.getText().toString();
                quantityStr = etQuantity.getText().toString();
                String weight = etWeight.getText().toString();
                String expiry = etExpiryDate.getText().toString();
                String desc = etDescription.getText().toString();
                String timeFrom = etTimeFrom.getText().toString();
                String timeTo = etTimeTo.getText().toString();

                if (title.isEmpty() || quantityStr.isEmpty() || weight.isEmpty() || expiry.isEmpty() || desc.isEmpty()){
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedImageUri == null) {
                    Toast.makeText(getContext(), "Please upload a photo", Toast.LENGTH_SHORT).show();
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

                // Get User Info
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                donator = (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) ? user.getDisplayName() : "Anonymous";

                location = "Petaling Jaya"; // Mock location

                // Prepare Data variables for navigation
                pickupTime = new String[]{timeFrom, timeTo};
                category = toggleGroupDonationType.getCheckedRadioButtonId();

                // --- REAL DATABASE CODE (Commented Out) ---
                /*
                String uniqueId = UUID.randomUUID().toString();
                String imageUriString = selectedImageUri.toString();
                double lat = 3.118; // Mock lat
                double lng = 101.60; // Mock lng

                FoodItem foodItem = new FoodItem(uniqueId, title, location, quantityStr, donator, imageUriString, lat, lng);

                db.collection("donations").document(uniqueId).set(foodItem)
                    .addOnSuccessListener(aVoid -> {
                        // Success Navigation
                        FragmentManager fragmentManager = getParentFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.DonateFragmentContainer, new DonateNotifyFragment(title, pickupTime, category, Integer.parseInt(quantityStr), location, donator))
                                .addToBackStack("DonateSuccess")
                                .commit();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to save donation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                */

                // --- MOCK NAVIGATION (Temporary) ---
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.DonateFragmentContainer, new DonateNotifyFragment(title, pickupTime, category, Integer.parseInt(quantityStr), location, donator))
                        .addToBackStack("DonateSuccess")
                        .commit();
                // -----------------------------------
            });
        }
    }
}