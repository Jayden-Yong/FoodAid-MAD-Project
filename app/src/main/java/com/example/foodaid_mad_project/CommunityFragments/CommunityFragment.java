package com.example.foodaid_mad_project.CommunityFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CommunityFragment extends Fragment {

    private Spinner spinnerIssueType;
    private EditText etDescription;
    private MaterialButton btnSubmitReport;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerIssueType = view.findViewById(R.id.spinnerIssueType);
        etDescription = view.findViewById(R.id.etDescription);
        btnSubmitReport = view.findViewById(R.id.btnSubmitReport);

        // Setup Spinner
        String[] issueTypes = { "Foodbank Closed", "Incorrect Location", "Inventory Empty", "Other Issue" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, issueTypes);
        spinnerIssueType.setAdapter(adapter);

        btnSubmitReport.setOnClickListener(v -> submitReport());
    }

    private void submitReport() {
        String description = etDescription.getText().toString().trim();
        String issueType = spinnerIssueType.getSelectedItem().toString();

        if (description.isEmpty()) {
            etDescription.setError("Please describe the issue");
            return;
        }

        // Get User ID (Safe)
        String userId = "anonymous";
        if (com.example.foodaid_mad_project.UserManager.getInstance().getUser() != null) {
            userId = com.example.foodaid_mad_project.UserManager.getInstance().getUser().getUid();
        }

        Map<String, Object> report = new HashMap<>();
        report.put("reporterId", userId);
        report.put("issueType", issueType);
        report.put("description", description);
        report.put("timestamp", FieldValue.serverTimestamp());
        report.put("status", "Pending");

        FirebaseFirestore.getInstance().collection("reports")
                .add(report)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(getContext(), "Report Submitted. Thank you!", Toast.LENGTH_SHORT).show();
                    etDescription.setText(""); // Clear input
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
