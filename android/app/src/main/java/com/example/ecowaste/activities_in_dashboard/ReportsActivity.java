package com.example.ecowaste.activities_in_dashboard;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecowaste.R;
import com.example.ecowaste.models.ReportItem;
import com.example.ecowaste.network.ApiClient;
import com.example.ecowaste.network.ApiService;
import com.example.ecowaste.utils.SharedPreferencesManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity {

    private TableLayout reportTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());

        reportTable = findViewById(R.id.reportTable);

        String userId = SharedPreferencesManager.getUserId();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<ReportItem>> call = apiService.getUserReport(userId);
        call.enqueue(new Callback<List<ReportItem>>() {
            @Override
            public void onResponse(Call<List<ReportItem>> call, Response<List<ReportItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateTable(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ReportItem>> call, Throwable t) {
                Toast.makeText(ReportsActivity.this, "Eroare: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateTable(List<ReportItem> reports) {
        TableRow headerRow = new TableRow(this);
        TextView header1 = new TextView(this);
        header1.setText("Material");
        header1.setTypeface(null, Typeface.BOLD);
        TextView header2 = new TextView(this);
        header2.setText("Număr clasificări");
        header2.setTypeface(null, Typeface.BOLD);

        headerRow.addView(header1);
        headerRow.addView(header2);


        for (ReportItem item : reports) {
            TableRow row = new TableRow(this);

            TextView material = new TextView(this);
            material.setText(item.get_id());
            TextView count = new TextView(this);
            count.setText(String.valueOf(item.getCount()));

            row.addView(material);
            row.addView(count);

            reportTable.addView(row);
        }
    }
}
