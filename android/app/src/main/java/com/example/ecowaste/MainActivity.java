package com.example.ecowaste;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import com.example.ecowaste.network.ApiClient;
import com.example.ecowaste.network.ApiService;
import com.example.ecowaste.network.LoginRequest;
import com.example.ecowaste.network.LoginResponse;
import com.example.ecowaste.network.VerifyResponse;
import com.example.ecowaste.utils.SharedPreferencesManager;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private Button loginButton;
    private TextView registerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        SharedPreferencesManager.init(getApplicationContext());
        String token = SharedPreferencesManager.getToken();

        if (token != null) {
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            apiService.verifyToken("Bearer " + token).enqueue(new Callback<VerifyResponse>() {
                @Override
                public void onResponse(Call<VerifyResponse> call, Response<VerifyResponse> response) {
                    if (response.isSuccessful()) {
                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<VerifyResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Autologin failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Remember Me
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("saved_email", "");
        String savedPassword = prefs.getString("saved_password", "");
        boolean remember = prefs.getBoolean("remember_me", false);

        if (remember) {
            emailEditText.setText(savedEmail);
            passwordEditText.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
        }

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        registerTextView.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SignupActivity.class)));
    }

    private void loginUser(String email, String password) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        LoginRequest request = new LoginRequest(email, password);

        Call<LoginResponse> call = apiService.loginUser(request);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    String token = response.body().getAccessToken();

                    // Salvează tokenul și email-ul
                    SharedPreferencesManager.saveToken(token);
                    SharedPreferencesManager.saveEmail(email);

                    String userId = response.body().getUserId();
                    SharedPreferencesManager.saveUserId(userId);

                    // Remember Me logic
                    SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
                    if (rememberMeCheckBox.isChecked()) {
                        editor.putString("saved_email", email);
                        editor.putString("saved_password", password);
                        editor.putBoolean("remember_me", true);
                    } else {
                        editor.remove("saved_email");
                        editor.remove("saved_password");
                        editor.putBoolean("remember_me", false);
                    }
                    editor.apply();

                    boolean isAdmin = response.body().isAdmin();

                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                    if (isAdmin) {
                        startActivity(new Intent(MainActivity.this, com.example.ecowaste.AdminDashboardActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                    }
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
