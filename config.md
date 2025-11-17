# Configuration Guide

## MQTT Server Settings

Before building the app, you MUST configure your MQTT server settings in:
`app/src/main/java/com/system/service/Config.java`

### Required Settings:

1. **MQTT_BROKER_URL**: Your MQTT broker address
   - Format: `tcp://your-server.com:1883`
   - Example: `tcp://192.168.1.100:1883` or `tcp://mqtt.example.com:1883`

2. **MQTT_USERNAME**: Your MQTT username (leave as empty string "" if no auth)

3. **MQTT_PASSWORD**: Your MQTT password (leave as empty string "" if no auth)

4. **MQTT_TOPIC**: The topic where GPS data will be published
   - Default: `gps/location`
   - You can change this to any topic you prefer

### Optional Settings:

5. **LOCATION_UPDATE_INTERVAL**: How often to get GPS updates (in milliseconds)
   - Default: 300000 (5 minutes)
   - Example: 60000 = 1 minute, 600000 = 10 minutes

6. **LOCATION_FASTEST_INTERVAL**: Fastest rate for GPS updates (in milliseconds)
   - Default: 120000 (2 minutes)

## Data Format

The app sends GPS data in this exact format:
```json
{
  "device_id": "a1b2c3d4e5f6",
  "latitude": -33.5873,
  "longitude": 22.19503,
  "timestamp": "2025-11-17T14:18:36"
}
```

- `device_id`: MAC address (lowercase, no dashes) or Android ID if MAC unavailable
- `latitude`: GPS latitude coordinate
- `longitude`: GPS longitude coordinate
- `timestamp`: UTC timestamp in ISO 8601 format

## Installation Instructions for Staff

1. Enable "Install from Unknown Sources" in Android Settings
2. Install the APK file
3. Grant all requested permissions:
   - Location (Always)
   - Device Administrator
   - Battery Optimization exemption
4. The app will minimize after setup and run in the background

## Important Notes

- The app uses MAC address as device ID (formatted as lowercase without dashes)
- If MAC address is unavailable (Android 10+), it falls back to Android ID
- The app runs as a foreground service for reliability
- Device Admin prevents easy uninstallation
- To uninstall, first disable Device Admin in Settings > Security > Device Administrators
