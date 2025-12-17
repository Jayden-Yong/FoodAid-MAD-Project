package com.example.foodaid_mad_project.HomeFragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
<<<<<<< HEAD

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
=======
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodaid_mad_project.Model.NotificationItem;
import com.example.foodaid_mad_project.R;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<NotificationItem> notificationList;

    public NotificationAdapter(List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
    }

    public void updateList(List<NotificationItem> newList) {
        this.notificationList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (notificationList.get(position).isHeader()) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
>>>>>>> b2f77a9f
    }

    @NonNull
    @Override
<<<<<<< HEAD
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, listener);
=======
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_notification_item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_item_notification, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvHeader.setText(item.getTitle());
        } else if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            itemHolder.tvTitle.setText(item.getTitle());
            itemHolder.tvDesc.setText(item.getDescription());
            itemHolder.tvTime.setText(item.getTimeString());

            // Icon Logic
            if (item.getType() != null) {
                switch (item.getType()) {
                    case "Request":
                    case "Donation":
                        itemHolder.ivIcon.setImageResource(R.drawable.ic_new_donation);
                        break;
                    case "Pickup":
                        itemHolder.ivIcon.setImageResource(R.drawable.ic_qr);
                        break;
                    case "Community":
                        itemHolder.ivIcon.setImageResource(R.drawable.ic_community_selected);
                        break;
                    case "Impact":
                        itemHolder.ivIcon.setImageResource(R.drawable.ic_notification); // Use default if leaf not available
                        break;
                    default:
                        itemHolder.ivIcon.setImageResource(R.drawable.ic_notification);
                }
            }

            if (!item.isRead()) {
                itemHolder.unreadDot.setVisibility(View.VISIBLE);
            } else {
                itemHolder.unreadDot.setVisibility(View.GONE);
            }
        }
>>>>>>> b2f77a9f
    }

    @Override
    public int getItemCount() {
<<<<<<< HEAD
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
=======
        return notificationList.size();
    }

    // View Holders
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tv_header_title);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvTime;
        ImageView ivIcon;
        View unreadDot;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notify_title);
            tvDesc = itemView.findViewById(R.id.tv_notify_desc);
            tvTime = itemView.findViewById(R.id.tv_notify_time);
            ivIcon = itemView.findViewById(R.id.iv_notify_icon);
            unreadDot = itemView.findViewById(R.id.view_unread_dot);
        }
    }
}
>>>>>>> b2f77a9f
