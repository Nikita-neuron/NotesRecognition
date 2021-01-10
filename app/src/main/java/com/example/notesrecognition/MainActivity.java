package com.example.notesrecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnStart;
    Button btnStop;
    Button btnPause;
    FrameLayout[] frameLayouts;
    TextView textView;
    HorizontalScrollView scrollView;

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


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        width = displaymetrics.widthPixels;  // deprecated
        height = displaymetrics.heightPixels;  // deprecated

        // кнопки начала и остановки распознования
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnPause = findViewById(R.id.btnPause);
        // массив струн
        frameLayouts = new FrameLayout[6];

        // добавляем струны
        frameLayouts[0] = findViewById(R.id.line_1);
        frameLayouts[1] = findViewById(R.id.line_2);
        frameLayouts[2] = findViewById(R.id.line_3);
        frameLayouts[3] = findViewById(R.id.line_4);
        frameLayouts[4] = findViewById(R.id.line_5);
        frameLayouts[5] = findViewById(R.id.line_6);

        scrollView = findViewById(R.id.horizontalScroll);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnPause.setOnClickListener(this);


        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                // получаем ноту по частоте, переданной из детектора
                int[] note = getNote(msg.what);

                // если распознано, то добавляем на струну
                if(note[0] >= 0 && note[1] >= 0) {
                    setNoteText(note);
                }
            };
        };

        audioReciever = new AudioReciever(handler);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnStart) {
            audioReciever.start();
        }
        if (v.getId() == R.id.btnPause) {
            audioReciever.stop();
        }
        if (v.getId() == R.id.btnStop) {
            audioReciever.stop();
            stopRec();
        }
    }

    private int[] getNote(int freq) {
        for(int i = 0; i<frequency.length; i++) {
            for(int j = 0; j<frequency[i].length; j++) {
                // если частота совпадает с погрешностью, то возвращаем
                // i - струна
                // j - лад
                if(freq >= frequency[i][j] - er && freq <= frequency[i][j] + er) {
                    return new int[] {i, j};
                }
            }
        }
        // если не совпадает, то возвращаем -1
        return new int[] {-1, -1};
    }

    private void deleteNoteViews() {
        // удаление нот со струн
        for (FrameLayout frameLayout : frameLayouts) {
            int count = frameLayout.getChildCount();
            if (count > 1) {
                for (int j = 1; j < count; j++) {
                    frameLayout.removeViewAt(1);
                }
            }
        }
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void setNoteText(int[] note) {
        // добавление ноты на струну
        // утановление margin для ноты
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(distanceNote,0,0,0);

        textView = new TextView(this);
        textView.setBackground(getDrawable(R.drawable.rectangle_2));
        textView.setText(note[1]+"");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(20);
        textView.setPadding(0, 0, 2, 0);
        textView.setLayoutParams(params);
        textView.setTag(R.id.strings, (note[0]+1) + "_" + (note[1] + 1));

        frameLayouts[note[0]].addView(textView);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(frameLayouts[note[0]].getWidth() + 60, frameLayouts[note[0]].getHeight());
        if (distanceNote + 30 >= width) {
            for (FrameLayout frameLayout : frameLayouts) {
                frameLayout.setLayoutParams(layoutParams);
            }
        }
        scrollView.scrollTo(frameLayouts[note[0]].getWidth(), 0);
        distanceNote += maxDistanceNote;
    }

    private void stopRec() {
        distanceNote = 150;
        deleteNoteViews();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, frameLayouts[0].getHeight());
        for (FrameLayout frameLayout : frameLayouts) {
            frameLayout.setLayoutParams(layoutParams);
        }
        scrollView.scrollTo(0, 0);
    }
}