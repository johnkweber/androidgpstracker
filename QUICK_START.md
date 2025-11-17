# Quick Start Guide - Push to GitHub and Build APK

## Option 1: Use the Setup Script (Easiest)

```bash
cd "/mnt/c/downloads/leon nel/gps android app"
./GITHUB_SETUP.sh
```

Follow the prompts to enter your GitHub username and repository name.

---

## Option 2: Manual Setup

### 1. Create GitHub Repository
1. Go to **https://github.com/new**
2. Repository name: `staff-gps-tracker`
3. Choose Public or Private
4. **DO NOT** check "Initialize with README"
5. Click **"Create repository"**

### 2. Open Terminal in This Folder
```bash
cd "/mnt/c/downloads/leon nel/gps android app"
```

### 3. Run These Commands

Replace `YOUR_USERNAME` and `YOUR_REPO` with your actual values:

```bash
# Initialize git
git init

# Add all files
git add .

# Create first commit
git commit -m "Initial commit: GPS tracking app"

# Set main branch
git branch -M main

# Add your GitHub repository
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git

# Push to GitHub
git push -u origin main
```

### 4. GitHub Will Ask for Credentials
- Username: Your GitHub username
- Password: Use a **Personal Access Token** (not your GitHub password)
  - Get token at: https://github.com/settings/tokens
  - Click "Generate new token (classic)"
  - Select scopes: `repo` (full control)
  - Generate and copy the token
  - Use this token as your password

---

## After Pushing to GitHub

### Download Your APK

1. Go to your repository on GitHub
2. Click **"Actions"** tab at the top
3. You'll see a workflow running called "Build Android APK"
4. Wait 2-3 minutes for it to complete (green checkmark)
5. Click on the completed workflow
6. Scroll down to **"Artifacts"** section
7. Download **"app-release"**
8. Extract the ZIP file to get `app-release-unsigned.apk`

**OR**

1. Go to your repository
2. Click **"Releases"** on the right side
3. Download the latest APK file

---

## Install on Staff Phones

### For Each Phone:

1. **Transfer APK**
   - Email it, use USB, or cloud storage
   - Copy to phone's Downloads folder

2. **Enable Unknown Sources**
   - Android 7-9: Settings → Security → Unknown Sources (enable)
   - Android 10+: Settings → Apps → Special Access → Install Unknown Apps → (Your File Manager) → Allow

3. **Install**
   - Open the APK file
   - Tap "Install"
   - Wait for installation

4. **Grant Permissions**
   - Location: Select **"Allow all the time"**
   - Device Administrator: Tap **"Activate"**
   - Battery Optimization: Tap **"Don't optimize"** or **"Allow"**

5. **Done!**
   - App will minimize automatically
   - Starts tracking in background
   - Auto-starts after phone reboots

---

## Verify It's Working

### Check MQTT Data
Each phone will publish GPS data to:
- Topic: `/iotds/leonnel/{macaddress}/gpsdata`
- Example: `/iotds/leonnel/a1b2c3d4e5f6/gpsdata`

### Data Format:
```json
{
  "device_id": "a1b2c3d4e5f6",
  "latitude": -33.5873,
  "longitude": 22.19503,
  "timestamp": "2025-11-17T14:18:36"
}
```

Updates every 5 minutes.

---

## Troubleshooting

### "Permission denied" when pushing to GitHub
- Make sure you're using a Personal Access Token, not your password
- Token needs `repo` scope enabled

### Build fails on GitHub Actions
- Check Actions tab for error details
- Ensure all files were committed (`git status`)
- Verify no syntax errors in code files

### No GPS data received
- Check phone has internet connection
- Verify location permissions are "Always allow"
- Check MQTT broker is accessible: `server.iotdataserver.com:1883`
- Verify topic subscription: `/iotds/leonnel/+/gpsdata` (+ is wildcard)

### App doesn't auto-start after reboot
- Make sure Device Administrator is enabled
- Check battery optimization is disabled
- Some manufacturers (Xiaomi, Huawei) need additional permissions in their custom settings

---

## Need Help?

- Check `README.md` for full documentation
- Check `config.md` for configuration options
- Review `SETUP.md` for detailed setup instructions
