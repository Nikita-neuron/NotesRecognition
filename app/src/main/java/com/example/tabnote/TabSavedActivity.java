package com.example.tabnote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.JsonWriter;
import android.util.Log;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TabSavedActivity extends AppCompatActivity{
    boolean reco = false;

    Bitmap playImage;
    Bitmap pauseImage;

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

    AudioReciever audioReciever;
    Handler handler;

    // ноты
    char[] notes = new char[]{'E', 'B', 'G', 'D', 'A', 'E'};

    // частоты
    // по вертикале - струны, по горизонтале - лады
    int[][] frequency = new int[][]{
            {329, 349, 369, 391, 415, 440},
            {246, 261, 277, 293, 311, 329},
            {196, 207, 220, 233, 246, 261},
            {146, 155, 164, 174, 185, 196},
            {110, 116, 123, 130, 138, 146},
            {82, 87, 92, 98, 103, 110}
    };

    // погрешность
    public int er = 3;
    // максимальное расстояние между нотами на струне
    public int maxDistanceNote = 100;
    // начальное расстояние
    public int distanceNote = 150;

    public int width;
    public int height;

    // массив нот
    ArrayList<Note> notesFile = new ArrayList<>();
//    String notesFile = "";

    // сохранён файл или нет
    boolean save = true;

    String fileName;

    Button btnPlay;
    LinearLayout strings;

    TabRec tabRec;

    @SuppressLint({"HandlerLeak", "WrongViewCast"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_cards_layout);

        // получение названия файла
        fileName = getIntent().getExtras().getString("file");

        // кнопки начала, остановки распознования, кнопка назад
        btnClear = findViewById(R.id.btnClear);
        btnPausePlay = findViewById(R.id.btnPausePlay);
        btnSave = findViewById(R.id.btnSaveTab);

        btnPlay = findViewById(R.id.btnPlay);

        frequencyText = findViewById(R.id.frequencyText);

        scrollView = findViewById(R.id.horizontalScroll);

        frequencyText = findViewById(R.id.frequencyText);

        strings = findViewById(R.id.strings);

        // кнопка назад
        ActionBar actionBar =getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // картинки плея и паузы
        playImage = BitmapFactory.decodeResource(getResources(), R.drawable.microphone);
        pauseImage = BitmapFactory.decodeResource(getResources(), R.drawable.no_microphone);

        tabRec = new TabRec(this, btnClear, btnPausePlay, btnSave, btnPlay, frequencyText, strings, scrollView);

        try {
            // чтение табулатуры
            tabRec.readJSONFile(fileName);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // стрелочка назад в верхнем меню
        switch (item.getItemId()) {
            case android.R.id.home:
                tabRec.reco = false;
                btnPausePlay.setImageBitmap(pauseImage);
                tabRec.audioReciever.stop();

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
        tabRec.audioReciever.stop();

        if (!save) {
            tabRec.createDialog("back", "");
        } else {
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
        }
    }
}
