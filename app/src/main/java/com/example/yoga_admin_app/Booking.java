package com.example.yoga_admin_app;

import java.util.ArrayList;
import java.util.List;

public class Booking {
    private long id; // Auto-generated table ID
    private String bookingId; // Unique booking identifier
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String classInstanceId;
    private String className; // Primary class name (for backwards compatibility)
    private List<String> allClassNames; // List of all booked classes
    private String bookingDate; // Date of the booking
    private String bookingTime; // Time of the booking
    private String status; // "confirmed", "pending", "cancelled", "completed"
    private String paymentStatus; // "paid", "pending", "refunded", "failed"
    private double paymentAmount;
    private String paymentMethod;
    private String notes;
    private String createdAt;
    private String updatedAt;
    
    // Sync fields
    private boolean synced;
    private long lastModified;

    // Constructor
    public Booking() {
        this.allClassNames = new ArrayList<>();
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getClassInstanceId() { return classInstanceId; }
    public void setClassInstanceId(String classInstanceId) { this.classInstanceId = classInstanceId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public List<String> getAllClassNames() { 
        return allClassNames != null ? allClassNames : new ArrayList<>(); 
    }
    
    public void setAllClassNames(List<String> allClassNames) { 
        this.allClassNames = allClassNames != null ? allClassNames : new ArrayList<>(); 
    }
    
    public void addClassName(String className) {
        if (allClassNames == null) {
            allClassNames = new ArrayList<>();
        }
        if (className != null && !className.trim().isEmpty() && !allClassNames.contains(className)) {
            allClassNames.add(className);
        }
    }
    
    public String getFormattedClassNames() {
        if (allClassNames == null || allClassNames.isEmpty()) {
            return className != null ? className : "No classes";
        }
        if (allClassNames.size() == 1) {
            return allClassNames.get(0);
        }
        return String.join(", ", allClassNames);
    }
    
    public int getClassCount() {
        return allClassNames != null ? allClassNames.size() : (className != null && !className.trim().isEmpty() ? 1 : 0);
    }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingTime() { return bookingTime; }
    public void setBookingTime(String bookingTime) { this.bookingTime = bookingTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public double getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(double paymentAmount) { this.paymentAmount = paymentAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", bookingId='" + bookingId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", className='" + className + '\'' +
                ", bookingDate='" + bookingDate + '\'' +
                ", bookingTime='" + bookingTime + '\'' +
                ", status='" + status + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", paymentAmount=" + paymentAmount +
                '}';
    }
}
