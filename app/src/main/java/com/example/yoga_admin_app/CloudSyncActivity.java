package com.example.yoga_admin_app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class CloudSyncActivity extends AppCompatActivity {
    
    private TextView tvNetworkStatus;
    private TextView tvLastSync;
    private Button btnUploadData;
    private Button btnDownloadSync;
    private Button btnCheckConnection;
    private Button btnBack;
    
    private CloudSyncService cloudSyncService;
    private ProgressDialog progressDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_sync);
        
        // Initialize cloud sync service
        cloudSyncService = new CloudSyncService(this);
        
        // Initialize views
        initializeViews();
        
        // Setup listeners
        setupListeners();
        
        // Update network status
        updateNetworkStatus();
    }
    
    private void initializeViews() {
        tvNetworkStatus = findViewById(R.id.tv_network_status);
        tvLastSync = findViewById(R.id.tv_last_sync);
        btnUploadData = findViewById(R.id.btn_upload_data);
        btnDownloadSync = findViewById(R.id.btn_download_sync);
        btnCheckConnection = findViewById(R.id.btn_check_connection);
        btnBack = findViewById(R.id.btn_back);
        
        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }
    
    private void setupListeners() {
        btnUploadData.setOnClickListener(v -> showUploadConfirmation());
        
        btnDownloadSync.setOnClickListener(v -> showDownloadConfirmation());
        
        btnCheckConnection.setOnClickListener(v -> checkCloudConnection());
        
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void updateNetworkStatus() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            String networkType = NetworkUtils.getNetworkType(this);
            tvNetworkStatus.setText("Connected (" + networkType + ")");
            tvNetworkStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvNetworkStatus.setText("No Internet Connection");
            tvNetworkStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }
    
    private void showUploadConfirmation() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Upload Data to Cloud")
                .setMessage("This will upload all yoga classes and instances to the cloud service. Continue?")
                .setPositiveButton("Upload", (dialog, which) -> uploadData())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showDownloadConfirmation() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Sync with Cloud")
                .setMessage("This will download data from cloud and sync with local database. Any conflicts will be resolved by using cloud data. Continue?")
                .setPositiveButton("Sync", (dialog, which) -> downloadAndSync())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void uploadData() {
        showProgress("Uploading data...");
        
        cloudSyncService.uploadAllData(new CloudSyncService.SyncCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    hideProgress();
                    Toast.makeText(CloudSyncActivity.this, message, Toast.LENGTH_LONG).show();
                    updateLastSyncTime();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideProgress();
                    Toast.makeText(CloudSyncActivity.this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
            
            @Override
            public void onProgress(String progress) {
                runOnUiThread(() -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.setMessage(progress);
                    }
                });
            }
        });
    }
    
    private void downloadAndSync() {
        showProgress("Syncing with cloud...");
        
        cloudSyncService.downloadAndSync(new CloudSyncService.SyncCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    hideProgress();
                    Toast.makeText(CloudSyncActivity.this, message, Toast.LENGTH_LONG).show();
                    updateLastSyncTime();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideProgress();
                    Toast.makeText(CloudSyncActivity.this, "Sync failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
            
            @Override
            public void onProgress(String progress) {
                runOnUiThread(() -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.setMessage(progress);
                    }
                });
            }
        });
    }
    
    private void checkCloudConnection() {
        showProgress("Checking cloud connection...");
        
        cloudSyncService.checkCloudConnection(new CloudSyncService.SyncCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    hideProgress();
                    Toast.makeText(CloudSyncActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideProgress();
                    Toast.makeText(CloudSyncActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
            
            @Override
            public void onProgress(String progress) {
                runOnUiThread(() -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.setMessage(progress);
                    }
                });
            }
        });
    }
    
    private void showProgress(String message) {
        progressDialog.setMessage(message);
        progressDialog.show();
    }
    
    private void hideProgress() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    private void updateLastSyncTime() {
        String currentTime = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
        tvLastSync.setText("Last sync: " + currentTime);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateNetworkStatus();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cloudSyncService != null) {
            cloudSyncService.shutdown();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
