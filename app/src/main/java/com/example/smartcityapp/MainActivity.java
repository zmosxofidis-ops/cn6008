package com.example.smartcityapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcityapp.models.CityReport;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Κεντρική δραστηριότητα της εφαρμογής SmartCity.
 * Διαχειρίζεται την αρχική οθόνη και το ιστορικό αναφορών.
 * (Οι λειτουργίες Google Maps έχουν αφαιρεθεί)
 */
public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private ProgressBar loadingSpinner;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Τοπική βάση δεδομένων SQLite
    private DatabaseHelper dbHelper;

    private Uri photoURI;
    private ImageView ivReportPreview;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    // UI Layouts για εναλλαγή οθονών
    private View layoutHome;
    private View layoutHistoryContainer;
    
    private RecyclerView rvHistory;
    private ReportAdapter reportAdapter;
    private final List<CityReport> allReports = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Διαχείριση αποτελέσματος κάμερας
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result && ivReportPreview != null) {
                        View cardPreview = ivReportPreview.getRootView().findViewById(R.id.card_image_preview);
                        if (cardPreview != null) cardPreview.setVisibility(View.VISIBLE);
                        ivReportPreview.setImageURI(photoURI);
                    }
                }
        );

        setContentView(R.layout.activity_main);

        // Αρχικοποίηση Database Helper
        dbHelper = new DatabaseHelper(this);

        // Υπηρεσίες τοποθεσίας και UI components
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        loadingSpinner = findViewById(R.id.loading_spinner);
        layoutHome = findViewById(R.id.layout_home);
        layoutHistoryContainer = findViewById(R.id.layout_history_container);

        // Ρύθμιση RecyclerView για το Ιστορικό
        rvHistory = findViewById(R.id.rv_history);
        if (rvHistory != null) {
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            reportAdapter = new ReportAdapter(allReports);
            rvHistory.setAdapter(reportAdapter);
        }

        // Toolbar Ιστορικού
        MaterialToolbar toolbarHistory = findViewById(R.id.toolbar_history);
        if (toolbarHistory != null) {
            toolbarHistory.setNavigationOnClickListener(v -> showHome());
        }

        // Click Listeners για τα κουμπιά της αρχικής οθόνης
        View btnHomeReport = findViewById(R.id.btn_home_report);
        if (btnHomeReport != null) btnHomeReport.setOnClickListener(v -> showReportDialog());

        View btnHomeHistory = findViewById(R.id.btn_home_history);
        if (btnHomeHistory != null) btnHomeHistory.setOnClickListener(v -> showHistory());

        // Φόρτωση αναφορών από την SQLite
        loadReportsFromLocalDb();
    }

    /**
     * Φορτώνει όλες τις αναφορές από την τοπική βάση δεδομένων σε ξεχωριστό thread.
     */
    private void loadReportsFromLocalDb() {
        new Thread(() -> {
            List<CityReport> fetched = dbHelper.getAllReports();
            new Handler(Looper.getMainLooper()).post(() -> {
                allReports.clear();
                allReports.addAll(fetched);
                if (reportAdapter != null) reportAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void showHome() {
        layoutHome.setVisibility(View.VISIBLE);
        layoutHistoryContainer.setVisibility(View.GONE);
    }

    private void showHistory() {
        layoutHome.setVisibility(View.GONE);
        layoutHistoryContainer.setVisibility(View.VISIBLE);
        if (reportAdapter != null) reportAdapter.notifyDataSetChanged();
    }

    /**
     * Εμφανίζει τον διάλογο για τη δημιουργία νέας αναφοράς.
     */
    private void showReportDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report, null);
        TextInputEditText etDesc = dialogView.findViewById(R.id.et_description);
        Spinner spinnerCat = dialogView.findViewById(R.id.spinner_category);
        Button btnPhoto = dialogView.findViewById(R.id.btn_take_photo);
        ivReportPreview = dialogView.findViewById(R.id.iv_report_image);

        btnPhoto.setOnClickListener(v -> checkCameraPermissionAndTakePhoto());

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Αποστολή", (dialog, which) -> {
                    String category = spinnerCat.getSelectedItem() != null ? spinnerCat.getSelectedItem().toString() : "Άλλο";
                    String description = etDesc.getText() != null ? etDesc.getText().toString() : "";
                    submitReport(category, description, category);
                })
                .setNegativeButton("Ακύρωση", null)
                .show();
    }

    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1002);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            showToast("Σφάλμα αρχείου");
        }
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    photoFile);
            takePictureLauncher.launch(photoURI);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void submitReport(String title, String desc, String category) {
        if (desc.isEmpty()) {
            showToast("Παρακαλώ εισάγετε περιγραφή");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        loadingSpinner.setVisibility(View.VISIBLE);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    double lat = (location != null) ? location.getLatitude() : 37.9838;
                    double lon = (location != null) ? location.getLongitude() : 23.7275;
                    saveToLocalDb(title, desc, category, lat, lon);
                })
                .addOnFailureListener(e -> {
                    saveToLocalDb(title, desc, category, 37.9838, 23.7275);
                });
    }

    private void saveToLocalDb(String title, String desc, String category, double lat, double lon) {
        new Thread(() -> {
            CityReport report = new CityReport(title, desc, category, lat, lon);
            dbHelper.addReport(report);
            
            new Handler(Looper.getMainLooper()).post(() -> {
                loadingSpinner.setVisibility(View.GONE);
                showToast("Η αναφορά αποθηκεύτηκε τοπικά!");
                loadReportsFromLocalDb();
                showHome();
            });
        }).start();
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            }
        } else if (requestCode == 1002) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            }
        }
    }
}
