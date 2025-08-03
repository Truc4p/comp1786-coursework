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
import java.util.ArrayList;
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
                
                // Get class instances that need syncing
                List<ClassInstance> changedInstances = databaseHelper.getInstancesNeedingSync();
                
                // Get pending deletions that need to be synced
                List<PendingDeletion> pendingDeletions = databaseHelper.getPendingDeletions();
                
                // Enhanced logging for debugging
                Log.d(TAG, "Found " + changedClasses.size() + " classes needing sync");
                Log.d(TAG, "Found " + changedInstances.size() + " class instances needing sync");
                Log.d(TAG, "Found " + pendingDeletions.size() + " pending deletions");
                
                for (YogaClass cls : changedClasses) {
                    Log.d(TAG, "Class needing sync: ID=" + cls.getId() + ", Type=" + cls.getClassType() + ", NeedsSync=" + cls.needsSync());
                }
                
                for (ClassInstance inst : changedInstances) {
                    Log.d(TAG, "Instance needing sync: ID=" + inst.getId() + ", ClassID=" + inst.getYogaClassId() + ", Date=" + inst.getDate());
                }
                
                if (changedClasses.isEmpty() && changedInstances.isEmpty() && pendingDeletions.isEmpty()) {
                    // Let's also check total classes in database for debugging
                    List<YogaClass> allClasses = databaseHelper.getAllYogaClasses();
                    Log.d(TAG, "Total classes in database: " + allClasses.size());
                    
                    callback.onSuccess("No changes to upload. Total classes: " + allClasses.size() + ", Total instances: " + 
                                     databaseHelper.getAllClassInstances().size());
                    return;
                }
                
                callback.onProgress("Uploading " + changedClasses.size() + " changed classes, " + 
                                  changedInstances.size() + " class instances, and " + 
                                  pendingDeletions.size() + " deletions...");
                
                int successCount = 0;
                int errorCount = 0;
                int deletedCount = 0;
                int instanceSuccessCount = 0;
                
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
                
                // Then, process class uploads
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
                
                // Finally, process standalone class instance uploads
                for (ClassInstance instance : changedInstances) {
                    try {
                        boolean success = uploadSingleClassInstance(instance);
                        
                        if (success) {
                            databaseHelper.markInstanceAsSynced(instance.getId());
                            instanceSuccessCount++;
                            
                            callback.onProgress("Uploaded instance " + instanceSuccessCount + "/" + changedInstances.size());
                        } else {
                            errorCount++;
                            Log.e(TAG, "Failed to upload class instance: " + instance.getId());
                        }
                        
                    } catch (Exception e) {
                        errorCount++;
                        Log.e(TAG, "Error uploading class instance: " + instance.getId(), e);
                    }
                }
                
                if (errorCount == 0) {
                    String message = "Successfully uploaded " + successCount + " classes";
                    if (instanceSuccessCount > 0) {
                        message += " and " + instanceSuccessCount + " class instances";
                    }
                    if (deletedCount > 0) {
                        message += " and deleted " + deletedCount + " classes from cloud";
                    }
                    callback.onSuccess(message);
                } else {
                    String message = "Upload completed with " + errorCount + " errors. " + 
                                   successCount + " classes";
                    if (instanceSuccessCount > 0) {
                        message += " and " + instanceSuccessCount + " instances";
                    }
                    message += " uploaded";
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
     * Upload a single class instance
     */
    private boolean uploadSingleClassInstance(ClassInstance instance) {
        try {
            String instanceEndpoint = CLASS_INSTANCES_ENDPOINT + "/" + instance.getId() + ".json";
            JSONObject instanceData = createInstanceJson(instance);
            
            String response = sendDataToFirebase(instanceEndpoint, instanceData, "PUT");
            
            return response != null;
            
        } catch (Exception e) {
            Log.e(TAG, "Error uploading single class instance", e);
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
    
    // ==================== BOOKING SYNCHRONIZATION METHODS ====================
    
    /**
     * Sync bookings from Firebase (read-only for admin app)
     * Admin app only reads bookings created by customer app
     */
    public void syncBookingsFromFirebase(SyncCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onError("No internet connection available");
            return;
        }
        
        executorService.execute(() -> {
            try {
                callback.onProgress("Downloading bookings from cloud...");
                
                // Get bookings from Firebase
                String bookingsUrl = BASE_URL + "bookings.json";
                Log.d(TAG, "Requesting bookings from URL: " + bookingsUrl);
                String response = makeGetRequest(bookingsUrl);
                
                Log.d(TAG, "Raw Firebase response: " + response);
                
                if (response == null || response.equals("null") || response.trim().isEmpty()) {
                    callback.onSuccess("No bookings found in cloud");
                    return;
                }
                
                JSONObject bookingsJson = new JSONObject(response);
                int syncedCount = 0;
                int newCount = 0;
                
                Log.d(TAG, "Processing " + bookingsJson.length() + " bookings from Firebase");
                
                Iterator<String> keys = bookingsJson.keys();
                while (keys.hasNext()) {
                    String firebaseKey = keys.next();
                    JSONObject bookingJson = bookingsJson.getJSONObject(firebaseKey);
                    
                    // Convert JSON to Booking object, using Firebase key as fallback ID
                    Booking booking = jsonToBooking(bookingJson, firebaseKey);
                    if (booking != null) {
                        Log.d(TAG, "Processing booking: " + booking.getBookingId());
                        
                        // Skip bookings with empty or null booking IDs
                        if (booking.getBookingId() == null || booking.getBookingId().trim().isEmpty()) {
                            Log.w(TAG, "Skipping booking with empty booking ID");
                            continue;
                        }
                        
                        // Check if booking already exists using efficient method
                        if (databaseHelper.bookingExists(booking.getBookingId())) {
                            // Get existing booking to compare timestamps
                            Booking existingBooking = databaseHelper.getBookingByBookingId(booking.getBookingId());
                            if (existingBooking != null && booking.getLastModified() > existingBooking.getLastModified()) {
                                updateBookingInDatabase(booking);
                                syncedCount++;
                                Log.d(TAG, "Updated existing booking: " + booking.getBookingId());
                            } else {
                                Log.d(TAG, "Booking already up to date: " + booking.getBookingId());
                            }
                        } else {
                            // Add new booking
                            long result = databaseHelper.addBooking(booking);
                            if (result != -1) {
                                newCount++;
                                Log.d(TAG, "Added new booking: " + booking.getBookingId() + " with DB id: " + result);
                            } else {
                                Log.e(TAG, "Failed to add booking: " + booking.getBookingId());
                            }
                        }
                    } else {
                        Log.e(TAG, "Failed to convert JSON to booking for key: " + firebaseKey);
                    }
                }
                
                // HANDLE DELETIONS: Remove local bookings that no longer exist in Firebase
                callback.onProgress("Checking for deleted bookings...");
                int deletedCount = syncBookingDeletions(bookingsJson);
                
                String message = String.format("Bookings sync completed. %d new, %d updated, %d deleted", 
                    newCount, syncedCount, deletedCount);
                callback.onSuccess(message);
                
            } catch (Exception e) {
                Log.e(TAG, "Error syncing bookings from Firebase", e);
                callback.onError("Failed to sync bookings: " + e.getMessage());
            }
        });
    }
    
    /**
     * Sync booking deletions: Remove local bookings that no longer exist in Firebase
     */
    private int syncBookingDeletions(JSONObject firebaseBookings) {
        try {
            Log.d(TAG, "=== CHECKING FOR DELETED BOOKINGS ===");
            
            // Get all booking IDs currently in Firebase
            List<String> firebaseBookingIds = new ArrayList<>();
            Iterator<String> firebaseKeys = firebaseBookings.keys();
            while (firebaseKeys.hasNext()) {
                String firebaseKey = firebaseKeys.next();
                JSONObject bookingJson = firebaseBookings.getJSONObject(firebaseKey);
                
                // Extract booking ID from the booking data
                String bookingId = bookingJson.optString("bookingId", "");
                if (bookingId.isEmpty()) {
                    bookingId = bookingJson.optString("id", "");
                    if (bookingId.isEmpty()) {
                        bookingId = firebaseKey; // Use Firebase key as fallback
                    }
                }
                
                if (!bookingId.trim().isEmpty()) {
                    firebaseBookingIds.add(bookingId);
                }
            }
            
            Log.d(TAG, "Firebase has " + firebaseBookingIds.size() + " bookings");
            
            // Get all booking IDs from local database
            List<String> localBookingIds = databaseHelper.getAllBookingIds();
            Log.d(TAG, "Local database has " + localBookingIds.size() + " bookings");
            
            // Find bookings that exist locally but not in Firebase (deleted bookings)
            List<String> bookingsToDelete = new ArrayList<>();
            for (String localId : localBookingIds) {
                if (!firebaseBookingIds.contains(localId)) {
                    bookingsToDelete.add(localId);
                    Log.d(TAG, "Booking marked for deletion: " + localId);
                }
            }
            
            // Delete the bookings that no longer exist in Firebase
            int deletedCount = 0;
            for (String bookingIdToDelete : bookingsToDelete) {
                int result = databaseHelper.deleteBookingByBookingId(bookingIdToDelete);
                if (result > 0) {
                    deletedCount++;
                    Log.d(TAG, "Successfully deleted booking: " + bookingIdToDelete);
                } else {
                    Log.w(TAG, "Failed to delete booking: " + bookingIdToDelete);
                }
            }
            
            Log.d(TAG, "=== DELETION SYNC COMPLETE: " + deletedCount + " bookings deleted ===");
            return deletedCount;
            
        } catch (Exception e) {
            Log.e(TAG, "Error during booking deletion sync", e);
            return 0;
        }
    }
    
    /**
     * Update booking status in Firebase (admin can update booking status)
     */
    public void updateBookingStatusInFirebase(String bookingId, String newStatus, SyncCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onError("No internet connection available");
            return;
        }
        
        executorService.execute(() -> {
            try {
                callback.onProgress("Updating booking status in cloud...");
                
                // First find the booking in Firebase
                String bookingsUrl = BASE_URL + "bookings.json";
                String response = makeGetRequest(bookingsUrl);
                
                if (response == null || response.equals("null")) {
                    callback.onError("Booking not found in cloud");
                    return;
                }
                
                JSONObject bookingsJson = new JSONObject(response);
                String firebaseKey = null;
                
                // Find the booking by bookingId
                Iterator<String> keys = bookingsJson.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject bookingJson = bookingsJson.getJSONObject(key);
                    
                    // Try multiple field names for booking ID
                    String currentBookingId = "";
                    if (bookingJson.has("id")) {
                        currentBookingId = bookingJson.getString("id");
                    } else if (bookingJson.has("bookingId")) {
                        currentBookingId = bookingJson.getString("bookingId");
                    }
                    
                    Log.d(TAG, "Checking Firebase booking with key: " + key + ", id: " + currentBookingId);
                    
                    if (currentBookingId.equals(bookingId)) {
                        firebaseKey = key;
                        Log.d(TAG, "Found matching booking with Firebase key: " + firebaseKey);
                        break;
                    }
                }
                
                if (firebaseKey == null) {
                    callback.onError("Booking not found in cloud");
                    return;
                }
                
                // Update the booking status
                String updateUrl = BASE_URL + "bookings/" + firebaseKey + ".json";
                JSONObject updateData = new JSONObject();
                updateData.put("status", newStatus);
                updateData.put("updatedAt", System.currentTimeMillis());
                updateData.put("lastModified", System.currentTimeMillis());
                
                String updateResponse = makePatchRequest(updateUrl, updateData.toString());
                
                if (updateResponse != null) {
                    // Update local database
                    databaseHelper.updateBookingStatus(bookingId, newStatus);
                    callback.onSuccess("Booking status updated successfully");
                } else {
                    callback.onError("Failed to update booking status in cloud");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error updating booking status", e);
                callback.onError("Failed to update booking status: " + e.getMessage());
            }
        });
    }
    
    /**
     * Convert JSON object to Booking
     */
    private Booking jsonToBooking(JSONObject json, String firebaseKey) {
        try {
            // Log the raw JSON to see what fields are available
            Log.d(TAG, "=== PARSING BOOKING ===");
            Log.d(TAG, "Firebase key: " + firebaseKey);
            Log.d(TAG, "Raw Firebase booking JSON: " + json.toString());
            Log.d(TAG, "JSON keys: " + json.keys().toString());
            
            Booking booking = new Booking();
            
            // Extract booking ID, use Firebase key as fallback
            String bookingId = json.optString("bookingId", "");
            if (bookingId.isEmpty()) {
                // Try alternative field names
                bookingId = json.optString("id", "");
                if (bookingId.isEmpty()) {
                    bookingId = firebaseKey; // Use Firebase key as fallback
                    Log.d(TAG, "Using Firebase key as booking ID: " + firebaseKey);
                }
            }
            Log.d(TAG, "Final bookingId: '" + bookingId + "'");
            
            // Extract customer information from nested customerInfo object
            String customerName = "";
            String customerEmail = "";
            String customerPhone = "";
            
            if (json.has("customerInfo")) {
                JSONObject customerInfo = json.optJSONObject("customerInfo");
                if (customerInfo != null) {
                    customerName = customerInfo.optString("name", "");
                    customerEmail = customerInfo.optString("email", "");
                    customerPhone = customerInfo.optString("phone", "");
                    Log.d(TAG, "Extracted customer info from nested object");
                }
            }
            
            // Try direct fields as fallback
            if (customerName.isEmpty()) {
                customerName = json.optString("customerName", json.optString("name", ""));
            }
            if (customerEmail.isEmpty()) {
                customerEmail = json.optString("customerEmail", json.optString("email", ""));
            }
            if (customerPhone.isEmpty()) {
                customerPhone = json.optString("customerPhone", json.optString("phone", ""));
            }
            
            // Extract booking-level information
            String bookingDate = json.optString("bookingDate", json.optString("date", ""));
            String bookingTime = json.optString("bookingTime", json.optString("time", ""));
            
            // Format the booking date if it's in ISO format
            if (!bookingDate.isEmpty() && bookingDate.contains("T")) {
                bookingDate = formatISODate(bookingDate);
            }
            
            // Extract payment information
            double totalAmount = json.optDouble("totalAmount", 0.0);
            if (totalAmount == 0.0) {
                // Try alternative field names
                totalAmount = json.optDouble("paymentAmount", json.optDouble("amount", 0.0));
            }
            
            // Try to extract class information from structure
            String className = "";
            String classTime = "";
            String instructor = "";
            List<String> allClassNames = new ArrayList<>();
            
            // Check if there's a "classes" array (not object)
            if (json.has("classes")) {
                JSONArray classesArray = json.optJSONArray("classes");
                if (classesArray != null && classesArray.length() > 0) {
                    // Extract all classes from the array
                    for (int i = 0; i < classesArray.length(); i++) {
                        JSONObject classInfo = classesArray.optJSONObject(i);
                        if (classInfo != null) {
                            // Extract class details
                            String classType = classInfo.optString("name", classInfo.optString("type", classInfo.optString("classType", "")));
                            String level = classInfo.optString("level", classInfo.optString("difficulty", ""));
                            
                            // Build class name from available info
                            String currentClassName = "";
                            if (!classType.isEmpty() && !level.isEmpty()) {
                                currentClassName = classType + " (" + level + ")";
                            } else if (!classType.isEmpty()) {
                                currentClassName = classType;
                            } else if (!level.isEmpty()) {
                                currentClassName = level + " Yoga";
                            } else {
                                currentClassName = "Yoga Class";
                            }
                            
                            // Add to all classes list
                            allClassNames.add(currentClassName);
                            
                            // Use first class as primary class name
                            if (i == 0) {
                                className = currentClassName;
                                instructor = classInfo.optString("instructor", "");
                                String time = classInfo.optString("time", "");
                                
                                // Use class time if booking time is empty
                                if (bookingTime.isEmpty() && !time.isEmpty()) {
                                    bookingTime = time;
                                }
                            }
                        }
                    }
                    
                    Log.d(TAG, "Extracted " + allClassNames.size() + " classes from array:");
                    for (int i = 0; i < allClassNames.size(); i++) {
                        Log.d(TAG, "  Class " + (i + 1) + ": '" + allClassNames.get(i) + "'");
                    }
                    Log.d(TAG, "  Primary className: '" + className + "'");
                    Log.d(TAG, "  instructor: '" + instructor + "'");
                }
            }
            
            // If still no class name, check for nested "classes" object (fallback)
            if (className.isEmpty() && json.has("classes")) {
                JSONObject classesObj = json.optJSONObject("classes");
                if (classesObj != null) {
                    // Get the first class (assuming single class booking)
                    String firstKey = classesObj.keys().hasNext() ? classesObj.keys().next() : null;
                    if (firstKey != null) {
                        JSONObject classInfo = classesObj.optJSONObject(firstKey);
                        if (classInfo != null) {
                            // Extract class details
                            String classType = classInfo.optString("type", classInfo.optString("classType", ""));
                            String level = classInfo.optString("level", classInfo.optString("difficulty", ""));
                            instructor = classInfo.optString("instructor", "");
                            String time = classInfo.optString("time", "");
                            
                            // Build class name from available info
                            if (!classType.isEmpty() && !level.isEmpty()) {
                                className = classType + " (" + level + ")";
                            } else if (!classType.isEmpty()) {
                                className = classType;
                            } else if (!level.isEmpty()) {
                                className = level + " Yoga";
                            } else {
                                className = "Yoga Class";
                            }
                            
                            // Use class time if booking time is empty
                            if (bookingTime.isEmpty() && !time.isEmpty()) {
                                bookingTime = time;
                            }
                            
                            Log.d(TAG, "Extracted from nested classes object:");
                            Log.d(TAG, "  className: '" + className + "'");
                            Log.d(TAG, "  instructor: '" + instructor + "'");
                            Log.d(TAG, "  time: '" + time + "'");
                        }
                    }
                }
            }
            
            // If still no class name, try direct fields
            if (className.isEmpty()) {
                className = json.optString("className", "");
            }
            
            // Extract all other fields with detailed logging
            Log.d(TAG, "Final extracted values for booking ID: " + bookingId);
            
            booking.setBookingId(bookingId);
            booking.setCustomerName(customerName);
            booking.setCustomerEmail(customerEmail);
            booking.setCustomerPhone(customerPhone);
            booking.setClassInstanceId(json.optString("classInstanceId", firebaseKey));
            booking.setClassName(className);
            booking.setAllClassNames(allClassNames); // Set all class names
            
            // Debug logging for multiple classes
            Log.d(TAG, "=== FINAL BOOKING CLASS INFO ===");
            Log.d(TAG, "Primary className: '" + className + "'");
            Log.d(TAG, "All classes count: " + allClassNames.size());
            for (int i = 0; i < allClassNames.size(); i++) {
                Log.d(TAG, "  Class " + (i + 1) + ": '" + allClassNames.get(i) + "'");
            }
            Log.d(TAG, "=== END CLASS INFO ===");
            
            booking.setBookingDate(bookingDate);
            booking.setBookingTime(bookingTime);
            booking.setStatus(json.optString("status", "confirmed"));
            booking.setPaymentStatus(json.optString("paymentStatus", "pending"));
            booking.setPaymentAmount(totalAmount); // Use the extracted totalAmount
            booking.setPaymentMethod(json.optString("paymentMethod", ""));
            booking.setNotes(json.optString("notes", ""));
            booking.setCreatedAt(json.optString("createdAt", ""));
            booking.setUpdatedAt(json.optString("updatedAt", ""));
            booking.setSynced(true); // Coming from Firebase, so it's synced
            booking.setLastModified(json.optLong("lastModified", System.currentTimeMillis()));
            
            return booking;
            
        } catch (Exception e) {
            Log.e(TAG, "Error converting JSON to Booking", e);
            return null;
        }
    }
    
    /**
     * Update booking in database
     */
    private void updateBookingInDatabase(Booking booking) {
        // Use update method instead of delete and re-add
        int result = databaseHelper.updateBooking(booking);
        if (result == 0) {
            Log.w(TAG, "No booking found to update with ID: " + booking.getBookingId());
        }
    }
    
    /**
     * Make PATCH request for partial updates
     */
    private String makePatchRequest(String urlString, String data) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PATCH");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = data.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            } else {
                Log.e(TAG, "PATCH request failed with response code: " + responseCode);
                return null;
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Error making PATCH request", e);
            return null;
        }
    }
    
    /**
     * Make GET request
     */
    private String makeGetRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            } else {
                Log.e(TAG, "GET request failed with response code: " + responseCode);
                return null;
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Error making GET request", e);
            return null;
        }
    }
    
    /**
     * Format ISO date string to readable format
     * Converts "2025-07-30T05:51:56.479Z" to "July 30, 2025 at 05:51"
     */
    private String formatISODate(String isoDate) {
        try {
            // Parse the ISO date
            java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date date = isoFormat.parse(isoDate);
            
            // Format to readable string
            java.text.SimpleDateFormat readableFormat = new java.text.SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm");
            return readableFormat.format(date);
            
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + isoDate, e);
            // Return original if parsing fails
            return isoDate;
        }
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
