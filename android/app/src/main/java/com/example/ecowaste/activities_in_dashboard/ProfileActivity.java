package com.example.ecowaste.activities_in_dashboard;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.ecowaste.DashboardActivity;
import com.example.ecowaste.MainActivity;
import com.example.ecowaste.R;
import com.example.ecowaste.network.ApiClient;
import com.example.ecowaste.network.ApiService;
import com.example.ecowaste.utils.SharedPreferencesManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1001;
    private static final int TAKE_PHOTO = 1002;

    private EditText fullName, email, birthday;
    private RadioGroup genderGroup;
    private RadioButton maleRadio, femaleRadio;
    private ImageView profileImage, backButton;
    private Button changeProfileButton, saveProfileButton, editProfileButton, deleteAccountButton;
    private SharedPreferences sharedPreferences;
    private String currentPhotoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, 100);
            }
        }

        sharedPreferences = SharedPreferencesManager.getUserProfilePrefs(this);

        backButton = findViewById(R.id.backButton);
        profileImage = findViewById(R.id.profileImage);
        changeProfileButton = findViewById(R.id.changeProfileButton);
        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        birthday = findViewById(R.id.birthday);
        genderGroup = findViewById(R.id.genderGroup);
        maleRadio = findViewById(R.id.maleRadio);
        femaleRadio = findViewById(R.id.femaleRadio);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        editProfileButton = findViewById(R.id.editProfileButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        email.setEnabled(false);

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, DashboardActivity.class));
            finish();
        });

        changeProfileButton.setOnClickListener(v -> showImagePickerDialog());
        birthday.setOnClickListener(v -> showDatePicker());

        saveProfileButton.setOnClickListener(v -> {
            saveUserProfile();
            Animation shake = AnimationUtils.loadAnimation(ProfileActivity.this, R.anim.shake);
            saveProfileButton.startAnimation(shake);
            saveProfileButton.setVisibility(View.GONE);
            editProfileButton.setVisibility(View.VISIBLE);
            setEditable(false);
            showToast("Profil salvat");
        });

        editProfileButton.setOnClickListener(v -> {
            setEditable(true);
            saveProfileButton.setVisibility(View.VISIBLE);
            editProfileButton.setVisibility(View.GONE);
        });

        deleteAccountButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        loadUserProfile();
    }

    private void setEditable(boolean editable) {
        fullName.setEnabled(editable);
        birthday.setEnabled(editable);
        maleRadio.setEnabled(editable);
        femaleRadio.setEnabled(editable);
        changeProfileButton.setEnabled(editable);
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selectează sursa imaginii")
                .setItems(new CharSequence[]{"Fă o poză", "Alege din galerie"}, (dialog, which) -> {
                    if (which == 0) takePhoto();
                    else if (which == 1) pickImageFromGallery();
                }).show();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = new File(getExternalFilesDir("Pictures"), "profile.jpg");
        currentPhotoPath = photoFile.getAbsolutePath();
        Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        try {
            Bitmap bitmap = null;
            if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
            } else if (requestCode == TAKE_PHOTO) {
                bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            }

            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap);
                saveImageToInternalStorage(bitmap);
            }

        } catch (Exception e) {
            showToast("Eroare imagine");
            e.printStackTrace();
        }
    }

    private void saveImageToInternalStorage(Bitmap bitmap) {
        try {
            File file = new File(getFilesDir(), "profile_image.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteLocalProfileImage() {
        File file = new File(getFilesDir(), "profile_image.png");
        if (file.exists()) {
            file.delete();
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int y = calendar.get(Calendar.YEAR), m = calendar.get(Calendar.MONTH), d = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) ->
                        birthday.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                y, m, d);
        dialog.show();
    }

    private void saveUserProfile() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("fullName", fullName.getText().toString().trim());
        editor.putString("birthday", birthday.getText().toString().trim());
        String gender = maleRadio.isChecked() ? "Male" : femaleRadio.isChecked() ? "Female" : "";
        editor.putString("gender", gender);
        editor.apply();
    }

    private void loadUserProfile() {
        fullName.setText(sharedPreferences.getString("fullName", ""));
        birthday.setText(sharedPreferences.getString("birthday", ""));
        email.setText(getSharedPreferences("app_prefs", MODE_PRIVATE).getString("user_email", ""));
        email.setEnabled(false);

        String gender = sharedPreferences.getString("gender", "");
        if (gender.equals("Male")) maleRadio.setChecked(true);
        else if (gender.equals("Female")) femaleRadio.setChecked(true);

        loadSavedProfileImage();
    }

    private void loadSavedProfileImage() {
        File file = new File(getFilesDir(), "profile_image.png");
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            profileImage.setImageBitmap(bitmap);
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmare ștergere")
                .setMessage("Ești sigur că vrei să îți ștergi contul? Această acțiune este ireversibilă.")
                .setPositiveButton("Șterge", (dialog, which) -> deleteAccount())
                .setNegativeButton("Anulează", null)
                .show();
    }

    private void deleteAccount() {
        String token = SharedPreferencesManager.getToken();
        if (token == null || token.isEmpty()) {
            showToast("Token lipsă. Te rugăm să te reautentifici.");
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.deleteAccount("Bearer " + token);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    deleteLocalProfileImage();
                    SharedPreferencesManager.clearAll();
                    showToast("Contul a fost șters.");
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    showToast("Eroare la ștergere cont.");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showToast("Eroare rețea: " + t.getMessage());
            }
        });
    }
}
