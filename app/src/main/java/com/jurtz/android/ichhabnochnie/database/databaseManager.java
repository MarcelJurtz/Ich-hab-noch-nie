package com.jurtz.android.ichhabnochnie.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jurtz.android.ichhabnochnie.message.Message;
import com.jurtz.android.ichhabnochnie.message.MessageHelper;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by marcel on 27.05.16.
 */
public class databaseManager extends SQLiteOpenHelper {

    private static final String dbName = "db_IchHabNochNie";
    // version 9: Update 12.7.
    private static final int dbVersion = 9;

    private static final String tableName = "message";
    private static final String createTable = "CREATE TABLE "+tableName+"(" +
                "text VARCHAR(100) PRIMARY KEY, " +
                "author VARCHAR(6), " +
                "date_added VARCHAR(10)" +
            ");";

    private static final String dropTable = "DROP TABLE IF EXISTS "+tableName+";";

    public static final String SELECT_USER_MESSAGES = "SELECT text FROM "+tableName+" WHERE author='CUSTOM';";
    public static final String SELECT_SYSTEM_MESSAGES = "SELECT text FROM "+tableName+" WHERE author='SYSTEM';";
    public static final String SELECT_ALL_MESSAGES = "SELECT text FROM "+tableName+";";

    public databaseManager(Context context) {
        super(context,dbName,null,dbVersion);
    }

    public databaseManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTable);

        HashSet<Message> messages = MessageHelper.getMessages();
        for(Message msg : messages) {
            db.execSQL(MessageHelper.getInputCommand(msg,tableName));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Backup and Re-Insert CUSTOMs
        ArrayList<Message> customs = new ArrayList<>();
        boolean customsExist = false;
        Cursor c = db.rawQuery(SELECT_USER_MESSAGES, null);
        if(c.getCount() > 0) {
            customsExist = true;
            String text;
            String author;
            String date;
            if (c.moveToFirst()) {
                while (c.isAfterLast() == false) {
                    text = c.getString(c.getColumnIndex("text"));
                    date = c.getString(c.getColumnIndex("date_added"));
                    author = c.getString(c.getColumnIndex("author"));
                    Message msg = new Message(text, date, author);
                    customs.add(msg);
                    c.moveToNext();
                }
            }
        }
        db.execSQL(dropTable);
        db.execSQL(createTable);
        if(customsExist) {
            // Einträge wieder einfügen
            for(int i = 0; i<customs.size(); i++) {
                String sql = MessageHelper.getInputCommand(customs.get(i),tableName);
                try {
                    db.execSQL(sql);
                } catch (SQLException sqlEx) {
                    // To be implemented
                }
            }
        }

        HashSet<Message> messages = MessageHelper.getMessages();
        for(Message msg : messages) {
            db.execSQL(MessageHelper.getInputCommand(msg,tableName));
        }
    }
    public static String getTableName() {
        return tableName;
    }
}
