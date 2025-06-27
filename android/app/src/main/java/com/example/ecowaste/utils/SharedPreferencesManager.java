package com.example.ecowaste.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREF_NAME = "app_prefs";
    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public static void saveEmail(String email) {
        sharedPreferences.edit().putString("user_email", email).apply();
    }

    public static String getEmail() {
        return sharedPreferences.getString("user_email", "");
    }

    public static void saveToken(String token) {
        sharedPreferences.edit().putString("jwt_token", token).apply();
    }

    public static String getToken() {
        return sharedPreferences.getString("jwt_token", null);
    }

    public static void saveUserId(String userId) {
        sharedPreferences.edit().putString("user_id", userId).apply();
    }

    public static String getUserId() {
        return sharedPreferences.getString("user_id", "default_user");
    }

    public static SharedPreferences getUserProfilePrefs(Context context) {
        String email = getEmail();
        return context.getSharedPreferences("UserProfile_" + email, Context.MODE_PRIVATE);
    }

    public static void clearAll() {
        sharedPreferences.edit().clear().apply();
    }

    public static void clearAll(Context context) {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE).edit().clear().apply();
    }
}
