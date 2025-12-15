package com.example.foodaid_mad_project.CommunityFragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.UserManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddReport;
    private ReportAdapter adapter;
    private TextView tvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewReports);
        fabAddReport = view.findViewById(R.id.fabAddReport);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        setupRecyclerView();

        fabAddReport.setOnClickListener(v -> showAddReportDialog());
    }

    private void setupRecyclerView() {
        Query query = FirebaseFirestore.getInstance()
                .collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Report> options = new FirestoreRecyclerOptions.Builder<Report>()
                .setQuery(query, Report.class)
                .build();

        adapter = new ReportAdapter(options) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                tvEmpty.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void showAddReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_report, null);
        builder.setView(dialogView);

        Spinner spinnerIssueType = dialogView.findViewById(R.id.spinnerIssueType);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        // Reuse the logic for Spinner
        String[] issueTypes = { "Foodbank Closed", "Incorrect Location", "Inventory Empty", "Other Issue" };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, issueTypes);
        spinnerIssueType.setAdapter(spinnerAdapter);

        builder.setTitle("Report Issue")
                .setPositiveButton("Submit", (dialog, which) -> {
                    // Start auto-close, actual submit handled below to prevent closing on error
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override Positive Button to prevent auto-close on validation error
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String description = etDescription.getText().toString().trim();
            String issueType = spinnerIssueType.getSelectedItem().toString();

            if (description.isEmpty()) {
                etDescription.setError("Please describe the issue");
                return;
            }

            submitReport(issueType, description);
            dialog.dismiss();
        });
    }

    private void submitReport(String issueType, String description) {
        String userId = "anonymous";
        if (UserManager.getInstance().getUser() != null) {
            userId = UserManager.getInstance().getUser().getUid();
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
                    Toast.makeText(getContext(), "Report Submitted!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
