package com.example.tabnote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class TabPreview {

    Context context;

    int imageWidth;
    int imageHeight;

    int width;

    int[] notesMargin = new int[] {
            1, 18, 35, 52, 69, 86
    };

    // начальное количество картинок грифа
    int countBegGriff;
    // количество нарисованных сначала грифов
    int countCurrGriff = 0;

    LinearLayout strings;

    HorizontalScrollView scrollView;

    Bitmap guitarGriff;

    public TabPreview(Context context, View view) {
        this.context = context;

        strings = view.findViewById(R.id.previewStrings);
        scrollView = view.findViewById(R.id.previewHorizontalScroll);

        width = getPixelsFromDp(200);

        imageWidth = getPixelsFromDp(40);
        imageHeight = getPixelsFromDp(100);

        // get guitar image
        guitarGriff = BitmapFactory.decodeResource(context.getResources(), R.drawable.strings);

        countBegGriff = width / imageWidth;
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void setNoteText(int[] note) {
        // note[0]  - струна
        // note[1] - лад

        // добавление ноты на струну
        // утановление margin для ноты
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPixelsFromDp(15), getPixelsFromDp(15));
        LinearLayout.LayoutParams layoutPlayParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(layoutPlayParams);
        linearLayout.setBackgroundColor(Color.argb(0, 106, 161, 71));

        int noteMargin = getPixelsFromDp(notesMargin[note[0]]);

//        params.setMargins(80, notesMargin[note[0]],0,0);
        params.setMargins(getPixelsFromDp(14), noteMargin,0,0);

        TextView textView = new TextView(context);
        textView.setText(note[1]+"");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(8);
        textView.setLayoutParams(params);
        textView.setTag(R.string.noteValue, (note[0]) + "_" + (note[1]));

        textView.setBackground(context.getDrawable(R.drawable.note_standard));

        textView.setGravity(Gravity.CENTER);

        FrameLayout griff = drawGriff();
        griff.addView(textView);
        griff.addView(linearLayout);
        strings.addView(griff);
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

    public void readJSONFile(String jsonText) throws JSONException {
        // создаём json объект
        JSONObject jsonObject = new JSONObject(jsonText);

        for (int i = 0; i < jsonObject.length(); i++) {
            int[] note = new int[2];

            JSONObject noteObj = jsonObject.getJSONObject(String.valueOf(i));
            note[0] = noteObj.getInt("String");
            note[1] = noteObj.getInt("Fret");

            // устанавливаем ноту на струну
            setNoteText(note);
        }
    }

    private int getPixelsFromDp(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
