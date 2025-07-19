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

## Option 3: Firebase Realtime Database

1. Go to Firebase Console: https://console.firebase.google.com/
2. Create new project
3. Enable Realtime Database
4. Set rules to allow read/write (for testing):
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```
5. Update CloudSyncService.java:
```java
private static final String BASE_URL = "https://YOUR_PROJECT_ID-default-rtdb.firebaseio.com/";
```

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
