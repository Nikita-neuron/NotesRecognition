package com.example.tabnote;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TabRec implements View.OnClickListener{
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

    Context context;

    @SuppressLint("HandlerLeak")
    public TabRec(Context context, View btnClear, View btnPausePlay, View btnSave, View btnPlay,
                  View frequencyText, View strings, View scrollView) {
        this.context = context;

        // размеры экрана
        DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
        width = displaymetrics.widthPixels;  // deprecated
        height = displaymetrics.heightPixels;  // deprecated

        // кнопки начала, остановки распознования, кнопка назад
        this.btnClear = (Button) btnClear;
        this.btnPausePlay = (ImageView) btnPausePlay;
        this.btnSave = (ImageView) btnSave;

        this.btnPlay = (Button) btnPlay;

        this.frequencyText = (TextView) frequencyText;

        // картинки плея и паузы
        playImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.microphone);
        pauseImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_microphone);

        guitarGriff = BitmapFactory.decodeResource(context.getResources(), R.drawable.strings);

        this.strings = (LinearLayout) strings;

        // массив струн
        frameLayouts = new FrameLayout[6];

        this.scrollView = (HorizontalScrollView) scrollView;

        this.btnPausePlay.setOnClickListener(this);
        this.btnSave.setOnClickListener(this);
        this.btnClear.setOnClickListener(this);
        this.btnPlay.setOnClickListener(this);

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                // получаем ноту по частоте, переданной из детектора
                LinkedHashMap<Integer, Integer> spectrum = (LinkedHashMap<Integer, Integer>) msg.obj;

                int[] note = getNote(spectrum);

                // если распознано, то добавляем на струну
                if(note[0] >= 0 && note[1] >= 0) {
                    setNoteText(note);
                }
            };
        };

        audioReciever = new AudioReciever(handler);

        playNotes = new PlayNotes(context, notesFile, this.strings);

        setBeginGriff();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.btnPausePlay): {
                // при нажатии на кнопку старт или пауза
                if (!playNote) {
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
                } else {
                    Toast.makeText(context, "Остановите воспроизведение", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case (R.id.btnSaveTab): {
                // при нажатии на сохранить
                if (!playNote) {
                    if (reco) {
                        Toast.makeText(context, "Остановите запись", Toast.LENGTH_LONG).show();
                    } else {
                        createDialog("writeFile", "");
                    }
                } else {
                    Toast.makeText(context, "Остановите воспроизведение", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case (R.id.btnClear): {
                if (!playNote) {
                    if (reco) {
                        Toast.makeText(context, "Остановите запись", Toast.LENGTH_LONG).show();
                    } else {
                        btnPausePlay.setImageBitmap(pauseImage);
                        audioReciever.stop();
                        stopRec();
                    }
                } else {
                    Toast.makeText(context, "Остановите воспроизведение", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case (R.id.btnPlay): {
                System.out.println("playNote: " + playNote);
                if (!playNote) {
                    if (reco) {
                        Toast.makeText(context, "Остановите запись", Toast.LENGTH_LONG).show();
                    } else {
                        playNote();
                    }
                } else {
                    playNotes.pause();
                }
                break;
            }
        }
    }

    private int[] getNote(LinkedHashMap<Integer, Integer> spectrum) {
        // получение ноты

        /* find the peak magnitude and it's index */
        /*
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

         */

        double freq = 0;
//        freq = getMaxIndex(spectrum) *24000 / spectrum.size();
        freq = getMaxIndex(spectrum);
        frequencyText.setText("Frequency: " + freq);

        System.out.println("result freq: " + freq);

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
//        return (int) Math.round(f);
//        return (int) f;
        // если не совпадает, то возвращаем -1
        return new int[] {-1, -1};
    }

    public double getMaxIndex(LinkedHashMap spectrum) {
        double fr = 0;
        double maxx = 0;

        Set set = spectrum.entrySet();
        for (Object o : set) {
            Map.Entry item = (Map.Entry) o;
            int freq = (int) item.getKey();
            int val = (int) item.getValue();

            if (freq < 1500) {
                if (val > maxx) {
                    maxx = val;
                    fr = freq;
                }
            }
        }
        return fr;
    }

    private void playNote() {
        playNotes.setNotes(notesFile, strings);
        playNote = true;
        playNotes.initPlay();
        playNotes.play();

        new Thread(() ->{
            while (playNote) {
                playNote = playNotes.playNote;
            }
        }).start();
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void setNoteText(int[] note) {
        // note[0]  - струна
        // note[1] - лад

        save = false;

        // добавление ноты на струну
        // утановление margin для ноты
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams layoutPlayParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(layoutPlayParams);
        linearLayout.setBackgroundColor(Color.argb(0, 106, 161, 71));

        params.setMargins(80, notesMargin[note[0]],0,0);

        textView = new TextView(context);
        textView.setBackground(context.getDrawable(R.drawable.rectangle_2));
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
            griff.addView(linearLayout);
            countCurrGriff ++;
        } else {
            FrameLayout griff = drawGriff();
            griff.addView(textView);
            griff.addView(linearLayout);
            strings.addView(griff);
        }

        scrollView.scrollTo(strings.getWidth(), 0);
        notesFile.add(new Note(note[0], note[1]));
    }

    private FrameLayout drawGriff() {
        FrameLayout frameLayout = new FrameLayout(context);

        LinearLayout.LayoutParams layoutImageParams = new LinearLayout.LayoutParams(203, LinearLayout.LayoutParams.MATCH_PARENT);

        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(guitarGriff);
        imageView.setLayoutParams(layoutImageParams);

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
            File file = new File(context.getFilesDir(), name);

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
            Toast.makeText(context, "Файл записан", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readJSONFile(String name) throws IOException, JSONException {
        // чтение json файла

        String jsonText = readFile(context.getFilesDir() + "/" + name);

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

    public void createDialog(String nameDialog, String lastName) {
        switch (nameDialog){
            case "writeFile": {
                // диалоговое окно для записи файла
                String[] files = context.fileList();

                EditText editText = new EditText(context);
                editText.setText(lastName);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

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
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

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
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Сохранить изменения?");

                builder.setNegativeButton("Нет", (dialog, which) -> {
                    Intent in = new Intent(context, MainActivity.class);
                    context.startActivity(in);
                });
                builder.setPositiveButton("Да", (dialog, which) -> {
                    createDialog("writeFile", "");
                    Intent in = new Intent(context, MainActivity.class);
                    context.startActivity(in);
                });

                builder.show();
                break;
            }
        }
    }
}
