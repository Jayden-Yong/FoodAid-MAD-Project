package com.example.foodaid_mad_project.CommunityFragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodaid_mad_project.Model.CommunityPost;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.Utils.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <h1>CommunityFragment</h1>
 * <p>
 * Displays a social feed of community posts.
 * Users can:
 * <ul>
 * <li>View posts from other users (stories, updates).</li>
 * <li>Create new posts with optional image attachments.</li>
 * <li>See real-time updates as new posts are added.</li>
 * </ul>
 * </p>
 */
public class CommunityFragment extends Fragment {

    // UI Elements
    private RecyclerView rvPosts;
    private CommunityPostAdapter adapter;
    private List<CommunityPost> postList;

    // Firebase
    private FirebaseFirestore db;
    private ListenerRegistration postListener;

    // Post Creation
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;
    private ImageView ivDialogPreview; // Reference to preview in the dialog

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Image Picker
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                if (ivDialogPreview != null) {
                    ivDialogPreview.setImageURI(uri);
                    ivDialogPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // 1. Setup RecyclerView
        rvPosts = view.findViewById(R.id.rvPosts);
        postList = new ArrayList<>();
        adapter = new CommunityPostAdapter(postList);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setAdapter(adapter);
        rvPosts.setNestedScrollingEnabled(false);

        // 2. Initialize Listeners
        setupRealtimeUpdates();

        // 3. Post Creation Logic
        ImageButton btnCreatePost = view.findViewById(R.id.btnCreatePost);
        if (btnCreatePost != null) {
            btnCreatePost.setOnClickListener(v -> createNewPost());
        }
    }

    private void setupRealtimeUpdates() {
        Query query = db.collection("community_posts").orderBy("timestamp", Query.Direction.DESCENDING);

        postListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null)
                return;

            if (snapshots != null) {
                postList.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    CommunityPost post = doc.toObject(CommunityPost.class);
                    post.setPostId(doc.getId());
                    postList.add(post);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void createNewPost() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please login to post", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_post, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etContent = dialogView.findViewById(R.id.etPostContent);
        ivDialogPreview = dialogView.findViewById(R.id.ivPostImage);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelPost);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitPost);

        selectedImageUri = null; // Reset

        if (ivDialogPreview != null) {
            ivDialogPreview.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String content = etContent.getText().toString().trim();
                if (content.isEmpty() && selectedImageUri == null) {
                    Toast.makeText(getContext(), "Please enter content or allow photo", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isNetworkAvailable()) {
                    Toast.makeText(getContext(), "No internet connection.", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnSubmit.setEnabled(false);
                btnSubmit.setText("Posting...");

                submitPostToFirestore(content, dialog, btnSubmit);
            });
        }

        dialog.show();
    }

    private void submitPostToFirestore(String content, AlertDialog dialog, Button btnSubmit) {
        String uid = FirebaseAuth.getInstance().getUid();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (userName == null || userName.isEmpty())
            userName = "Anonymous";

        String finalImageBase64 = null;
        if (selectedImageUri != null) {
            try {
                finalImageBase64 = ImageUtil.uriToBase64(getContext(), selectedImageUri);
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Post");
                return;
            }
        }

        DocumentReference newPostRef = db.collection("community_posts").document();
        CommunityPost newPost = new CommunityPost(
                newPostRef.getId(),
                uid,
                userName,
                null,
                content,
                finalImageBase64,
                "Story",
                System.currentTimeMillis());

        newPostRef.set(newPost)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Post created!", Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();

                    // Create Notification for GLOBAL feed
                    createGlobalNotification(newPost);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Post");
                });
    }

    private void createGlobalNotification(CommunityPost post) {
        DocumentReference notifRef = db.collection("notifications").document();
        Map<String, Object> data = new HashMap<>();
        data.put("id", notifRef.getId());
        data.put("title", "New Community Post");
        data.put("message", post.getUserName() + " shared a " + post.getPostType());
        data.put("type", "Community");
        data.put("relatedId", post.getPostId());
        data.put("timestamp", System.currentTimeMillis());
        data.put("isRead", false);
        data.put("userId", "ALL"); // Broadcast to All

        notifRef.set(data).addOnFailureListener(e -> {
            // Log failure strictly, don't interrupt user flow
        });
    }

    private boolean isNetworkAvailable() {
        if (getContext() == null)
            return false;
        ConnectivityManager cm = (ConnectivityManager) getContext()
                .getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    private String calculateTimeAgo(long timestamp) {
        long duration = new Date().getTime() - timestamp;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long days = TimeUnit.MILLISECONDS.toDays(duration);

        if (seconds < 60)
            return "Just now";
        if (minutes < 60)
            return minutes + " mins ago";
        if (hours < 24)
            return hours + " hours ago";
        return days + " days ago";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (postListener != null)
            postListener.remove();
    }

    // ============================================================================================
    // ADAPTER
    // ============================================================================================

    public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder> {
        private List<CommunityPost> posts;

        public CommunityPostAdapter(List<CommunityPost> posts) {
            this.posts = posts;
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_community_post, parent, false);
            return new PostViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
            CommunityPost post = posts.get(position);

            holder.tvUserAction.setText(getString(R.string.User_Action_Post, post.getUserName()));
            holder.tvTimeAgo.setText(calculateTimeAgo(post.getTimestamp()));
            holder.tvPostContent.setText(post.getContent());

            if (post.getPostImage() != null && !post.getPostImage().isEmpty()) {
                holder.ivPostImage.setVisibility(View.VISIBLE);
                try {
                    byte[] imageBytes = ImageUtil.base64ToBytes(post.getPostImage());
                    Glide.with(holder.itemView.getContext())
                            .load(imageBytes)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .into(holder.ivPostImage);
                } catch (Exception e) {
                    holder.ivPostImage.setImageResource(R.drawable.ic_launcher_background);
                }
            } else {
                holder.ivPostImage.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserAction, tvTimeAgo, tvPostContent;
            ImageView ivPostImage, civUserAvatar;

            public PostViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUserAction = itemView.findViewById(R.id.tvUserAction);
                tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
                tvPostContent = itemView.findViewById(R.id.tvPostContent);
                ivPostImage = itemView.findViewById(R.id.ivPostContentImage); // Ensure XML matches this
                civUserAvatar = itemView.findViewById(R.id.civUserAvatar);
            }
        }
    }
}