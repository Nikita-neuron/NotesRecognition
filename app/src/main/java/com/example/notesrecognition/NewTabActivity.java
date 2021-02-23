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
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

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

public class NewTabActivity extends AppCompatActivity implements View.OnClickListener{
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

    LinearLayout strings;

    AudioReciever audioReciever;
    Handler handler;

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

        frequencyText = findViewById(R.id.frequencyText);

        // картинки плея и паузы
        playImage = BitmapFactory.decodeResource(getResources(), R.drawable.microphone);
        pauseImage = BitmapFactory.decodeResource(getResources(), R.drawable.no_microphone);

        guitarGriff = BitmapFactory.decodeResource(getResources(), R.drawable.strings);

        // кнопка назад
        ActionBar actionBar =getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

//        chart = findViewById(R.id.barchart);

        // массив струн
        frameLayouts = new FrameLayout[6];

        // добавляем струны
//        frameLayouts[0] = findViewById(R.id.line_1);
//        frameLayouts[1] = findViewById(R.id.line_2);
//        frameLayouts[2] = findViewById(R.id.line_3);
//        frameLayouts[3] = findViewById(R.id.line_4);
//        frameLayouts[4] = findViewById(R.id.line_5);
//        frameLayouts[5] = findViewById(R.id.line_6);
        strings = findViewById(R.id.strings);

        scrollView = findViewById(R.id.horizontalScroll);

        btnPausePlay.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnClear.setOnClickListener(this);

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

        setBeginGriff();

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                reco = false;
                btnPausePlay.setImageBitmap(pauseImage);
                audioReciever.stop();

                if (!save) {
                    createDialog("back", "");
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
    public void onClick(View v) {
//        if (v.getId() == R.id.btnStart) {
//            reco = true;
//            audioReciever.start();
//        }
//        if (v.getId() == R.id.btnPause) {
//            reco = false;
//            audioReciever.stop();
//        }
//        if (v.getId() == R.id.btnStop) {
//            reco = false;
//            audioReciever.stop();
//            stopRec();
//        }

        switch (v.getId()) {
            case (R.id.btnPausePlay): {
                // при нажатии на кнопку старт или пауза
                if (reco) {
                    // если пауза
                    reco = false;
                    btnPausePlay.setImageBitmap(pauseImage);
                    audioReciever.stop();
                } else {
                    // если старт
                    reco = true;
                    btnPausePlay.setImageBitmap(playImage);
                    audioReciever.start();
                }
                break;
            }

            case (R.id.btnSaveTab): {
                // при нажатии на сохранить
                if(reco) {
                    Toast.makeText(this, "Остановите запись", Toast.LENGTH_LONG).show();
                } else {
                    createDialog("writeFile", "");
                }
                break;
            }

            case (R.id.btnClear): {
                if(reco) {
                    Toast.makeText(this, "Остановите запись", Toast.LENGTH_LONG).show();
                } else {
                    btnPausePlay.setImageBitmap(pauseImage);
                    audioReciever.stop();
                    stopRec();
                }
                break;
            }
        }
//        if (v.getId() == R.id.btnOpenTab) {
//            if(reco) {
//                Toast.makeText(this, "Остановите запись", Toast.LENGTH_LONG).show();
//            } else {
//                createDialog("readFile", "");
//            }
//        }
    }

    @Override
    public void onBackPressed() {
        reco = false;
        btnPausePlay.setImageBitmap(pauseImage);
        audioReciever.stop();

        if (!save) {
            createDialog("back", "");
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
//        runOnUiThread(runnable);

        /* find the peak magnitude and it's index */
        double maxMag = Double.NEGATIVE_INFINITY;
        int maxInd = -1;
        for (int i = 0; i < spectrum.length / 2; ++i) {
            double re = spectrum[2 * i];
            double im = spectrum[2 * i + 1];
            double mag = Math.sqrt(re * re + im * im);
//            double mag = a[i];
            // ограничить по частоте                        <--
//            Log.d("freq", "mag: " + mag + " freq: " + (int) 12000 * i / (spectrum.length / 2));
            if (mag > maxMag) {
                maxMag = mag;
                maxInd = i;
            }
        }
        int freq = (int) 24000 * maxInd / (spectrum.length / 2);
        frequencyText.setText("Frequency: " + freq);
//        System.out.println(freq+"");

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

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void setNoteText(int[] note) {
        // note[0]  - струна
        // note[1] - лад

        save = false;

        // добавление ноты на струну
        // утановление margin для ноты
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(80, notesMargin[note[0]],0,0);

        textView = new TextView(this);
        textView.setBackground(getDrawable(R.drawable.rectangle_2));
        textView.setText(note[1]+"");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(20);
        textView.setPadding(0, 0, 4, 4);
        textView.setLayoutParams(params);
        textView.setTag((note[0]+1) + "_" + (note[1] + 1));

        if (countBegGriff != countCurrGriff) {
            FrameLayout griff = (FrameLayout) strings.getChildAt(countCurrGriff);
            griff.addView(textView);
            countCurrGriff ++;
        } else {
            FrameLayout griff = drawGriff();
            griff.addView(textView);
            strings.addView(griff);
        }


        scrollView.scrollTo(strings.getWidth(), 0);
        notesFile.add(new Note(note[0], note[1]));
    }

    private FrameLayout drawGriff() {
        FrameLayout frameLayout = new FrameLayout(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(203, LinearLayout.LayoutParams.MATCH_PARENT);

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(guitarGriff);
        imageView.setLayoutParams(layoutParams);

        frameLayout.addView(imageView);

        return frameLayout;
    }

    private void setBeginGriff() {
        countBegGriff = width / 203;

        for (int i = 0; i < countBegGriff; i++) {
            FrameLayout griff = drawGriff();
            strings.addView(griff);
        }
    }

    private void stopRec() {
        distanceNote = 150;
        strings.removeAllViews();
        notesFile.clear();

        scrollView.scrollTo(0, 0);
        setBeginGriff();
        countCurrGriff = 0;
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

            save = true;
            Toast.makeText(this, "Файл записан", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDialog(String nameDialog, String lastName) {
        switch (nameDialog){
            case "writeFile": {
                // диалоговое окно для записи файла
                String[] files = fileList();

                EditText editText = new EditText(this);
                editText.setText(lastName);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage("Укажите название табулатуры");
                builder.setView(editText);
                builder.setNegativeButton("Отмена", (dialog, which) -> { });
                builder.setPositiveButton("OK", (dialog, which) -> {
                    String name = editText.getText().toString();
                    boolean indName = true;

                    for(String fileName: files) {
                        if(fileName.equals(name + ".json")) {
                            createDialog("rewriteFile", name);
                            indName = false;
                        }
                    }

                    if(indName) {
                        writeFile(name + ".json");
                    }
                });

                builder.show();
                break;
            }
            case "rewriteFile": {
                // диалоговое окно для перезаписи файла
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Данный файл уже существует");
                builder.setMessage("Вы хотите его перезаписать?");
                builder.setNegativeButton("Нет", (dialog, which) -> {
                    createDialog("writeFile", lastName);
                });
                builder.setPositiveButton("Да", (dialog, which) -> {
                    writeFile(lastName + ".json");
                });

                builder.show();
                break;
            }
            case "back": {
                // диалоговое окно при выходе без сохранения
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Сохранить изменения?");

                builder.setNegativeButton("Нет", (dialog, which) -> {
                    Intent in = new Intent(this, MainActivity.class);
                    startActivity(in);
                });
                builder.setPositiveButton("Да", (dialog, which) -> {
                    createDialog("writeFile", "");
                    Intent in = new Intent(this, MainActivity.class);
                    startActivity(in);
                });

                builder.show();
                break;
            }
        }
    }
}
