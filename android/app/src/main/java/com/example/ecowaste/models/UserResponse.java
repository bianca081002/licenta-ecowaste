package com.example.ecowaste.models;

public class UserResponse {
    private String id;
    private String username;
    private String email;
    private boolean is_admin;

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isIs_admin() { return is_admin; }
}