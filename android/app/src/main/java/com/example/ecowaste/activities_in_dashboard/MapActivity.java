package com.example.ecowaste.activities_in_dashboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.ecowaste.R;
import com.example.ecowaste.models.RecyclingCenter;
import com.example.ecowaste.network.ApiClient;
import com.example.ecowaste.network.ApiService;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Marker currentLocationMarker;

    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private boolean cameraCentered = false;
    private boolean centersLoaded = false;
    private final HashMap<Marker, RecyclingCenter> markerData = new HashMap<>();
    private final List<String> selectedTypes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        CheckBox checkPlastic = findViewById(R.id.checkPlastic);
        CheckBox checkHartie = findViewById(R.id.checkHartie);
        CheckBox checkSticla = findViewById(R.id.checkSticla);
        CheckBox checkMetal = findViewById(R.id.checkMetal);

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            selectedTypes.clear();
            if (checkPlastic.isChecked()) selectedTypes.add("plastic");
            if (checkHartie.isChecked()) selectedTypes.add("hartie");
            if (checkSticla.isChecked()) selectedTypes.add("sticla");
            if (checkMetal.isChecked()) selectedTypes.add("metal");

            centersLoaded = false;
            mMap.clear();
            if (currentLocationMarker != null) {
                currentLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(currentLocationMarker.getPosition())
                        .title("Locația ta curentă"));
            }
            loadRecyclingCentersFromApi();
        };

        checkPlastic.setOnCheckedChangeListener(listener);
        checkHartie.setOnCheckedChangeListener(listener);
        checkSticla.setOnCheckedChangeListener(listener);
        checkMetal.setOnCheckedChangeListener(listener);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                                .position(userLocation)
                                .title("Locația ta curentă"));

                        if (!cameraCentered) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                            cameraCentered = true;
                        }

                        Toast.makeText(this,
                                "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude(),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Locație indisponibilă", Toast.LENGTH_SHORT).show();
                    }
                });

        mMap.setOnMarkerClickListener(marker -> {
            RecyclingCenter center = markerData.get(marker);
            if (center != null) {
                showBottomSheet(center);
                return true;
            }
            return false;
        });

        startLocationUpdates();
        loadRecyclingCentersFromApi();
    }

    private void startLocationUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    if (currentLocationMarker != null) {
                        currentLocationMarker.setPosition(userLocation);
                    } else {
                        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                                .position(userLocation)
                                .title("Locația ta curentă"));
                    }

                    if (!cameraCentered) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                        cameraCentered = true;
                    }

                    if (!centersLoaded) {
                        loadRecyclingCentersFromApi();
                        centersLoaded = true;
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void loadRecyclingCentersFromApi() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getRecyclingCenters().enqueue(new Callback<List<RecyclingCenter>>() {
            @Override
            public void onResponse(Call<List<RecyclingCenter>> call, Response<List<RecyclingCenter>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RecyclingCenter> filteredCenters = new ArrayList<>();

                    for (RecyclingCenter center : response.body()) {
                        if (!selectedTypes.isEmpty()) {
                            boolean matches = false;
                            for (String type : center.types) {
                                String normalizedType = type.toLowerCase(Locale.ROOT)
                                        .replace("â", "a").replace("ă", "a").replace("î", "i")
                                        .replace("ș", "s").replace("ţ", "t").replace("ț", "t");

                                for (String selected : selectedTypes) {
                                    String normalizedSelected = selected.toLowerCase(Locale.ROOT)
                                            .replace("â", "a").replace("ă", "a").replace("î", "i")
                                            .replace("ș", "s").replace("ţ", "t").replace("ț", "t");

                                    if (normalizedType.equals(normalizedSelected)) {
                                        matches = true;
                                        break;
                                    }
                                }
                                if (matches) break;
                            }
                            if (!matches) continue;
                        }
                        filteredCenters.add(center);
                    }

                    if (filteredCenters.isEmpty()) {
                        Toast.makeText(MapActivity.this, "Niciun centru găsit pentru filtrul selectat", Toast.LENGTH_SHORT).show();
                    }

                    for (RecyclingCenter center : filteredCenters) {
                        LatLng location = new LatLng(center.lat, center.lng);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(center.name)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        if (marker != null) {
                            markerData.put(marker, center);
                        }
                    }
                } else {
                    Toast.makeText(MapActivity.this, "Eroare la încărcare centre", Toast.LENGTH_SHORT).show();
                    Log.e("API_DEBUG", "Cod răspuns: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<RecyclingCenter>> call, Throwable t) {
                Toast.makeText(MapActivity.this, "Conexiune eșuată: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", "Eroare Retrofit: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(mMap);
            } else {
                Toast.makeText(this, "Permisiune refuzată", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showBottomSheet(RecyclingCenter center) {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_recycling_center, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);

        TextView tvName = view.findViewById(R.id.tvCenterName);
        TextView tvTypes = view.findViewById(R.id.tvTypes);
        TextView tvSchedule = view.findViewById(R.id.tvSchedule);
        TextView tvStatus = view.findViewById(R.id.tvStatus);
        TextView tvWebsite = view.findViewById(R.id.tvWebsite);

        tvName.setText(center.name);
        tvTypes.setText("Tipuri: " + String.join(", ", center.types));

        StringBuilder scheduleBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : center.schedule.entrySet()) {
            scheduleBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        tvSchedule.setText(scheduleBuilder.toString());

        String today = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date()).toLowerCase();
        String hourNow = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        String interval = center.schedule.get(today);
        if (interval == null || interval.equalsIgnoreCase("Închis")) {
            tvStatus.setText("Închis");
            tvStatus.setTextColor(Color.RED);
        } else {
            String[] parts = interval.split("-");
            if (parts.length == 2 && hourNow.compareTo(parts[0]) >= 0 && hourNow.compareTo(parts[1]) <= 0) {
                tvStatus.setText("Deschis acum");
                tvStatus.setTextColor(Color.GREEN);
            } else {
                tvStatus.setText("Închis");
                tvStatus.setTextColor(Color.RED);
            }
        }

        tvWebsite.setText(center.url);
        tvWebsite.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(center.url));
            startActivity(intent);
        });

        dialog.show();
    }
}