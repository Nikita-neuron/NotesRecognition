package com.example.tabnote;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.util.LinkedHashMap;

public class AudioReciever{
    private final int freq = 24000;
    private AudioRecord audioRecord;
    private Thread Rthread;
    private final Handler handler;

    private Complex complex;
    private FFTAnother fftAnother;

    private boolean running = false;

    FrequencyScanner frequencyScanner = new FrequencyScanner();

    public AudioReciever(Handler handler) {
        this.handler = handler;
    }

    protected void start() {
        running = true;
        loopback();
    }

    protected  void stop() throws InterruptedException {
        running = false;
        Rthread.join();
    }

    protected void loopback() {

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        final int bufferSize = 16384;
//        final int bufferSize = 8192;
//        final int bufferSize = AudioRecord.getMinBufferSize(freq,
//                AudioFormat.CHANNEL_CONFIGURATION_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, freq,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                MediaRecorder.AudioEncoder.AMR_NB, bufferSize);

        final short[] buffer = new short[bufferSize];
        final short[] buffer2 = new short[bufferSize];

        complex = new Complex();
        fftAnother = new FFTAnother();

        audioRecord.startRecording();
        Rthread = new Thread(() -> {
            while (running) {
                try {
                    audioRecord.read(buffer, 0, bufferSize);
                    double[] res = frequencyScanner.extractFrequency(buffer, freq);

                    Thread.sleep(10);

                    audioRecord.read(buffer2, 0, bufferSize);
                    double[] res2 = frequencyScanner.extractFrequency(buffer2, freq);

                    for (int i = 0; i < res.length; i++) {
                        res[i] /= bufferSize;
                        res2[i] /= bufferSize;
                    }
//                    System.out.println("res: " + res[0] + " " + res[1]);
//                    System.out.println("res2: " + res2[0] + " " + res2[1]);

                    Complex[] buffer_complex = complex.realToComplex(buffer);
                    Complex[] buffer_complex2 = complex.realToComplex(buffer2);

                    Complex[] res_complex = fftAnother.DecimationInTime(buffer_complex, true, true);
                    Complex[] res_complex2 = fftAnother.DecimationInTime(buffer_complex2, true, true);

                    for (int i = 0; i < res_complex.length; i++) {
                        res_complex[i].re /= bufferSize;
                        res_complex2[i].re /= bufferSize;
                    }

                    LinkedHashMap<Integer, Integer> spectrum = Filters.GetJoinedSpectrum(res_complex, res_complex2, 16, freq);
                    spectrum = Filters.Antialiasing(spectrum);
//                    System.out.println("spectrum: " + spectrum.toString());

                    String data1 = "\n";
//                    for(int i = 0; i < res.length / 2; i++) {
//                        Log.d("d", i+"");
//                        Log.d("d", 8000 * i / (res.length / 2) + " : " + res[i]);
//                        data1 += String.valueOf(v);
//                        data1 += "\n";
//                    }
//                    Log.d("d", "-------");
//                    Log.d("d", data1);
                    handler.sendMessage(handler.obtainMessage(2, spectrum));
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
