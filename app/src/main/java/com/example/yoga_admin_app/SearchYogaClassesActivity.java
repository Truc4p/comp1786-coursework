package com.example.yoga_admin_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SearchYogaClassesActivity extends AppCompatActivity {

    private EditText etInstructorName;
    private Spinner spinnerDayOfWeek;
    private EditText etDate;
    private Button btnSearch;
    private Button btnClear;
    private Button btnBack;
    private ListView listviewSearchResults;
    private TextView tvSearchResultsHeader;
    private TextView tvNoResults;
    
    private DatabaseHelper databaseHelper;
    private YogaClassAdapter searchAdapter;
    private List<YogaClass> searchResults;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    
    private String[] daysOfWeek = {"All Days", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_yoga_classes);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        
        // Initialize date formatter
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        calendar = Calendar.getInstance();
        
        // Initialize search results list
        searchResults = new ArrayList<>();

        // Initialize views
        initializeViews();

        // Setup listeners
        setupListeners();
        
        // Setup day of week spinner
        setupDayOfWeekSpinner();
    }

    private void initializeViews() {
        etInstructorName = findViewById(R.id.et_instructor_name);
        spinnerDayOfWeek = findViewById(R.id.spinner_day_of_week);
        etDate = findViewById(R.id.et_date);
        btnSearch = findViewById(R.id.btn_search);
        btnClear = findViewById(R.id.btn_clear);
        btnBack = findViewById(R.id.btn_back);
        listviewSearchResults = findViewById(R.id.listview_search_results);
        tvSearchResultsHeader = findViewById(R.id.tv_search_results_header);
        tvNoResults = findViewById(R.id.tv_no_results);
    }

    private void setupListeners() {
        // Search button
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        // Clear button
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSearchFields();
            }
        });

        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Date picker
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Real-time search as user types instructor name
        etInstructorName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Perform search with a slight delay to avoid too many database calls
                if (s.length() >= 2) {
                    performSearch();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Search results list click listener
        listviewSearchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                YogaClass selectedClass = searchResults.get(position);
                showClassDetailsDialog(selectedClass);
            }
        });
    }

    private void setupDayOfWeekSpinner() {
        CustomSpinnerAdapter dayAdapter = new CustomSpinnerAdapter(this, daysOfWeek);
        spinnerDayOfWeek.setAdapter(dayAdapter);
        
        spinnerDayOfWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Auto-search when day is selected (except for "All Days")
                if (position > 0) {
                    performSearch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    etDate.setText(dateFormatter.format(calendar.getTime()));
                    performSearch();
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }

    private void performSearch() {
        String instructorName = etInstructorName.getText().toString().trim();
        String selectedDay = spinnerDayOfWeek.getSelectedItem().toString();
        String selectedDate = etDate.getText().toString().trim();
        
        // Convert "All Days" to empty string for search
        String dayOfWeek = selectedDay.equals("All Days") ? "" : selectedDay;
        
        // Check if at least one search criterion is provided
        if (instructorName.isEmpty() && dayOfWeek.isEmpty() && selectedDate.isEmpty()) {
            Toast.makeText(this, "Please enter at least one search criterion", Toast.LENGTH_SHORT).show();
            hideSearchResults();
            return;
        }
        
        // Perform search
        searchResults = databaseHelper.searchYogaClasses(instructorName, dayOfWeek, selectedDate);
        
        if (searchResults.isEmpty()) {
            showNoResults();
        } else {
            showSearchResults();
        }
    }

    private void showSearchResults() {
        tvSearchResultsHeader.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        listviewSearchResults.setVisibility(View.VISIBLE);
        
        searchAdapter = new YogaClassAdapter(this, searchResults);
        listviewSearchResults.setAdapter(searchAdapter);
        
        // Set the ListView height to show all items
        setListViewHeightBasedOnItems(listviewSearchResults);
        
        // Update header text with count
        tvSearchResultsHeader.setText("Search Results (" + searchResults.size() + " found)");
    }
    
    private void setListViewHeightBasedOnItems(ListView listView) {
        YogaClassAdapter adapter = (YogaClassAdapter) listView.getAdapter();
        if (adapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void showNoResults() {
        tvSearchResultsHeader.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.VISIBLE);
        listviewSearchResults.setVisibility(View.GONE);
    }

    private void hideSearchResults() {
        tvSearchResultsHeader.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);
        listviewSearchResults.setVisibility(View.GONE);
    }

    private void clearSearchFields() {
        etInstructorName.setText("");
        spinnerDayOfWeek.setSelection(0); // Set to "All Days"
        etDate.setText("");
        hideSearchResults();
    }

    private void showClassDetailsDialog(YogaClass yogaClass) {
        Intent intent = new Intent(this, ClassDetailsActivity.class);
        intent.putExtra("classId", yogaClass.getId());
        
        // Pass search context if available
        String instructorName = etInstructorName.getText().toString().trim();
        String selectedDate = etDate.getText().toString().trim();
        
        if (!instructorName.isEmpty()) {
            intent.putExtra("searchInstructor", instructorName);
        }
        
        if (!selectedDate.isEmpty()) {
            intent.putExtra("searchDate", selectedDate);
        }
        
        startActivity(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh search results if there are any
        if (!searchResults.isEmpty()) {
            performSearch();
        }
    }
}
