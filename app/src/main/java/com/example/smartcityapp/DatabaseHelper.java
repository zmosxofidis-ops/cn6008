package com.example.smartcityapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartcityapp.models.CityReport;

import java.util.ArrayList;
import java.util.List;

/**
 * Διαχειριστής της τοπικής βάσης δεδομένων SQLite.
 * Υπεύθυνος για τη δημιουργία του πίνακα αναφορών και τις λειτουργίες CRUD.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartCity.db";
    private static final int DATABASE_VERSION = 1;

    // Ονόματα πίνακα και στηλών
    private static final String TABLE_REPORTS = "reports";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Δημιουργία πίνακα αναφορών
        String CREATE_REPORTS_TABLE = "CREATE TABLE " + TABLE_REPORTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_LATITUDE + " REAL,"
                + COLUMN_LONGITUDE + " REAL" + ")";
        db.execSQL(CREATE_REPORTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Διαγραφή και επαναδημιουργία σε περίπτωση αναβάθμισης
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTS);
        onCreate(db);
    }

    /**
     * Προσθέτει μια νέα αναφορά στη βάση δεδομένων.
     */
    public void addReport(CityReport report) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, report.getTitle());
        values.put(COLUMN_DESCRIPTION, report.getDescription());
        values.put(COLUMN_CATEGORY, report.getCategory());
        values.put(COLUMN_LATITUDE, report.getLatitude());
        values.put(COLUMN_LONGITUDE, report.getLongitude());

        db.insert(TABLE_REPORTS, null, values);
        db.close();
    }

    /**
     * Επιστρέφει μια λίστα με όλες τις αποθηκευμένες αναφορές.
     */
    public List<CityReport> getAllReports() {
        List<CityReport> reportList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_REPORTS + " ORDER BY " + COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            int titleIdx = cursor.getColumnIndex(COLUMN_TITLE);
            int descIdx = cursor.getColumnIndex(COLUMN_DESCRIPTION);
            int catIdx = cursor.getColumnIndex(COLUMN_CATEGORY);
            int latIdx = cursor.getColumnIndex(COLUMN_LATITUDE);
            int lonIdx = cursor.getColumnIndex(COLUMN_LONGITUDE);

            do {
                CityReport report = new CityReport(
                        cursor.getString(titleIdx),
                        cursor.getString(descIdx),
                        cursor.getString(catIdx),
                        cursor.getDouble(latIdx),
                        cursor.getDouble(lonIdx)
                );
                reportList.add(report);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return reportList;
    }
}
