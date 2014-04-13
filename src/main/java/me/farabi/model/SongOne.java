package me.farabi.model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import me.farabi.AudioFormatWritable;
import me.farabi.MDFWritable;

/**
 * Farabi
 * Şarkı işlendikten sonra elde edilen bilgilerin tutulduğu kayıt.
 * Bunu seralize edip mongoda tutabilirik.
 * User: Bahadir
 * Date: 18.03.2014
 * Time: 10:19
 */
@SuppressWarnings("UnusedDeclaration")
@Entity("SongOne")
@Indexes({ @Index("tags") })
public class SongOne<T> {
    @Entity("SOTags")
    @Indexes({ @Index("title,artist") })
    static class Tags {
        @Id
        public int ID = 0; // can't be null
        public String title = "<unknown>";
        public String artist = "<unknown>";
        public String album = "<unknown>";
        public String genreDesc = "<unknown>";
        public String year = "<unknown>";


    }

    @SuppressWarnings("FieldCanBeLocal")
    @Id
    private String id;
    private Tags tags;
    private int outputFrequency;
    private int outputChannels;
    private int bitrate;
    private int framesize;
    private boolean vbr;

    /**
     * Contains user defined custom data
     */
    private T data = null;

    public static SongOne createFromMDF(MDFWritable mdf) {
        SongOne sone = new SongOne();
        AudioFormatWritable afw = mdf.getAudioFormatWritable();

        sone.setBitrate(Integer.parseInt(afw.getProperties().get("bitrate").toString()));
//        sone.setBitrate(mdf.getBitrate().get());
        sone.setFramesize(afw.getFrameSize().get());
//        sone.setFramesize(mdf.getFramesize().get());
        sone.setOutputChannels(afw.getChannels().get());
//        sone.setOutputChannels(mdf.getOutputChannels().get());
        sone.setOutputFrequency(((int)afw.getSampleRate().get()));
//        sone.setOutputFrequency(mdf.getOutputFrequency().get());
        sone.setVbr(Boolean.parseBoolean(afw.getProperties().get("vbr").toString()));
//        sone.setVbr(mdf.isVbr());

        Tags tags = new Tags();
        tags.album = mdf.tags.getAlbum().toString();
        tags.artist = mdf.tags.getArtist().toString();
        tags.genreDesc = mdf.tags.getGenreDesc().toString();
        tags.ID = mdf.tags.getID().get();
        tags.title = mdf.tags.getTitle().toString();
        tags.year = mdf.tags.getYear().toString();

        sone.setTags(tags);
        return sone;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
        this.id = String.format("%s/%s/%s/%s",
                tags.artist, tags.album, tags.title, tags.ID);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getOutputFrequency() {
        return outputFrequency;
    }

    public void setOutputFrequency(int outputFrequency) {
        this.outputFrequency = outputFrequency;
    }

    public int getOutputChannels() {
        return outputChannels;
    }

    public void setOutputChannels(int outputChannels) {
        this.outputChannels = outputChannels;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getFramesize() {
        return framesize;
    }

    public void setFramesize(int framesize) {
        this.framesize = framesize;
    }

    public boolean isVbr() {
        return vbr;
    }

    public void setVbr(boolean vbr) {
        this.vbr = vbr;
    }
}
