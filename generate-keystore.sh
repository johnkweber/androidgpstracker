#!/bin/bash

echo "Generating keystore for Android app..."

cd "/mnt/c/downloads/leon nel/gps android app"

# Create keystore directory
mkdir -p keystore

# Generate private key and certificate
openssl req -newkey rsa:2048 -nodes \
  -keyout keystore/private.key \
  -x509 -days 10000 \
  -out keystore/certificate.crt \
  -subj "/C=ZA/ST=State/L=City/O=Company/OU=Mobile/CN=GPSTracker"

echo "Certificate created..."

# Convert to PKCS12 format
openssl pkcs12 -export \
  -in keystore/certificate.crt \
  -inkey keystore/private.key \
  -out keystore/release-key.jks \
  -name release-key \
  -password pass:YourStorePassword123!

echo "Keystore created!"

# List files
ls -lh keystore/

# Convert to base64
base64 -w 0 keystore/release-key.jks > keystore-base64.txt

echo ""
echo "Success! Keystore created at: keystore/release-key.jks"
echo "Base64 version saved to: keystore-base64.txt"
echo ""
echo "Now run: cat keystore-base64.txt"
echo "And copy the output to add to GitHub Secrets"
