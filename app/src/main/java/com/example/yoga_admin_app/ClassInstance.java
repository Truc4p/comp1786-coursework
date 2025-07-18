package com.example.yoga_admin_app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ClassInstance {
    private long id;
    private long yogaClassId; // Foreign key to YogaClass
    private String date; // Format: dd/MM/yyyy
    private String instructor; // Required field
    private String additionalComments; // Optional field

    // Constructor
    public ClassInstance() {}

    public ClassInstance(long yogaClassId, String date, String instructor, String additionalComments) {
        this.yogaClassId = yogaClassId;
        this.date = date;
        this.instructor = instructor;
        this.additionalComments = additionalComments;
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getYogaClassId() { return yogaClassId; }
    public void setYogaClassId(long yogaClassId) { this.yogaClassId = yogaClassId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public String getAdditionalComments() { return additionalComments; }
    public void setAdditionalComments(String additionalComments) { this.additionalComments = additionalComments; }

    // Helper method to validate if the date matches the day of week of the associated yoga class
    public boolean isDateMatchingDayOfWeek(String dayOfWeek) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date parsedDate = dateFormat.parse(date);
            
            // Using Calendar to get day of week for consistency with ManageClassInstancesActivity
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);
            
            int dayOfWeekValue = calendar.get(Calendar.DAY_OF_WEEK);
            String dayOfWeekFromDate;
            
            // Note: Calendar.DAY_OF_WEEK uses 1 (Sunday) through 7 (Saturday)
            switch (dayOfWeekValue) {
                case Calendar.SUNDAY: dayOfWeekFromDate = "Sunday"; break;    // Calendar.SUNDAY = 1
                case Calendar.MONDAY: dayOfWeekFromDate = "Monday"; break;    // Calendar.MONDAY = 2
                case Calendar.TUESDAY: dayOfWeekFromDate = "Tuesday"; break;   // Calendar.TUESDAY = 3
                case Calendar.WEDNESDAY: dayOfWeekFromDate = "Wednesday"; break; // Calendar.WEDNESDAY = 4
                case Calendar.THURSDAY: dayOfWeekFromDate = "Thursday"; break;  // Calendar.THURSDAY = 5
                case Calendar.FRIDAY: dayOfWeekFromDate = "Friday"; break;    // Calendar.FRIDAY = 6
                case Calendar.SATURDAY: dayOfWeekFromDate = "Saturday"; break;  // Calendar.SATURDAY = 7
                default: dayOfWeekFromDate = "";
            }
            
            return dayOfWeekFromDate.equalsIgnoreCase(dayOfWeek);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return date + " - " + instructor;
    }
}