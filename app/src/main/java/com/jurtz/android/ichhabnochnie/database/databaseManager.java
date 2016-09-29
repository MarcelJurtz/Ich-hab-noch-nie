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
    private static final int dbVersion = 22;

    public static String versionDate = "2016-09-28";

    private static final String tableName = "message";
    private static final String createTable = "CREATE TABLE "+tableName+"(" +
                "text VARCHAR(100) PRIMARY KEY, " +
                "author VARCHAR(6), " +
                "date_added VARCHAR(10)" +
            ");";

    private static final String dropTable = "DROP TABLE IF EXISTS "+tableName+";";

    public static final String STR_MESSAGE_CUSTOM = "CUSTOM";
    public static final String STR_MESSAGE_SYSTEM = "SYSTEM";
    public static final String STR_MESSAGE_CUSTOM_DELETED = "CUSTOM_DELETED";
    public static final String STR_MESSAGE_SYSTEM_DELETED = "SYSTEM_DELETED";

    public static final String SELECT_USER_MESSAGES = "SELECT text, author, date_added FROM "+tableName+" WHERE author='CUSTOM';";
    public static final String SELECT_SYSTEM_MESSAGES = "SELECT text, author, date_added FROM "+tableName+" WHERE author='SYSTEM';";
    public static final String SELECT_ALL_MESSAGES = "SELECT text, author, date_added FROM "+tableName+" WHERE author = 'SYSTEM' OR author = 'CUSTOM';";
    public static final String SELECT_DELETED_MESSAGES = "SELECT text, author, date_added FROM "+tableName+" WHERE author = '"+STR_MESSAGE_CUSTOM_DELETED+"' OR author = '"+STR_MESSAGE_SYSTEM_DELETED+"';";

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
        // Eigene Einträge abspeichern und wieder einfügen
        ArrayList<String> customEntriesArray = new ArrayList<>();
        boolean customsExist = false;
        Cursor c = db.rawQuery(SELECT_USER_MESSAGES, null);
        if(c.getCount() > 0) {
            customsExist = true;
            String text;
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    try {
                        text = c.getString(c.getColumnIndex("text"));
                        customEntriesArray.add(text);
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
        // Gelöschte Einträge als solche Markieren und wieder einfügen
        // Eigene gelöschte Einträge werden nicht doppelt beinhaltet:
        //  eigene: author=CUSTOM
        //  eigene gelöschte: author=CUSTOM_DELETED
        ArrayList<Message> deletedEntriesArray = new ArrayList<>();
        boolean deletedExists = false;
        Cursor delCursor = db.rawQuery(SELECT_DELETED_MESSAGES,null);
        if(delCursor.getCount() > 0) {
            deletedExists = true;
            String text;
            String author;
            String date;
            if(delCursor.moveToFirst()) {
                while(!delCursor.isAfterLast()) {
                    try {
                        text = delCursor.getString(delCursor.getColumnIndex("text"));
                        author = delCursor.getString(delCursor.getColumnIndex("author"));
                        date = delCursor.getString(delCursor.getColumnIndex("date_added"));
                        deletedEntriesArray.add(new Message(text, date, author));
                        delCursor.moveToNext();
                    } catch(Exception ex) {
                        // Fehler bei Übernahme der Daten
                        // While-Loop beenden
                        break;
                    }
                }
            }
        }

        db.execSQL(dropTable);
        db.execSQL(createTable);
        if(customsExist) {
            // Einträge wieder einfügen
            for(int i = 0; i<customEntriesArray.size(); i++) {
                String sql = MessageHelper.getInputCommand(customEntriesArray.get(i), versionDate, "CUSTOM", tableName);
                try {
                    db.execSQL(sql);
                } catch (SQLException sqlEx) {
                    // Eintrag kann nicht eingefügt werden
                    // BSP: Eintrag, der selbst erstellt wurde, ist in Update enthalten
                    // -> Überspringen
                }
            }
        }
        // Standardeinträge einfügen
        HashSet<Message> messages = MessageHelper.getMessages();
        for(Message msg : messages) {
            db.execSQL(MessageHelper.getInputCommand(msg.getText(),msg.getDate(),msg.getAuthor(),tableName));
        }

        // Gelöschte Einträge einspielen
        if(deletedExists) {
            String query = "";
            for(int i = 0; i<deletedEntriesArray.size(); i++) {
                Message currentMessage = deletedEntriesArray.get(i);
                if(currentMessage.getAuthor().equals(STR_MESSAGE_SYSTEM_DELETED)) {
                    // Systemnachricht gelöscht: UPDATE
                    query = "UPDATE "+tableName+" SET author='"+STR_MESSAGE_SYSTEM_DELETED+"' WHERE text = '"+currentMessage.getText()+"'";
                } else {
                    // Eigene Nachricht: INSERT
                    query = MessageHelper.getInputCommand(currentMessage.getText(),currentMessage.getDate(), currentMessage.getAuthor(), tableName);
                }
                try {
                    db.execSQL(query);
                } catch (Exception ex) {
                    // Fehler beim Einspielen gelöschter Einträge
                }
            }
        }
    }
    public static String getTableName() {
        return tableName;
    }
    public static String getVersionDate() {
        return versionDate;
    }
}
