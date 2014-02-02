package org.farabiproject.mdf;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;

/**
 * Created by Bahadir on 02.02.2014.
 */
@XmlRootElement(name = "mdf")
public class MusicDataFile {
    // number of channels of PCM samples output by this decoder.
    @XmlElement
    private int outputFrequency;
    @XmlElement
    private int outputChannels;
    @XmlElement
    private MDFMetaTag tags;
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
