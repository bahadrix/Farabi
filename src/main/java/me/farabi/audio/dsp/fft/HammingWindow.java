package me.farabi.audio.dsp.fft;

/**
 * Created by umutcanguney on 15/03/14.
 */
public class HammingWindow extends WindowFunction {

    public HammingWindow() {

    }

    @Override
    protected float value(int length, int n) {
        return 0.54f - 0.46f * (float) Math.cos(TWO_PI * n / length);
    }
}
