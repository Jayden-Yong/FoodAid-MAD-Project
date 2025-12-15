package com.example.foodaid_mad_project.AuthFragments;

import java.util.List;
import java.util.Map;

public class User {
    private String uid;
    private String email;
    private String name;

    public User() {
    }

    public User(String uid, String email, String name) {
        this.uid = uid;
        this.email = email;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
