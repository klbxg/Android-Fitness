package com.example.weiweili.isfitness;

/**
 * Created by weiweili on 7/16/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "users.db";
    private static final String TABLE_NAME = "users";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    SQLiteDatabase db;

    private static final String TABLE_CREATE = "create table users (id integer primary key not null, " +
            "name text not null , username text not null, email text not null, password text not null)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        this.db = db;
    }

    public void insertUser(User user){
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String query = "select * from users";
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();

        values.put(COLUMN_ID, count);
        values.put(COLUMN_NAME, user.name);
        values.put(COLUMN_EMAIL, user.email);
        values.put(COLUMN_USERNAME, user.username);
        values.put(COLUMN_PASSWORD, user.password);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public String searchPass(String username) {
        db = this.getReadableDatabase();
        String query = "select username, password from " +TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        String user, pass;
        pass = "not found";
        if(cursor.moveToFirst()) {
            do {
                user = cursor.getString(0);
                pass = cursor.getString(1);

                if(user.equals(username)) {
                    pass = cursor.getString(1);
                    break;
                }
            }
            while (cursor.moveToNext());
        }
        db.close();
        return pass;
    }

    public User searchInfo(String username) {
        db = this.getReadableDatabase();
        String query = "select username, name, email, password from " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do {
                String userFind = cursor.getString(0);

                if(userFind.equals(username)) {
                    //int age=cursor.getInt(1);
                    String name=cursor.getString(1);
                    String email=cursor.getString(2);
                    String password=cursor.getString(3);
                    User user = new User(name, 28, username, email, password);
                    db.close();
                    return user;
                }
            }
            while (cursor.moveToNext());
        }
        db.close();
        return null;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS" + TABLE_NAME;
        db.execSQL(query);
        this.onCreate(db);
    }
}
