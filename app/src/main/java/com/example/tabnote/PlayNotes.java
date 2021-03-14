package com.example.tabnote;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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

    PlayNotes(Context context, ArrayList<Note> notes, LinearLayout strings) {
        this.context = context;
        this.notes = notes;
        this.strings = strings;

        mediaPlayer = new MediaPlayer();

        stringCurrent = (FrameLayout) this.strings.getChildAt(currentIndexNote);

        mediaPlayer.setOnCompletionListener(mp -> {
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

//        mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
    }

    public void start() {
        playNote = true;
        initPlay();
    }
    public void play() {
        playNote = true;
        playNote();
    }
    public void pause() {
        mediaPlayer.pause();
        playNote = false;
    }

    public void setNotes(ArrayList<Note> notes, LinearLayout strings) {
        this.notes = notes;
        this.strings = strings;
    }

    public  void stop() {
        playNote = false;

        mediaPlayer.stop();
        mediaPlayer.release();

        stringCurrent.getChildAt(2).setBackgroundColor(Color.argb(0, 106, 161, 71));

        stringCurrent = (FrameLayout) this.strings.getChildAt(currentIndexNote);
        currentIndexNote = 0;
    }

    public void initPlay() {
        stringCurrent = (FrameLayout) this.strings.getChildAt(currentIndexNote);
        stringCurrent.getChildAt(2).setBackgroundColor(Color.argb(0, 106, 161, 71));

        Note currentNote = notes.get(currentIndexNote);
        String fileName = "str_" + currentNote;

        boolean f = true;

        String[] files = new String[] {"str_1_0", "str_2_0"};

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

        System.out.println("Play: true");

        stringCurrent = (FrameLayout) strings.getChildAt(currentIndexNote);

        stringCurrent.getChildAt(2).setBackgroundColor(Color.argb(60, 106, 161, 71));
    }
}
