package com.example.ecowaste;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecowaste.activities_in_dashboard.CommunityActivity;
import com.example.ecowaste.activities_in_dashboard.MapActivity;
import com.example.ecowaste.activities_in_dashboard.ProfileActivity;
import com.example.ecowaste.activities_in_dashboard.ReportsActivity;
import com.example.ecowaste.activities_in_dashboard.ScanActivity;
import com.example.ecowaste.utils.SharedPreferencesManager;
import com.example.ecowaste.activities_in_dashboard.RecyclingTipsActivity;


public class DashboardActivity extends AppCompatActivity {

    private GridLayout dashboardGrid;
    private ImageView dashboardImage;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dashboardGrid = findViewById(R.id.dashboardGrid);
        dashboardImage = findViewById(R.id.dashboardImage);
        logoutButton = findViewById(R.id.logoutButton);

        // Animation on dashboard image click
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
        dashboardImage.setOnClickListener(v -> {
            dashboardImage.startAnimation(rotate);
            Toast.makeText(this, "🌿 Let's recycle!", Toast.LENGTH_SHORT).show();
        });

        // Card 0: Scan Waste
        dashboardGrid.getChildAt(0).setOnClickListener(v ->
                startActivity(new Intent(this, ScanActivity.class)));

        // Card 1: Recycling Tips
        dashboardGrid.getChildAt(1).setOnClickListener(v ->
                startActivity(new Intent(this, RecyclingTipsActivity.class)));

        // Card 2: My Reports
        dashboardGrid.getChildAt(2).setOnClickListener(v ->
                startActivity(new Intent(this, ReportsActivity.class)));

        // Card 3: Community
        dashboardGrid.getChildAt(3).setOnClickListener(v ->
                startActivity(new Intent(this, CommunityActivity.class)));

        // Card 4: Recycling Map
        dashboardGrid.getChildAt(4).setOnClickListener(v ->
                startActivity(new Intent(this, MapActivity.class)));

        // Card 5: My Profile
        dashboardGrid.getChildAt(5).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Logout Button
        logoutButton.setOnClickListener(v -> {
            SharedPreferencesManager.init(getApplicationContext());
            SharedPreferencesManager.clearAll();

            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
