package com.example.foodaid_mad_project;

import com.example.foodaid_mad_project.AuthFragments.User;

/**
 * <h1>UserManager</h1>
 * <p>
 * Singleton class for managing the current user's session data.
 * Useful for accessing user details (like name, email, earned badges) globally
 * without refetching from Firestore every time.
 * </p>
 */
public class UserManager {
    private static UserManager instance;
    private User currentUser;

    private UserManager() {
    }

    /**
     * @return The singleton instance of UserManager.
     */
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * Sets the current active user.
     * 
     * @param user User object fetched from Firestore.
     */
    public void setUser(User user) {
        this.currentUser = user;
    }

    /**
     * @return The current active user, or null if not set.
     */
    public User getUser() {
        return currentUser;
    }
}
