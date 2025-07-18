package com.example.yoga_admin_app;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class EditYogaClassActivity extends AppCompatActivity {

    private Spinner spinnerDayOfWeek;
    private EditText etTime;
    private EditText etCapacity;
    private EditText etDuration;
    private EditText etPrice;
    private Spinner spinnerClassType;
    private EditText etDescription;
    private Spinner spinnerDifficulty;
    private Button btnUpdate;
    private Button btnCancel;

    private DatabaseHelper databaseHelper;
    private long classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_yoga_class);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Get class ID from intent
        classId = getIntent().getLongExtra("classId", -1);
        if (classId == -1) {
            Toast.makeText(this, "Error: Class not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();
        
        // Setup spinners
        setupSpinners();
        
        // Setup time picker
        setupTimePicker();
        
        // Load existing data
        loadExistingData();
        
        // Setup button listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        spinnerDayOfWeek = findViewById(R.id.spinner_day_of_week);
        etTime = findViewById(R.id.et_time);
        etCapacity = findViewById(R.id.et_capacity);
        etDuration = findViewById(R.id.et_duration);
        etPrice = findViewById(R.id.et_price);
        spinnerClassType = findViewById(R.id.spinner_class_type);
        etDescription = findViewById(R.id.et_description);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        btnUpdate = findViewById(R.id.btn_update);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupSpinners() {
        // Days of week
        String[] daysOfWeek = {"Select day of week", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        CustomSpinnerAdapter dayAdapter = new CustomSpinnerAdapter(this, daysOfWeek);
        spinnerDayOfWeek.setAdapter(dayAdapter);

        // Class types
        String[] classTypes = {"Select class type", "Flow Yoga", "Aerial Yoga", "Family Yoga", "Hatha Yoga", "Vinyasa Yoga", "Restorative Yoga"};
        CustomSpinnerAdapter typeAdapter = new CustomSpinnerAdapter(this, classTypes);
        spinnerClassType.setAdapter(typeAdapter);

        // Difficulty levels
        String[] difficulties = {"Select difficulty level", "Beginner", "Intermediate", "Advanced"};
        CustomSpinnerAdapter difficultyAdapter = new CustomSpinnerAdapter(this, difficulties);
        spinnerDifficulty.setAdapter(difficultyAdapter);
    }

    private void setupTimePicker() {
        // Make time field non-editable but clickable
        etTime.setFocusable(false);
        etTime.setClickable(true);
        
        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
    }

    private void showTimePickerDialog() {
        // Get current time from the field or use current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Try to parse existing time if available
        String currentTime = etTime.getText().toString().trim();
        if (!currentTime.isEmpty()) {
            try {
                // Parse time like "10:00 AM"
                String[] parts = currentTime.split(" ");
                if (parts.length == 2) {
                    String[] timeParts = parts[0].split(":");
                    if (timeParts.length == 2) {
                        int parsedHour = Integer.parseInt(timeParts[0]);
                        int parsedMinute = Integer.parseInt(timeParts[1]);
                        
                        if (parts[1].equalsIgnoreCase("PM") && parsedHour != 12) {
                            parsedHour += 12;
                        } else if (parts[1].equalsIgnoreCase("AM") && parsedHour == 12) {
                            parsedHour = 0;
                        }
                        
                        hour = parsedHour;
                        minute = parsedMinute;
                    }
                }
            } catch (NumberFormatException e) {
                // Use current time if parsing fails
            }
        }

        // Create time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Format time in 12-hour format with AM/PM
                        String timeString = formatTime(hourOfDay, minute);
                        etTime.setText(timeString);
                    }
                }, hour, minute, false); // false for 12-hour format

        timePickerDialog.show();
    }

    private String formatTime(int hour, int minute) {
        String amPm = "AM";
        int displayHour = hour;
        
        if (hour == 0) {
            displayHour = 12;
        } else if (hour > 12) {
            displayHour = hour - 12;
            amPm = "PM";
        } else if (hour == 12) {
            amPm = "PM";
        }
        
        return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm);
    }

    private void loadExistingData() {
        Intent intent = getIntent();
        
        // Set spinner selections
        setSpinnerSelection(spinnerDayOfWeek, intent.getStringExtra("dayOfWeek"));
        setSpinnerSelection(spinnerClassType, intent.getStringExtra("classType"));
        setSpinnerSelection(spinnerDifficulty, intent.getStringExtra("difficulty"));
        
        // Set text fields
        etTime.setText(intent.getStringExtra("time"));
        etCapacity.setText(String.valueOf(intent.getIntExtra("capacity", 0)));
        etDuration.setText(String.valueOf(intent.getIntExtra("duration", 0)));
        etPrice.setText(String.valueOf(intent.getDoubleExtra("price", 0.0)));
        etDescription.setText(intent.getStringExtra("description"));
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value != null) {
            CustomSpinnerAdapter adapter = (CustomSpinnerAdapter) spinner.getAdapter();
            // Find the position of the value in the adapter
            for (int i = 0; i < adapter.getCount(); i++) {
                if (value.equals(adapter.getItem(i))) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupButtonListeners() {
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    updateYogaClass();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean validateInputs() {
        // Reset error states
        clearErrors();

        boolean isValid = true;

        // Validate day of week
        if (spinnerDayOfWeek.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a day of week", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate time
        if (etTime.getText().toString().trim().isEmpty()) {
            etTime.setError("Time is required");
            isValid = false;
        }

        // Validate capacity
        String capacityStr = etCapacity.getText().toString().trim();
        if (capacityStr.isEmpty()) {
            etCapacity.setError("Capacity is required");
            isValid = false;
        } else {
            try {
                int capacity = Integer.parseInt(capacityStr);
                if (capacity <= 0) {
                    etCapacity.setError("Capacity must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etCapacity.setError("Please enter a valid number");
                isValid = false;
            }
        }

        // Validate duration
        String durationStr = etDuration.getText().toString().trim();
        if (durationStr.isEmpty()) {
            etDuration.setError("Duration is required");
            isValid = false;
        } else {
            try {
                int duration = Integer.parseInt(durationStr);
                if (duration <= 0) {
                    etDuration.setError("Duration must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etDuration.setError("Please enter a valid number");
                isValid = false;
            }
        }

        // Validate price
        String priceStr = etPrice.getText().toString().trim();
        if (priceStr.isEmpty()) {
            etPrice.setError("Price is required");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceStr);
                if (price < 0) {
                    etPrice.setError("Price cannot be negative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etPrice.setError("Please enter a valid price");
                isValid = false;
            }
        }

        // Validate class type
        if (spinnerClassType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a class type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate difficulty
        if (spinnerDifficulty.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a difficulty level", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(this, "Please correct the errors above", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    private void clearErrors() {
        etTime.setError(null);
        etCapacity.setError(null);
        etDuration.setError(null);
        etPrice.setError(null);
    }

    private void updateYogaClass() {
        // Create updated YogaClass object
        YogaClass yogaClass = new YogaClass();
        yogaClass.setId(classId);
        yogaClass.setDayOfWeek(spinnerDayOfWeek.getSelectedItem().toString());
        yogaClass.setTime(etTime.getText().toString().trim());
        yogaClass.setCapacity(Integer.parseInt(etCapacity.getText().toString().trim()));
        yogaClass.setDuration(Integer.parseInt(etDuration.getText().toString().trim()));
        yogaClass.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
        yogaClass.setClassType(spinnerClassType.getSelectedItem().toString());
        yogaClass.setDescription(etDescription.getText().toString().trim());
        yogaClass.setInstructor(""); // Set empty string for instructor field
        yogaClass.setDifficulty(spinnerDifficulty.getSelectedItem().toString());

        // Update in database
        int result = databaseHelper.updateYogaClass(yogaClass);
        
        if (result > 0) {
            Toast.makeText(this, "Yoga class updated successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Return to previous activity
        } else {
            Toast.makeText(this, "Error updating yoga class. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
} 