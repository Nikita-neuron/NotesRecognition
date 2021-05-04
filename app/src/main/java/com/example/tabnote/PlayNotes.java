package com.example.tabnote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.ArrayList;

public class PlayNotes {
    private MediaPlayer mediaPlayer;

    private Context context;

    private int currentIndexNote;

    private ArrayList<Note> notes;

    public boolean playNote = false;

    private LinearLayout strings;
    private FrameLayout stringCurrent;
    private ImageView btnPlay;

    Bitmap imagePlay;
    Bitmap imagePause;

    PlayNotes(Context context, ArrayList<Note> notes, LinearLayout strings, ImageView btnPlay) {
        this.context = context;
        this.notes = notes;
        this.strings = strings;
        this.btnPlay = btnPlay;

        mediaPlayer = new MediaPlayer();

        stringCurrent = (FrameLayout) this.strings.getChildAt(currentIndexNote);

        imagePlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.play);
        imagePause = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause);

        mediaPlayer.setOnCompletionListener(mp -> {
            stringCurrent.getChildAt(2).setBackgroundColor(Color.argb(0, 106, 161, 71));
            mediaPlayer.stop();
            if (currentIndexNote == notes.size() - 1) {
                stop();
            }
            else {
                currentIndexNote ++;
            }
            if (playNote) {
                initPlay();
                play();
            }
        });
    }

    public void play() {
        playNote = true;
        btnPlay.setImageBitmap(imagePause);
        playNote();
    }
    public void pause() {
        mediaPlayer.pause();
        playNote = false;
        btnPlay.setImageBitmap(imagePlay);
    }

    public void setNotes(ArrayList<Note> notes, LinearLayout strings) {
        this.notes = notes;
        this.strings = strings;
    }

    public void stop() {
        playNote = false;

        mediaPlayer.stop();
        stringCurrent.getChildAt(2).setBackgroundColor(Color.argb(0, 106, 161, 71));

        stringCurrent = (FrameLayout) strings.getChildAt(currentIndexNote);
        currentIndexNote = 0;
        btnPlay.setImageBitmap(imagePlay);
    }

    public void initPlay() {
        stringCurrent = (FrameLayout) strings.getChildAt(currentIndexNote);
        Note currentNote = notes.get(currentIndexNote);
        String fileName = "str_" + currentNote;

        boolean f = true;
        String[] files = new String[] {
                "str_1_0", "str_1_1", "str_1_2", "str_1_3", "str_1_4", "str_1_5", "str_1_6", "str_1_7",
                "str_1_8", "str_1_9", "str_1_10", "str_1_11", "str_1_12",
                "str_2_0", "str_2_1", "str_2_2", "str_2_3", "str_2_4", "str_2_5", "str_2_6", "str_2_7",
                "str_2_8", "str_2_9", "str_2_10", "str_2_11", "str_2_12",
                "str_3_0", "str_3_1", "str_3_2", "str_3_3", "str_3_4", "str_3_5", "str_3_6", "str_3_7",
                "str_3_8", "str_3_9", "str_3_10", "str_3_11", "str_3_12",
                "str_4_0", "str_4_1", "str_4_2", "str_4_3", "str_4_4", "str_4_5", "str_4_6", "str_4_7",
                "str_4_8", "str_4_9", "str_4_10", "str_4_11", "str_4_12",
                "str_5_0", "str_5_1", "str_5_2", "str_5_3", "str_5_4", "str_5_5", "str_5_6", "str_5_7",
                "str_5_8", "str_5_9", "str_5_10", "str_5_11", "str_5_12",
                "str_6_0"
        };

        for (String name : files) {
            if (name.equals(fileName)) {
                f = false;
                break;
            }
        }
        if (f) {
            fileName = "str_1_0";
        }
        String res = "android.resource://" + context.getPackageName() + "/raw/" + fileName;
        Uri url = Uri.parse(res);

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(context, url);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playNote() {
        mediaPlayer.start();

        stringCurrent = (FrameLayout) strings.getChildAt(currentIndexNote);

        stringCurrent.getChildAt(2).setBackgroundColor(Color.argb(60, 106, 161, 71));
    }
}
