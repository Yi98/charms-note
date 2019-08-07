package com.example.todolists;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TaskDbHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "tasks_db";


    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create notes table
        db.execSQL(Task.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Task.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public long insertNote(String note, int color, String[] subtasks) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        String concatTasks = Task.convertArrayToString(subtasks);

        values.put(Task.COLUMN_TASK, note);
        values.put(Task.COLUMN_COLOR, color);
        values.put(Task.COLUMN_SUBTASKS, concatTasks);


        // insert row
        long id = db.insert(Task.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public Task getNote(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Task.TABLE_NAME,
                new String[]{Task.COLUMN_ID, Task.COLUMN_TASK},
                Task.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        Task note = new Task(
                cursor.getInt(cursor.getColumnIndex(Task.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Task.COLUMN_TASK)),
                cursor.getInt(cursor.getColumnIndex(Task.COLUMN_COLOR)),
                cursor.getString(cursor.getColumnIndex(Task.COLUMN_SUBTASKS))
                );

        // close the db connection
        cursor.close();

        return note;
    }

    public List<Task> getAllNotes() {
        List<Task> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Task.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Task note = new Task();
                note.setId(cursor.getInt(cursor.getColumnIndex(Task.COLUMN_ID)));
                note.setNote(cursor.getString(cursor.getColumnIndex(Task.COLUMN_TASK)));
                note.setColor(cursor.getInt(cursor.getColumnIndex(Task.COLUMN_COLOR)));
                note.setSubtasks(cursor.getString(cursor.getColumnIndex(Task.COLUMN_SUBTASKS)));

                notes.add(note);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return notes;
    }

    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + Task.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updateNote(Task note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Task.COLUMN_TASK, note.getNote());

        // updating row
        return db.update(Task.TABLE_NAME, values, Task.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }

    public void deleteNote(Task note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Task.TABLE_NAME, Task.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        db.close();
    }
}
