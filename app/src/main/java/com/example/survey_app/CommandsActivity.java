package com.example.survey_app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CommandsActivity extends AppCompatActivity {


    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;
    private TableLayout tableLayout;
    private static final String TAG = "CommandsActivity";
    private static final String CHANNEL_ID = "notification_channel";
    private Uri fileUri;  // Uri to store the file location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commmands_opt_activity);
        String plaqueName = getIntent().getStringExtra("plaqueName");
        String villeName = getIntent().getStringExtra("villeName");
        String survey="Survey";
        TextView plaqueNameTextView = findViewById(R.id.plaqueNameTextView);

        // Set the plaque name in the TextView with "Plaque" prefix
        if (plaqueName != null) {
            plaqueNameTextView.setText(villeName.toUpperCase()+ " - Plaque " + plaqueName); // Prepend "Plaque"
        } else {
            plaqueNameTextView.setText("Plaque"); // Default text in case plaqueName is null
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Your custom back press logic
                Intent intent = new Intent(CommandsActivity.this, MainActivity.class);

                // Set flags to clear the back stack and start MainActivity as a new task
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                // Start MainActivity
                startActivity(intent);

                // Finish CommandsActivity to prevent it from being accessed via back button again
                finish();

                // No need to call super.onBackPressed() since we're handling the navigation ourselves
            }
        });
        tableLayout = findViewById(R.id.tableLayout);
        databaseHelper = new DatabaseHelper(this);
        Button homebutton = findViewById(R.id.roundButton);

        loadDataIntoTable();

        Button terrainSurveyButton = findViewById(R.id.buttonTerrainSurvey);
        terrainSurveyButton.setOnClickListener(v -> {
            Intent intent = new Intent(CommandsActivity.this, MainActivity.class);
            intent.putExtra("plaqueName", plaqueName);
            intent.putExtra("villeName", villeName);
            startActivity(intent);
        });

        Button generateKmzButton = findViewById(R.id.buttonGenerateKmz);
        generateKmzButton.setOnClickListener(v -> promptForFileName("KMZ", "application/vnd.google-earth.kmz",survey, villeName, plaqueName));

        Button generateXlsxButton = findViewById(R.id.buttonGenerateXlsx);
        generateXlsxButton.setOnClickListener(v -> promptForFileName("Excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",survey, villeName, plaqueName));

        Button deleteButton = findViewById(R.id.buttonDelete);
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());


        homebutton.setOnClickListener(v -> {
            Intent intent = new Intent(CommandsActivity.this, VillesActivity.class);
            intent.putExtra("plaqueName", plaqueName);
            intent.putExtra("villeName", villeName);
            startActivity(intent);
        });



    }


    @SuppressLint("Range")
    private void loadDataIntoTable() {
        // Clear the existing rows from the table layout
        tableLayout.removeAllViews();

        String plaqueName = getIntent().getStringExtra("plaqueName");
        Cursor cursor = databaseHelper.getDataByPlaqueName(plaqueName);

        if (cursor != null && cursor.getCount() > 0) {
            // Create and add column headers if there's data
            TableRow headerRow = new TableRow(this);
            String[] columnNames = {"ID", "Latitude", "Longitude", "B2C", "B2B", "Total Clients", "Rue"};

            for (String columnName : columnNames) {
                TextView headerTextView = new TextView(this);
                headerTextView.setText(columnName);
                headerTextView.setPadding(8, 8, 8, 8); // Optional: Add padding for better spacing
                headerTextView.setTextColor(Color.WHITE); // Set text color to white
                headerTextView.setBackgroundColor(Color.BLACK); // Set background color to black for outline
                headerTextView.setGravity(Gravity.CENTER); // Center the text
                headerTextView.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1f
                ));

                headerRow.addView(headerTextView);
            }
            headerRow.setBackgroundColor(Color.BLACK); // Set background color for the header row
            tableLayout.addView(headerRow);

            // Populate data rows
            int idIndex = cursor.getColumnIndex("id");
            int latitudeIndex = cursor.getColumnIndex("latitude");
            int longitudeIndex = cursor.getColumnIndex("longitude");
            int rueIndex = cursor.getColumnIndex("rue");
            int nbrB2CIndex = cursor.getColumnIndex("nbr_b2c");
            int nbrB2BIndex = cursor.getColumnIndex("nbr_b2b");
            int totalClientsIndex = cursor.getColumnIndex("total_clients");

            while (cursor.moveToNext()) {
                TableRow tableRow = new TableRow(this);

                TextView idTextView = new TextView(this);
                idTextView.setText(cursor.getString(idIndex));
                styleCell(idTextView);
                tableRow.addView(idTextView);

                TextView latitudeTextView = new TextView(this);
                latitudeTextView.setText(cursor.getString(latitudeIndex));
                styleCell(latitudeTextView);
                tableRow.addView(latitudeTextView);

                TextView longitudeTextView = new TextView(this);
                longitudeTextView.setText(cursor.getString(longitudeIndex));
                styleCell(longitudeTextView);
                tableRow.addView(longitudeTextView);

                TextView nbrB2CTextView = new TextView(this);
                nbrB2CTextView.setText(cursor.getString(nbrB2CIndex));
                styleCell(nbrB2CTextView);
                tableRow.addView(nbrB2CTextView);

                TextView nbrB2BTextView = new TextView(this);
                nbrB2BTextView.setText(cursor.getString(nbrB2BIndex));
                styleCell(nbrB2BTextView);
                tableRow.addView(nbrB2BTextView);

                TextView totalClientsTextView = new TextView(this);
                totalClientsTextView.setText(cursor.getString(totalClientsIndex));
                styleCell(totalClientsTextView);
                tableRow.addView(totalClientsTextView);

                TextView rueTextView = new TextView(this);
                rueTextView.setText(cursor.getString(rueIndex));
                styleCell(rueTextView);
                tableRow.addView(rueTextView);

                tableRow.setBackgroundColor(Color.BLACK); // Set background color for the row
                // Insert the new row at the top, after the header row
                tableLayout.addView(tableRow, 1);
            }
            cursor.close();
        } else {
            enableSaveButtons(false);
        }
    }



    // Helper method to style each cell
    private void styleCell(TextView textView) {
        // Load the custom font from the resources
        Typeface customFont = ResourcesCompat.getFont(this, R.font.roboto); // Change 'roboto' to your font file name without extension

        textView.setPadding(8, 8, 8, 8);
        textView.setTextColor(Color.WHITE);
        textView.setBackgroundColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER); // Center the text

        // Apply the custom font to the TextView
        textView.setTypeface(customFont);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
        );
        params.setMargins(1, 1, 1, 1);
        textView.setLayoutParams(params);
    }

    private void enableSaveButtons(boolean enabled) {
        Button generateKmzButton = findViewById(R.id.buttonGenerateKmz);
        Button generateXlsxButton = findViewById(R.id.buttonGenerateXlsx);

        generateKmzButton.setEnabled(enabled);
        generateXlsxButton.setEnabled(enabled);
    }
    private void showDeleteConfirmationDialog() {
        Typeface customFont = ResourcesCompat.getFont(this, R.font.roboto);
        TextView title = new TextView(this);
        title.setText("Supprimer Données");
        title.setPadding(20, 20, 20, 20); // Optional: Add padding
        title.setGravity(Gravity.CENTER); // Center the title
        title.setTextSize(20); // Set the size of the text
        title.setTypeface(customFont, Typeface.BOLD); // Apply custom font and make the text bold
        title.setTextColor(Color.WHITE);
        String plaqueName = getIntent().getStringExtra("plaqueName");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCustomTitle(title)
                .setMessage("\nEtes-vous sûr de vouloir supprimer la table ?")
                .setPositiveButton("Oui", (dialogInterface, which) -> {
                    databaseHelper.truncateTable(plaqueName);
                    refreshTable();
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


    private void refreshTable() {
        loadDataIntoTable(); // Reload the data
    }

    private void promptForFileName(String fileType, String mimeType, String Survey, String villeName, String plaqueName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create a custom drawable with a black background and blue corners
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.BLACK); // Set the background color to black
        background.setCornerRadius(20); // Set the corner radius to make the corners rounded
        background.setStroke(5, Color.parseColor("#010C40")); // Blue border with a width of 5 pixels

        // Inflate your custom view for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_file_name, null);
        builder.setView(dialogView);

        final EditText input = dialogView.findViewById(R.id.dialog_input);

        // Set the default file name
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = dateFormat.format(new Date());
        String defaultFileName = Survey + " " + villeName + "-" + plaqueName + " (" + currentDate + ")";
        input.setText(defaultFileName);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String fileName = input.getText().toString().trim();
            if (!fileName.isEmpty()) {
                if (fileType.equals("Excel")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        saveExcelFileUsingMediaStore(fileName);
                    } else {
                        Toast.makeText(this, "Cette fonctionnalité n'est pas prise en charge sur votre version Android.", Toast.LENGTH_SHORT).show();
                    }
                } else if (fileType.equals("KMZ")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        generateKmzFile(fileName);
                    }
                }
            } else {
                Toast.makeText(this, "Le nom du fichier ne peut pas être vide.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            // Set the custom drawable as the background of the dialog
            dialog.getWindow().setBackgroundDrawable(background);

            // Access and set the button colors
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(Color.BLUE); // Set the text color of the positive button
            }

            if (negativeButton != null) {
                negativeButton.setTextColor(Color.BLUE); // Set the text color of the negative button
            }
        });

        dialog.show();
    }

    @SuppressLint("Range")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void saveExcelFileUsingMediaStore(String fileName) {
        String plaqueName = getIntent().getStringExtra("plaqueName");
        ContentResolver resolver = getContentResolver();
        ContentValues contentValues = new ContentValues();

        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + ".xlsx");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Surveys");

        Uri fileUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);

        if (fileUri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(fileUri)) {
                if (outputStream != null) {
                    XSSFWorkbook workbook = new XSSFWorkbook();
                    org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Survey Data");

                    // Create header row
                    Row headerRow = sheet.createRow(0);
                    String[] columns = {
                            "ID", "Rue", "Num Imm", "Nbr B2C", "Nbr B2B", "Total Clients",
                            "Largeur Trotoire ML", "Latitude", "Longitude", "Type Immeuble",
                            "Nbr Etages", "Boitier", "Remarque", "Zone", "Sous Sol"
                    };

                    // Create a CellStyle for the header row with a red background
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
                    headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

                    for (int i = 0; i < columns.length; i++) {
                        Cell headerCell = headerRow.createCell(i);
                        headerCell.setCellValue(columns[i]);
                        headerCell.setCellStyle(headerCellStyle);
                    }

                    // Populate data rows
                    Cursor cursor = databaseHelper.getDataByPlaqueName(plaqueName);
                    int rowNum = 1;
                    if (cursor != null && cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {
                            Row row = sheet.createRow(rowNum++);

                            for (int i = 0; i < columns.length; i++) {
                                Cell cell = row.createCell(i);
                                cell.setCellValue(cursor.getString(cursor.getColumnIndex(columns[i].toLowerCase().replace(" ", "_"))));
                            }
                        }
                        cursor.close();
                    }

                    workbook.write(outputStream);
                    workbook.close();

                    // Show the custom dialog to open the file
                    showExcelSavedDialog(fileUri);

                }
            } catch (IOException e) {
                Log.e(TAG, "Error writing Excel file", e);
                Toast.makeText(this, "Erreur lors de l'enregistrement du fichier Excel", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Échec de la création du fichier.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExcelSavedDialog(Uri fileUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        Typeface robotoTypeface = Typeface.createFromAsset(getAssets(), "fonts/roboto.ttf");
        Typeface robotoTitle = Typeface.createFromAsset(getAssets(), "fonts/robotobold.ttf");
        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        layout.setBackgroundColor(Color.parseColor("#006400"));

        TextView title = new TextView(this);
        title.setText("Fichier Excel Sauvegardé");
        title.setTypeface(robotoTitle);
        title.setTextSize(23);
        title.setTextColor(Color.WHITE);
        title.setPadding(16, 16, 16, 16);

        title.setGravity(View.TEXT_ALIGNMENT_CENTER); // Center the title
        layout.addView(title);

        // Create a TextView for the dialog message
        TextView message = new TextView(this);
        message.setText("Le fichier Excel a été sauvegardé Dans Documents/Surveys. Voulez-vous ouvrir le fichier ?");
        message.setTextColor(Color.WHITE);
        message.setTextSize(16);
        message.setTypeface(robotoTypeface);
        layout.addView(message);

        builder.setView(layout);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#006400")); // Set the background color to black
        background.setCornerRadius(20); // Set the corner radius to make the corners rounded
        background.setStroke(5, Color.parseColor("#006400")); // Gray border with a width of 5 pixels

        builder.setPositiveButton("Ouvrir", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Aucune application trouvée pour ouvrir ce fichier", Toast.LENGTH_SHORT).show();


            }

        });

        // Set up the negative button with the "Annuler" action
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            // Set the custom drawable as the background of the dialog
            dialog.getWindow().setBackgroundDrawable(background);

            // Access and set the button colors
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(Color.BLACK); // Set the text color of the positive button
            }

            if (negativeButton != null) {
                negativeButton.setTextColor(Color.BLACK); // Set the text color of the negative button
            }
        });

        dialog.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void generateKmzFile(String fileName) {
        String plaqueName = getIntent().getStringExtra("plaqueName");
        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + ".kmz");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.google-earth.kmz");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Surveys");

        Uri uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
        if (uri != null) {
            try (OutputStream outputStream = contentResolver.openOutputStream(uri);
                 ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

                String kmlContent = createKmlContent();
                ZipEntry kmlEntry = new ZipEntry("doc.kml");
                zipOutputStream.putNextEntry(kmlEntry);

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zipOutputStream))) {
                    writer.write(kmlContent);
                }
                zipOutputStream.closeEntry();

                Toast.makeText(this, "Fichier KMZ sauvegardé dans Documents/Surveys", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {

                showKmzFileSavedDialog(uri);
            }
        } else {
            Toast.makeText(this, "Échec de la création du fichier KMZ.", Toast.LENGTH_SHORT).show();
        }
    }

    private String createKmlContent() {
        String plaqueName = getIntent().getStringExtra("plaqueName");
        StringBuilder kmlBuilder = new StringBuilder();
        kmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
                .append("<Document>\n")
                .append("<name>Survey Data</name>\n");

        Cursor cursor = databaseHelper.getDataByPlaqueName(plaqueName);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String rue = escapeXml(cursor.getString(cursor.getColumnIndex("rue")));
                @SuppressLint("Range") String typeImmeuble = escapeXml(cursor.getString(cursor.getColumnIndex("type_immeuble")));
                @SuppressLint("Range") String nbrEtages = escapeXml(cursor.getString(cursor.getColumnIndex("nbr_etages")));
                @SuppressLint("Range") String totalClients = escapeXml(cursor.getString(cursor.getColumnIndex("total_clients")));
                @SuppressLint("Range") String largeurTrotoire = escapeXml(cursor.getString(cursor.getColumnIndex("largeur_trotoire_ml")));
                @SuppressLint("Range") String remarque = escapeXml(cursor.getString(cursor.getColumnIndex("remarque")));
                @SuppressLint("Range") String boitier = escapeXml(cursor.getString(cursor.getColumnIndex("boitier")));
                @SuppressLint("Range") String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                @SuppressLint("Range") String longitude = cursor.getString(cursor.getColumnIndex("longitude"));

                // Log coordinates for debugging
                Log.d(TAG, "Coordinates for Placemark: " + longitude + ", " + latitude);

                String description = String.format(
                        "<b>Rue:</b> %s<br>" +
                                "<b>Type Immeuble:</b> %s<br>" +
                                "<b>Nombre d'Étages:</b> %s<br>" +
                                "<b>Total Clients:</b> %s<br>" +
                                "<b>Largeur Trotoire (ML):</b> %s<br>" +
                                "<b>Remarque:</b> %s<br>" +
                                "<b>Boitier:</b> %s",
                        rue, typeImmeuble, nbrEtages, totalClients, largeurTrotoire, remarque, boitier
                );

                kmlBuilder.append("<Placemark>\n")
                        .append("<name>").append(escapeXml(id)).append("</name>\n")
                        .append("<description><![CDATA[").append(description).append("]]></description>\n")
                        .append("<Point>\n")
                        .append("<coordinates>")
                        .append(longitude).append(",").append(latitude).append("</coordinates>\n") // Ensure correct order
                        .append("</Point>\n")
                        .append("</Placemark>\n");
            }
            cursor.close();
        }

        kmlBuilder.append("</Document>\n")
                .append("</kml>");

        return kmlBuilder.toString();
    }



    // Utility function to convert DMS to Decimal Degrees
  
    // Helper method to escape XML special characters
    private String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }



    private void showKmzFileSavedDialog(Uri uri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        Typeface robotoTypeface = Typeface.createFromAsset(getAssets(), "fonts/roboto.ttf");
        Typeface robotoTitle = Typeface.createFromAsset(getAssets(), "fonts/robotobold.ttf");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        layout.setBackgroundColor(Color.GRAY);


        TextView title = new TextView(this);
        title.setText("Fichier KMZ Sauvegardé");
        title.setTypeface(robotoTitle);
        title.setTextSize(23);
        title.setTextColor(Color.WHITE);
        title.setPadding(16, 16, 16, 16);
        title.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(title);


        TextView message = new TextView(this);
        message.setText("Le fichier KMZ a été sauvegardé Dans Documents/Surveys . Voulez-vous ouvrir le fichier ?");
        message.setTextColor(Color.WHITE);
        message.setTextSize(16);
        message.setTypeface(robotoTypeface);
        layout.addView(message);

        builder.setView(layout);


        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.GRAY); // Set the background color to black
        background.setCornerRadius(20); // Set the corner radius to make the corners rounded
        background.setStroke(5, Color.GRAY); // Blue border with a width of 5 pixels

        builder.setPositiveButton("Ouvrir", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.google-earth.kmz");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Aucune application trouvée pour ouvrir ce fichier", Toast.LENGTH_SHORT).show();


            }
        });

        // Set up the negative button with the "Annuler" action
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            // Set the custom drawable as the background of the dialog
            dialog.getWindow().setBackgroundDrawable(background);

            // Access and set the button colors
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(Color.BLACK); // Set the text color of the positive button
            }

            if (negativeButton != null) {
                negativeButton.setTextColor(Color.BLACK); // Set the text color of the negative button
            }
        });

        dialog.show();
    }





}
