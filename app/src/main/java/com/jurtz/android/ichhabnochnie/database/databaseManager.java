package com.jurtz.android.ichhabnochnie.database;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

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
    private static final int dbVersion = 17;

    public static String versionDate = "2016-07-16";

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
            db.execSQL(MessageHelper.getInputCommand(msg.getText(),msg.getDate(),msg.getAuthor(),tableName));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Backup and Re-Insert CUSTOMs
        ArrayList<String> customs = new ArrayList<>();
        boolean customsExist = false;
        Cursor c = db.rawQuery(SELECT_USER_MESSAGES, null);
        if(c.getCount() > 0) {
            customsExist = true;
            String text;
            if (c.moveToFirst()) {
                while (c.isAfterLast() == false) {
                    try {
                        text = c.getString(c.getColumnIndex("text"));
                        customs.add(text);
                        c.moveToNext();
                    }
                    catch (Exception ex) {
                        // Fehler bei Übernahme der Daten
                        // while-Loop beenden
                        break;
                    }
                }
            }
        }
        db.execSQL(dropTable);
        db.execSQL(createTable);
        if(customsExist) {
            // Einträge wieder einfügen
            for(int i = 0; i<customs.size(); i++) {
                String sql = MessageHelper.getInputCommand(customs.get(i), versionDate, "CUSTOM", tableName);
                try {
                    db.execSQL(sql);
                } catch (SQLException sqlEx) {
                    // To be implemented
                }
            }
        }
        // Standardeinträge einfügen
        HashSet<Message> messages = MessageHelper.getMessages();
        for(Message msg : messages) {
            db.execSQL(MessageHelper.getInputCommand(msg.getText(),msg.getDate(),msg.getAuthor(),tableName));
        }
    }
    public static String getTableName() {
        return tableName;
    }
    public static String getVersionDate() {
        return versionDate;
    }
}
