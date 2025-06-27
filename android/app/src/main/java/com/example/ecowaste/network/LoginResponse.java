package com.example.ecowaste.network;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("is_admin")
    private boolean isAdmin;
    @SerializedName("user_id")
    private String userId;
    public boolean isAdmin() {
        return isAdmin;
    }
    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
    public String getUserId() {
        return userId;
    }
}