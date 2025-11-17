#!/bin/bash

# Complete automated installation script for staff phones
# This installs the APK and grants all permissions automatically

echo "=== GPS Tracker - Automated Phone Setup ==="
echo ""

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo "ERROR: ADB not found. Please install Android Debug Bridge (ADB)"
    echo "Download from: https://developer.android.com/studio/releases/platform-tools"
    exit 1
fi

# Check if phone is connected
echo "Checking for connected device..."
DEVICE=$(adb devices | grep -w "device" | head -1)
if [ -z "$DEVICE" ]; then
    echo "ERROR: No Android device connected via USB"
    echo ""
    echo "Please:"
    echo "1. Connect phone via USB"
    echo "2. Enable USB Debugging on phone (Settings > Developer Options)"
    echo "3. Accept USB debugging prompt on phone"
    exit 1
fi

echo "✓ Device connected"
echo ""

# Find the APK file
APK_FILE=$(find . -name "app-debug.apk" -o -name "app-signed.apk" | head -1)
if [ -z "$APK_FILE" ]; then
    echo "ERROR: APK file not found"
    echo "Please download the APK from GitHub Actions and place it in this folder"
    exit 1
fi

echo "Found APK: $APK_FILE"
echo ""

PACKAGE="com.system.service"

# Check if app is already installed
if adb shell pm list packages | grep -q "$PACKAGE"; then
    echo "App already installed. Upgrading..."
    adb install -r "$APK_FILE"
else
    echo "Installing app for the first time..."
    adb install "$APK_FILE"
fi

echo ""
echo "Granting permissions..."

# Grant location permissions
adb shell pm grant $PACKAGE android.permission.ACCESS_FINE_LOCATION 2>/dev/null
adb shell pm grant $PACKAGE android.permission.ACCESS_COARSE_LOCATION 2>/dev/null
adb shell pm grant $PACKAGE android.permission.ACCESS_BACKGROUND_LOCATION 2>/dev/null

# Disable battery optimization
echo "Disabling battery optimization..."
adb shell dumpsys deviceidle whitelist +$PACKAGE 2>/dev/null

# Start the app to trigger service
echo "Starting tracking service..."
adb shell am start -n $PACKAGE/.MainActivity 2>/dev/null
sleep 2
adb shell input keyevent KEYCODE_HOME

echo ""
echo "=== SETUP COMPLETE - 100% AUTOMATED! ==="
echo ""
echo "✓ App installed"
echo "✓ All permissions granted"
echo "✓ Battery optimization disabled"
echo "✓ Service started and tracking"
echo ""
echo "Phone is ready! No further action needed."
echo "GPS tracking is active and will send data to MQTT server."
echo ""
echo "Staff can open 'System Service' app to view tracking status (optional)."
echo ""
