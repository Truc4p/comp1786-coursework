package com.example.yoga_admin_app;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Calendar;
import java.util.Locale;

public class AddYogaClassActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private Spinner spinnerDayOfWeek;
    private EditText etTime;
    private EditText etCapacity;
    private EditText etDuration;
    private EditText etPrice;
    private Spinner spinnerClassType;
    private EditText etDescription;
    private Spinner spinnerDifficulty;
    private Button btnConfirm;
    private Button btnCancel;
    
    // Location fields
    private Button btnGetLocation;
    private TextView tvLocationStatus;
    private EditText etLocationAddress;
    
    private DatabaseHelper databaseHelper;
    private LocationService locationService;
    
    // Location data
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentLocationAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_yoga_class);

        // Initialize database helper and location service
        databaseHelper = new DatabaseHelper(this);
        locationService = new LocationService(this);

        // Initialize views
        initializeViews();
        
        // Setup spinners
        setupSpinners();
        
        // Setup time picker
        setupTimePicker();
        
        // Setup location functionality
        setupLocationFunctionality();
        
        // Setup button listeners
        setupButtonListeners();
        
        // Check if returning from confirmation with data to restore
        restoreFormDataIfNeeded();
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
        btnConfirm = findViewById(R.id.btn_confirm);
        btnCancel = findViewById(R.id.btn_cancel);
        
        // Location views
        btnGetLocation = findViewById(R.id.btn_get_location);
        tvLocationStatus = findViewById(R.id.tv_location_status);
        etLocationAddress = findViewById(R.id.et_location_address);
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
        // Get current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

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

    private void setupLocationFunctionality() {
        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });
    }

    private void getCurrentLocation() {
        // Check permissions first
        if (!locationService.hasLocationPermissions()) {
            requestLocationPermissions();
            return;
        }

        // Show loading state
        btnGetLocation.setEnabled(false);
        tvLocationStatus.setText("🔄 Getting your location...");
        
        locationService.getCurrentLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude, String address) {
                runOnUiThread(() -> {
                    currentLatitude = latitude;
                    currentLongitude = longitude;
                    currentLocationAddress = address;
                    
                    // Update UI
                    etLocationAddress.setText(address);
                    tvLocationStatus.setText("✅ Location detected successfully");
                    btnGetLocation.setEnabled(true);
                    btnGetLocation.setText("📍 Update Location");
                    
                    Toast.makeText(AddYogaClassActivity.this, "Location detected!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onLocationError(String error) {
                runOnUiThread(() -> {
                    tvLocationStatus.setText("❌ " + error);
                    btnGetLocation.setEnabled(true);
                    Toast.makeText(AddYogaClassActivity.this, "Location error: " + error, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onPermissionRequired() {
                runOnUiThread(() -> {
                    requestLocationPermissions();
                });
            }
        });
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tvLocationStatus.setText("Permission granted! Tap to get location");
                Toast.makeText(this, "Location permission granted. Tap the button to get your location.", Toast.LENGTH_SHORT).show();
            } else {
                tvLocationStatus.setText("Location permission required for automatic detection");
                Toast.makeText(this, "Location permission denied. You can still create classes without location.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationService != null) {
            locationService.stopLocationUpdates();
        }
    }

    private void setupButtonListeners() {
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    createYogaClassAndProceed();
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

    private void createYogaClassAndProceed() {
        // Create YogaClass object
        YogaClass yogaClass = new YogaClass();
        yogaClass.setDayOfWeek(spinnerDayOfWeek.getSelectedItem().toString());
        yogaClass.setTime(etTime.getText().toString().trim());
        yogaClass.setCapacity(Integer.parseInt(etCapacity.getText().toString().trim()));
        yogaClass.setDuration(Integer.parseInt(etDuration.getText().toString().trim()));
        yogaClass.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
        yogaClass.setClassType(spinnerClassType.getSelectedItem().toString());
        yogaClass.setDescription(etDescription.getText().toString().trim());
        yogaClass.setInstructor(""); // Set empty string for instructor field
        yogaClass.setDifficulty(spinnerDifficulty.getSelectedItem().toString());
        
        // Set location data
        yogaClass.setLatitude(currentLatitude);
        yogaClass.setLongitude(currentLongitude);
        yogaClass.setLocationAddress(currentLocationAddress);

        // Pass to confirmation activity
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra("dayOfWeek", yogaClass.getDayOfWeek());
        intent.putExtra("time", yogaClass.getTime());
        intent.putExtra("capacity", yogaClass.getCapacity());
        intent.putExtra("duration", yogaClass.getDuration());
        intent.putExtra("price", yogaClass.getPrice());
        intent.putExtra("classType", yogaClass.getClassType());
        intent.putExtra("description", yogaClass.getDescription());
        intent.putExtra("difficulty", yogaClass.getDifficulty());
        intent.putExtra("latitude", yogaClass.getLatitude());
        intent.putExtra("longitude", yogaClass.getLongitude());
        intent.putExtra("locationAddress", yogaClass.getLocationAddress());
        startActivity(intent);
        finish();
    }
    
    private void restoreFormDataIfNeeded() {
        Intent intent = getIntent();
        boolean isEditMode = intent.getBooleanExtra("isEditMode", false);
        
        if (isEditMode) {
            // Restore all form fields with the data passed back from confirmation
            
            // Day of week
            String dayOfWeek = intent.getStringExtra("dayOfWeek");
            if (dayOfWeek != null) {
                for (int i = 0; i < spinnerDayOfWeek.getCount(); i++) {
                    if (spinnerDayOfWeek.getItemAtPosition(i).toString().equals(dayOfWeek)) {
                        spinnerDayOfWeek.setSelection(i);
                        break;
                    }
                }
            }
            
            // Time
            String time = intent.getStringExtra("time");
            if (time != null) {
                etTime.setText(time);
            }
            
            // Capacity
            int capacity = intent.getIntExtra("capacity", 0);
            if (capacity > 0) {
                etCapacity.setText(String.valueOf(capacity));
            }
            
            // Duration
            int duration = intent.getIntExtra("duration", 0);
            if (duration > 0) {
                etDuration.setText(String.valueOf(duration));
            }
            
            // Price
            double price = intent.getDoubleExtra("price", 0.0);
            if (price > 0) {
                etPrice.setText(String.valueOf(price));
            }
            
            // Class type
            String classType = intent.getStringExtra("classType");
            if (classType != null) {
                for (int i = 0; i < spinnerClassType.getCount(); i++) {
                    if (spinnerClassType.getItemAtPosition(i).toString().equals(classType)) {
                        spinnerClassType.setSelection(i);
                        break;
                    }
                }
            }
            
            // Description
            String description = intent.getStringExtra("description");
            if (description != null) {
                etDescription.setText(description);
            }
            
            // Difficulty
            String difficulty = intent.getStringExtra("difficulty");
            if (difficulty != null) {
                for (int i = 0; i < spinnerDifficulty.getCount(); i++) {
                    if (spinnerDifficulty.getItemAtPosition(i).toString().equals(difficulty)) {
                        spinnerDifficulty.setSelection(i);
                        break;
                    }
                }
            }
            
            // Location data
            double latitude = intent.getDoubleExtra("latitude", 0.0);
            double longitude = intent.getDoubleExtra("longitude", 0.0);
            String locationAddress = intent.getStringExtra("locationAddress");
            
            if (latitude != 0.0 || longitude != 0.0 || (locationAddress != null && !locationAddress.isEmpty())) {
                currentLatitude = latitude;
                currentLongitude = longitude;
                currentLocationAddress = locationAddress != null ? locationAddress : "";
                
                if (etLocationAddress != null) {
                    etLocationAddress.setText(currentLocationAddress);
                }
                
                if (tvLocationStatus != null) {
                    if (!currentLocationAddress.isEmpty()) {
                        tvLocationStatus.setText("✅ Location detected: " + currentLocationAddress);
                    } else if (latitude != 0.0 || longitude != 0.0) {
                        tvLocationStatus.setText("✅ Location detected: " + latitude + ", " + longitude);
                    }
                }
            }
        }
    }
} 