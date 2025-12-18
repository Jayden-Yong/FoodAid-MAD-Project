package com.example.foodaid_mad_project.ProfileFragments;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Switch;
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
import com.example.foodaid_mad_project.Utils.ImageUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>ProfileFragment</h1>
 * <p>
 * Displays the user's profile information, earned badges, and settings.
 * Features:
 * <ul>
 * <li>View/Edit Profile (Name, Photo).</li>
 * <li>View Earned Badges.</li>
 * <li>Manage Settings (Notifications, Privacy, Password).</li>
 * <li>Help & Support (FAQ, Contact).</li>
 * <li>Logout functionality.</li>
 * </ul>
 * </p>
 */
public class ProfileFragment extends Fragment {

    // UI Components
    private ImageView ivProfile;
    private TextView tvUserName, tvUserId;
    private RecyclerView badgesRecyclerView;
    private BadgeAdapter badgeAdapter;

    // Data
    private List<Badge> allBadges; // Master list definition
    private List<String> userEarnedBadges = new ArrayList<>();

    // Firebase & Auth
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private ListenerRegistration userListener;

    // Image Picker
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Google Sign-In (for logout)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Initialize Image Picker
        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                uploadImageToFirestore(uri);
            } else {
                Log.d("Profile", "No media selected");
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupBadges();
        setupListeners(view);
        loadUserData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }

    // ============================================================================================
    // INITIALIZATION & SETUP
    // ============================================================================================

    private void initializeViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserId = view.findViewById(R.id.tvUserId);
        ivProfile = view.findViewById(R.id.ivProfile);
        badgesRecyclerView = view.findViewById(R.id.badgesContainer);
    }

    private void setupListeners(View view) {
        // Profile Image Click
        View profileImageContainer = view.findViewById(R.id.profileImageContainer);
        profileImageContainer.setOnClickListener(v -> launchImagePicker());

        // Logout
        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> performLogout());

        // Edit Profile
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // View All Badges
        TextView tvViewAllBadges = view.findViewById(R.id.tvViewAllBadges);
        tvViewAllBadges.setOnClickListener(v -> Toast.makeText(getContext(), "Coming Soon", Toast.LENGTH_SHORT).show());

        // Settings Buttons
        view.findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());
        view.findViewById(R.id.btnPrivacySettings).setOnClickListener(v -> showPrivacySettingsDialog());
        view.findViewById(R.id.btnHelpFaq).setOnClickListener(v -> showHelpFaqDialog());
        view.findViewById(R.id.btnContactReport).setOnClickListener(v -> showContactReportDialog());

        // Toggles
        Switch switchPush = view.findViewById(R.id.switchPush);
        Switch switchEmail = view.findViewById(R.id.switchEmail);
        loadNotificationSettings(switchPush, switchEmail);

        switchPush.setOnCheckedChangeListener(
                (buttonView, isChecked) -> updateNotificationSetting("pushNotificationEnabled", isChecked));
        switchEmail.setOnCheckedChangeListener(
                (buttonView, isChecked) -> updateNotificationSetting("emailNotificationEnabled", isChecked));
    }

    private void setupBadges() {
        allBadges = new ArrayList<>();
        allBadges.add(
                new Badge("badge_10kg", "10kg Saved", "Saved 10kg of food", 10.0, R.drawable.ic_launcher_foreground));
        allBadges.add(
                new Badge("badge_50kg", "50kg Saved", "Saved 50kg of food", 50.0, R.drawable.ic_launcher_foreground));
        allBadges.add(new Badge("badge_100kg", "100kg Saved", "Saved 100kg of food", 100.0,
                R.drawable.ic_launcher_foreground));

        badgeAdapter = new BadgeAdapter(new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        badgesRecyclerView.setLayoutManager(layoutManager);
        badgesRecyclerView.setAdapter(badgeAdapter);
    }

    // ============================================================================================
    // DATA LOADING
    // ============================================================================================

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null)
            return;

        if (userListener != null)
            userListener.remove();
        userListener = db.collection("users").document(user.getUid())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("Profile", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists() && isAdded()) {
                        // 1. Name
                        String name = snapshot.getString("name");
                        if (name == null)
                            name = user.getDisplayName();
                        if (name == null)
                            name = "User";
                        tvUserName.setText(name);
                        tvUserId.setText(user.getEmail());

                        // 2. Photo
                        String photoData = snapshot.getString("photoUrl");
                        loadProfileImage(photoData, user.getPhotoUrl());

                        // 3. Badges
                        List<String> earnedIds = (List<String>) snapshot.get("earnedBadges");
                        updateBadgesList(earnedIds);
                    }
                });
    }

    private void loadProfileImage(String firestorePhotoData, Uri authPhotoUrl) {
        try {
            if (firestorePhotoData != null && !firestorePhotoData.isEmpty()) {
                if (firestorePhotoData.startsWith("http")) {
                    Glide.with(this).load(firestorePhotoData).circleCrop().into(ivProfile);
                } else {
                    byte[] imageBytes = ImageUtil.base64ToBytes(firestorePhotoData);
                    Glide.with(this).asBitmap().load(imageBytes).circleCrop()
                            .placeholder(R.drawable.ic_launcher_background).into(ivProfile);
                }
            } else if (authPhotoUrl != null) {
                Glide.with(this).load(authPhotoUrl).circleCrop().placeholder(R.drawable.ic_launcher_background)
                        .into(ivProfile);
            } else {
                Glide.with(this).load(R.drawable.ic_launcher_background).circleCrop().into(ivProfile);
            }
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error loading image", e);
        }
    }

    private void updateBadgesList(List<String> earnedIds) {
        if (earnedIds != null) {
            userEarnedBadges.clear();
            userEarnedBadges.addAll(earnedIds);

            List<Badge> displayedBadges = new ArrayList<>();
            for (Badge b : allBadges) {
                if (userEarnedBadges.contains(b.getId())) {
                    displayedBadges.add(b);
                }
            }
            badgeAdapter.updateBadges(displayedBadges);
        } else {
            badgeAdapter.updateBadges(new ArrayList<>());
        }
    }

    private void loadNotificationSettings(Switch sPush, Switch sEmail) {
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

    // ============================================================================================
    // ACTIONS
    // ============================================================================================

    private void performLogout() {
        auth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void launchImagePicker() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void uploadImageToFirestore(Uri imageUri) {
        if (auth.getCurrentUser() == null)
            return;
        Toast.makeText(getContext(), "Processing image...", Toast.LENGTH_SHORT).show();

        try {
            String base64Image = ImageUtil.uriToBase64(getContext(), imageUri);
            if (base64Image != null) {
                db.collection("users").document(auth.getCurrentUser().getUid())
                        .update("photoUrl", base64Image)
                        .addOnSuccessListener(
                                aVoid -> Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(
                                e -> Toast.makeText(getContext(), "Update Failed", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Profile", "Image Upload Error", e);
            Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNotificationSetting(String field, boolean isEnabled) {
        if (auth.getCurrentUser() == null)
            return;
        db.collection("users").document(auth.getCurrentUser().getUid())
                .update(field, isEnabled)
                .addOnFailureListener(e -> Log.e("Profile", "Failed to update setting: " + field));
    }

    // ============================================================================================
    // DIALOGS
    // ============================================================================================

    private void showEditProfileDialog() {
        if (auth.getCurrentUser() == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Profile");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        // Change Photo Button
        Button btnChangePhoto = new Button(getContext());
        btnChangePhoto.setText("Change Profile Photo");
        btnChangePhoto.setOnClickListener(v -> launchImagePicker());
        layout.addView(btnChangePhoto);

        // Spacer
        Space space = new Space(getContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30));
        layout.addView(space);

        // Name Input
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter your name");
        input.setText(tvUserName.getText().toString());
        layout.addView(input);

        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty())
                updateUserName(newName);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateUserName(String newName) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null)
            return;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                db.collection("users").document(user.getUid())
                        .update("name", newName)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Name updated", Toast.LENGTH_SHORT).show();
                            tvUserName.setText(newName); // Immediate UI update
                        });
            }
        });
    }

    private void showChangePasswordDialog() {
        if (auth.getCurrentUser() == null || auth.getCurrentUser().getEmail() == null)
            return;
        new AlertDialog.Builder(getContext())
                .setTitle("Change Password")
                .setMessage("Send password reset email to " + auth.getCurrentUser().getEmail() + "?")
                .setPositiveButton("Send Email", (dialog, which) -> {
                    auth.sendPasswordResetEmail(auth.getCurrentUser().getEmail())
                            .addOnSuccessListener(
                                    aVoid -> Toast.makeText(getContext(), "Email sent!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast
                                    .makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPrivacySettingsDialog() {
        if (auth.getCurrentUser() == null)
            return;
        db.collection("users").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    boolean isPrivate = Boolean.TRUE.equals(snapshot.getBoolean("isPrivate"));
                    new AlertDialog.Builder(getContext())
                            .setTitle("Privacy Settings")
                            .setMultiChoiceItems(new String[] { "Private Profile" }, new boolean[] { isPrivate },
                                    (dialog, which, isChecked) -> {
                                        db.collection("users").document(auth.getCurrentUser().getUid())
                                                .update("isPrivate", isChecked);
                                    })
                            .setPositiveButton("Done", null)
                            .show();
                });
    }

    private void showHelpFaqDialog() {
        ScrollView scrollView = new ScrollView(getContext());
        TextView tvContent = new TextView(getContext());
        tvContent.setPadding(40, 40, 40, 40);
        tvContent.setText(
                "Q: How do I donate?\nA: Use the donate tab.\n\nQ: How do I claim?\nA: Use the map or list selection.\n\nQ: Contact Support?\nA: Use the 'Contact' button.");
        scrollView.addView(tvContent);

        new AlertDialog.Builder(getContext())
                .setTitle("Help & FAQ")
                .setView(scrollView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showContactReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Contact & Report");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText etSubject = new EditText(getContext());
        etSubject.setHint("Subject");
        layout.addView(etSubject);

        final EditText etMessage = new EditText(getContext());
        etMessage.setHint("Message...");
        etMessage.setMinLines(3);
        layout.addView(etMessage);

        builder.setView(layout);
        builder.setPositiveButton("Submit", (dialog, which) -> {
            String subject = etSubject.getText().toString().trim();
            String message = etMessage.getText().toString().trim();
            if (!subject.isEmpty() && !message.isEmpty() && auth.getCurrentUser() != null) {
                Map<String, Object> report = new HashMap<>();
                report.put("userId", auth.getCurrentUser().getUid());
                report.put("userEmail", auth.getCurrentUser().getEmail());
                report.put("subject", subject);
                report.put("message", message);
                report.put("timestamp", System.currentTimeMillis());

                db.collection("reports").add(report)
                        .addOnSuccessListener(
                                ref -> Toast.makeText(getContext(), "Report submitted!", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // ============================================================================================
    // ADAPTER
    // ============================================================================================

    public static class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {
        private List<Badge> badges;

        public BadgeAdapter(List<Badge> badges) {
            this.badges = badges;
        }

        @NonNull
        @Override
        public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_badge, parent,
                    false);
            // Dynamic width logic for 3 columns
            int screenWidth = parent.getContext().getResources().getDisplayMetrics().widthPixels;
            int itemWidth = (screenWidth - (int) (32 * parent.getContext().getResources().getDisplayMetrics().density))
                    / 3;
            if (view.getLayoutParams() != null)
                view.getLayoutParams().width = itemWidth;
            return new BadgeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
            Badge badge = badges.get(position);
            holder.tvBadgeTitle.setText(badge.getName());
            holder.ivBadgeImage.setImageResource(badge.getIconResId());
        }

        @Override
        public int getItemCount() {
            return badges.size();
        }

        public void updateBadges(List<Badge> newBadges) {
            this.badges = newBadges;
            notifyDataSetChanged();
        }

        static class BadgeViewHolder extends RecyclerView.ViewHolder {
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