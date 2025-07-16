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
    private static final int DATABASE_VERSION = 3; // Increased version for schema update with ON DELETE CASCADE
    
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
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_INSTRUCTOR = "instructor";
    private static final String KEY_DIFFICULTY = "difficulty";
    
    // Column names for class_instances table
    private static final String KEY_INSTANCE_ID = "id";
    private static final String KEY_YOGA_CLASS_ID = "yoga_class_id";
    private static final String KEY_DATE = "date";
    private static final String KEY_TEACHER = "teacher";
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
                + KEY_INSTRUCTOR + " TEXT,"
                + KEY_DIFFICULTY + " TEXT" + ")";
        db.execSQL(CREATE_YOGA_CLASSES_TABLE);
        
        // Create class instances table
        String CREATE_CLASS_INSTANCES_TABLE = "CREATE TABLE " + TABLE_CLASS_INSTANCES + "("
                + KEY_INSTANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_YOGA_CLASS_ID + " INTEGER NOT NULL,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_TEACHER + " TEXT NOT NULL,"
                + KEY_ADDITIONAL_COMMENTS + " TEXT,"
                + "FOREIGN KEY(" + KEY_YOGA_CLASS_ID + ") REFERENCES " + TABLE_YOGA_CLASSES + "(" + KEY_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_CLASS_INSTANCES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON");
        
        if (oldVersion < 2) {
            // Create class instances table if upgrading from version 1
            String CREATE_CLASS_INSTANCES_TABLE = "CREATE TABLE " + TABLE_CLASS_INSTANCES + "("
                    + KEY_INSTANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_YOGA_CLASS_ID + " INTEGER NOT NULL,"
                    + KEY_DATE + " TEXT NOT NULL,"
                    + KEY_TEACHER + " TEXT NOT NULL,"
                    + KEY_ADDITIONAL_COMMENTS + " TEXT,"
                    + "FOREIGN KEY(" + KEY_YOGA_CLASS_ID + ") REFERENCES " + TABLE_YOGA_CLASSES + "(" + KEY_ID + ") ON DELETE CASCADE)";
            db.execSQL(CREATE_CLASS_INSTANCES_TABLE);
        } else if (oldVersion < 3) {
            // Update the foreign key constraint to include ON DELETE CASCADE
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCES);
            String CREATE_CLASS_INSTANCES_TABLE = "CREATE TABLE " + TABLE_CLASS_INSTANCES + "("
                    + KEY_INSTANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_YOGA_CLASS_ID + " INTEGER NOT NULL,"
                    + KEY_DATE + " TEXT NOT NULL,"
                    + KEY_TEACHER + " TEXT NOT NULL,"
                    + KEY_ADDITIONAL_COMMENTS + " TEXT,"
                    + "FOREIGN KEY(" + KEY_YOGA_CLASS_ID + ") REFERENCES " + TABLE_YOGA_CLASSES + "(" + KEY_ID + ") ON DELETE CASCADE)";
            db.execSQL(CREATE_CLASS_INSTANCES_TABLE);
        }
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
        values.put(KEY_INSTRUCTOR, yogaClass.getInstructor());
        values.put(KEY_DIFFICULTY, yogaClass.getDifficulty());
        
        long id = db.insert(TABLE_YOGA_CLASSES, null, values);
        db.close();
        return id;
    }

    // Get all yoga classes
    public List<YogaClass> getAllYogaClasses() {
        List<YogaClass> yogaClassList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_YOGA_CLASSES + " ORDER BY " + KEY_DAY_OF_WEEK + ", " + KEY_TIME;
        
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
                yogaClass.setInstructor(cursor.getString(cursor.getColumnIndexOrThrow(KEY_INSTRUCTOR)));
                yogaClass.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIFFICULTY)));
                
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
            yogaClass.setInstructor(cursor.getString(cursor.getColumnIndexOrThrow(KEY_INSTRUCTOR)));
            yogaClass.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIFFICULTY)));
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
        values.put(KEY_INSTRUCTOR, yogaClass.getInstructor());
        values.put(KEY_DIFFICULTY, yogaClass.getDifficulty());
        
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
        values.put(KEY_TEACHER, classInstance.getTeacher());
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
                instance.setTeacher(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TEACHER)));
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
            instance.setTeacher(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TEACHER)));
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
        values.put(KEY_TEACHER, instance.getTeacher());
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
}