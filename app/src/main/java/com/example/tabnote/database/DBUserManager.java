package com.example.tabnote.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBUserManager {

    private final String tableName = "USER";
    private final String tableUserName = "NAME";
    private final String tableUserBody = "PASSWORD";

    private final SQLiteDatabase db;

    @SuppressLint("StaticFieldLeak")
    private static DBUserManager dbUserManager;

    public static DBUserManager getInstance(Context context) {
        if (dbUserManager == null) {
            dbUserManager = new DBUserManager(context);
        }
        return dbUserManager;
    }

    private DBUserManager(Context context) {
        String DB_NAME = "user.db";
        db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        createTablesIfNeedBe();
    }

    public void addUser(String name, String text) {
        String request = String.format("INSERT INTO %s VALUES ('%s', '%s');", tableName, name, text);
        db.execSQL(request);
    }

    public void deleteUser(String userName) {
        String request = String.format("DELETE FROM %s WHERE %s = '%s';", tableName, tableUserName, userName);
        db.execSQL(request);
    }

    public String existUser() {
        String data = "";
        String request = String.format("SELECT %s FROM %s", tableUserName, tableName);
        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery(request, null);
        boolean hasMoreData = cursor.moveToFirst();

        while (hasMoreData) {
            data = cursor.getString(cursor.getColumnIndex(tableUserName));
            hasMoreData = cursor.moveToNext();
        }

        return data;
    }

    private void createTablesIfNeedBe() {
        String request = String.format("CREATE TABLE IF NOT EXISTS %s (%s TEXT, %s TEXT);", tableName, tableUserName, tableUserBody);
        db.execSQL(request);
    }
}
