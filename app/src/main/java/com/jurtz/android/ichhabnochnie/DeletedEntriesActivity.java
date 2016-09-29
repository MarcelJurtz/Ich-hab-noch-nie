package com.jurtz.android.ichhabnochnie;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jurtz.android.ichhabnochnie.database.databaseManager;
import com.jurtz.android.ichhabnochnie.message.Message;

import java.util.HashMap;

public class DeletedEntriesActivity extends AppCompatActivity {

    private Button cmdReturn;
    private LinearLayout llDeleted;
    private SQLiteDatabase db;
    private databaseManager dbManager;
    private HashMap<String,TextView> HMdeletedTexts;
    private TextView txtCaption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted_entries);

        HMdeletedTexts = new HashMap<>();

        txtCaption = (TextView)findViewById(R.id.lblDeletedEntriesCaption);
        txtCaption.setPaintFlags(txtCaption.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        cmdReturn = (Button)findViewById(R.id.cmdDeletedEntriesReturn);
        cmdReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        llDeleted = (LinearLayout)findViewById(R.id.llDeletedTexts);

        dbManager = new databaseManager(this);
        db = dbManager.getReadableDatabase();
        // Gelöschte Einträge liegen vor mit Eintrag in DB (author)
        // CUSTOM_DELETED und SYSTEM_DELETED
        String sql = "SELECT * FROM "+dbManager.getTableName()+" WHERE author LIKE '%_DELETED' ORDER BY text;";
        try {
            Cursor dbCursor = db.rawQuery(sql,null);
            String text;
            String author;
            String date;
            if(dbCursor.getCount() > 0) {
                if (dbCursor.moveToFirst()) {
                    while (dbCursor.isAfterLast() == false) {
                        text = dbCursor.getString(dbCursor.getColumnIndex("text"));
                        author = dbCursor.getString(dbCursor.getColumnIndex("author"));
                        date = dbCursor.getString(dbCursor.getColumnIndex("date_added"));
                        Message message = new Message(text,date,author);


                        final String currentText = text;
                        final String currentAuthor = author;
                        TextView txt = new TextView(getApplicationContext());
                        txt.setText("\u2022 " + text);
                        txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        txt.setTextColor(getResources().getColor(R.color.colorMainForeground));
                        txt.setClickable(true);
                        txt.setPadding(0,10,0,10);
                        txt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                restoreEntry(currentText, currentAuthor);
                            }
                        });
                        llDeleted.addView(txt);
                        HMdeletedTexts.put(currentText,txt);
                        dbCursor.moveToNext();
                    }
                }
            }
        } catch(Exception ex) {
            Toast.makeText(getApplicationContext(),"Fehlerhafte Datenbank",Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }
    private void restoreEntry(String text, String author) {
        db = dbManager.getReadableDatabase();
        try {
            if(author.equals(databaseManager.STR_MESSAGE_SYSTEM_DELETED)) {
                author = databaseManager.STR_MESSAGE_SYSTEM;
            } else {
                author = databaseManager.STR_MESSAGE_CUSTOM;
            }
            String sql = "UPDATE "+databaseManager.getTableName()+" SET author = '"+author+"' WHERE text = '"+text+"'";
            db.execSQL(sql);
            llDeleted.removeView(HMdeletedTexts.get(text));
            Toast.makeText(getApplicationContext(),"Eintrag wiederhergestellt",Toast.LENGTH_SHORT).show();
        } catch(Exception ex) {
            Toast.makeText(getApplicationContext(),"Fehler bei der Wiederherstellung",Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }

        // TODO: Updateverhalten bei gelöschten Einträgen
    }
}
