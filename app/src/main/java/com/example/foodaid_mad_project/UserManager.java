package com.example.foodaid_mad_project;

import com.example.foodaid_mad_project.AuthFragments.User;

public class UserManager {
    private static UserManager instance;
    private User currentUser;

    private UserManager() {
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    public User getUser() {
        return currentUser;
    }

    public void clear() {
        currentUser = null;
        instance = null;
    }
}
