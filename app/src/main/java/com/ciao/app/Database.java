package com.ciao.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Database
 */
public class Database extends SQLiteOpenHelper {
    /**
     * Name of table
     */
    public static final String TABLE_NAME = "CIAO";
    /**
     * Name of temp table
     */
    public static final String TMP_TABLE_NAME = "TMP";

    /**
     * Constructor
     *
     * @param context Context
     */
    public Database(@Nullable Context context) {
        super(context, "database", null, BuildConfig.VERSION_CODE);
    }

    /**
     * On create
     *
     * @param db Database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(id TEXT, thumbnail TEXT, title TEXT, tags TEXT, date TEXT, location TEXT, type TEXT, path TEXT, link TEXT, description TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TMP_TABLE_NAME + "(id TEXT, thumbnail TEXT, title TEXT, tags TEXT, date TEXT, location TEXT, type TEXT, path TEXT, link TEXT, description TEXT)");
        Log.d("database", "created tables");
    }

    /**
     * On upgrade
     *
     * @param db         Database
     * @param oldVersion Old version
     * @param newVersion New version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TMP_TABLE_NAME);
        Log.d("database", "upgrade");
        onCreate(db);
    }

    /**
     * Insert rows into database
     *
     * @param table Table
     * @param rows  Rows
     */
    public void insertInto(String table, ArrayList<HashMap<String, String>> rows) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (HashMap<String, String> row : rows) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", row.get("id"));
            contentValues.put("thumbnail", row.get("thumbnail"));
            contentValues.put("title", row.get("title"));
            contentValues.put("tags", row.get("tags"));
            contentValues.put("date", row.get("date"));
            contentValues.put("location", row.get("location"));
            contentValues.put("type", row.get("type"));
            contentValues.put("path", row.get("path"));
            contentValues.put("link", row.get("link"));
            contentValues.put("description", row.get("description"));
            db.insert(table, null, contentValues);
        }
        db.close();
        Log.d("database", toString(table));
    }

    /**
     * Clear database
     *
     * @param table Table
     */
    public void clear(String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + table);
        db.close();
        Log.d("database", "cleared table " + table);
    }

    /**
     * Get rows of database by filter
     *
     * @param filter   Filter
     * @param location Location
     * @return Rows
     */
    public ArrayList<HashMap<String, String>> getRowsByFilter(String filter, String location) {
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY date DESC";
        if (location != null) {
            query = "SELECT * FROM " + TABLE_NAME + " WHERE location LIKE '%" + location + "%' ORDER BY date DESC";
        } else if (filter != null) {
            query = "SELECT * FROM " + TABLE_NAME + " WHERE tags LIKE '%" + filter + "%' ORDER BY date DESC";
        }
        ArrayList<HashMap<String, String>> rows = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            HashMap<String, String> row = new HashMap<>();
            row.put("id", cursor.getString(0));
            row.put("thumbnail", cursor.getString(1));
            row.put("title", cursor.getString(2));
            row.put("tags", cursor.getString(3));
            row.put("date", cursor.getString(4));
            row.put("location", cursor.getString(5));
            row.put("type", cursor.getString(6));
            row.put("path", cursor.getString(7));
            row.put("link", cursor.getString(8));
            row.put("description", cursor.getString(9));
            rows.add(row);
        }
        cursor.close();
        db.close();
        return rows;
    }

    /**
     * Get rows of database by title
     *
     * @param search Search
     * @return Rows
     */
    public ArrayList<HashMap<String, String>> getRowsBySearch(String search) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE title LIKE ? OR tags LIKE ? OR id LIKE ? OR date LIKE ? OR location LIKE ? ORDER BY date DESC";
        ArrayList<HashMap<String, String>> rows = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{"%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%"});
        while (cursor.moveToNext()) {
            HashMap<String, String> row = new HashMap<>();
            row.put("id", cursor.getString(0));
            row.put("thumbnail", cursor.getString(1));
            row.put("title", cursor.getString(2));
            row.put("tags", cursor.getString(3));
            row.put("date", cursor.getString(4));
            row.put("location", cursor.getString(5));
            row.put("type", cursor.getString(6));
            row.put("path", cursor.getString(7));
            row.put("link", cursor.getString(8));
            row.put("description", cursor.getString(9));
            rows.add(row);
        }
        cursor.close();
        db.close();
        return rows;
    }

    /**
     * To string
     *
     * @param table Table
     * @return String
     */
    public String toString(String table) {
        String string = table + " :\n";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + table, null);
        while (cursor.moveToNext()) {
            string += cursor.getString(0) + " | " +
                    cursor.getString(1) + " | " +
                    cursor.getString(2) + " | " +
                    cursor.getString(3) + " | " +
                    cursor.getString(4) + " | " +
                    cursor.getString(5) + " | " +
                    cursor.getString(6) + " | " +
                    cursor.getString(7) + " | " +
                    cursor.getString(8) + " | " +
                    cursor.getString(9) + "\n";
        }
        cursor.close();
        db.close();
        return string;
    }

    /**
     * Get row by id
     *
     * @param id Id
     * @return Row
     */
    public HashMap<String, String> getRowById(String id) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE id='" + id + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToNext();
        HashMap<String, String> row = new HashMap<>();
        row.put("id", cursor.getString(0));
        row.put("thumbnail", cursor.getString(1));
        row.put("title", cursor.getString(2));
        row.put("tags", cursor.getString(3));
        row.put("date", cursor.getString(4));
        row.put("location", cursor.getString(5));
        row.put("type", cursor.getString(6));
        row.put("path", cursor.getString(7));
        row.put("link", cursor.getString(8));
        row.put("description", cursor.getString(9));
        cursor.close();
        db.close();
        return row;
    }

    /**
     * Compare tables
     *
     * @return Difference
     */
    public ArrayList<HashMap<String, String>> compareTables() {
        String query = "SELECT * FROM " + TMP_TABLE_NAME + " WHERE id NOT IN (SELECT id FROM " + TABLE_NAME + ")";
        ArrayList<HashMap<String, String>> rows = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            HashMap<String, String> row = new HashMap<>();
            row.put("id", cursor.getString(0));
            row.put("thumbnail", cursor.getString(1));
            row.put("title", cursor.getString(2));
            row.put("tags", cursor.getString(3));
            row.put("date", cursor.getString(4));
            row.put("location", cursor.getString(5));
            row.put("type", cursor.getString(6));
            row.put("path", cursor.getString(7));
            row.put("link", cursor.getString(8));
            row.put("description", cursor.getString(9));
            rows.add(row);
        }
        cursor.close();
        db.close();
        return rows;
    }
}
