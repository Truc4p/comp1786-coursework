package com.example.yoga_admin_app;

import android.content.Intent;
import android.os.Bundle;
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
    private TextView tvClassCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        
        // Initialize views
        initializeViews();
        
        // Update class count
        updateClassCount();
    }

    private void initializeViews() {
        Button btnAddClass = findViewById(R.id.btn_add_class);
        Button btnViewClasses = findViewById(R.id.btn_view_classes);
        Button btnResetDatabase = findViewById(R.id.btn_reset_database);
        tvClassCount = findViewById(R.id.tv_class_count);

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

        btnResetDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetConfirmation();
            }
        });
    }

    private void showResetConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Database");
        builder.setMessage("Are you sure you want to delete all yoga classes? This action cannot be undone.");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            databaseHelper.resetDatabase();
            updateClassCount();
            Toast.makeText(MainActivity.this, "Database reset successfully", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void updateClassCount() {
        int count = databaseHelper.getYogaClassCount();
        tvClassCount.setText("Total Classes: " + count);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateClassCount();
    }
}