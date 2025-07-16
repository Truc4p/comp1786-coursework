package com.example.yoga_admin_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ViewYogaClassesActivity extends AppCompatActivity {

    private ListView listViewYogaClasses;
    private LinearLayout tvNoClasses;
    private Button btnBack;
    private DatabaseHelper databaseHelper;
    private YogaClassAdapter adapter;
    private List<YogaClass> yogaClassList;

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
    }

    private void initializeViews() {
        listViewYogaClasses = findViewById(R.id.listview_yoga_classes);
        tvNoClasses = findViewById(R.id.tv_no_classes);
        btnBack = findViewById(R.id.btn_back);
    }

    private void loadYogaClasses() {
        yogaClassList = databaseHelper.getAllYogaClasses();

        if (yogaClassList.isEmpty()) {
            listViewYogaClasses.setVisibility(View.GONE);
            tvNoClasses.setVisibility(View.VISIBLE);
        } else {
            listViewYogaClasses.setVisibility(View.VISIBLE);
            tvNoClasses.setVisibility(View.GONE);

            adapter = new YogaClassAdapter(this, yogaClassList);
            listViewYogaClasses.setAdapter(adapter);

            // Set up item click listener for editing
            listViewYogaClasses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    YogaClass selectedClass = yogaClassList.get(position);
                    showClassOptionsDialog(selectedClass);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Class Details");

        StringBuilder details = new StringBuilder();
        details.append("Day: ").append(yogaClass.getDayOfWeek()).append("\n\n");
        details.append("Time: ").append(yogaClass.getTime()).append("\n\n");
        details.append("Type: ").append(yogaClass.getClassType()).append("\n\n");
        details.append("Capacity: ").append(yogaClass.getCapacity()).append(" people\n\n");
        details.append("Duration: ").append(yogaClass.getDuration()).append(" minutes\n\n");
        details.append("Price: £").append(String.format("%.2f", yogaClass.getPrice())).append("\n\n");
        details.append("Instructor: ").append(
            yogaClass.getInstructor() != null && !yogaClass.getInstructor().trim().isEmpty() 
                ? yogaClass.getInstructor() : "Not specified").append("\n\n");
        details.append("Difficulty: ").append(yogaClass.getDifficulty()).append("\n\n");
        details.append("Description: ").append(
            yogaClass.getDescription() != null && !yogaClass.getDescription().trim().isEmpty() 
                ? yogaClass.getDescription() : "No description provided");

        builder.setMessage(details.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
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
        intent.putExtra("instructor", yogaClass.getInstructor());
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadYogaClasses(); // Refresh the list when returning from edit
    }
    
    private void manageClassInstances(YogaClass yogaClass) {
        // Log the yoga class details before creating the intent
        System.out.println("Managing class instances for: " + yogaClass.getClassType() + ", ID: " + yogaClass.getId());
        
        Intent intent = new Intent(this, ManageClassInstancesActivity.class);
        intent.putExtra("classId", yogaClass.getId());
        intent.putExtra("className", yogaClass.getClassType());
        intent.putExtra("dayOfWeek", yogaClass.getDayOfWeek());
        intent.putExtra("time", yogaClass.getTime());
        
        // Log the intent extras for debugging
        System.out.println("Intent extras - classId: " + yogaClass.getId() + 
                          ", className: " + yogaClass.getClassType() + 
                          ", dayOfWeek: " + yogaClass.getDayOfWeek() + 
                          ", time: " + yogaClass.getTime());
        
        startActivity(intent);
    }
}