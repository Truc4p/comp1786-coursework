package com.example.yoga_admin_app;

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
        System.out.println("Successfully retrieved yoga class: " + yogaClass.getClassType() + ", ID: " + yogaClass.getId());

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

        final EditText etDate = view.findViewById(R.id.et_date);
        final EditText etTeacher = view.findViewById(R.id.et_teacher);
        final EditText etComments = view.findViewById(R.id.et_comments);

        // Set positive and negative buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Validate input
                String date = etDate.getText().toString().trim();
                String teacher = etTeacher.getText().toString().trim();
                String comments = etComments.getText().toString().trim();

                if (date.isEmpty() || teacher.isEmpty()) {
                    Toast.makeText(ManageClassInstancesActivity.this, "Date and teacher are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate date format (dd/MM/yyyy)
                if (!isValidDateFormat(date)) {
                    Toast.makeText(ManageClassInstancesActivity.this, "Invalid date format. Use dd/MM/yyyy", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if date matches day of week
                if (!isDateMatchingDayOfWeek(date, yogaClass.getDayOfWeek())) {
                    Toast.makeText(ManageClassInstancesActivity.this, 
                            "Date must be a " + yogaClass.getDayOfWeek(), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create and save the class instance
                ClassInstance instance = new ClassInstance();
                instance.setYogaClassId(yogaClassId);
                instance.setDate(date);
                instance.setTeacher(teacher);
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

        final EditText etDate = view.findViewById(R.id.et_date);
        final EditText etTeacher = view.findViewById(R.id.et_teacher);
        final EditText etComments = view.findViewById(R.id.et_comments);

        // Pre-fill with existing data
        etDate.setText(instance.getDate());
        etTeacher.setText(instance.getTeacher());
        etComments.setText(instance.getAdditionalComments());

        // Set positive and negative buttons
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Validate input
                String date = etDate.getText().toString().trim();
                String teacher = etTeacher.getText().toString().trim();
                String comments = etComments.getText().toString().trim();

                if (date.isEmpty() || teacher.isEmpty()) {
                    Toast.makeText(ManageClassInstancesActivity.this, "Date and teacher are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate date format (dd/MM/yyyy)
                if (!isValidDateFormat(date)) {
                    Toast.makeText(ManageClassInstancesActivity.this, "Invalid date format. Use dd/MM/yyyy", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if date matches day of week
                if (!isDateMatchingDayOfWeek(date, yogaClass.getDayOfWeek())) {
                    Toast.makeText(ManageClassInstancesActivity.this, 
                            "Date must be a " + yogaClass.getDayOfWeek(), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update the class instance
                instance.setDate(date);
                instance.setTeacher(teacher);
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
            System.out.println("Checking if date " + dateStr + " matches day of week " + requiredDayOfWeek);
            
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
            
            System.out.println("Calendar.DAY_OF_WEEK value: " + dayOfWeek + ", Calculated day name: " + dayName);
            System.out.println("Required day of week: " + requiredDayOfWeek);
            System.out.println("Do they match? " + dayName.equalsIgnoreCase(requiredDayOfWeek));
            
            return dayName.equalsIgnoreCase(requiredDayOfWeek);
        } catch (ParseException e) {
            System.out.println("ParseException in isDateMatchingDayOfWeek: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}