package com.example.notesrecognition;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.DisplayMetrics;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
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

public class TabActivity extends AppCompatActivity implements View.OnClickListener{
    boolean reco = false;

    Bitmap playImage;
    Bitmap pauseImage;

    Button btnStart;
    Button btnStop;
    ImageView btnPausePlay;
    ImageView arrowBack;
    Button btnSave;
    Button btnOpenTab;
    BarChart chart;
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

    // массив нот
    ArrayList<Note> notesFile = new ArrayList<>();
//    String notesFile = "";

    // сохранён файл или нет
    boolean save = true;

    String fileName;

    @SuppressLint({"HandlerLeak", "WrongViewCast"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_cards_layout);

        // получение названия файла
        fileName = getIntent().getExtras().getString("file");

        // размеры экрана
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        width = displaymetrics.widthPixels;  // deprecated
        height = displaymetrics.heightPixels;  // deprecated

        // кнопки начала, остановки распознования, кнопка назад
//        btnStart = findViewById(R.id.btnStart);
//        btnStop = findViewById(R.id.btnStop);
        btnPausePlay = findViewById(R.id.btnPausePlay);
        btnSave = findViewById(R.id.btnSaveTab);
        arrowBack = findViewById(R.id.arrowBack);

//        chart = findViewById(R.id.barchart);

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

//        btnStart.setOnClickListener(this);
//        btnStop.setOnClickListener(this);
        btnPausePlay.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        arrowBack.setOnClickListener(this);

        // картинки плея и паузы
        playImage = BitmapFactory.decodeResource(getResources(), R.drawable.play);
        pauseImage = BitmapFactory.decodeResource(getResources(), R.drawable.pause);


        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                // получаем ноту по частоте, переданной из детектора
                double[] spectrum = (double[]) msg.obj;

                int[] note = getNote(spectrum);

                // если распознано, то добавляем на струну
                if(note[0] >= 0 && note[1] >= 0) {
                    setNoteText(note);
                }
            };
        };

        audioReciever = new AudioReciever(handler);

        try {
            // чтение табулатуры
            readJSONFile(fileName);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

//        ArrayList <BarEntry> NoOfEmp = new ArrayList <>();
//
//        NoOfEmp.add(new BarEntry(945f, 0));
//        NoOfEmp.add(new BarEntry(1040f, 1));
//        NoOfEmp.add(new BarEntry(1133f, 2));
//        NoOfEmp.add(new BarEntry(1240f, 3));
//        NoOfEmp.add(new BarEntry(1369f, 4));
//        NoOfEmp.add(new BarEntry(1487f, 5));
//        NoOfEmp.add(new BarEntry(1501f, 6));
//        NoOfEmp.add(new BarEntry(1645f, 7));
//        NoOfEmp.add(new BarEntry(1578f, 8));
//        NoOfEmp.add(new BarEntry(1695f, 9));

//        ArrayList<String> year = new ArrayList<>();
//        String[] year = new String[NoOfEmp.size()];
//
//        year[0] = "2008";
//        year[0] = "2009";
//        year[0] = "2010";
//        year[0] = "2011";
//        year[0] = "2012";
//        year[0] = "2013";
//        year[0] = "2014";
//        year[0] = "2015";
//        year[0] = "2016";
//        year[0] = "2017";

//        year.add("2008");
//        year.add("2009");
//        year.add("2010");
//        year.add("2011");
//        year.add("2012");
//        year.add("2013");
//        year.add("2014");
//        year.add("2015");
//        year.add("2016");
//        year.add("2017");

//        BarDataSet bardataset = new BarDataSet(NoOfEmp, "No Of Employee");
//        bardataset.setStackLabels(year);
////        chart.animateY(5000);
//        BarData data = new BarData(bardataset);
//        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
//        chart.setData(data);
    }

    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.btnStart) {
//            reco = true;
//            audioReciever.start();
//        }
        switch (v.getId()) {
            case (R.id.btnPausePlay): {
                // при нажатии на кнопку старт или пауза
                if (reco) {
                    // если пауза
                    reco = false;
                    btnPausePlay.setImageBitmap(playImage);
                    audioReciever.stop();
                } else {
                    // если старт
                    reco = true;
                    btnPausePlay.setImageBitmap(pauseImage);
                    audioReciever.start();
                }
                break;
            }

            case (R.id.btnSaveTab): {
                // при нажатии сохранить
                if(reco) {
                    Toast.makeText(this, "Остановите запись", Toast.LENGTH_LONG).show();
                } else {
                    writeFile(fileName);
                }
                break;
            }

            case (R.id.arrowBack): {
                // при нажатии стрелочки назад
                reco = false;
                btnPausePlay.setImageBitmap(playImage);
                audioReciever.stop();

                if (!save) {
                    createDialog();
                } else {
                    Intent in = new Intent(this, MainActivity.class);
                    startActivity(in);
                }
            }
        }
//        if (v.getId() == R.id.btnStop) {
//            reco = false;
//            audioReciever.stop();
//            stopRec();
//        }
    }

    @Override
    public void onBackPressed() {
        reco = false;
        btnPausePlay.setImageBitmap(playImage);
        audioReciever.stop();

        if (!save) {
            createDialog();
        } else {
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
        }
    }

    private int[] getNote(double[] spectrum) {
        // получение ноты

//        Runnable runnable = () -> {
//            String data = "\n";
//            float f = 2f;
//            ArrayList <BarEntry> entries = new ArrayList <>();
//
//            for(double v : spectrum) {
////            Log.d("d", v+"");
//                entries.add(new BarEntry(f, (float) v));
//                data += String.valueOf(v);
//                data += "\n";
//                f += 2f;
//            }
//
//            BarDataSet bardataset = new BarDataSet(entries, "No Of Employee");
//            BarData barData = new BarData(bardataset);
//            bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
////
//            chart.post(() -> chart.setData(barData));
//
////        writeFile(data);
//        };
//        Thread thread = new Thread(runnable);
//        thread.start();
//        Runnable runnable = () -> {
//            String data = "\n";
//            for(double v : spectrum) {
//                data += String.valueOf(v);
//                data += "\n";
//            }
////            Log.d("d", data);
//            writeFile(data);
//        };
//        Thread thread = new Thread(runnable);
//        thread.start();

        /* find the peak magnitude and it's index */
        double maxMag = Double.NEGATIVE_INFINITY;
        int maxInd = -1;
        for (int i = 0; i < spectrum.length / 2; ++i) {
            double re = spectrum[2 * i];
            double im = spectrum[2 * i + 1];
            double mag = Math.sqrt(re * re + im * im);
//            double mag = a[i];
            if (mag > maxMag) {
                maxMag = mag;
                maxInd = i;
            }
        }
        int freq = (int) 8000 * maxInd / (spectrum.length / 2);

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
            Log.d("count", count+"");
            if (count > 1) {
                for (int j = 1; j < count; j++) {
                    frameLayout.removeViewAt(1);
                }
            }
        }
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void setNoteText(int[] note) {
        // note[0]  - струна
        // note[1] - лад

        save = false;

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

//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(frameLayouts[note[0]].getWidth() + 60, frameLayouts[note[0]].getHeight());
//        if (distanceNote + 30 >= width) {
//            for (FrameLayout frameLayout : frameLayouts) {
//                frameLayout.setLayoutParams(layoutParams);
//            }
//        }
        scrollView.scrollTo(frameLayouts[note[0]].getWidth(), 0);
        distanceNote += maxDistanceNote;
        notesFile.add(new Note(note[0], note[1]));
    }

    private void stopRec() {
        distanceNote = 150;
        deleteNoteViews();
        notesFile.clear();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, frameLayouts[0].getHeight());
        for (FrameLayout frameLayout : frameLayouts) {
            frameLayout.setLayoutParams(layoutParams);
        }
        scrollView.scrollTo(0, 0);
    }

    private void writeFile(String name) {
        // запись табулатуры
        try {
            File file = new File(getFilesDir(), name);

            // создание файла
            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));

            // начало главного массива
            jsonWriter.beginArray();

            for(Note note: notesFile) {
                // создание объекта ноты в json
                jsonWriter.beginObject();

                jsonWriter.name("String").value(note.string);
                jsonWriter.name("Fret").value(note.fret);

                jsonWriter.endObject();
            }
            jsonWriter.endArray();
            jsonWriter.close();

            Log.d("file", "Файл записан");
            save = true;
            Toast.makeText(this, "Файл записан", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readJSONFile(String name) throws IOException, JSONException {
        // чтение json файла

        String jsonText = readFile(getFilesDir() + "/" + name);

        // создаём json объект
        JSONArray obj = new JSONArray(jsonText);

        for (int i = 0; i < obj.length(); i++) {
            int[] note = new int[2];

            JSONObject noteObj = obj.getJSONObject(i);
            note[0] = noteObj.getInt("String");
            note[1] = noteObj.getInt("Fret");

            // устанавливаем ноту на струну
            setNoteText(note);
        }
    }

    private String readFile(String name) throws IOException {
        File file = new File(name);
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb= new StringBuilder();
        String s;
        while((  s = br.readLine())!=null) {
            sb.append(s);
            sb.append("\n");
        }
        br.close();
        return sb.toString();
    }

    private void createDialog() {
        // при выходе без сохранения
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Сохранить изменения?");

        builder.setNegativeButton("Нет", (dialog, which) -> {
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
        });
        builder.setPositiveButton("Да", (dialog, which) -> {
            writeFile(fileName);
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
        });

        builder.show();
    }
}
