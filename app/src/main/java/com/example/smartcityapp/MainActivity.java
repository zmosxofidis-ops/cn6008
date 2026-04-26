package com.example.smartcityapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcityapp.models.CityReport;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private ProgressBar loadingSpinner;
    private DatabaseHelper dbHelper;

    private Uri photoURI;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private View layoutHome, layoutHistoryContainer, layoutReportForm;
    private ImageView ivReportPreview;
    private View cardPreview;
    private TextInputEditText etDescription;
    private Spinner spinnerCategory;
    
    private RecyclerView rvHistory;
    private ReportAdapter reportAdapter;
    private final List<CityReport> allReports = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI References
        layoutHome = findViewById(R.id.layout_home);
        layoutHistoryContainer = findViewById(R.id.layout_history_container);
        layoutReportForm = findViewById(R.id.layout_report_form);
        loadingSpinner = findViewById(R.id.loading_spinner);
        
        ivReportPreview = findViewById(R.id.report_iv_preview);
        cardPreview = findViewById(R.id.report_card_preview);
        etDescription = findViewById(R.id.report_et_description);
        spinnerCategory = findViewById(R.id.report_spinner_category);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Activity Result Launchers
        setupLaunchers();

        // Buttons
        findViewById(R.id.btn_home_report).setOnClickListener(v -> showReportForm());
        findViewById(R.id.btn_home_history).setOnClickListener(v -> showHistory());
        findViewById(R.id.report_btn_photo).setOnClickListener(v -> checkPermissionAndTakePhoto());
        findViewById(R.id.report_btn_submit).setOnClickListener(v -> submitReport());

        // Toolbars
        ((MaterialToolbar) findViewById(R.id.toolbar_report)).setNavigationOnClickListener(v -> showHome());
        ((MaterialToolbar) findViewById(R.id.toolbar_history)).setNavigationOnClickListener(v -> showHome());

        // RecyclerView
        rvHistory = findViewById(R.id.rv_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        reportAdapter = new ReportAdapter(allReports);
        rvHistory.setAdapter(reportAdapter);

        loadReportsFromLocalDb();
    }

    private void setupLaunchers() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        cardPreview.setVisibility(View.VISIBLE);
                        ivReportPreview.setImageURI(photoURI);
                    }
                }
        );

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) dispatchTakePictureIntent();
                    else showToast("Απαιτείται άδεια κάμερας");
                }
        );
    }

    private void showHome() {
        layoutHome.setVisibility(View.VISIBLE);
        layoutHistoryContainer.setVisibility(View.GONE);
        layoutReportForm.setVisibility(View.GONE);
    }

    private void showReportForm() {
        layoutHome.setVisibility(View.GONE);
        layoutHistoryContainer.setVisibility(View.GONE);
        layoutReportForm.setVisibility(View.VISIBLE);
    }

    private void showHistory() {
        layoutHome.setVisibility(View.GONE);
        layoutReportForm.setVisibility(View.GONE);
        layoutHistoryContainer.setVisibility(View.VISIBLE);
        loadReportsFromLocalDb();
    }

    private void checkPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void dispatchTakePictureIntent() {
        try {
            File photoFile = File.createTempFile("TEMP_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePictureLauncher.launch(photoURI);
        } catch (IOException ex) {
            showToast("Σφάλμα αρχείου");
        }
    }

    private void submitReport() {
        String desc = etDescription.getText().toString().trim();
        if (desc.isEmpty()) {
            showToast("Παρακαλώ εισάγετε περιγραφή");
            return;
        }

        loadingSpinner.setVisibility(View.VISIBLE);
        String category = spinnerCategory.getSelectedItem().toString();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    double lat = (location != null) ? location.getLatitude() : 37.9838;
                    double lon = (location != null) ? location.getLongitude() : 23.7275;
                    saveReport(category, desc, lat, lon);
                })
                .addOnFailureListener(e -> saveReport(category, desc, 37.9838, 23.7275));
    }

    private void saveReport(String category, String desc, double lat, double lon) {
        new Thread(() -> {
            String savedPath = null;
            if (photoURI != null) {
                savedPath = processAndSaveImage(photoURI);
            }

            CityReport report = new CityReport(category, desc, category, lat, lon, savedPath);
            dbHelper.addReport(report);

            new Handler(Looper.getMainLooper()).post(() -> {
                loadingSpinner.setVisibility(View.GONE);
                showToast("Η αναφορά αποθηκεύτηκε!");
                resetForm();
                showHistory();
            });
        }).start();
    }

    private String processAndSaveImage(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            // Downsample for memory safety
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; 
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
            
            if (bitmap == null) return null;

            File file = new File(getFilesDir(), "IMG_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void resetForm() {
        etDescription.setText("");
        cardPreview.setVisibility(View.GONE);
        photoURI = null;
    }

    private void loadReportsFromLocalDb() {
        new Thread(() -> {
            List<CityReport> fetched = dbHelper.getAllReports();
            new Handler(Looper.getMainLooper()).post(() -> {
                allReports.clear();
                allReports.addAll(fetched);
                reportAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}