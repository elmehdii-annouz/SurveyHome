package com.example.survey_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class VillesActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private LinearLayout villeContainer;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.villes_activity);

        villeContainer = findViewById(R.id.villeContainer);
        Button addVilleButton = findViewById(R.id.addVilleButton);

        // Initialize the database
        database = openOrCreateDatabase("VillesDB", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS villes(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");

        loadVillesFromDatabase();

        addVilleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateVilleDialog();
            }
        });


    }
    private void showCreateVilleDialog() {
        Typeface customFont = ResourcesCompat.getFont(this, R.font.roboto);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        // Inflate the custom dialog layout
        View dialogView = inflater.inflate(R.layout.dialog_create_ville, null);
        final EditText villeNameEditText = dialogView.findViewById(R.id.villeNameEditText);

        // Create the dialog
        AlertDialog dialog = builder.setView(dialogView)
                .setPositiveButton("OK", null) // Initially set null for custom handling later
                .setNegativeButton("Annuler", null)
                .create();

        // Set the custom title
        TextView title = new TextView(this);
        title.setText("Nom de la ville");
        title.setPadding(20, 20, 20, 20); // Padding for the title
        title.setGravity(Gravity.CENTER); // Center the title text
        title.setTextSize(20); // Set the title text size
        title.setTextColor(Color.WHITE);
        title.setTypeface(customFont);// Set the title text color to white

        // Apply the custom title
        dialog.setCustomTitle(title);

        // Show the dialog before further customizations
        dialog.show();

        // Customize dialog background programmatically
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Set transparent to handle all via Java
        }

        // Create a custom background drawable programmatically with rounded corners and outline
        GradientDrawable dialogBackground = new GradientDrawable();
        dialogBackground.setColor(Color.BLACK); // Set background color to black
        dialogBackground.setCornerRadius(24);   // Rounded corners
        dialogBackground.setStroke(4, Color.parseColor("#C0C0C0")); // White outline with 4dp width

        // Apply the custom background to the root view of the dialog
        dialog.getWindow().setBackgroundDrawable(dialogBackground);

        // Customize buttons and their actions
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String villeName = villeNameEditText.getText().toString().trim();

            if (villeName.isEmpty()) {
                Toast.makeText(VillesActivity.this, "Veuillez remplir le champ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isVilleNameExists(villeName)) {
                Toast.makeText(VillesActivity.this, "La ville existe déjà", Toast.LENGTH_SHORT).show();
                return;
            }

            long villeId = saveVilleToDatabase(villeName);
            addVilleToContainer(villeId, villeName);
            dialog.dismiss();
        });

        // Set button text color to white
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
    }

    private boolean isVilleNameExists(String villeName) {
        for (int i = 0; i < villeContainer.getChildCount(); i++) {
            View villeView = villeContainer.getChildAt(i);
            TextView villeNameTextView = villeView.findViewById(R.id.villeNameTextView);
            if (villeNameTextView != null && villeNameTextView.getText().toString().equalsIgnoreCase(villeName.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private void addVilleToContainer(final long villeId, String villeName) {
        // Check if villeName is empty or null
        if (villeName == null || villeName.trim().isEmpty()) {
            Toast.makeText(this, "Veuillez remplir le champ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Additional check to ensure the ville is not already added
        if (isVilleNameExists(villeName)) {
            Toast.makeText(this, "La ville existe déjà", Toast.LENGTH_SHORT).show();
            return;
        }

        final View villeView = getLayoutInflater().inflate(R.layout.ville_item, villeContainer, false);

        TextView villeNameTextView = villeView.findViewById(R.id.villeNameTextView);
        Button voirLesPlaquesButton = villeView.findViewById(R.id.voirLesPlaquesButton);
        Button deleteVilleButton = villeView.findViewById(R.id.deleteVilleButton);

        villeNameTextView.setText(villeName);

        deleteVilleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(villeId, villeView);
            }
        });

        // Add the new ville view at the top of the villeContainer
        villeContainer.addView(villeView, 0);  // Adding at index 0 to place it on top

        voirLesPlaquesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VillesActivity.this, PlaquesActivity.class);
                intent.putExtra("villeId", villeId);
                intent.putExtra("villeName", villeName);
                startActivity(intent);
            }
        });
    }
    private void showDeleteConfirmationDialog(final long villeId, final View villeView) {

        Typeface customFont = ResourcesCompat.getFont(this, R.font.roboto);

        // Create a custom TextView for the dialog title
        TextView title = new TextView(this);
        title.setText("Supprimer Ville");
        title.setPadding(20, 20, 20, 20); // Optional: Add padding
        title.setGravity(Gravity.CENTER); // Center the title
        title.setTextSize(20); // Set the size of the text
        title.setTypeface(customFont, Typeface.BOLD); // Apply custom font and make the text bold
        title.setTextColor(Color.WHITE);

        String villeName = ((TextView) villeView.findViewById(R.id.villeNameTextView))
                .getText().toString().trim();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCustomTitle(title)
                .setMessage("Êtes-vous sûr de vouloir supprimer cette ville ? ")
                .setPositiveButton("Oui", (dialogInterface, which) -> {
                    boolean villeDeleted = deleteVilleFromDatabase(villeId);

                    if (villeDeleted) {
                        // Initialize database helper if it's null
                        if (databaseHelper == null) {
                            databaseHelper = new DatabaseHelper(VillesActivity.this);
                        }

                        try {
                            databaseHelper.DeleteVille(villeName);
                            Toast.makeText(VillesActivity.this, "Ville et données associées supprimées", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(VillesActivity.this, "Erreur lors de la suppression des données: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        // Remove the ville view from the UI
                        villeContainer.removeView(villeView);
                    } else {
                        Toast.makeText(VillesActivity.this, "Erreur lors de la suppression de la ville", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Non", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            // Set background and border
            GradientDrawable background = new GradientDrawable();
            background.setColor(ContextCompat.getColor(this, android.R.color.black));
            background.setCornerRadius(20); // Corner radius in pixels
            background.setStroke(5, ContextCompat.getColor(this, R.color.darkblue)); // Blue border with width of 5 pixels

            dialog.getWindow().setBackgroundDrawable(background);

            // Set text color for title and message
            int whiteColor = Color.WHITE;
            try {
                // Use reflection to access the title TextView
                TextView titleView = (TextView) dialog.findViewById(getResources().getIdentifier("alertTitle", "id", "android"));
                if (titleView != null) {
                    titleView.setTextColor(whiteColor);
                    titleView.setTypeface(customFont);
                }

                // Access the message TextView
                TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                if (messageView != null) {
                    messageView.setTextColor(whiteColor);
                    messageView.setTypeface(customFont);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Set text color for the buttons
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
        });

        dialog.show();
    }


    private long saveVilleToDatabase(String villeName) {
        database.execSQL("INSERT INTO villes(name) VALUES('" + villeName + "')");
        Cursor cursor = database.rawQuery("SELECT last_insert_rowid()", null);
        long lastId = -1;
        if (cursor.moveToFirst()) {
            lastId = cursor.getLong(0);
        }
        cursor.close();
        return lastId;
    }

    private boolean deleteVilleFromDatabase(long villeId) {
        database.execSQL("DELETE FROM villes WHERE id=" + villeId);
        return true;
    }

    private void loadVillesFromDatabase() {
        Cursor cursor = database.rawQuery("SELECT * FROM villes", null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long villeId = cursor.getLong(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String villeName = cursor.getString(cursor.getColumnIndex("name"));
                addVilleToContainer(villeId, villeName);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
