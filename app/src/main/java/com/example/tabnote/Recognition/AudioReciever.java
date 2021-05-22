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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AudioReciever{
    private final int freq = 24000;
    private AudioRecord audioRecord;
    private Thread Rthread;
    private final Handler handler;

    private Complex complex;
    private FFT fft;
    Context context;

    private Spinner spinner;

    private boolean running = false;

    String threshold = "6";
    String[] thresholds;

    ArrayList<Integer> freqListOld = new ArrayList<>();
    ArrayList<Integer> valListOld = new ArrayList<>();

    ArrayList<Integer> countPickesSpectrums = new ArrayList<>();

    FrequencyScanner frequencyScanner = new FrequencyScanner();

    public AudioReciever(Handler handler, Context context, Spinner spinner, String[] thresholds) {
        this.handler = handler;
        this.context = context;
        this.spinner = spinner;
        this.thresholds = thresholds;

        this.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
//        final int bufferSize = 2048;

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, freq,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                MediaRecorder.AudioEncoder.AMR_NB, bufferSize);

        final short[] buffer = new short[bufferSize];
        final short[] buffer2 = new short[bufferSize];

        short[] bufferZero = new short[2*bufferSize];
        short[] bufferZero2 = new short[2*bufferSize];

        complex = new Complex();
        fft = new FFT();

        audioRecord.startRecording();
        Rthread = new Thread(() -> {
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
                    double[] res = frequencyScanner.extractFrequency(buffer, freq);

                    Thread.sleep(16);

                    audioRecord.read(buffer2, 0, bufferSize);
                    double[] res2 = frequencyScanner.extractFrequency(buffer2, freq);

//                    for (int i = 0; i < res.length; i++) {
//                        res[i] /= bufferSize;
//                        res2[i] /= bufferSize;
//                    }
//                    System.out.println("res: " + res[0] + " " + res[1]);
//                    System.out.println("res2: " + res2[0] + " " + res2[1]);
//                    for (int i = 1; i < bufferZero.length; i++) {
//                        bufferZero[i] = 0;
//                        bufferZero2[i] = 0;
//                    }

//                    bufferZero[0] = buffer[0];
//                    bufferZero2[0] = buffer2[0];

//                    ArrayList<Short> arrayList = new ArrayList<>();
//                    ArrayList<Short> arrayList2 = new ArrayList<>();
//
//                    for (int i = 0; i < bufferSize; i++) {
//                        arrayList.add(buffer[i]);
//                        arrayList2.add(buffer2[i]);
//                        arrayList.add((short) 0);
//                        arrayList2.add((short) 0);
//                    }
//
//                    for (int i = 0; i < arrayList.size(); i++) {
//                        bufferZero[i] = arrayList.get(i);
//                        bufferZero2[i] = arrayList2.get(i);
//                    }

                    for (int i = 0; i < bufferSize; i++) {
                        buffer[i] *= frequencyScanner.Gausse(i, bufferSize);
                        buffer2[i] *= frequencyScanner.Gausse(i, bufferSize);
                    }
//                    for (int i = 0; i < bufferZero.length; i++) {
//                        bufferZero[i] *= frequencyScanner.Gausse(i, bufferZero.length);
//                        bufferZero2[i] *= frequencyScanner.Gausse(i, bufferZero2.length);
//                    }
//                    ind ++;

//                    new Thread(){
//                        @Override
//                        public void run() {
//                            try {
//                                FileOutputStream out = context.openFileOutput("spectrum" + ind, Context.MODE_PRIVATE);
//                                PrintWriter write = new PrintWriter(out);
//                                write.println(Arrays.toString(bufferZero));
//                                write.println(Arrays.toString(bufferZero2));
//                                write.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }.start();

                    Complex[] buffer_complex = complex.realToComplex(buffer);
                    Complex[] buffer_complex2 = complex.realToComplex(buffer2);

//                    Complex[] buffer_complex = complex.realToComplex(bufferZero);
//                    Complex[] buffer_complex2 = complex.realToComplex(bufferZero2);

                    Complex[] res_complex = fft.DecimationInFrequency(buffer_complex, false);
                    Complex[] res_complex2 = fft.DecimationInFrequency(buffer_complex2, false);

                    for (int i = 0; i < res_complex.length; i++) {
                        res_complex[i].re /= bufferSize;
                        res_complex2[i].re /= bufferSize;
                    }
//                    System.out.println(res_complex[16385]);
//                    for (int i = 0; i < res_complex.length; i++) {
//
//                        res_complex[i].re /= bufferZero.length;
//                        res_complex2[i].re /= bufferZero2.length;
//                    }

                    short[] char_complex_buffer = complex.complexToShort(res_complex);
                    short[] char_complex_buffer2 = complex.complexToShort(res_complex2);

//                    new Thread(){
//                        @Override
//                        public void run() {
//                            try {
//                                FileOutputStream out = context.openFileOutput("buffers" + ind + ".txt", Context.MODE_PRIVATE);
//                                PrintWriter write = new PrintWriter(out);
//                                write.println(Arrays.toString(char_complex_buffer));
//                                write.println("\n");
//                                write.println(Arrays.toString(char_complex_buffer2));
//                                write.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }.start();

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


//                    LinkedHashMap<Integer, Integer> finalSpectrum = spectrum;
//                    new Thread(){
//                        @Override
//                        public void run() {
//                            try {
//                                FileOutputStream out = context.openFileOutput("spectrum" + ind + ".txt", Context.MODE_PRIVATE);
//                                PrintWriter write = new PrintWriter(out);
//                                write.println(finalSpectrum.values().toString());
//                                write.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }.start();
                    double maxFreq = getMaxFreq(spectrum);
                    handler.sendMessage(handler.obtainMessage(2, maxFreq));
//                    handler.sendMessage(handler.obtainMessage(2, res));
//                    Log.d("data", res+"");
                } catch (Throwable t) {
                    Log.d("Error", "Read write failed");
                    t.printStackTrace();
                }
            }
        });
        Rthread.start();
    }

    private double calculateAverage(List<Integer> marks) {
        Integer sum = 0;
        if(!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
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
//            if (n_pickes > 25 || n_pickes < 15) break;
        }


//        System.out.println("pickes: " + n_pickes);
//        System.out.println(arrayList + " > " + threshold);

//        System.out.println("max freq: " + freqList);
//        System.out.println("max val: " + valList);

//        if (arrayList.size() > 0) {
//            if (arrayList.get(0) < 160) {
//                System.out.println("fr: " + fr);
//                fr /= 2.8;
//            }
//        }
        double ampl_freq_distance = 0;
        double ampl_dist = 0;
        if (freqList.size() > 1 && freqListOld.size() > 1) {
            double cf = 3.0;
            double cp = 100.0;

            // если равно нулю, то не выводить

            ampl_freq_distance = Math.sqrt(Math.pow((freqList.get(0) - freqListOld.get(0)) / cf, 2));
            ampl_dist = Math.sqrt(Math.pow((10*Math.log10(valList.get(0)) - 10*Math.log10(valListOld.get(0))) / cp, 2));

            System.out.println(ampl_freq_distance);
        }
        freqListOld = freqList;
        valListOld = valList;

        if (arrayList.size() > 0) {
            if (Collections.min(freqList) < 170) {
//                System.out.println("fr: " + fr);

                int zeroFr = arrayList.get(0);

//                if (zeroFr >= 140 && zeroFr <= 148 ) System.out.println("RESULT: 4 СТРУНА ОТКРЫТЫЙ ЛАД");
//                else if (zeroFr >= 100 && zeroFr <= 110 ) System.out.println("RESULT: 5 СТРУНА ОТКРЫТЫЙ ЛАД");ц
//                else if (zeroFr >= 150 && zeroFr <= 156 ) System.out.println("RESULT: 6 СТРУНА ОТКРЫТЫЙ ЛАД");

                fr /= 2.8;
            }
        }

//        System.out.println("average freq: " + calculateAverage(freqList));

//        fr = arrayList.get(0);
//        boolean filterPickes = n_pickes > 25 || n_pickes < 15;
        // 16 9 7 6
        if (countPickesSpectrums.size() > 0) {
            if (n_pickes < countPickesSpectrums.get(countPickesSpectrums.size()-1)) {
                countPickesSpectrums.add(n_pickes);
                return 0;
            } else {
                countPickesSpectrums.clear();
//                if (ampl_freq_distance > 1 && ampl_dist > 0.01) {
                    return fr;
//                }
            }
        } else {
            countPickesSpectrums.add(n_pickes);
//            if (ampl_freq_distance > 1 && ampl_dist > 0.01) {
                return fr;
//            }
        }
//        return 0;
    }

}
