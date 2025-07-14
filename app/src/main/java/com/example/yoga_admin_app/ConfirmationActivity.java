package com.example.yoga_admin_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ConfirmationActivity extends AppCompatActivity {

    private TextView tvDayOfWeek;
    private TextView tvTime;
    private TextView tvCapacity;
    private TextView tvDuration;
    private TextView tvPrice;
    private TextView tvClassType;
    private TextView tvDescription;
    private TextView tvInstructor;
    private TextView tvDifficulty;
    private Button btnSave;
    private Button btnBack;

    private DatabaseHelper databaseHelper;
    private YogaClass yogaClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Get data from intent
        getDataFromIntent();

        // Display data
        displayData();

        // Setup button listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        tvDayOfWeek = findViewById(R.id.tv_day_of_week);
        tvTime = findViewById(R.id.tv_time);
        tvCapacity = findViewById(R.id.tv_capacity);
        tvDuration = findViewById(R.id.tv_duration);
        tvPrice = findViewById(R.id.tv_price);
        tvClassType = findViewById(R.id.tv_class_type);
        tvDescription = findViewById(R.id.tv_description);
        tvInstructor = findViewById(R.id.tv_instructor);
        tvDifficulty = findViewById(R.id.tv_difficulty);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        yogaClass = new YogaClass();
        yogaClass.setDayOfWeek(intent.getStringExtra("dayOfWeek"));
        yogaClass.setTime(intent.getStringExtra("time"));
        yogaClass.setCapacity(intent.getIntExtra("capacity", 0));
        yogaClass.setDuration(intent.getIntExtra("duration", 0));
        yogaClass.setPrice(intent.getDoubleExtra("price", 0.0));
        yogaClass.setClassType(intent.getStringExtra("classType"));
        yogaClass.setDescription(intent.getStringExtra("description"));
        yogaClass.setInstructor(intent.getStringExtra("instructor"));
        yogaClass.setDifficulty(intent.getStringExtra("difficulty"));
    }

    private void displayData() {
        tvDayOfWeek.setText(yogaClass.getDayOfWeek());
        tvTime.setText(yogaClass.getTime());
        tvCapacity.setText(String.valueOf(yogaClass.getCapacity()) + " people");
        tvDuration.setText(String.valueOf(yogaClass.getDuration()) + " minutes");
        tvPrice.setText("£" + String.format("%.2f", yogaClass.getPrice()));
        tvClassType.setText(yogaClass.getClassType());
        
        // Handle optional fields
        String description = yogaClass.getDescription();
        if (description == null || description.trim().isEmpty()) {
            tvDescription.setText("No description provided");
            tvDescription.setTypeface(null, android.graphics.Typeface.ITALIC);
        } else {
            tvDescription.setText(description);
        }

        String instructor = yogaClass.getInstructor();
        if (instructor == null || instructor.trim().isEmpty()) {
            tvInstructor.setText("Not specified");
            tvInstructor.setTypeface(null, android.graphics.Typeface.ITALIC);
        } else {
            tvInstructor.setText(instructor);
        }

        tvDifficulty.setText(yogaClass.getDifficulty());
    }

    private void setupButtonListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveYogaClass();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to AddYogaClassActivity
                Intent intent = new Intent(ConfirmationActivity.this, AddYogaClassActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void saveYogaClass() {
        long result = databaseHelper.addYogaClass(yogaClass);
        
        if (result != -1) {
            Toast.makeText(this, "Yoga class added successfully!", Toast.LENGTH_SHORT).show();
            
            // Go back to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error saving yoga class. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
} 