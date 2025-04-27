package com.example.survey_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Name and Version
    private static final String DATABASE_NAME = "survey_app.db";
    private static final int DATABASE_VERSION = 3; // Incremented version

    // Table Name
    private static final String TABLE_NAME = "survey_data";

    // Table Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_RUE = "rue";
    private static final String COLUMN_NUM_IMM = "num_imm";
    private static final String COLUMN_NBR_B2C = "nbr_b2c";
    private static final String COLUMN_NBR_B2B = "nbr_b2b";
    private static final String COLUMN_LARGEUR_TROTOIRE_ML = "largeur_trotoire_ml";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_TYPE_IMMEUBLE = "type_immeuble";
    private static final String COLUMN_NBR_ETAGES = "nbr_etages";
    private static final String COLUMN_BOITIER = "boitier";
    private static final String COLUMN_REMARQUE = "remarque";
    private static final String COLUMN_ZONE = "zone";
    private static final String COLUMN_SOUS_SOL = "sous_sol";
    private static final String COLUMN_TOTAL_CLIENTS = "total_clients";
    private static final String COLUMN_PLAQUENAME = "plaqueName";
    private static final String COLUMN_NAMEVILLE = "nameVille"; // New column

    // Create Table SQL Query
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_RUE + " TEXT, " +
                    COLUMN_NUM_IMM + " TEXT, " +
                    COLUMN_NBR_B2C + " TEXT, " +
                    COLUMN_NBR_B2B + " TEXT, " +
                    COLUMN_LARGEUR_TROTOIRE_ML + " TEXT, " +
                    COLUMN_LATITUDE + " TEXT, " +
                    COLUMN_LONGITUDE + " TEXT, " +
                    COLUMN_TYPE_IMMEUBLE + " TEXT, " +
                    COLUMN_NBR_ETAGES + " TEXT, " +
                    COLUMN_BOITIER + " TEXT, " +
                    COLUMN_REMARQUE + " TEXT, " +
                    COLUMN_ZONE + " TEXT, " +
                    COLUMN_SOUS_SOL + " TEXT, " +
                    COLUMN_PLAQUENAME + " TEXT, " +
                    COLUMN_NAMEVILLE + " TEXT, " +
                    COLUMN_TOTAL_CLIENTS + " TEXT);"; // Added total_clients

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_TOTAL_CLIENTS + " TEXT");
        }
    }

    public long insertData(String rue, String nonImm, String nbrB2C, String nbrB2B,
                           String largeurTrotoireML, String latitude, String longitude, String typeImmeuble,
                           String nbrEtages, String boitier, String remarque, String zone, String sousSole,String plaqueName,String nameVille) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Calculate total_clients as the sum of nbr_b2c and nbr_b2b
        int totalClients = Integer.parseInt(nbrB2C) + Integer.parseInt(nbrB2B);
        values.put(COLUMN_NAMEVILLE,nameVille);
        values.put(COLUMN_PLAQUENAME,plaqueName);
        values.put(COLUMN_RUE, rue);
        values.put(COLUMN_NUM_IMM, nonImm);
        values.put(COLUMN_NBR_B2C, nbrB2C);
        values.put(COLUMN_NBR_B2B, nbrB2B);
        values.put(COLUMN_LARGEUR_TROTOIRE_ML, largeurTrotoireML);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_TYPE_IMMEUBLE, typeImmeuble);
        values.put(COLUMN_NBR_ETAGES, nbrEtages);
        values.put(COLUMN_BOITIER, boitier);
        values.put(COLUMN_REMARQUE, remarque);
        values.put(COLUMN_ZONE, zone);
        values.put(COLUMN_SOUS_SOL, sousSole);
        values.put(COLUMN_TOTAL_CLIENTS, String.valueOf(totalClients));

        return db.insert(TABLE_NAME, null, values);
    }


    public void truncateTable(String plaqueName) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();

            String whereClause = "plaqueName = ?";
            String[] whereArgs = new String[] { plaqueName };

            // Delete rows matching the plaque name
            db.delete(TABLE_NAME, whereClause, whereArgs);

            // Reset the auto-increment value
            db.execSQL("DELETE FROM sqlite_sequence WHERE name = '" + TABLE_NAME + "';");

            db.setTransactionSuccessful(); // Mark the transaction as successful
        } catch (Exception e) {
            e.printStackTrace(); // Log any exceptions
        } finally {
            db.endTransaction(); // End the transaction
        }
    }

    public void DeleteVille(String villeName) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Define the whereClause and whereArgs
            String whereClause = "nameVille = ?";
            String[] whereArgs = new String[] { villeName };

            // Execute the delete query
            db.delete(TABLE_NAME, whereClause, whereArgs);
            db.execSQL("DELETE FROM sqlite_sequence WHERE name = '" + TABLE_NAME + "';");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace(); // Log any exceptions
        } finally {
            db.close(); // Close the database
        }
    }

    public Cursor getDataByPlaqueName(String plaqueName) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM survey_data WHERE plaqueName = ?", new String[]{plaqueName});
    }


    public boolean isEntryExists(String rue, String latitude, String longitude) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM survey_data WHERE rue = ? AND ABS(latitude - ?) < 0.0001 AND ABS(longitude - ?) < 0.0001";
        Cursor cursor = db.rawQuery(query, new String[]{rue, latitude, longitude});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

}
