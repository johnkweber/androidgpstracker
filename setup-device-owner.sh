#!/bin/bash

# Device Owner Setup - FULLY AUTOMATED (No user interaction needed)
#
# REQUIREMENTS:
# - Phone must be factory reset OR never had a Google account added
# - USB debugging enabled
# - Phone connected via USB

echo "=== Device Owner Setup - Fully Automated GPS Tracking ==="
echo ""
echo "WARNING: This only works on:"
echo "  - Factory reset phones (no Google account added yet)"
echo "  - OR phones that have never been set up"
echo ""
read -p "Has this phone been factory reset? (y/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Please factory reset the phone first:"
    echo "Settings > System > Reset > Factory data reset"
    exit 1
fi

PACKAGE="com.system.service"

# Install the APK
echo "Installing APK..."
APK_FILE=$(find . -name "app-debug.apk" -o -name "app-signed.apk" | head -1)
if [ -z "$APK_FILE" ]; then
    echo "ERROR: APK file not found"
    exit 1
fi

adb install "$APK_FILE"

echo ""
echo "Setting app as Device Owner..."

# Set as device owner (this is the magic command!)
adb shell dpm set-device-owner $PACKAGE/.AppDeviceAdminReceiver

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Device Owner set successfully!"
    echo ""

    # Grant all permissions automatically
    echo "Granting all permissions automatically..."
    adb shell pm grant $PACKAGE android.permission.ACCESS_FINE_LOCATION
    adb shell pm grant $PACKAGE android.permission.ACCESS_COARSE_LOCATION
    adb shell pm grant $PACKAGE android.permission.ACCESS_BACKGROUND_LOCATION

    # Disable battery optimization
    adb shell dumpsys deviceidle whitelist +$PACKAGE

    # Start the service
    echo "Starting tracking service..."
    adb shell am start -n $PACKAGE/.MainActivity
    sleep 2
    adb shell input keyevent KEYCODE_HOME

    echo ""
    echo "=== FULLY AUTOMATED SETUP COMPLETE! ==="
    echo ""
    echo "✓ App is now Device Owner"
    echo "✓ All permissions granted automatically"
    echo "✓ Cannot be uninstalled by user"
    echo "✓ No battery optimization"
    echo "✓ Service running"
    echo ""
    echo "The phone is ready to give to staff!"
    echo "No further setup needed."
else
    echo ""
    echo "✗ Failed to set Device Owner"
    echo ""
    echo "Common reasons:"
    echo "1. Phone has a Google account already - MUST factory reset first"
    echo "2. Phone is not connected via USB"
    echo "3. USB debugging not enabled"
    echo ""
    echo "Use the regular install script instead: ./install-on-phone.sh"
fi
