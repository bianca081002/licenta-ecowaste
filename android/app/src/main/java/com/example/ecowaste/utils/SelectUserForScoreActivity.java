package com.example.ecowaste.utils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.ScoreUsersAdapter;
import com.example.ecowaste.adapters.UsersAdapter;
import com.example.ecowaste.models.UserResponse;
import com.example.ecowaste.network.ApiClient;
import com.example.ecowaste.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectUserForScoreActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ApiService apiService;
    private List<UserResponse> userList = new ArrayList<>();
    private ScoreUsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users); // Refolosim același layout

        SharedPreferencesManager.init(getApplicationContext());
        apiService = ApiClient.getClient().create(ApiService.class);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers(); // Reîncarcă lista când revii în activitate
    }

    private void loadUsers() {
        String token = SharedPreferencesManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Token lipsă. Te rog autentifică-te.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService.getAllUsers("Bearer " + token).enqueue(new Callback<List<UserResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserResponse>> call, @NonNull Response<List<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList = response.body();
                    adapter = new ScoreUsersAdapter(userList, SelectUserForScoreActivity.this, user -> {
                        Intent intent = new Intent(SelectUserForScoreActivity.this, UpdateScoreActivity.class);
                        intent.putExtra("user_id", user.getId());
                        startActivity(intent);
                    });
                    recyclerView.setAdapter(adapter);

                } else {
                    Toast.makeText(SelectUserForScoreActivity.this, "Acces interzis sau token invalid", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserResponse>> call, @NonNull Throwable t) {
                Toast.makeText(SelectUserForScoreActivity.this, "Eroare: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
