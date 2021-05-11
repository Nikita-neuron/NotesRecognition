package com.example.tabnote.Recognition;

import android.content.Context;
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
    Context context;

    private boolean running = false;

    FrequencyScanner frequencyScanner = new FrequencyScanner();

    public AudioReciever(Handler handler, Context context) {
        this.handler = handler;
        this.context = context;
    }

    public void start() {
        running = true;
        loopback();
    }

    public void stop() throws InterruptedException {
        running = false;
//        Rthread.join();
//        Rthread.interrupt();
    }

    protected void loopback() {

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
//        final int bufferSize = 16384;
        final int bufferSize = 8192;
//        final int bufferSize = AudioRecord.getMinBufferSize(freq,
//                AudioFormat.CHANNEL_CONFIGURATION_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, freq,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                MediaRecorder.AudioEncoder.AMR_NB, bufferSize);

        final short[] buffer = new short[bufferSize];
        final short[] buffer2 = new short[bufferSize];

        short[] bufferZero = new short[2*bufferSize];
        short[] bufferZero2 = new short[2*bufferSize];

        complex = new Complex();
        fftAnother = new FFTAnother();

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

                    Complex[] res_complex = fftAnother.DecimationInFrequency(buffer_complex, false);
                    Complex[] res_complex2 = fftAnother.DecimationInFrequency(buffer_complex2, false);

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

                    handler.sendMessage(handler.obtainMessage(2, spectrum));
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
}
