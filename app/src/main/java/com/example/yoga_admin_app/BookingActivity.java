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
        bookingList = databaseHelper.getAllBookings();
        filteredBookingList = new ArrayList<>(bookingList);

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
            boolean matchesSearch = searchText.isEmpty() || 
                booking.getCustomerName().toLowerCase().contains(searchText) ||
                booking.getCustomerEmail().toLowerCase().contains(searchText) ||
                booking.getClassName().toLowerCase().contains(searchText) ||
                booking.getBookingDate().toLowerCase().contains(searchText);

            boolean matchesStatus = selectedStatus.equals("All Bookings") || 
                booking.getStatus().equalsIgnoreCase(selectedStatus);

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
                    Toast.makeText(BookingActivity.this, message, Toast.LENGTH_LONG).show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Booking Details");

        String details = String.format(
            "Customer: %s\n" +
            "Email: %s\n" +
            "Phone: %s\n" +
            "Class: %s\n" +
            "Date: %s\n" +
            "Time: %s\n" +
            "Status: %s\n" +
            "Payment: %s (%.2f)\n" +
            "Notes: %s",
            booking.getCustomerName(),
            booking.getCustomerEmail(),
            booking.getCustomerPhone(),
            booking.getClassName(),
            booking.getBookingDate(),
            booking.getBookingTime(),
            booking.getStatus(),
            booking.getPaymentStatus(),
            booking.getPaymentAmount(),
            booking.getNotes().isEmpty() ? "None" : booking.getNotes()
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
