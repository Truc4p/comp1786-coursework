package com.example.yoga_admin_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * CloudSyncActivity - Provides UI for cloud synchronization management
 * 
 * Features:
 * - Display network and sync status
 * - Manual sync trigger
 * - Connection testing
 * - Real-time status updates
 */
public class CloudSyncActivity extends AppCompatActivity {
    private static final String TAG = "CloudSyncActivity";
    
    // UI Components
    private TextView tvNetworkStatus;
    private TextView tvLastSync;
    private Button btnDownloadSync;
    private Button btnCheckConnection;
    private Button btnBack;
    
    // Services
    private CloudSyncService cloudSyncService;
    private DatabaseHelper databaseHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_sync);
        
        // Initialize services
        initializeServices();
        
        // Initialize UI
        initializeViews();
        
        // Set up event listeners
        setupEventListeners();
        
        // Update initial status
        updateStatus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh status when returning to this activity
        updateStatus();
    }
    
    /**
     * Initialize cloud sync service and database helper
     */
    private void initializeServices() {
        try {
            cloudSyncService = new CloudSyncService(this);
            databaseHelper = new DatabaseHelper(this);
            cloudSyncService.setDatabaseHelper(databaseHelper);
            
            Log.d(TAG, "Services initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing services", e);
            Toast.makeText(this, "Error initializing cloud services", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Initialize UI components
     */
    private void initializeViews() {
        tvNetworkStatus = findViewById(R.id.tv_network_status);
        tvLastSync = findViewById(R.id.tv_last_sync);
        btnDownloadSync = findViewById(R.id.btn_download_sync);
        btnCheckConnection = findViewById(R.id.btn_check_connection);
        btnBack = findViewById(R.id.btn_back);
    }
    
    /**
     * Set up button click listeners
     */
    private void setupEventListeners() {
        // Sync Data Button
        btnDownloadSync.setOnClickListener(v -> {
            Log.d(TAG, "Sync button clicked");
            performSync();
        });
        
        // Test Connection Button
        btnCheckConnection.setOnClickListener(v -> {
            Log.d(TAG, "Check connection button clicked");
            testConnection();
        });
        
        // Back Button
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });
    }
    
    /**
     * Update the status displays
     */
    private void updateStatus() {
        // Update network status
        updateNetworkStatus();
        
        // Update last sync time
        updateLastSyncTime();
    }
    
    /**
     * Update network connection status
     */
    private void updateNetworkStatus() {
        boolean isConnected = NetworkUtils.isNetworkAvailable(this);
        
        if (isConnected) {
            tvNetworkStatus.setText("Connected");
            tvNetworkStatus.setTextColor(getResources().getColor(R.color.success_green));
        } else {
            tvNetworkStatus.setText("Disconnected");
            tvNetworkStatus.setTextColor(getResources().getColor(R.color.error_red));
        }
    }
    
    /**
     * Update last sync time display
     */
    private void updateLastSyncTime() {
        SharedPreferences prefs = getSharedPreferences("sync_prefs", Context.MODE_PRIVATE);
        long lastSyncTime = prefs.getLong("last_sync_time", 0);
        
        if (lastSyncTime == 0) {
            tvLastSync.setText("Never");
            tvLastSync.setTextColor(getResources().getColor(R.color.dark_gray));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String formattedTime = sdf.format(new Date(lastSyncTime));
            tvLastSync.setText(formattedTime);
            tvLastSync.setTextColor(getResources().getColor(R.color.dark_gray));
        }
    }
    
    /**
     * Perform cloud synchronization
     */
    private void performSync() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_SHORT).show();
            updateNetworkStatus();
            return;
        }
        
        // Disable sync button during operation
        btnDownloadSync.setEnabled(false);
        btnDownloadSync.setText("Syncing...");
        
        Log.d(TAG, "Starting two-way sync");
        
        cloudSyncService.performTwoWaySync(new CloudSyncService.SyncCallback() {
            @Override
            public void onProgress(String progress) {
                Log.d(TAG, "Sync progress: " + progress);
                runOnUiThread(() -> {
                    btnDownloadSync.setText(progress);
                });
            }
            
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "Sync successful: " + message);
                runOnUiThread(() -> {
                    btnDownloadSync.setEnabled(true);
                    btnDownloadSync.setText("Sync Data with Cloud");
                    Toast.makeText(CloudSyncActivity.this, "Sync completed successfully", Toast.LENGTH_SHORT).show();
                    updateLastSyncTime(); // Refresh the last sync time
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Sync error: " + error);
                runOnUiThread(() -> {
                    btnDownloadSync.setEnabled(true);
                    btnDownloadSync.setText("Sync Data with Cloud");
                    Toast.makeText(CloudSyncActivity.this, "Sync failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * Test cloud connection
     */
    private void testConnection() {
        // Disable button during test
        btnCheckConnection.setEnabled(false);
        btnCheckConnection.setText("Testing...");
        
        // Update network status first
        updateNetworkStatus();
        
        if (!NetworkUtils.isNetworkAvailable(this)) {
            runOnUiThread(() -> {
                btnCheckConnection.setEnabled(true);
                btnCheckConnection.setText("Test Cloud Connection");
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            });
            return;
        }
        
        Log.d(TAG, "Testing cloud connection");
        
        // Test with a simple download operation
        cloudSyncService.performDownloadOnlySync(new CloudSyncService.SyncCallback() {
            @Override
            public void onProgress(String progress) {
                Log.d(TAG, "Connection test progress: " + progress);
                runOnUiThread(() -> {
                    btnCheckConnection.setText("Testing...");
                });
            }
            
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "Connection test successful: " + message);
                runOnUiThread(() -> {
                    btnCheckConnection.setEnabled(true);
                    btnCheckConnection.setText("Test Cloud Connection");
                    Toast.makeText(CloudSyncActivity.this, "Cloud connection successful!", Toast.LENGTH_SHORT).show();
                    updateStatus(); // Refresh all status
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Connection test failed: " + error);
                runOnUiThread(() -> {
                    btnCheckConnection.setEnabled(true);
                    btnCheckConnection.setText("Test Cloud Connection");
                    Toast.makeText(CloudSyncActivity.this, "Connection failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up services
        if (cloudSyncService != null) {
            cloudSyncService.shutdown();
        }
        
        Log.d(TAG, "Activity destroyed, services cleaned up");
    }
    
    /**
     * Static method to create intent for this activity
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, CloudSyncActivity.class);
    }
}
