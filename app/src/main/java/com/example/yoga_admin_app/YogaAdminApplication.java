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
            
            // Get the correct Firebase Database URL for our region
            String correctUrl = "https://yogaapp-12d2b-default-rtdb.asia-southeast1.firebasedatabase.app";
            
            // Initialize Firebase Database with correct URL
            FirebaseDatabase database = FirebaseDatabase.getInstance(correctUrl);
            
            // Enable offline persistence for Firebase Database
            // allows the app to cache data locally, so it can read and write 
            // to the database even when offline. 
            // Changes are synchronized with the server when the device reconnects.
            database.setPersistenceEnabled(true);
            Log.d(TAG, "Firebase persistence enabled for URL: " + correctUrl);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase", e);
        }
    }
}
