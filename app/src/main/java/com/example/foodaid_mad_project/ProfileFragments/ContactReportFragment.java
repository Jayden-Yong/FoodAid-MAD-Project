package com.example.foodaid_mad_project.ProfileFragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.R;
import com.google.android.material.textfield.TextInputEditText;

public class ContactReportFragment extends Fragment {

    private TextInputEditText etSubject, etMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        Button btnSendEmail = view.findViewById(R.id.btnSendEmail);
        etSubject = view.findViewById(R.id.etSubject);
        etMessage = view.findViewById(R.id.etMessage);

        btnBack.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).popBackStack());

        btnSendEmail.setOnClickListener(v -> sendEmail());
    }

    private void sendEmail() {
        String subject = etSubject.getText() != null ? etSubject.getText().toString().trim() : "";
        String message = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";

        if (subject.isEmpty() || message.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in both Subject and Message.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "support@foodaid.com" });
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FoodAid Support: " + subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
