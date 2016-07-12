package com.jurtz.android.ichhabnochnie;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jurtz.android.ichhabnochnie.database.databaseManager;

import java.util.ArrayList;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {

    CheckBox chkSystemMessages;
    CheckBox chkUserMessages;
    Button cmdPlay;
    Button cmdAddCustomEntry;
    ImageButton cmdIcon;
    int iconClicks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isFirstRun = getSharedPreferences("PREFERENCE",MODE_PRIVATE).getBoolean("isFirstRun",true);
        if(isFirstRun)
        {
            // Noch nicht gelesen
            // --> Meldung ausgeben
            // --> Datenbankeintrag updaten
            Activity activity = this;
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Haftungshinweis");
            builder.setMessage("Für eventuelle, durch diese App verursachte, Schäden wird keine Haftung übernommen.\n\nMit der Verwendung dieser App stimmen Sie der Bedingung zu.");
            builder.setNeutralButton("OK",null);
            builder.create();
            builder.show();

            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun",false).apply();
        }

        iconClicks = 0;

        chkSystemMessages = (CheckBox)findViewById(R.id.chkSystemMessages);
        chkUserMessages = (CheckBox)findViewById(R.id.chkUserMessages);
        cmdPlay = (Button)findViewById(R.id.cmdPlay);

        cmdPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameIntent = new Intent(MainActivity.this,GameActivity.class);
                String sql = "";
                if(chkUserMessages.isChecked() && chkSystemMessages.isChecked()) {
                    sql = databaseManager.SELECT_ALL_MESSAGES;
                } else if(chkSystemMessages.isChecked()) {
                    sql = databaseManager.SELECT_SYSTEM_MESSAGES;
                } else if(chkUserMessages.isChecked()) {
                    sql = databaseManager.SELECT_USER_MESSAGES;
                }
                if(sql != "") {
                    Bundle data = new Bundle();
                    gameIntent.putExtra("sql",sql);
                    startActivity(gameIntent);
                }
            }
        });

        cmdAddCustomEntry = (Button)findViewById(R.id.cmdAddCustomMessage);
        cmdAddCustomEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customEntryIntent = new Intent(MainActivity.this,customEntryActivity.class);
                startActivity(customEntryIntent);
            }
        });

        cmdIcon = (ImageButton)findViewById(R.id.cmdMainIcon);
        cmdIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconClicks++;
                if(iconClicks == 18) {
                    Toast.makeText(getApplicationContext(),"Design by Manuel Micheler",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
