package com.example.notesrecognition;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

public class AudioReciever{
    private final int freq = 8000;
    private AudioRecord audioRecord;
    private Thread Rthread;
    private final Handler handler;

    private boolean running = false;

    FrequencyScanner frequencyScanner = new FrequencyScanner();

    public AudioReciever(Handler handler) {
        this.handler = handler;
    }

    protected void start() {
        running = true;
        loopback();
    }

    protected  void stop() {
        running = false;
    }

    protected void loopback() {

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        final int bufferSize = AudioRecord.getMinBufferSize(freq,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, freq,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                MediaRecorder.AudioEncoder.AMR_NB, bufferSize);

        final short[] buffer = new short[bufferSize];
        audioRecord.startRecording();
        Rthread = new Thread(() -> {
            while (running) {
                try {
                    int data = audioRecord.read(buffer, 0, bufferSize);

                    double res = frequencyScanner.extractFrequency(buffer, freq);
                    handler.sendMessage(handler.obtainMessage((int) res));
//                    Log.d("data", res+"");

                } catch (Throwable t) {
                    Log.d("Error", "Read write failed");
                    t.printStackTrace();
                }
            }
        });
        Rthread.start();
    }
}
