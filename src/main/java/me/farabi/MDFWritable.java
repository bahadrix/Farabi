package me.farabi;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import javazoom.jl.decoder.*;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

import java.io.*;

/**
 * Farabi
 * I think this class gonna replace that MDFDataFile class..
 * Working on it..
 * User: Bahadir
 * Date: 04.02.2014
 * Time: 09:37
 */

@SuppressWarnings("UnusedDeclaration") // kullanmamis olabiliris ama kullancas, soz!
public class MDFWritable implements Writable {


    public MDFSongTags tags;


    protected IntWritable outputFrequency = new IntWritable(0);
    protected IntWritable outputChannels = new IntWritable(0);
    protected IntWritable bitrate = new IntWritable(0);
    protected IntWritable framesize = new IntWritable(0);
    protected BooleanWritable vbr = new BooleanWritable(false);
    protected BytesWritable fileData = new BytesWritable(new byte[]{0});

    public MDFWritable() {
        tags = new MDFSongTags();
    }

    public MDFWritable(byte[] fileData, long fileLength, boolean decodeRaw) throws InvalidDataException, IOException, UnsupportedTagException, BitstreamException, DecoderException {

        // Get tags ==================================================================
        tags = new MDFSongTags(new ByteArrayInputStream(fileData), fileLength, 0);


        // Get fileData ==============================================================

        Decoder decoder = new Decoder();
        Bitstream stream = new Bitstream(new ByteArrayInputStream(fileData));
        ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);

        int frameCount = Integer.MAX_VALUE;
        SampleBuffer buff;

        boolean swap = false;

        // Decoded veriyi sifir basiyo gibi gozukuyo ama Lame ile 16bit red deneyince de
        // tomarla sifir geliyo.

        for (int frame = 0; frame < frameCount; frame++) {
            Header header = stream.readFrame();
            if (header == null) {
                break;
            }
            if (frame == 0) {
                setOutputChannels(header.mode() == Header.SINGLE_CHANNEL ? 1 : 2);
                setOutputFrequency(header.frequency());
                setBitrate(header.bitrate());
                setFramesize(header.framesize);
                setVbr(header.vbr());
                swap = header.frequency() >= 44000; // bkz: http://stackoverflow.com/a/15187707
                if (!decodeRaw)
                    break;
            }

            buff = (SampleBuffer) decoder.decodeFrame(header, stream);

            //fileData = ArrayUtils.addAll(fileData, buff.getBuffer());
            short[] pcm = buff.getBuffer();
            for (short s : pcm) {
                // bkz: http://stackoverflow.com/a/15187707

                if (swap) {
                    bout.write(s & 0xff);
                    bout.write((s >> 8) & 0xff);
                } else {
                    bout.write((s >> 8) & 0xff);
                    bout.write(s & 0xff);
                }
            }

            stream.closeFrame();
        }

        if (!decodeRaw) {
            setFileData(new BytesWritable(
                    fileData
            ));
        } else {
            setFileData(new BytesWritable(
                    bout.toByteArray()
            ));
        }

    }

    public MDFWritable(File mp3File, boolean decodeRaw) throws IOException, DecoderException, BitstreamException, InvalidDataException, UnsupportedTagException {
        this(FileUtils.readFileToByteArray(mp3File), mp3File.length(), decodeRaw);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        tags.write(out);
        fileData.write(out);
        outputFrequency.write(out);
        outputChannels.write(out);
        bitrate.write(out);
        framesize.write(out);
        vbr.write(out);
        fileData.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        tags.readFields(in);
        fileData.readFields(in);
        outputFrequency.readFields(in);
        outputChannels.readFields(in);
        bitrate.readFields(in);
        framesize.readFields(in);
        vbr.readFields(in);
        fileData.readFields(in);
    }

    public BytesWritable getFileData() {
        return fileData;
    }

    public void setFileData(BytesWritable fileData) {
        this.fileData = fileData;
    }

    public IntWritable getOutputFrequency() {
        return outputFrequency;
    }


    public void setOutputFrequency(int outputFrequency) {
        this.outputFrequency = new IntWritable(outputFrequency);
    }

    public IntWritable getOutputChannels() {
        return outputChannels;
    }


    public void setOutputChannels(int outputChannels) {
        this.outputChannels = new IntWritable(outputChannels);
    }

    public IntWritable getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = new IntWritable(bitrate);
    }


    public IntWritable getFramesize() {
        return framesize;
    }

    public void setFramesize(int framesize) {
        this.framesize = new IntWritable(framesize);
    }

    public boolean isVbr() {
        return vbr.get();
    }


    public void setVbr(boolean vbr) {
        this.vbr = new BooleanWritable(vbr);
    }
}
