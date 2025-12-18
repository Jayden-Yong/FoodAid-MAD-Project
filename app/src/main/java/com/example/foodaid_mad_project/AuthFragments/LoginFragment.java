package com.example.foodaid_mad_project.AuthFragments;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.InputType;
import android.util.Patterns;
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
import com.example.foodaid_mad_project.UserManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * <h1>LoginFragment</h1>
 * <p>
 * This fragment handles the user login process.
 * It supports:
 * <ul>
 * <li>Email/Password login.</li>
 * <li>Google Sign-In integration (using Android Credential Manager).</li>
 * <li>"Remember Me" functionality using SharedPreferences.</li>
 * <li>Password visibility toggling.</li>
 * <li>Navigation to "Forgot Password" and Registration flows.</li>
 * </ul>
 * </p>
 */
public class LoginFragment extends Fragment {

    // SharedPreferences Constants
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_REMEMBER_ME = "remember_me";

    // Firebase & Utilities
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CredentialManager credentialManager;
    private SharedPreferences sharedPreferences;

    // UI Elements
    private EditText etLoginEmail, etLoginPassword;
    private CheckBox btnCheckPassword, cbRememberMe;
    private TextView tvForgotPassword;
    private MaterialButton btnLogin, btnGoogle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase and Helpers
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        credentialManager = CredentialManager.create(requireContext());
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Bind Views
        initializeViews(view);

        // Load Preferences
        loadSavedEmail();

        // Setup Listeners
        setupListeners(view);
    }

    /**
     * Finds and assigns all UI components from the layout.
     *
     * @param view The root view of the fragment.
     */
    private void initializeViews(View view) {
        etLoginEmail = view.findViewById(R.id.etLoginEmail);
        etLoginPassword = view.findViewById(R.id.etLoginPassword);
        btnCheckPassword = view.findViewById(R.id.btnCheckPassword);
        cbRememberMe = view.findViewById(R.id.cbRememberMe);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoogle = view.findViewById(R.id.btnGoogle);
    }

    /**
     * Sets up click listeners for buttons and input interactions.
     *
     * @param view The root view (used for finding the back button).
     */
    private void setupListeners(View view) {
        // Back Button Logic
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showLandingPage();
            }
        });

        // Main Actions
        btnLogin.setOnClickListener(v -> loginEmail());
        tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
        btnGoogle.setOnClickListener(v -> launchCredentialManager());

        // Password Visibility Toggle
        btnCheckPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Typeface passwordTypeface = etLoginPassword.getTypeface(); // Preserve font style
            if (isChecked) {
                // Show password
                etLoginPassword
                        .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                // Hide password
                etLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etLoginPassword.setTypeface(passwordTypeface);
            etLoginPassword.setSelection(etLoginPassword.getText().length()); // Keep cursor at end
        });
    }

    /**
     * Navigates to the ResetPasswordFragment.
     */
    private void navigateToForgotPassword() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.authFragmentContainer, new ResetPasswordFragment(auth))
                .addToBackStack(null)
                .commit();
    }

    /**
     * <h2>loginEmail()</h2>
     * <p>
     * Validates input fields and attempts to sign in via Firebase Auth with
     * Email/Password.
     * </p>
     */
    private void loginEmail() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        // Step 1: Validate Inputs
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter all credentials", Toast.LENGTH_LONG).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_LONG).show();
            return;
        }

        // Step 2: Firebase Sign In
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Step 3: Success Handler
                            saveEmailPreference(email); // Save if "Remember Me" is checked
                            fetchUserAndNavigate(auth.getCurrentUser().getUid());
                        } else {
                            // Step 4: Failure Handler
                            Toast.makeText(getContext(), "Login failed, please try again", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // ============================================================================================
    // PREFERENCES (Remember Me)
    // ============================================================================================

    /**
     * Loads the saved email from SharedPreferences if "Remember Me" was previously
     * enabled.
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
     * Saves or clears the user's email based on the "Remember Me" checkbox state.
     *
     * @param email The email to save.
     */
    private void saveEmailPreference(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (cbRememberMe.isChecked()) {
            editor.putString(KEY_SAVED_EMAIL, email);
            editor.putBoolean(KEY_REMEMBER_ME, true);
        } else {
            editor.remove(KEY_SAVED_EMAIL);
            editor.putBoolean(KEY_REMEMBER_ME, false);
        }
        editor.apply();
    }

    // ============================================================================================
    // GOOGLE SIGN IN LOGIC
    // ============================================================================================

    /**
     * Initiates the Google Sign-In process using Android Credential Manager.
     * Tries to find authorized accounts first.
     */
    private void launchCredentialManager() {
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
                        // If no authorized accounts found, try asking user to select any account
                        launchCredentialManagerAllAccounts();
                    }
                });
    }

    /**
     * Fallback method to show selector for ALL Google accounts on the device.
     */
    private void launchCredentialManagerAllAccounts() {
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
                        requireActivity().runOnUiThread(() -> Toast
                                .makeText(getContext(), "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_LONG)
                                .show());
                    }
                });
    }

    /**
     * Handles the result from Credential Manager and extracts the Google ID Token.
     */
    private void handleSignIn(Credential credential) {
        if (credential instanceof CustomCredential
                && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            CustomCredential customCredential = (CustomCredential) credential;
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);

            // Proceed to Firebase Auth
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            requireActivity().runOnUiThread(
                    () -> Toast.makeText(getContext(), "Unexpected credential type", Toast.LENGTH_LONG).show());
        }
    }

    /**
     * Authenticates with Firebase using the Google ID Token.
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Ensure user exists in Firestore
                            saveUserToFirestore(user);
                        }
                    } else {
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";
                        Toast.makeText(getContext(), "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ============================================================================================
    // FIRESTORE & NAVIGATION
    // ============================================================================================

    /**
     * create or update the user's document in Firestore after a Google Sign-In.
     * Merges with existing data to preserve fields like 'earnedBadges'.
     */
    private void saveUserToFirestore(FirebaseUser user) {
        String uid = user.getUid();
        String email = user.getEmail();
        String googleName = user.getDisplayName();
        String googlePhotoUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;

        // Fallback for missing display name
        String defaultName = (googleName != null && !googleName.isEmpty()) ? googleName
                : (email != null && email.contains("@") ? email.substring(0, email.indexOf("@")) : "User");

        final String finalDefaultName = defaultName;

        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("lastLogin", System.currentTimeMillis());

            if (documentSnapshot.exists()) {
                // UPDATE EXISTING USER: Only fill missing fields
                if (documentSnapshot.getString("photoUrl") == null && googlePhotoUrl != null) {
                    userData.put("photoUrl", googlePhotoUrl);
                }
                if (documentSnapshot.getString("displayName") == null) {
                    userData.put("displayName", finalDefaultName);
                }
                userData.put("email", email);
            } else {
                // CREATE NEW USER: Initialize all fields
                userData.put("uid", uid);
                userData.put("email", email);
                userData.put("displayName", finalDefaultName);
                userData.put("photoUrl", googlePhotoUrl);
                userData.put("earnedBadges", java.util.Collections.emptyList());
                userData.put("createdAt", System.currentTimeMillis());
            }

            // Save to Firestore (Merge to avoid overwriting unrelated data)
            db.collection("users").document(uid).set(userData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> fetchUserAndNavigate(uid))
                    .addOnFailureListener(e -> Toast.makeText(getContext(),
                            "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show());

        }).addOnFailureListener(e -> Toast.makeText(getContext(),
                "Error checking user data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Fetches the full User object from Firestore and navigates to the Main
     * Activity.
     * Updates the global UserManager singleton.
     */
    private void fetchUserAndNavigate(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    try {
                        com.example.foodaid_mad_project.AuthFragments.User currentUser = documentSnapshot
                                .toObject(com.example.foodaid_mad_project.AuthFragments.User.class);
                        UserManager.getInstance().setUser(currentUser);

                        Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getContext(), MainActivity.class));
                        requireActivity().finish();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error loading user data: " + e.getMessage(), Toast.LENGTH_LONG)
                                .show();
                        // Fail safe: Navigate anyway to prevent user lockout
                        startActivity(new Intent(getContext(), MainActivity.class));
                        requireActivity().finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to retrieve user data: " + e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                });
    }
}
