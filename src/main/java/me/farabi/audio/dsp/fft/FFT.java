package me.farabi.audio.dsp.fft;

/**
 * Created by umutcanguney on 15/03/14.
 * Calculates the FFT of a given byte array.
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

    public double binToHz(final int binIndex, final float sampleRate) {
        return binIndex * sampleRate / (double) this.fftSize;
    }

}
