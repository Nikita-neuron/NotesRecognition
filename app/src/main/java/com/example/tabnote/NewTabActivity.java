package com.example.tabnote;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.JsonWriter;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class NewTabActivity extends AppCompatActivity{
    boolean reco = false;

    Bitmap playImage;
    Bitmap pauseImage;

    Bitmap guitarGriff;

    Button btnStart;
    Button btnClear;
    ImageView btnPausePlay;
    ImageView arrowBack;
    ImageView btnSave;
    Button btnOpenTab;
    BarChart chart;
    FrameLayout[] frameLayouts;
    TextView textView;
    HorizontalScrollView scrollView;
    TextView frequencyText;
    Button btnPlay;

    LinearLayout strings;

    AudioReciever audioReciever;
    Handler handler;

    PlayNotes playNotes;

    int[] notesMargin = new int[] {
            6, 110, 200, 280, 360, 450
    };
    // начальное количество картинок грифа
    int countBegGriff;
    // количество нарисованных сначало грифов
    int countCurrGriff = 0;

    // ноты
    char[] notes = new char[]{'E', 'B', 'G', 'D', 'A', 'E'};

    // частоты
    // по вертикале - струны, по горизонтале - лады
    int[][] frequency = new int[][]{
            // 0  1    2    3    4    5    6    7    8    9    10   11   12
            {329, 349, 369, 391, 415, 440, 466, 494, 523, 554, 587, 622, 659},
            {246, 261, 277, 293, 311, 329, 349, 370, 392, 415, 440, 466, 494},
            {196, 207, 220, 233, 246, 261, 277, 294, 311, 329, 349, 370, 392},
            {146, 155, 164, 174, 185, 196, 208, 220, 233, 247, 262, 277, 294},
            {110, 116, 123, 130, 138, 146, 156, 165, 175, 185, 196, 208, 220},
            {82,  87,  92,  98,  103, 110, 117, 123, 131, 139, 147, 156, 165}
    };

    // погрешность
    public int er = 3;
    // максимальное расстояние между нотами на струне
    public int maxDistanceNote = 100;
    // начальное расстояние
    public int distanceNote = 150;

    public int width;
    public int height;

    private boolean playNote = false;

    // массив нот
    ArrayList<Note> notesFile = new ArrayList<>();

    // сохранён файл или нет
    boolean save = true;
    TabRec tabRec;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_cards_layout);

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

//        guitarGriff = BitmapFactory.decodeResource(getResources(), R.drawable.strings);

        // кнопка назад
        ActionBar actionBar =getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // массив струн
        frameLayouts = new FrameLayout[6];

        strings = findViewById(R.id.strings);

        scrollView = findViewById(R.id.horizontalScroll);

        tabRec = new TabRec(this, btnClear, btnPausePlay, btnSave, btnPlay, frequencyText, strings, scrollView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // стрелочка назад в верхнем меню
        switch (item.getItemId()) {
            case android.R.id.home:
                tabRec.reco = false;
                btnPausePlay.setImageBitmap(pauseImage);
                try {
                    tabRec.audioReciever.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!save) {
                    tabRec.createDialog("back", "");
                } else {
                    Intent in = new Intent(this, MainActivity.class);
                    startActivity(in);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        tabRec.reco = false;
        btnPausePlay.setImageBitmap(pauseImage);
        try {
            tabRec.audioReciever.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!save) {
            tabRec.createDialog("back", "");
        } else {
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
        }
    }
}
