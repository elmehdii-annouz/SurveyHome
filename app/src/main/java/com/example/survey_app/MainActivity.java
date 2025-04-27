package com.example.survey_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText inputRue, inputNumImm, inputNbrB2C, inputNbrB2B, inputLargeurTrotoireML, inputLatitude, inputLongitude;
    private Spinner inputTypeImmeuble, inputNbrEtages, inputBoitier, inputRemarque, inputZone, inputSousSol;
    private Button saveButton;
    private EditText plaqueEditText;
    private EditText villeEditText;
    private WebView mapView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 1001;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Initialize views
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mapView = findViewById(R.id.map_view);
        inputRue = findViewById(R.id.input_rue);
        inputNumImm = findViewById(R.id.input_num_imm);
        inputNbrB2C = findViewById(R.id.input_nbr_b2c);
        inputNbrB2B = findViewById(R.id.input_nbr_b2b);
        inputLargeurTrotoireML = findViewById(R.id.input_largeur_trotoire_ml);
        inputLatitude = findViewById(R.id.input_latitude);
        inputLongitude = findViewById(R.id.input_longitude);
        plaqueEditText = findViewById(R.id.plaquename);
        villeEditText =  findViewById(R.id.villename);
        saveButton = findViewById(R.id.save_button);

        String villeName = getIntent().getStringExtra("villeName");
        String plaque = getIntent().getStringExtra("plaqueName");

        if (plaque != null) {
            plaqueEditText.setText(plaque);
        }

        if(villeName != null) {
            villeEditText.setText(villeName);
        }

        // Set up the web view
        mapView.getSettings().setJavaScriptEnabled(true);
        mapView.setWebViewClient(new WebViewClient());

        // Initialize animations and apply to views
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        inputLatitude.startAnimation(fadeInAnimation);
        inputLongitude.startAnimation(fadeInAnimation);

        // Initialize the spinners and set up adapters
        inputTypeImmeuble = findViewById(R.id.input_type_immeuble);
        inputNbrEtages = findViewById(R.id.input_nbr_etages);
        inputBoitier = findViewById(R.id.input_boitier);
        inputRemarque = findViewById(R.id.input_remarque);
        inputZone = findViewById(R.id.input_zone);
        inputSousSol = findViewById(R.id.input_sous_sol);

        setupSpinner(inputTypeImmeuble, R.array.type_immeuble_options);
        setupSpinner(inputNbrEtages, R.array.nbr_etages_options);
        setupSpinner(inputBoitier, R.array.boitier_options);
        setupSpinner(inputRemarque, R.array.remarque_options);
        setupSpinner(inputZone, R.array.zone_options);
        setupSpinner(inputSousSol, R.array.sous_sol_options);

        // Initialize the DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        checkLocationSettings();
        getLocation();

        // Set up the swipe refresh layout listener to refresh location
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh the location and street name
                getLocation();
                // Stop the refreshing animation once done
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Set up the save button listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    saveData();
                } else {
                    Toast.makeText(MainActivity.this, "Veuillez remplir tous les champs obligatoires.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up a listener for the list button to navigate to another activity
        Button listButton = findViewById(R.id.list);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CommandsActivity.class);
                intent.putExtra("plaqueName", plaque);
                intent.putExtra("villeName", villeName);
                startActivity(intent);
            }
        });
    }

    private void setupSpinner(Spinner spinner, int arrayResource) {
        List<String> items = Arrays.asList(getResources().getStringArray(arrayResource));
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void getLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Get last known location
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                        // Log raw latitude and longitude values here
                        Log.d(TAG, "Raw Latitude: " + location.getLatitude());
                        Log.d(TAG, "Raw Longitude: " + location.getLongitude());

                        updateLocationUI(location);
                    } else {
                        // If last known location is null, request location updates
                        requestLocationUpdates();
                    }
                }
            });

        } else {
            // Request location permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }





    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    5000L  // Reduced to 5 seconds for faster high-accuracy updates
            )
                    .setMinUpdateIntervalMillis(2000L) // Minimum interval of 2 seconds
                    .setMaxUpdateDelayMillis(10000L)  // Maximum delay of 10 seconds
                    .build();

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        } else {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION_CODE);
        }
    }


    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if (locationResult != null) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateLocationUI(location);
                }
            }
        }
    };
    private void checkLocationSettings() {
        try {
            int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (locationMode != Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) {
                // Location mode is not set to high accuracy, prompt user to enable it
                Toast.makeText(this, "Please enable High Accuracy Location Mode", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            } else {

                getLocation();
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void updateLocationUI(Location location) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#######"); // Format for 7 decimal places
        String formattedLatitude = decimalFormat.format(location.getLatitude());
        String formattedLongitude = decimalFormat.format(location.getLongitude());

        inputLatitude.setText(formattedLatitude);
        inputLongitude.setText(formattedLongitude);
        fetchStreetName(location.getLatitude(), location.getLongitude());
        displayMap(location.getLatitude(), location.getLongitude());
    }

    private void displayMap(double latitude, double longitude) {
        String mapUrl = "https://www.openstreetmap.org/export/embed.html?bbox=" +
                (longitude - 0.005) + "%2C" + (latitude - 0.005) + "%2C" + (longitude + 0.005) + "%2C" + (latitude + 0.005) +
                "&layer=mapnik&marker=" + latitude + "%2C" + longitude;

        mapView.loadUrl(mapUrl);
    }

    private void fetchStreetName(double latitude, double longitude) {
        // Using Geocode.xyz API for reverse geocoding without requiring a token
        String url = "https://geocode.xyz/" + latitude + "," + longitude + "?geoit=json";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Geocode.xyz API call failed: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);

                        // Explicitly check for error or invalid responses
                        String errorMessage = jsonObject.optString("error", "");
                        String streetAddress = jsonObject.optString("staddress", "").trim();

                        // Check if the response contains typical throttling or error indicators
                        if (!errorMessage.isEmpty() ||
                                streetAddress.equalsIgnoreCase("Throttled!") ||
                                streetAddress.contains("Throttled") ||
                                streetAddress.equalsIgnoreCase("Address not found") ||
                                streetAddress.isEmpty()) {
                            Log.e(TAG, "Invalid address or error detected in API response");
                            return; // Skip updating the UI if the response is not valid
                        }

                        // Retrieve additional location info if available
                        String city = jsonObject.optString("city", "").trim();
                        String finalAddress = streetAddress + (city.isEmpty() ? "" : ", " + city);

                        // Update the UI with the valid address
                        runOnUiThread(() -> inputRue.setText(finalAddress));

                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse Geocode.xyz response: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Geocode.xyz API call unsuccessful");
                }
            }
        });
    }



    private void saveData() {
        if (validateFields()) {
            String rue = inputRue.getText().toString().trim();
            String numImm = inputNumImm.getText().toString().trim();
            String nbrB2C = inputNbrB2C.getText().toString().trim();
            String nbrB2B = inputNbrB2B.getText().toString().trim();
            String largeurTrotoireML = inputLargeurTrotoireML.getText().toString().trim();
            String latitude = inputLatitude.getText().toString().trim();
            String longitude = inputLongitude.getText().toString().trim();
            String typeImmeuble = inputTypeImmeuble.getSelectedItem().toString();
            String nbrEtages = inputNbrEtages.getSelectedItem().toString();
            String boitier = inputBoitier.getSelectedItem().toString();
            String remarque = inputRemarque.getSelectedItem().toString();
            String zone = inputZone.getSelectedItem().toString();
            String sousSol = inputSousSol.getSelectedItem().toString();
            String plaqueName = plaqueEditText.getText().toString().trim();
            String nameVille = villeEditText.getText().toString().trim();

            // Check if an entry already exists with the same address and close coordinates

            long result = databaseHelper.insertData(
                    rue, numImm, nbrB2C, nbrB2B, largeurTrotoireML,
                    latitude, longitude, typeImmeuble, nbrEtages,
                    boitier, remarque, zone, sousSol, plaqueName,nameVille);

            if (result != -1) {
                String villeName = getIntent().getStringExtra("villeName");
                Intent intent = new Intent(MainActivity.this, CommandsActivity.class);
                intent.putExtra("plaqueName", plaqueName);
                intent.putExtra("villeName", villeName);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Échec de l'enregistrement des données.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "\n" +
                    "Veuillez remplir tous les champs correctement.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateFields() {
        return !inputRue.getText().toString().isEmpty() &&
                !villeEditText.getText().toString().isEmpty() &&
                !plaqueEditText.getText().toString().isEmpty() &&
                !inputNumImm.getText().toString().isEmpty() &&
                !inputNbrB2C.getText().toString().isEmpty() &&
                !inputNbrB2B.getText().toString().isEmpty() &&
                !inputLargeurTrotoireML.getText().toString().isEmpty() &&
                !inputLatitude.getText().toString().isEmpty() &&
                !inputLongitude.getText().toString().isEmpty() &&
                inputTypeImmeuble.getSelectedItemPosition() != 0 &&
                inputNbrEtages.getSelectedItemPosition() != 0 &&
                inputBoitier.getSelectedItemPosition() != 0 &&
                inputRemarque.getSelectedItemPosition() != 0 &&
                inputZone.getSelectedItemPosition() != 0 &&
                inputSousSol.getSelectedItemPosition() != 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "\n" +
                        "Une autorisation de localisation est requise.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
