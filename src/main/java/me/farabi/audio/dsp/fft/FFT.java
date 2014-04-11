package me.farabi.audio.dsp.fft;

/**
 * Created by umutcanguney on 15/03/14.
 * Calculates the FFT of a given byte array.
 * inspired from https://github.com/JorenSix/TarsosDSP
 */

public class FFT {
    private final FloatFFT fft;
    private final WindowFunction wf;
    private final int fftSize;

    public FFT(final int size) {
        this(size,null);
    }

    public FFT(final int size, final WindowFunction windowFunction) {
        this.fftSize = size;
        this.fft = new FloatFFT(this.fftSize);
        this.wf = windowFunction;
    }

    public void forwardTransform(final float[] data) {
        if (this.wf != null) {
            this.wf.apply(data);
        }

        this.fft.realForward(data);
    }

    public void complexForwardTransform(final float[] data) {
        if (this.wf != null) {
            this.wf.apply(data);
        }

        this.fft.complexForward(data);
    }

    public float[] transformAndGetMagnitudes(final float[] data) {
        forwardTransform(data);
        return calculatePowers(data);
    }

    public double[] transformAndGetDecibels(final float[] data) {
        forwardTransform(data);
        return calculateDecibels(data);
    }

    public double binToHz(final int binIndex, final float sampleRate) {
        return binIndex * sampleRate / (double) this.fftSize;
    }

    public float calculatePower(float real, float im, final int index) {
        return (float) Math.sqrt(real*real + im*im);
    }

    public float[] calculatePowers(final float[] data) {
        float[] magnitudes = new float[data.length / 2];
        calculatePowers(data, magnitudes);
        return magnitudes;
    }

    public void calculatePowers(final float[] data, final float[] magnitudes) {
        int l = magnitudes.length;
        for (int i = 0; i < l; i++) {
            magnitudes[i] = calculatePower(data[2*i], data[2*i + 1], i);
        }
    }

    public double calculateDecibel(float real, float im) {
        return (double) 20*Math.log10(Math.sqrt(real*real + im*im) / this.fftSize);
    }

    public double[] calculateDecibels(final float[] data) {
        double[] magnitudes = new double[data.length / 2];
        calculateDecibels(data, magnitudes);
        return magnitudes;
    }

    public void calculateDecibels(final float[] data, final double[] magnitudes) {
        int l = magnitudes.length;
        for (int i = 0; i < l; i++) {
            magnitudes[i] = calculateDecibel(data[2*i], data[2*i + 1]);
        }
    }

}
