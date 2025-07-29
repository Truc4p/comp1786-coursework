package com.example.yoga_admin_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudSyncService {
    private static final String TAG = "CloudSyncService";
    private static final String BASE_URL = "https://yogaapp-12d2b-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String YOGA_CLASSES_ENDPOINT = "yoga-classes";
    private static final String CLASS_INSTANCES_ENDPOINT = "class-instances";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    
    public CloudSyncService(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public interface SyncCallback {
        void onSuccess(String message);
        void onError(String error);
        void onProgress(String progress);
    }
    
    /**
     * IMPROVED: Upload only changed/new classes (incremental sync)
     */
    public void uploadChangedData(SyncCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onError("No internet connection available");
            return;
        }
        
        executorService.execute(() -> {
            try {
                callback.onProgress("Checking for changes to upload...");
                
                // Get only classes that need syncing
                List<YogaClass> changedClasses = databaseHelper.getClassesNeedingSync();
                
                // Get pending deletions that need to be synced
                List<PendingDeletion> pendingDeletions = databaseHelper.getPendingDeletions();
                
                if (changedClasses.isEmpty() && pendingDeletions.isEmpty()) {
                    callback.onSuccess("No changes to upload");
                    return;
                }
                
                callback.onProgress("Uploading " + changedClasses.size() + " changed classes and " + 
                                  pendingDeletions.size() + " deletions...");
                
                int successCount = 0;
                int errorCount = 0;
                int deletedCount = 0;
                
                // First, process deletions
                for (PendingDeletion deletion : pendingDeletions) {
                    try {
                        if ("class".equals(deletion.getItemType())) {
                            boolean success = deleteClassFromCloudSync(deletion.getCloudId());
                            if (success) {
                                databaseHelper.removePendingDeletion(deletion.getId());
                                deletedCount++;
                                callback.onProgress("Deleted class from cloud " + deletedCount + "/" + pendingDeletions.size());
                            } else {
                                errorCount++;
                                Log.e(TAG, "Failed to delete class from cloud: " + deletion.getCloudId());
                            }
                        }
                        // Handle instance deletions if needed in the future
                    } catch (Exception e) {
                        errorCount++;
                        Log.e(TAG, "Error deleting from cloud: " + deletion.getCloudId(), e);
                    }
                }
                
                // Then, process uploads
                for (YogaClass yogaClass : changedClasses) {
                    try {
                        // Upload individual class
                        boolean success = uploadSingleClass(yogaClass);
                        
                        if (success) {
                            // Upload instances for this class
                            uploadClassInstances(yogaClass.getId());
                            
                            // Mark as synced in local database
                            databaseHelper.markClassAsSynced(yogaClass.getId());
                            successCount++;
                            
                            callback.onProgress("Uploaded class " + successCount + "/" + changedClasses.size());
                        } else {
                            errorCount++;
                            Log.e(TAG, "Failed to upload class: " + yogaClass.getId());
                        }
                        
                    } catch (Exception e) {
                        errorCount++;
                        Log.e(TAG, "Error uploading class: " + yogaClass.getId(), e);
                    }
                }
                
                if (errorCount == 0) {
                    String message = "Successfully uploaded " + successCount + " classes";
                    if (deletedCount > 0) {
                        message += " and deleted " + deletedCount + " classes from cloud";
                    }
                    callback.onSuccess(message);
                } else {
                    String message = "Upload completed with " + errorCount + " errors. " + 
                                   successCount + " classes uploaded";
                    if (deletedCount > 0) {
                        message += " and " + deletedCount + " classes deleted";
                    }
                    message += " successfully.";
                    callback.onError(message);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error in incremental upload", e);
                callback.onError("Upload failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Upload a single yoga class to its own Firebase path
     */
    private boolean uploadSingleClass(YogaClass yogaClass) {
        try {
            // Create individual class endpoint
            String classEndpoint = YOGA_CLASSES_ENDPOINT + "/" + yogaClass.getId() + ".json";
            
            // Create JSON for this class
            JSONObject classData = createClassJson(yogaClass);
            
            // Send to Firebase
            String response = sendDataToFirebase(classEndpoint, classData, "PUT");
            
            return response != null;
            
        } catch (Exception e) {
            Log.e(TAG, "Error uploading single class", e);
            return false;
        }
    }
    
    /**
     * Upload instances for a specific class
     */
    private boolean uploadClassInstances(long yogaClassId) {
        try {
            List<ClassInstance> instances = databaseHelper.getClassInstancesByYogaClassId(yogaClassId);
            boolean allSuccess = true;
            
            for (ClassInstance instance : instances) {
                if (instance.needsSync()) {
                    String instanceEndpoint = CLASS_INSTANCES_ENDPOINT + "/" + instance.getId() + ".json";
                    JSONObject instanceData = createInstanceJson(instance);
                    
                    String response = sendDataToFirebase(instanceEndpoint, instanceData, "PUT");
                    
                    if (response != null) {
                        databaseHelper.markInstanceAsSynced(instance.getId());
                    } else {
                        allSuccess = false;
                    }
                }
            }
            
            return allSuccess;
            
        } catch (Exception e) {
            Log.e(TAG, "Error uploading class instances", e);
            return false;
        }
    }
    
    /**
     * IMPROVED: Two-way sync - download changes first, then upload
     */
    public void performTwoWaySync(SyncCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onError("No internet connection available");
            return;
        }
        
        executorService.execute(() -> {
            try {
                // Step 1: Download and merge changes from cloud
                callback.onProgress("Downloading changes from cloud...");
                downloadAndMergeChanges(callback);
                
                // Step 2: Upload local changes
                callback.onProgress("Uploading local changes...");
                uploadChangedData(new SyncCallback() {
                    @Override
                    public void onSuccess(String message) {
                        callback.onSuccess("Two-way sync completed successfully");
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError("Upload phase failed: " + error);
                    }
                    
                    @Override
                    public void onProgress(String progress) {
                        callback.onProgress(progress);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error in two-way sync", e);
                callback.onError("Two-way sync failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Download changes and merge with local data (with conflict resolution)
     */
    private void downloadAndMergeChanges(SyncCallback callback) throws Exception {
        // Get timestamp of last sync
        long lastSyncTime = getLastSyncTimestamp();
        
        // Download all classes from Firebase
        String cloudData = downloadDataFromFirebase(YOGA_CLASSES_ENDPOINT + ".json");
        
        if (cloudData == null || cloudData.equals("null")) {
            Log.d(TAG, "No cloud data found");
            return; // No cloud data
        }
        
        Log.d(TAG, "Cloud data received: " + cloudData.substring(0, Math.min(cloudData.length(), 200)) + "...");
        
        // Firebase might return either JSONObject or JSONArray depending on data structure
        try {
            // First try to parse as JSONObject (key-value pairs)
            JSONObject cloudClasses = new JSONObject(cloudData);
            processCloudClassesFromObject(cloudClasses, lastSyncTime);
        } catch (JSONException e) {
            try {
                // If that fails, try to parse as JSONArray
                JSONArray cloudArray = new JSONArray(cloudData);
                processCloudClassesFromArray(cloudArray, lastSyncTime);
            } catch (JSONException e2) {
                Log.e(TAG, "Could not parse cloud data as JSONObject or JSONArray: " + cloudData);
                throw new Exception("Invalid cloud data format: " + e2.getMessage());
            }
        }
        
        // Update last sync timestamp
        updateLastSyncTimestamp(System.currentTimeMillis());
    }
    
    /**
     * Process cloud classes when they come as a JSONObject (key-value pairs)
     */
    private void processCloudClassesFromObject(JSONObject cloudClasses, long lastSyncTime) throws Exception {
        // Process each class from cloud
        Iterator<String> keys = cloudClasses.keys();
        while (keys.hasNext()) {
            String classId = keys.next();
            JSONObject cloudClass = cloudClasses.getJSONObject(classId);
            long cloudTimestamp = cloudClass.optLong("lastModified", 0);
            
            // Only process if cloud version is newer than our last sync
            if (cloudTimestamp > lastSyncTime) {
                long classIdLong = Long.parseLong(classId);
                processIndividualCloudClass(cloudClass, classIdLong, cloudTimestamp);
            }
        }
    }
    
    /**
     * Process cloud classes when they come as a JSONArray
     */
    private void processCloudClassesFromArray(JSONArray cloudArray, long lastSyncTime) throws Exception {
        for (int i = 0; i < cloudArray.length(); i++) {
            Object item = cloudArray.get(i);
            
            // Skip null entries in the array
            if (item == null || item == JSONObject.NULL) {
                continue;
            }
            
            if (item instanceof JSONObject) {
                JSONObject cloudClass = (JSONObject) item;
                long cloudTimestamp = cloudClass.optLong("lastModified", 0);
                
                // Only process if cloud version is newer than our last sync
                if (cloudTimestamp > lastSyncTime) {
                    // Try to get the actual ID from the JSON, don't use array index
                    long classIdLong = cloudClass.optLong("id", -1);
                    if (classIdLong != -1) {
                        processIndividualCloudClass(cloudClass, classIdLong, cloudTimestamp);
                    } else {
                        Log.w(TAG, "Cloud class at index " + i + " has no ID, skipping");
                    }
                }
            }
        }
    }
    
    /**
     * Process an individual cloud class record
     */
    private void processIndividualCloudClass(JSONObject cloudClass, long classIdLong, long cloudTimestamp) throws Exception {
        YogaClass localClass = databaseHelper.getYogaClass(classIdLong);
        
        if (localClass == null) {
            // Check if this might be a class we uploaded but don't recognize
            // by looking for matching content instead of just ID
            YogaClass potentialMatch = findMatchingLocalClass(cloudClass);
            
            if (potentialMatch != null) {
                // This is likely a class we uploaded - update it instead of creating duplicate
                Log.d(TAG, "Found matching local class " + potentialMatch.getId() + " for cloud class " + classIdLong);
                if (cloudTimestamp > potentialMatch.getLastModified()) {
                    YogaClass updatedClass = createYogaClassFromJson(cloudClass);
                    updatedClass.setId(potentialMatch.getId()); // Keep local ID
                    databaseHelper.updateYogaClass(updatedClass);
                }
            } else {
                // Truly new class from cloud - add it
                YogaClass newClass = createYogaClassFromJson(cloudClass);
                newClass.setId(classIdLong);
                databaseHelper.addYogaClass(newClass);
            }
            
        } else if (cloudTimestamp > localClass.getLastModified()) {
            // Cloud version is newer - update local
            YogaClass updatedClass = createYogaClassFromJson(cloudClass);
            updatedClass.setId(classIdLong);
            databaseHelper.updateYogaClass(updatedClass);
            
        } else if (localClass.getLastModified() > cloudTimestamp) {
            // Local version is newer - will be uploaded in next phase
            Log.d(TAG, "Local class " + classIdLong + " is newer, will upload");
        }
        // If timestamps are equal, no action needed
    }
    
    /**
     * Delete a class from Firebase
     */
    public void deleteClassFromCloud(long classId, SyncCallback callback) {
        executorService.execute(() -> {
            try {
                // Delete class
                String classEndpoint = YOGA_CLASSES_ENDPOINT + "/" + classId + ".json";
                String response = sendDataToFirebase(classEndpoint, null, "DELETE");
                
                if (response != null) {
                    // Delete associated instances
                    List<ClassInstance> instances = databaseHelper.getClassInstancesByYogaClassId(classId);
                    for (ClassInstance instance : instances) {
                        String instanceEndpoint = CLASS_INSTANCES_ENDPOINT + "/" + instance.getId() + ".json";
                        sendDataToFirebase(instanceEndpoint, null, "DELETE");
                    }
                    
                    callback.onSuccess("Class deleted from cloud");
                } else {
                    callback.onError("Failed to delete class from cloud");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error deleting class from cloud", e);
                callback.onError("Delete failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Delete a class from Firebase synchronously (for use in sync operations)
     */
    private boolean deleteClassFromCloudSync(String cloudId) {
        try {
            // Delete class
            String classEndpoint = YOGA_CLASSES_ENDPOINT + "/" + cloudId + ".json";
            String response = sendDataToFirebase(classEndpoint, null, "DELETE");
            
            if (response != null) {
                // Note: We don't delete instances here since they would have been deleted
                // when the class was deleted locally due to foreign key constraints
                Log.d(TAG, "Successfully deleted class " + cloudId + " from cloud");
                return true;
            } else {
                Log.e(TAG, "Failed to delete class " + cloudId + " from cloud");
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting class " + cloudId + " from cloud", e);
            return false;
        }
    }
    
    /**
     * Generic Firebase HTTP method (GET, PUT, POST, DELETE)
     */
    private String sendDataToFirebase(String endpoint, JSONObject data, String method) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            
            // Send data for PUT/POST requests
            if (data != null && ("PUT".equals(method) || "POST".equals(method))) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            
            int responseCode = connection.getResponseCode();
            Log.d(TAG, method + " " + endpoint + " - Response: " + responseCode);
            
            if (responseCode >= 200 && responseCode < 300) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                return response.toString();
            } else {
                Log.e(TAG, "HTTP Error: " + responseCode);
                return null;
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Network error in " + method + " " + endpoint, e);
            return null;
        }
    }
    
    /**
     * Download data from Firebase
     */
    private String downloadDataFromFirebase(String endpoint) {
        return sendDataToFirebase(endpoint, null, "GET");
    }
    
    /**
     * Create JSON object for a yoga class
     */
    private JSONObject createClassJson(YogaClass yogaClass) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", yogaClass.getId()); // Include ID for proper matching
        json.put("dayOfWeek", yogaClass.getDayOfWeek());
        json.put("time", yogaClass.getTime());
        json.put("capacity", yogaClass.getCapacity());
        json.put("duration", yogaClass.getDuration());
        json.put("price", yogaClass.getPrice());
        json.put("classType", yogaClass.getClassType());
        json.put("description", yogaClass.getDescription());
        json.put("difficulty", yogaClass.getDifficulty());
        json.put("latitude", yogaClass.getLatitude());
        json.put("longitude", yogaClass.getLongitude());
        json.put("locationAddress", yogaClass.getLocationAddress());
        json.put("lastModified", System.currentTimeMillis());
        return json;
    }
    
    /**
     * Create JSON object for a class instance
     */
    private JSONObject createInstanceJson(ClassInstance instance) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", instance.getId()); // Include ID for proper matching
        json.put("yogaClassId", instance.getYogaClassId());
        json.put("date", instance.getDate());
        json.put("instructor", instance.getInstructor());
        json.put("additionalComments", instance.getAdditionalComments());
        json.put("lastModified", System.currentTimeMillis());
        return json;
    }
    
    /**
     * Helper methods for sync timestamps
     */
    private long getLastSyncTimestamp() {
        // Get from SharedPreferences or database
        return context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
                .getLong("last_sync_time", 0);
    }
    
    private void updateLastSyncTimestamp(long timestamp) {
        context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_sync_time", timestamp)
                .apply();
    }
    
    /**
     * Find a local class that matches the cloud class by content (not just ID)
     * This helps prevent duplicates when the same class has different IDs locally vs cloud
     */
    private YogaClass findMatchingLocalClass(JSONObject cloudClass) {
        try {
            String dayOfWeek = cloudClass.optString("dayOfWeek", "");
            String time = cloudClass.optString("time", "");
            String classType = cloudClass.optString("classType", "");
            
            // Get all local classes and check for content match
            List<YogaClass> allClasses = databaseHelper.getAllYogaClasses();
            
            for (YogaClass localClass : allClasses) {
                // Match by key identifying fields
                if (localClass.getDayOfWeek().equals(dayOfWeek) &&
                    localClass.getTime().equals(time) &&
                    localClass.getClassType().equals(classType)) {
                    
                    // Additional checks to ensure it's really the same class
                    if (localClass.getCapacity() == cloudClass.optInt("capacity", 0) &&
                        Math.abs(localClass.getPrice() - cloudClass.optDouble("price", 0.0)) < 0.01) {
                        return localClass;
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error finding matching local class", e);
        }
        
        return null; // No match found
    }
    
    /**
     * Create YogaClass object from JSON (with conflict resolution)
     */
    private YogaClass createYogaClassFromJson(JSONObject json) throws JSONException {
        YogaClass yogaClass = new YogaClass();
        yogaClass.setDayOfWeek(json.optString("dayOfWeek", ""));
        yogaClass.setTime(json.optString("time", ""));
        yogaClass.setCapacity(json.optInt("capacity", 0));
        yogaClass.setDuration(json.optInt("duration", 0));
        yogaClass.setPrice(json.optDouble("price", 0.0));
        yogaClass.setClassType(json.optString("classType", ""));
        yogaClass.setDescription(json.optString("description", ""));
        yogaClass.setDifficulty(json.optString("difficulty", ""));
        yogaClass.setLatitude(json.optDouble("latitude", 0.0));
        yogaClass.setLongitude(json.optDouble("longitude", 0.0));
        yogaClass.setLocationAddress(json.optString("locationAddress", ""));
        yogaClass.setLastModified(json.optLong("lastModified", System.currentTimeMillis()));
        yogaClass.setNeedsSync(false); // From cloud, so doesn't need sync
        
        // Set cloudId to the ID from JSON for future matching
        if (json.has("id")) {
            yogaClass.setCloudId(String.valueOf(json.getLong("id")));
        }
        
        return yogaClass;
    }
    
    /**
     * Create ClassInstance object from JSON (with conflict resolution)
     */
    private ClassInstance createInstanceFromJson(JSONObject json, long yogaClassId) throws JSONException {
        ClassInstance instance = new ClassInstance();
        instance.setYogaClassId(yogaClassId);
        instance.setDate(json.getString("date"));
        instance.setInstructor(json.getString("instructor"));
        instance.setAdditionalComments(json.optString("additionalComments", ""));
        instance.setLastModified(json.optLong("lastModified", System.currentTimeMillis()));
        instance.setNeedsSync(false); // From cloud, so doesn't need sync
        return instance;
    }
    
    /**
     * Get a simple device identifier for sync tracking
     */
    private String getDeviceId() {
        // In a real app, you might use a more sophisticated device ID
        return android.provider.Settings.Secure.getString(
            context.getContentResolver(),
            android.provider.Settings.Secure.ANDROID_ID
        );
    }
    
    /**
     * Check if cloud service is reachable (Firebase format)
     */
    public void checkCloudConnection(SyncCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onError("No internet connection available");
            return;
        }
        
        executorService.execute(() -> {
            try {
                // Test Firebase connection by trying to read from root
                String response = downloadDataFromFirebase(".json");
                
                if (response != null) {
                    callback.onSuccess("Firebase cloud service is reachable");
                } else {
                    callback.onError("Cannot reach Firebase cloud service");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Firebase connection test failed", e);
                callback.onError("Cannot reach Firebase cloud service: " + e.getMessage());
            }
        });
    }
    
    /**
     * Batch sync all changes (most efficient method)
     * This combines upload and download in one operation
     */
    public void performBatchSync(SyncCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onError("No internet connection available");
            return;
        }
        
        executorService.execute(() -> {
            try {
                callback.onProgress("Starting batch synchronization...");
                
                // Step 1: Get local changes
                List<YogaClass> localChanges = databaseHelper.getClassesNeedingSync();
                List<ClassInstance> localInstanceChanges = databaseHelper.getInstancesNeedingSync();
                
                // Step 2: Download cloud changes first
                callback.onProgress("Downloading cloud changes...");
                downloadAndMergeChanges(callback);
                
                // Step 3: Upload local changes
                callback.onProgress("Uploading local changes...");
                int uploadedClasses = 0;
                int uploadedInstances = 0;
                
                // Upload classes
                for (YogaClass yogaClass : localChanges) {
                    if (uploadSingleClass(yogaClass)) {
                        databaseHelper.markClassAsSynced(yogaClass.getId());
                        uploadedClasses++;
                    }
                }
                
                // Upload instances
                for (ClassInstance instance : localInstanceChanges) {
                    String instanceEndpoint = CLASS_INSTANCES_ENDPOINT + "/" + instance.getId() + ".json";
                    JSONObject instanceData = createInstanceJson(instance);
                    
                    if (sendDataToFirebase(instanceEndpoint, instanceData, "PUT") != null) {
                        databaseHelper.markInstanceAsSynced(instance.getId());
                        uploadedInstances++;
                    }
                }
                
                // Step 4: Update sync timestamp
                updateLastSyncTimestamp(System.currentTimeMillis());
                
                String message = String.format("Batch sync completed successfully. " +
                        "Uploaded: %d classes, %d instances", uploadedClasses, uploadedInstances);
                callback.onSuccess(message);
                
            } catch (Exception e) {
                Log.e(TAG, "Error in batch sync", e);
                callback.onError("Batch sync failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Force sync all data (emergency method)
     * Use this only when incremental sync fails
     */
    public void forceFullSync(SyncCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onError("No internet connection available");
            return;
        }
        
        executorService.execute(() -> {
            try {
                callback.onProgress("Starting full sync (this may take time)...");
                
                // Mark all records as needing sync
                markAllRecordsForSync();
                
                // Perform batch sync
                performBatchSync(new SyncCallback() {
                    @Override
                    public void onSuccess(String message) {
                        callback.onSuccess("Full sync completed: " + message);
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError("Full sync failed: " + error);
                    }
                    
                    @Override
                    public void onProgress(String progress) {
                        callback.onProgress("Full sync: " + progress);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error in force full sync", e);
                callback.onError("Force full sync failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Mark all local records as needing sync
     */
    private void markAllRecordsForSync() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        
        // Mark all classes as needing sync
        ContentValues classValues = new ContentValues();
        classValues.put("needs_sync", 1);
        classValues.put("last_modified", System.currentTimeMillis());
        db.update("yoga_classes", classValues, null, null);
        
        // Mark all instances as needing sync
        ContentValues instanceValues = new ContentValues();
        instanceValues.put("needs_sync", 1);
        instanceValues.put("last_modified", System.currentTimeMillis());
        db.update("class_instances", instanceValues, null, null);
        
        db.close();
    }
    
    /**
     * Get sync statistics for debugging/monitoring
     */
    public void getSyncStats(SyncCallback callback) {
        executorService.execute(() -> {
            try {
                List<YogaClass> classesNeedingSync = databaseHelper.getClassesNeedingSync();
                List<ClassInstance> instancesNeedingSync = databaseHelper.getInstancesNeedingSync();
                long lastSyncTime = getLastSyncTimestamp();
                
                String stats = String.format(
                    "Sync Statistics:\n" +
                    "Classes needing sync: %d\n" +
                    "Instances needing sync: %d\n" +
                    "Last sync: %s\n" +
                    "Device ID: %s",
                    classesNeedingSync.size(),
                    instancesNeedingSync.size(),
                    lastSyncTime > 0 ? new java.util.Date(lastSyncTime).toString() : "Never",
                    getDeviceId()
                );
                
                callback.onSuccess(stats);
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting sync stats", e);
                callback.onError("Failed to get sync stats: " + e.getMessage());
            }
        });
    }
    
    /**
     * Debug method to inspect Firebase data structure
     */
    public void inspectFirebaseData(SyncCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onError("No internet connection available");
            return;
        }
        
        executorService.execute(() -> {
            try {
                callback.onProgress("Inspecting Firebase data structure...");
                
                // Download raw data from Firebase
                String cloudData = downloadDataFromFirebase(YOGA_CLASSES_ENDPOINT + ".json");
                
                if (cloudData == null || cloudData.equals("null")) {
                    callback.onSuccess("Firebase data: No data found (null)");
                    return;
                }
                
                // Determine data type
                String dataType = "Unknown";
                String summary = "";
                
                try {
                    JSONObject obj = new JSONObject(cloudData);
                    dataType = "JSONObject (key-value pairs)";
                    summary = "Keys: " + obj.length();
                } catch (JSONException e1) {
                    try {
                        JSONArray arr = new JSONArray(cloudData);
                        dataType = "JSONArray (indexed array)";
                        summary = "Length: " + arr.length();
                    } catch (JSONException e2) {
                        dataType = "Raw string";
                        summary = "Length: " + cloudData.length();
                    }
                }
                
                String result = String.format(
                    "Firebase Data Inspection:\n" +
                    "Type: %s\n" +
                    "Summary: %s\n" +
                    "First 300 chars: %s",
                    dataType,
                    summary,
                    cloudData.length() > 300 ? cloudData.substring(0, 300) + "..." : cloudData
                );
                
                callback.onSuccess(result);
                
            } catch (Exception e) {
                Log.e(TAG, "Error inspecting Firebase data", e);
                callback.onError("Inspection failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Clean up duplicate classes by identifying and removing classes with identical content
     * Use this if you already have duplicates from previous sync issues
     */
    public void cleanupDuplicateClasses(SyncCallback callback) {
        executorService.execute(() -> {
            try {
                callback.onProgress("Scanning for duplicate classes...");
                
                List<YogaClass> allClasses = databaseHelper.getAllYogaClasses();
                int duplicatesRemoved = 0;
                
                // Group classes by their key identifying features
                for (int i = 0; i < allClasses.size(); i++) {
                    YogaClass class1 = allClasses.get(i);
                    if (class1 == null) continue; // Already processed
                    
                    for (int j = i + 1; j < allClasses.size(); j++) {
                        YogaClass class2 = allClasses.get(j);
                        if (class2 == null) continue; // Already processed
                        
                        // Check if these are duplicates
                        if (areClassesDuplicates(class1, class2)) {
                            // Keep the one with the earlier ID (likely original)
                            YogaClass toRemove = (class1.getId() < class2.getId()) ? class2 : class1;
                            YogaClass toKeep = (class1.getId() < class2.getId()) ? class1 : class2;
                            
                            Log.d(TAG, "Removing duplicate class " + toRemove.getId() + 
                                       ", keeping " + toKeep.getId());
                            
                            // Transfer any instances to the kept class
                            List<ClassInstance> instances = databaseHelper.getClassInstancesByYogaClassId(toRemove.getId());
                            for (ClassInstance instance : instances) {
                                instance.setYogaClassId(toKeep.getId());
                                databaseHelper.updateClassInstance(instance);
                            }
                            
                            // Remove the duplicate
                            databaseHelper.deleteYogaClass(toRemove.getId());
                            duplicatesRemoved++;
                            
                            // Mark as processed
                            if (toRemove == class1) {
                                allClasses.set(i, null);
                            } else {
                                allClasses.set(j, null);
                            }
                        }
                    }
                }
                
                if (duplicatesRemoved > 0) {
                    callback.onSuccess("Cleaned up " + duplicatesRemoved + " duplicate classes");
                } else {
                    callback.onSuccess("No duplicate classes found");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning duplicates", e);
                callback.onError("Cleanup failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Check if two yoga classes are duplicates (same content, different IDs)
     */
    private boolean areClassesDuplicates(YogaClass class1, YogaClass class2) {
        return class1.getDayOfWeek().equals(class2.getDayOfWeek()) &&
               class1.getTime().equals(class2.getTime()) &&
               class1.getClassType().equals(class2.getClassType()) &&
               class1.getCapacity() == class2.getCapacity() &&
               Math.abs(class1.getPrice() - class2.getPrice()) < 0.01 &&
               class1.getDuration() == class2.getDuration() &&
               class1.getDescription().equals(class2.getDescription()) &&
               class1.getDifficulty().equals(class2.getDifficulty());
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
