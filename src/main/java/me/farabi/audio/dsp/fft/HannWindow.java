package me.farabi.audio.dsp.fft;

/**
 * Created by umutcanguney on 15/03/14.
 */
public class HannWindow extends WindowFunction {
    public HannWindow() {

    }

    @Override
    protected float value(int length, int n) {
        return 0.5f * (1f - (float) Math.cos(TWO_PI * n / length - 1f));
    }
}
