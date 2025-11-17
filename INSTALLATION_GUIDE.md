# Installation Guide - Zero-Touch Setup for Staff Phones

Since you own these phones, you can set them up with **zero user interaction** required.

---

## Method 1: Device Owner Mode (BEST - Fully Automated) ✨

**Use this for NEW phones or phones you can factory reset**

### What You Get:
- ✅ **100% automated** - no user interaction needed
- ✅ All permissions granted automatically
- ✅ Cannot be uninstalled by staff
- ✅ No battery optimization
- ✅ Starts automatically on boot
- ✅ No permission prompts ever

### Requirements:
- Factory reset phone OR brand new phone
- Phone must NOT have Google account added yet
- USB debugging enabled

### Steps:

1. **Factory reset the phone:**
   - Settings → System → Reset → Factory data reset

2. **Enable USB debugging:**
   - Complete initial setup (skip Google account!)
   - Settings → About Phone → Tap "Build Number" 7 times
   - Settings → Developer Options → Enable "USB Debugging"

3. **Connect phone via USB and run:**
   ```bash
   cd "/mnt/c/downloads/leon nel/gps android app"
   ./setup-device-owner.sh
   ```

4. **Done!** Give the phone to staff. Everything is already configured.

---

## Method 2: ADB Installation (If Phone Already Set Up)

**Use this if you can't factory reset the phone**

### What You Get:
- ✅ Most permissions granted automatically
- ✅ No battery optimization
- ✅ Starts automatically
- ⚠️ Staff can still uninstall (unless you manually enable Device Admin)

### Steps:

1. **Connect phone via USB**

2. **Run installation script:**
   ```bash
   cd "/mnt/c/downloads/leon nel/gps android app"
   ./install-on-phone.sh
   ```

3. **Manually enable Device Admin (one-time):**
   - On the phone: Settings → Security → Device Administrators
   - Enable "System Service"

4. **Done!**

---

## Method 3: Manual Installation (Fallback)

If you don't have ADB available:

1. Transfer APK to phone
2. Install APK
3. Open app and grant permissions when prompted
4. Enable Device Admin when prompted
5. Allow battery optimization exemption

---

## Recommended Workflow for Multiple Phones:

### For New Phones You're Provisioning:
1. Unbox phone
2. Do minimal setup (WiFi only, skip Google account)
3. Enable USB debugging
4. Connect via USB
5. Run `./setup-device-owner.sh`
6. Done - give to staff

### For Existing Staff Phones:
1. Ask staff to enable USB debugging (you can do this remotely via MDM)
2. Have them connect phone or do it yourself
3. Run `./install-on-phone.sh`
4. Enable Device Admin (you or staff)
5. Done

---

## What Happens After Installation:

1. **App starts automatically** - no need to open it
2. **Tracks GPS every 2 minutes when moving**
3. **Tracks GPS every 10 minutes when stationary**
4. **Sends data to MQTT:** `server.iotdataservice.com`
5. **Topic:** `/iotds/leonnel/{macaddress}/gpsdata`
6. **Staff can open app** to view status (optional)

---

## Troubleshooting:

### "Cannot set device owner - user already present"
- Phone must be factory reset
- No Google account can be added
- Use Method 2 instead

### "ADB not found"
- Install Android Platform Tools: https://developer.android.com/studio/releases/platform-tools
- Add to PATH or run from platform-tools folder

### "Device not authorized"
- Check phone screen for USB debugging prompt
- Accept the prompt
- Run script again

---

## Security Note:

Device Owner mode is designed for enterprise deployments. Since you own these phones and provide them to staff:
- This is a **legitimate use case**
- Staff should be informed the phone tracks location
- Complies with fleet management practices
- Cannot be used on personal phones without consent

