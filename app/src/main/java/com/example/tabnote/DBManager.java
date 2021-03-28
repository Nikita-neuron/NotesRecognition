package com.example.tabnote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DBManager {

    private final String tableName = "TABS";
    private final String tableTabName = "NAME";
    private final String tableTabBody = "BODY";

    private final SQLiteDatabase db;

    @SuppressLint("StaticFieldLeak")
    private static DBManager dbManager;

    public static DBManager getInstance(Context context) {
        if (dbManager == null) {
            dbManager = new DBManager(context);
        }
        return dbManager;
    }

    private DBManager(Context context) {
        String DB_NAME = "tabnote.db";
        db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        createTablesIfNeedBe();
    }

    void addResult(String name, String text) {
        String request = String.format("INSERT INTO %s VALUES ('%s', '%s');", tableName, name, text);
        db.execSQL(request);
    }

    void updateTab(String tabName, String text) {
        String request = String.format("UPDATE %s SET %s = '%s' WHERE %s = '%s';", tableName, tableTabBody, text, tableTabName, tabName);
        db.execSQL(request);
    }

    void deleteTab(String tabName) {
        String request = String.format("DELETE FROM %s WHERE %s = '%s';", tableName, tableTabName, tabName);
        db.execSQL(request);
    }

    ArrayList<String> getTabNames() {
        ArrayList<String> names = new ArrayList<>();

        String request = String.format("SELECT %s FROM %s", tableTabName, tableName);
        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery(request, null);

        boolean hasMoreData = cursor.moveToFirst();

        while (hasMoreData) {
            String name = cursor.getString(cursor.getColumnIndex(tableTabName));

            names.add(name);

            hasMoreData = cursor.moveToNext();
        }
        return names;
    }

    String getTab(String tabName) {
        String data = "";
        String request = String.format("SELECT * FROM %s WHERE %s = '%s';", tableName, tableTabName, tabName);
        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery(request, null);
        boolean hasMoreData = cursor.moveToFirst();

        while (hasMoreData) {
            data = cursor.getString(cursor.getColumnIndex(tableTabBody));
            hasMoreData = cursor.moveToNext();
        }
        return data;
    }

    private void createTablesIfNeedBe() {
        String request = String.format("CREATE TABLE IF NOT EXISTS %s (%s TEXT, %s TEXT);", tableName, tableTabName, tableTabBody);
        db.execSQL(request);
    }
}
