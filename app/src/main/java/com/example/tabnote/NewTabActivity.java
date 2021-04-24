package com.example.tabnote;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class NewTabActivity extends AppCompatActivity{
    Bitmap playImage;
    Bitmap pauseImage;

    Button btnClear;
    ImageView btnPausePlay;
    ImageView btnSave;
    FrameLayout[] frameLayouts;
    HorizontalScrollView scrollView;
    TextView frequencyText;
    Button btnPlay;

    LinearLayout strings;


    public int width;
    public int height;

    TabRec tabRec;

    String userName;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_cards_layout);

        userName = getIntent().getExtras().getString("userName");

        // размеры экрана
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        width = displaymetrics.widthPixels;  // deprecated
        height = displaymetrics.heightPixels;  // deprecated

        // кнопки начала, остановки распознования, кнопка назад
        btnClear = findViewById(R.id.btnClear);
        btnPausePlay = findViewById(R.id.btnPausePlay);
        btnSave = findViewById(R.id.btnSaveTab);

        btnPlay = findViewById(R.id.btnPlay);

        frequencyText = findViewById(R.id.frequencyText);

        // картинки плея и паузы
        playImage = BitmapFactory.decodeResource(getResources(), R.drawable.microphone);
        pauseImage = BitmapFactory.decodeResource(getResources(), R.drawable.no_microphone);

//        // кнопка назад
//        ActionBar actionBar =getSupportActionBar();
//        assert actionBar != null;
//        actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        @SuppressLint("ResourceAsColor")
//        ColorDrawable colorDrawable
//                = new ColorDrawable(R.color.topMenuColor);
//        actionBar.setBackgroundDrawable(colorDrawable);

        // массив струн
        frameLayouts = new FrameLayout[6];

        strings = findViewById(R.id.strings);

        scrollView = findViewById(R.id.horizontalScroll);

        tabRec = new TabRec(this, btnClear, btnPausePlay, btnSave, btnPlay, frequencyText, strings, scrollView, userName);
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
