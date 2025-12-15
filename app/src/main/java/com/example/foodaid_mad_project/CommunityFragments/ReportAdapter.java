package com.example.foodaid_mad_project.CommunityFragments;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ReportAdapter extends FirestoreRecyclerAdapter<Report, ReportAdapter.ReportViewHolder> {

    public ReportAdapter(@NonNull FirestoreRecyclerOptions<Report> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ReportViewHolder holder, int position, @NonNull Report model) {
        holder.tvIssueType.setText(model.getIssueType());
        holder.tvDescription.setText(model.getDescription());
        holder.tvStatus.setText(model.getStatus() != null ? model.getStatus() : "Pending");

        // Format Timestamp
        if (model.getTimestamp() != null) {
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    model.getTimestamp().toDate().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS);
            holder.tvTimestamp.setText(timeAgo);
        } else {
            holder.tvTimestamp.setText("Just now");
        }

        // Set Icon based on Issue Type (Simple logic)
        switch (model.getIssueType()) {
            case "Foodbank Closed":
                holder.imgIssueIcon.setImageResource(R.drawable.ic_notification);
                break;
            case "Incorrect Location":
                holder.imgIssueIcon.setImageResource(R.drawable.ic_map_view);
                break;
            case "Inventory Empty":
                holder.imgIssueIcon.setImageResource(R.drawable.ic_new_donation);
                break;
            default:
                holder.imgIssueIcon.setImageResource(R.drawable.ic_notification);
                break;
        }
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_report, parent, false);
        return new ReportViewHolder(view);
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvIssueType, tvDescription, tvTimestamp, tvStatus;
        ImageView imgIssueIcon;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIssueType = itemView.findViewById(R.id.tvIssueType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgIssueIcon = itemView.findViewById(R.id.imgIssueIcon);
        }
    }
}
