package me.farabi.audio;

import me.farabi.audio.dsp.utils.AudioFloatConverter;

import javax.sound.sampled.AudioFormat;

/**
 * Created by umutcanguney on 16/03/14.
 * This class will handle calculations for audio analysis of a given frame.
 */
public class AudioEvent {

    private final AudioFormat format;

    private final AudioFloatConverter converter;

    /**
     * The audio data encoded in floats from -1.0 to 1.0.
     */
    private float[] floatBuffer;

    /**
     * The audio data encoded in bytes from -128 to 127.
     */
    private byte[] byteBuffer;

    /**
     * fft overlap in samples
     */
    private int overlap;

    /**
     * The length of the stream, expressed in sample frames rather than bytes
     */
    private long frameLength;

    public AudioEvent(AudioFormat format,long frameLength){
        this.format = format;
        this.converter = AudioFloatConverter.getConverter(format);
        this.overlap = 0;
        this.frameLength = frameLength;
    }

    public long getFrameLength() {
        return frameLength;
    }

    public void setFrameLength(long frameLength) {
        this.frameLength = frameLength;
    }

    public int getOverlap() {
        return overlap;
    }

    public void setOverlap(int overlap) {
        this.overlap = overlap;
    }

    public byte[] getByteBuffer() {
        int length = getFloatBuffer().length * format.getFrameSize();
        if(byteBuffer == null || byteBuffer.length != length){
            byteBuffer = new byte[length];
        }
        converter.toByteArray(getFloatBuffer(), byteBuffer);
        return byteBuffer;
    }

    public void setByteBuffer(byte[] byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public float[] getFloatBuffer() {
        return floatBuffer;
    }

    public void setFloatBuffer(float[] floatBuffer) {
        this.floatBuffer = floatBuffer;
    }

    public AudioFloatConverter getConverter() {
        return this.converter;
    }

}
