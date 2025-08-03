package com.example.yoga_admin_app;

import androidx.appcompat.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageClassInstancesActivity extends AppCompatActivity {

    private TextView tvClassName;
    private TextView tvClassDetails;
    private ListView listViewInstances;
    private LinearLayout tvNoInstances;
    private Button btnAddInstance;
    private Button btnBack;

    private DatabaseHelper databaseHelper;
    private ClassInstanceAdapter adapter;
    private List<ClassInstance> instanceList;
    private YogaClass yogaClass;
    private long yogaClassId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_class_instances);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Get yoga class ID from intent
        yogaClassId = getIntent().getLongExtra("classId", -1);
        if (yogaClassId == -1) {
            Toast.makeText(this, "Error: Yoga class ID not found in intent", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get the yoga class
        yogaClass = databaseHelper.getYogaClass(yogaClassId);
        if (yogaClass == null) {
            Toast.makeText(this, "Error: Yoga class with ID " + yogaClassId + " not found in database", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Log successful retrieval for debugging
        System.out.println("Successfully retrieved yoga class");

        // Initialize views
        tvClassName = findViewById(R.id.tv_class_name);
        tvClassDetails = findViewById(R.id.tv_class_details);
        listViewInstances = findViewById(R.id.listview_class_instances);
        tvNoInstances = findViewById(R.id.tv_no_instances);
        btnAddInstance = findViewById(R.id.btn_add_instance);
        btnBack = findViewById(R.id.btn_back);

        // Set class details
        tvClassName.setText(yogaClass.getClassType());
        tvClassDetails.setText(yogaClass.getDayOfWeek() + " at " + yogaClass.getTime());

        // Load class instances
        loadClassInstances();

        // Set up click listeners
        btnAddInstance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddInstanceDialog();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set up item click listener for editing/deleting instances
        listViewInstances.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClassInstance instance = (ClassInstance) adapter.getItem(position);
                showInstanceOptionsDialog(instance);
            }
        });
    }

    private void loadClassInstances() {
        instanceList = databaseHelper.getClassInstancesByYogaClassId(yogaClassId);

        if (instanceList.isEmpty()) {
            listViewInstances.setVisibility(View.GONE);
            tvNoInstances.setVisibility(View.VISIBLE);
        } else {
            listViewInstances.setVisibility(View.VISIBLE);
            tvNoInstances.setVisibility(View.GONE);

            adapter = new ClassInstanceAdapter(this, instanceList);
            listViewInstances.setAdapter(adapter);
        }
    }

    private void showAddInstanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Class Instance");

        // Inflate the dialog layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_class_instance, null);
        builder.setView(view);

        final Button btnSelectDate = view.findViewById(R.id.btn_select_date);
        final EditText etInstructor = view.findViewById(R.id.et_instructor);
        final EditText etComments = view.findViewById(R.id.et_comments);

        // Variable to store selected date
        final String[] selectedDate = {""};

        // Set up date picker button
        btnSelectDate.setText("📅 Select " + yogaClass.getDayOfWeek() + " Date");
        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("=== DATE BUTTON CLICKED ===");
                System.out.println("Day of week: " + yogaClass.getDayOfWeek());
                showDatePickerForDayOfWeek(yogaClass.getDayOfWeek(), new DatePickerCallback() {
                    @Override
                    public void onDateSelected(String date) {
                        System.out.println("Date selected: " + date);
                        selectedDate[0] = date;
                        btnSelectDate.setText(date);
                        btnSelectDate.setTextColor(getResources().getColor(android.R.color.black));
                    }
                });
            }
        });

        // Set positive and negative buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Validate input
                String date = selectedDate[0];
                String instructor = etInstructor.getText().toString().trim();
                String comments = etComments.getText().toString().trim();

                if (date.isEmpty() || instructor.isEmpty()) {
                    Toast.makeText(ManageClassInstancesActivity.this, "Date and instructor are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create and save the class instance
                ClassInstance instance = new ClassInstance();
                instance.setYogaClassId(yogaClassId);
                instance.setDate(date);
                instance.setInstructor(instructor);
                instance.setAdditionalComments(comments);

                long id = databaseHelper.addClassInstance(instance);
                if (id > 0) {
                    Toast.makeText(ManageClassInstancesActivity.this, "Class instance added successfully", Toast.LENGTH_SHORT).show();
                    loadClassInstances(); // Refresh the list
                } else {
                    Toast.makeText(ManageClassInstancesActivity.this, "Failed to add class instance", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void showInstanceOptionsDialog(final ClassInstance instance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Class Instance Options");
        String[] options = {"Edit", "Delete"};

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Edit
                        showEditInstanceDialog(instance);
                        break;
                    case 1: // Delete
                        showDeleteInstanceConfirmation(instance);
                        break;
                }
            }
        });

        builder.create().show();
    }

    private void showEditInstanceDialog(final ClassInstance instance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Class Instance");

        // Inflate the dialog layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_class_instance, null);
        builder.setView(view);

        final Button btnSelectDate = view.findViewById(R.id.btn_select_date);
        final EditText etInstructor = view.findViewById(R.id.et_instructor);
        final EditText etComments = view.findViewById(R.id.et_comments);

        // Variable to store selected date, initialize with existing date
        final String[] selectedDate = {instance.getDate()};

        // Pre-fill with existing data
        btnSelectDate.setText(instance.getDate());
        btnSelectDate.setTextColor(getResources().getColor(android.R.color.black));
        etInstructor.setText(instance.getInstructor());
        etComments.setText(instance.getAdditionalComments());

        // Set up date picker button
        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerForDayOfWeek(yogaClass.getDayOfWeek(), new DatePickerCallback() {
                    @Override
                    public void onDateSelected(String date) {
                        selectedDate[0] = date;
                        btnSelectDate.setText(date);
                        btnSelectDate.setTextColor(getResources().getColor(android.R.color.black));
                    }
                });
            }
        });

        // Set positive and negative buttons
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Validate input
                String date = selectedDate[0];
                String instructor = etInstructor.getText().toString().trim();
                String comments = etComments.getText().toString().trim();

                if (date.isEmpty() || instructor.isEmpty()) {
                    Toast.makeText(ManageClassInstancesActivity.this, "Date and instructor are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update the class instance
                instance.setDate(date);
                instance.setInstructor(instructor);
                instance.setAdditionalComments(comments);

                int result = databaseHelper.updateClassInstance(instance);
                if (result > 0) {
                    Toast.makeText(ManageClassInstancesActivity.this, "Class instance updated successfully", Toast.LENGTH_SHORT).show();
                    loadClassInstances(); // Refresh the list
                } else {
                    Toast.makeText(ManageClassInstancesActivity.this, "Failed to update class instance", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void showDeleteInstanceConfirmation(final ClassInstance instance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Class Instance");
        builder.setMessage("Are you sure you want to delete this class instance?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseHelper.deleteClassInstance(instance.getId());
                Toast.makeText(ManageClassInstancesActivity.this, "Class instance deleted", Toast.LENGTH_SHORT).show();
                loadClassInstances(); // Refresh the list
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    // Helper method to validate date format
    private boolean isValidDateFormat(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // Helper method to check if date matches day of week
    private boolean isDateMatchingDayOfWeek(String dateStr, String requiredDayOfWeek) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = dateFormat.parse(dateStr);
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            String dayName;
            
            // Note: Calendar.DAY_OF_WEEK uses 1 (Sunday) through 7 (Saturday)
            switch (dayOfWeek) {
                case Calendar.SUNDAY: dayName = "Sunday"; break;    // Calendar.SUNDAY = 1
                case Calendar.MONDAY: dayName = "Monday"; break;    // Calendar.MONDAY = 2
                case Calendar.TUESDAY: dayName = "Tuesday"; break;   // Calendar.TUESDAY = 3
                case Calendar.WEDNESDAY: dayName = "Wednesday"; break; // Calendar.WEDNESDAY = 4
                case Calendar.THURSDAY: dayName = "Thursday"; break;  // Calendar.THURSDAY = 5
                case Calendar.FRIDAY: dayName = "Friday"; break;    // Calendar.FRIDAY = 6
                case Calendar.SATURDAY: dayName = "Saturday"; break;  // Calendar.SATURDAY = 7
                default: dayName = "";
            }
            
            return dayName.equalsIgnoreCase(requiredDayOfWeek);
        } catch (ParseException e) {
            System.out.println("ParseException in isDateMatchingDayOfWeek");
            e.printStackTrace();
            return false;
        }
    }

    // Interface for date picker callback
    private interface DatePickerCallback {
        void onDateSelected(String date);
    }

    // Method to show date picker that only allows selection of dates matching the specified day of week
    private void showDatePickerForDayOfWeek(String requiredDayOfWeek, DatePickerCallback callback) {
        System.out.println("=== showDatePickerForDayOfWeek called ===");
        System.out.println("Required day: " + requiredDayOfWeek);
        
        // Use the smart date selection that generates real dates for the correct day of week
        showCustomDateSelectionDialog(requiredDayOfWeek, callback);
    }

    // Simplified date selection that always shows dates
    private void showSimpleDateSelectionDialog(String requiredDayOfWeek, DatePickerCallback callback) {
        System.out.println("=== STARTING showSimpleDateSelectionDialog ===");
        System.out.println("Required day of week: " + requiredDayOfWeek);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Date for " + requiredDayOfWeek + " Class");
        
        // Create simple weekly dates starting from next week
        List<String> dates = new java.util.ArrayList<>();
        Calendar cal = Calendar.getInstance();
        
        System.out.println("Current date: " + cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 7); // Start from next week
        System.out.println("Starting date: " + cal.getTime());
        
        // Generate 8 weeks of dates
        for (int i = 0; i < 8; i++) {
            String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR));
            
            // Create display format
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String displayDate = displayFormat.format(cal.getTime());
            
            String displayString = requiredDayOfWeek + " - " + displayDate + " (" + formattedDate + ")";
            dates.add(displayString);
            
            System.out.println("Generated date " + i + ": " + displayString);
            
            cal.add(Calendar.WEEK_OF_YEAR, 1); // Move to next week
        }
        
        System.out.println("Total dates generated: " + dates.size());
        
        if (dates.isEmpty()) {
            System.out.println("ERROR: No dates generated!");
            Toast.makeText(this, "Error generating dates", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] dateArray = dates.toArray(new String[0]);
        System.out.println("Date array length: " + dateArray.length);
        
        builder.setItems(dateArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("Date selected: " + which);
                // Extract the date from the display string
                String selectedItem = dateArray[which];
                String date = selectedItem.substring(selectedItem.lastIndexOf("(") + 1, selectedItem.lastIndexOf(")"));
                System.out.println("Extracted date: " + date);
                callback.onDateSelected(date);
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        // REMOVED setMessage() to allow items to show
        
        System.out.println("About to show dialog...");
        AlertDialog dialog = builder.create();
        dialog.show();
        System.out.println("Dialog shown!");
    }

    // Custom date selection dialog that shows only valid dates
    private void showCustomDateSelectionDialog(String requiredDayOfWeek, DatePickerCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Date for " + requiredDayOfWeek + " Class");
        
        // Generate next 8 weeks of valid dates for this day of week
        List<String> validDates = generateValidDates(requiredDayOfWeek, 8);
        
        System.out.println("Valid dates list size: " + validDates.size());
        for (String date : validDates) {
            System.out.println("Valid date: " + date);
        }
        
        if (validDates.isEmpty()) {
            // Fallback: show error message and use simple date generation
            Toast.makeText(this, "Using fallback date generation...", Toast.LENGTH_SHORT).show();
            validDates = generateFallbackDates(8);
        }
        
        String[] dateArray = validDates.toArray(new String[0]);
        
        // Create display strings with day info
        String[] displayArray = new String[dateArray.length];
        for (int i = 0; i < dateArray.length; i++) {
            displayArray[i] = formatDateForDisplay(dateArray[i], requiredDayOfWeek);
            System.out.println("Display string " + i + ": " + displayArray[i]);
        }
        
        builder.setItems(displayArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onDateSelected(dateArray[which]);
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        
        // REMOVED setMessage() to allow items to show
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Fallback method to generate simple future dates
    private List<String> generateFallbackDates(int numberOfDates) {
        List<String> dates = new java.util.ArrayList<>();
        Calendar cal = Calendar.getInstance();
        
        for (int i = 1; i <= numberOfDates; i++) {
            cal.add(Calendar.DAY_OF_MONTH, 7); // Add 7 days each time
            String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR));
            dates.add(formattedDate);
        }
        
        return dates;
    }

    // Generate a list of valid dates for the specified day of week
    private List<String> generateValidDates(String requiredDayOfWeek, int numberOfWeeks) {
        List<String> validDates = new java.util.ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        
        // Get the target day of week as Calendar constant
        int targetDayOfWeek = getCalendarDayOfWeek(requiredDayOfWeek);
        
        // Debug logging
        System.out.println("Required day: " + requiredDayOfWeek + ", Target day of week: " + targetDayOfWeek);
        System.out.println("Current date: " + calendar.getTime());
        System.out.println("Current day of week: " + calendar.get(Calendar.DAY_OF_WEEK));
        
        // Find the next occurrence of the target day of week
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysToAdd = (targetDayOfWeek - currentDayOfWeek + 7) % 7;
        
        // If it's the same day and we're past a reasonable time, move to next week
        if (daysToAdd == 0) {
            // If it's today but past 6 PM, move to next week
            if (calendar.get(Calendar.HOUR_OF_DAY) >= 18) {
                daysToAdd = 7;
            }
        }
        
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
        
        System.out.println("First valid date: " + calendar.getTime());
        
        // Generate dates for the specified number of weeks
        for (int i = 0; i < numberOfWeeks; i++) {
            String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR));
            validDates.add(formattedDate);
            
            System.out.println("Added date: " + formattedDate);
            
            // Move to next week (same day)
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
        
        System.out.println("Total valid dates generated: " + validDates.size());
        return validDates;
    }

    // Format date for better display in the selection list
    private String formatDateForDisplay(String dateStr, String dayOfWeek) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            
            String formattedDate = displayFormat.format(date);
            
            // Add relative time information
            Calendar today = Calendar.getInstance();
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(date);
            
            long diffInMillis = dateCalendar.getTimeInMillis() - today.getTimeInMillis();
            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
            
            String relativeInfo = "";
            if (diffInDays == 0) {
                relativeInfo = " (Today)";
            } else if (diffInDays == 1) {
                relativeInfo = " (Tomorrow)";
            } else if (diffInDays < 7) {
                relativeInfo = " (in " + diffInDays + " days)";
            } else if (diffInDays < 14) {
                relativeInfo = " (next week)";
            } else {
                int weeks = (int) (diffInDays / 7);
                relativeInfo = " (in " + weeks + " weeks)";
            }
            
            return dayOfWeek + " - " + formattedDate + relativeInfo;
            
        } catch (ParseException e) {
            return dayOfWeek + " - " + dateStr;
        }
    }

    // Convert day name to Calendar constant
    private int getCalendarDayOfWeek(String dayName) {
        switch (dayName.toLowerCase()) {
            case "sunday": return Calendar.SUNDAY;
            case "monday": return Calendar.MONDAY;
            case "tuesday": return Calendar.TUESDAY;
            case "wednesday": return Calendar.WEDNESDAY;
            case "thursday": return Calendar.THURSDAY;
            case "friday": return Calendar.FRIDAY;
            case "saturday": return Calendar.SATURDAY;
            default: return Calendar.MONDAY; // Default fallback
        }
    }

    // Helper method to get day name from Calendar day of week constant
    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "Sunday";
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
            default: return "";
        }
    }
}