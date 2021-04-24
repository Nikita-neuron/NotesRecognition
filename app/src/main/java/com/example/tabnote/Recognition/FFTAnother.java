package com.example.tabnote.Recognition;

import android.util.Log;

/**
 * Created by alexey on 08.01.16.
 */
public class FFTAnother {

    //magnitude = Math.Sqrt(x.Real*x.Real + x.Imaginary*x.Imaginary)
    //phase = Math.Atan2(x.Imaginary, x.Real)

        public final double SinglePi = Math.PI;
        public static final double DoublePi = 2*Math.PI;

    Complex[] spectrumOdd_2;
    boolean worked = false;

    public Complex[] DecimationInTime(Complex[] frame, boolean direct, boolean first) {
            if (frame.length == 1) return frame;
            int frameHalfSize = frame.length >> 1; // frame.Length/2
            int frameFullSize = frame.length;

            Complex[] frameOdd = new Complex[frameHalfSize];
            Complex[] frameEven = new Complex[frameHalfSize];
            for (int i = 0; i < frameHalfSize; i++)
            {
                int j = i << 1; // i = 2*j;
                frameOdd[i] = frame[j + 1];
                frameEven[i] = frame[j];
            }

        Complex[] spectrumOdd = new Complex[0];

        if (first) {
            worked = false;
            new Thread(() -> {
                Log.i("fft", "thread_start");
                spectrumOdd_2 = DecimationInTime(frameOdd, direct, false);
                worked = true;
                Log.i("fft", "thread_end");
            }).start();
        } else {
            spectrumOdd = DecimationInTime(frameOdd, direct, false);
        }
        Complex[] spectrumEven = DecimationInTime(frameEven, direct, false);

            double arg = direct ? -DoublePi/frameFullSize : DoublePi/frameFullSize;
            Complex omegaPowBase = new Complex(Math.cos(arg), Math.sin(arg));
            Complex[] spectrum = new Complex[frameFullSize];


        Complex omega = new Complex(1.0, 0.0);
        if (first) {
            while (!worked) {
                try {
                    Thread.sleep(10);
                    Log.i("FFT", "sleeping");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (int j = 0; j < frameHalfSize; j++)
            {
                spectrum[j] = spectrumEven[j].plus(omega.times(spectrumOdd_2[j]));
                spectrum[j + frameHalfSize] = spectrumEven[j].minus(omega.times(spectrumOdd_2[j]));
                omega = omega.times(omegaPowBase);
            }
        } else {
            for (int j = 0; j < frameHalfSize; j++) {
                spectrum[j] = spectrumEven[j].plus(omega.times(spectrumOdd[j])) ;
                spectrum[j + frameHalfSize] = spectrumEven[j].minus(omega.times(spectrumOdd[j])) ;
                omega = omega.times(omegaPowBase);
            }
        }
            return spectrum;
        }

        public static Complex[] DecimationInFrequency(Complex[] frame, boolean direct)
        {
            if (frame.length == 1) return frame;
            int halfSampleSize = frame.length >> 1; // frame.Length/2
            int fullSampleSize = frame.length;

            double arg = direct ? -DoublePi/fullSampleSize : DoublePi/fullSampleSize;
            Complex omegaPowBase = new Complex(Math.cos(arg), Math.sin(arg));
            Complex omega = new Complex(1.0,0.0);
            Complex[] spectrum = new Complex[fullSampleSize];

            for (int j = 0; j < halfSampleSize; j++)
            {
                spectrum[j] = frame[j].plus(frame[j + halfSampleSize]);
                spectrum[j + halfSampleSize] = omega.times(frame[j].minus(frame[j + halfSampleSize]));
                omega = omega.times(omegaPowBase) ;
            }

            Complex[] yTop = new Complex[halfSampleSize];
            Complex[] yBottom = new Complex[halfSampleSize];
            for (int i = 0; i < halfSampleSize; i++)
            {
                yTop[i] = spectrum[i];
                yBottom[i] = spectrum[i + halfSampleSize];
            }

            yTop = DecimationInFrequency(yTop, direct);
            yBottom = DecimationInFrequency(yBottom, direct);
            for (int i = 0; i < halfSampleSize; i++)
            {
                int j = i << 1; // i = 2*j;
                spectrum[j] = yTop[i];
                spectrum[j + 1] = yBottom[i];
            }

            return spectrum;
        }
    }
