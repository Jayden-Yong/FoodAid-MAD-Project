package com.example.foodaid_mad_project.AuthFragments;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * <h1>RegisterFragment</h1>
 * <p>
 * This fragment handles new user registration.
 * It provides:
 * <ul>
 * <li>Email/Password registration with strict password strength
 * validation.</li>
 * <li>Google Sign-In integration.</li>
 * <li>Password visibility toggling for both password and confirm password
 * fields.</li>
 * <li>Automatic basic profile creation in Firestore upon successful
 * registration.</li>
 * </ul>
 * </p>
 */
public class RegisterFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    // Firebase & Utilities
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CredentialManager credentialManager;

    // UI Elements
    private EditText etRegisterName, etRegisterEmail, etRegisterPassword, etRegisterConfirmPassword;
    private CheckBox btnCheckPassword, btnCheckConfirmPassword;
    private MaterialButton btnRegister, btnGoogle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase and Helpers
        auth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(requireContext());
        db = FirebaseFirestore.getInstance();

        // Bind Views
        initializeViews(view);

        // Setup Listeners
        setupListeners(view);
    }

    /**
     * Finds and assigns all UI components from the layout.
     */
    private void initializeViews(View view) {
        etRegisterName = view.findViewById(R.id.etRegisterName);
        etRegisterEmail = view.findViewById(R.id.etRegisterEmail);
        etRegisterPassword = view.findViewById(R.id.etRegisterPassword);
        etRegisterConfirmPassword = view.findViewById(R.id.etRegisterConfirmPassword);
        btnCheckPassword = view.findViewById(R.id.btnCheckPassword);
        btnCheckConfirmPassword = view.findViewById(R.id.btnCheckConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnGoogle = view.findViewById(R.id.btnGoogle);
    }

    /**
     * Sets up click listeners for buttons and input interactions.
     */
    private void setupListeners(View view) {
        // Back Button
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showLandingPage();
            }
        });

        // Checkbox Listeners (implemented in onCheckedChanged)
        btnCheckPassword.setOnCheckedChangeListener(this);
        btnCheckConfirmPassword.setOnCheckedChangeListener(this);

        // Main Actions
        btnRegister.setOnClickListener(v -> registerNewUser());
        btnGoogle.setOnClickListener(v -> launchCredentialManager());
    }

    /**
     * Validates password strength according to security requirements.
     * Password must:
     * <ul>
     * <li>Be at least 8 characters long</li>
     * <li>Contain at least one uppercase letter</li>
     * <li>Contain at least one lowercase letter</li>
     * <li>Contain at least one digit</li>
     * <li>Contain at least one special character</li>
     * </ul>
     *
     * @param password The password to validate.
     * @return Error message if validation fails, null if password is valid.
     */
    private String validatePasswordStrength(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUppercase = true;
            else if (Character.isLowerCase(c))
                hasLowercase = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            else if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c))
                hasSpecial = true;

            // Early exit if all requirements are met
            if (hasUppercase && hasLowercase && hasDigit && hasSpecial) {
                break;
            }
        }

        if (!hasUppercase)
            return "Password must contain at least one uppercase letter";
        if (!hasLowercase)
            return "Password must contain at least one lowercase letter";
        if (!hasDigit)
            return "Password must contain at least one digit";
        if (!hasSpecial)
            return "Password must contain at least one special character";

        return null; // Password is valid
    }

    /**
     * Validates input fields and attempts to create a new user in Firebase Auth.
     */
    private void registerNewUser() {
        String name = etRegisterName.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String password = etRegisterPassword.getText().toString();
        String confirmPassword = etRegisterConfirmPassword.getText().toString();

        // Step 1: Input Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please enter all credentials", Toast.LENGTH_LONG).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_LONG).show();
            return;
        }

        if (!confirmPassword.equals(password)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_LONG).show();
            return;
        }

        String passwordError = validatePasswordStrength(password);
        if (passwordError != null) {
            Toast.makeText(getContext(), passwordError, Toast.LENGTH_LONG).show();
            return;
        }

        // Step 2: Create User in Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        // Step 3: Create User Profile in Firestore
                        saveUserToFirestore(user, "email", name, null);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getContext(), "User already exists", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    /**
     * Handles password visibility toggling for both password fields.
     */
    @Override
    public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.btnCheckPassword) {
            togglePasswordVisibility(etRegisterPassword, isChecked);
        } else {
            togglePasswordVisibility(etRegisterConfirmPassword, isChecked);
        }
    }

    /**
     * Helper to toggle password visibility and preserve cursor position/font.
     */
    private void togglePasswordVisibility(EditText editText, boolean isVisible) {
        Typeface originalTypeface = editText.getTypeface();
        if (isVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        editText.setTypeface(originalTypeface);
        editText.setSelection(editText.getText().length());
    }

    // ============================================================================================
    // GOOGLE SIGN IN
    // ============================================================================================

    private void launchCredentialManager() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // For Reg, we might want to allow any account
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

    private void handleSignIn(Credential credential) {
        if (credential instanceof CustomCredential
                && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            CustomCredential customCredential = (CustomCredential) credential;
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);

            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            requireActivity().runOnUiThread(
                    () -> Toast.makeText(getContext(), "Unexpected credential type", Toast.LENGTH_LONG).show());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String displayName = user.getDisplayName();
                            String photoUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;
                            saveUserToFirestore(user, "google", displayName, photoUrl);
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
    // FIRESTORE
    // ============================================================================================

    /**
     * Saves or updates user data in Firestore.
     *
     * @param user         The FirebaseUser object.
     * @param providerType The type of login ("email" or "google").
     * @param localName    The name provided in the registration form (if
     *                     applicable).
     * @param photoUrl     The photo URL to save.
     */
    private void saveUserToFirestore(FirebaseUser user, String providerType, String localName, String photoUrl) {
        String uid = user.getUid();
        String email = user.getEmail();

        // Priority 1: Name from Registration Form
        // Priority 2: Name from Google Account
        String displayName = (localName != null && !localName.isEmpty()) ? localName : user.getDisplayName();

        // Priority 1: Photo from args
        // Priority 2: Photo from Google Account
        String finalPhotoUrl = (photoUrl != null) ? photoUrl
                : (user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);

        // Fallback Name Generation
        if (displayName == null || displayName.isEmpty()) {
            if (email != null && email.contains("@")) {
                displayName = email.substring(0, email.indexOf("@"));
            } else {
                displayName = "User";
            }
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", email);
        userData.put("displayName", displayName);
        userData.put("photoUrl", finalPhotoUrl);

        // logic for new vs existing handling
        if ("email".equals(providerType)) {
            // New Email Registration: Initialize metadata
            userData.put("createdAt", System.currentTimeMillis());
            userData.put("earnedBadges", new java.util.ArrayList<String>());
        } else {
            // Google Login/Reg: Update login time
            userData.put("lastLogin", System.currentTimeMillis());
        }

        // Save to Firestore (Merge)
        db.collection("users").document(uid).set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Fetch full user object for Session Manager
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                User currentUser = documentSnapshot.toObject(User.class);
                                UserManager.getInstance().setUser(currentUser);

                                Toast.makeText(getContext(), "Registration/Login successful!", Toast.LENGTH_LONG)
                                        .show();
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                requireActivity().finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(),
                                    "Failed to retrieve user data: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
