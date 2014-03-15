package me.farabi.audio.dsp.fft;

/**
 * Created by umutcanguney on 15/03/14.
 * Not sure if this implementation is correct, need to come back later.
 */
public class KaiserWindow extends WindowFunction {
    private double alpha;
    /**
     * Maximum error allowed zeroth order modified bessel function of the first kind.
     */
    private final double IZEROEPS = 1E-21;

    private double Izero(double x) {
        int n = 1;
        double sum = 1.0;
        double D = 1.0;
        double temp;

        while (D >= IZEROEPS * sum) {
            temp = x / (2.0 * n);
            n++;
            temp *= temp;
            D *= temp;
            sum += D;
        }
        return sum;
    }

    public KaiserWindow() {
        this(0.2);
    }

    public KaiserWindow(double alpha) {
        this.alpha = alpha;
    }

    @Override
    protected float value(int length, int n) {
        return (float) (Izero(this.alpha * Math.sqrt(n * (2.0 * length - n)) / length) / Izero(this.alpha));
    }
}
