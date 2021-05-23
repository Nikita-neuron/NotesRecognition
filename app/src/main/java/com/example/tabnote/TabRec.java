package com.example.tabnote;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tabnote.Recognition.AudioReciever;
import com.example.tabnote.database.DBManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TabRec implements View.OnClickListener, View.OnLongClickListener{

    public boolean reco = false;

    Bitmap microphoneImage;
    Bitmap noMicrophoneImage;
    Bitmap guitarGriff;

    ImageView btnClear;
    ImageView btnMicrophone;
    ImageView btnSave;
    ImageView btnBack;
    ImageView btnPlay;

    HorizontalScrollView scrollView;

    LinearLayout strings;

    public AudioReciever audioReciever;

    Spinner spinner;

    PlayNotes playNotes;

    int[] notesMargin = new int[] {
            2, 34, 66, 100, 130, 160
    };

    // начальное количество картинок грифа
    int countBegGriff;
    // количество нарисованных сначала грифов
    int countCurrGriff = 0;

    // частоты
    // по вертикали - струны, по горизонтали - лады
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

    public int width;
    public int height;

    private boolean playNote = false;

    // массив нот
    ArrayList<Note> notesFile = new ArrayList<>();

    // сохранён файл или нет
    public boolean save = true;

    Context context;

    DBManager dbManager;

    String userName;

    String[] thresholds = new String[] {"4", "5", "6", "7", "8", "9"};

    int imageWidth;
    int imageHeight;

    @SuppressLint("HandlerLeak")
    public TabRec(Context context, String userName, View tabRoot) {
        this.context = context;
        this.userName = userName;

        // get database
        dbManager = DBManager.getInstance(context);

        // get screen size
        DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
        width = displaymetrics.widthPixels;
        height = displaymetrics.heightPixels;

        // find clear, microphone, save and play  buttons
        btnClear = tabRoot.findViewById(R.id.btnClear);
        btnMicrophone = tabRoot.findViewById(R.id.btnPausePlay);
        btnSave = tabRoot.findViewById(R.id.btnSaveTab);
        btnPlay = tabRoot.findViewById(R.id.btnPlay);
        btnBack = tabRoot.findViewById(R.id.arrowBack);

        // images microphone and no microphone
        microphoneImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.microphone);
        noMicrophoneImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_microphone);

        // get guitar image
        guitarGriff = BitmapFactory.decodeResource(context.getResources(), R.drawable.strings);

        // find view of strings
        strings = tabRoot.findViewById(R.id.strings);

        // find scrolls
        scrollView = tabRoot.findViewById(R.id.horizontalScroll);

        // find spinner
        spinner = tabRoot.findViewById(R.id.threshold);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, thresholds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(2);

        btnMicrophone.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        // get spectrum fom audioReceiver
        Handler handler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(android.os.Message msg) {
                // получаем ноту по частоте, переданной из детектора
                double maxFreq = (double) msg.obj;
//                double[] spectrum = (double[]) msg.obj;

                int[] note = getNote(maxFreq);

                // если распознано, то добавляем на струну
                if (note[0] >= 0 && note[1] >= 0) {
                    setNoteText(note, "standard", null, 0);
                }
            }
        };

        imageWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, context.getResources().getDisplayMetrics());
        imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 190, context.getResources().getDisplayMetrics());

        countBegGriff = width / imageWidth;

        audioReciever = new AudioReciever(handler, context, spinner, thresholds);

        playNotes = new PlayNotes(context, notesFile, this.strings, btnPlay, scrollView, countBegGriff);

        setBeginGriff();

//        for (int i = 0; i < 6; i++) {
//            setNoteText(new int[] {i, 0}, "standard", null, 0);
//        }
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(R.string.noteValue) != null) {
            int type = (int) v.getTag(R.string.noteType);
            if (type == R.string.noteTypeUpdate) {
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
                        btnMicrophone.setImageBitmap(noMicrophoneImage);
                        audioReciever.stop();
                    } else {
                        // если старт
                        reco = true;
                        btnMicrophone.setImageBitmap(microphoneImage);
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
                        if (notesFile.size() > 0) {
                            createDialog("writeFile", "");
                        }
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
                        createDialog("clear", "");
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
                    } else if (notesFile.size() > 0){
                        playNote();
                    }
                } else {
                    playNotes.pause();
                }
                break;
            }

            case (R.id.arrowBack): {
                btnMicrophone.setImageBitmap(noMicrophoneImage);
                if (reco) {
                    audioReciever.stop();
                }
                reco = false;

                if (!save) {
                    if (notesFile.size() > 0) {
                        createDialog("back", "");
                    }
                    Intent in = new Intent(context, MainActivity.class);
                    in.putExtra("userName", userName);
                    context.startActivity(in);
                } else {
                    Intent in = new Intent(context, MainActivity.class);
                    in.putExtra("userName", userName);
                    context.startActivity(in);
                }
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getTag(R.string.noteValue) != null) {
            int type = (int) v.getTag(R.string.noteType);
            if (type == R.string.noteTypeStandard) {
                updateNoteText(v);
                return true;
            }
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    private int[] getNote(double freq) {
        // получение ноты
        for(int i = 0; i<frequency.length; i++) {
            for(int j = 0; j<frequency[i].length; j++) {
                // если частота совпадает с погрешностью, то возвращаем
                // i - струна
                // j - лад
                if(freq >= frequency[i][j] - er && freq <= frequency[i][j] + er) {
                    return new int[]{i, j};
                }
            }
        }
        save = false;

        // если не совпадает, то возвращаем -1
        return new int[] {-1, -1};
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

        // добавление ноты на струну
        // утановление margin для ноты
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams layoutPlayParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(layoutPlayParams);
        linearLayout.setBackgroundColor(Color.argb(0, 106, 161, 71));

        int noteMargin = getPixelsFromDp(notesMargin[note[0]]);

        params.setMargins(80, noteMargin,0,0);

        TextView textView = new EditText(context);
        textView.setText(note[1]+"");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(20);
        textView.setPadding(0, 0, 4, 4);
        textView.setLayoutParams(params);
        textView.setTag(R.string.noteValue, (note[0]) + "_" + (note[1]));

        textView.setCursorVisible(false);

        switch (type) {
            case "standard":
                notesFile.add(new Note(note[0], note[1]));

                textView.setBackground(context.getDrawable(R.drawable.note_standard));
                textView.setTag(R.string.noteType, R.string.noteTypeStandard);
                textView.setTag(R.string.noteID, notesFile.size() - 1);
                textView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                textView.setOnClickListener(this);

                textView.setOnLongClickListener(this);

                textView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String line = s.toString();
                        if (!line.equals("")) {
                            notesFile.set((Integer) textView.getTag(R.string.noteID), new Note(note[0], Integer.parseInt(line)));
                            textView.setTag(R.string.noteValue, (note[0]) + "_" + (line));
                            System.out.println(textView.getTag(R.string.noteValue));
                            save = false;
                        }
                    }
                });

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
                scrollView.fullScroll(View.FOCUS_RIGHT);
                break;
            case "update":
                textView.setBackground(context.getDrawable(R.drawable.note_update));
                textView.setTag(R.string.noteType, R.string.noteTypeUpdate);
                textView.setTag(R.string.noteID, noteIndex);

                textView.setFocusableInTouchMode(false);
                textView.setCursorVisible(false);

                textView.setOnClickListener(this);

                parent.addView(textView);
                save = false;
                break;
            case "delete":
                textView.setText("");
                LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                deleteParams.setMargins(80, getPixelsFromDp(-10),0,0);
                textView.setLayoutParams(deleteParams);
                textView.setBackground(context.getDrawable(R.drawable.note_update));
                textView.setTag(R.string.noteType, "Delete");
                textView.setTag(R.string.noteID, noteIndex);

                textView.setFocusableInTouchMode(false);
                textView.setCursorVisible(false);

                textView.setOnClickListener(this);

                parent.addView(textView);
                save = false;
                break;
            case "setUpdate":
                notesFile.set(noteIndex, new Note(note[0], note[1]));

                textView.setBackground(context.getDrawable(R.drawable.note_standard));
                textView.setTag(R.string.noteType, R.string.noteTypeStandard);
                textView.setTag(R.string.noteID, noteIndex);
                textView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                textView.setOnClickListener(this);

                textView.setOnLongClickListener(this);

                textView.setEnabled(true);
                textView.setCursorVisible(true);

                textView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String line = s.toString();
                        if (!line.equals("")) {
                            notesFile.set((Integer) textView.getTag(R.string.noteID), new Note(note[0], Integer.parseInt(line)));
                            textView.setTag(R.string.noteValue, (note[0]) + "_" + (line));
                            System.out.println(textView.getTag(R.string.noteValue));
                            save = false;
                            textView.setCursorVisible(false);
                        }
                    }
                });

                parent.addView(textView);
                parent.addView(linearLayout);
                break;
        }
    }

    private FrameLayout drawGriff() {
        // создание layout грифа
        FrameLayout frameLayout = new FrameLayout(context);

        LinearLayout.LayoutParams layoutImageParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);

        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(guitarGriff);
        imageView.setLayoutParams(layoutImageParams);

        frameLayout.addView(imageView);

        return frameLayout;
    }

    private void setBeginGriff() {
        // создание первоначального грифа

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
        System.out.println(tag);
        String[] noteTag = tag.split("_");
        int noteFret = Integer.parseInt(noteTag[1]);

        while (parent.getChildCount() > 1) {
            parent.removeViewAt(1);
        }

//        setNoteText(new int[]{0, noteFret}, "delete", parent, noteIndex);

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
        if (notesFile.size() > 0) {
            strings.removeAllViews();
            notesFile.clear();

            scrollView.scrollTo(0, 0);
            setBeginGriff();
            countCurrGriff = 0;
        }
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
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);

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
                        in.putExtra("userName", userName);
                        context.startActivity(in);
                    }
                });

                builder.show();
                break;
            }
            case "rewriteFile": {
                // диалоговое окно для перезаписи файла
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);

                builder.setTitle("Данный файл уже существует");
                builder.setMessage("Вы хотите его перезаписать?");
                builder.setNegativeButton("Нет", (dialog, which) -> createDialog("writeFile", lastName));
                builder.setPositiveButton("Да", (dialog, which) -> writeFile(lastName, true));

                builder.show();
                break;
            }
            case "back": {
                // диалоговое окно при выходе без сохранения
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
                builder.setMessage("Сохранить изменения?");

                builder.setNegativeButton("Нет", (dialog, which) -> {
                    Intent in = new Intent(context, MainActivity.class);
                    in.putExtra("userName", userName);
                    context.startActivity(in);
                });
                builder.setPositiveButton("Да", (dialog, which) -> createDialog("writeFile", ""));

                builder.show();
                break;
            }
            case "clear": {
                // диалоговое окно при выходе без сохранения
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
                builder.setTitle("Очистить табулатуру?");
                builder.setMessage("Все изменения будут потеряны");

                builder.setNegativeButton("Нет", (dialog, which) -> { });
                builder.setPositiveButton("Да", (dialog, which) -> stopRec());

                builder.show();
                break;
            }
        }
    }

    private int getPixelsFromDp(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
