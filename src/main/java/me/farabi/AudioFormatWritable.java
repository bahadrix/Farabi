package me.farabi;

import org.apache.hadoop.io.*;
import org.apache.log4j.Logger;

import javax.sound.sampled.AudioFormat;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

@SuppressWarnings("UnusedDeclaration")
public class AudioFormatWritable implements Writable {
    private static org.apache.log4j.Logger log = Logger.getLogger(AudioFormatWritable.class);

    public Text encoding = new Text("");
    public FloatWritable sampleRate;
    public IntWritable sampleSizeInBits;
    public IntWritable channels;
    public IntWritable frameSize;
    public FloatWritable frameRate;
    public BooleanWritable bigEndian;
    public MapWritable properties;

    public Text stringRep = new Text("");

    public AudioFormatWritable() {
        sampleRate = new FloatWritable(0);
        sampleSizeInBits = new IntWritable(0);
        channels = new IntWritable(0);
        frameSize = new IntWritable(0);
        frameRate = new FloatWritable();
        bigEndian = new BooleanWritable();
    }

    public AudioFormatWritable(AudioFormat format) {
        setEncoding(format.getEncoding().toString());
        setSampleRate(format.getSampleRate());
        setSampleSizeInBits(format.getSampleSizeInBits());
        setChannels(format.getChannels());
        setFrameSize(format.getFrameSize());
        setFrameRate(format.getFrameRate());
        setBigEndian(format.isBigEndian());
        setProperties(format.properties());
        setStringRep(format.toString());
    }

    @Override
    public void write(DataOutput out) throws IOException {
        encoding.write(out);
        sampleRate.write(out);
        sampleSizeInBits.write(out);
        channels.write(out);
        frameSize.write(out);
        frameRate.write(out);
        bigEndian.write(out);
        properties.write(out);
        stringRep.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        encoding.readFields(in);
        sampleRate.readFields(in);
        sampleSizeInBits.readFields(in);
        channels.readFields(in);
        frameSize.readFields(in);
        frameRate.readFields(in);
        bigEndian.readFields(in);
        properties.readFields(in);
        stringRep.readFields(in);
    }

    public void setStringRep(String s) {
        this.stringRep = new Text(s);
    }

    public Text getStringRep() {
        return this.stringRep;
    }

    public Text getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding == null ? new Text() : new Text(encoding);
    }

    public FloatWritable getSampleRate() {
        return this.sampleRate;
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = new FloatWritable(sampleRate);
    }

    public IntWritable getSampleSizeInBits() {
        return this.sampleSizeInBits;
    }

    public void setSampleSizeInBits(int sampleRateInBits) {
        this.sampleSizeInBits = new IntWritable(sampleRateInBits);
    }

    public IntWritable getChannels() {
        return this.channels;
    }

    public void setChannels(int channels) {
        this.channels = new IntWritable(channels);
    }

    public IntWritable getFrameSize() {
        return this.frameSize;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = new IntWritable(frameSize);
    }

    public FloatWritable getFrameRate() {
        return this.frameRate;
    }

    public void setFrameRate(float frameRate) {
        this.frameRate = new FloatWritable(frameRate);
    }

    public boolean isBigEndian() {
        return this.bigEndian.get();
    }

    public void setBigEndian(boolean b) {
        this.bigEndian = new BooleanWritable(b);
    }

    public MapWritable getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, Object> map) {
        for (String s : map.keySet()) {
            if (!this.properties.containsKey(new Text(s))) {
                this.properties.put(new Text(s), new ObjectWritable(map.get(s)));
            }
        }
    }

}
