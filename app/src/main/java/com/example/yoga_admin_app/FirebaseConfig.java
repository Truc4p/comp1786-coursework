package com.example.yoga_admin_app;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Firebase configuration helper class
 * Provides methods to get Firebase configuration dynamically instead of hardcoding URLs
 */
public class FirebaseConfig {
    private static final String TAG = "FirebaseConfig";
    private static final String GOOGLE_SERVICES_FILE = "google-services.json";
    
    /**
     * Get Firebase Database URL from configuration
     * @param context Application context
     * @return Firebase Database URL
     */
    public static String getFirebaseDatabaseUrl(Context context) {
        try {
            // First try to get URL from Firebase SDK
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String databaseUrl = database.getReference().toString();
            
            if (databaseUrl != null && !databaseUrl.isEmpty()) {
                if (databaseUrl.endsWith("/")) {
                    Log.d(TAG, "Using Firebase URL from SDK: " + databaseUrl);
                    return databaseUrl;
                } else {
                    Log.d(TAG, "Using Firebase URL from SDK: " + databaseUrl + "/");
                    return databaseUrl + "/";
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get Firebase URL from SDK, trying alternative methods", e);
        }
        
        // Fallback to project ID based URL construction
        String projectId = getProjectIdFromFirebaseApp(context);
        if (projectId != null && !projectId.isEmpty()) {
            String url = constructDatabaseUrl(projectId);
            Log.d(TAG, "Using constructed Firebase URL: " + url);
            return url;
        }
        
        // Last resort: read from google-services.json
        projectId = getProjectIdFromGoogleServices(context);
        if (projectId != null && !projectId.isEmpty()) {
            String url = constructDatabaseUrl(projectId);
            Log.d(TAG, "Using Firebase URL from google-services.json: " + url);
            return url;
        }
        
        // Should not reach here in a properly configured app
        Log.e(TAG, "Unable to determine Firebase URL from any configuration source");
        throw new RuntimeException("Firebase configuration not found. Please ensure google-services.json is properly configured.");
    }
    
    /**
     * Get project ID from Firebase App instance
     */
    private static String getProjectIdFromFirebaseApp(Context context) {
        try {
            if (FirebaseApp.getApps(context).size() > 0) {
                FirebaseApp app = FirebaseApp.getInstance();
                if (app != null && app.getOptions() != null) {
                    return app.getOptions().getProjectId();
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get project ID from Firebase App", e);
        }
        return null;
    }
    
    /**
     * Read project ID from google-services.json file
     */
    private static String getProjectIdFromGoogleServices(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(GOOGLE_SERVICES_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonString = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            
            reader.close();
            inputStream.close();
            
            JSONObject jsonObject = new JSONObject(jsonString.toString());
            JSONObject projectInfo = jsonObject.getJSONObject("project_info");
            return projectInfo.getString("project_id");
            
        } catch (Exception e) {
            Log.w(TAG, "Could not read project ID from google-services.json", e);
        }
        return null;
    }
    
    /**
     * Construct Firebase Database URL from project ID
     */
    private static String constructDatabaseUrl(String projectId) {
        // Default Firebase Realtime Database URL pattern
        // Format: https://{project-id}-default-rtdb.{region}.firebasedatabase.app/
        return "https://" + projectId + "-default-rtdb.asia-southeast1.firebasedatabase.app/";
    }
    
    /**
     * Check if Firebase is properly configured
     */
    public static boolean isFirebaseConfigured(Context context) {
        try {
            String url = getFirebaseDatabaseUrl(context);
            return url != null && !url.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
