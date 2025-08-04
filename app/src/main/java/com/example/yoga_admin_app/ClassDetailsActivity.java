package com.example.yoga_admin_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ClassDetailsActivity extends AppCompatActivity {

    private TextView tvClassTitle;
    private TextView tvDay, tvTime, tvDuration;
    private TextView tvType, tvDifficulty, tvCapacity, tvPrice;
    private TextView tvLocation, tvDescription;
    private TextView tvDate, tvInstructor;
    private LinearLayout layoutDate, layoutInstructor;
    private Button btnBack, btnManageInstances, btnEditClass, btnDeleteClass;
    
    private DatabaseHelper databaseHelper;
    private YogaClass yogaClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Get class data from intent
        getClassDataFromIntent();

        // Setup button listeners
        setupButtonListeners();

        // Populate views with class data
        populateClassDetailsViews();
    }

    private void initializeViews() {
        tvClassTitle = findViewById(R.id.tv_class_title);
        tvDay = findViewById(R.id.tv_day);
        tvTime = findViewById(R.id.tv_time);
        tvDuration = findViewById(R.id.tv_duration);
        tvType = findViewById(R.id.tv_type);
        tvDifficulty = findViewById(R.id.tv_difficulty);
        tvCapacity = findViewById(R.id.tv_capacity);
        tvPrice = findViewById(R.id.tv_price);
        tvLocation = findViewById(R.id.tv_location);
        tvDescription = findViewById(R.id.tv_description);
        tvDate = findViewById(R.id.tv_date);
        tvInstructor = findViewById(R.id.tv_instructor);
        layoutDate = findViewById(R.id.layout_date);
        layoutInstructor = findViewById(R.id.layout_instructor);
        
        btnBack = findViewById(R.id.btn_back);
        btnManageInstances = findViewById(R.id.btn_manage_instances);
        btnEditClass = findViewById(R.id.btn_edit_class);
        btnDeleteClass = findViewById(R.id.btn_delete_class);
    }

    private void getClassDataFromIntent() {
        Intent intent = getIntent();
        long classId = intent.getLongExtra("classId", -1);
        
        if (classId != -1) {
            yogaClass = databaseHelper.getYogaClass(classId);
            if (yogaClass == null) {
                Toast.makeText(this, "Class not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Check if this is coming from search with specific context
            String searchInstructor = intent.getStringExtra("searchInstructor");
            String searchDate = intent.getStringExtra("searchDate");
            
            if (searchInstructor != null) {
                yogaClass.setSearchInstructor(searchInstructor);
            }
            if (searchDate != null) {
                yogaClass.setSearchDate(searchDate);
            }
        } else {
            Toast.makeText(this, "Invalid class ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupButtonListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnManageInstances.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageClassInstances();
            }
        });

        btnEditClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editYogaClass();
            }
        });

        btnDeleteClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteClass();
            }
        });
    }

    private void populateClassDetailsViews() {
        if (yogaClass == null) return;

        // Set title
        tvClassTitle.setText("🧘 " + yogaClass.getClassType() + " Details");

        // Schedule section
        tvDay.setText(yogaClass.getDayOfWeek());
        tvTime.setText(yogaClass.getTime());
        tvDuration.setText(yogaClass.getDuration() + " minutes");

        // Class information section
        tvType.setText(yogaClass.getClassType());
        tvDifficulty.setText(getDifficultyWithIndicator(yogaClass.getDifficulty()));
        tvCapacity.setText(yogaClass.getCapacity() + " people");
        tvPrice.setText("£" + String.format("%.2f", yogaClass.getPrice()));

        // Location section
        tvLocation.setText(getLocationDisplayText(yogaClass));

        // Description section
        String description = yogaClass.getDescription() != null && !yogaClass.getDescription().trim().isEmpty() 
            ? yogaClass.getDescription() : "No description provided";
        tvDescription.setText(description);
        
        // Show search context fields if available
        if (yogaClass.getSearchDate() != null && !yogaClass.getSearchDate().isEmpty()) {
            layoutDate.setVisibility(View.VISIBLE);
            tvDate.setText(yogaClass.getSearchDate());
        } else {
            layoutDate.setVisibility(View.GONE);
        }
        
        if (yogaClass.getSearchInstructor() != null && !yogaClass.getSearchInstructor().isEmpty()) {
            layoutInstructor.setVisibility(View.VISIBLE);
            tvInstructor.setText(yogaClass.getSearchInstructor());
        } else {
            layoutInstructor.setVisibility(View.GONE);
        }
    }

    private String getDifficultyWithIndicator(String difficulty) {
        if (difficulty == null) return "Not specified";
        
        switch (difficulty.toLowerCase()) {
            case "beginner":
                return "🟢 " + difficulty;
            case "intermediate":
                return "🟡 " + difficulty;
            case "advanced":
                return "🔴 " + difficulty;
            case "all levels":
                return "🌈 " + difficulty;
            default:
                return "⚪ " + difficulty;
        }
    }

    private String getLocationDisplayText(YogaClass yogaClass) {
        String locationAddress = yogaClass.getLocationAddress();
        
        // If we have a readable address, use it
        if (locationAddress != null && !locationAddress.trim().isEmpty() 
            && !locationAddress.equals("Unknown location")) {
            return locationAddress;
        }
        
        // If we have coordinates but no address, show coordinates
        if (yogaClass.getLatitude() != 0.0 || yogaClass.getLongitude() != 0.0) {
            return String.format("📍 %.6f, %.6f", yogaClass.getLatitude(), yogaClass.getLongitude());
        }
        
        // No location information available
        return "Not specified";
    }

    private void manageClassInstances() {
        Intent intent = new Intent(this, ManageClassInstancesActivity.class);
        intent.putExtra("classId", yogaClass.getId());
        intent.putExtra("className", yogaClass.getClassType());
        intent.putExtra("dayOfWeek", yogaClass.getDayOfWeek());
        intent.putExtra("time", yogaClass.getTime());
        startActivity(intent);
    }

    private void editYogaClass() {
        Intent intent = new Intent(this, EditYogaClassActivity.class);
        intent.putExtra("classId", yogaClass.getId());
        startActivity(intent);
    }

    private void confirmDeleteClass() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Class");
        builder.setMessage("Are you sure you want to delete this yoga class?\n\n" +
                yogaClass.getClassType() + " - " + yogaClass.getDayOfWeek() + " at " + yogaClass.getTime());
        
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseHelper.deleteYogaClass(yogaClass.getId());
                Toast.makeText(ClassDetailsActivity.this, "Class deleted successfully", Toast.LENGTH_SHORT).show();
                finish(); // Return to previous activity
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh class data when returning from edit
        if (yogaClass != null) {
            YogaClass updatedClass = databaseHelper.getYogaClass(yogaClass.getId());
            if (updatedClass != null) {
                yogaClass = updatedClass;
                populateClassDetailsViews();
            }
        }
    }
}
