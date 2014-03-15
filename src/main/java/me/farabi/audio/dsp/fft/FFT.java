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
        float[] magnitudes = calculatePowers(data);
        return magnitudes;
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

}
