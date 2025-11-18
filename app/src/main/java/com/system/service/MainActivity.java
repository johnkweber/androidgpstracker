package com.system.service;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int DEVICE_ADMIN_REQUEST_CODE = 101;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName deviceAdminComponent;

    private TextView serviceStatus;
    private TextView deviceIdText;
    private TextView mqttBroker;
    private TextView mqttTopic;
    private TextView lastPosition;
    private TextView lastUpdateTime;
    private TextView mqttStatus;
    private TextView movementStatus;
    private TextView updateInterval;
    private TextView configuredIntervals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        deviceAdminComponent = new ComponentName(this, AppDeviceAdminReceiver.class);

        // Initialize views
        serviceStatus = findViewById(R.id.serviceStatus);
        deviceIdText = findViewById(R.id.deviceId);
        mqttBroker = findViewById(R.id.mqttBroker);
        mqttTopic = findViewById(R.id.mqttTopic);
        lastPosition = findViewById(R.id.lastPosition);
        lastUpdateTime = findViewById(R.id.lastUpdateTime);
        mqttStatus = findViewById(R.id.mqttStatus);
        movementStatus = findViewById(R.id.movementStatus);
        updateInterval = findViewById(R.id.updateInterval);
        configuredIntervals = findViewById(R.id.configuredIntervals);

        Button refreshButton = findViewById(R.id.refreshButton);
        Button settingsButton = findViewById(R.id.settingsButton);
        Button closeButton = findViewById(R.id.closeButton);

        refreshButton.setOnClickListener(v -> updateStatus());
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        closeButton.setOnClickListener(v -> finish());

        // Check and request permissions
        checkAndRequestPermissions();

        // Update status display
        updateStatus();
    }

    private void updateStatus() {
        // Check if service is running
        if (isServiceRunning(LocationTrackingService.class)) {
            serviceStatus.setText("✓ Running");
            serviceStatus.setBackgroundColor(0xFFE8F5E9);
            serviceStatus.setTextColor(0xFF006400);
        } else {
            serviceStatus.setText("✗ Not Running");
            serviceStatus.setBackgroundColor(0xFFFFEBEE);
            serviceStatus.setTextColor(0xFFD32F2F);
        }

        // Get stable device ID
        String deviceId = DeviceIdManager.getDeviceId(this);
        deviceIdText.setText(deviceId);

        // Show MQTT settings
        mqttBroker.setText("Broker: " + Config.MQTT_BROKER_URL);

        // Get base topic from settings
        SharedPreferences trackingPrefs = getSharedPreferences("tracking_settings", MODE_PRIVATE);
        String baseTopic = trackingPrefs.getString("mqtt_base_topic", Config.MQTT_TOPIC_BASE);
        mqttTopic.setText("Topic: " + baseTopic + deviceId + "/gpsdata");

        // Get last position from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("gps_tracking", MODE_PRIVATE);
        String lastLat = prefs.getString("last_lat", "N/A");
        String lastLon = prefs.getString("last_lon", "N/A");
        String lastTime = prefs.getString("last_time", "Never");
        boolean mqttConnected = prefs.getBoolean("mqtt_connected", false);

        if (!lastLat.equals("N/A")) {
            lastPosition.setText("Lat: " + lastLat + "\nLon: " + lastLon);
        } else {
            lastPosition.setText("No position sent yet");
        }

        lastUpdateTime.setText(lastTime);

        if (mqttConnected) {
            mqttStatus.setText("✓ Connected");
            mqttStatus.setBackgroundColor(0xFFE8F5E9);
            mqttStatus.setTextColor(0xFF006400);
        } else {
            mqttStatus.setText("✗ Disconnected");
            mqttStatus.setBackgroundColor(0xFFFFEBEE);
            mqttStatus.setTextColor(0xFFD32F2F);
        }

        // Movement status and current speed
        boolean isMoving = prefs.getBoolean("is_moving", false);
        float currentSpeed = prefs.getFloat("current_speed", 0);
        long currentIntervalMs = prefs.getLong("current_interval", Config.LOCATION_UPDATE_INTERVAL_MOVING);

        if (isMoving) {
            movementStatus.setText("Moving (" + String.format("%.1f", currentSpeed * 3.6f) + " km/h)");
            movementStatus.setBackgroundColor(0xFFE3F2FD);
            movementStatus.setTextColor(0xFF1976D2);
        } else {
            movementStatus.setText("Stationary (" + String.format("%.1f", currentSpeed * 3.6f) + " km/h)");
            movementStatus.setBackgroundColor(0xFFFFF3E0);
            movementStatus.setTextColor(0xFFF57C00);
        }

        // Current interval
        long currentIntervalMin = currentIntervalMs / 60000;
        updateInterval.setText(currentIntervalMin + " minutes (" + (isMoving ? "Moving Mode" : "Stationary Mode") + ")");

        // Configured intervals - read from settings (reuse trackingPrefs from above)
        long movingMs = trackingPrefs.getLong("moving_interval", Config.LOCATION_UPDATE_INTERVAL_MOVING);
        long stationaryMs = trackingPrefs.getLong("stationary_interval", Config.LOCATION_UPDATE_INTERVAL_STATIONARY);
        float speedThreshold = trackingPrefs.getFloat("speed_threshold", Config.SPEED_THRESHOLD_MOVING);

        String movingStr = formatInterval(movingMs);
        String stationaryStr = formatInterval(stationaryMs);

        configuredIntervals.setText(
                "Moving: " + movingStr + "\n" +
                "Stationary: " + stationaryStr + "\n" +
                "Speed threshold: " + String.format("%.1f", speedThreshold * 3.6f) + " km/h"
        );
    }


    private String formatInterval(long milliseconds) {
        long seconds = milliseconds / 1000;
        if (seconds < 60) {
            return seconds + " sec";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " min";
        }
        long hours = minutes / 60;
        return hours + " hr";
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkAndRequestPermissions() {
        boolean needsPermission = false;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            needsPermission = true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                needsPermission = true;
            }
        }

        if (needsPermission) {
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
        } else {
            enableDeviceAdmin();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            enableDeviceAdmin();
        }
    }

    private void enableDeviceAdmin() {
        // Device Admin is optional - skip if not already enabled
        // (Can be enabled manually via Settings if needed)
        requestBatteryOptimization();
    }

    private void requestBatteryOptimization() {
        // Battery optimization is handled via ADB for automated setup
        // Skip user prompt
        startServiceIfNotRunning();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEVICE_ADMIN_REQUEST_CODE) {
            requestBatteryOptimization();
        }
    }

    private void startServiceIfNotRunning() {
        if (!isServiceRunning(LocationTrackingService.class)) {
            Intent serviceIntent = new Intent(this, LocationTrackingService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}
