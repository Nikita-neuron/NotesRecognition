package com.example.tabnote;

import java.util.Objects;

public class Complex_1 {
    private final double re;   // the real part
    private final double im;   // the imaginary part

    // create a new object with the given real and imaginary parts
    public Complex_1(double real, double imag) {
        re = real;
        im = imag;
    }

    // return a string representation of the invoking Complex object
    public String toString() {
        if (im == 0) return re + "";
        if (re == 0) return im + "i";
        if (im < 0) return re + " - " + (-im) + "i";
        return re + " + " + im + "i";
    }

    // return abs/modulus/magnitude
    public double abs() {
        return Math.hypot(re, im);
    }

    // return angle/phase/argument, normalized to be between -pi and pi
    public double phase() {
        return Math.atan2(im, re);
    }

    // return a new Complex object whose value is (this + b)
    public Complex_1 plus(Complex_1 b) {
        Complex_1 a = this;             // invoking object
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new Complex_1(real, imag);
    }

    // return a new Complex object whose value is (this - b)
    public Complex_1 minus(Complex_1 b) {
        Complex_1 a = this;
        double real = a.re - b.re;
        double imag = a.im - b.im;
        return new Complex_1(real, imag);
    }

    // return a new Complex object whose value is (this * b)
    public Complex_1 times(Complex_1 b) {
        Complex_1 a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new Complex_1(real, imag);
    }

    // return a new object whose value is (this * alpha)
    public Complex_1 scale(double alpha) {
        return new Complex_1(alpha * re, alpha * im);
    }

    public Complex_1 division(double alpha) {
        return new Complex_1(re/alpha, im/alpha);
    }

    // return a new Complex object whose value is the conjugate of this
    public Complex_1 conjugate() {
        return new Complex_1(re, -im);
    }

    // return a new Complex object whose value is the reciprocal of this
    public Complex_1 reciprocal() {
        double scale = re * re + im * im;
        return new Complex_1(re / scale, -im / scale);
    }

    // return the real or imaginary part
    public double re() {
        return re;
    }

    public double im() {
        return im;
    }

    // return a / b
    public Complex_1 divides(Complex_1 b) {
        Complex_1 a = this;
        return a.times(b.reciprocal());
    }

    // return a new Complex object whose value is the complex exponential of this
    public Complex_1 exp() {
        return new Complex_1(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
    }

    // return a new Complex object whose value is the complex sine of this
    public Complex_1 sin() {
        return new Complex_1(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
    }

    // return a new Complex object whose value is the complex cosine of this
    public Complex_1 cos() {
        return new Complex_1(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
    }

    // return a new Complex object whose value is the complex tangent of this
    public Complex_1 tan() {
        return sin().divides(cos());
    }


    // a static version of plus
    public static Complex_1 plus(Complex_1 a, Complex_1 b) {
        double real = a.re + b.re;
        double imag = a.im + b.im;
        Complex_1 sum = new Complex_1(real, imag);
        return sum;
    }

    // See Section 3.3.
    public boolean equals(Object x) {
        if (x == null) return false;
        if (this.getClass() != x.getClass()) return false;
        Complex_1 that = (Complex_1) x;
        return (this.re == that.re) && (this.im == that.im);
    }

    // See Section 3.3.
    public int hashCode() {
        return Objects.hash(re, im);
    }
}