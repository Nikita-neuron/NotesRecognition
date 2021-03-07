package com.example.tabnote;

import android.content.Context;
import android.util.Log;

/**
 * Created by alexey on 08.01.16.
 */
public class Complex {
    /******************************************************************************
     * Compilation:  javac Complex.java
     * Execution:    java Complex
     * <p>
     * Data type for complex numbers.
     * <p>
     * The data type is "immutable" so once you create and initialize
     * a Complex object, you cannot change it. The "final" keyword
     * when declaring re and im enforces this rule, making it a
     * compile-time error to change the .re or .im fields after
     * they've been initialized.
     * <p>
     * % java Complex
     * a            = 5.0 + 6.0i
     * b            = -3.0 + 4.0i
     * Re(a)        = 5.0
     * Im(a)        = 6.0
     * b + a        = 2.0 + 10.0i
     * a - b        = 8.0 + 2.0i
     * a * b        = -39.0 + 2.0i
     * b * a        = -39.0 + 2.0i
     * a / b        = 0.36 - 1.52i
     * (a / b) * b  = 5.0 + 6.0i
     * conj(a)      = 5.0 - 6.0i
     * |a|          = 7.810249675906654
     * tan(a)       = -6.685231390246571E-6 + 1.0000103108981198i
     ******************************************************************************/

    public double re;   // the real part
    public double im;   // the imaginary part
    public double abs = Math.sqrt(re * re + im * im);

    // create a new object with the given real and imaginary parts
    public Complex(double real, double imag) {
        re = real;
        im = imag;
    }

    public Complex() {

    }

    // return a string representation of the invoking Complex object
    public String toString() {
        if (im == 0) return re + "";
        if (re == 0) return im + "i";
        if (im < 0) return re + " - " + (-im) + "i";
        return re + " + " + im + "i";
    }

    // return abs/modulus/magnitude and angle/phase/argument
    public double abs() {
        return Math.hypot(re, im);
    }  // Math.sqrt(re*re + im*im)

    public double absOne(Complex a) {
        return Math.sqrt(a.re * a.re + a.im * a.im);
    }

    public double phase() {
        return Math.atan2(im, re);
    }  // between -pi and pi

    // return a new Complex object whose value is (this + b)
    public Complex plus(Complex b) {
        Complex a = this;             // invoking object
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new Complex(real, imag);
    }

    // return a new Complex object whose value is (this - b)
    public Complex minus(Complex b) {
        Complex a = this;
        double real = a.re - b.re;
        double imag = a.im - b.im;
        return new Complex(real, imag);
    }

    // return a new Complex object whose value is (this * b)
    public Complex times(Complex b) {
        Complex a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new Complex(real, imag);
    }

    // scalar multiplication
    // return a new object whose value is (this * alpha)
    public Complex times(double alpha) {
        return new Complex(alpha * re, alpha * im);
    }

    // return a new Complex object whose value is the conjugate of this
    public Complex conjugate() { //сопряженное
        return new Complex(re, -im);
    }

    // return a new Complex object whose value is the reciprocal of this
    public Complex reciprocal() {
        double scale = re * re + im * im;
        return new Complex(re / scale, -im / scale);
    }

    // return the real or imaginary part
    //реальная часть косплексного числа
    public double re() {
        return re;
    }

    //мнимая часть комплексного числа
    public double im() {
        return im;
    }

    // return a / b
    public Complex divides(Complex b) {
        Complex a = this;
        return a.times(b.reciprocal());
    }

    // return a new Complex object whose value is the complex exponential of this
    public Complex exp() {
        return new Complex(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
    }

    // return a new Complex object whose value is the complex sine of this
    public Complex sin() {
        return new Complex(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
    }

    // return a new Complex object whose value is the complex cosine of this
    public Complex cos() {
        return new Complex(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
    }

    // return a new Complex object whose value is the complex tangent of this
    public Complex tan() {

        return sin().divides(cos());
    }

    public Complex ctg() {

        return cos().divides(sin());
    }

    public Complex PoweredE() {
        double e = Math.exp(re);
        return new Complex(e * Math.cos(im), e * Math.sin(im));
    }

    final Object b = "";
    boolean worked = false;

    public Complex[] realToComplex(short arr[]) {
        Complex[] complex = new Complex[arr.length];
        try {


//        int first_block = arr.length / 3;
        int second_block = arr.length;
//        new Thread(() -> {
//                worked = false;
//                for (int i = 0; i < first_block; i++) {
//                    complex[i] = new Complex(arr[i], 0);
//                }
//                worked = true;
//        }).start();

        for (int i = 0; i < second_block; i++) {
            complex[i] = new Complex(arr[i], 0);
        }

//        while(!worked){
//            try {
//                Thread.sleep(10);
////                    Log.i("FFT","sleeping");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        } catch (Exception e) {
            Log.e("Complex", e.toString());
        }
        return complex;
    }

    public short[] complexToShort(Complex[] arr) {
        short[] mas = new short[arr.length];
        for (int i = 0; i < arr.length; i++) {
            mas[i] = (short) Math.sqrt(arr[i].re * arr[i].re + arr[i].im * arr[i].im);
        }
        return mas;
    }
}
