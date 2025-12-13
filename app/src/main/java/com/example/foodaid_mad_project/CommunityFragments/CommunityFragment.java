package com.example.foodaid_mad_project.CommunityFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.R;

import java.text.BreakIterator;
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
    // TODO: Database

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO: Initialize Firebase Firestore

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

    private void setupRealtimeUpdates() {
        postList.clear();
        //TODO: Clear postList, and then get Post data from database and add to postList based on timestamps
        //TODO: Change timestamp to post time using calculateTimeAgo()

        //sample post
        CommunityPost post = new CommunityPost(
                //to be replaced by real post data from database
                "1",
                "FoodAid User",
                "story",
                "Just now",
                "I just shared a donation! Together we can make a difference.",
                3,
                1
        );
        postList.add(post);
    }

    private String calculateTimeAgo(Date date) {
        if (date == null) return "Just now";

        long duration = new Date().getTime() - date.getTime();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long days = TimeUnit.MILLISECONDS.toDays(duration);

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + "minutes ago";
        } else if (hours < 24) {
            return hours + "hours ago";
        } else {
            return days + "days ago";
        }
    }

    private void createNewPost() {
        // Create Mock Data Map
        Map<String, Object> newPost = new HashMap<>();
        newPost.put("userName", "AAAA");
        newPost.put("postType", "tip");
        newPost.put("content", "You can share your tips here!");
        newPost.put("likeCount", 0);
        newPost.put("commentCount", 0);
        newPost.put("timestamp", new Date());

        //TODO: Add newPost to Firestore, then call setupRealtimeUpdates to update the posts


        setupRealtimeUpdates();
    }

    public static class CommunityPost {
        String postId;
        String userName;
        String postType;
        String timeAgo;
        String content;
        long likeCount;
        long commentCount;

        public CommunityPost(String postId, String userName, String postType, String timeAgo, String content, long likeCount, long commentCount) {
            this.postId = postId;
            this.userName = userName;
            this.postType = postType;
            this.timeAgo = timeAgo;
            this.content = content;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
        }
    }

    // --- Adapter ---
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

            holder.tvUserAction.setText(getString(R.string.Post_Title, post.userName, post.postType));
            holder.tvTimeAgo.setText(post.timeAgo);
            holder.tvPostContent.setText(post.content);
            holder.tvLikeCount.setText(String.valueOf(post.likeCount));
            holder.tvCommentCount.setText(String.valueOf(post.commentCount));

            // Handle Like Button Click (Checkbox)
            holder.cbLike.setOnCheckedChangeListener(null);

            //TODO: check if the current user ID is in a "likes" array and set either true or false
            holder.cbLike.setChecked(false);

            holder.cbLike.setOnClickListener(v -> {
                //TODO: Increment like count in Firestore atomically and update using setupRealtimeUpdates()
                //Sample increase like count(local only)
                TextView tvLikeCount = getView().findViewById(R.id.tvLikeCount);
                tvLikeCount.setText(String.valueOf(post.likeCount + 1));


                setupRealtimeUpdates();
            });
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        public class PostViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserAction, tvTimeAgo, tvPostContent, tvLikeCount, tvCommentCount;
            CheckBox cbLike;

            public PostViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUserAction = itemView.findViewById(R.id.tvUserAction);
                tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
                tvPostContent = itemView.findViewById(R.id.tvPostContent);
                tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
                tvCommentCount = itemView.findViewById(R.id.tvCommentCount);

                // Find the Like CheckBox
                View llLikes = itemView.findViewById(R.id.llLikes);
                if (llLikes instanceof ViewGroup) {
                    for(int i=0; i<((ViewGroup)llLikes).getChildCount(); i++) {
                        View child = ((ViewGroup)llLikes).getChildAt(i);
                        if(child instanceof CheckBox) {
                            cbLike = (CheckBox) child;
                            break;
                        }
                    }
                }
            }
        }
    }
}