package com.example.yoga_admin_app;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Application class to initialize Firebase
 */
public class YogaAdminApplication extends Application {
    private static final String TAG = "YogaAdminApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
            
            // Get the Firebase Database URL from configuration
            String databaseUrl = FirebaseConfig.getFirebaseDatabaseUrl(this);
            
            if (databaseUrl != null) {
                // Initialize Firebase Database with configured URL
                FirebaseDatabase database = FirebaseDatabase.getInstance(databaseUrl);
                
                // Enable offline persistence for Firebase Database
                // allows the app to cache data locally, so it can read and write 
                // to the database even when offline. 
                // Changes are synchronized with the server when the device reconnects.
                database.setPersistenceEnabled(true);
                Log.d(TAG, "Firebase persistence enabled for configured URL");
            } else {
                Log.e(TAG, "Could not get Firebase Database URL from configuration");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase", e);
        }
    }
}
