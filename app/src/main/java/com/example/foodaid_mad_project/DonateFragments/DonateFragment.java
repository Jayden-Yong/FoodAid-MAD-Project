package com.example.foodaid_mad_project.DonateFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.HomeFragments.ItemDetailsFragment;
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
                // TODO: Spinner must be selected
            }
        });

        // Set Toolbar title
        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText(getString(R.string.String, "Donate Food"));

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

                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.DonateFragmentContainer, new DonateNotifyFragment(title, pickupTime, category, quantity, location, donator))
                        .addToBackStack("DonateSuccess")
                        .commit();
            });
        }
    }
}