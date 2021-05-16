package com.example.tabnote.Recognition;

public class FFT {
    public static final double DoublePi = 2*Math.PI;

    public Complex[] DecimationInFrequency(Complex[] frame, boolean direct) {

        if (frame.length == 1) return frame;
        int halfSampleSize = frame.length >> 1; // frame.Length/2
        int fullSampleSize = frame.length;

        double arg = direct ? -DoublePi/fullSampleSize : DoublePi/fullSampleSize;
        Complex omegaPowBase = new Complex(Math.cos(arg), Math.sin(arg));
        Complex omega = new Complex(1.0,0.0);
        Complex[] spectrum = new Complex[fullSampleSize];

        for (int j = 0; j < halfSampleSize; j++) {
            spectrum[j] = frame[j].plus(frame[j + halfSampleSize]);
            spectrum[j + halfSampleSize] = omega.times(frame[j].minus(frame[j + halfSampleSize]));
            omega = omega.times(omegaPowBase);
        }

        Complex[] yTop = new Complex[halfSampleSize];
        Complex[] yBottom = new Complex[halfSampleSize];
        for (int i = 0; i < halfSampleSize; i++) {
            yTop[i] = spectrum[i];
            yBottom[i] = spectrum[i + halfSampleSize];
        }

        yTop = DecimationInFrequency(yTop, direct);
        yBottom = DecimationInFrequency(yBottom, direct);
        for (int i = 0; i < halfSampleSize; i++) {
            int j = i << 1; // i = 2*j;
            spectrum[j] = yTop[i];
            spectrum[j + 1] = yBottom[i];
        }

        return spectrum;
    }
}
