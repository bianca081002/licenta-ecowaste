package com.example.ecowaste.activities_in_dashboard;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.adapters.CommunityAdapter;
import com.example.ecowaste.network.ApiClient;
import com.example.ecowaste.network.ApiService;
import com.example.ecowaste.network.CommunityUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommunityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerCommunity);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getCommunityData();
    }

    private void getCommunityData() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<CommunityUser>> call = apiService.getCommunityReports();

        call.enqueue(new Callback<List<CommunityUser>>() {
            @Override
            public void onResponse(Call<List<CommunityUser>> call, Response<List<CommunityUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new CommunityAdapter(response.body());
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<CommunityUser>> call, Throwable t) {
                Toast.makeText(CommunityActivity.this, "Failed to load community data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
