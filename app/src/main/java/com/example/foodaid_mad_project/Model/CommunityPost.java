package com.example.foodaid_mad_project.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * CommunityPost
 *
 * Represents a user-generated post in the Community feed.
 * Includes text content, optional images, and interaction data (likes/comments).
 */
public class CommunityPost {
    private String postId;
    private String userId;
    private String userName;
    private String userImage; // Optional Base64 or URL
    private String content;
    private String postImage; // Base64 string of compressed image
    private String postType; // "Story", "Tip", etc.
    private long timestamp;
    private List<String> likes; // List of User IDs who liked
    private int commentCount;

    // Empty constructor for Firestore deserialization
    public CommunityPost() {
        this.likes = new ArrayList<>();
    }

    public CommunityPost(String postId, String userId, String userName, String userImage,
            String content, String postImage, String postType, long timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;
        this.content = content;
        this.postImage = postImage;
        this.postType = postType;
        this.timestamp = timestamp;
        this.likes = new ArrayList<>();
        this.commentCount = 0;
    }

    // ============================================================================================
    // GETTERS & SETTERS
    // ============================================================================================

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}
