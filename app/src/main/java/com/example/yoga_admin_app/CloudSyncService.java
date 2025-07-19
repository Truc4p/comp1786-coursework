package com.example.yoga_admin_app;

import android.content.Context;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudSyncService {
    private static final String TAG = "CloudSyncService";
    
    // Option 1: JSONBin.io (for testing) - Replace YOUR_BIN_ID with actual bin ID
    // private static final String BASE_URL = "https://api.jsonbin.io/v3/b/YOUR_BIN_ID/";
    
    // Option 2: Firebase Realtime Database - Replace YOUR_PROJECT_ID
    private static final String BASE_URL = "https://yogaapp-12d2b-default-rtdb.asia-southeast1.firebasedatabase.app/";
    
    // // Option 3: Your actual cloud service
    // private static final String BASE_URL = "https://your-cloud-service.com/api/";
    
    private static final String YOGA_CLASSES_ENDPOINT = "yoga-classes";
    private static final String CLASS_INSTANCES_ENDPOINT = "class-instances";
    private static final String SYNC_ENDPOINT = "sync";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    
    public CloudSyncService(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Interface for sync callbacks
     */
    public interface SyncCallback {
        void onSuccess(String message);
        void onError(String error);
        void onProgress(String progress);
    }
    
    /**
     * Upload all yoga classes and instances to cloud
     */
    public void uploadAllData(SyncCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onError("No internet connection available");
            return;
        }
        
        executorService.execute(() -> {
            try {
                callback.onProgress("Preparing data for upload...");
                
                // Get all yoga classes and instances
                List<YogaClass> yogaClasses = databaseHelper.getAllYogaClasses();
                
                if (yogaClasses.isEmpty()) {
                    callback.onError("No yoga classes found to upload");
                    return;
                }
                
                callback.onProgress("Uploading " + yogaClasses.size() + " yoga classes...");
                
                // Create JSON payload
                JSONObject payload = createUploadPayload(yogaClasses);
                
                // Upload to cloud
                String response = sendDataToCloud(SYNC_ENDPOINT, payload);
                
                if (response != null) {
                    callback.onProgress("Upload completed successfully");
                    callback.onSuccess("Successfully uploaded " + yogaClasses.size() + " yoga classes and their instances to cloud");
                } else {
                    callback.onError("Failed to upload data to cloud service");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error uploading data", e);
                callback.onError("Upload failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Download and sync data from cloud
     */
    public void downloadAndSync(SyncCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onError("No internet connection available");
            return;
        }
        
        executorService.execute(() -> {
            try {
                callback.onProgress("Downloading data from cloud...");
                
                // Download data from cloud
                String cloudData = downloadDataFromCloud(SYNC_ENDPOINT);
                
                if (cloudData != null) {
                    callback.onProgress("Processing downloaded data...");
                    
                    // Parse and sync with local database
                    syncWithLocalDatabase(cloudData);
                    
                    callback.onSuccess("Successfully synchronized with cloud database");
                } else {
                    callback.onError("Failed to download data from cloud service");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error syncing data", e);
                callback.onError("Sync failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Create JSON payload for upload
     */
    private JSONObject createUploadPayload(List<YogaClass> yogaClasses) throws JSONException {
        JSONObject payload = new JSONObject();
        JSONArray classesArray = new JSONArray();
        
        for (YogaClass yogaClass : yogaClasses) {
            JSONObject classObj = new JSONObject();
            classObj.put("id", yogaClass.getId());
            classObj.put("dayOfWeek", yogaClass.getDayOfWeek());
            classObj.put("time", yogaClass.getTime());
            classObj.put("capacity", yogaClass.getCapacity());
            classObj.put("duration", yogaClass.getDuration());
            classObj.put("price", yogaClass.getPrice());
            classObj.put("classType", yogaClass.getClassType());
            classObj.put("description", yogaClass.getDescription());
            classObj.put("difficulty", yogaClass.getDifficulty());
            classObj.put("latitude", yogaClass.getLatitude());
            classObj.put("longitude", yogaClass.getLongitude());
            classObj.put("locationAddress", yogaClass.getLocationAddress());
            
            // Get instances for this class
            List<ClassInstance> instances = databaseHelper.getClassInstancesByYogaClassId(yogaClass.getId());
            JSONArray instancesArray = new JSONArray();
            
            for (ClassInstance instance : instances) {
                JSONObject instanceObj = new JSONObject();
                instanceObj.put("id", instance.getId());
                instanceObj.put("yogaClassId", instance.getYogaClassId());
                instanceObj.put("date", instance.getDate());
                instanceObj.put("instructor", instance.getInstructor());
                instanceObj.put("additionalComments", instance.getAdditionalComments());
                instancesArray.put(instanceObj);
            }
            
            classObj.put("instances", instancesArray);
            classesArray.put(classObj);
        }
        
        payload.put("yogaClasses", classesArray);
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("deviceId", getDeviceId());
        
        return payload;
    }
    
    /**
     * Send data to cloud service (Firebase Realtime Database format)
     */
    private String sendDataToCloud(String endpoint, JSONObject data) {
        try {
            // Firebase Realtime Database REST API format
            URL url = new URL(BASE_URL + endpoint + ".json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Set request properties
            connection.setRequestMethod("PUT"); // Firebase uses PUT for writing data
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(15000); // 15 seconds
            
            // Send data
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Get response
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Firebase response code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                Log.d(TAG, "Firebase response: " + response.toString());
                return response.toString();
            } else {
                // Read error response
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                
                Log.e(TAG, "HTTP Error: " + responseCode + ", Response: " + errorResponse.toString());
                return null;
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Network error", e);
            return null;
        }
    }
    
    /**
     * Download data from cloud service (Firebase Realtime Database format)
     */
    private String downloadDataFromCloud(String endpoint) {
        try {
            // Firebase Realtime Database REST API format
            URL url = new URL(BASE_URL + endpoint + ".json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Set request properties
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(15000); // 15 seconds
            
            // Get response
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Firebase download response code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                Log.d(TAG, "Firebase download response: " + response.toString());
                return response.toString();
            } else {
                Log.e(TAG, "HTTP Error: " + responseCode);
                return null;
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Network error", e);
            return null;
        }
    }
    
    /**
     * Sync downloaded data with local database (Firebase format)
     */
    private void syncWithLocalDatabase(String cloudData) throws JSONException {
        // Firebase might return null if no data exists
        if (cloudData == null || cloudData.equals("null")) {
            Log.d(TAG, "No cloud data found, nothing to sync");
            return;
        }
        
        JSONObject data = new JSONObject(cloudData);
        
        // Check if yogaClasses exists in the response
        if (!data.has("yogaClasses")) {
            Log.d(TAG, "No yogaClasses found in cloud data");
            return;
        }
        
        JSONArray yogaClasses = data.getJSONArray("yogaClasses");
        
        // For this implementation, we'll do a simple overwrite sync
        // In a real application, you might want more sophisticated conflict resolution
        
        for (int i = 0; i < yogaClasses.length(); i++) {
            JSONObject classObj = yogaClasses.getJSONObject(i);
            
            // Check if class exists locally
            long classId = classObj.getLong("id");
            YogaClass existingClass = databaseHelper.getYogaClass(classId);
            
            if (existingClass == null) {
                // Create new class
                YogaClass newClass = createYogaClassFromJson(classObj);
                long newId = databaseHelper.addYogaClass(newClass);
                
                // Add instances if they exist
                if (classObj.has("instances")) {
                    JSONArray instances = classObj.getJSONArray("instances");
                    for (int j = 0; j < instances.length(); j++) {
                        JSONObject instanceObj = instances.getJSONObject(j);
                        ClassInstance instance = createInstanceFromJson(instanceObj, newId);
                        databaseHelper.addClassInstance(instance);
                    }
                }
            } else {
                // Update existing class if needed
                YogaClass updatedClass = createYogaClassFromJson(classObj);
                updatedClass.setId(classId);
                databaseHelper.updateYogaClass(updatedClass);
                
                // Sync instances (simple approach: delete and recreate)
                databaseHelper.deleteAllInstancesForYogaClass(classId);
                if (classObj.has("instances")) {
                    JSONArray instances = classObj.getJSONArray("instances");
                    for (int j = 0; j < instances.length(); j++) {
                        JSONObject instanceObj = instances.getJSONObject(j);
                        ClassInstance instance = createInstanceFromJson(instanceObj, classId);
                        databaseHelper.addClassInstance(instance);
                    }
                }
            }
        }
    }
    
    /**
     * Create YogaClass object from JSON
     */
    private YogaClass createYogaClassFromJson(JSONObject json) throws JSONException {
        YogaClass yogaClass = new YogaClass();
        yogaClass.setDayOfWeek(json.getString("dayOfWeek"));
        yogaClass.setTime(json.getString("time"));
        yogaClass.setCapacity(json.getInt("capacity"));
        yogaClass.setDuration(json.getInt("duration"));
        yogaClass.setPrice(json.getDouble("price"));
        yogaClass.setClassType(json.getString("classType"));
        yogaClass.setDescription(json.optString("description", ""));
        yogaClass.setDifficulty(json.optString("difficulty", ""));
        yogaClass.setLatitude(json.optDouble("latitude", 0.0));
        yogaClass.setLongitude(json.optDouble("longitude", 0.0));
        yogaClass.setLocationAddress(json.optString("locationAddress", ""));
        return yogaClass;
    }
    
    /**
     * Create ClassInstance object from JSON
     */
    private ClassInstance createInstanceFromJson(JSONObject json, long yogaClassId) throws JSONException {
        ClassInstance instance = new ClassInstance();
        instance.setYogaClassId(yogaClassId);
        instance.setDate(json.getString("date"));
        instance.setInstructor(json.getString("instructor"));
        instance.setAdditionalComments(json.optString("additionalComments", ""));
        return instance;
    }
    
    /**
     * Get a simple device identifier
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
                URL url = new URL(BASE_URL + ".json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Firebase connection test response: " + responseCode);
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    callback.onSuccess("Firebase cloud service is reachable");
                } else {
                    callback.onError("Firebase cloud service returned error: " + responseCode);
                }
                
            } catch (IOException e) {
                Log.e(TAG, "Firebase connection test failed", e);
                callback.onError("Cannot reach Firebase cloud service: " + e.getMessage());
            }
        });
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
