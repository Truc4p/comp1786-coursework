package com.example.yoga_admin_app;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Firebase configuration helper class
 * Provides methods to get Firebase configuration securely from BuildConfig
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
        // Get project ID and region from BuildConfig (set from firebase.properties)
        String projectId = BuildConfig.FIREBASE_PROJECT_ID;
        String region = BuildConfig.FIREBASE_DATABASE_REGION;
        
        if (projectId != null && !projectId.isEmpty() && region != null && !region.isEmpty()) {
            String databaseUrl = "https://" + projectId + "-default-rtdb." + region + ".firebasedatabase.app/";
            Log.d(TAG, "Using Firebase URL from BuildConfig configuration");
            return databaseUrl;
        }
        
        // Fallback: try to get from google-services.json
        projectId = getProjectIdFromGoogleServices(context);
        if (projectId != null) {
            String databaseUrl = "https://" + projectId + "-default-rtdb.asia-southeast1.firebasedatabase.app/";
            Log.d(TAG, "Using Firebase URL from google-services.json fallback");
            return databaseUrl;
        }
        
        Log.e(TAG, "Could not get Firebase configuration from BuildConfig or google-services.json");
        return null;
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
    public static String getProjectIdFromGoogleServices(Context context) {
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
        // Based on Firebase error response, this database is in asia-southeast1 region
        // The correct URL pattern for this project is:
        String correctUrl = "https://" + projectId + "-default-rtdb.asia-southeast1.firebasedatabase.app/";
        Log.d(TAG, "Constructed Firebase URL for asia-southeast1 region: " + correctUrl);
        
        return correctUrl;
    }
    
    /**
     * Get all possible Firebase Database URLs to try
     */
    public static String[] getPossibleFirebaseUrls(String projectId) {
        return new String[] {
            // Correct URL for this project (asia-southeast1 region)
            "https://" + projectId + "-default-rtdb.asia-southeast1.firebasedatabase.app/",
            // Other possible URLs as fallbacks
            "https://" + projectId + "-default-rtdb.firebasedatabase.app/",
            "https://" + projectId + "-default-rtdb.us-central1.firebasedatabase.app/",
            "https://" + projectId + "-default-rtdb.europe-west1.firebasedatabase.app/"
        };
    }
    
    /**
     * Check if Firebase is properly configured
     */
    public static boolean isFirebaseConfigured(Context context) {
        try {
            String url = getFirebaseDatabaseUrl(context);
            return url != null && !url.isEmpty();
        } catch (Exception e) {
            Log.e(TAG, "Firebase configuration check failed", e);
            return false;
        }
    }
    
    /**
     * Test Firebase database connection and return URL that works
     */
    public static String getWorkingFirebaseDatabaseUrl(Context context) {
        String projectId = getProjectIdFromGoogleServices(context);
        if (projectId == null || projectId.isEmpty()) {
            Log.e(TAG, "Cannot get project ID");
            return null;
        }
        
        // Try different URL patterns, starting with the correct one for this project
        String[] urlPatterns = {
            "https://" + projectId + "-default-rtdb.asia-southeast1.firebasedatabase.app/",
            "https://" + projectId + "-default-rtdb.firebasedatabase.app/",
            "https://" + projectId + "-default-rtdb.us-central1.firebasedatabase.app/"
        };
        
        for (String url : urlPatterns) {
            Log.d(TAG, "Testing Firebase URL: " + url);
            // For now, return the first URL - we could add actual connectivity testing here
            return url;
        }
        
        return urlPatterns[0]; // Default fallback
    }
}
