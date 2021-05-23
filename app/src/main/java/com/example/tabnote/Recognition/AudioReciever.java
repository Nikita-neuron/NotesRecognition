package com.example.tabnote.Recognition;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AudioReciever{
    private final int freq = 24000;
    private AudioRecord audioRecord;
    private final Handler handler;

    private Complex complex;
    private FFT fft;
    Context context;

    private boolean running = false;

    String threshold = "6";
    String[] thresholds;

    ArrayList<Integer> freqListOld = new ArrayList<>();
    ArrayList<Integer> valListOld = new ArrayList<>();

    ArrayList<Integer> countPickesSpectrums = new ArrayList<>();

    public AudioReciever(Handler handler, Context context, Spinner spinner, String[] thresholds) {
        this.handler = handler;
        this.context = context;
        this.thresholds = thresholds;

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                threshold = thresholds[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void start() {
        running = true;
        loopback();
    }

    public void stop() {
        running = false;
    }

    protected void loopback() {

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
//        final int bufferSize = 16384;
//        final int bufferSize = 8192;
        final int bufferSize = 4096;

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, freq,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                MediaRecorder.AudioEncoder.AMR_NB, bufferSize);

        final short[] buffer = new short[bufferSize];
        final short[] buffer2 = new short[bufferSize];

        complex = new Complex();
        fft = new FFT();

        audioRecord.startRecording();
        Thread rthread = new Thread(() -> {
            if (Thread.interrupted()) {
                try {
                    throw new InterruptedException();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (running) {
                try {
                    audioRecord.read(buffer, 0, bufferSize);

                    Thread.sleep(16);

                    audioRecord.read(buffer2, 0, bufferSize);

                    for (int i = 0; i < bufferSize; i++) {
                        buffer[i] *= Gausse(i, bufferSize);
                        buffer2[i] *= Gausse(i, bufferSize);
                    }

                    Complex[] buffer_complex = complex.realToComplex(buffer);
                    Complex[] buffer_complex2 = complex.realToComplex(buffer2);

                    Complex[] res_complex = fft.DecimationInFrequency(buffer_complex, false);
                    Complex[] res_complex2 = fft.DecimationInFrequency(buffer_complex2, false);

                    for (int i = 0; i < res_complex.length; i++) {
                        res_complex[i].re /= bufferSize;
                        res_complex2[i].re /= bufferSize;
                    }

                    LinkedHashMap<Integer, Integer> spectrum = Filters.GetJoinedSpectrum(res_complex, res_complex2, 16, freq);
                    spectrum = Filters.Antialiasing(spectrum);

                    double maxFreq = getMaxFreq(spectrum);
                    handler.sendMessage(handler.obtainMessage(2, maxFreq));
                } catch (Throwable t) {
                    Log.d("Error", "Read write failed");
                    t.printStackTrace();
                }
            }
        });
        rthread.start();
    }

    public double Gausse(double n, double frameSize) {
        double a = (frameSize - 1) / 2;
        double t = (n - a) / (0.5 * a);
        t = t * t;
        return Math.exp(-t / 2);
    }

    public double getMaxFreq(LinkedHashMap<Integer, Integer> spectrumLast) {

        LinkedHashMap<Integer, Integer> spectrum = new LinkedHashMap<>();
        for (Integer key: spectrumLast.keySet()) {
            if (key >= 1001) break;
            spectrum.put(key, spectrumLast.get(key));
        }

        double fr = 0;
        double maxx = 0;
        int n_pickes = 0;

        ArrayList<Integer> arrayList = new ArrayList<>();

        Set set = spectrum.entrySet();
        ArrayList<Integer> valList = new ArrayList<>();
        ArrayList<Integer> freqList = new ArrayList<>();
        int ind = 0;
        for (Object o : set) {
            Map.Entry item = (Map.Entry) o;
            int freq = (int) item.getKey();
            int val = (int) item.getValue();

            if (freq < 1500) {
                if (val > maxx && val > (Integer.parseInt(threshold)*100000)) {
                    maxx = val;
                    fr = freq;
                    n_pickes ++;
                }
            }

            if (val > (Integer.parseInt(threshold)*100000)) {
                arrayList.add(freq);
                if (ind <= 6) {
                    freqList.add(freq);
                    valList.add(val);
                }
                ind ++;
            }
        }
        double ampl_freq_distance;
        if (freqList.size() > 1 && freqListOld.size() > 1) {
            double cf = 3.0;

            ampl_freq_distance = Math.sqrt(Math.pow((freqList.get(0) - freqListOld.get(0)) / cf, 2));

            System.out.println(ampl_freq_distance);
        }
        freqListOld = freqList;
        valListOld = valList;

        if (arrayList.size() > 0) {
            if (Collections.min(freqList) < 170) {
                fr /= 2.8;
            }
        }
        // 16 9 7 6
//        return fr;
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
    }

}
