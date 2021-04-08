package com.example.tabnote;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.util.DisplayMetrics;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TabRec implements View.OnClickListener{
    boolean reco = false;

    Bitmap playImage;
    Bitmap pauseImage;
    Bitmap guitarGriff;
    Button btnClear;
    ImageView btnPausePlay;
    ImageView btnSave;
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
            40, 120, 220, 300, 380, 470
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

    DBManager dbManager;

    ArrayList<Integer> countPickesSpectrums = new ArrayList<>();

    private int ind = 0;

    @SuppressLint("HandlerLeak")
    public TabRec(Context context, View btnClear, View btnPausePlay, View btnSave, View btnPlay,
                  View frequencyText, View strings, View scrollView) {
        this.context = context;

        dbManager = DBManager.getInstance(context);

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
//                double[] spectrum = (double[]) msg.obj;

                int[] note = getNote(spectrum);

                // если распознано, то добавляем на струну
                if(note[0] >= 0 && note[1] >= 0) {
                    setNoteText(note, "standard", null, 0);
                }
            };
        };

        audioReciever = new AudioReciever(handler, context);

        playNotes = new PlayNotes(context, notesFile, this.strings);

        setBeginGriff();

//        for (int i = 0; i < 6; i++) {
//            setNoteText(new int[] {i, 0}, "standard", null, 0);
//        }
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(R.string.noteValue) != null) {
            int type = (int) v.getTag(R.string.noteType);
            if (type == R.string.noteTypeStandard) {
                updateNoteText(v);
            }
            else if (type == R.string.noteTypeUpdate) {
                setUpdateNoteText(v);
            }
        }
        switch (v.getId()) {
            case (R.id.btnPausePlay): {
                // при нажатии на кнопку старт или пауза
                if (!playNote) {
                    if (reco) {
                        // если пауза
                        reco = false;
                        btnPausePlay.setImageBitmap(pauseImage);
                        try {
                            audioReciever.stop();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
                // если нажата кнопка очистки
                if (!playNote) {
                    if (reco) {
                        Toast.makeText(context, "Остановите запись", Toast.LENGTH_LONG).show();
                    } else {
                        btnPausePlay.setImageBitmap(pauseImage);
                        try {
                            audioReciever.stop();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        stopRec();
                    }
                } else {
                    Toast.makeText(context, "Остановите воспроизведение", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case (R.id.btnPlay): {
                // если нажата кнопка воспроизведения
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

    private int[] getNote(LinkedHashMap<Integer, Integer> spectrumBig) {
//    private int[] getNote(double[] spectrum) {
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
        System.out.println(freq+"");

        new Thread(){
            @Override
            public void run() {
                try {
                    FileOutputStream out = context.openFileOutput("spectrum" + freq + ".txt", Context.MODE_PRIVATE);
                    PrintWriter write = new PrintWriter(out);
                    for (int i = 0; i < spectrum.length / 2; ++i) {
                        double re = spectrum[2 * i];
                        double im = spectrum[2 * i + 1];
                        double mag = Math.sqrt(re * re + im * im);

                        double freq = 24000 * i / (spectrum.length / 2);

                        write.println(freq + " " + mag);
                    }
                    write.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

         */


        LinkedHashMap<Integer, Integer> spectrum = new LinkedHashMap<>();
        for (Integer key: spectrumBig.keySet()) {
            if (key >= 1001) break;
            spectrum.put(key, spectrumBig.get(key));
        }

        double freq = 0;
        freq = getMaxIndex(spectrum);
        frequencyText.setText("Frequency: " + freq);
        // входной порог в буфер почистить его
        // эквалайзер
        // поиграться со временем между буферами (10)
        // скрипка
        // посчитать частоты по формуле

        //....
        // нули между отсчётами в буфере

        System.out.println("result freq: " + freq);
        System.out.println("length of spectrum: " + spectrum.size());

        LinkedHashMap<Integer, Integer> finalSpectrum = spectrum;
        double finalFreq = freq;
        new Thread(){
            @Override
            public void run() {
                try {
                    FileOutputStream out = context.openFileOutput("spectrum" + finalFreq + ".txt", Context.MODE_PRIVATE);
                    PrintWriter write = new PrintWriter(out);
                    for (Integer key: spectrum.keySet()) {
                        write.println(key + " " + spectrum.get(key));
                    }
                    write.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        for(int i = 0; i<frequency.length; i++) {
            for(int j = 0; j<frequency[i].length; j++) {
                // если частота совпадает с погрешностью, то возвращаем
                // i - струна
                // j - лад
                double crat = freq / frequency[i][j];
                double crat_freq = freq / crat;
//                System.out.println("crat: " + crat);
                if(freq >= frequency[i][j] - er && freq <= frequency[i][j] + er) {
                    ind++;

                    return new int[]{i, j};
                }
                else {
                    for (int k = 2; k <= 10; k++) {
                        if (freq / k == frequency[i][j]) {
                            System.out.println("crat_freq: " + crat_freq);
                            return new int[] {i, j};
                        }
                    }
                }
            }
        }
//        return (int) Math.round(f);
//        return (int) f

        // если не совпадает, то возвращаем -1
        return new int[] {-1, -1};
    }

    public double getMaxIndex(LinkedHashMap<Integer, Integer> spectrum) {
        double fr = 0;
        double maxx = 0;
        int n_pickes = 0;

        ArrayList<Integer> arrayList = new ArrayList<>();

//        int ind = 0;
//
//        for (Integer val: spectrum.values()) {
//            if (ind > spectrum.size() / 2) break;
//            if (val > maxx) {
//                maxx = val;
//                fr = ind;
//                n_pickes ++;
//            }
//            ind ++;
//            if (val > 200000) {
//                arrayList.add(ind);
//            }
//        }
//        fr = fr * 24000 / spectrum.size();

        Set set = spectrum.entrySet();
        for (Object o : set) {
            Map.Entry item = (Map.Entry) o;
            int freq = (int) item.getKey();
            int val = (int) item.getValue();

            if (freq < 1500) {
                if (val > maxx) {
                    maxx = val;
                    fr = freq;
                    n_pickes ++;
                }
            }

            if (val > 200000) {
                arrayList.add(freq);
            }
//            if (n_pickes > 25 || n_pickes < 15) break;
        }


        System.out.println("pickes: " + n_pickes);
        System.out.println(arrayList + " > 500000");
//        boolean filterPickes = n_pickes > 25 || n_pickes < 15;
        // 16 9 7 6
        if (countPickesSpectrums.size() > 0) {
            if (n_pickes < countPickesSpectrums.get(countPickesSpectrums.size()-1)) {
                countPickesSpectrums.add(n_pickes);
                return 0;
            } else {
                countPickesSpectrums.clear();
                return fr;
            }
        } else {
            countPickesSpectrums.add(n_pickes);
            return fr;
        }
//        return 0;
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
    private void setNoteText(int[] note, String type, FrameLayout parent, int noteIndex) {
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
        textView.setText(note[1]+"");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(20);
        textView.setPadding(0, 0, 4, 4);
        textView.setLayoutParams(params);
        textView.setTag(R.string.noteValue, (note[0]) + "_" + (note[1]));

        switch (type) {
            case "standard":
                notesFile.add(new Note(note[0], note[1]));

                textView.setBackground(context.getDrawable(R.drawable.note_standard));
                textView.setTag(R.string.noteType, R.string.noteTypeStandard);
                textView.setTag(R.string.noteID, notesFile.size() - 1);
                textView.setOnClickListener(this);

                if (countBegGriff != countCurrGriff) {
                    FrameLayout griff = (FrameLayout) strings.getChildAt(countCurrGriff);
                    griff.addView(textView);
                    griff.addView(linearLayout);
                    countCurrGriff++;
                } else {
                    FrameLayout griff = drawGriff();
                    griff.addView(textView);
                    griff.addView(linearLayout);
                    strings.addView(griff);
                }

                scrollView.scrollTo(strings.getWidth(), 0);
                break;
            case "update":
                textView.setBackground(context.getDrawable(R.drawable.note_update));
                textView.setTag(R.string.noteType, R.string.noteTypeUpdate);
                textView.setTag(R.string.noteID, noteIndex);
                textView.setOnClickListener(this);

                parent.addView(textView);
                break;
            case "setUpdate":
                notesFile.set(noteIndex, new Note(note[0], note[1]));

                textView.setBackground(context.getDrawable(R.drawable.note_standard));
                textView.setTag(R.string.noteType, R.string.noteTypeStandard);
                textView.setTag(R.string.noteID, noteIndex);
                textView.setOnClickListener(this);

                parent.addView(textView);
                parent.addView(linearLayout);
                break;
        }
    }

    private FrameLayout drawGriff() {
        // создание layout грифа
        FrameLayout frameLayout = new FrameLayout(context);

        LinearLayout.LayoutParams layoutImageParams = new LinearLayout.LayoutParams(203, LinearLayout.LayoutParams.MATCH_PARENT);

        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(guitarGriff);
        imageView.setLayoutParams(layoutImageParams);

        frameLayout.addView(imageView);

        return frameLayout;
    }

    private void setBeginGriff() {
        // создание первоначального грифа
        countBegGriff = width / 203;

        for (int i = 0; i < countBegGriff; i++) {
            FrameLayout griff = drawGriff();
            strings.addView(griff);
        }
    }

    private void updateNoteText(View note) {
        // список нот для изменения
        FrameLayout parent = (FrameLayout) note.getParent();

        String tag = (String) note.getTag(R.string.noteValue);
        int noteIndex = (int) note.getTag(R.string.noteID);
        String[] noteTag = tag.split("_");
        int noteString = Integer.parseInt(noteTag[0]);
        int noteFret = Integer.parseInt(noteTag[1]);

        while (parent.getChildCount() > 1) {
            parent.removeViewAt(1);
        }

        for (int i = 0; i < notesMargin.length; i++) {
            setNoteText(new int[]{i, noteFret}, "update", parent, noteIndex);
        }
    }

    private void setUpdateNoteText(View note) {
        // установление новой ноты
        FrameLayout parent = (FrameLayout) note.getParent();

        String tag = (String) note.getTag(R.string.noteValue);
        int noteIndex = (int) note.getTag(R.string.noteID);
        String[] noteTag = tag.split("_");
        int noteString = Integer.parseInt(noteTag[0]);
        int noteFret = Integer.parseInt(noteTag[1]);

        while (parent.getChildCount() > 1) {
            parent.removeViewAt(1);
        }
        setNoteText(new int[] {noteString, noteFret}, "setUpdate", parent, noteIndex);
    }

    private void stopRec() {
        strings.removeAllViews();
        notesFile.clear();

        scrollView.scrollTo(0, 0);
        setBeginGriff();
        countCurrGriff = 0;
    }

    private void writeFile(String name, boolean update) {
        // запись табулатуры
        try {
            // создание json объекта
            JSONObject jsonObject = new JSONObject();
            int i = 0;

            for(Note note: notesFile) {
                // создание объекта ноты в json
                JSONObject noteObject = new JSONObject();
                noteObject.put("String", note.string);
                noteObject.put("Fret", note.fret);

                jsonObject.put(String.valueOf(i), noteObject);
                i++;
            }

            // перезаписывать или нет
            if (update) {
                dbManager.updateTab(name, jsonObject.toString());
            } else {
                dbManager.addResult(name, jsonObject.toString());
            }

            save = true;
            Toast.makeText(context, "Табулатура сохранена", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void readJSONFile(String jsonText) throws JSONException {
        // создаём json объект
        JSONObject jsonObject = new JSONObject(jsonText);

        for (int i = 0; i < jsonObject.length(); i++) {
            int[] note = new int[2];

            JSONObject noteObj = jsonObject.getJSONObject(String.valueOf(i));
            note[0] = noteObj.getInt("String");
            note[1] = noteObj.getInt("Fret");

            // устанавливаем ноту на струну
            setNoteText(note, "standard", null, 0);
        }
    }

    public void createDialog(String nameDialog, String lastName) {
        switch (nameDialog){
            case "writeFile": {
                // диалоговое окно для записи файла
                ArrayList<String> tabNames = dbManager.getTabNames();

                EditText editText = new EditText(context);
                editText.setText(lastName);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setMessage("Укажите название табулатуры");
                builder.setView(editText);
                builder.setNegativeButton("Отмена", (dialog, which) -> { });
                builder.setPositiveButton("OK", (dialog, which) -> {
                    String name = editText.getText().toString();
                    boolean indName = true;

                    for(String tabName: tabNames) {
                        if(tabName.equals(name)) {
                            createDialog("rewriteFile", name);
                            indName = false;
                        }
                    }
                    if(indName) {
                        writeFile(name, false);
                        Intent in = new Intent(context, MainActivity.class);
                        context.startActivity(in);
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
                    writeFile(lastName, true);
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
                });

                builder.show();
                break;
            }
        }
    }
}
