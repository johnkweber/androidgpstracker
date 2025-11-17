#!/bin/bash

# GitHub Setup Script
# Replace YOUR_USERNAME and YOUR_REPO with your actual GitHub username and repository name

echo "=== GPS Tracker - GitHub Setup ==="
echo ""
echo "Before running this script:"
echo "1. Create a GitHub account at https://github.com"
echo "2. Create a new repository (DO NOT initialize with README)"
echo "3. Copy your repository URL"
echo ""
read -p "Enter your GitHub username: " GITHUB_USER
read -p "Enter your repository name: " REPO_NAME
echo ""

# Initialize git
echo "Initializing git repository..."
git init

# Add all files
echo "Adding files..."
git add .

# Commit
echo "Creating initial commit..."
git commit -m "Initial commit: GPS tracking app for staff phones"

# Rename branch to main
echo "Setting up main branch..."
git branch -M main

# Add remote
echo "Adding GitHub remote..."
git remote add origin "https://github.com/$GITHUB_USER/$REPO_NAME.git"

# Push to GitHub
echo "Pushing to GitHub..."
echo "You may be asked to enter your GitHub credentials..."
git push -u origin main

echo ""
echo "=== Setup Complete! ==="
echo ""
echo "Next steps:"
echo "1. Go to https://github.com/$GITHUB_USER/$REPO_NAME"
echo "2. Click 'Actions' tab"
echo "3. Wait 2-3 minutes for the build to complete"
echo "4. Download your APK from the completed workflow"
echo ""
