package com.example.yoga_admin_app;

public class YogaClass {
    private long id;
    private String dayOfWeek;
    private String time;
    private int capacity;
    private int duration;
    private double price;
    private String classType;
    private String description;
    private String instructor; // Additional field
    private String difficulty; // Additional field (Beginner, Intermediate, Advanced)
    
    // Location fields
    private double latitude;
    private double longitude;
    private String locationAddress; // Human-readable address
    
    // Search context fields - to show additional info when displaying search results
    private String searchInstructor; // Instructor name from class instances for search results
    private String searchDate; // Specific date from class instances for search results

    // Constructor
    public YogaClass() {}

    public YogaClass(String dayOfWeek, String time, int capacity, int duration, 
                     double price, String classType, String description, 
                     String instructor, String difficulty) {
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.capacity = capacity;
        this.duration = duration;
        this.price = price;
        this.classType = classType;
        this.description = description;
        this.instructor = instructor;
        this.difficulty = difficulty;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.locationAddress = "";
    }

    // Full constructor with location
    public YogaClass(String dayOfWeek, String time, int capacity, int duration, 
                     double price, String classType, String description, 
                     String instructor, String difficulty, double latitude, 
                     double longitude, String locationAddress) {
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.capacity = capacity;
        this.duration = duration;
        this.price = price;
        this.classType = classType;
        this.description = description;
        this.instructor = instructor;
        this.difficulty = difficulty;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationAddress = locationAddress;
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }

    public String getSearchInstructor() { return searchInstructor; }
    public void setSearchInstructor(String searchInstructor) { this.searchInstructor = searchInstructor; }

    public String getSearchDate() { return searchDate; }
    public void setSearchDate(String searchDate) { this.searchDate = searchDate; }

    @Override
    public String toString() {
        return classType + " - " + dayOfWeek + " at " + time;
    }
} 