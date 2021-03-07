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

    public boolean endPlay = true;

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
                endPlay = true;
            }
            else {
                currentIndexNote ++;
            }
            initPlay();
        });
    }

    public void start() {
        endPlay = false;

        initPlay();
    }
    public void pause() {
        mediaPlayer.pause();
//        endPlay = true;
    }

    public  void stop() {
        endPlay = true;

        mediaPlayer.stop();

        stringCurrent.getChildAt(2).setBackgroundColor(Color.argb(0, 106, 161, 71));

        stringCurrent = (FrameLayout) this.strings.getChildAt(currentIndexNote);
        currentIndexNote = 0;
    }

    private void initPlay() {
        stringCurrent.getChildAt(2).setBackgroundColor(Color.argb(0, 106, 161, 71));

        if (!endPlay) {

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
                play();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void play() {
        mediaPlayer.start();

        System.out.println("Play: true");

        stringCurrent = (FrameLayout) strings.getChildAt(currentIndexNote);

        stringCurrent.getChildAt(2).setBackgroundColor(Color.argb(60, 106, 161, 71));
    }
}
