package com.example.smartcityapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.smartcityapp.models.CityReport;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper: Διαχειριστής SQLite.
 * Βελτιωμένος για σταθερότητα και αποφυγή ANR.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartCity.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_REPORTS = "reports";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_IMAGE_PATH = "image_path";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_REPORTS_TABLE = "CREATE TABLE " + TABLE_REPORTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_LATITUDE + " REAL,"
                + COLUMN_LONGITUDE + " REAL,"
                + COLUMN_IMAGE_PATH + " TEXT" + ")";
        db.execSQL(CREATE_REPORTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_REPORTS + " ADD COLUMN " + COLUMN_IMAGE_PATH + " TEXT");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error upgrading database: " + e.getMessage());
            }
        }
    }

    public void addReport(CityReport report) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, report.getTitle());
        values.put(COLUMN_DESCRIPTION, report.getDescription());
        values.put(COLUMN_CATEGORY, report.getCategory());
        values.put(COLUMN_LATITUDE, report.getLatitude());
        values.put(COLUMN_LONGITUDE, report.getLongitude());
        values.put(COLUMN_IMAGE_PATH, report.getImagePath());

        db.insert(TABLE_REPORTS, null, values);
        // Δεν κλείνουμε τη βάση εδώ, το διαχειρίζεται το OpenHelper
    }

    public List<CityReport> getAllReports() {
        List<CityReport> reportList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_REPORTS + " ORDER BY " + COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor != null && cursor.moveToFirst()) {
                int titleIdx = cursor.getColumnIndex(COLUMN_TITLE);
                int descIdx = cursor.getColumnIndex(COLUMN_DESCRIPTION);
                int catIdx = cursor.getColumnIndex(COLUMN_CATEGORY);
                int latIdx = cursor.getColumnIndex(COLUMN_LATITUDE);
                int lonIdx = cursor.getColumnIndex(COLUMN_LONGITUDE);
                int pathIdx = cursor.getColumnIndex(COLUMN_IMAGE_PATH);

                do {
                    // Έλεγχος αν οι στήλες υπάρχουν όντως (index != -1)
                    String title = (titleIdx != -1) ? cursor.getString(titleIdx) : "Αναφορά";
                    String desc = (descIdx != -1) ? cursor.getString(descIdx) : "";
                    String cat = (catIdx != -1) ? cursor.getString(catIdx) : "Άλλο";
                    double lat = (latIdx != -1) ? cursor.getDouble(latIdx) : 0.0;
                    double lon = (lonIdx != -1) ? cursor.getDouble(lonIdx) : 0.0;
                    String path = (pathIdx != -1) ? cursor.getString(pathIdx) : null;

                    CityReport report = new CityReport(title, desc, cat, lat, lon, path);
                    reportList.add(report);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fetching reports: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return reportList;
    }
}