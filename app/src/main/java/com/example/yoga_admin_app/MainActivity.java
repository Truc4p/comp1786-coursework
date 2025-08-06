package com.example.yoga_admin_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    
    private DatabaseHelper databaseHelper;
    private AuthenticationManager authManager;
    private TextView tvClassCount;
    private TextView tvWelcomeMessage;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize authentication manager first
        authManager = AuthenticationManager.getInstance(this);
        
        // Check authentication before proceeding
        if (!authManager.isSessionValid()) {
            redirectToLogin();
            return;
        }
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        
        // Initialize cloud sync service and set up bidirectional relationship
        CloudSyncService cloudSyncService = new CloudSyncService(this);
        cloudSyncService.setDatabaseHelper(databaseHelper);
        databaseHelper.setCloudSyncService(cloudSyncService);
        
        // Perform initial sync to get latest data from Firebase
        cloudSyncService.performInitialSync();
        
        // Debug: Check sync status after a delay to see what data we have
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            cloudSyncService.debugSyncStatus();
        }, 3000); // Wait 3 seconds for initial sync to complete
        
        // Initialize views
        initializeViews();
        
        // Update class count and welcome message
        updateClassCount();
        updateWelcomeMessage();
    }

    private void initializeViews() {
        Button btnAddClass = findViewById(R.id.btn_add_class);
        Button btnViewClasses = findViewById(R.id.btn_view_classes);
        Button btnSearchClasses = findViewById(R.id.btn_search_classes);
        Button btnViewBookings = findViewById(R.id.btn_view_bookings);
        btnLogout = findViewById(R.id.btn_logout);
        tvClassCount = findViewById(R.id.tv_class_count);
        tvWelcomeMessage = findViewById(R.id.tv_welcome_message);

        // Setup logout button listener
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        btnAddClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddYogaClassActivity.class);
                startActivity(intent);
            }
        });

        btnViewClasses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewYogaClassesActivity.class);
                startActivity(intent);
            }
        });

        btnSearchClasses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchYogaClassesActivity.class);
                startActivity(intent);
            }
        });

        btnViewBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BookingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateClassCount() {
        int count = databaseHelper.getYogaClassCount();
        tvClassCount.setText("Total Classes: " + count);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Check session validity when activity resumes
        if (!authManager.isSessionValid()) {
            redirectToLogin();
            return;
        }
        
        updateClassCount();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("logout_occurred", true); // Security flag to clear sensitive data
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void updateWelcomeMessage() {
        User currentUser = authManager.getCurrentUser();
        if (currentUser != null && tvWelcomeMessage != null) {
            tvWelcomeMessage.setText("Welcome, " + currentUser.getUsername() + "!");
        }
    }
    
    /**
     * [L1] Proper Session Logout with confirmation
     */
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", null)
                .show();
    }
    
    private void performLogout() {
        boolean success = authManager.logout();
        if (success) {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        } else {
            Toast.makeText(this, "Logout failed", Toast.LENGTH_SHORT).show();
        }
    }
}