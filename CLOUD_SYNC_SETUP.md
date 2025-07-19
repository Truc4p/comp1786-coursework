# Cloud Synchronization Setup Guide

## Overview
The Yoga Admin App now includes cloud synchronization functionality that allows users to:
- Upload all yoga class data to a cloud-based web service
- Download and synchronize data from the cloud
- Check network connectivity before operations
- Handle automatic conflict resolution

## Features Implemented

### 1. Network Connectivity Check
- **NetworkUtils.java**: Utility class that checks internet connectivity
- Supports WiFi, Mobile Data, and Ethernet connections
- Works on both new and legacy Android versions

### 2. Cloud Sync Service
- **CloudSyncService.java**: Core service for cloud operations
- JSON-based data format for API communication
- Asynchronous operations with progress callbacks
- Error handling and network timeout management

### 3. Cloud Sync Activity
- **CloudSyncActivity.java**: User interface for cloud operations
- Real-time network status display
- Progress dialogs for upload/download operations
- Confirmation dialogs for data operations

### 4. Data Synchronization
- Upload: Sends all local yoga classes and instances to cloud
- Download: Retrieves cloud data and merges with local database
- Conflict Resolution: Cloud data takes precedence (configurable)
- Preserves referential integrity between classes and instances

## Setup Instructions

### 1. Cloud Service Configuration
Update the `BASE_URL` in `CloudSyncService.java`:
```java
private static final String BASE_URL = "https://your-actual-cloud-service.com/api/";
```

### 2. API Endpoints Required
Your cloud service should implement these endpoints:

#### POST /sync
- Accepts JSON payload with yoga classes and instances
- Returns success/error response

#### GET /sync?deviceId={id}
- Returns JSON with all yoga classes and instances
- Filters by device ID if needed

#### GET /health
- Simple health check endpoint
- Returns HTTP 200 if service is operational

### 3. JSON Data Format

#### Upload Format:
```json
{
  "yogaClasses": [
    {
      "id": 1,
      "dayOfWeek": "Monday",
      "time": "10:00 AM",
      "capacity": 20,
      "duration": 60,
      "price": 15.00,
      "classType": "Hatha Yoga",
      "description": "Beginner-friendly class",
      "difficulty": "Beginner",
      "instances": [
        {
          "id": 1,
          "yogaClassId": 1,
          "date": "21/07/2025",
          "instructor": "Jane Doe",
          "additionalComments": "Bring your own mat"
        }
      ]
    }
  ],
  "timestamp": 1642598400000,
  "deviceId": "unique-device-id"
}
```

### 4. Security Considerations
- Add authentication headers to HTTP requests
- Implement device registration/authentication
- Use HTTPS for all communications
- Validate all incoming data on server side

### 5. Testing the Implementation

#### Local Testing:
1. Use a local server (e.g., Node.js, Python Flask)
2. Update BASE_URL to your local IP
3. Implement basic endpoints for testing

#### Mock Service:
```javascript
// Example Node.js Express mock service
app.post('/api/sync', (req, res) => {
  console.log('Received data:', req.body);
  res.json({ success: true, message: 'Data uploaded successfully' });
});

app.get('/api/sync', (req, res) => {
  res.json({ yogaClasses: [] }); // Return mock data
});

app.get('/api/health', (req, res) => {
  res.json({ status: 'healthy' });
});
```

## Usage Instructions

### For Users:
1. Open the app and tap "Cloud Sync" from the main menu
2. Check network status (displayed at top)
3. Use "Test Cloud Connection" to verify service availability
4. Use "Upload All Data to Cloud" to backup local data
5. Use "Download & Sync from Cloud" to restore/sync data

### Network Requirements:
- Active internet connection (WiFi or Mobile Data)
- Access to the configured cloud service URL
- Minimum 1MB of available bandwidth for typical operations

## Error Handling

The app handles various error scenarios:
- No internet connection
- Cloud service unavailable
- Network timeouts
- Invalid data format
- Server errors

Users receive clear error messages and can retry operations.

## Future Enhancements

Possible improvements:
1. **Authentication**: User login/registration system
2. **Incremental Sync**: Only sync changed data
3. **Conflict Resolution**: More sophisticated merge strategies
4. **Offline Queue**: Queue operations when offline
5. **Encryption**: Encrypt sensitive data before upload
6. **Backup Scheduling**: Automatic periodic backups

## Technical Notes

- Uses Android's `HttpURLConnection` for HTTP operations
- All network operations run on background threads
- Progress updates are posted to main UI thread
- Database operations maintain ACID properties
- Foreign key constraints are preserved during sync

## Support

For implementation support:
1. Check network connectivity first
2. Verify cloud service endpoints are accessible
3. Review app logs for detailed error messages
4. Test with a simple mock service before using production endpoints
