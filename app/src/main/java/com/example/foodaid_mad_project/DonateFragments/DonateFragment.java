package com.example.foodaid_mad_project.DonateFragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import com.example.foodaid_mad_project.HomeFragments.ItemDetailsFragment;
import com.example.foodaid_mad_project.HomeFragments.MapFragment;
import com.example.foodaid_mad_project.R;

import java.util.Objects;

public class DonateFragment extends Fragment {

    private ItemDetailsFragment itemDetailsFragment;
    private String title;
    private String[] pickupTime;
    private int category;
    private int quantity;
    private String location;
    private String donator;

    //Upload photo
    private ImageView ivSelectedPhoto;
    private TextView tvUploadPlaceholder;
    private Uri selectedImageUri;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                // Image selected successfully
                selectedImageUri = uri;

                // Update UI: Show Image, Hide Placeholder
                ivSelectedPhoto.setImageURI(uri);
                ivSelectedPhoto.setVisibility(View.VISIBLE);
                tvUploadPlaceholder.setVisibility(View.GONE);

            } else {
                // User cancelled the picker
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
        //TODO:Location Setting variable
        Spinner spinnerPickupMethod = view.findViewById(R.id.spinnerPickupMethod);
        EditText etTimeFrom = view.findViewById(R.id.etTimeFrom);
        EditText etTimeTo = view.findViewById(R.id.etTimeTo);
        CheckBox cbConfirm = view.findViewById(R.id.cbConfirm);

        // Set Toolbar title
        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText(getString(R.string.String, "Donate Food"));

        //Setup photo upload
        ivSelectedPhoto = view.findViewById(R.id.ivSelectedPhoto);
        tvUploadPlaceholder = view.findViewById(R.id.tvUploadPlaceholder);
        if (cvUploadPhoto != null) {
            cvUploadPhoto.setOnClickListener(v -> {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            });
        }

        // Set Spinner Item
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.Pickup_Method_List, R.layout.spinner_item_selected);
        adapter.setDropDownViewResource(R.layout.spinner_pickup_method);
        spinnerPickupMethod.setAdapter(adapter);
        spinnerPickupMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(0);
            }
        });

        // Set System Back action
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Manually pop the Donate Fragment
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });

        // Set Toolbar navigation button
        Toolbar toolbar = view.findViewById(R.id.Toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("Donate", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            });
        }

        // Set Donate Button Action
        Button btnDonate = view.findViewById(R.id.btnDonate);
        if (btnDonate != null) {
            btnDonate.setOnClickListener(v -> {
                //TODO: Save data to Firebase

                //TODO: get data from inputs and replace the test data
                //Test data
                title = "Tiger Biscuit Original Multipack (7 sachets)";
                pickupTime = new String[]{"10:00AM", "12:00PM"};
                category = R.id.radioPantry;
                quantity = 100;
                location = "Tasik Varsiti";
                donator = "KMUM (Kesatuan Mahasiswa UM)";



                // Validation: Check if all fields are filled
                if(toggleGroupDonationType.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getContext(), "Please select a donation type", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (etItemName.getText().toString().isEmpty() ||
                        etQuantity.getText().toString().isEmpty() ||
                        etWeight.getText().toString().isEmpty() ||
                        etExpiryDate.getText().toString().isEmpty() ||
                        etDescription.getText().toString().isEmpty()){
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ivSelectedPhoto.getVisibility() == View.GONE) {
                    Toast.makeText(getContext(), "Please upload a photo", Toast.LENGTH_SHORT).show();
                }

                //TODO:Location Validate

                if (spinnerPickupMethod.getSelectedItemPosition() == 0) {
                    Toast.makeText(getContext(), "Please select a pickup method", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (etTimeFrom.getText().toString().isEmpty() ||
                        etTimeTo.getText().toString().isEmpty()){
                    Toast.makeText(getContext(), "Please fill in all time fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!cbConfirm.isChecked()) {
                    Toast.makeText(getContext(), "You must confirm the donation details", Toast.LENGTH_SHORT).show();
                    return;
                }

                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.DonateFragmentContainer, new DonateNotifyFragment(title, pickupTime, category, quantity, location, donator))
                        .addToBackStack("DonateSuccess")
                        .commit();
            });
        }
    }
}