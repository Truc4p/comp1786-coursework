# Cloud Server Setup Guide

## Option 1: JSONBin.io (Quickest for Testing)

1. Go to https://jsonbin.io
2. Create a free account
3. Create a new bin with initial data:
```json
{
  "yoga_classes": [],
  "class_instances": []
}
```
4. Get your bin ID from the URL
5. Update CloudSyncService.java:
```java
private static final String BASE_URL = "https://api.jsonbin.io/v3/b/YOUR_BIN_ID/";
```

## Option 2: Simple Node.js/Express Server

### 1. Create server.js:
```javascript
const express = require('express');
const cors = require('cors');
const app = express();
const port = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

let yogaClasses = [];
let classInstances = [];

// Get all yoga classes
app.get('/api/yoga-classes', (req, res) => {
  res.json(yogaClasses);
});

// Upload yoga classes
app.post('/api/yoga-classes', (req, res) => {
  yogaClasses = req.body.yoga_classes || [];
  res.json({ success: true, message: 'Yoga classes uploaded successfully' });
});

// Get all class instances
app.get('/api/class-instances', (req, res) => {
  res.json(classInstances);
});

// Upload class instances
app.post('/api/class-instances', (req, res) => {
  classInstances = req.body.class_instances || [];
  res.json({ success: true, message: 'Class instances uploaded successfully' });
});

// Sync endpoint (upload all data)
app.post('/api/sync', (req, res) => {
  const { yoga_classes, class_instances } = req.body;
  
  if (yoga_classes) yogaClasses = yoga_classes;
  if (class_instances) classInstances = class_instances;
  
  res.json({ 
    success: true, 
    message: 'Data synced successfully',
    data: {
      yoga_classes: yogaClasses,
      class_instances: classInstances
    }
  });
});

app.listen(port, () => {
  console.log(`Yoga API server running on port ${port}`);
});
```

### 2. Create package.json:
```json
{
  "name": "yoga-admin-api",
  "version": "1.0.0",
  "scripts": {
    "start": "node server.js"
  },
  "dependencies": {
    "express": "^4.18.2",
    "cors": "^2.8.5"
  }
}
```

### 3. Deploy to Railway (Free):
1. Install Railway CLI: `npm install -g @railway/cli`
2. Login: `railway login`
3. Deploy: `railway new` and follow prompts
4. Get your URL from Railway dashboard
5. Update CloudSyncService.java with your Railway URL

### 4. Deploy to Heroku:
1. Create Heroku account
2. Install Heroku CLI
3. Create app: `heroku create your-yoga-api`
4. Deploy: `git push heroku main`
5. Update CloudSyncService.java with your Heroku URL

## Option 3: Firebase Realtime Database (Recommended)

### Step-by-Step Firebase Setup:

1. **Go to Firebase Console**: https://console.firebase.google.com/
2. **Create new project**:
   - Project name: `yoga-admin-app` (or your preferred name)
   - **Enable Google Analytics**: ✅ YES (Recommended for insights and error tracking)
   - Choose your Google Analytics account or create new one
   - Accept terms and create project

3. **Add Android App**:
   - Click "Add app" → Android icon
   - Package name: `com.example.yoga_admin_app` (must match your app)
   - App nickname: `Yoga Admin App`
   - Download `google-services.json` file
   - Place the file in `app/` directory (same level as `build.gradle`)

4. **Enable Realtime Database**:
   - Go to "Realtime Database" in left sidebar
   - Click "Create Database"
   - Choose location (closest to your users)
   - Start in **test mode** (allows read/write without auth)

5. **Database Rules** (for testing):
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

6. **Get your Database URL**:
   - In Realtime Database, copy the URL (looks like: `https://yoga-admin-app-12345-default-rtdb.firebaseio.com/`)

7. **Update CloudSyncService.java**:
```java
private static final String BASE_URL = "https://YOUR_PROJECT_ID-default-rtdb.firebaseio.com/";
```

### Why Enable Google Analytics?
- **Free unlimited analytics** for your yoga admin app
- **Crashlytics integration** - automatically track app crashes and errors
- **User behavior insights** - see which features are used most
- **Performance monitoring** - identify slow operations
- **Future features** - A/B testing, user segmentation
- **No cost** and can be disabled anytime

### Firebase Integration (After downloading google-services.json):

**Add to project-level build.gradle.kts**:
```kotlin
plugins {
    // Add this line
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

**Add to app-level build.gradle.kts**:
```kotlin
plugins {
    // Add this line
    id("com.google.gms.google-services")
}

dependencies {
    // Add Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics") // If you enabled Analytics
}
```

### Testing Firebase Connection:
1. Place `google-services.json` in `app/` folder
2. Add Firebase dependencies to build.gradle
3. Update BASE_URL in CloudSyncService.java
4. Build and run your app
5. Try cloud sync - data should appear in Firebase Realtime Database

## Current Configuration in CloudSyncService.java

Update the BASE_URL constant with one of these options:

```java
// For JSONBin.io
private static final String BASE_URL = "https://api.jsonbin.io/v3/b/YOUR_BIN_ID/";

// For Firebase
private static final String BASE_URL = "https://YOUR_PROJECT_ID-default-rtdb.firebaseio.com/";

// For Railway
private static final String BASE_URL = "https://your-app-name.railway.app/api/";

// For Heroku
private static final String BASE_URL = "https://your-app-name.herokuapp.com/api/";

// For local development
private static final String BASE_URL = "http://10.0.2.2:3000/api/"; // Android emulator
// or
private static final String BASE_URL = "http://YOUR_LOCAL_IP:3000/api/"; // Real device
```

## Testing Your Setup

1. Update the BASE_URL in CloudSyncService.java
2. Build and run your app
3. Go to Cloud Sync in the app
4. Try uploading data
5. Check your cloud service to verify data was received

## Security Considerations for Production

- Add authentication (API keys, JWT tokens)
- Implement proper error handling
- Add data validation
- Use HTTPS only
- Implement rate limiting
- Add proper CORS configuration
