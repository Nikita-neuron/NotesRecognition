package com.example.tabnote;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tabnote.database.DBManager;

import org.json.JSONException;

public class TabActivity extends AppCompatActivity {

    RelativeLayout tabRoot;

    Bitmap microphoneImage;
    Bitmap noMicrophoneImage;

    ImageView btnMicrophone;

    TabRec tabRec;

    DBManager dbManager;

    String userName;

    @SuppressLint({"CommitTransaction", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_layout);

        // get database
        dbManager = DBManager.getInstance(this);

        // find root element
        tabRoot = findViewById(R.id.tabRoot);

        // find microphone button
        btnMicrophone = findViewById(R.id.btnPausePlay);

        // get microphone images
        microphoneImage = BitmapFactory.decodeResource(getResources(), R.drawable.microphone);
        noMicrophoneImage = BitmapFactory.decodeResource(getResources(), R.drawable.no_microphone);

        // get type, user name and tab name from intent
        String tabType = getIntent().getExtras().getString("tabType");
        String body = getIntent().getExtras().getString("body");
        String tabName = getIntent().getExtras().getString("name");
        userName = getIntent().getExtras().getString("userName");

        tabRec = new TabRec(this, userName, tabRoot);

        try {
            switch (tabType) {
                case "local":
                    String jsonText = dbManager.getTab(tabName);
                    tabRec.readJSONFile(jsonText);
                    break;
                case "out":
                    tabRec.readJSONFile(body);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // don't work
        if (item.getItemId() == android.R.id.home) {
            btnMicrophone.setImageBitmap(microphoneImage);
            if (tabRec.reco) {
                tabRec.audioReciever.stop();
            }
            tabRec.reco = false;

            if (!tabRec.save) {
                tabRec.createDialog("back", "");
            } else {
                Intent in = new Intent(this, MainActivity.class);
                in.putExtra("userName", userName);
                startActivity(in);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // press on back button
        btnMicrophone.setImageBitmap(noMicrophoneImage);
        if (tabRec.reco) {
            tabRec.audioReciever.stop();
        }
        tabRec.reco = false;

        if (!tabRec.save) {
            tabRec.createDialog("back", "");
        } else {
            Intent in = new Intent(this, MainActivity.class);
            in.putExtra("userName", userName);
            startActivity(in);
        }
    }
}
