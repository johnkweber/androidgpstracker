package com.system.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Spinner movingIntervalSpinner;
    private Spinner stationaryIntervalSpinner;
    private Spinner speedThresholdSpinner;
    private android.widget.EditText clientNameInput;
    private TextView settingsPreview;
    private Button saveButton;
    private Button resetButton;
    private Button cancelButton;

    private SharedPreferences prefs;

    // Available options
    private String[] movingIntervals = {
            "30 seconds", "1 minute", "2 minutes", "3 minutes",
            "5 minutes", "10 minutes"
    };
    private long[] movingIntervalValues = {
            30 * 1000L, 1 * 60 * 1000L, 2 * 60 * 1000L, 3 * 60 * 1000L,
            5 * 60 * 1000L, 10 * 60 * 1000L
    };

    private String[] stationaryIntervals = {
            "1 minute", "2 minutes", "5 minutes", "10 minutes",
            "15 minutes", "30 minutes", "1 hour"
    };
    private long[] stationaryIntervalValues = {
            1 * 60 * 1000L, 2 * 60 * 1000L, 5 * 60 * 1000L, 10 * 60 * 1000L,
            15 * 60 * 1000L, 30 * 60 * 1000L, 60 * 60 * 1000L
    };

    private String[] speedThresholds = {
            "1 km/h (0.3 m/s)", "2 km/h (0.6 m/s)", "3.6 km/h (1.0 m/s)",
            "5 km/h (1.4 m/s)", "10 km/h (2.8 m/s)"
    };
    private float[] speedThresholdValues = {
            0.3f, 0.6f, 1.0f, 1.4f, 2.8f
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("tracking_settings", MODE_PRIVATE);

        // Initialize views
        movingIntervalSpinner = findViewById(R.id.movingIntervalSpinner);
        stationaryIntervalSpinner = findViewById(R.id.stationaryIntervalSpinner);
        speedThresholdSpinner = findViewById(R.id.speedThresholdSpinner);
        clientNameInput = findViewById(R.id.clientNameInput);
        settingsPreview = findViewById(R.id.settingsPreview);
        saveButton = findViewById(R.id.saveButton);
        resetButton = findViewById(R.id.resetButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Setup spinners
        setupSpinners();

        // Load current settings
        loadSettings();

        // Setup listeners
        AdapterView.OnItemSelectedListener previewListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        movingIntervalSpinner.setOnItemSelectedListener(previewListener);
        stationaryIntervalSpinner.setOnItemSelectedListener(previewListener);
        speedThresholdSpinner.setOnItemSelectedListener(previewListener);

        // Update preview when client name changes
        clientNameInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        saveButton.setOnClickListener(v -> saveSettings());
        resetButton.setOnClickListener(v -> resetToDefaults());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        ArrayAdapter<String> movingAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, movingIntervals);
        movingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        movingIntervalSpinner.setAdapter(movingAdapter);

        ArrayAdapter<String> stationaryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, stationaryIntervals);
        stationaryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stationaryIntervalSpinner.setAdapter(stationaryAdapter);

        ArrayAdapter<String> speedAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, speedThresholds);
        speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedThresholdSpinner.setAdapter(speedAdapter);
    }

    private void loadSettings() {
        // Get saved values or defaults
        long movingInterval = prefs.getLong("moving_interval", Config.LOCATION_UPDATE_INTERVAL_MOVING);
        long stationaryInterval = prefs.getLong("stationary_interval", Config.LOCATION_UPDATE_INTERVAL_STATIONARY);
        float speedThreshold = prefs.getFloat("speed_threshold", Config.SPEED_THRESHOLD_MOVING);
        String clientName = prefs.getString("mqtt_client_name", Config.MQTT_CLIENT_NAME);

        // Set spinner positions
        movingIntervalSpinner.setSelection(findClosestIndex(movingInterval, movingIntervalValues));
        stationaryIntervalSpinner.setSelection(findClosestIndex(stationaryInterval, stationaryIntervalValues));
        speedThresholdSpinner.setSelection(findClosestIndex(speedThreshold, speedThresholdValues));
        clientNameInput.setText(clientName);

        updatePreview();
    }

    private int findClosestIndex(long value, long[] array) {
        int closestIndex = 0;
        long minDiff = Math.abs(array[0] - value);

        for (int i = 1; i < array.length; i++) {
            long diff = Math.abs(array[i] - value);
            if (diff < minDiff) {
                minDiff = diff;
                closestIndex = i;
            }
        }
        return closestIndex;
    }

    private int findClosestIndex(float value, float[] array) {
        int closestIndex = 0;
        float minDiff = Math.abs(array[0] - value);

        for (int i = 1; i < array.length; i++) {
            float diff = Math.abs(array[i] - value);
            if (diff < minDiff) {
                minDiff = diff;
                closestIndex = i;
            }
        }
        return closestIndex;
    }

    private void updatePreview() {
        int movingPos = movingIntervalSpinner.getSelectedItemPosition();
        int stationaryPos = stationaryIntervalSpinner.getSelectedItemPosition();
        int speedPos = speedThresholdSpinner.getSelectedItemPosition();
        String clientName = clientNameInput.getText().toString().trim();

        if (clientName.isEmpty()) {
            clientName = Config.MQTT_CLIENT_NAME;
        }

        String preview = "MQTT Topic: /iotds/" + clientName + "/{device_id}/gpsdata\n\n" +
                "When moving faster than " + speedThresholds[speedPos] + ":\n" +
                "  → Send GPS data every " + movingIntervals[movingPos] + "\n\n" +
                "When stationary or moving slowly:\n" +
                "  → Send GPS data every " + stationaryIntervals[stationaryPos];

        settingsPreview.setText(preview);
    }

    private void saveSettings() {
        int movingPos = movingIntervalSpinner.getSelectedItemPosition();
        int stationaryPos = stationaryIntervalSpinner.getSelectedItemPosition();
        int speedPos = speedThresholdSpinner.getSelectedItemPosition();
        String clientName = clientNameInput.getText().toString().trim();

        // Validate client name
        if (clientName.isEmpty()) {
            clientName = Config.MQTT_CLIENT_NAME;
        }

        // Remove any forward slashes from client name
        clientName = clientName.replace("/", "");

        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("moving_interval", movingIntervalValues[movingPos]);
        editor.putLong("stationary_interval", stationaryIntervalValues[stationaryPos]);
        editor.putFloat("speed_threshold", speedThresholdValues[speedPos]);
        editor.putString("mqtt_client_name", clientName);
        editor.apply();

        Toast.makeText(this, "Settings saved! Restart tracking service for changes to take effect.",
                Toast.LENGTH_LONG).show();

        // Restart the tracking service to apply new settings
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        stopService(serviceIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        Toast.makeText(this, "Tracking service restarted with new settings", Toast.LENGTH_SHORT).show();

        finish();
    }

    private void resetToDefaults() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("moving_interval", Config.LOCATION_UPDATE_INTERVAL_MOVING);
        editor.putLong("stationary_interval", Config.LOCATION_UPDATE_INTERVAL_STATIONARY);
        editor.putFloat("speed_threshold", Config.SPEED_THRESHOLD_MOVING);
        editor.putString("mqtt_client_name", Config.MQTT_CLIENT_NAME);
        editor.apply();

        loadSettings();
        Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
    }
}
