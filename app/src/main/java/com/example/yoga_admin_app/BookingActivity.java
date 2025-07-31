package com.example.yoga_admin_app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class BookingActivity extends AppCompatActivity {

    private ListView listViewBookings;
    private LinearLayout tvNoBookings;
    private Button btnBack;
    private Button btnSyncBookings;
    private Button btnRefresh;
    private EditText etSearchBookings;
    private Spinner spinnerStatusFilter;
    private DatabaseHelper databaseHelper;
    private CloudSyncService cloudSyncService;
    private BookingAdapter adapter;
    private List<Booking> bookingList;
    private List<Booking> filteredBookingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Initialize database helper and cloud sync service
        databaseHelper = new DatabaseHelper(this);
        cloudSyncService = new CloudSyncService(this);

        // Initialize views
        initializeViews();

        // Set up event listeners
        setupEventListeners();

        // Load and display bookings
        loadBookings();
    }

    private void initializeViews() {
        listViewBookings = findViewById(R.id.listViewBookings);
        tvNoBookings = findViewById(R.id.tvNoBookings);
        btnBack = findViewById(R.id.btnBack);
        btnSyncBookings = findViewById(R.id.btnSyncBookings);
        btnRefresh = findViewById(R.id.btnRefresh);
        etSearchBookings = findViewById(R.id.etSearchBookings);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);

        // Set up status filter spinner
        setupStatusFilter();
    }

    private void setupStatusFilter() {
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("All Bookings");
        statusOptions.add("Confirmed");
        statusOptions.add("Pending");
        statusOptions.add("Cancelled");
        statusOptions.add("Completed");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);
    }

    private void setupEventListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Sync bookings button
        btnSyncBookings.setOnClickListener(v -> syncBookingsFromCloud());

        // Refresh button
        btnRefresh.setOnClickListener(v -> {
            loadBookings();
            Toast.makeText(this, "Bookings refreshed", Toast.LENGTH_SHORT).show();
        });

        // Search functionality
        etSearchBookings.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBookings();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Status filter
        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterBookings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Booking item click for details/status update
        listViewBookings.setOnItemClickListener((parent, view, position, id) -> {
            if (filteredBookingList != null && position < filteredBookingList.size()) {
                Booking booking = filteredBookingList.get(position);
                showBookingDetailsDialog(booking);
            }
        });
    }

    private void loadBookings() {
        // Debug: Log database state
        int bookingCount = databaseHelper.getBookingCount();
        android.util.Log.d("BookingActivity", "Database booking count: " + bookingCount);
        databaseHelper.logAllBookingIds();
        
        bookingList = databaseHelper.getAllBookings();
        filteredBookingList = new ArrayList<>(bookingList);
        
        android.util.Log.d("BookingActivity", "Loaded " + bookingList.size() + " bookings from database");

        if (bookingList.isEmpty()) {
            listViewBookings.setVisibility(View.GONE);
            tvNoBookings.setVisibility(View.VISIBLE);
        } else {
            listViewBookings.setVisibility(View.VISIBLE);
            tvNoBookings.setVisibility(View.GONE);

            adapter = new BookingAdapter(this, filteredBookingList);
            listViewBookings.setAdapter(adapter);
        }
    }

    private void filterBookings() {
        if (bookingList == null) return;

        String searchText = etSearchBookings.getText().toString().toLowerCase().trim();
        String selectedStatus = spinnerStatusFilter.getSelectedItem().toString();

        filteredBookingList.clear();

        for (Booking booking : bookingList) {
            // Helper method to safely get string values for searching
            String customerName = booking.getCustomerName() != null ? booking.getCustomerName() : "";
            String customerEmail = booking.getCustomerEmail() != null ? booking.getCustomerEmail() : "";
            String className = booking.getClassName() != null ? booking.getClassName() : "";
            String bookingDate = booking.getBookingDate() != null ? booking.getBookingDate() : "";
            String status = booking.getStatus() != null ? booking.getStatus() : "";
            
            boolean matchesSearch = searchText.isEmpty() || 
                customerName.toLowerCase().contains(searchText) ||
                customerEmail.toLowerCase().contains(searchText) ||
                className.toLowerCase().contains(searchText) ||
                bookingDate.toLowerCase().contains(searchText);

            boolean matchesStatus = selectedStatus.equals("All Bookings") || 
                status.equalsIgnoreCase(selectedStatus);

            if (matchesSearch && matchesStatus) {
                filteredBookingList.add(booking);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Update visibility
        if (filteredBookingList.isEmpty()) {
            listViewBookings.setVisibility(View.GONE);
            tvNoBookings.setVisibility(View.VISIBLE);
        } else {
            listViewBookings.setVisibility(View.VISIBLE);
            tvNoBookings.setVisibility(View.GONE);
        }
    }

    private void syncBookingsFromCloud() {
        btnSyncBookings.setEnabled(false);
        btnSyncBookings.setText("Syncing...");

        cloudSyncService.syncBookingsFromFirebase(new CloudSyncService.SyncCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    btnSyncBookings.setEnabled(true);
                    btnSyncBookings.setText("Sync Bookings");
                    
                    // Clean up any bookings with empty IDs
                    int cleanedUp = databaseHelper.cleanupEmptyBookingIds();
                    final String finalMessage = cleanedUp > 0 ? 
                        message + ". Cleaned up " + cleanedUp + " invalid bookings." : message;
                    
                    Toast.makeText(BookingActivity.this, finalMessage, Toast.LENGTH_LONG).show();
                    
                    // Debug: Check database state after sync
                    android.util.Log.d("BookingActivity", "After sync - Database booking count: " + databaseHelper.getBookingCount());
                    databaseHelper.logAllBookingIds();
                    
                    loadBookings(); // Refresh the list
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnSyncBookings.setEnabled(true);
                    btnSyncBookings.setText("Sync Bookings");
                    Toast.makeText(BookingActivity.this, "Sync failed: " + error, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onProgress(String progress) {
                runOnUiThread(() -> {
                    btnSyncBookings.setText(progress);
                });
            }
        });
    }

    private void showBookingDetailsDialog(Booking booking) {
        // Debug logging to see what data we have
        android.util.Log.d("BookingActivity", "Showing booking details:");
        android.util.Log.d("BookingActivity", "  Booking ID: " + booking.getBookingId());
        android.util.Log.d("BookingActivity", "  Customer Name: " + booking.getCustomerName());
        android.util.Log.d("BookingActivity", "  Customer Email: " + booking.getCustomerEmail());
        android.util.Log.d("BookingActivity", "  Customer Phone: " + booking.getCustomerPhone());
        android.util.Log.d("BookingActivity", "  Class Name: " + booking.getClassName());
        android.util.Log.d("BookingActivity", "  Booking Date: " + booking.getBookingDate());
        android.util.Log.d("BookingActivity", "  Booking Time: " + booking.getBookingTime());
        android.util.Log.d("BookingActivity", "  Status: " + booking.getStatus());
        android.util.Log.d("BookingActivity", "  Payment Status: " + booking.getPaymentStatus());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Booking Details");

        // Build classes information
        String classesInfo;
        List<String> allClasses = booking.getAllClassNames();
        if (allClasses != null && !allClasses.isEmpty()) {
            if (allClasses.size() == 1) {
                classesInfo = allClasses.get(0);
            } else {
                classesInfo = "Multiple Classes (" + allClasses.size() + "):\n";
                for (int i = 0; i < allClasses.size(); i++) {
                    classesInfo += "  " + (i + 1) + ". " + allClasses.get(i) + "\n";
                }
                classesInfo = classesInfo.trim(); // Remove trailing newline
            }
        } else {
            classesInfo = (booking.getClassName() != null && !booking.getClassName().trim().isEmpty()) ? 
                booking.getClassName() : "Not provided";
        }

        String details = String.format(
            "Booking ID: %s\n" +
            "Customer: %s\n" +
            "Email: %s\n" +
            "Phone: %s\n" +
            "Classes: %s\n" +
            "Date: %s\n" +
            "Time: %s\n" +
            "Status: %s\n" +
            "Payment: %s (%.2f)\n" +
            "Notes: %s",
            (booking.getBookingId() != null && !booking.getBookingId().trim().isEmpty()) ? booking.getBookingId() : "Not provided",
            (booking.getCustomerName() != null && !booking.getCustomerName().trim().isEmpty()) ? booking.getCustomerName() : "Not provided",
            (booking.getCustomerEmail() != null && !booking.getCustomerEmail().trim().isEmpty()) ? booking.getCustomerEmail() : "Not provided",
            (booking.getCustomerPhone() != null && !booking.getCustomerPhone().trim().isEmpty()) ? booking.getCustomerPhone() : "Not provided",
            classesInfo,
            (booking.getBookingDate() != null && !booking.getBookingDate().trim().isEmpty()) ? booking.getBookingDate() : "Not provided",
            (booking.getBookingTime() != null && !booking.getBookingTime().trim().isEmpty()) ? booking.getBookingTime() : "Not provided",
            (booking.getStatus() != null && !booking.getStatus().trim().isEmpty()) ? booking.getStatus() : "Not provided",
            (booking.getPaymentStatus() != null && !booking.getPaymentStatus().trim().isEmpty()) ? booking.getPaymentStatus() : "Not provided",
            booking.getPaymentAmount(),
            (booking.getNotes() != null && !booking.getNotes().trim().isEmpty()) ? booking.getNotes() : "None"
        );

        builder.setMessage(details);

        // Add action buttons for status updates
        builder.setPositiveButton("Update Status", (dialog, which) -> {
            showStatusUpdateDialog(booking);
        });

        builder.setNeutralButton("Payment Status", (dialog, which) -> {
            showPaymentStatusDialog(booking);
        });

        builder.setNegativeButton("Close", null);

        builder.show();
    }

    private void showStatusUpdateDialog(Booking booking) {
        String[] statusOptions = {"confirmed", "pending", "cancelled", "completed"};
        int currentStatusIndex = 0;

        // Find current status index
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equalsIgnoreCase(booking.getStatus())) {
                currentStatusIndex = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Booking Status");
        builder.setSingleChoiceItems(statusOptions, currentStatusIndex, null);

        builder.setPositiveButton("Update", (dialog, which) -> {
            int selectedIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
            if (selectedIndex >= 0) {
                String newStatus = statusOptions[selectedIndex];
                updateBookingStatus(booking, newStatus);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showPaymentStatusDialog(Booking booking) {
        String[] paymentOptions = {"paid", "pending", "refunded", "failed"};
        int currentPaymentIndex = 0;

        // Find current payment status index
        for (int i = 0; i < paymentOptions.length; i++) {
            if (paymentOptions[i].equalsIgnoreCase(booking.getPaymentStatus())) {
                currentPaymentIndex = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Payment Status");
        builder.setSingleChoiceItems(paymentOptions, currentPaymentIndex, null);

        builder.setPositiveButton("Update", (dialog, which) -> {
            int selectedIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
            if (selectedIndex >= 0) {
                String newPaymentStatus = paymentOptions[selectedIndex];
                updateBookingPaymentStatus(booking, newPaymentStatus);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateBookingStatus(Booking booking, String newStatus) {
        // Update local database
        int result = databaseHelper.updateBookingStatus(booking.getBookingId(), newStatus);
        
        if (result > 0) {
            // Update in Firebase
            cloudSyncService.updateBookingStatusInFirebase(booking.getBookingId(), newStatus, 
                new CloudSyncService.SyncCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(BookingActivity.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                            loadBookings(); // Refresh the list
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(BookingActivity.this, "Failed to sync status: " + error, Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onProgress(String progress) {
                        // Handle progress if needed
                    }
                });
        } else {
            Toast.makeText(this, "Failed to update booking status", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBookingPaymentStatus(Booking booking, String newPaymentStatus) {
        // Update local database
        int result = databaseHelper.updateBookingPaymentStatus(booking.getBookingId(), newPaymentStatus);
        
        if (result > 0) {
            Toast.makeText(this, "Payment status updated", Toast.LENGTH_SHORT).show();
            loadBookings(); // Refresh the list
            
            // Note: You might want to sync payment status to Firebase too
            // Similar to updateBookingStatus method
        } else {
            Toast.makeText(this, "Failed to update payment status", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cloudSyncService != null) {
            cloudSyncService.shutdown();
        }
    }
}
