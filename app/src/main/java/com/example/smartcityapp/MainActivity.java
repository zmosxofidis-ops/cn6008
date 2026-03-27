package com.example.smartcityapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    Button btnReport, btnSubmit;
    ImageView imgPreview;
    TextView txtLocation;
    TextInputEditText edtComment;
    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnReport = findViewById(R.id.btnReport);
        btnSubmit = findViewById(R.id.btnSubmit);
        imgPreview = findViewById(R.id.imgPreview);
        txtLocation = findViewById(R.id.txtLocation);
        edtComment = findViewById(R.id.edtComment);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnReport.setOnClickListener(v -> checkPermissions());

        btnSubmit.setOnClickListener(v -> {
            if (edtComment.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Παρακαλώ προσθέστε μια περιγραφή", Toast.LENGTH_SHORT).show();
            } else {
                showSuccessDialog();
            }
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Επιτυχία!")
                .setMessage("Η αναφορά σας στάλθηκε στην υπηρεσία του Δήμου. Ευχαριστούμε!")
                .setPositiveButton("OK", (dialog, which) -> resetFields())
                .setCancelable(false)
                .setCancelable(false)
                .show();
    }

    private void resetFields() {
        edtComment.setText("");
        txtLocation.setText("Τοποθεσία: Δεν έχει ληφθεί");
        imgPreview.setImageResource(android.R.drawable.ic_menu_camera);
        imgPreview.setBackgroundColor(android.graphics.Color.parseColor("#F0F0F0"));
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        } else {
            openCamera();
            getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    txtLocation.setText("📍 Τοποθεσία: " + location.getLatitude() + ", " + location.getLongitude());
                }
            });
        }
    }

    private void openCamera() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            imgPreview.setImageBitmap(image);
        }
    }
}