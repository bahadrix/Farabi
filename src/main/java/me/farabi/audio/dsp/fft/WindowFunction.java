package me.farabi.audio.dsp.fft;

/**
 * A Window function represents a curve which is applied to a sample buffer to
 * reduce the introduction of spectral leakage in the Fourier transform.
 *
 * Windowing is the process of shaping the audio samples before transforming
 * them to the frequency domain. The Fourier Transform assumes the sample buffer
 * is is a repetitive signal, if a sample buffer is not truly periodic within
 * the measured interval sharp discontinuities may arise that can introduce
 * spectral leakage. Spectral leakage is the speading of signal energy across
 * multiple FFT bins. This "spreading" can drown out narrow band signals and
 * hinder detection.
 */
public abstract class WindowFunction {
    protected static final float TWO_PI = (float)(2 * (Math.PI));
    protected int length;

    public WindowFunction() {

    }

    public void apply(float[] samples) {
        this.length = samples.length;

        for (int n = 0; n<this.length; n++) {
            samples[n] *= value(this.length,n);
        }
    }

    protected abstract float value(int length, int n);

}
