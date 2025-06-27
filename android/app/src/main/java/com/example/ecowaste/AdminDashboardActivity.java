package com.example.ecowaste;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.ecowaste.utils.AdminUsersActivity;
import com.example.ecowaste.utils.SelectUserForScoreActivity;
import com.example.ecowaste.utils.SharedPreferencesManager;
import com.example.ecowaste.utils.UpdateScoreActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private CardView cardViewUsers;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Inițializează accesul la SharedPreferences
        SharedPreferencesManager.init(getApplicationContext());
        String token = SharedPreferencesManager.getToken();

        if (token == null) {
            Toast.makeText(this, "Acces interzis. Te rugăm să te autentifici.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Legătură cu componentele din XML
        cardViewUsers = findViewById(R.id.cardViewUsers);
        logoutButton = findViewById(R.id.logoutButton);

        // Click pe cardul "View Users"
        cardViewUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminUsersActivity.class);
            startActivity(intent);
        });

        // Logout admin
        logoutButton.setOnClickListener(v -> {
            SharedPreferencesManager.clearAll();
            Toast.makeText(this, "Deconectat cu succes", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        CardView cardViewResetScore = findViewById(R.id.cardViewResetScore);
        cardViewResetScore.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, SelectUserForScoreActivity.class);
            startActivity(intent);
        });

    }
}
