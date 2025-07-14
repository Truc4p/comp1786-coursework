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
    private static final int DATABASE_VERSION = 1;
    
    // Table name
    private static final String TABLE_YOGA_CLASSES = "yoga_classes";
    
    // Column names
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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_CLASSES);
        onCreate(db);
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
        db.delete(TABLE_YOGA_CLASSES, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Reset database (delete all data)
    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
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
} 