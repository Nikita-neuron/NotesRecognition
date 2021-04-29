package com.example.tabnote;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.io.IOException;

public class TabSavedActivity extends AppCompatActivity{
    Bitmap playImage;
    Bitmap pauseImage;

    Button btnClear;
    ImageView btnPausePlay;
    ImageView btnSave;
    HorizontalScrollView scrollView;
    TextView frequencyText;

    LinearLayout tabRoot;

    String fileName;

    Button btnPlay;
    LinearLayout strings;

    TabRec tabRec;
    DBManager dbManager;

    String userName;

    @SuppressLint({"HandlerLeak", "WrongViewCast"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_cards_layout);
        dbManager = DBManager.getInstance(this);

        // получение названия файла
        fileName = getIntent().getExtras().getString("name");
        String tabType = getIntent().getExtras().getString("tabType");

        userName = getIntent().getExtras().getString("userName");

        // кнопки начала, остановки распознования, кнопка назад
        btnClear = findViewById(R.id.btnClear);
        btnPausePlay = findViewById(R.id.btnPausePlay);
        btnSave = findViewById(R.id.btnSaveTab);

        btnPlay = findViewById(R.id.btnPlay);

        frequencyText = findViewById(R.id.frequencyText);

        scrollView = findViewById(R.id.horizontalScroll);

        frequencyText = findViewById(R.id.frequencyText);

        strings = findViewById(R.id.strings);

        tabRoot = findViewById(R.id.tabRoot);

        // кнопка назад
//        ActionBar actionBar =getSupportActionBar();
//        assert actionBar != null;
//        actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        @SuppressLint("ResourceAsColor")
//        ColorDrawable colorDrawable
//                = new ColorDrawable(R.color.topMenuColor);
//        actionBar.setBackgroundDrawable(colorDrawable);

        // картинки плея и паузы
        playImage = BitmapFactory.decodeResource(getResources(), R.drawable.microphone);
        pauseImage = BitmapFactory.decodeResource(getResources(), R.drawable.no_microphone);

        tabRec = new TabRec(this, btnClear, btnPausePlay, btnSave, btnPlay, frequencyText, strings, scrollView, userName, tabRoot);

        try {
            // чтение табулатуры
            // получение json строки из базы данных
            switch (tabType) {
                case "local":
                    String jsonText = dbManager.getTab(fileName);
                    tabRec.readJSONFile(jsonText);
                    break;
                case "out":
                    tabRec.readJSONFile(fileName);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // стрелочка назад в верхнем меню
        if (item.getItemId() == android.R.id.home) {
            btnPausePlay.setImageBitmap(pauseImage);
            if (tabRec.reco) {
                try {
                    tabRec.audioReciever.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
        btnPausePlay.setImageBitmap(pauseImage);
        if (tabRec.reco) {
            try {
                tabRec.audioReciever.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
