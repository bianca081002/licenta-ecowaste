package com.example.ecowaste.activities_in_dashboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.ecowaste.R;
import com.example.ecowaste.models.ClassificationResponse;
import com.example.ecowaste.network.ApiClient;
import com.example.ecowaste.network.ApiService;
import com.example.ecowaste.utils.SharedPreferencesManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private ImageView previewImage;
    private CardView resultCard;
    private TextView resultText;
    private Button takePhotoButton;

    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        previewImage = findViewById(R.id.previewImage);
        resultCard = findViewById(R.id.resultCard);
        resultText = findViewById(R.id.resultText);
        takePhotoButton = findViewById(R.id.captureImageButton);// același ID din layout

        SharedPreferencesManager.init(this);
        requestPermissions();

        takePhotoButton.setOnClickListener(v -> dispatchTakePictureIntent());
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Eroare creare fișier imagine", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName() + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File imageFile = new File(currentPhotoPath);
            previewImage.setImageURI(Uri.fromFile(imageFile)); // afișăm imaginea
            previewImage.setVisibility(View.VISIBLE);
            classifyImage(imageFile); // trimitem imaginea la server pentru clasificare
        }
    }

    private void classifyImage(File imageFile) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        String userId = SharedPreferencesManager.getUserId();
        RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), userId);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ClassificationResponse> call = apiService.classifyImage(body, userIdBody);

        call.enqueue(new Callback<ClassificationResponse>() {
            @Override
            public void onResponse(Call<ClassificationResponse> call, Response<ClassificationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String bin = response.body().getBin().toLowerCase();

                    int bgColor;
                    switch (bin) {
                        case "plastic":
                            bgColor = Color.parseColor("#4CAF50");
                            break;
                        case "paper":
                            bgColor = Color.parseColor("#2196F3");
                            break;
                        case "glass":
                            bgColor = Color.parseColor("#8BC34A");
                            break;
                        case "metal":
                            bgColor = Color.parseColor("#9E9E9E");
                            break;
                        case "cardboard":
                            bgColor = Color.parseColor("#A1887F");
                            break;
                        case "trash":
                        default:
                            bgColor = Color.parseColor("#F44336");
                            break;
                    }

                    resultCard.setCardBackgroundColor(bgColor);
                    resultText.setText("Clasificat ca: " + bin.toUpperCase());

                    Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    Animation bounce = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
                    resultCard.setVisibility(View.VISIBLE);
                    resultCard.startAnimation(fadeIn);
                    resultText.startAnimation(bounce);
                } else {
                    Toast.makeText(ScanActivity.this, "Eroare API: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ClassificationResponse> call, Throwable t) {
                Toast.makeText(ScanActivity.this, "Eroare rețea: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
