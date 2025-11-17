package com.system.service;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SetupActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int BATTERY_REQUEST_CODE = 101;

    private TextView setupMessage;
    private Button setupButton;
    private int setupStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if setup is already complete
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (prefs.getBoolean("setup_complete", false)) {
            // Setup already done, go to main activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_setup);

        setupMessage = findViewById(R.id.setupMessage);
        setupButton = findViewById(R.id.setupButton);

        setupButton.setOnClickListener(v -> nextStep());
    }

    private void nextStep() {
        switch (setupStep) {
            case 0:
                // Request location permissions
                requestLocationPermissions();
                break;
            case 1:
                // Request battery optimization exemption
                requestBatteryExemption();
                break;
            case 2:
                // All done - start service and go to main activity
                completeSetup();
                break;
        }
    }

    private void requestLocationPermissions() {
        setupMessage.setText("Step 1 of 2\n\nTap 'Allow' on the next screens to enable location tracking.");
        setupButton.setText("ALLOW LOCATION ACCESS");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    },
                    PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            setupStep = 1;
            nextStep();
        }
    }

    private void requestBatteryExemption() {
        setupMessage.setText("Step 2 of 2\n\nTap 'Allow' to prevent battery optimization from stopping GPS tracking.");
        setupButton.setText("ALLOW BATTERY EXEMPTION");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            String packageName = getPackageName();
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivityForResult(intent, BATTERY_REQUEST_CODE);
                return;
            }
        }

        // Already exempt or not needed
        setupStep = 2;
        nextStep();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BATTERY_REQUEST_CODE) {
            setupStep = 2;
            nextStep();
        }
    }

    private void completeSetup() {
        // Mark setup as complete
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("setup_complete", true).apply();

        // Start the tracking service
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // Show success message
        setupMessage.setText("✓ Setup Complete!\n\nGPS tracking is now active.\n\nYou can close this app.");
        setupButton.setText("VIEW STATUS");
        setupButton.setOnClickListener(v -> {
            Intent intent = new Intent(SetupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
