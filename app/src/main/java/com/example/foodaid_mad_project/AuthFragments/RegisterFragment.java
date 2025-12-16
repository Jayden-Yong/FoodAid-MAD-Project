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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class RegisterFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CredentialManager credentialManager;
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

        // Handle back button
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showLandingPage();
            }
        });

        auth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(requireContext());
        db = FirebaseFirestore.getInstance();

        etRegisterName = view.findViewById(R.id.etRegisterName);
        etRegisterEmail = view.findViewById(R.id.etRegisterEmail);
        etRegisterPassword = view.findViewById(R.id.etRegisterPassword);
        etRegisterConfirmPassword = view.findViewById(R.id.etRegisterConfirmPassword);
        btnCheckPassword = view.findViewById(R.id.btnCheckPassword);
        btnCheckConfirmPassword = view.findViewById(R.id.btnCheckConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnGoogle = view.findViewById(R.id.btnGoogle);

        btnCheckPassword.setOnCheckedChangeListener(this);
        btnCheckConfirmPassword.setOnCheckedChangeListener(this);

        btnRegister.setOnClickListener(v -> registerNewUser());
        btnGoogle.setOnClickListener(v -> launchCredentialManager());
    }

    /**
     * Validates password strength according to security requirements.
     * Password must:
     * - Be at least 8 characters long
     * - Contain at least one uppercase letter
     * - Contain at least one lowercase letter
     * - Contain at least one digit
     * - Contain at least one special character
     *
     * @param password The password to validate
     * @return Error message if validation fails, null if password is valid
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
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
                hasSpecial = true;
            }

            // Early exit if all requirements are met
            if (hasUppercase && hasLowercase && hasDigit && hasSpecial) {
                break;
            }
        }

        if (!hasUppercase) {
            return "Password must contain at least one uppercase letter";
        }
        if (!hasLowercase) {
            return "Password must contain at least one lowercase letter";
        }
        if (!hasDigit) {
            return "Password must contain at least one digit";
        }
        if (!hasSpecial) {
            return "Password must contain at least one special character";
        }

        return null; // Password is valid
    }

    private void registerNewUser() {
        String name = etRegisterName.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String password = etRegisterPassword.getText().toString();
        String confirmPassword = etRegisterConfirmPassword.getText().toString();

        // input validations
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

        // register new user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
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

    @Override
    public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.btnCheckPassword) {
            Typeface passwordTypeface = etRegisterPassword.getTypeface();
            if (isChecked) {
                etRegisterPassword
                        .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etRegisterPassword.setTypeface(passwordTypeface);
            etRegisterPassword.setSelection(etRegisterPassword.getText().length());
        } else {
            Typeface confirmPasswordTypeface = etRegisterConfirmPassword.getTypeface();
            if (isChecked) {
                etRegisterConfirmPassword
                        .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etRegisterConfirmPassword
                        .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etRegisterConfirmPassword.setTypeface(confirmPasswordTypeface);
            etRegisterConfirmPassword.setSelection(etRegisterConfirmPassword.getText().length());
        }
    }

    // use google account
    private void launchCredentialManager() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // starts credential manager ui
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
        // check if credential is google id
        if (credential instanceof CustomCredential
                && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            CustomCredential customCredential = (CustomCredential) credential;
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);

            // sign in to firebase with google
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

    private void saveUserToFirestore(FirebaseUser user, String providerType, String localName, String photoUrl) {
        String uid = user.getUid();
        String email = user.getEmail();

        // Priority: localName (Form matches intent) > user.getDisplayName()
        // (Firebase/Google)
        String displayName = (localName != null && !localName.isEmpty()) ? localName : user.getDisplayName();

        // Priority: photoUrl (Google) > user.getPhotoUrl()
        String finalPhotoUrl = (photoUrl != null) ? photoUrl
                : (user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);

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
        // userData.put("earnedBadges", ...); // Not needed to explicitly put empty list
        // on MERGE if we don't want to overwrite, but for new user it's good.
        // We'll let Firestore handle it or initialize if missing in future logic.
        // For now, let's keep it simple and safe.
        // If document exists, we merge. If we want to Initialize badges ONLY if they
        // don't exist, we can't do it easily with just .set(merge).
        // But for "Register & Go", minimal friction.

        // Checking if we should overwrite badges?
        // If it's a NEW registration, overwrite is fine.
        // If it's a LOGIN that triggers this, we shouldn't overwrite badges.

        // Simplified Logic: Just update profile info.
        userData.put("userType", "student");

        if ("email".equals(providerType)) {
            userData.put("createdAt", System.currentTimeMillis());
            userData.put("earnedBadges", new java.util.ArrayList<String>()); // Only init for new email reg
        } else {
            userData.put("lastLogin", System.currentTimeMillis());
            // Don't overwrite badges for Google Login if they exist
        }

        db.collection("users").document(uid).set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Fetch latest data to ensure UserManager is up to date (handles merges for
                    // existing users)
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
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to retrieve user data: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                });
    }
}
