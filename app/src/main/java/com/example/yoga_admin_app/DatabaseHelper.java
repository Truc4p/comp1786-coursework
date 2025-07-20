package com.example.yoga_admin_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "yoga_admin.db";
    private static final int DATABASE_VERSION = 7; // Added location fields
    
    // Table names
    private static final String TABLE_YOGA_CLASSES = "yoga_classes";
    private static final String TABLE_CLASS_INSTANCES = "class_instances";
    
    // Column names for yoga_classes table
    private static final String KEY_ID = "id";
    private static final String KEY_DAY_OF_WEEK = "day_of_week";
    private static final String KEY_TIME = "time";
    private static final String KEY_CAPACITY = "capacity";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_PRICE = "price";
    private static final String KEY_CLASS_TYPE = "class_type";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_DESCRIPTION = "description";
    
    // Location columns
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LOCATION_ADDRESS = "location_address";

    
    // Column names for class_instances table
    private static final String KEY_INSTANCE_ID = "id";
    private static final String KEY_YOGA_CLASS_ID = "yoga_class_id";
    private static final String KEY_DATE = "date";
    private static final String KEY_INSTANCE_INSTRUCTOR = "instructor";
    private static final String KEY_ADDITIONAL_COMMENTS = "additional_comments";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraints
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON");
        
        // Create yoga classes table
        String CREATE_YOGA_CLASSES_TABLE = "CREATE TABLE " + TABLE_YOGA_CLASSES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DAY_OF_WEEK + " TEXT NOT NULL,"
                + KEY_TIME + " TEXT NOT NULL,"
                + KEY_CAPACITY + " INTEGER NOT NULL,"
                + KEY_DURATION + " INTEGER NOT NULL,"
                + KEY_PRICE + " REAL NOT NULL,"
                + KEY_CLASS_TYPE + " TEXT NOT NULL,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DIFFICULTY + " TEXT,"
                + KEY_LATITUDE + " REAL DEFAULT 0.0,"
                + KEY_LONGITUDE + " REAL DEFAULT 0.0,"
                + KEY_LOCATION_ADDRESS + " TEXT" + ")";
        db.execSQL(CREATE_YOGA_CLASSES_TABLE);
        
        // Create class instances table
        String CREATE_CLASS_INSTANCES_TABLE = "CREATE TABLE " + TABLE_CLASS_INSTANCES + "("
                + KEY_INSTANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_YOGA_CLASS_ID + " INTEGER NOT NULL,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_INSTANCE_INSTRUCTOR + " TEXT NOT NULL,"
                + KEY_ADDITIONAL_COMMENTS + " TEXT,"
                + "FOREIGN KEY(" + KEY_YOGA_CLASS_ID + ") REFERENCES " + TABLE_YOGA_CLASSES + "(" + KEY_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_CLASS_INSTANCES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON");
        
        // For any version before 7, do a complete recreation to add location fields
        if (oldVersion < 7) {
            // Drop all tables and recreate from scratch
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_CLASSES);
            
            // Recreate tables with correct schema including location fields
            String CREATE_YOGA_CLASSES_TABLE = "CREATE TABLE " + TABLE_YOGA_CLASSES + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_DAY_OF_WEEK + " TEXT NOT NULL,"
                    + KEY_TIME + " TEXT NOT NULL,"
                    + KEY_CAPACITY + " INTEGER NOT NULL,"
                    + KEY_DURATION + " INTEGER NOT NULL,"
                    + KEY_PRICE + " REAL NOT NULL,"
                    + KEY_CLASS_TYPE + " TEXT NOT NULL,"
                    + KEY_DESCRIPTION + " TEXT,"
                    + KEY_DIFFICULTY + " TEXT,"
                    + KEY_LATITUDE + " REAL DEFAULT 0.0,"
                    + KEY_LONGITUDE + " REAL DEFAULT 0.0,"
                    + KEY_LOCATION_ADDRESS + " TEXT" + ")";
            db.execSQL(CREATE_YOGA_CLASSES_TABLE);
            
            String CREATE_CLASS_INSTANCES_TABLE = "CREATE TABLE " + TABLE_CLASS_INSTANCES + "("
                    + KEY_INSTANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_YOGA_CLASS_ID + " INTEGER NOT NULL,"
                    + KEY_DATE + " TEXT NOT NULL,"
                    + KEY_INSTANCE_INSTRUCTOR + " TEXT NOT NULL,"
                    + KEY_ADDITIONAL_COMMENTS + " TEXT,"
                    + "FOREIGN KEY(" + KEY_YOGA_CLASS_ID + ") REFERENCES " + TABLE_YOGA_CLASSES + "(" + KEY_ID + ") ON DELETE CASCADE)";
            db.execSQL(CREATE_CLASS_INSTANCES_TABLE);
        }
        
        // Future upgrade logic can be added here
        // if (oldVersion < 7) {
        //     // Add future schema changes here
        // }
    }

    // Add a new yoga class
    public long addYogaClass(YogaClass yogaClass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_DAY_OF_WEEK, yogaClass.getDayOfWeek());
        values.put(KEY_TIME, yogaClass.getTime());
        values.put(KEY_CAPACITY, yogaClass.getCapacity());
        values.put(KEY_DURATION, yogaClass.getDuration());
        values.put(KEY_PRICE, yogaClass.getPrice());
        values.put(KEY_CLASS_TYPE, yogaClass.getClassType());
        values.put(KEY_DESCRIPTION, yogaClass.getDescription());
        values.put(KEY_DIFFICULTY, yogaClass.getDifficulty());
        values.put(KEY_LATITUDE, yogaClass.getLatitude());
        values.put(KEY_LONGITUDE, yogaClass.getLongitude());
        values.put(KEY_LOCATION_ADDRESS, yogaClass.getLocationAddress());
        
        long id = db.insert(TABLE_YOGA_CLASSES, null, values);
        db.close();
        return id;
    }

    // Get all yoga classes
    public List<YogaClass> getAllYogaClasses() {
        List<YogaClass> yogaClassList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_YOGA_CLASSES + 
                " ORDER BY CASE " + KEY_DAY_OF_WEEK + 
                " WHEN 'Monday' THEN 1" +
                " WHEN 'Tuesday' THEN 2" +
                " WHEN 'Wednesday' THEN 3" +
                " WHEN 'Thursday' THEN 4" +
                " WHEN 'Friday' THEN 5" +
                " WHEN 'Saturday' THEN 6" +
                " WHEN 'Sunday' THEN 7" +
                " END, " +
                " CASE WHEN " + KEY_TIME + " LIKE '%AM' THEN " +
                "   CASE WHEN SUBSTR(" + KEY_TIME + ", 1, INSTR(" + KEY_TIME + ", ':') - 1) = '12' THEN " +
                "     '00' || SUBSTR(" + KEY_TIME + ", INSTR(" + KEY_TIME + ", ':'), LENGTH(" + KEY_TIME + ") - INSTR(" + KEY_TIME + ", ':') - 2)" +
                "   ELSE " +
                "     PRINTF('%02d', CAST(SUBSTR(" + KEY_TIME + ", 1, INSTR(" + KEY_TIME + ", ':') - 1) AS INTEGER)) || SUBSTR(" + KEY_TIME + ", INSTR(" + KEY_TIME + ", ':'), LENGTH(" + KEY_TIME + ") - INSTR(" + KEY_TIME + ", ':') - 2)" +
                "   END " +
                " ELSE " +
                "   CASE WHEN SUBSTR(" + KEY_TIME + ", 1, INSTR(" + KEY_TIME + ", ':') - 1) = '12' THEN " +
                "     '12' || SUBSTR(" + KEY_TIME + ", INSTR(" + KEY_TIME + ", ':'), LENGTH(" + KEY_TIME + ") - INSTR(" + KEY_TIME + ", ':') - 2)" +
                "   ELSE " +
                "     PRINTF('%02d', CAST(SUBSTR(" + KEY_TIME + ", 1, INSTR(" + KEY_TIME + ", ':') - 1) AS INTEGER) + 12) || SUBSTR(" + KEY_TIME + ", INSTR(" + KEY_TIME + ", ':'), LENGTH(" + KEY_TIME + ") - INSTR(" + KEY_TIME + ", ':') - 2)" +
                "   END " +
                " END";
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                YogaClass yogaClass = new YogaClass();
                yogaClass.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                yogaClass.setDayOfWeek(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DAY_OF_WEEK)));
                yogaClass.setTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)));
                yogaClass.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CAPACITY)));
                yogaClass.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DURATION)));
                yogaClass.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE)));
                yogaClass.setClassType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS_TYPE)));
                yogaClass.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
                yogaClass.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIFFICULTY)));
                yogaClass.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LATITUDE)));
                yogaClass.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LONGITUDE)));
                yogaClass.setLocationAddress(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION_ADDRESS)));
                
                yogaClassList.add(yogaClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return yogaClassList;
    }

    // Get a single yoga class
    public YogaClass getYogaClass(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_YOGA_CLASSES, null, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            YogaClass yogaClass = new YogaClass();
            yogaClass.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
            yogaClass.setDayOfWeek(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DAY_OF_WEEK)));
            yogaClass.setTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)));
            yogaClass.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CAPACITY)));
            yogaClass.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DURATION)));
            yogaClass.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE)));
            yogaClass.setClassType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS_TYPE)));
            yogaClass.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
            yogaClass.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIFFICULTY)));
            yogaClass.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LATITUDE)));
            yogaClass.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LONGITUDE)));
            yogaClass.setLocationAddress(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION_ADDRESS)));
            cursor.close();
            db.close();
            return yogaClass;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }

    // Update a yoga class
    public int updateYogaClass(YogaClass yogaClass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_DAY_OF_WEEK, yogaClass.getDayOfWeek());
        values.put(KEY_TIME, yogaClass.getTime());
        values.put(KEY_CAPACITY, yogaClass.getCapacity());
        values.put(KEY_DURATION, yogaClass.getDuration());
        values.put(KEY_PRICE, yogaClass.getPrice());
        values.put(KEY_CLASS_TYPE, yogaClass.getClassType());
        values.put(KEY_DESCRIPTION, yogaClass.getDescription());
        values.put(KEY_DIFFICULTY, yogaClass.getDifficulty());
        values.put(KEY_LATITUDE, yogaClass.getLatitude());
        values.put(KEY_LONGITUDE, yogaClass.getLongitude());
        values.put(KEY_LOCATION_ADDRESS, yogaClass.getLocationAddress());
        
        int result = db.update(TABLE_YOGA_CLASSES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(yogaClass.getId())});
        db.close();
        return result;
    }

    // Delete a yoga class
    public void deleteYogaClass(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // First delete all associated class instances
        deleteAllInstancesForYogaClass(id);
        // Then delete the yoga class
        db.delete(TABLE_YOGA_CLASSES, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Reset database (delete all data)
    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CLASS_INSTANCES);
        db.execSQL("DELETE FROM " + TABLE_YOGA_CLASSES);
        db.close();
    }

    // Get count of yoga classes
    public int getYogaClassCount() {
        String countQuery = "SELECT * FROM " + TABLE_YOGA_CLASSES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
    
    // Class Instance CRUD Operations
    
    // Add a new class instance
    public long addClassInstance(ClassInstance classInstance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_YOGA_CLASS_ID, classInstance.getYogaClassId());
        values.put(KEY_DATE, classInstance.getDate());
        values.put(KEY_INSTANCE_INSTRUCTOR, classInstance.getInstructor());
        values.put(KEY_ADDITIONAL_COMMENTS, classInstance.getAdditionalComments());
        
        long id = db.insert(TABLE_CLASS_INSTANCES, null, values);
        db.close();
        return id;
    }
    
    // Get all class instances for a specific yoga class
    public List<ClassInstance> getClassInstancesByYogaClassId(long yogaClassId) {
        List<ClassInstance> instanceList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCES + 
                " WHERE " + KEY_YOGA_CLASS_ID + " = ? ORDER BY " + KEY_DATE;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(yogaClassId)});
        
        if (cursor.moveToFirst()) {
            do {
                ClassInstance instance = new ClassInstance();
                instance.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INSTANCE_ID)));
                instance.setYogaClassId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_YOGA_CLASS_ID)));
                instance.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)));
                instance.setInstructor(cursor.getString(cursor.getColumnIndexOrThrow(KEY_INSTANCE_INSTRUCTOR)));
                instance.setAdditionalComments(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADDITIONAL_COMMENTS)));
                
                instanceList.add(instance);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return instanceList;
    }
    
    // Get a single class instance
    public ClassInstance getClassInstance(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CLASS_INSTANCES, null, KEY_INSTANCE_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            ClassInstance instance = new ClassInstance();
            instance.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INSTANCE_ID)));
            instance.setYogaClassId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_YOGA_CLASS_ID)));
            instance.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)));
            instance.setInstructor(cursor.getString(cursor.getColumnIndexOrThrow(KEY_INSTANCE_INSTRUCTOR)));
            instance.setAdditionalComments(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADDITIONAL_COMMENTS)));
            cursor.close();
            db.close();
            return instance;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }
    
    // Update a class instance
    public int updateClassInstance(ClassInstance instance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_YOGA_CLASS_ID, instance.getYogaClassId());
        values.put(KEY_DATE, instance.getDate());
        values.put(KEY_INSTANCE_INSTRUCTOR, instance.getInstructor());
        values.put(KEY_ADDITIONAL_COMMENTS, instance.getAdditionalComments());
        
        int result = db.update(TABLE_CLASS_INSTANCES, values, KEY_INSTANCE_ID + " = ?",
                new String[]{String.valueOf(instance.getId())});
        db.close();
        return result;
    }
    
    // Delete a class instance
    public void deleteClassInstance(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CLASS_INSTANCES, KEY_INSTANCE_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
    
    // Delete all instances for a yoga class
    public void deleteAllInstancesForYogaClass(long yogaClassId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CLASS_INSTANCES, KEY_YOGA_CLASS_ID + " = ?", new String[]{String.valueOf(yogaClassId)});
        db.close();
    }
    
    // Get count of class instances for a yoga class
    public int getClassInstanceCount(long yogaClassId) {
        String countQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCES + 
                " WHERE " + KEY_YOGA_CLASS_ID + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(yogaClassId)});
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
    
    // Search Methods
    
    // Search yoga classes by instructor name (from class instances)
    public List<YogaClass> searchYogaClassesByInstructor(String instructorName) {
        List<YogaClass> yogaClassList = new ArrayList<>();
        String searchQuery = "SELECT DISTINCT yc.*, " +
                "COALESCE(ci." + KEY_INSTANCE_INSTRUCTOR + ", 'No Instructor Assigned') as instructor_name " +
                "FROM " + TABLE_YOGA_CLASSES + " yc " +
                "INNER JOIN " + TABLE_CLASS_INSTANCES + " ci ON yc." + KEY_ID + " = ci." + KEY_YOGA_CLASS_ID +
                " WHERE ci." + KEY_INSTANCE_INSTRUCTOR + " LIKE ? " +
                " ORDER BY CASE yc." + KEY_DAY_OF_WEEK + 
                " WHEN 'Monday' THEN 1" +
                " WHEN 'Tuesday' THEN 2" +
                " WHEN 'Wednesday' THEN 3" +
                " WHEN 'Thursday' THEN 4" +
                " WHEN 'Friday' THEN 5" +
                " WHEN 'Saturday' THEN 6" +
                " WHEN 'Sunday' THEN 7" +
                " END, " +
                " CASE WHEN yc." + KEY_TIME + " LIKE '%AM' THEN " +
                "   CASE WHEN SUBSTR(yc." + KEY_TIME + ", 1, INSTR(yc." + KEY_TIME + ", ':') - 1) = '12' THEN " +
                "     '00' || SUBSTR(yc." + KEY_TIME + ", INSTR(yc." + KEY_TIME + ", ':'), LENGTH(yc." + KEY_TIME + ") - INSTR(yc." + KEY_TIME + ", ':') - 2)" +
                "   ELSE " +
                "     PRINTF('%02d', CAST(SUBSTR(yc." + KEY_TIME + ", 1, INSTR(yc." + KEY_TIME + ", ':') - 1) AS INTEGER)) || SUBSTR(yc." + KEY_TIME + ", INSTR(yc." + KEY_TIME + ", ':'), LENGTH(yc." + KEY_TIME + ") - INSTR(yc." + KEY_TIME + ", ':') - 2)" +
                "   END " +
                " ELSE " +
                "   CASE WHEN SUBSTR(yc." + KEY_TIME + ", 1, INSTR(yc." + KEY_TIME + ", ':') - 1) = '12' THEN " +
                "     '12' || SUBSTR(yc." + KEY_TIME + ", INSTR(yc." + KEY_TIME + ", ':'), LENGTH(yc." + KEY_TIME + ") - INSTR(yc." + KEY_TIME + ", ':') - 2)" +
                "   ELSE " +
                "     PRINTF('%02d', CAST(SUBSTR(yc." + KEY_TIME + ", 1, INSTR(yc." + KEY_TIME + ", ':') - 1) AS INTEGER) + 12) || SUBSTR(yc." + KEY_TIME + ", INSTR(yc." + KEY_TIME + ", ':'), LENGTH(yc." + KEY_TIME + ") - INSTR(yc." + KEY_TIME + ", ':') - 2)" +
                "   END " +
                " END";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(searchQuery, new String[]{"%" + instructorName + "%"});
        
        if (cursor.moveToFirst()) {
            do {
                YogaClass yogaClass = new YogaClass();
                yogaClass.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                yogaClass.setDayOfWeek(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DAY_OF_WEEK)));
                yogaClass.setTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)));
                yogaClass.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CAPACITY)));
                yogaClass.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DURATION)));
                yogaClass.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE)));
                yogaClass.setClassType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS_TYPE)));
                yogaClass.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
                yogaClass.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIFFICULTY)));
                
                // Set search context - instructor name from the search
                yogaClass.setSearchInstructor(cursor.getString(cursor.getColumnIndexOrThrow("instructor_name")));
                
                yogaClassList.add(yogaClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return yogaClassList;
    }
    
    // Search yoga classes by day of the week
    public List<YogaClass> searchYogaClassesByDayOfWeek(String dayOfWeek) {
        List<YogaClass> yogaClassList = new ArrayList<>();
        String searchQuery = "SELECT * FROM " + TABLE_YOGA_CLASSES + 
                " WHERE " + KEY_DAY_OF_WEEK + " = ? " +
                "ORDER BY " + KEY_TIME;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(searchQuery, new String[]{dayOfWeek});
        
        if (cursor.moveToFirst()) {
            do {
                YogaClass yogaClass = new YogaClass();
                yogaClass.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                yogaClass.setDayOfWeek(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DAY_OF_WEEK)));
                yogaClass.setTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)));
                yogaClass.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CAPACITY)));
                yogaClass.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DURATION)));
                yogaClass.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE)));
                yogaClass.setClassType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS_TYPE)));
                yogaClass.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
                yogaClass.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIFFICULTY)));
                
                yogaClassList.add(yogaClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return yogaClassList;
    }
    
    // Search yoga classes by specific date (from class instances)
    public List<YogaClass> searchYogaClassesByDate(String date) {
        List<YogaClass> yogaClassList = new ArrayList<>();
        String searchQuery = "SELECT DISTINCT yc.*, ci." + KEY_DATE + " as search_date, " +
                "COALESCE(ci." + KEY_INSTANCE_INSTRUCTOR + ", 'No Instructor Assigned') as instructor_name " +
                "FROM " + TABLE_YOGA_CLASSES + " yc " +
                "INNER JOIN " + TABLE_CLASS_INSTANCES + " ci ON yc." + KEY_ID + " = ci." + KEY_YOGA_CLASS_ID +
                " WHERE ci." + KEY_DATE + " = ? " +
                "ORDER BY yc." + KEY_TIME;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(searchQuery, new String[]{date});
        
        if (cursor.moveToFirst()) {
            do {
                YogaClass yogaClass = new YogaClass();
                yogaClass.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                yogaClass.setDayOfWeek(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DAY_OF_WEEK)));
                yogaClass.setTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)));
                yogaClass.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CAPACITY)));
                yogaClass.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DURATION)));
                yogaClass.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE)));
                yogaClass.setClassType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS_TYPE)));
                yogaClass.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
                yogaClass.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIFFICULTY)));
                
                // Set search context - date and instructor from the search
                yogaClass.setSearchDate(cursor.getString(cursor.getColumnIndexOrThrow("search_date")));
                yogaClass.setSearchInstructor(cursor.getString(cursor.getColumnIndexOrThrow("instructor_name")));
                
                yogaClassList.add(yogaClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return yogaClassList;
    }
    
    // Combined search method for advanced search
    public List<YogaClass> searchYogaClasses(String instructorName, String dayOfWeek, String date) {
        List<YogaClass> yogaClassList = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        List<String> params = new ArrayList<>();
        
        // Build select clause to include search context fields
        queryBuilder.append("SELECT DISTINCT yc.*");
        
        boolean hasInstanceCriteria = (instructorName != null && !instructorName.trim().isEmpty()) || 
                                    (date != null && !date.trim().isEmpty());
        
        if (hasInstanceCriteria) {
            queryBuilder.append(", COALESCE(ci.").append(KEY_INSTANCE_INSTRUCTOR).append(", 'No Instructor Assigned') as instructor_name");
            queryBuilder.append(", ci.").append(KEY_DATE).append(" as search_date");
        }
        
        queryBuilder.append(" FROM ").append(TABLE_YOGA_CLASSES).append(" yc ");
        
        if (hasInstanceCriteria) {
            queryBuilder.append("INNER JOIN ").append(TABLE_CLASS_INSTANCES).append(" ci ON yc.")
                       .append(KEY_ID).append(" = ci.").append(KEY_YOGA_CLASS_ID).append(" ");
        }
        
        queryBuilder.append("WHERE 1=1 ");
        
        if (instructorName != null && !instructorName.trim().isEmpty()) {
            queryBuilder.append("AND ci.").append(KEY_INSTANCE_INSTRUCTOR).append(" LIKE ? ");
            params.add("%" + instructorName + "%");
        }
        
        if (dayOfWeek != null && !dayOfWeek.trim().isEmpty()) {
            queryBuilder.append("AND yc.").append(KEY_DAY_OF_WEEK).append(" = ? ");
            params.add(dayOfWeek);
        }
        
        if (date != null && !date.trim().isEmpty()) {
            queryBuilder.append("AND ci.").append(KEY_DATE).append(" = ? ");
            params.add(date);
        }
        
        queryBuilder.append(" ORDER BY CASE yc.").append(KEY_DAY_OF_WEEK)
                .append(" WHEN 'Monday' THEN 1")
                .append(" WHEN 'Tuesday' THEN 2")
                .append(" WHEN 'Wednesday' THEN 3")
                .append(" WHEN 'Thursday' THEN 4")
                .append(" WHEN 'Friday' THEN 5")
                .append(" WHEN 'Saturday' THEN 6")
                .append(" WHEN 'Sunday' THEN 7")
                .append(" END, ")
                .append(" CASE WHEN yc.").append(KEY_TIME).append(" LIKE '%AM' THEN ")
                .append("   CASE WHEN SUBSTR(yc.").append(KEY_TIME).append(", 1, INSTR(yc.").append(KEY_TIME).append(", ':') - 1) = '12' THEN ")
                .append("     '00' || SUBSTR(yc.").append(KEY_TIME).append(", INSTR(yc.").append(KEY_TIME).append(", ':'), LENGTH(yc.").append(KEY_TIME).append(") - INSTR(yc.").append(KEY_TIME).append(", ':') - 2)")
                .append("   ELSE ")
                .append("     PRINTF('%02d', CAST(SUBSTR(yc.").append(KEY_TIME).append(", 1, INSTR(yc.").append(KEY_TIME).append(", ':') - 1) AS INTEGER)) || SUBSTR(yc.").append(KEY_TIME).append(", INSTR(yc.").append(KEY_TIME).append(", ':'), LENGTH(yc.").append(KEY_TIME).append(") - INSTR(yc.").append(KEY_TIME).append(", ':') - 2)")
                .append("   END ")
                .append(" ELSE ")
                .append("   CASE WHEN SUBSTR(yc.").append(KEY_TIME).append(", 1, INSTR(yc.").append(KEY_TIME).append(", ':') - 1) = '12' THEN ")
                .append("     '12' || SUBSTR(yc.").append(KEY_TIME).append(", INSTR(yc.").append(KEY_TIME).append(", ':'), LENGTH(yc.").append(KEY_TIME).append(") - INSTR(yc.").append(KEY_TIME).append(", ':') - 2)")
                .append("   ELSE ")
                .append("     PRINTF('%02d', CAST(SUBSTR(yc.").append(KEY_TIME).append(", 1, INSTR(yc.").append(KEY_TIME).append(", ':') - 1) AS INTEGER) + 12) || SUBSTR(yc.").append(KEY_TIME).append(", INSTR(yc.").append(KEY_TIME).append(", ':'), LENGTH(yc.").append(KEY_TIME).append(") - INSTR(yc.").append(KEY_TIME).append(", ':') - 2)")
                .append("   END ")
                .append(" END");
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryBuilder.toString(), params.toArray(new String[0]));
        
        if (cursor.moveToFirst()) {
            do {
                YogaClass yogaClass = new YogaClass();
                yogaClass.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                yogaClass.setDayOfWeek(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DAY_OF_WEEK)));
                yogaClass.setTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)));
                yogaClass.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CAPACITY)));
                yogaClass.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DURATION)));
                yogaClass.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE)));
                yogaClass.setClassType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS_TYPE)));
                yogaClass.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
                yogaClass.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIFFICULTY)));
                
                // Set search context if available
                if (hasInstanceCriteria) {
                    try {
                        // Always get instructor name when we have instance criteria
                        String instructorFromDb = cursor.getString(cursor.getColumnIndexOrThrow("instructor_name"));
                        yogaClass.setSearchInstructor(instructorFromDb);
                        
                        // Set search date if we're searching by date
                        if (date != null && !date.trim().isEmpty()) {
                            yogaClass.setSearchDate(cursor.getString(cursor.getColumnIndexOrThrow("search_date")));
                        }
                    } catch (Exception e) {
                        // In case columns don't exist, just continue without search context
                    }
                }
                
                yogaClassList.add(yogaClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return yogaClassList;
    }
}