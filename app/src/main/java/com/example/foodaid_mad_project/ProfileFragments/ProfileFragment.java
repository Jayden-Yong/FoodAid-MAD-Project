package com.example.foodaid_mad_project.ProfileFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.AuthActivity; // Ensure this matches your package
import com.example.foodaid_mad_project.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private RecyclerView badgesRecyclerView;
    private BadgeAdapter badgeAdapter;
    private List<Badge> badgeList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvUserName = view.findViewById(R.id.tvUserName);
        tvUserName.setText(getString(R.string.String, "John Doe"));
        TextView tvUserId = view.findViewById(R.id.tvUserId);
        tvUserId.setText(getString(R.string.Matric_No, 23000000));


        badgesRecyclerView = view.findViewById(R.id.badgesContainer);

        // Initialize Mock Data
        badgeList = new ArrayList<>();
        badgeList.add(new Badge("First\nDonation", R.drawable.ic_launcher_foreground)); // Replace with actual badge drawables
        badgeList.add(new Badge("50kg\nFood Saved", R.drawable.ic_launcher_foreground));
        badgeList.add(new Badge("Weekly\nContributor", R.drawable.ic_launcher_foreground));
        badgeList.add(new Badge("Community\nStar", R.drawable.ic_launcher_foreground));

        // Initialize Adapter
        badgeAdapter = new BadgeAdapter(badgeList);

        // Set Layout Manager to HORIZONTAL
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        badgesRecyclerView.setLayoutManager(layoutManager);
        badgesRecyclerView.setAdapter(badgeAdapter);

        // 2. Setup Logout Button
        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            // Redirect to AuthActivity
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit Profile Clicked", Toast.LENGTH_SHORT).show();
            // TODO: To Edit Profile
        });
    }

    public static class Badge {
        String title;
        int imageResId;

        public Badge(String title, int imageResId) {
            this.title = title;
            this.imageResId = imageResId;
        }
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_badge, parent, false);

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
            holder.tvBadgeTitle.setText(badge.title);
            holder.ivBadgeImage.setImageResource(badge.imageResId);
        }

        @Override
        public int getItemCount() {
            if (badges.size() > 3) {
                return 3;
            }
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