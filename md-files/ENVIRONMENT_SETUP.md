# Environment Configuration

This app uses environment variables to configure Firebase and other settings. This allows you to easily switch between different Firebase projects (development, staging, production) without changing the code.

## Setup Instructions

1. **Copy the example environment file:**
   ```bash
   cp .env.example .env
   ```

2. **Update the `.env` file with your Firebase configuration:**
   - Replace `EXPO_PUBLIC_API_BASE_URL` with your Firebase Realtime Database URL
   - You can find this URL in your Firebase Console under Realtime Database

3. **Firebase URL Format:**
   ```
   https://YOUR_PROJECT_ID-default-rtdb.REGION.firebasedatabase.app/
   ```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `EXPO_PUBLIC_API_BASE_URL` | Firebase Realtime Database URL | Fallback URL provided |
| `EXPO_PUBLIC_APP_NAME` | Application name | "Yoga Studio" |
| `EXPO_PUBLIC_APP_VERSION` | Application version | "1.0.0" |
| `EXPO_PUBLIC_ENABLE_PUSH_NOTIFICATIONS` | Enable push notifications | "false" |
| `EXPO_PUBLIC_ENABLE_PAYMENT_INTEGRATION` | Enable payment features | "false" |
| `EXPO_PUBLIC_ENABLE_SOCIAL_LOGIN` | Enable social login | "false" |
| `EXPO_PUBLIC_ENABLE_AUTHENTICATION` | Enable authentication | "true" |
| `EXPO_PUBLIC_ENABLE_GUEST_MODE` | Enable guest mode | "true" |

## Important Notes

- **EXPO_PUBLIC_ prefix**: In Expo/React Native, environment variables that should be available in the client code must be prefixed with `EXPO_PUBLIC_`
- **Security**: Never commit your actual `.env` file to version control. The `.env.example` file is provided as a template
- **Fallbacks**: The app includes fallback values so it will work even without a `.env` file, but you should set up proper environment variables for production

## Different Environments

You can create different environment files for different deployments:

- `.env.development` - For development
- `.env.staging` - For staging  
- `.env.production` - For production

## Getting Your Firebase URL

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to "Realtime Database" in the sidebar
4. Your URL will be displayed at the top of the page
5. It should look like: `https://yourproject-12345-default-rtdb.asia-southeast1.firebasedatabase.app/`
