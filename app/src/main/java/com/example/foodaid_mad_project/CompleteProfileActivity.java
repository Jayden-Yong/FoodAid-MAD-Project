package com.example.foodaid_mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodaid_mad_project.AuthFragments.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.Toast;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompleteProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText etFullName;
    private CheckBox cbNone, cbHalal, cbVegetarian, cbNoBeef;
    private Spinner spinnerCPFaculty, spinnerCPResidential;
    private MaterialButton btnCPComplete;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        // populate faculty spinner
        spinnerCPFaculty = findViewById(R.id.spinnerCPFaculty);
        ArrayAdapter<CharSequence> adapterFaculty = ArrayAdapter.createFromResource(this, R.array.Faculty_List, R.layout.spinner_item_selected);
        adapterFaculty.setDropDownViewResource(R.layout.spinner_pickup_method);
        spinnerCPFaculty.setAdapter(adapterFaculty);

        // populate residential college spinner
        spinnerCPResidential = findViewById(R.id.spinnerCPResidential);
        ArrayAdapter<CharSequence> adapterResidential = ArrayAdapter.createFromResource(this, R.array.Residential_College_List, R.layout.spinner_item_selected);
        adapterResidential.setDropDownViewResource(R.layout.spinner_pickup_method);
        spinnerCPResidential.setAdapter(adapterResidential);

        // other widgets
        etFullName = findViewById(R.id.etFullName);
        cbNone = findViewById(R.id.cbNone);
        cbHalal = findViewById(R.id.cbHalal);
        cbVegetarian = findViewById(R.id.cbVegetarian);
        cbNoBeef = findViewById(R.id.cbNoBeef);
        btnCPComplete = findViewById(R.id.btnCPComplete);

        btnCPComplete.setOnClickListener(v -> onClickComplete(v));
    }

    public void onClickComplete(View view) {
        String fullName = etFullName.getText().toString();
        String faculty = spinnerCPFaculty.getSelectedItemId() == 0 ? "" : spinnerCPFaculty.getSelectedItem().toString();
        String residentialCollege = spinnerCPResidential.getSelectedItemId() == 0 ? "" : spinnerCPResidential.getSelectedItem().toString();
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

                    Toast.makeText(this, "Profile Completed!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CompleteProfileActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
    }
}
