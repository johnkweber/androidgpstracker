# Setup Instructions

## Step 1: Configure MQTT Settings

1. Open `app/src/main/java/com/system/service/Config.java`
2. Update these values:
   ```java
   MQTT_BROKER_URL = "tcp://YOUR_MQTT_SERVER:1883"
   MQTT_USERNAME = "your_username"  // or "" if no auth
   MQTT_PASSWORD = "your_password"  // or "" if no auth
   MQTT_TOPIC = "gps/location"      // or your preferred topic
   ```

## Step 2: Create GitHub Repository

1. Go to https://github.com/new
2. Create a new repository (public or private)
3. Do NOT initialize with README (we already have files)

## Step 3: Push Code to GitHub

Run these commands in this directory:

```bash
git init
git add .
git commit -m "Initial GPS tracking app"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
git push -u origin main
```

## Step 4: Get Your APK

### Option A: From GitHub Actions (Recommended)
1. Go to your repository on GitHub
2. Click "Actions" tab
3. Wait for the build to complete (2-3 minutes)
4. Click on the completed workflow
5. Download "app-release" artifact
6. Extract the ZIP to get your APK

### Option B: From Releases
1. Go to your repository on GitHub
2. Click "Releases" section
3. Download the latest APK file

## Step 5: Install on Phones

1. Transfer APK to each staff phone
2. Enable "Install from Unknown Sources":
   - Settings → Security → Unknown Sources (Android 7-9)
   - Settings → Apps → Special Access → Install Unknown Apps (Android 10+)
3. Install the APK
4. Grant all permissions when prompted:
   - Location: Select "Allow all the time"
   - Device Administrator: Tap "Activate"
   - Battery: Select "Don't optimize"

## Step 6: Verify It's Working

1. Check your MQTT broker for incoming messages on your configured topic
2. Device ID will be the phone's MAC address (lowercase, no dashes)
3. Location updates every 5 minutes (configurable in Config.java)

## Troubleshooting

### "Build failed" on GitHub Actions
- Check that all files were committed
- Verify Config.java has valid Java syntax
- Check Actions tab for detailed error logs

### App crashes on install
- Ensure phone is Android 7.0 or higher
- Check that Google Play Services is installed
- Try installing on a different device

### No location data received
- Verify MQTT broker URL is correct and accessible
- Check phone has internet connection
- Ensure location permissions are granted
- Check MQTT topic matches your subscriber

### Can't find MAC address
- Some newer Android versions restrict MAC access
- App will use Android ID instead
- Format: `device_<androidid>` instead of MAC

## Next Steps

After installation:
- App runs automatically in background
- Auto-starts after phone reboot
- Continues tracking until manually stopped
- To stop: Settings → Security → Device Admin → Disable "System Service"

## Privacy & Legal

- This app is for company-owned devices only
- Ensure staff are informed about tracking
- Comply with local privacy and employment laws
- Keep MQTT credentials secure
