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

        // Edit Profile (Placeholder or simple toast for now)
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(
                v -> Toast.makeText(getContext(), "Profile editing coming soon!", Toast.LENGTH_SHORT).show());
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

                String photoUrl = snapshot.getString("photoUrl");
                if (photoUrl == null && user.getPhotoUrl() != null)
                    photoUrl = user.getPhotoUrl().toString();

                tvUserName.setText(name);
                tvUserId.setText(user.getEmail()); // Or matric number if stored

                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(this).load(photoUrl).circleCrop().into(ivProfile);
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

    private void uploadImageToStorage(Uri imageUri) {
        if (auth.getCurrentUser() == null)
            return;
        Toast.makeText(getContext(), "Uploading...", Toast.LENGTH_SHORT).show();

        String uid = auth.getCurrentUser().getUid();
        StorageReference ref = storage.getReference().child("profile_images/" + uid + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateProfileUrl(uri.toString());
                }))
                .addOnFailureListener(e -> Toast
                        .makeText(getContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateProfileUrl(String url) {
        if (auth.getCurrentUser() == null)
            return;
        db.collection("users").document(auth.getCurrentUser().getUid())
                .update("photoUrl", url)
                .addOnSuccessListener(
                        aVoid -> Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show());
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