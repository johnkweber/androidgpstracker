package com.system.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class LocationTrackingService extends Service {
    private static final String CHANNEL_ID = "SystemServiceChannel";
    private static final int NOTIFICATION_ID = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private MqttClient mqttClient;
    private String deviceId;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        // Load settings from SharedPreferences
        loadSettings();

        // Get stable device ID
        deviceId = DeviceIdManager.getDeviceId(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupLocationTracking();
        setupMqttClient();
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("tracking_settings", MODE_PRIVATE);
        // Update Config defaults if custom settings exist (we'll read from prefs in the tracking code)
    }

    private void setupLocationTracking() {
        // Load custom intervals from settings
        SharedPreferences prefs = getSharedPreferences("tracking_settings", MODE_PRIVATE);
        long movingInterval = prefs.getLong("moving_interval", Config.LOCATION_UPDATE_INTERVAL_MOVING);

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                movingInterval)
                .setMinUpdateIntervalMillis(Config.LOCATION_FASTEST_INTERVAL)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        // Load custom settings
                        SharedPreferences prefs = getSharedPreferences("tracking_settings", MODE_PRIVATE);
                        float speedThreshold = prefs.getFloat("speed_threshold", Config.SPEED_THRESHOLD_MOVING);
                        long movingInterval = prefs.getLong("moving_interval", Config.LOCATION_UPDATE_INTERVAL_MOVING);
                        long stationaryInterval = prefs.getLong("stationary_interval", Config.LOCATION_UPDATE_INTERVAL_STATIONARY);

                        // Check if device is moving and adjust update interval
                        boolean isMoving = location.hasSpeed() && location.getSpeed() > speedThreshold;
                        long newInterval = isMoving ? movingInterval : stationaryInterval;

                        // Save movement status
                        saveMovementStatus(isMoving, location.hasSpeed() ? location.getSpeed() : 0);

                        // Update location request if interval changed significantly
                        updateLocationInterval(newInterval);

                        sendLocationData(location);
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        }
    }

    private long currentInterval = Config.LOCATION_UPDATE_INTERVAL_MOVING;

    private void updateLocationInterval(long newInterval) {
        if (newInterval != currentInterval) {
            currentInterval = newInterval;
            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    newInterval)
                    .setMinUpdateIntervalMillis(Config.LOCATION_FASTEST_INTERVAL)
                    .build();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.removeLocationUpdates(locationCallback);
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        Looper.getMainLooper());
            }
        }
    }

    private void saveMovementStatus(boolean isMoving, float speed) {
        SharedPreferences prefs = getSharedPreferences("gps_tracking", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_moving", isMoving);
        editor.putFloat("current_speed", speed);
        editor.putLong("current_interval", currentInterval);
        editor.apply();
    }

    private void setupMqttClient() {
        new Thread(() -> {
            try {
                // Use static client ID - same device always gets same ID
                // This ensures only one connection per device on the broker
                String clientId = "android_device_" + deviceId;
                mqttClient = new MqttClient(Config.MQTT_BROKER_URL, clientId, new MemoryPersistence());

                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setAutomaticReconnect(true);
                options.setConnectionTimeout(10);
                options.setKeepAliveInterval(60);

                if (!Config.MQTT_USERNAME.isEmpty()) {
                    options.setUserName(Config.MQTT_USERNAME);
                }
                if (!Config.MQTT_PASSWORD.isEmpty()) {
                    options.setPassword(Config.MQTT_PASSWORD.toCharArray());
                }

                mqttClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        reconnectMqtt();
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                    }
                });

                mqttClient.connect(options);
            } catch (MqttException e) {
                e.printStackTrace();
                // Retry connection after delay
                reconnectMqtt();
            }
        }).start();
    }

    private void reconnectMqtt() {
        new Thread(() -> {
            try {
                Thread.sleep(30000); // Wait 30 seconds before reconnecting
                if (mqttClient != null && !mqttClient.isConnected()) {
                    // Try to reconnect existing client first
                    try {
                        MqttConnectOptions options = new MqttConnectOptions();
                        options.setCleanSession(true);
                        options.setAutomaticReconnect(true);
                        options.setConnectionTimeout(10);
                        options.setKeepAliveInterval(60);

                        if (!Config.MQTT_USERNAME.isEmpty()) {
                            options.setUserName(Config.MQTT_USERNAME);
                        }
                        if (!Config.MQTT_PASSWORD.isEmpty()) {
                            options.setPassword(Config.MQTT_PASSWORD.toCharArray());
                        }

                        mqttClient.connect(options);
                    } catch (MqttException e) {
                        // If reconnect fails, close old client and create new one
                        try {
                            mqttClient.close();
                        } catch (Exception ex) {
                            // Ignore close errors
                        }
                        setupMqttClient();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendLocationData(Location location) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("device_id", deviceId);
                json.put("latitude", location.getLatitude());
                json.put("longitude", location.getLongitude());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                json.put("timestamp", sdf.format(new Date()));

                if (mqttClient != null && mqttClient.isConnected()) {
                    // Get client name from settings
                    SharedPreferences prefs = getSharedPreferences("tracking_settings", MODE_PRIVATE);
                    String clientName = prefs.getString("mqtt_client_name", Config.MQTT_CLIENT_NAME);

                    // Construct topic: /iotds/{clientname}/{device_id}/gpsdata
                    String topic = "/iotds/" + clientName + "/" + deviceId + "/gpsdata";
                    MqttMessage message = new MqttMessage(json.toString().getBytes());
                    message.setQos(1);
                    message.setRetained(false);
                    mqttClient.publish(topic, message);

                    // Save last position to SharedPreferences
                    saveLastPosition(location);
                } else {
                    // Attempt to reconnect if not connected
                    reconnectMqtt();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void saveLastPosition(Location location) {
        SharedPreferences prefs = getSharedPreferences("gps_tracking", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_lat", String.format("%.6f", location.getLatitude()));
        editor.putString("last_lon", String.format("%.6f", location.getLongitude()));
        editor.putString("last_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
        editor.putBoolean("mqtt_connected", mqttClient != null && mqttClient.isConnected());
        editor.apply();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "System Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setShowBadge(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.service_notification_title))
                .setContentText(getString(R.string.service_notification_text))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (mqttClient != null) {
            try {
                // Disconnect and close the client to free resources
                if (mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
                mqttClient.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
