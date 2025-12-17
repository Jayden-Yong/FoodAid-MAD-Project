package com.example.foodaid_mad_project.HomeFragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.Model.Notification;
import com.example.foodaid_mad_project.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnItemClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        ImageView ivUnreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
            ivUnreadDot = itemView.findViewById(R.id.ivUnreadDot);
        }

        public void bind(Notification notification, OnItemClickListener listener) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
            tvTime.setText(sdf.format(new Date(notification.getTimestamp())));

            if (notification.isRead()) {
                ivUnreadDot.setVisibility(View.GONE);
                tvTitle.setAlpha(0.6f);
            } else {
                ivUnreadDot.setVisibility(View.VISIBLE);
                tvTitle.setAlpha(1.0f);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(notification));
        }
    }
}
