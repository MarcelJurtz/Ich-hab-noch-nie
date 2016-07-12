package com.jurtz.android.ichhabnochnie;

import android.app.Activity;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jurtz.android.ichhabnochnie.database.databaseManager;
import com.jurtz.android.ichhabnochnie.message.Message;
import com.jurtz.android.ichhabnochnie.message.MessageHelper;

import java.util.Date;

public class customEntryActivity extends AppCompatActivity {

    TextView lblCustomEntryInfo;
    Button cmdEnterCustomEntry;
    Button cmdCancelCustomEntry;
    EditText txtCustomEntry;
    RelativeLayout customEntryActivityLayout;
    Activity activity;

    private SQLiteDatabase db;
    private databaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_entry);

        dbManager = new databaseManager(this);

        activity = this;
        customEntryActivityLayout = (RelativeLayout)findViewById(R.id.customEntryActivityLayout);
        customEntryActivityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view = activity.getCurrentFocus();
                if(view == null) {
                    view = new View(activity);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(),0);
            }
        });

        lblCustomEntryInfo = (TextView)findViewById(R.id.lblCustomEntryInfo);
        lblCustomEntryInfo.setText("Ich hab noch nie...");

        cmdCancelCustomEntry = (Button)findViewById(R.id.cmdCustomEntryCancel);
        cmdCancelCustomEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.close();
                finish();
            }
        });

        cmdEnterCustomEntry = (Button)findViewById(R.id.cmdCustomEntryEnter);
        cmdEnterCustomEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtCustomEntry.getText().toString().length() > 0) {
                    if (txtCustomEntry.getText().toString().contains("'")) {
                        Toast.makeText(getApplicationContext(), "Ungültige Eingabe", Toast.LENGTH_SHORT).show();
                        txtCustomEntry.setText("");
                    } else {
                        Date today = new Date();
                        Message msg = new Message(txtCustomEntry.getText().toString(), today, "CUSTOM");
                        String sql = MessageHelper.getInputCommand(msg, databaseManager.getTableName());
                        try {
                            db.execSQL(sql);
                            Toast.makeText(getApplicationContext(), "Eintrag hinzugefügt", Toast.LENGTH_SHORT).show();
                        } catch (SQLException sqlEx) {
                            Toast.makeText(getApplicationContext(), "Fehler beim Eintrag in die Datenbank", Toast.LENGTH_SHORT).show();
                        }
                    }
                    txtCustomEntry.setText("");
                } else {
                    Toast.makeText(getApplicationContext(),"Keine Eingabe vorhanden",Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtCustomEntry = (EditText)findViewById(R.id.txtCustomEntry);

    }

    // DB-Methoden

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        db = dbManager.getReadableDatabase();
        // Toast.makeText(getApplicationContext(),"DB geöffnet",Toast.LENGTH_LONG).show();
    }

    private boolean validateInput(String input) {
        if(input.contains("'")) {
            return false;
        } else {
            return true;
        }
    }
}
