package com.jurtz.android.ichhabnochnie;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jurtz.android.ichhabnochnie.database.databaseManager;
import com.jurtz.android.ichhabnochnie.message.Message;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    RelativeLayout mainLayout;
    TextView txtMessage;
    ImageButton cmdDelete;
    TextView lblRestoreDeletedMessage;

    private SQLiteDatabase db;
    private databaseManager dbManager;

    String sql;
    Random random;
    String currentMessage;
    String currentMessageDate;
    String currentMessageAuthor;
    String emptyMessage;
    String deletedMessage;

    // Speichert sämtliche Strings
    ArrayList<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        emptyMessage = "Keine Einträge vorhanden\n\n¯\\_(ツ)_/¯";
        currentMessage = "";
        currentMessageDate= "";
        currentMessageAuthor = "";

        // SQL-Befehl anhand Auswahl aus voriger Activity
        Bundle data = getIntent().getExtras();
        sql = databaseManager.SELECT_ALL_MESSAGES; // Standard
        if(data != null)
            sql = data.getString("sql");

        // Instanziierung dbManager & Liste
        random = new Random();
        dbManager = new databaseManager(this);
        messages = new ArrayList<>();

        // Layout
        mainLayout = (RelativeLayout)findViewById(R.id.layoutMain);
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMessage = getRandomEntry(random);
                updateMessage();
            }
        });
        mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                finish();
                return true;
            }
        });
        txtMessage = (TextView)findViewById(R.id.txtMessage);
        cmdDelete = (ImageButton)findViewById(R.id.cmdDel);
        cmdDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Funktioniert zwar dank SQL auch so, aber unnötige DB-Verbindung wird bei leerem String vermieden
                if (currentMessage != "" && currentMessage != null && currentMessage != emptyMessage) {
                    String sql = "DELETE FROM " + databaseManager.getTableName() + " WHERE text='" + currentMessage + "'";

                    // Eintrag löschen
                    db = dbManager.getReadableDatabase();
                    db.execSQL(sql);
                    Toast.makeText(getApplicationContext(), "Eintrag wurde gelöscht", Toast.LENGTH_SHORT).show();
                    // Neuen Eintrag laden
                    currentMessage = getRandomEntry(random);
                    updateMessage();
                    db.close();
                }
            }
        });
        // TextView, Klick stellt gelöschten Eintrag wieder her
        lblRestoreDeletedMessage = (TextView)findViewById(R.id.lblRestoreDeletedMessage);
        lblRestoreDeletedMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deletedMessage != "" && deletedMessage != null) {
                    String sql = "INSERT INTO "+databaseManager.getTableName()+" VALUES('"+currentMessage+"','"+currentMessageAuthor+"','"+currentMessageDate+"');";
                    Toast.makeText(getApplicationContext(),sql,Toast.LENGTH_SHORT).show();
                    
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        db = dbManager.getReadableDatabase();
        // Toast.makeText(getApplicationContext(), "Datenbank geöffnet", Toast.LENGTH_SHORT).show();

        Cursor klassenCursor = db.rawQuery(sql,null);
        String text;
        String author;
        String date;
        if(klassenCursor.getCount() > 0) {
            if (klassenCursor.moveToFirst()) {
                while (klassenCursor.isAfterLast() == false) {
                    text = klassenCursor.getString(klassenCursor.getColumnIndex("text"));
                    author = klassenCursor.getString(klassenCursor.getColumnIndex("author"));
                    date = klassenCursor.getString(klassenCursor.getColumnIndex("date_added"));
                    messages.add(new Message(text,date,author));
                    klassenCursor.moveToNext();
                }
            }
        }
        db.close();
        currentMessage = getRandomEntry(random);
        updateMessage();
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
    }

    // Lade zufälligen Eintrag aus der Liste mit allen Sprüchen
    // Eintrag wird anschließend aus der Liste entfernt, um doppelte Texte zu vermeiden
    String getRandomEntry(Random r) {
        if(messages.size() == 0) {
            return emptyMessage;
        } else {
            int index = r.nextInt(messages.size());
            String entry = messages.get(index).getText();
            currentMessageAuthor = messages.get(index).getAuthor();
            currentMessageDate = messages.get(index).getDate();
            messages.remove(index);
            return entry;
        }
    }

    private void updateMessage() {
        if(currentMessage != emptyMessage) {
            txtMessage.setText("Ich hab noch nie "+currentMessage);
        } else {
            txtMessage.setText(currentMessage);
        }
    }
}
