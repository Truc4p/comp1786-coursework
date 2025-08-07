# Firebase Configuration Setup

## Security Notice
This project uses a secure configuration approach to prevent sensitive Firebase information from being exposed in the source code.

## Setup Instructions

1. **Copy the template file:**
   ```bash
   cp app/firebase.properties.template app/firebase.properties
   ```

2. **Edit the firebase.properties file:**
   - Open `app/firebase.properties`
   - Replace the placeholder values with your actual Firebase project details
   - Example:
     ```
     FIREBASE_PROJECT_ID=yogaapp-29e1f
     FIREBASE_DATABASE_REGION=asia-southeast1
     ```

3. **Never commit firebase.properties:**
   - The `firebase.properties` file is already included in `.gitignore`
   - This prevents your sensitive Firebase configuration from being uploaded to GitHub

## How it Works

- Firebase configuration is loaded from `firebase.properties` at build time
- The values are injected into `BuildConfig` fields
- The application uses `FirebaseConfig.getFirebaseDatabaseUrl()` to get the secure URL
- If `firebase.properties` is missing, it falls back to reading from `google-services.json`

## Files to Keep Private

- `app/firebase.properties` - Contains your Firebase project configuration
- `app/google-services.json` - Contains Firebase service account information

Both files are excluded from version control via `.gitignore`.
