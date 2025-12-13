package com.example.foodaid_mad_project.AuthFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodaid_mad_project.HomeFragments.HomeFragment;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.UserManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.Toast;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompleteProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private EditText etFullName;
    private CheckBox cbNone, cbHalal, cbVegetarian, cbNoBeef;
    private Spinner spinnerCPFaculty, spinnerCPResidential;
    private MaterialButton btnCPComplete;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_complete_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // populate faculty spinner
        spinnerCPFaculty = view.findViewById(R.id.spinnerCPFaculty);
        List<String> facultyList = new ArrayList<>();
        facultyList.add("Choose your faculty");
        facultyList.add("Faculty of Built Environment");
        facultyList.add("Faculty of Languages and Linguistics");
        facultyList.add("Faculty of Pharmacy");
        facultyList.add("Faculty of Engineering");
        facultyList.add("Faculty of Education");
        facultyList.add("Faculty of Dentistry");
        facultyList.add("Faculty of Business and Economics");
        facultyList.add("Faculty of Medicine");
        facultyList.add("Faculty of Science");
        facultyList.add("Faculty of Computer Science and Information Technology");
        facultyList.add("Faculty of Arts and Social Sciences");
        facultyList.add("Faculty of Creative Arts");
        facultyList.add("Faculty of Law");
        facultyList.add("Faculty of Sport & Exercise Sciences");

        ArrayAdapter<String> facultyAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                facultyList);

        facultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCPFaculty.setAdapter(facultyAdapter);

        // populate residential college spinner
        spinnerCPResidential = view.findViewById(R.id.spinnerCPResidential);
        List<String> residentialList = new ArrayList<>();
        residentialList.add("Choose your residential college");
        residentialList.add("Kolej Kediaman Pertama (ASTAR)");
        residentialList.add("Kolej Kediaman Kedua (Tuanku Bahiyah)");
        residentialList.add("Kolej Kediaman Ketiga (Tunku Kurshiah)");
        residentialList.add("Kolej Kediaman Keempat (Bestari)");
        residentialList.add("Kolej Kediaman Kelima (Dayasari)");
        residentialList.add("Kolej Kediaman Keenam (Ibnu Sina)");
        residentialList.add("Kolej Kediaman Ketujuh (Za'ba)");
        residentialList.add("Kolej Kediaman Kelapan (Kinabalu)");
        residentialList.add("Kolej Kediaman Kesembilan (Tun Syed Zahiruddin)");
        residentialList.add("Kolej Kediaman Kesepuluh (Tun Ahmad Zaidi)");
        residentialList.add("Kolej Kediaman Kesebelas (Ungku Aziz)");
        residentialList.add("Kolej Kediaman Keduabelas (Raja Dr. Nazrin Shah)");
        residentialList.add("Kolej Kediaman Ketigabelas");

        ArrayAdapter<String> residentialAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                residentialList);

        residentialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCPResidential.setAdapter(residentialAdapter);

        // other widgets
        etFullName = view.findViewById(R.id.etFullName);
        cbNone = view.findViewById(R.id.cbNone);
        cbHalal = view.findViewById(R.id.cbHalal);
        cbVegetarian = view.findViewById(R.id.cbVegetarian);
        cbNoBeef = view.findViewById(R.id.cbNoBeef);
        btnCPComplete = view.findViewById(R.id.btnCPComplete);

        btnCPComplete.setOnClickListener(v -> onClickComplete(view));
    }

    public void onClickComplete(View view) {
        String fullName = etFullName.getText().toString();
        String faculty = spinnerCPFaculty.getSelectedItem().toString();
        String residentialCollege = spinnerCPResidential.getSelectedItem().toString();
        List<String> dietaryPreferences = new ArrayList<>();

        if (cbNone.isChecked()) {
            dietaryPreferences.add("None");
        } else {
            if (cbHalal.isChecked()) {
                dietaryPreferences.add("Halal");
            }
            if (cbVegetarian.isChecked()) {
                dietaryPreferences.add("Vegetarian");
            }
            if (cbNoBeef.isChecked()) {
                dietaryPreferences.add("No Beef");
            }
        }

        // prepare to upload to firestore
        Map<String, Object> additionalUserData = new HashMap<>();
        additionalUserData.put("fullName", fullName);
        additionalUserData.put("faculty", faculty);
        additionalUserData.put("residentialCollege", residentialCollege);
        additionalUserData.put("dietaryPreferences", dietaryPreferences);

        // upload to firestore
        db = FirebaseFirestore.getInstance();
        User user = UserManager.getInstance().getUser();
        String uid = user.getUid();

        DocumentReference userRef = db.collection("users").document(uid);
        userRef.update(additionalUserData)
                .addOnSuccessListener(aVoid -> {
                    user.addAdditionalData(additionalUserData);
                    UserManager.getInstance().setUser(user);

                    Toast.makeText(requireContext(), "Profile Completed!", Toast.LENGTH_SHORT).show();

                    // navigate back to home fragment
                    Navigation.findNavController(view).navigate(R.id.homeFragment);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
    }
}
