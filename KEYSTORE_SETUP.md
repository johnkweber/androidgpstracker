# Keystore Setup Guide (FREE & Easy)

This guide will help you create a signing key for your app so that future versions can upgrade without uninstalling.

## What You'll Do:
1. Generate a keystore file (1 command, takes 1 minute)
2. Add the keystore to GitHub Secrets (copy & paste)
3. Done! All future builds will be signed and upgradeable

---

## Step 1: Generate Keystore (FREE)

Run this command in your terminal:

```bash
cd "/mnt/c/downloads/leon nel/gps android app"
./create-keystore.sh
```

**That's it!** The keystore is created at `keystore/release-key.jks`

---

## Step 2: Add Keystore to GitHub Secrets

### A. Convert keystore to Base64

Run this command:

```bash
cd "/mnt/c/downloads/leon nel/gps android app"
base64 -w 0 keystore/release-key.jks > keystore-base64.txt
```

This creates a text file with the encoded keystore.

### B. Add Secrets to GitHub

1. Go to your repository: https://github.com/johnkweber/androidgpstracker
2. Click **Settings** (top right)
3. Click **Secrets and variables** → **Actions** (left sidebar)
4. Click **New repository secret** (green button)

Add these 4 secrets one by one:

#### Secret 1: KEYSTORE_BASE64
- Name: `KEYSTORE_BASE64`
- Value: Open `keystore-base64.txt` and copy the entire contents (it's one long line)
- Click **Add secret**

#### Secret 2: KEYSTORE_PASSWORD
- Name: `KEYSTORE_PASSWORD`
- Value: `YourStorePassword123!`
- Click **Add secret**

#### Secret 3: KEY_ALIAS
- Name: `KEY_ALIAS`
- Value: `release-key`
- Click **Add secret**

#### Secret 4: KEY_PASSWORD
- Name: `KEY_PASSWORD`
- Value: `YourKeyPassword123!`
- Click **Add secret**

---

## Step 3: Test It

```bash
cd "/mnt/c/downloads/leon nel/gps android app"
git add -A
git commit -m "Add keystore signing configuration"
git push
```

Wait for the build to complete (~2 minutes), then download the new APK.

---

## What This Does:

- ✓ All future APKs will be signed with the same key
- ✓ You can upgrade the app without uninstalling
- ✓ Works for all staff phones
- ✓ Completely FREE
- ✓ Secure (keystore is encrypted in GitHub Secrets)

---

## Important Notes:

1. **Keep the keystore safe!** If you lose it, you'll need to uninstall/reinstall again
2. The passwords I used are in the script. You can change them if you want (not required)
3. Never commit the `keystore/` folder to git (it's already in .gitignore)
4. The base64 file is safe to delete after uploading to GitHub

---

## Troubleshooting:

### "keytool: command not found"
You need Java installed. If you have Java, try:
- Windows: Use Git Bash or WSL
- The keytool should be in your Java installation

### Build still fails
Check that all 4 secrets are added correctly in GitHub Settings → Secrets and variables → Actions

### Want to change passwords?
Edit `create-keystore.sh` and change the passwords, then re-run it and update the GitHub secrets.

---

## For Right Now (Quick Fix):

While you set this up, just:
1. Uninstall the old app from the phone
2. Install the new version
3. After keystore setup, all future updates will work without uninstalling
