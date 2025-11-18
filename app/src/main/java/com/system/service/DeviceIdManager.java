package com.system.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import java.util.UUID;

public class DeviceIdManager {

    private static final String PREFS_NAME = "device_id_prefs";
    private static final String KEY_DEVICE_ID = "stable_device_id";

    /**
     * Gets a stable device ID that never changes.
     * Priority:
     * 1. Previously saved stable ID (persists across app updates)
     * 2. MAC address (if available)
     * 3. Android ID
     * 4. Generated UUID (saved for future use)
     */
    public static String getDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Check if we already have a saved ID
        String savedId = prefs.getString(KEY_DEVICE_ID, null);
        if (savedId != null && !savedId.isEmpty()) {
            return savedId;
        }

        // Try to get MAC address (works on Android 9 and below)
        String macId = getMacAddress(context);
        if (macId != null && !macId.isEmpty()) {
            // Save it for future use
            prefs.edit().putString(KEY_DEVICE_ID, macId).apply();
            return macId;
        }

        // Try Android ID
        String androidId = getAndroidId(context);
        if (androidId != null && !androidId.isEmpty() && !androidId.equals("9774d56d682e549c")) {
            // 9774d56d682e549c is a known bug value on some emulators
            String deviceId = "device_" + androidId.toLowerCase();
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
            return deviceId;
        }

        // Last resort: Generate a unique UUID and save it
        String generatedId = "device_" + UUID.randomUUID().toString().replace("-", "");
        prefs.edit().putString(KEY_DEVICE_ID, generatedId).apply();
        return generatedId;
    }

    /**
     * Attempts to get MAC address.
     * Note: Returns null on Android 10+ due to privacy restrictions.
     */
    private static String getMacAddress(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);

            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String macAddress = wifiInfo.getMacAddress();

                // Check if it's a valid MAC address
                if (macAddress != null
                    && !macAddress.equals("02:00:00:00:00:00") // Android 6+ fake MAC
                    && !macAddress.equals("00:00:00:00:00:00") // Invalid
                    && macAddress.length() >= 12) {

                    // Format: remove colons/dashes and make lowercase
                    return macAddress.replaceAll("[:-]", "").toLowerCase();
                }
            }
        } catch (Exception e) {
            // Permission denied or other error
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the Android ID (unique per app installation)
     */
    private static String getAndroidId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Manually set a device ID (useful for migration or manual configuration)
     */
    public static void setDeviceId(Context context, String deviceId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
    }

    /**
     * Get the current saved device ID without generating a new one
     */
    public static String getSavedDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_DEVICE_ID, null);
    }

    /**
     * Clear the saved device ID (will generate a new one on next call)
     */
    public static void clearDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_DEVICE_ID).apply();
    }
}
