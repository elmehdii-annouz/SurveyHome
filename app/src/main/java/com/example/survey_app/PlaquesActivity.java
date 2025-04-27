package com.example.survey_app;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
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

public class PlaquesActivity extends AppCompatActivity {
    private DatabaseHelper databasehelper;
    private LinearLayout plaqueContainer;
    private SQLiteDatabase database;
    private long villeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plaques);

        plaqueContainer = findViewById(R.id.plaqueContainer);
        Button addPlaqueButton = findViewById(R.id.addPlaqueButton);

        // Get the Ville ID passed from VillesActivity
        villeId = getIntent().getLongExtra("villeId", -1);

        database = openOrCreateDatabase("VillesDB", MODE_PRIVATE, null);

        // Create the 'plaques' table if it doesn't exist
        String createTableQuery = "CREATE TABLE IF NOT EXISTS plaques (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ville_id INTEGER, " +
                "name TEXT, " +
                "FOREIGN KEY(ville_id) REFERENCES villes(id));";
        database.execSQL(createTableQuery);

        loadPlaquesFromDatabase();

        addPlaqueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreatePlaqueDialog();
            }
        });
    }

    private void showCreatePlaqueDialog() {
        // Load custom font if required (replace 'roboto' with your desired font resource)
        Typeface customFont = ResourcesCompat.getFont(this, R.font.roboto);

        // Create AlertDialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        // Inflate the custom dialog layout
        View dialogView = inflater.inflate(R.layout.dialog_create_plaque, null);
        final EditText plaqueNameEditText = dialogView.findViewById(R.id.plaqueNameEditText);

        // Create the dialog
        AlertDialog dialog = builder.setView(dialogView)
                .setPositiveButton("OK", null) // Set null to handle the click event manually
                .setNegativeButton("Annuler", null)
                .create();

        // Set the custom title
        TextView title = new TextView(this);
        title.setText("Nom de la plaque");
        title.setPadding(20, 20, 20, 20); // Set padding for the title
        title.setGravity(Gravity.CENTER); // Center the title text
        title.setTextSize(20); // Set the title text size
        title.setTextColor(Color.WHITE); // Set the title text color to white
        title.setTypeface(customFont); // Set custom font for the title

        // Apply the custom title to the dialog
        dialog.setCustomTitle(title);

        // Show the dialog before further customization
        dialog.show();

        // Set a custom background drawable for the dialog with rounded corners and an outline
        if (dialog.getWindow() != null) {
            GradientDrawable dialogBackground = new GradientDrawable();
            dialogBackground.setColor(Color.BLACK); // Set the background color to black
            dialogBackground.setCornerRadius(24); // Rounded corners
            dialogBackground.setStroke(4, Color.parseColor("#C0C0C0")); // Gray outline with 4dp width

            // Apply the custom background to the dialog window
            dialog.getWindow().setBackgroundDrawable(dialogBackground);
        }

        // Customize buttons and their actions
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String plaqueName = plaqueNameEditText.getText().toString().trim();

            // Check if plaqueName is empty or already exists
            if (plaqueName.isEmpty()) {
                Toast.makeText(PlaquesActivity.this, "Veuillez remplir le champ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isPlaqueNameExists("Plaque " + plaqueName)) { // Check with prefix in UI
                Toast.makeText(PlaquesActivity.this, "La plaque existe déjà", Toast.LENGTH_SHORT).show();
                return;
            }

            long plaqueId = savePlaqueToDatabase(plaqueName); // Save without prefix
            addPlaqueToContainer(plaqueId, plaqueName);
            dialog.dismiss();
        });

        // Set the button text colors to white
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
    }

    private boolean isPlaqueNameExists(String plaqueName) {
        for (int i = 0; i < plaqueContainer.getChildCount(); i++) {
            View plaqueView = plaqueContainer.getChildAt(i);
            TextView plaqueNameTextView = plaqueView.findViewById(R.id.plaqueNameTextView);
            if (plaqueNameTextView != null && plaqueNameTextView.getText().toString().equalsIgnoreCase(plaqueName.trim())) {
                return true;
            }
        }
        return false;
    }


    private void addPlaqueToContainer(final long plaqueId, String plaqueName) {
        // Check if plaqueName is empty or null
        if (plaqueName == null || plaqueName.trim().isEmpty()) {
            Toast.makeText(this, "Veuillez remplir le champ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepend "Plaque" only when displaying
        String displayName = "Plaque " + plaqueName.trim();

        // Additional check to ensure the plaque is not already added
        if (isPlaqueNameExists(displayName)) {
            Toast.makeText(this, "La plaque existe déjà", Toast.LENGTH_SHORT).show();
            return;
        }

        final View plaqueView = getLayoutInflater().inflate(R.layout.plaque_item, plaqueContainer, false);

        TextView plaqueNameTextView = plaqueView.findViewById(R.id.plaqueNameTextView);
        Button deletePlaqueButton = plaqueView.findViewById(R.id.deletePlaqueButton);
        Button surveyButton = plaqueView.findViewById(R.id.surveyButton);

        plaqueNameTextView.setText(displayName); // Use display name

        deletePlaqueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(plaqueId, plaqueView);
            }
        });

        surveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String villeName = getIntent().getStringExtra("villeName");
                Intent intent = new Intent(PlaquesActivity.this, MainActivity.class);
                intent.putExtra("villeName", villeName);
                intent.putExtra("plaqueName", plaqueName.trim()); // Pass raw name without "Plaque"
                startActivity(intent);
            }
        });

        plaqueContainer.addView(plaqueView, 0);  // Add to the top of the container
    }


    private void showDeleteConfirmationDialog(final long plaqueId, final View plaqueView) {

        Typeface customFont = ResourcesCompat.getFont(this, R.font.roboto);

        // Create a custom TextView for the dialog title
        TextView title = new TextView(this);
        title.setText("Supprimer Plaque");
        title.setPadding(20, 20, 20, 20); // Optional: Add padding
        title.setGravity(Gravity.CENTER); // Center the title
        title.setTextSize(20); // Set the size of the text
        title.setTypeface(customFont, Typeface.BOLD); // Apply custom font and make the text bold
        title.setTextColor(Color.WHITE);
        String plaqueName = ((TextView) plaqueView.findViewById(R.id.plaqueNameTextView))
                .getText().toString().replace("Plaque ", "").trim();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCustomTitle(title)
                .setMessage("Êtes-vous sûr de vouloir supprimer cette plaque ?")
                .setPositiveButton("Oui", (dialogInterface, which) -> {
                    boolean plaqueDeleted = deletePlaqueFromDatabase(plaqueId);

                    if (plaqueDeleted) {
                        if (databasehelper == null) {
                            databasehelper = new DatabaseHelper(PlaquesActivity.this);
                        }

                        try {
                            databasehelper.truncateTable(plaqueName);
                            Toast.makeText(PlaquesActivity.this, "Plaque et données associées supprimées", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(PlaquesActivity.this, "Erreur lors de la suppression des données: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        // Remove the plaque view from the UI
                        plaqueContainer.removeView(plaqueView);
                    } else {
                        Toast.makeText(PlaquesActivity.this, "Erreur lors de la suppression de la plaque", Toast.LENGTH_SHORT).show();
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

    private boolean deletePlaqueFromDatabase(long plaqueId) {
        try {
            database.execSQL("DELETE FROM plaques WHERE id=" + plaqueId);
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "Erreur de suppression dans la base de données: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
    private long savePlaqueToDatabase(String plaqueName) {
        database.execSQL("INSERT INTO plaques(ville_id, name) VALUES(" + villeId + ", '" + plaqueName + "')");
        Cursor cursor = database.rawQuery("SELECT last_insert_rowid()", null);
        long lastId = -1;
        if (cursor.moveToFirst()) {
            lastId = cursor.getLong(0);
        }
        cursor.close();
        return lastId;
    }



    private void loadPlaquesFromDatabase() {
        Cursor cursor = database.rawQuery("SELECT * FROM plaques WHERE ville_id=" + villeId, null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long plaqueId = cursor.getLong(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String plaqueName = cursor.getString(cursor.getColumnIndex("name"));
                addPlaqueToContainer(plaqueId, plaqueName);
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
