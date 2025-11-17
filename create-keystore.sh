#!/bin/bash

# This script creates a keystore for signing your Android app
# It's completely FREE and takes 1 minute

echo "=== Android App Keystore Generator ==="
echo ""
echo "This will create a signing key for your app (completely FREE)"
echo "You'll need to answer a few questions:"
echo ""

# Create keystore directory
mkdir -p keystore

# Generate the keystore
keytool -genkey -v \
  -keystore keystore/release-key.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias release-key \
  -storepass "YourStorePassword123!" \
  -keypass "YourKeyPassword123!" \
  -dname "CN=GPS Tracker, OU=Mobile, O=YourCompany, L=City, S=State, C=ZA"

echo ""
echo "=== Keystore Created Successfully! ==="
echo ""
echo "File location: keystore/release-key.jks"
echo "Store Password: YourStorePassword123!"
echo "Key Password: YourKeyPassword123!"
echo "Alias: release-key"
echo ""
echo "IMPORTANT: Keep these passwords safe!"
echo "Next: Add these to GitHub Secrets (I'll show you how)"
echo ""
