package com.example.notesrecognition;

import android.util.Log;

import org.jtransforms.dct.DoubleDCT_1D;
import org.jtransforms.fft.DoubleFFT_1D;
import jwave.*;
import jwave.transforms.AncientEgyptianDecomposition;
import jwave.transforms.DiscreteFourierTransform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.haar.Haar1;

public class FrequencyScanner {
    private double[] window;
    double SinglePi = Math.PI;
    double DoublePi = 2*Math.PI;

    public double extractFrequency(short[] sampleData, int sampleRate) {

        DoubleDCT_1D fft = new DoubleDCT_1D(sampleData.length + 24 * sampleData.length);
        double[] a = new double[(sampleData.length + 24 * sampleData.length) * 2];
        System.arraycopy(applyWindow(sampleData), 0, a, 0, sampleData.length);
        fft.forward(a, false);

        /* find the peak magnitude and it's index */
        double maxMag = Double.NEGATIVE_INFINITY;
        int maxInd = -1;
        for (int i = 0; i < a.length / 2; ++i) {
            double re = a[2 * i];
            double im = a[2 * i + 1];
            double mag = Math.sqrt(re * re + im * im);
//            double mag = a[i];
            if (mag > maxMag) {
                maxMag = mag;
                maxInd = i;
            }
        }
        /* calculate the frequency */
        return (double) sampleRate * maxInd / (a.length / 2);


//        double[] b = new double[sampleData.length];
//        Complex[] frame = new Complex[sampleData.length];
//        for (int i = 0; i<sampleData.length; i++) {
//            b[i] = sampleData[i];
//            frame[i] = new Complex(sampleData[i], 0);
//        }
//
//        Complex[] spectrum = decimationInFrequency(frame, true);
//        double[] spectrogram = new double[frame.length];
//        for (int i = 0; i < frame.length; i++)
//            spectrogram[i] = spectrum[i].division(frame.length).abs();
//        Transform t = TransformBuilder.create( "Fast Wavelet Transform", "Haar" );
//        Transform t = new Transform(
//                new AncientEgyptianDecomposition(
//                        new FastWaveletTransform(
//                                new Haar1( ) ) ) );
//        Log.d("b length", b.length+"");
//        Log.d("b", b[4]+"");
//        double[] arrFreq = t.forward( b );
        /* sampleData + zero padding */
//        DoubleFFT_1D fft = new DoubleFFT_1D(sampleData.length + 24 * sampleData.length);
//        DoubleDCT_1D fft = new DoubleDCT_1D(4096);
//        double[] arrFreq = t.forward( a );
//        double[ ] arrReco = t.reverse( arrFreq );
//        fft.complexForward(a);
//        double[] someresult = discreteHaarWaveletTransform(sampleData);
    }
    private void buildHammWindow(int size) {
        if (window != null && window.length == size) {
            return;
        }
        window = new double[size];
        for (int i = 0; i < size; ++i) {
            window[i] = .54 - .46 * Math.cos(2 * Math.PI * i / (size - 1.0));
        }
    }
    private double[] applyWindow(short[] input) {
        double[] res = new double[input.length];
        buildHammWindow(input.length);
        for (int i = 0; i < input.length; ++i) {
            res[i] = (double) input[i] * window[i];
//            res[i] = (double) input[i];
        }
        return res;
    }




    /**/
    public double[] applyHannWindow(double[] input){
        double[] out = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            double mul = 0.5 * (1 - Math.cos(2*Math.PI*i/input.length-1));
            out[i] = mul * input[i];
        }
        return out;
    }

    public static double[] discreteHaarWaveletTransform(short[] input) {
        // This function assumes that input.length=2^n, n>1
        double[] output = new double[input.length];

        for (int length = input.length / 2; ; length = length / 2) {
            // length is the current length of the working area of the output array.
            // length starts at half of the array size and every iteration is halved until it is 1.
            for (int i = 0; i < length; ++i) {
                int sum = input[i * 2] + input[i * 2 + 1];
                int difference = input[i * 2] - input[i * 2 + 1];
                output[i] = sum;
                output[length + i] = difference;
            }
            if (length == 1) {
                return output;
            }

            //Swap arrays to do next iteration
//            System.arraycopy(output, 0, input, 0, length);
        }
    }



    public static Complex[] decimationInFrequency(Complex[] frame, boolean direct)
    {
        if (frame.length == 1) return frame;
        double halfSampleSize = frame.length / 2; // frame.Length/2
        int fullSampleSize = frame.length;

        double arg = direct ? -Math.PI/fullSampleSize : Math.PI/fullSampleSize;
        Complex omegaPowBase = new Complex(Math.cos(arg), Math.sin(arg));
        Complex omega = new Complex(1, 0);
        Complex[] spectrum = new Complex[fullSampleSize];

        for (int j = 0; j < halfSampleSize; j++)
        {
            spectrum[j] = frame[j].plus(frame[(int) (j + halfSampleSize)]);
            spectrum[(int) (j + halfSampleSize)] = omega.times(frame[j].minus(frame[(int) (j + halfSampleSize)]));
            omega.times(omegaPowBase);
        }

        Complex[] yTop = new Complex[(int) halfSampleSize];
        Complex[] yBottom = new Complex[(int) halfSampleSize];
        for (int i = 0; i < halfSampleSize; i++)
        {
            yTop[i] = spectrum[i];
            yBottom[i] = spectrum[(int) (i + halfSampleSize)];
        }

        yTop = decimationInFrequency(yTop, direct);
        yBottom = decimationInFrequency(yBottom, direct);
        for (int i = 0; i < halfSampleSize; i++)
        {
            int j = i << 1; // i = 2*j;
            spectrum[j] = yTop[i];
            spectrum[j + 1] = yBottom[i];
        }

        return spectrum;
    }
}