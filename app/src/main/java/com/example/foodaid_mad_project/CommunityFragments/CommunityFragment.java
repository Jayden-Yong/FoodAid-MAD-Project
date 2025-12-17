package com.example.foodaid_mad_project.CommunityFragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.firebase.firestore.FieldValue;
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

public class CommunityFragment extends Fragment {

    private RecyclerView rvPosts;
    private CommunityPostAdapter adapter;
    private List<CommunityPost> postList;
    private FirebaseFirestore db;
    private ListenerRegistration postListener;

    // Image Picking for New Post
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;
    private ImageView ivDialogPreview; // Ref to dialog's ImageView

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        rvPosts = view.findViewById(R.id.rvPosts);
        postList = new ArrayList<>();
        adapter = new CommunityPostAdapter(postList);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setAdapter(adapter);
        rvPosts.setNestedScrollingEnabled(false);

        // Listen for Realtime Updates from Firestore
        setupRealtimeUpdates();

        // Handle "Create Post" Button
        ImageButton btnCreatePost = view.findViewById(R.id.btnCreatePost);
        btnCreatePost.setOnClickListener(v -> createNewPost());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (postListener != null) {
            postListener.remove();
        }
    }

    private void setupRealtimeUpdates() {
        // Query posts, order by timestamp desc
        Query query = db.collection("community_posts").orderBy("timestamp", Query.Direction.DESCENDING);

        postListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                return;
            }

            if (snapshots != null) {
                postList.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    CommunityPost post = doc.toObject(CommunityPost.class);
                    // Manually set ID if not part of object serialization (though it usually is if
                    // getters/setters align)
                    post.setPostId(doc.getId());
                    postList.add(post);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private String calculateTimeAgo(long timestamp) {
        long duration = new Date().getTime() - timestamp;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long days = TimeUnit.MILLISECONDS.toDays(duration);

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " mins ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            return days + " days ago";
        }
    }

    private void createNewPost() {
        // Check Auth
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please login to post", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_post, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Transparent bg
        }

        EditText etContent = dialogView.findViewById(R.id.etPostContent);
        ivDialogPreview = dialogView.findViewById(R.id.ivPostImage);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelPost);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitPost);

        selectedImageUri = null; // Reset selection

        ivDialogPreview.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            if (content.isEmpty() && selectedImageUri == null) {
                Toast.makeText(getContext(), "Please enter content or allow photo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button to prevent double-click
            btnSubmit.setEnabled(false);
            btnSubmit.setText("Posting...");

            submitPostToFirestore(content, dialog, btnSubmit);
        });

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
                // Compress Image using ImageUtil (max dim ~300, quality 40)
                finalImageBase64 = ImageUtil.uriToBase64(getContext(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(true);
                btnSubmit.setText(getString(R.string.Post));
                return;
            }
        }

        DocumentReference newPostRef = db.collection("community_posts").document();
        CommunityPost newPost = new CommunityPost(
                newPostRef.getId(),
                uid,
                userName,
                null, // User image placeholder
                content,
                finalImageBase64,
                "Story",
                System.currentTimeMillis());

        newPostRef.set(newPost)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Post created!", Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    // Realtime listener will auto-update UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText(getString(R.string.Post));
                });
    }

    // --- Adapter ---
    public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder> {

        private List<CommunityPost> posts;
        private String currentUserId;

        public CommunityPostAdapter(List<CommunityPost> posts) {
            this.posts = posts;
            currentUserId = FirebaseAuth.getInstance().getUid();
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

            holder.tvUserAction.setText(getString(R.string.User_Action_Post, post.getUserName())); // Requires string
                                                                                                   // resource update or
                                                                                                   // logic
            // Fallback if string resource not flexible: just name + type
            // holder.tvUserAction.setText(post.getUserName() + " shared a " +
            // post.getPostType());

            holder.tvTimeAgo.setText(calculateTimeAgo(post.getTimestamp()));
            holder.tvPostContent.setText(post.getContent());

            // Post Image
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
                    holder.ivPostImage.setVisibility(View.GONE);
                }
            } else {
                holder.ivPostImage.setVisibility(View.GONE);
            }

            // User Avatar (Placeholder)
            holder.civUserAvatar.setImageResource(R.drawable.ic_user_male); // Ensure this drawable exists
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        public class PostViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserAction, tvTimeAgo, tvPostContent;
            ImageView ivPostImage, civUserAvatar;

            public PostViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUserAction = itemView.findViewById(R.id.tvUserAction);
                tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
                tvPostContent = itemView.findViewById(R.id.tvPostContent);
                // Removed like/comment views lookup

                // Assuming fragment_community_post layout has an ImageView for the post image
                // now?
                // The original code didn't have one. We need to add one to the XML or
                // dynamically?
                // Or maybe reuse a view.
                // Let's assume user wants to SEE the image. We need to FIND it.
                // If it doesn't exist in XML, we might crash if we try to set it.
                // I will update XML in next step. For now, let's look for an ID or just expect
                // one.
                ivPostImage = itemView.findViewById(R.id.ivPostContentImage); // New ID

                // Avatar
                civUserAvatar = itemView.findViewById(R.id.civUserAvatar);
            }
        }
    }
}