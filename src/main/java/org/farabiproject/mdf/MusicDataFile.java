package org.farabiproject.mdf;

import javazoom.jl.decoder.*;
import org.apache.commons.lang.ArrayUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;

/**
 * MDF Object.
 * Created by Bahadir on 02.02.2014.
 */
@Deprecated // Bunun yerine MDFWritable daha guzel oluyo.
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement(name = "mdf")
public class MusicDataFile {
    // number of channels of PCM samples output by this decoder.
    @XmlElement private int outputFrequency;
    @XmlElement private int outputChannels;
    @XmlElement private MDFMetaTag tags;
    @XmlElement private int bitrate;
    @XmlElement private int framesize;

    @XmlElement(name = "isVBR")
    private boolean vbr;

    @XmlElement @XmlList
    private short[] frames;


    public MusicDataFile() {}

    /**
     * Get MDF Ibject from Serialized DataStream
     * @param MDFInputStream Serialized data of MDF
     */
    public MusicDataFile(InputStream MDFInputStream) {

    }

    public void decode(InputStream inputStream) {
    }

    /**
     * Create MDF Object from MP3 DataStream
     * @param inputStream InputStream of MP3 file
     * @return Unseralized MDF Object
     */
    public static MusicDataFile create(InputStream inputStream) {


        MusicDataFile mdf = new MusicDataFile();

        //TODO: Make mdf from mp3 file

        Decoder decoder = new Decoder();
        Bitstream stream = new Bitstream(inputStream);

        int frameCount = Integer.MAX_VALUE;
        SampleBuffer buff;

        short[] frames = null;

        try {
            for (int frame = 0; frame < frameCount; frame++) {
                Header header = stream.readFrame();
                if (header == null) {
                    break;
                }
                if(frame == 0) {
                    mdf.setOutputChannels(header.mode() == Header.SINGLE_CHANNEL ? 1 : 2);
                    mdf.setOutputFrequency(header.frequency());
                    mdf.setBitrate(header.bitrate());
                    mdf.setFramesize(header.framesize);
                    mdf.setVbr(header.vbr());

                }

                /*
                if (decoder.getOutputChannels() == 0) {
                    int channels = (header.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
                    int freq = header.frequency();
                    decoder.initOutputBuffer(channels, freq, destFileName);
                }
                */
                buff = (SampleBuffer)decoder.decodeFrame(header, stream);

                frames = ArrayUtils.addAll(frames, buff.getBuffer());

                stream.closeFrame();
            }
        } catch (BitstreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (DecoderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        mdf.setFrames(frames);
        return mdf;
    }

    public int getOutputFrequency() {
        return outputFrequency;
    }

    public int getOutputChannels() {
        return outputChannels;
    }

    public MDFMetaTag getTags() {
        return tags;
    }

    public short[] getFrames() {
        return frames;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getFramesize() {
        return framesize;
    }

    public boolean isVbr() {
        return vbr;
    }

    void setVbr(boolean vbr) {
        this.vbr = vbr;
    }

    void setFramesize(int framesize) {
        this.framesize = framesize;
    }

    void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    //bu metotlar public olmadigindan boyle aurica belirtmek durumundayiz
    void setOutputFrequency(int outputFrequency) {
        this.outputFrequency = outputFrequency;
    }

    void setOutputChannels(int outputChannels) {
        this.outputChannels = outputChannels;
    }

    void setTags(MDFMetaTag tags) {
        this.tags = tags;
    }

    void setFrames(short[] frames) {
        this.frames = frames;
    }

}
