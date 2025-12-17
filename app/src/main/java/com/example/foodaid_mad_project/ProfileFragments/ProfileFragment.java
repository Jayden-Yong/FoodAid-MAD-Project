package com.example.foodaid_mad_project.ProfileFragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodaid_mad_project.AuthActivity;
import com.example.foodaid_mad_project.Model.Badge;
import com.example.foodaid_mad_project.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment {

    private RecyclerView badgesRecyclerView;
    private BadgeAdapter badgeAdapter;
    private List<Badge> allBadges; // Master list of badges
    private List<String> userEarnedBadges = new ArrayList<>(); // IDs of badges user has earned

    private ImageView ivProfile;
    private TextView tvUserName, tvUserId;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private GoogleSignInClient mGoogleSignInClient;

    // Image Picker Launcher
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Helpers
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Configure Google Sign In to support logout
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Initialize Image Picker
        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                uploadImageToStorage(uri);
            } else {
                Log.d("Profile", "No media selected");
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserId = view.findViewById(R.id.tvUserId);
        ivProfile = view.findViewById(R.id.ivProfile);
        View profileImageContainer = view.findViewById(R.id.profileImageContainer);
        badgesRecyclerView = view.findViewById(R.id.badgesContainer);

        setupBadges();
        loadUserData();

        // Profile Image Click -> Pick Image
        profileImageContainer.setOnClickListener(v -> {
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        // Logout
        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            mGoogleSignInClient.signOut();
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // View All Badges (Claims)
        TextView tvViewAllBadges = view.findViewById(R.id.tvViewAllBadges);
        tvViewAllBadges.setOnClickListener(v -> {
            // Use NavController to switch tabs since Profile is inside NavHost
            if (getActivity() instanceof com.example.foodaid_mad_project.MainActivity) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = getActivity()
                        .findViewById(R.id.bottomNavigationView);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.impactFragment);
                }
            }
        });

        // Edit Profile (Placeholder or simple toast for now)
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // --- New Features ---
        View btnChangePassword = view.findViewById(R.id.btnChangePassword);
        View btnPrivacySettings = view.findViewById(R.id.btnPrivacySettings);
        android.widget.Switch switchPush = view.findViewById(R.id.switchPush);
        android.widget.Switch switchEmail = view.findViewById(R.id.switchEmail);
        View btnHelpFaq = view.findViewById(R.id.btnHelpFaq);
        View btnContactReport = view.findViewById(R.id.btnContactReport);

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnPrivacySettings.setOnClickListener(v -> showPrivacySettingsDialog());
        btnHelpFaq.setOnClickListener(v -> showHelpFaqDialog());
        btnContactReport.setOnClickListener(v -> showContactReportDialog());

        // Load Toggle States
        loadNotificationSettings(switchPush, switchEmail);

        // Toggle Listeners
        switchPush.setOnCheckedChangeListener(
                (buttonView, isChecked) -> updateNotificationSetting("pushNotificationEnabled", isChecked));
        switchEmail.setOnCheckedChangeListener(
                (buttonView, isChecked) -> updateNotificationSetting("emailNotificationEnabled", isChecked));
    }

    private void showChangePasswordDialog() {
        if (auth.getCurrentUser() == null || auth.getCurrentUser().getEmail() == null)
            return;

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Change Password")
                .setMessage("We will send a password reset link to your email: " + auth.getCurrentUser().getEmail()
                        + ". Continue?")
                .setPositiveButton("Send Email", (dialog, which) -> {
                    auth.sendPasswordResetEmail(auth.getCurrentUser().getEmail())
                            .addOnSuccessListener(aVoid -> Toast
                                    .makeText(getContext(), "Reset email sent!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast
                                    .makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPrivacySettingsDialog() {
        // Toggle Private Profile
        if (auth.getCurrentUser() == null)
            return;

        // Fetch current state first
        db.collection("users").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    boolean isPrivate = snapshot.contains("isPrivate")
                            && Boolean.TRUE.equals(snapshot.getBoolean("isPrivate"));

                    String[] options = { "Private Profile" };
                    boolean[] checkedItems = { isPrivate };

                    new android.app.AlertDialog.Builder(getContext())
                            .setTitle("Privacy Settings")
                            .setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
                                // Update Firestore immediately
                                db.collection("users").document(auth.getCurrentUser().getUid())
                                        .update("isPrivate", isChecked)
                                        .addOnSuccessListener(aVoid -> Toast
                                                .makeText(getContext(), "Privacy setting updated", Toast.LENGTH_SHORT)
                                                .show());
                            })
                            .setPositiveButton("Done", null)
                            .show();
                });
    }

    private void showHelpFaqDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Help & FAQ");

        android.widget.ScrollView scrollView = new android.widget.ScrollView(getContext());
        TextView tvContent = new TextView(getContext());
        tvContent.setPadding(40, 40, 40, 40); // px
        tvContent.setTextSize(14f);
        tvContent.setText(
                "Q: How do I donate food?\n" +
                        "A: Go to the 'Donate' tab, fill in the details, upload a photo, and submit.\n\n" +
                        "Q: How do I claim food?\n" +
                        "A: Browse the map or list, select an item, and click 'Claim'.\n\n" +
                        "Q: What is a Foodbank?\n" +
                        "A: Verified organizations that distribute food to those in need.\n\n" +
                        "Q: Is my data safe?\n" +
                        "A: Yes, we value your privacy. Check Privacy Settings to manage visibility.\n\n" +
                        "Q: How do I contact support?\n" +
                        "A: Use the 'Contact & Report Issue' button in settings.");
        scrollView.addView(tvContent);
        builder.setView(scrollView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void showContactReportDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Contact & Report");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final android.widget.EditText etSubject = new android.widget.EditText(getContext());
        etSubject.setHint("Subject");
        layout.addView(etSubject);

        final android.widget.EditText etMessage = new android.widget.EditText(getContext());
        etMessage.setHint("Describe your issue or feedback...");
        etMessage.setMinLines(3);
        layout.addView(etMessage);

        builder.setView(layout);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String subject = etSubject.getText().toString().trim();
            String message = etMessage.getText().toString().trim();

            if (!subject.isEmpty() && !message.isEmpty() && auth.getCurrentUser() != null) {
                java.util.Map<String, Object> report = new java.util.HashMap<>();
                report.put("userId", auth.getCurrentUser().getUid());
                report.put("userEmail", auth.getCurrentUser().getEmail());
                report.put("subject", subject);
                report.put("message", message);
                report.put("timestamp", System.currentTimeMillis());

                db.collection("reports").add(report)
                        .addOnSuccessListener(
                                ref -> Toast.makeText(getContext(), "Report submitted!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(
                                e -> Toast.makeText(getContext(), "Failed to submit", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadNotificationSettings(android.widget.Switch sPush, android.widget.Switch sEmail) {
        if (auth.getCurrentUser() == null)
            return;
        db.collection("users").document(auth.getCurrentUser().getUid()).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                if (snapshot.contains("pushNotificationEnabled")) {
                    sPush.setChecked(Boolean.TRUE.equals(snapshot.getBoolean("pushNotificationEnabled")));
                }
                if (snapshot.contains("emailNotificationEnabled")) {
                    sEmail.setChecked(Boolean.TRUE.equals(snapshot.getBoolean("emailNotificationEnabled")));
                }
            }
        });
    }

    private void updateNotificationSetting(String field, boolean isEnabled) {
        if (auth.getCurrentUser() == null)
            return;
        db.collection("users").document(auth.getCurrentUser().getUid())
                .update(field, isEnabled)
                .addOnFailureListener(e -> Log.e("Profile", "Failed to update setting: " + field));
    }

    private void showEditProfileDialog() {
        if (auth.getCurrentUser() == null)
            return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Edit Profile");

        // Layout Container
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density); // 20dp
        layout.setPadding(padding, padding, padding, padding);

        // 1. Change Photo Button
        Button btnChangePhoto = new Button(getContext());
        btnChangePhoto.setText("Change Profile Photo");
        // Style it slightly if possible, or keep default
        btnChangePhoto.setOnClickListener(v -> {
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
        layout.addView(btnChangePhoto);

        // Spacer
        android.widget.Space space = new android.widget.Space(getContext());
        space.setLayoutParams(new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30));
        layout.addView(space);

        // 2. Name Input
        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter your name");
        String currentName = tvUserName.getText().toString();
        input.setText(currentName);
        input.setSelection(input.getText().length());

        layout.addView(input);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                updateUserName(newName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUserName(String newName) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null)
            return;

        // 1. Update Auth Profile (DisplayName)
        com.google.firebase.auth.UserProfileChangeRequest profileUpdates = new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 2. Update Firestore
                        db.collection("users").document(user.getUid())
                                .update("name", newName)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Name updated successfully", Toast.LENGTH_SHORT)
                                            .show();
                                    tvUserName.setText(newName);
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(),
                                        "Failed to update Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(getContext(), "Failed to update Auth: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupBadges() {
        // Master List of Badges
        allBadges = new ArrayList<>();
        allBadges.add(
                new Badge("badge_10kg", "10kg Saved", "Saved 10kg of food", 10.0, R.drawable.ic_launcher_foreground));
        allBadges.add(
                new Badge("badge_50kg", "50kg Saved", "Saved 50kg of food", 50.0, R.drawable.ic_launcher_foreground));
        // Add more master badges here matching IDs used in ImpactBodyFragment

        badgeAdapter = new BadgeAdapter(allBadges);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        badgesRecyclerView.setLayoutManager(layoutManager);
        badgesRecyclerView.setAdapter(badgeAdapter);
    }

    private void uploadImageToStorage(Uri imageUri) {
        if (auth.getCurrentUser() == null)
            return;
        Toast.makeText(getContext(), "Processing image...", Toast.LENGTH_SHORT).show();

        try {
            // Database-Only approach: Convert to Base64
            String base64Image = com.example.foodaid_mad_project.Utils.ImageUtil.uriToBase64(getContext(), imageUri);

            if (base64Image != null) {
                // Prepend base64 header for easier identification, though optional for our
                // internal logic
                // Ideally keeping it raw string is smaller. But let's check size.
                // If > 1MB it will crash Firestore. ImageUtil resizes to 500px, so it should be
                // fine (~50KB).
                updateProfileUrl(base64Image);
            } else {
                Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileUrl(String base64String) {
        if (auth.getCurrentUser() == null)
            return;

        // Note: we are storing the Base64 string in the "photoUrl" field.
        // It's a misnomer now, but saves refactoring the Model.
        db.collection("users").document(auth.getCurrentUser().getUid())
                .update("photoUrl", base64String)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
                    // Load immediately
                    byte[] imageBytes = com.example.foodaid_mad_project.Utils.ImageUtil.base64ToBytes(base64String);
                    if (imageBytes.length > 0) {
                        Glide.with(this).load(imageBytes).circleCrop().into(ivProfile);
                    }
                })
                .addOnFailureListener(e -> Toast
                        .makeText(getContext(), "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null)
            return;

        // Load specific user document
        db.collection("users").document(user.getUid()).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("Profile", "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                String name = snapshot.getString("name");
                if (name == null && user.getDisplayName() != null)
                    name = user.getDisplayName();
                if (name == null)
                    name = "User";

                String photoData = snapshot.getString("photoUrl");
                // This might be a URL (old data) or Base64 (new data)

                tvUserName.setText(name);
                tvUserId.setText(user.getEmail());

                if (photoData != null && !photoData.isEmpty()) {
                    try {
                        if (photoData.startsWith("http")) {
                            // Old URL
                            Glide.with(this)
                                    .load(photoData)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_background)
                                    .circleCrop()
                                    .into(ivProfile);
                        } else {
                            // Base64
                            byte[] imageBytes = com.example.foodaid_mad_project.Utils.ImageUtil
                                    .base64ToBytes(photoData);
                            if (imageBytes.length > 0) {
                                Glide.with(this)
                                        .asBitmap()
                                        .load(imageBytes)
                                        .placeholder(R.drawable.ic_launcher_background)
                                        .error(R.drawable.ic_launcher_background)
                                        .circleCrop()
                                        .into(ivProfile);
                            }
                        }
                    } catch (Exception imageError) {
                        Log.e("ProfileFragment", "Error loading profile image", imageError);
                        Glide.with(this).load(R.drawable.ic_launcher_background).circleCrop().into(ivProfile);
                    }
                } else if (user.getPhotoUrl() != null) {
                    // Fallback to Auth Photo if Firestore empty
                    Glide.with(this)
                            .load(user.getPhotoUrl())
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .circleCrop()
                            .into(ivProfile);
                }

                // Load Badges
                List<String> badges = (List<String>) snapshot.get("earnedBadges");
                if (badges != null) {
                    userEarnedBadges.clear();
                    userEarnedBadges.addAll(badges);
                    badgeAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    // -- Adapter --
    public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

        private List<Badge> badges;

        public BadgeAdapter(List<Badge> badges) {
            this.badges = badges;
        }

        @NonNull
        @Override
        public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_badge, parent,
                    false);
            // Dynamic width logic
            int screenWidth = parent.getContext().getResources().getDisplayMetrics().widthPixels;
            float density = parent.getContext().getResources().getDisplayMetrics().density;
            int totalPadding = (int) (32 * density);
            int itemWidth = (screenWidth - totalPadding) / 3;
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.width = itemWidth;
                view.setLayoutParams(layoutParams);
            }
            return new BadgeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
            Badge badge = badges.get(position);
            holder.tvBadgeTitle.setText(badge.getName()); // Use getters from Badge class
            holder.ivBadgeImage.setImageResource(badge.getIconResId());

            if (userEarnedBadges.contains(badge.getId())) {
                // Unlocked
                holder.ivBadgeImage.setAlpha(1.0f);
                holder.tvBadgeTitle.setAlpha(1.0f);
            } else {
                // Locked
                holder.ivBadgeImage.setAlpha(0.3f);
                holder.tvBadgeTitle.setAlpha(0.5f);
            }
        }

        @Override
        public int getItemCount() {
            return badges.size();
        }

        public class BadgeViewHolder extends RecyclerView.ViewHolder {
            ImageView ivBadgeImage;
            TextView tvBadgeTitle;

            public BadgeViewHolder(@NonNull View itemView) {
                super(itemView);
                ivBadgeImage = itemView.findViewById(R.id.ivBadgeIcon);
                tvBadgeTitle = itemView.findViewById(R.id.tvBadgeName);
            }
        }
    }
}