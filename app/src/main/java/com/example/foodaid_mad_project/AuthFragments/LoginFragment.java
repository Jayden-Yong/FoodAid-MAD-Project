package com.example.foodaid_mad_project.AuthFragments;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.AuthActivity;
import com.example.foodaid_mad_project.MainActivity;
import com.example.foodaid_mad_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

public class LoginFragment extends Fragment {

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_REMEMBER_ME = "remember_me";

    private FirebaseAuth auth;
    private CredentialManager credentialManager;
    private EditText etLoginEmail, etLoginPassword;
    private CheckBox btnCheckPassword, cbRememberMe;
    private TextView tvForgotPassword;
    private MaterialButton btnLogin, btnGoogle;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handle back button
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showLandingPage();
            }
        });

        auth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(requireContext());

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        etLoginEmail = view.findViewById(R.id.etLoginEmail);
        etLoginPassword = view.findViewById(R.id.etLoginPassword);
        btnCheckPassword = view.findViewById(R.id.btnCheckPassword);
        cbRememberMe = view.findViewById(R.id.cbRememberMe);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoogle = view.findViewById(R.id.btnGoogle);

        // Load saved email if Remember Me was checked
        loadSavedEmail();

        btnLogin.setOnClickListener(v -> loginEmail());
        tvForgotPassword.setOnClickListener(v -> forgotPassword());

        btnCheckPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Typeface passwordTypeface = etLoginPassword.getTypeface();

            if (isChecked) {
                etLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }

            etLoginPassword.setTypeface(passwordTypeface);
            etLoginPassword.setSelection(etLoginPassword.getText().length());
        });

        btnGoogle.setOnClickListener(v -> launchCredentialManager());
    }

    private void forgotPassword() {
        // check if email is empty
        String email = etLoginEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your email first", Toast.LENGTH_LONG).show();
            return;
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Password reset email sent", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Failed to send password reset email", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginEmail() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        // validate input
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter all credentials", Toast.LENGTH_LONG).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Save email if Remember Me is checked
                            saveEmailPreference(email);

                            Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getContext(), MainActivity.class));
                        } else {
                            Toast.makeText(getContext(), "Login failed, please try again", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Load saved email if Remember Me was previously checked
     */
    private void loadSavedEmail() {
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
        if (rememberMe) {
            String savedEmail = sharedPreferences.getString(KEY_SAVED_EMAIL, "");
            etLoginEmail.setText(savedEmail);
            cbRememberMe.setChecked(true);
        }
    }

    /**
     * Save or clear email based on Remember Me checkbox state
     */
    private void saveEmailPreference(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (cbRememberMe.isChecked()) {
            // Save email and Remember Me state
            editor.putString(KEY_SAVED_EMAIL, email);
            editor.putBoolean(KEY_REMEMBER_ME, true);
        } else {
            // Clear saved email and Remember Me state
            editor.remove(KEY_SAVED_EMAIL);
            editor.putBoolean(KEY_REMEMBER_ME, false);
        }

        editor.apply();
    }

    // GOOGLE sign in
    private void launchCredentialManager() {
        // First try with authorized accounts only
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                requireContext(),
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        // If no authorized accounts, try with all accounts
                        launchCredentialManagerAllAccounts();
                    }
                }
        );
    }

    private void launchCredentialManagerAllAccounts() {
        // Allow selection from all Google accounts
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                requireContext(),
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
                    }
                }
        );
    }

    private void handleSignIn(Credential credential) {
        // check if credential is google id
        if (credential instanceof CustomCredential
            && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            CustomCredential customCredential = (CustomCredential) credential;
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);

            // sign in to firebase with google
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "Unexpected credential type", Toast.LENGTH_LONG).show()
            );
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getContext(), MainActivity.class));
                        requireActivity().finish();
                    } else {
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";
                        Toast.makeText(getContext(), "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
