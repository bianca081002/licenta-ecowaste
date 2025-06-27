package com.example.ecowaste.utils;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.UsersAdapter;
import com.example.ecowaste.models.UserResponse;
import com.example.ecowaste.network.ApiClient;
import com.example.ecowaste.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<UserResponse> userList = new ArrayList<>();
    private ApiService apiService;
    private ImageButton backButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users); // asigură-te că acesta e layoutul corect

        // Inițializare buton de întoarcere
        backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());

        // Inițializare RecyclerView
        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsersAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        // Inițializare token din SharedPreferences
        SharedPreferencesManager.init(getApplicationContext());
        String savedJwtToken = SharedPreferencesManager.getToken();

        if (savedJwtToken == null) {
            Toast.makeText(this, "Nu ești autentificat.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Interacțiune cu API
        apiService = ApiClient.getClient().create(ApiService.class);
        String token = "Bearer " + savedJwtToken;

        apiService.getAllUsers(token).enqueue(new Callback<List<UserResponse>>() {
            @Override
            public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminUsersActivity.this, "Acces interzis sau token invalid", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                Toast.makeText(AdminUsersActivity.this, "Eroare: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
