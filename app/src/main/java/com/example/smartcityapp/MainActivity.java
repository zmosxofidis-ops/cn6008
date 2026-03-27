package com.example.smartcityapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.smartcityapp.models.CityReport;
import com.example.smartcityapp.models.ParkingSpot;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private ProgressBar loadingSpinner;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        loadingSpinner = findViewById(R.id.loading_spinner);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ExtendedFloatingActionButton fabReport = findViewById(R.id.fab_report);
        fabReport.setOnClickListener(v -> showReportDialog());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        fetchParkingSpots();
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            }
        });
    }

    private void fetchParkingSpots() {
        loadingSpinner.setVisibility(View.VISIBLE);
        db.collection("parking_spots").addSnapshotListener((value, error) -> {
            loadingSpinner.setVisibility(View.GONE);
            if (error != null) {
                Toast.makeText(this, "Σφάλμα φόρτωσης δεδομένων", Toast.LENGTH_SHORT).show();
                return;
            }

            mMap.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    ParkingSpot spot = doc.toObject(ParkingSpot.class);
                    LatLng pos = new LatLng(spot.getLocation().getLatitude(), spot.getLocation().getLongitude());
                    float color = spot.isAvailable() ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED;
                    
                    mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(spot.getName())
                            .snippet(spot.isAvailable() ? "Διαθέσιμο" : "Κατειλημμένο")
                            .icon(BitmapDescriptorFactory.defaultMarker(color)));
                }
            }
        });
    }

    private void showReportDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report, null);
        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etDesc = dialogView.findViewById(R.id.et_description);
        Spinner spinnerCat = dialogView.findViewById(R.id.spinner_category);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Αποστολή", (dialog, which) -> {
                    String title = etTitle.getText().toString();
                    String desc = etDesc.getText().toString();
                    String cat = spinnerCat.getSelectedItem().toString();
                    submitReport(title, desc, cat);
                })
                .setNegativeButton("Ακύρωση", null)
                .show();
    }

    private void submitReport(String title, String desc, String category) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Απαιτείται πρόσβαση στην τοποθεσία", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(this, "Δεν βρέθηκε τοποθεσία", Toast.LENGTH_SHORT).show();
                return;
            }

            CityReport report = new CityReport(
                    title, desc, category,
                    new GeoPoint(location.getLatitude(), location.getLongitude()),
                    new Timestamp(new Date())
            );

            db.collection("reports").add(report)
                    .addOnSuccessListener(documentReference -> 
                        Toast.makeText(this, "Η αναφορά στάλθηκε επιτυχώς!", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> 
                        Toast.makeText(this, "Αποτυχία αποστολής αναφοράς", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }
}
