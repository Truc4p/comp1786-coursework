package com.example.yoga_admin_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewYogaClassesActivity extends AppCompatActivity {

    private ListView listViewYogaClasses;
    private LinearLayout tvNoClasses;
    private Button btnBack;
    private Button btnAdvancedSearch;
    private EditText etQuickSearch;
    private DatabaseHelper databaseHelper;
    private YogaClassAdapter adapter;
    private List<YogaClass> yogaClassList;
    private List<YogaClass> filteredYogaClassList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_yoga_classes);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Load and display yoga classes
        loadYogaClasses();

        // Setup button listeners
        setupButtonListeners();
        
        // Setup search functionality
        setupSearchFunctionality();
    }

    private void initializeViews() {
        listViewYogaClasses = findViewById(R.id.listview_yoga_classes);
        tvNoClasses = findViewById(R.id.tv_no_classes);
        btnBack = findViewById(R.id.btn_back);
        btnAdvancedSearch = findViewById(R.id.btn_advanced_search);
        etQuickSearch = findViewById(R.id.et_quick_search);
        
        // Initialize filtered list
        filteredYogaClassList = new ArrayList<>();
    }

    private void loadYogaClasses() {
        yogaClassList = databaseHelper.getAllYogaClasses();
        filteredYogaClassList.clear();
        filteredYogaClassList.addAll(yogaClassList);

        if (filteredYogaClassList.isEmpty()) {
            listViewYogaClasses.setVisibility(View.GONE);
            tvNoClasses.setVisibility(View.VISIBLE);
        } else {
            listViewYogaClasses.setVisibility(View.VISIBLE);
            tvNoClasses.setVisibility(View.GONE);

            adapter = new YogaClassAdapter(this, filteredYogaClassList);
            listViewYogaClasses.setAdapter(adapter);

            // Set up item click listener for viewing details directly
            listViewYogaClasses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    YogaClass selectedClass = filteredYogaClassList.get(position);
                    showClassDetails(selectedClass);
                }
            });
        }
    }

    private void showClassOptionsDialog(YogaClass yogaClass) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(yogaClass.getClassType() + " - " + yogaClass.getDayOfWeek());
        
        String[] options = {"View Details", "Edit Class", "Manage Class Instances", "Delete Class"};
        
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // View Details
                        showClassDetails(yogaClass);
                        break;
                    case 1: // Edit Class
                        editYogaClass(yogaClass);
                        break;
                    case 2: // Manage Class Instances
                        manageClassInstances(yogaClass);
                        break;
                    case 3: // Delete Class
                        confirmDeleteClass(yogaClass);
                        break;
                }
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showClassDetails(YogaClass yogaClass) {
        Intent intent = new Intent(this, ClassDetailsActivity.class);
        intent.putExtra("classId", yogaClass.getId());
        startActivity(intent);
    }
    
    private void populateClassDetailsViews(View dialogView, YogaClass yogaClass) {
        // Schedule section
        TextView tvDay = dialogView.findViewById(R.id.tv_day);
        TextView tvTime = dialogView.findViewById(R.id.tv_time);
        TextView tvDuration = dialogView.findViewById(R.id.tv_duration);
        
        tvDay.setText(yogaClass.getDayOfWeek());
        tvTime.setText(yogaClass.getTime());
        tvDuration.setText(yogaClass.getDuration() + " minutes");
        
        // Class information section
        TextView tvType = dialogView.findViewById(R.id.tv_type);
        TextView tvDifficulty = dialogView.findViewById(R.id.tv_difficulty);
        TextView tvCapacity = dialogView.findViewById(R.id.tv_capacity);
        TextView tvPrice = dialogView.findViewById(R.id.tv_price);
        
        tvType.setText(yogaClass.getClassType());
        tvDifficulty.setText(getDifficultyWithIndicator(yogaClass.getDifficulty()));
        tvCapacity.setText(yogaClass.getCapacity() + " people");
        tvPrice.setText("£" + String.format("%.2f", yogaClass.getPrice()));
        
        // Location section
        TextView tvLocation = dialogView.findViewById(R.id.tv_location);
        tvLocation.setText(getLocationDisplayText(yogaClass));
        
        // Description section
        TextView tvDescription = dialogView.findViewById(R.id.tv_description);
        String description = yogaClass.getDescription() != null && !yogaClass.getDescription().trim().isEmpty() 
            ? yogaClass.getDescription() : "No description provided";
        tvDescription.setText(description);
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

    private void editYogaClass(YogaClass yogaClass) {
        Intent intent = new Intent(this, EditYogaClassActivity.class);
        intent.putExtra("classId", yogaClass.getId());
        intent.putExtra("dayOfWeek", yogaClass.getDayOfWeek());
        intent.putExtra("time", yogaClass.getTime());
        intent.putExtra("capacity", yogaClass.getCapacity());
        intent.putExtra("duration", yogaClass.getDuration());
        intent.putExtra("price", yogaClass.getPrice());
        intent.putExtra("classType", yogaClass.getClassType());
        intent.putExtra("description", yogaClass.getDescription());
        intent.putExtra("difficulty", yogaClass.getDifficulty());
        startActivity(intent);
    }

    private void confirmDeleteClass(YogaClass yogaClass) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Class");
        builder.setMessage("Are you sure you want to delete this yoga class?\n\n" +
                yogaClass.getClassType() + " - " + yogaClass.getDayOfWeek() + " at " + yogaClass.getTime());
        
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseHelper.deleteYogaClass(yogaClass.getId());
                Toast.makeText(ViewYogaClassesActivity.this, "Class deleted successfully", Toast.LENGTH_SHORT).show();
                loadYogaClasses(); // Refresh the list
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void setupButtonListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        btnAdvancedSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewYogaClassesActivity.this, SearchYogaClassesActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void setupSearchFunctionality() {
        etQuickSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClassesByInstructor(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }
    
    private void filterClassesByInstructor(String instructorName) {
        if (instructorName.trim().isEmpty()) {
            // Show all classes
            filteredYogaClassList.clear();
            filteredYogaClassList.addAll(yogaClassList);
        } else {
            // Filter by instructor name
            filteredYogaClassList = databaseHelper.searchYogaClassesByInstructor(instructorName);
        }
        
        // Update the adapter
        if (filteredYogaClassList.isEmpty()) {
            listViewYogaClasses.setVisibility(View.GONE);
            tvNoClasses.setVisibility(View.VISIBLE);
        } else {
            listViewYogaClasses.setVisibility(View.VISIBLE);
            tvNoClasses.setVisibility(View.GONE);
            adapter = new YogaClassAdapter(this, filteredYogaClassList);
            listViewYogaClasses.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadYogaClasses(); // Refresh the list when returning from edit
        etQuickSearch.setText(""); // Clear search when returning
    }
    
    private void manageClassInstances(YogaClass yogaClass) {
        // Log the yoga class details before creating the intent
        System.out.println("Managing class instances for yoga class");
        
        Intent intent = new Intent(this, ManageClassInstancesActivity.class);
        intent.putExtra("classId", yogaClass.getId());
        intent.putExtra("className", yogaClass.getClassType());
        intent.putExtra("dayOfWeek", yogaClass.getDayOfWeek());
        intent.putExtra("time", yogaClass.getTime());
        
        startActivity(intent);
    }
}