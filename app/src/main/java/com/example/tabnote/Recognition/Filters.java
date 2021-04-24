package com.example.tabnote.Recognition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by alexey on 13.01.16.
 */
public class Filters {


    //  Δ∂ωπ

    public final double SinglePi = Math.PI;
    public static final double DoublePi = 2 * Math.PI;

    public static LinkedHashMap<Double, Double> GetJoinedSpectrum(
            ArrayList<Complex> spectrum0, ArrayList<Complex> spectrum1,
            double shiftsPerFrame, double sampleRate) {

        int frameSize = spectrum0.size();//?????
        double frameTime = frameSize / sampleRate;
        double shiftTime = frameTime / shiftsPerFrame;
        double binToFrequancy = sampleRate / frameSize;
        LinkedHashMap<Double, Double> dictionary = new LinkedHashMap<>();//new Dictionary

        for (int bin = 0; bin < frameSize; bin++) {
            double omegaExpected = DoublePi * (bin * binToFrequancy); // ω=2πf
            double omegaActual = (spectrum1.get(bin).phase() - spectrum0.get(bin).phase()) / shiftTime; // ω=∂φ/∂t
            double omegaDelta = Align(omegaActual - omegaExpected, DoublePi); // Δω=(∂ω + π)%2π - π
            double binDelta = omegaDelta / (DoublePi * binToFrequancy);
            double frequancyActual = (bin + binDelta) * binToFrequancy;
            double magnitude = spectrum1.get(bin).abs() + spectrum0.get(bin).abs();
            dictionary.put(frequancyActual, magnitude * (0.5 + Math.abs(binDelta)));
        }

        return dictionary;
    }


    public static LinkedHashMap<Integer, Integer> GetJoinedSpectrum(
            Complex[] spectrum0, Complex[] spectrum1,
            double shiftsPerFrame, double sampleRate) {

        int frameSize = spectrum0.length;//?????
        double frameTime = frameSize / sampleRate;
        double shiftTime = frameTime / shiftsPerFrame;
        double binToFrequancy = sampleRate / frameSize;
        LinkedHashMap<Integer, Integer> dictionary = new LinkedHashMap<>();//new Dictionary

//        new Thread(() -> {
//            for (int bin = frameSize/2; bin < frameSize; bin++) {
//                double omegaExpected = DoublePi * (bin * binToFrequancy); // ω=2πf
//                double omegaActual = (spectrum1[bin].phase() - spectrum0[bin].phase()) / shiftTime; // ω=∂φ/∂t
//                double omegaDelta = Align(omegaActual - omegaExpected, DoublePi); // Δω=(∂ω + π)%2π - π
//                double binDelta = omegaDelta / (DoublePi * binToFrequancy);
//                double frequancyActual = (bin + binDelta) * binToFrequancy;
//                double magnitude = spectrum1[bin].abs() + spectrum0[bin].abs();
//                dictionary.put((int) Math.round(frequancyActual), (int) Math.round(magnitude * (0.5 + Math.abs(binDelta))));
//            }
//        }).start();
        for (int bin = 0; bin < frameSize; bin++) {
            double omegaExpected = DoublePi * (bin * binToFrequancy); // ω=2πf
            double omegaActual = (spectrum1[bin].phase() - spectrum0[bin].phase()) / shiftTime; // ω=∂φ/∂t
            double omegaDelta = Align(omegaActual - omegaExpected, DoublePi); // Δω=(∂ω + π)%2π - π
            double binDelta = omegaDelta / (DoublePi * binToFrequancy);
            double frequancyActual = (bin + binDelta) * binToFrequancy;
            double magnitude = spectrum1[bin].abs() + spectrum0[bin].abs();
            dictionary.put((int) Math.round(frequancyActual), (int) Math.round(magnitude * (0.5 + Math.abs(binDelta))));
        }

        return dictionary;
    }

    public static double Align(double angle, double period) {
        int qpd = (int) (angle / period);
        if (qpd >= 0) qpd += qpd & 1;
        else qpd -= qpd & 1;
        angle -= period * qpd;
        return angle;
    }

    public static LinkedHashMap <Integer, Integer> Antialiasing(LinkedHashMap<Integer, Integer> spectrum) {
        LinkedHashMap result = new LinkedHashMap<>();
        List keys = new ArrayList(spectrum.keySet());
        List values = new ArrayList(spectrum.values());
        for (int j = 0; j < spectrum.size() - 4; j++) {
            int i = j;
            int x0 = (int) keys.get(i);
            int x1 = (int) keys.get(i+1);
            int y0 = (int) values.get(i);
            int y1 = (int) values.get(i+1);

            double a = (y1 - y0)/(x1 - x0);
            double b = y0 - a*x0;

            i += 2;
            int u0 = (int) keys.get(i);
            int u1 = (int) keys.get(i+1);
            int v0 = (int) values.get(i);
            int v1 = (int) values.get(i+1);

            double c = (v1 - v0)/(u1 - u0);
            double d = v0 - c*u0;

            int x = (int) ((d - b)/(a - c));
            int y = (int) ((a*d - b*c)/(a - c));

            if (y > y0 && y > y1 && y > v0 && y > v1 &&
                    x > x0 && x > x1 && x < u0 && x < u1)
            {
                result.put(x1, y1);
                result.put(x, y);
            }
            else
            {
                result.put(x1, y1);
            }
        }

        return result;
    }
        }
//    public double getPitchInSampleRange(AudioSamples as, int start, int end) throws Exception {
//        //If your sound is musical note/voice you need to limit the results because it wouldn't be above 4500Hz or bellow 20Hz
//        int nLowPeriodInSamples = (int) as.getSamplingRate() / 4500;
//        int nHiPeriodInSamples = (int) as.getSamplingRate() / 20;
//
//        //I get my sample values from my AudioSamples class. You can get them from wherever you want
//        double[] samples = Arrays.copyOfRange((as.getSamplesChannelSegregated()[0]), start, end);
//        if(samples.length < nHiPeriodInSamples) throw new Exception("Not enough samples");
//
//        //Since we're looking the periodicity in samples, in our case it won't be more than the difference in sample numbers
//        double[] results = new double[nHiPeriodInSamples - nLowPeriodInSamples];
//
//        //Now you iterate the time lag
//        for(int period = nLowPeriodInSamples; period < nHiPeriodInSamples; period++) {
//            double sum = 0;
//            //Autocorrelation is multiplication of the original and time lagged signal values
//            for(int i = 0; i < samples.length - period; i++) {
//                sum += samples[i]*samples[i + period];
//            }
//            //find the average value of the sum
//            double mean = sum / (double)samples.length;
//            //and put it into results as a value for some time lag.
//            //You subtract the nLowPeriodInSamples for the index to start from 0.
//            results[period - nLowPeriodInSamples] = mean;
//        }
//        //Now, it is obvious that the mean will be highest for time lag equal to the periodicity of the signal because in that case
//        //most of the positive values will be multiplied with other positive and most of the negative values will be multiplied with other
//        //negative resulting again as positive numbers and the sum will be high positive number. For example, in the other case, for let's say half period
//        //autocorrelation will multiply negative with positive values resulting as negatives and you will get low value for the sum.
//        double fBestValue = Double.MIN_VALUE;
//        int nBestIndex = -1; //the index is the time lag
//        //So
//        //The autocorrelation is highest at the periodicity of the signal
//        //The periodicity of the signal can be transformed to frequency
//        for(int i = 0; i < results.length; i++) {
//            if(results[i] > fBestValue) {
//                nBestIndex = i;
//                fBestValue = results[i];
//            }
//        }
//        //Convert the period in samples to frequency and you got yourself a fundamental frequency of a sound
//        double res = as.getSamplingRate() / (nBestIndex + nLowPeriodInSamples);
//
//        return res;
//    }



//        public static int calculate(int sampleRate, short [] audioData){
//
//        int numSamples = audioData.length;
//        int numCrossing = 0;
//        for (int p = 0; p < numSamples-1; p++)
//        {
//            if ((audioData[p] > 0 && audioData[p + 1] <= 0) ||
//                    (audioData[p] < 0 && audioData[p + 1] >= 0))
//            {
//                numCrossing++;
//            }
//        }
//        float numSecondsRecorded = (float)numSamples/(float)sampleRate;
//        float numCycles = numCrossing/2;
//        float frequency = numCycles/numSecondsRecorded;
//
//        return (int)frequency;
//    }
