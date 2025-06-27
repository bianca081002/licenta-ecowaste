package com.example.ecowaste.utils;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecowaste.R;
import com.example.ecowaste.network.ApiClient;
import com.example.ecowaste.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateScoreActivity extends AppCompatActivity {

    EditText editTextUserId, editTextNewScore;
    Button btnUpdateScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_score);

        editTextUserId = findViewById(R.id.editTextUserId);
        editTextNewScore = findViewById(R.id.editTextNewScore);
        btnUpdateScore = findViewById(R.id.btnUpdateScore);

        SharedPreferencesManager.init(getApplicationContext());
        String token = SharedPreferencesManager.getToken();

        // ✅ Preluăm user_id automat din Intent și îl afișăm în câmp
        String receivedUserId = getIntent().getStringExtra("user_id");
        if (receivedUserId != null) {
            editTextUserId.setText(receivedUserId);
            editTextUserId.setEnabled(false);  // opțional: nu permite editarea
        }

        btnUpdateScore.setOnClickListener(v -> {
            String userId = editTextUserId.getText().toString().trim();
            String scoreText = editTextNewScore.getText().toString().trim();

            if (!userId.isEmpty() && !scoreText.isEmpty()) {
                int newScore;
                try {
                    newScore = Integer.parseInt(scoreText);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Scor invalid", Toast.LENGTH_SHORT).show();
                    return;
                }

                ApiService apiService = ApiClient.getClient().create(ApiService.class);
                Map<String, Integer> body = new HashMap<>();
                body.put("scans", newScore);

                Call<ResponseBody> call = apiService.updateUserScore("Bearer " + token, userId, body);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(UpdateScoreActivity.this, "Scor actualizat!", Toast.LENGTH_SHORT).show();
                            finish(); // închide activitatea
                        } else {
                            Toast.makeText(UpdateScoreActivity.this, "Eroare la actualizare", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(UpdateScoreActivity.this, "Eroare: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(this, "Completează toate câmpurile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
