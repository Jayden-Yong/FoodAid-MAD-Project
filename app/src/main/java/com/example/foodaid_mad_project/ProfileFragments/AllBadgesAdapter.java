package com.example.foodaid_mad_project.ProfileFragments;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.Model.Badge;
import com.example.foodaid_mad_project.R;

import java.util.ArrayList;
import java.util.List;

/**
 * AllBadgesAdapter
 *
 * This adapter manages the display of badges in a RecyclerView grid.
 * It handles the visual distinction between earned and unearned badges by applying
 * a grayscale filter to unearned items.
 */
public class AllBadgesAdapter extends RecyclerView.Adapter<AllBadgesAdapter.BadgeViewHolder> {

    private List<Badge> allBadges;
    private List<String> earnedBadgeIds;

    public AllBadgesAdapter(List<Badge> allBadges, List<String> earnedBadgeIds) {
        this.allBadges = allBadges;
        this.earnedBadgeIds = earnedBadgeIds != null ? earnedBadgeIds : new ArrayList<>();
    }

    public void updateData(List<Badge> allBadges, List<String> earnedBadgeIds) {
        this.allBadges = allBadges;
        this.earnedBadgeIds = earnedBadgeIds != null ? earnedBadgeIds : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = allBadges.get(position);

        holder.tvName.setText(badge.getName());
        holder.tvDesc.setText(badge.getDescription());
        holder.ivIcon.setImageResource(badge.getIconResId());

        boolean isEarned = earnedBadgeIds.contains(badge.getId());

        if (isEarned) {
            // Full color, full opacity
            holder.ivIcon.clearColorFilter();
            holder.ivIcon.setAlpha(1.0f);
            holder.tvName.setTextColor(Color.BLACK);
            holder.tvDesc.setTextColor(Color.DKGRAY);
        } else {
            // Grayscale, lower opacity
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.ivIcon.setColorFilter(filter);
            holder.ivIcon.setAlpha(0.5f);
            holder.tvName.setTextColor(Color.GRAY);
            holder.tvDesc.setTextColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return allBadges.size();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvDesc;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivBadgeIcon);
            tvName = itemView.findViewById(R.id.tvBadgeName);
            tvDesc = itemView.findViewById(R.id.tvBadgeDesc);
        }
    }
}
