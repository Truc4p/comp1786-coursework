package com.example.yoga_admin_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "yoga_admin.db";
    private static final int DATABASE_VERSION = 15; // Added users table for authentication
    
    private Context context;
    private CloudSyncService cloudSyncService;
    
    // Table names
    private static final String TABLE_YOGA_CLASSES = "yoga_classes";
    private static final String TABLE_CLASS_INSTANCES = "class_instances";
    private static final String TABLE_PENDING_DELETIONS = "pending_deletions";
    private static final String TABLE_BOOKINGS = "bookings";
    private static final String TABLE_USERS = "users";
    
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
    
    // Sync columns for yoga_classes
    private static final String KEY_LAST_MODIFIED = "last_modified";
    private static final String KEY_NEEDS_SYNC = "needs_sync";
    private static final String KEY_CLOUD_ID = "cloud_id";

    
    // Column names for class_instances table
    private static final String KEY_INSTANCE_ID = "id";
    private static final String KEY_YOGA_CLASS_ID = "yoga_class_id";
    private static final String KEY_DATE = "date";
    private static final String KEY_INSTANCE_INSTRUCTOR = "instructor";
    private static final String KEY_ADDITIONAL_COMMENTS = "additional_comments";
    
    // Sync columns for class_instances
    private static final String KEY_INSTANCE_LAST_MODIFIED = "last_modified";
    private static final String KEY_INSTANCE_NEEDS_SYNC = "needs_sync";
    private static final String KEY_INSTANCE_CLOUD_ID = "cloud_id";
    
    // Column names for pending_deletions table
    private static final String KEY_DELETION_ID = "id";
    private static final String KEY_DELETION_ITEM_TYPE = "item_type"; // "class" or "instance"
    private static final String KEY_DELETION_ITEM_ID = "item_id";
    private static final String KEY_DELETION_CLOUD_ID = "cloud_id";
    private static final String KEY_DELETION_TIMESTAMP = "deletion_timestamp";
    
    // Column names for bookings table
    private static final String COLUMN_BOOKING_TABLE_ID = "id";
    private static final String COLUMN_BOOKING_ID = "booking_id";
    private static final String COLUMN_BOOKING_CUSTOMER_NAME = "customer_name";
    private static final String COLUMN_BOOKING_CUSTOMER_EMAIL = "customer_email";
    private static final String COLUMN_BOOKING_CUSTOMER_PHONE = "customer_phone";
    private static final String COLUMN_BOOKING_CLASS_INSTANCE_ID = "class_instance_id";
    private static final String COLUMN_BOOKING_CLASS_NAME = "class_name";
    private static final String COLUMN_BOOKING_ALL_CLASSES = "all_classes"; // JSON string of all class names
    private static final String COLUMN_BOOKING_DATE = "booking_date";
    private static final String COLUMN_BOOKING_TIME = "booking_time";
    private static final String COLUMN_BOOKING_STATUS = "status";
    private static final String COLUMN_BOOKING_PAYMENT_STATUS = "payment_status";
    private static final String COLUMN_BOOKING_PAYMENT_AMOUNT = "payment_amount";
    private static final String COLUMN_BOOKING_PAYMENT_METHOD = "payment_method";
    private static final String COLUMN_BOOKING_NOTES = "notes";
    private static final String COLUMN_BOOKING_CREATED_AT = "created_at";
    private static final String COLUMN_BOOKING_UPDATED_AT = "updated_at";
    private static final String COLUMN_BOOKING_IS_SYNCED = "is_synced";
    private static final String COLUMN_BOOKING_LAST_MODIFIED = "last_modified";
    
    // Column names for users table
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD_HASH = "password_hash";
    private static final String COLUMN_SALT = "salt";
    private static final String COLUMN_IS_ACTIVE = "is_active";
    private static final String COLUMN_FAILED_ATTEMPTS = "failed_attempts";
    private static final String COLUMN_LOCKOUT_UNTIL = "lockout_until";
    private static final String COLUMN_LAST_LOGIN = "last_login";
    private static final String COLUMN_SESSION_TOKEN = "session_token";
    private static final String COLUMN_SESSION_EXPIRES_AT = "session_expires_at";
    private static final String COLUMN_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        // Don't create CloudSyncService here to avoid circular dependency
    }
    
    /**
     * Set the CloudSyncService instance for automatic syncing
     */
    public void setCloudSyncService(CloudSyncService cloudSyncService) {
        this.cloudSyncService = cloudSyncService;
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
                + KEY_LOCATION_ADDRESS + " TEXT,"
                + KEY_LAST_MODIFIED + " INTEGER DEFAULT 0,"
                + KEY_NEEDS_SYNC + " INTEGER DEFAULT 1,"
                + KEY_CLOUD_ID + " TEXT" + ")";
        db.execSQL(CREATE_YOGA_CLASSES_TABLE);
        
        // Create class instances table
        String CREATE_CLASS_INSTANCES_TABLE = "CREATE TABLE " + TABLE_CLASS_INSTANCES + "("
                + KEY_INSTANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_YOGA_CLASS_ID + " INTEGER NOT NULL,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_INSTANCE_INSTRUCTOR + " TEXT NOT NULL,"
                + KEY_ADDITIONAL_COMMENTS + " TEXT,"
                + KEY_INSTANCE_LAST_MODIFIED + " INTEGER DEFAULT 0,"
                + KEY_INSTANCE_NEEDS_SYNC + " INTEGER DEFAULT 1,"
                + KEY_INSTANCE_CLOUD_ID + " TEXT,"
                + "FOREIGN KEY(" + KEY_YOGA_CLASS_ID + ") REFERENCES " + TABLE_YOGA_CLASSES + "(" + KEY_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_CLASS_INSTANCES_TABLE);
        
        // Create pending deletions table
        String CREATE_PENDING_DELETIONS_TABLE = "CREATE TABLE " + TABLE_PENDING_DELETIONS + "("
                + KEY_DELETION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DELETION_ITEM_TYPE + " TEXT NOT NULL,"
                + KEY_DELETION_ITEM_ID + " INTEGER NOT NULL,"
                + KEY_DELETION_CLOUD_ID + " TEXT,"
                + KEY_DELETION_TIMESTAMP + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_PENDING_DELETIONS_TABLE);
        
        // Create bookings table
        String CREATE_BOOKINGS_TABLE = "CREATE TABLE " + TABLE_BOOKINGS + "("
                + COLUMN_BOOKING_TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BOOKING_ID + " TEXT NOT NULL UNIQUE,"
                + COLUMN_BOOKING_CUSTOMER_NAME + " TEXT NOT NULL,"
                + COLUMN_BOOKING_CUSTOMER_EMAIL + " TEXT NOT NULL,"
                + COLUMN_BOOKING_CUSTOMER_PHONE + " TEXT,"
                + COLUMN_BOOKING_CLASS_INSTANCE_ID + " TEXT NOT NULL,"
                + COLUMN_BOOKING_CLASS_NAME + " TEXT NOT NULL,"
                + COLUMN_BOOKING_ALL_CLASSES + " TEXT," // JSON string of all class names
                + COLUMN_BOOKING_DATE + " TEXT NOT NULL,"
                + COLUMN_BOOKING_TIME + " TEXT NOT NULL,"
                + COLUMN_BOOKING_STATUS + " TEXT DEFAULT 'confirmed',"
                + COLUMN_BOOKING_PAYMENT_STATUS + " TEXT DEFAULT 'pending',"
                + COLUMN_BOOKING_PAYMENT_AMOUNT + " REAL DEFAULT 0.0,"
                + COLUMN_BOOKING_PAYMENT_METHOD + " TEXT,"
                + COLUMN_BOOKING_NOTES + " TEXT,"
                + COLUMN_BOOKING_CREATED_AT + " TEXT,"
                + COLUMN_BOOKING_UPDATED_AT + " TEXT,"
                + COLUMN_BOOKING_IS_SYNCED + " INTEGER DEFAULT 0,"
                + COLUMN_BOOKING_LAST_MODIFIED + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_BOOKINGS_TABLE);
        
        // Create users table for authentication
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT NOT NULL UNIQUE,"
                + COLUMN_EMAIL + " TEXT NOT NULL UNIQUE,"
                + COLUMN_PASSWORD_HASH + " TEXT NOT NULL,"
                + COLUMN_SALT + " TEXT NOT NULL,"
                + COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1,"
                + COLUMN_FAILED_ATTEMPTS + " INTEGER DEFAULT 0,"
                + COLUMN_LOCKOUT_UNTIL + " INTEGER DEFAULT 0,"
                + COLUMN_LAST_LOGIN + " INTEGER DEFAULT 0,"
                + COLUMN_SESSION_TOKEN + " TEXT,"
                + COLUMN_SESSION_EXPIRES_AT + " INTEGER DEFAULT 0,"
                + COLUMN_CREATED_AT + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_USERS_TABLE);
        
        // Create default admin user
        createDefaultAdminUser(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Fresh start approach - drop all existing tables and recreate
        // This ensures a clean, consistent database schema for all users
        // Note: This will cause data loss for existing users
        
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON");
        
        // Drop all existing tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_CLASSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PENDING_DELETIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        
        // Recreate all tables with the current schema
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle downgrade by recreating the database
        // This is typically used during development when you need to go back to an earlier version
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_CLASSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PENDING_DELETIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        onCreate(db);
    }

    // Add a new yoga class
    public long addYogaClass(YogaClass yogaClass) {
        return addYogaClass(yogaClass, null);
    }
    
    // Add a new yoga class with sync callback
    public long addYogaClass(YogaClass yogaClass, CloudSyncService.SyncCallback syncCallback) {
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
        values.put(KEY_LAST_MODIFIED, System.currentTimeMillis());
        values.put(KEY_NEEDS_SYNC, 1);
        values.put(KEY_CLOUD_ID, yogaClass.getCloudId());
        
        long id = db.insert(TABLE_YOGA_CLASSES, null, values);
        db.close();
        
        // Auto-sync to Firebase if insert was successful
        if (id != -1 && cloudSyncService != null) {
            yogaClass.setId(id);
            cloudSyncService.autoSyncYogaClass(yogaClass, syncCallback);
        } else if (id != -1 && cloudSyncService == null && syncCallback != null) {
            syncCallback.onError("Sync service not available");
        }
        
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
                yogaClass.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_LAST_MODIFIED)));
                yogaClass.setNeedsSync(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NEEDS_SYNC)) == 1);
                yogaClass.setCloudId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLOUD_ID)));
                
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
            yogaClass.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_LAST_MODIFIED)));
            yogaClass.setNeedsSync(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NEEDS_SYNC)) == 1);
            yogaClass.setCloudId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLOUD_ID)));
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
        values.put(KEY_LAST_MODIFIED, System.currentTimeMillis());
        values.put(KEY_NEEDS_SYNC, 1);
        values.put(KEY_CLOUD_ID, yogaClass.getCloudId());
        
        int result = db.update(TABLE_YOGA_CLASSES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(yogaClass.getId())});
        db.close();
        
        // Auto-sync to Firebase if update was successful
        if (result > 0 && cloudSyncService != null) {
            cloudSyncService.autoSyncYogaClass(yogaClass);
        }
        
        return result;
    }
    
    // Mark a yoga class as synced (set needsSync to false)
    public void markYogaClassAsSynced(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NEEDS_SYNC, 0);
        
        db.update(TABLE_YOGA_CLASSES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    // Delete a yoga class
    public void deleteYogaClass(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // First, get the class info to check if it has a cloud ID (using same connection)
            String cloudId = null;
            Cursor cursor = db.query(TABLE_YOGA_CLASSES, new String[]{KEY_CLOUD_ID}, 
                                   KEY_ID + "=?", new String[]{String.valueOf(id)}, 
                                   null, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                cloudId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLOUD_ID));
                cursor.close();
            }
            
            // Record the deletion for cloud sync if it has a cloud ID
            if (cloudId != null && !cloudId.isEmpty()) {
                ContentValues deletionValues = new ContentValues();
                deletionValues.put(KEY_DELETION_ITEM_TYPE, "class");
                deletionValues.put(KEY_DELETION_ITEM_ID, id);
                deletionValues.put(KEY_DELETION_CLOUD_ID, cloudId);
                deletionValues.put(KEY_DELETION_TIMESTAMP, System.currentTimeMillis());
                db.insert(TABLE_PENDING_DELETIONS, null, deletionValues);
            }
            
            // Delete associated instances using the same connection
            db.delete(TABLE_CLASS_INSTANCES, KEY_YOGA_CLASS_ID + " = ?", new String[]{String.valueOf(id)});
            // Delete the yoga class
            db.delete(TABLE_YOGA_CLASSES, KEY_ID + " = ?", new String[]{String.valueOf(id)});
            
            // Auto-delete from Firebase
            if (cloudSyncService != null) {
                cloudSyncService.autoDeleteYogaClass(id);
            }
            
        } finally {
            db.close();
        }
    }

    // Reset database (delete all data)
    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + TABLE_CLASS_INSTANCES);
            db.execSQL("DELETE FROM " + TABLE_YOGA_CLASSES);
        } finally {
            db.close();
        }
    }

    // Get count of yoga classes
    public int getYogaClassCount() {
        String countQuery = "SELECT * FROM " + TABLE_YOGA_CLASSES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        // Don't close db here - let SQLiteOpenHelper manage it
        return count;
    }
    
    // Class Instance CRUD Operations
    
    // Add a new class instance
    public long addClassInstance(ClassInstance classInstance) {
        return addClassInstance(classInstance, null);
    }
    
    // Add a new class instance with sync callback
    public long addClassInstance(ClassInstance classInstance, CloudSyncService.SyncCallback syncCallback) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Check for duplicates first - prevent instances with same class, date, and instructor
        String checkQuery = "SELECT COUNT(*) FROM " + TABLE_CLASS_INSTANCES + 
                           " WHERE " + KEY_YOGA_CLASS_ID + " = ? AND " + 
                           KEY_DATE + " = ? AND " + 
                           KEY_INSTANCE_INSTRUCTOR + " = ?";
        
        Cursor cursor = db.rawQuery(checkQuery, new String[]{
            String.valueOf(classInstance.getYogaClassId()),
            classInstance.getDate(),
            classInstance.getInstructor()
        });
        
        int existingCount = 0;
        if (cursor.moveToFirst()) {
            existingCount = cursor.getInt(0);
        }
        cursor.close();
        
        if (existingCount > 0) {
            Log.w("DatabaseHelper", "🚫 Duplicate instance prevented: Class=" + classInstance.getYogaClassId() + 
                  ", Date=" + classInstance.getDate() + ", Instructor=" + classInstance.getInstructor());
            db.close();
            if (syncCallback != null) {
                syncCallback.onError("Instance already exists for this date and instructor");
            }
            return -1; // Return -1 to indicate duplicate prevention
        }
        
        ContentValues values = new ContentValues();
        values.put(KEY_YOGA_CLASS_ID, classInstance.getYogaClassId());
        values.put(KEY_DATE, classInstance.getDate());
        values.put(KEY_INSTANCE_INSTRUCTOR, classInstance.getInstructor());
        values.put(KEY_ADDITIONAL_COMMENTS, classInstance.getAdditionalComments());
        values.put(KEY_INSTANCE_LAST_MODIFIED, System.currentTimeMillis());
        values.put(KEY_INSTANCE_NEEDS_SYNC, 1);
        values.put(KEY_INSTANCE_CLOUD_ID, classInstance.getCloudId());
        
        long id = db.insert(TABLE_CLASS_INSTANCES, null, values);
        Log.d("DatabaseHelper", "✅ New instance created: ID=" + id + ", Class=" + classInstance.getYogaClassId() + 
              ", Date=" + classInstance.getDate() + ", Instructor=" + classInstance.getInstructor());
        db.close();
        
        // Auto-sync to Firebase if insert was successful
        if (id != -1 && cloudSyncService != null) {
            classInstance.setId(id);
            Log.d("DatabaseHelper", "🚀 Triggering auto-sync for new class instance: ID=" + id + ", needsSync=" + classInstance.needsSync());
            cloudSyncService.autoSyncClassInstance(classInstance, syncCallback);
        } else if (id != -1 && cloudSyncService == null) {
            Log.w("DatabaseHelper", "⚠️ CloudSyncService is null, cannot auto-sync instance " + id);
            if (syncCallback != null) {
                syncCallback.onError("Sync service not available");
            }
        }
        
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
                instance.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INSTANCE_LAST_MODIFIED)));
                instance.setNeedsSync(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_INSTANCE_NEEDS_SYNC)) == 1);
                instance.setCloudId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_INSTANCE_CLOUD_ID)));
                
                instanceList.add(instance);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return instanceList;
    }
    
    // Get all class instances
    public List<ClassInstance> getAllClassInstances() {
        List<ClassInstance> instanceList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCES + " ORDER BY " + KEY_DATE;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                ClassInstance instance = new ClassInstance();
                instance.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INSTANCE_ID)));
                instance.setYogaClassId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_YOGA_CLASS_ID)));
                instance.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)));
                instance.setInstructor(cursor.getString(cursor.getColumnIndexOrThrow(KEY_INSTANCE_INSTRUCTOR)));
                instance.setAdditionalComments(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADDITIONAL_COMMENTS)));
                instance.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INSTANCE_LAST_MODIFIED)));
                instance.setNeedsSync(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_INSTANCE_NEEDS_SYNC)) == 1);
                instance.setCloudId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_INSTANCE_CLOUD_ID)));
                
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
            instance.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INSTANCE_LAST_MODIFIED)));
            instance.setNeedsSync(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_INSTANCE_NEEDS_SYNC)) == 1);
            instance.setCloudId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_INSTANCE_CLOUD_ID)));
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
        return updateClassInstance(instance, null);
    }
    
    // Update a class instance with sync callback
    public int updateClassInstance(ClassInstance instance, CloudSyncService.SyncCallback syncCallback) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_YOGA_CLASS_ID, instance.getYogaClassId());
        values.put(KEY_DATE, instance.getDate());
        values.put(KEY_INSTANCE_INSTRUCTOR, instance.getInstructor());
        values.put(KEY_ADDITIONAL_COMMENTS, instance.getAdditionalComments());
        values.put(KEY_INSTANCE_LAST_MODIFIED, System.currentTimeMillis());
        values.put(KEY_INSTANCE_NEEDS_SYNC, 1);
        values.put(KEY_INSTANCE_CLOUD_ID, instance.getCloudId());
        
        int result = db.update(TABLE_CLASS_INSTANCES, values, KEY_INSTANCE_ID + " = ?",
                new String[]{String.valueOf(instance.getId())});
        db.close();
        
        // Auto-sync to Firebase if update was successful
        if (result > 0 && cloudSyncService != null) {
            cloudSyncService.autoSyncClassInstance(instance, syncCallback);
        } else if (result > 0 && cloudSyncService == null && syncCallback != null) {
            syncCallback.onError("Sync service not available");
        }
        
        return result;
    }
    
    // Mark a class instance as synced (set needsSync to false)
    public void markClassInstanceAsSynced(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_INSTANCE_NEEDS_SYNC, 0);
        
        db.update(TABLE_CLASS_INSTANCES, values, KEY_INSTANCE_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }
    
    // Delete a class instance
    public void deleteClassInstance(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_CLASS_INSTANCES, KEY_INSTANCE_ID + " = ?", new String[]{String.valueOf(id)});
            
            // Auto-delete from Firebase
            if (cloudSyncService != null) {
                cloudSyncService.autoDeleteClassInstance(id);
            }
            
        } finally {
            db.close();
        }
    }
    
    // Delete all instances for a yoga class
    public void deleteAllInstancesForYogaClass(long yogaClassId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_CLASS_INSTANCES, KEY_YOGA_CLASS_ID + " = ?", new String[]{String.valueOf(yogaClassId)});
        } finally {
            db.close();
        }
    }
    
    // Delete all instances for a yoga class using existing database connection
    private void deleteAllInstancesForYogaClass(SQLiteDatabase db, long yogaClassId) {
        db.delete(TABLE_CLASS_INSTANCES, KEY_YOGA_CLASS_ID + " = ?", new String[]{String.valueOf(yogaClassId)});
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
    
    // SYNC-RELATED METHODS FOR IMPROVED CLOUD SYNC
    
    /**
     * Get all yoga classes that need syncing to cloud
     */
    public List<YogaClass> getClassesNeedingSync() {
        List<YogaClass> yogaClassList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_YOGA_CLASSES + 
                " WHERE " + KEY_NEEDS_SYNC + " = 1";
        
        SQLiteDatabase db = this.getReadableDatabase();
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
                yogaClass.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_LAST_MODIFIED)));
                yogaClass.setNeedsSync(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NEEDS_SYNC)) == 1);
                yogaClass.setCloudId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLOUD_ID)));
                
                yogaClassList.add(yogaClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return yogaClassList;
    }
    
    /**
     * Mark a yoga class as synced (no longer needs sync)
     */
    public void markClassAsSynced(long classId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NEEDS_SYNC, 0);
        
        db.update(TABLE_YOGA_CLASSES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(classId)});
        db.close();
    }
    
    /**
     * Mark a class instance as synced (no longer needs sync)
     */
    public void markInstanceAsSynced(long instanceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_INSTANCE_NEEDS_SYNC, 0);
        
        db.update(TABLE_CLASS_INSTANCES, values, KEY_INSTANCE_ID + " = ?",
                new String[]{String.valueOf(instanceId)});
        db.close();
    }
    
    /**
     * Get all class instances that need syncing
     */
    public List<ClassInstance> getInstancesNeedingSync() {
        List<ClassInstance> instanceList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCES + 
                " WHERE " + KEY_INSTANCE_NEEDS_SYNC + " = 1";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                ClassInstance instance = new ClassInstance();
                instance.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INSTANCE_ID)));
                instance.setYogaClassId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_YOGA_CLASS_ID)));
                instance.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)));
                instance.setInstructor(cursor.getString(cursor.getColumnIndexOrThrow(KEY_INSTANCE_INSTRUCTOR)));
                instance.setAdditionalComments(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADDITIONAL_COMMENTS)));
                instance.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INSTANCE_LAST_MODIFIED)));
                instance.setNeedsSync(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_INSTANCE_NEEDS_SYNC)) == 1);
                instance.setCloudId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_INSTANCE_CLOUD_ID)));
                
                instanceList.add(instance);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return instanceList;
    }
    
    /**
     * Set cloud ID for a yoga class
     */
    public void setClassCloudId(long classId, String cloudId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CLOUD_ID, cloudId);
        
        db.update(TABLE_YOGA_CLASSES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(classId)});
        db.close();
    }
    
    /**
     * Set cloud ID for a class instance
     */
    public void setInstanceCloudId(long instanceId, String cloudId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_INSTANCE_CLOUD_ID, cloudId);
        
        db.update(TABLE_CLASS_INSTANCES, values, KEY_INSTANCE_ID + " = ?",
                new String[]{String.valueOf(instanceId)});
        db.close();
    }
    
    /**
     * Get all pending deletions that need to be synced to cloud
     */
    public List<PendingDeletion> getPendingDeletions() {
        List<PendingDeletion> deletions = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PENDING_DELETIONS;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                PendingDeletion deletion = new PendingDeletion();
                deletion.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DELETION_ID)));
                deletion.setItemType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DELETION_ITEM_TYPE)));
                deletion.setItemId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DELETION_ITEM_ID)));
                deletion.setCloudId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DELETION_CLOUD_ID)));
                deletion.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DELETION_TIMESTAMP)));
                
                deletions.add(deletion);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return deletions;
    }
    
    /**
     * Remove a pending deletion after successful cloud sync
     */
    public void removePendingDeletion(long deletionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_PENDING_DELETIONS, KEY_DELETION_ID + " = ?", 
                     new String[]{String.valueOf(deletionId)});
        } finally {
            db.close();
        }
    }
    
    /**
     * Clear all pending deletions (use with caution)
     */
    public void clearAllPendingDeletions() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_PENDING_DELETIONS, null, null);
        } finally {
            db.close();
        }
    }
    
    // ==================== BOOKING MANAGEMENT METHODS ====================
    
    /**
     * Add a new booking
     */
    public long addBooking(Booking booking) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_BOOKING_ID, booking.getBookingId());
            values.put(COLUMN_BOOKING_CUSTOMER_NAME, booking.getCustomerName());
            values.put(COLUMN_BOOKING_CUSTOMER_EMAIL, booking.getCustomerEmail());
            values.put(COLUMN_BOOKING_CUSTOMER_PHONE, booking.getCustomerPhone());
            values.put(COLUMN_BOOKING_CLASS_INSTANCE_ID, booking.getClassInstanceId());
            values.put(COLUMN_BOOKING_CLASS_NAME, booking.getClassName());
            values.put(COLUMN_BOOKING_ALL_CLASSES, classListToJson(booking.getAllClassNames()));
            values.put(COLUMN_BOOKING_DATE, booking.getBookingDate());
            values.put(COLUMN_BOOKING_TIME, booking.getBookingTime());
            values.put(COLUMN_BOOKING_STATUS, booking.getStatus());
            values.put(COLUMN_BOOKING_PAYMENT_STATUS, booking.getPaymentStatus());
            values.put(COLUMN_BOOKING_PAYMENT_AMOUNT, booking.getPaymentAmount());
            values.put(COLUMN_BOOKING_PAYMENT_METHOD, booking.getPaymentMethod());
            values.put(COLUMN_BOOKING_NOTES, booking.getNotes());
            values.put(COLUMN_BOOKING_CREATED_AT, booking.getCreatedAt());
            values.put(COLUMN_BOOKING_UPDATED_AT, booking.getUpdatedAt());
            values.put(COLUMN_BOOKING_IS_SYNCED, booking.isSynced() ? 1 : 0);
            values.put(COLUMN_BOOKING_LAST_MODIFIED, booking.getLastModified());
            
            return db.insert(TABLE_BOOKINGS, null, values);
        } catch (Exception e) {
            // Handle unique constraint violation
            android.util.Log.e("DatabaseHelper", "Error adding booking: " + booking.getBookingId(), e);
            return -1;
        } finally {
            db.close();
        }
    }
    
    /**
     * Check if booking exists by booking ID
     */
    public boolean bookingExists(String bookingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String selectQuery = "SELECT 1 FROM " + TABLE_BOOKINGS + " WHERE " + COLUMN_BOOKING_ID + " = ?";
            cursor = db.rawQuery(selectQuery, new String[]{bookingId});
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
    
    /**
     * Get booking by booking ID
     */
    public Booking getBookingByBookingId(String bookingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String selectQuery = "SELECT * FROM " + TABLE_BOOKINGS + " WHERE " + COLUMN_BOOKING_ID + " = ?";
            cursor = db.rawQuery(selectQuery, new String[]{bookingId});
            
            if (cursor.moveToFirst()) {
                Booking booking = new Booking();
                booking.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_TABLE_ID)));
                booking.setBookingId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_ID)));
                booking.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_NAME)));
                booking.setCustomerEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_EMAIL)));
                booking.setCustomerPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_PHONE)));
                booking.setClassInstanceId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CLASS_INSTANCE_ID)));
                booking.setClassName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CLASS_NAME)));
                booking.setBookingDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_DATE)));
                booking.setBookingTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_TIME)));
                booking.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_STATUS)));
                booking.setPaymentStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_PAYMENT_STATUS)));
                booking.setPaymentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_PAYMENT_AMOUNT)));
                booking.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_PAYMENT_METHOD)));
                booking.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_NOTES)));
                booking.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CREATED_AT)));
                booking.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_UPDATED_AT)));
                booking.setSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_IS_SYNCED)) == 1);
                booking.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_LAST_MODIFIED)));
                
                // Debug logging
                android.util.Log.d("DatabaseHelper", "Retrieved booking from database with ID: " + booking.getBookingId());
                
                return booking;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return null;
    }

    /**
     * Get all bookings
     */
    public List<Booking> getAllBookings() {
        List<Booking> bookingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String selectQuery = "SELECT * FROM " + TABLE_BOOKINGS + " ORDER BY " + COLUMN_BOOKING_DATE + " DESC, " + COLUMN_BOOKING_TIME + " DESC";
            cursor = db.rawQuery(selectQuery, null);
            
            if (cursor.moveToFirst()) {
                do {
                    Booking booking = new Booking();
                    booking.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_TABLE_ID)));
                    booking.setBookingId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_ID)));
                    booking.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_NAME)));
                    booking.setCustomerEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_EMAIL)));
                    booking.setCustomerPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_PHONE)));
                    booking.setClassInstanceId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CLASS_INSTANCE_ID)));
                    booking.setClassName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CLASS_NAME)));
                    
                    // Load all class names from JSON
                    String allClassesJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_ALL_CLASSES));
                    List<String> allClasses = jsonToClassList(allClassesJson);
                    booking.setAllClassNames(allClasses);
                    
                    booking.setBookingDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_DATE)));
                    booking.setBookingTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_TIME)));
                    booking.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_STATUS)));
                    booking.setPaymentStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_PAYMENT_STATUS)));
                    booking.setPaymentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_PAYMENT_AMOUNT)));
                    booking.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_PAYMENT_METHOD)));
                    booking.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_NOTES)));
                    booking.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CREATED_AT)));
                    booking.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_UPDATED_AT)));
                    booking.setSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_IS_SYNCED)) == 1);
                    booking.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_LAST_MODIFIED)));
                    
                    bookingList.add(booking);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return bookingList;
    }
    
    /**
     * Get bookings for a specific class instance
     */
    public List<Booking> getBookingsForClassInstance(String classInstanceId) {
        List<Booking> bookingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String selectQuery = "SELECT * FROM " + TABLE_BOOKINGS + 
                               " WHERE " + COLUMN_BOOKING_CLASS_INSTANCE_ID + " = ?" +
                               " ORDER BY " + COLUMN_BOOKING_CREATED_AT + " ASC";
            cursor = db.rawQuery(selectQuery, new String[]{classInstanceId});
            
            if (cursor.moveToFirst()) {
                do {
                    Booking booking = new Booking();
                    booking.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_TABLE_ID)));
                    booking.setBookingId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_ID)));
                    booking.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_NAME)));
                    booking.setCustomerEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_EMAIL)));
                    booking.setCustomerPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_PHONE)));
                    booking.setClassInstanceId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CLASS_INSTANCE_ID)));
                    booking.setClassName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CLASS_NAME)));
                    booking.setBookingDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_DATE)));
                    booking.setBookingTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_TIME)));
                    booking.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_STATUS)));
                    booking.setPaymentStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_PAYMENT_STATUS)));
                    booking.setPaymentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_PAYMENT_AMOUNT)));
                    booking.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_PAYMENT_METHOD)));
                    booking.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_NOTES)));
                    booking.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CREATED_AT)));
                    booking.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_UPDATED_AT)));
                    booking.setSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_IS_SYNCED)) == 1);
                    booking.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_LAST_MODIFIED)));
                    
                    bookingList.add(booking);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return bookingList;
    }
    
    /**
     * Update a booking by booking ID
     */
    public int updateBooking(Booking booking) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_BOOKING_CUSTOMER_NAME, booking.getCustomerName());
            values.put(COLUMN_BOOKING_CUSTOMER_EMAIL, booking.getCustomerEmail());
            values.put(COLUMN_BOOKING_CUSTOMER_PHONE, booking.getCustomerPhone());
            values.put(COLUMN_BOOKING_CLASS_INSTANCE_ID, booking.getClassInstanceId());
            values.put(COLUMN_BOOKING_CLASS_NAME, booking.getClassName());
            values.put(COLUMN_BOOKING_ALL_CLASSES, classListToJson(booking.getAllClassNames()));
            values.put(COLUMN_BOOKING_DATE, booking.getBookingDate());
            values.put(COLUMN_BOOKING_TIME, booking.getBookingTime());
            values.put(COLUMN_BOOKING_STATUS, booking.getStatus());
            values.put(COLUMN_BOOKING_PAYMENT_STATUS, booking.getPaymentStatus());
            values.put(COLUMN_BOOKING_PAYMENT_AMOUNT, booking.getPaymentAmount());
            values.put(COLUMN_BOOKING_PAYMENT_METHOD, booking.getPaymentMethod());
            values.put(COLUMN_BOOKING_NOTES, booking.getNotes());
            values.put(COLUMN_BOOKING_CREATED_AT, booking.getCreatedAt());
            values.put(COLUMN_BOOKING_UPDATED_AT, booking.getUpdatedAt());
            values.put(COLUMN_BOOKING_IS_SYNCED, booking.isSynced() ? 1 : 0);
            values.put(COLUMN_BOOKING_LAST_MODIFIED, booking.getLastModified());
            
            return db.update(TABLE_BOOKINGS, values, COLUMN_BOOKING_ID + " = ?", new String[]{booking.getBookingId()});
        } finally {
            db.close();
        }
    }

    /**
     * Update booking status
     */
    public int updateBookingStatus(String bookingId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_BOOKING_STATUS, newStatus);
            values.put(COLUMN_BOOKING_UPDATED_AT, System.currentTimeMillis());
            values.put(COLUMN_BOOKING_IS_SYNCED, 0); // Mark as not synced
            values.put(COLUMN_BOOKING_LAST_MODIFIED, System.currentTimeMillis());
            
            return db.update(TABLE_BOOKINGS, values, COLUMN_BOOKING_ID + " = ?", new String[]{bookingId});
        } finally {
            db.close();
        }
    }
    
    /**
     * Update booking payment status
     */
    public int updateBookingPaymentStatus(String bookingId, String paymentStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_BOOKING_PAYMENT_STATUS, paymentStatus);
            values.put(COLUMN_BOOKING_UPDATED_AT, System.currentTimeMillis());
            values.put(COLUMN_BOOKING_IS_SYNCED, 0); // Mark as not synced
            values.put(COLUMN_BOOKING_LAST_MODIFIED, System.currentTimeMillis());
            
            return db.update(TABLE_BOOKINGS, values, COLUMN_BOOKING_ID + " = ?", new String[]{bookingId});
        } finally {
            db.close();
        }
    }
    
    /**
     * Get bookings that need to be synced
     */
    public List<Booking> getUnsyncedBookings() {
        List<Booking> bookingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String selectQuery = "SELECT * FROM " + TABLE_BOOKINGS + " WHERE " + COLUMN_BOOKING_IS_SYNCED + " = 0";
            cursor = db.rawQuery(selectQuery, null);
            
            if (cursor.moveToFirst()) {
                do {
                    Booking booking = new Booking();
                    booking.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_TABLE_ID)));
                    booking.setBookingId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_ID)));
                    booking.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_NAME)));
                    booking.setCustomerEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_EMAIL)));
                    booking.setCustomerPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_PHONE)));
                    booking.setClassInstanceId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CLASS_INSTANCE_ID)));
                    booking.setClassName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CLASS_NAME)));
                    booking.setBookingDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_DATE)));
                    booking.setBookingTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_TIME)));
                    booking.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_STATUS)));
                    booking.setPaymentStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_PAYMENT_STATUS)));
                    booking.setPaymentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_PAYMENT_AMOUNT)));
                    booking.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_PAYMENT_METHOD)));
                    booking.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_NOTES)));
                    booking.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CREATED_AT)));
                    booking.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_UPDATED_AT)));
                    booking.setSynced(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_IS_SYNCED)) == 1);
                    booking.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_LAST_MODIFIED)));
                    
                    bookingList.add(booking);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return bookingList;
    }
    
    /**
     * Mark booking as synced
     */
    public void markBookingAsSynced(String bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_BOOKING_IS_SYNCED, 1);
            
            db.update(TABLE_BOOKINGS, values, COLUMN_BOOKING_ID + " = ?", new String[]{bookingId});
        } finally {
            db.close();
        }
    }
    
    /**
     * Delete a booking
     */
    public void deleteBooking(String bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_BOOKINGS, COLUMN_BOOKING_ID + " = ?", new String[]{bookingId});
        } finally {
            db.close();
        }
    }
    
    /**
     * Get count of bookings
     */
    public int getBookingCount() {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_BOOKINGS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
    
    /**
     * Debug method to log all booking IDs
     */
    public void logAllBookingIds() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String selectQuery = "SELECT " + COLUMN_BOOKING_ID + ", " + COLUMN_BOOKING_CUSTOMER_NAME + " FROM " + TABLE_BOOKINGS;
            cursor = db.rawQuery(selectQuery, null);
            
            android.util.Log.d("DatabaseHelper", "Total bookings in database: " + cursor.getCount());
            
            if (cursor.moveToFirst()) {
                do {
                    String bookingId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_ID));
                    String customerName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_CUSTOMER_NAME));
                    android.util.Log.d("DatabaseHelper", "Booking ID: " + bookingId);
                } while (cursor.moveToNext());
            } else {
                android.util.Log.d("DatabaseHelper", "No bookings found in database");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
    
    /**
     * Clean up bookings with empty or null booking IDs
     */
    public int cleanupEmptyBookingIds() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Delete bookings where booking_id is empty or null
            int deletedCount = db.delete(TABLE_BOOKINGS, 
                COLUMN_BOOKING_ID + " IS NULL OR " + COLUMN_BOOKING_ID + " = ''", 
                null);
            
            android.util.Log.d("DatabaseHelper", "Cleaned up " + deletedCount + " bookings with empty booking IDs");
            return deletedCount;
        } finally {
            db.close();
        }
    }
    
    /**
     * Get all booking IDs from the database (for deletion sync)
     */
    public List<String> getAllBookingIds() {
        List<String> bookingIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String selectQuery = "SELECT " + COLUMN_BOOKING_ID + " FROM " + TABLE_BOOKINGS + 
                " WHERE " + COLUMN_BOOKING_ID + " IS NOT NULL AND " + COLUMN_BOOKING_ID + " != ''";
            cursor = db.rawQuery(selectQuery, null);
            
            if (cursor.moveToFirst()) {
                do {
                    String bookingId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_ID));
                    if (bookingId != null && !bookingId.trim().isEmpty()) {
                        bookingIds.add(bookingId);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        android.util.Log.d("DatabaseHelper", "Found " + bookingIds.size() + " booking IDs in database");
        return bookingIds;
    }
    
    /**
     * Delete a booking by booking ID (for deletion sync)
     */
    public int deleteBookingByBookingId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            android.util.Log.w("DatabaseHelper", "Cannot delete booking with empty ID");
            return 0;
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int deletedRows = db.delete(TABLE_BOOKINGS, 
                COLUMN_BOOKING_ID + " = ?", 
                new String[]{bookingId});
            
            android.util.Log.d("DatabaseHelper", "Deleted " + deletedRows + " booking(s) with ID: " + bookingId);
            return deletedRows;
        } finally {
            db.close();
        }
    }
    
    /**
     * Convert list of class names to JSON string for database storage
     */
    private String classListToJson(List<String> classList) {
        if (classList == null || classList.isEmpty()) {
            return null;
        }
        try {
            JSONArray jsonArray = new JSONArray();
            for (String className : classList) {
                jsonArray.put(className);
            }
            return jsonArray.toString();
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error converting class list to JSON", e);
            return null;
        }
    }
    
    /**
     * Convert JSON string from database to list of class names
     */
    private List<String> jsonToClassList(String json) {
        List<String> classList = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return classList;
        }
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                classList.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            android.util.Log.e("DatabaseHelper", "Error parsing class list JSON: " + json, e);
        }
        return classList;
    }
    
    /**
     * Debug method to log all classes and their sync status
     */
    public void logAllClassesSyncStatus() {
        List<YogaClass> allClasses = getAllYogaClasses();
        List<ClassInstance> allInstances = getAllClassInstances();
        
        android.util.Log.d("DatabaseHelper", "=== ALL CLASSES SYNC STATUS ===");
        android.util.Log.d("DatabaseHelper", "Total classes in database: " + allClasses.size());
        android.util.Log.d("DatabaseHelper", "Total class instances in database: " + allInstances.size());
        
        for (YogaClass cls : allClasses) {
            android.util.Log.d("DatabaseHelper", 
                "CLASS - ID: " + cls.getId() + 
                ", Type: " + cls.getClassType() + 
                ", Day: " + cls.getDayOfWeek() + 
                ", Time: " + cls.getTime() + 
                ", NeedsSync: " + cls.needsSync() + 
                ", CloudId: " + cls.getCloudId());
        }
        
        for (ClassInstance inst : allInstances) {
            android.util.Log.d("DatabaseHelper", 
                "INSTANCE - ID: " + inst.getId() + 
                ", ClassID: " + inst.getYogaClassId() + 
                ", Date: " + inst.getDate() + 
                ", Instructor: " + inst.getInstructor() + 
                ", NeedsSync: " + inst.needsSync() + 
                ", CloudId: " + inst.getCloudId());
        }
        
        List<YogaClass> needsSync = getClassesNeedingSync();
        List<ClassInstance> instancesNeedSync = getInstancesNeedingSync();
        android.util.Log.d("DatabaseHelper", "Classes needing sync: " + needsSync.size());
        android.util.Log.d("DatabaseHelper", "Instances needing sync: " + instancesNeedSync.size());
        android.util.Log.d("DatabaseHelper", "=== END SYNC STATUS ===");
    }
    
    // ===============================
    // USER AUTHENTICATION METHODS
    // ===============================
    
    /**
     * Create default admin user during database initialization
     */
    private void createDefaultAdminUser(SQLiteDatabase db) {
        try {
            // Generate salt and hash for default password "admin123"
            String salt = generateSalt();
            String passwordHash = hashPassword("admin123", salt);
            
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, "admin");
            values.put(COLUMN_EMAIL, "admin@yogaapp.com");
            values.put(COLUMN_PASSWORD_HASH, passwordHash);
            values.put(COLUMN_SALT, salt);
            values.put(COLUMN_IS_ACTIVE, 1);
            values.put(COLUMN_FAILED_ATTEMPTS, 0);
            values.put(COLUMN_LOCKOUT_UNTIL, 0);
            values.put(COLUMN_LAST_LOGIN, 0);
            values.put(COLUMN_SESSION_TOKEN, (String) null);
            values.put(COLUMN_SESSION_EXPIRES_AT, 0);
            values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
            
            long result = db.insert(TABLE_USERS, null, values);
            if (result != -1) {
                android.util.Log.i("DatabaseHelper", "Default admin user created successfully");
            } else {
                android.util.Log.e("DatabaseHelper", "Failed to create default admin user");
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error creating default admin user", e);
        }
    }
    
    /**
     * Create a new user
     */
    public long createUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, user.getUsername());
            values.put(COLUMN_EMAIL, user.getEmail());
            values.put(COLUMN_PASSWORD_HASH, user.getPasswordHash());
            values.put(COLUMN_SALT, user.getSalt());
            values.put(COLUMN_IS_ACTIVE, user.isActive() ? 1 : 0);
            values.put(COLUMN_FAILED_ATTEMPTS, user.getFailedAttempts());
            values.put(COLUMN_LOCKOUT_UNTIL, user.getLockoutUntil());
            values.put(COLUMN_LAST_LOGIN, user.getLastLogin());
            values.put(COLUMN_SESSION_TOKEN, user.getSessionToken());
            values.put(COLUMN_SESSION_EXPIRES_AT, user.getSessionExpiresAt());
            values.put(COLUMN_CREATED_AT, user.getCreatedAt());
            
            long result = db.insert(TABLE_USERS, null, values);
            return result;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error creating user", e);
            return -1;
        } finally {
            db.close();
        }
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        
        try {
            Cursor cursor = db.query(TABLE_USERS, null, COLUMN_USER_ID + "=?", 
                    new String[]{String.valueOf(userId)}, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error getting user by ID", e);
        } finally {
            db.close();
        }
        
        return user;
    }
    
    /**
     * Get user by username or email
     */
    public User getUserByUsernameOrEmail(String usernameOrEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        
        try {
            String selection = COLUMN_USERNAME + "=? OR " + COLUMN_EMAIL + "=?";
            String[] selectionArgs = {usernameOrEmail, usernameOrEmail};
            
            Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error getting user by username/email", e);
        } finally {
            db.close();
        }
        
        return user;
    }
    
    /**
     * Update user information
     */
    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, user.getUsername());
            values.put(COLUMN_EMAIL, user.getEmail());
            values.put(COLUMN_PASSWORD_HASH, user.getPasswordHash());
            values.put(COLUMN_SALT, user.getSalt());
            values.put(COLUMN_IS_ACTIVE, user.isActive() ? 1 : 0);
            values.put(COLUMN_FAILED_ATTEMPTS, user.getFailedAttempts());
            values.put(COLUMN_LOCKOUT_UNTIL, user.getLockoutUntil());
            values.put(COLUMN_LAST_LOGIN, user.getLastLogin());
            values.put(COLUMN_SESSION_TOKEN, user.getSessionToken());
            values.put(COLUMN_SESSION_EXPIRES_AT, user.getSessionExpiresAt());
            
            int rowsAffected = db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", 
                    new String[]{String.valueOf(user.getId())});
            
            return rowsAffected > 0;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error updating user", e);
            return false;
        } finally {
            db.close();
        }
    }
    
    /**
     * Invalidate all sessions (for security)
     */
    public void invalidateAllSessions() {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_SESSION_TOKEN, (String) null);
            values.put(COLUMN_SESSION_EXPIRES_AT, 0);
            
            db.update(TABLE_USERS, values, null, null);
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error invalidating all sessions", e);
        } finally {
            db.close();
        }
    }
    
    /**
     * Convert cursor to User object
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)));
        user.setSalt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SALT)));
        user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1);
        user.setFailedAttempts(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAILED_ATTEMPTS)));
        user.setLockoutUntil(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LOCKOUT_UNTIL)));
        user.setLastLogin(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_LOGIN)));
        user.setSessionToken(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_TOKEN)));
        user.setSessionExpiresAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SESSION_EXPIRES_AT)));
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        
        return user;
    }
    
    /**
     * Helper method to generate salt for password hashing
     */
    private String generateSalt() {
        try {
            java.security.SecureRandom random = new java.security.SecureRandom();
            byte[] salt = new byte[32];
            random.nextBytes(salt);
            return android.util.Base64.encodeToString(salt, android.util.Base64.DEFAULT);
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error generating salt", e);
            return "defaultSalt" + System.currentTimeMillis(); // Fallback
        }
    }
    
    /**
     * Helper method to hash password with salt
     */
    private String hashPassword(String password, String salt) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            return android.util.Base64.encodeToString(hashedPassword, android.util.Base64.DEFAULT);
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error hashing password", e);
            return null;
        }
    }
    
    /**
     * Clean up duplicate class instances
     * Keeps the instance with the highest ID (most recent) for each duplicate group
     */
    public int cleanupDuplicateInstances() {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedCount = 0;
        
        try {
            // Find duplicates (same yoga_class_id, date, and instructor)
            String findDuplicatesQuery = 
                "SELECT " + KEY_YOGA_CLASS_ID + ", " + KEY_DATE + ", " + KEY_INSTANCE_INSTRUCTOR + ", " +
                "COUNT(*) as count, GROUP_CONCAT(" + KEY_INSTANCE_ID + " ORDER BY " + KEY_INSTANCE_ID + " DESC) as ids " +
                "FROM " + TABLE_CLASS_INSTANCES + 
                " GROUP BY " + KEY_YOGA_CLASS_ID + ", " + KEY_DATE + ", " + KEY_INSTANCE_INSTRUCTOR + 
                " HAVING COUNT(*) > 1";
            
            Cursor cursor = db.rawQuery(findDuplicatesQuery, null);
            
            if (cursor.moveToFirst()) {
                do {
                    String idsString = cursor.getString(cursor.getColumnIndexOrThrow("ids"));
                    String[] ids = idsString.split(",");
                    
                    // Keep the first ID (highest/most recent), delete the rest
                    for (int i = 1; i < ids.length; i++) {
                        int deleteCount = db.delete(TABLE_CLASS_INSTANCES, 
                                                  KEY_INSTANCE_ID + " = ?", 
                                                  new String[]{ids[i].trim()});
                        deletedCount += deleteCount;
                        Log.d("DatabaseHelper", "🗑️ Deleted duplicate instance ID: " + ids[i].trim());
                    }
                    
                    Log.d("DatabaseHelper", "🧹 Cleaned duplicate group - kept ID: " + ids[0].trim() + 
                          ", deleted " + (ids.length - 1) + " duplicates");
                    
                } while (cursor.moveToNext());
            }
            
            cursor.close();
            Log.d("DatabaseHelper", "✅ Cleanup completed: " + deletedCount + " duplicate instances removed");
            
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error cleaning up duplicate instances", e);
        } finally {
            db.close();
        }
        
        return deletedCount;
    }
}