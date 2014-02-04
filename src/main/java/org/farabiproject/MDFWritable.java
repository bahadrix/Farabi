package org.farabiproject;

import javazoom.jl.decoder.*;
import org.apache.hadoop.io.*;
import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.MediaFile;
import org.blinkenlights.jid3.v1.ID3V1_0Tag;
import org.blinkenlights.jid3.v1.ID3V1_1Tag;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;

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

    private Text title = new Text("<unknown>");
    private Text artist = new Text("<unknown>");
    private Text album = new Text("<unknown>");
    private Text genreDesc = new Text("<unknown>");
    private Text year = new Text("<unknown>");


    private IntWritable outputFrequency  = new IntWritable(0);
    private IntWritable outputChannels  = new IntWritable(0);
    private IntWritable bitrate  = new IntWritable(0);
    private IntWritable framesize  = new IntWritable(0);
    private BooleanWritable vbr  = new BooleanWritable(false);
    private BytesWritable frames = new BytesWritable(new byte[]{0});


    public static MDFWritable createFromFile(File mp3File) throws ID3Exception, FileNotFoundException, DecoderException, BitstreamException {
        MDFWritable mdfWritable = new MDFWritable();



        // Get tags ==================================================================


        MediaFile mediaFile = new MP3File(mp3File);
        ID3Tag[] tags = mediaFile.getTags();



        for (ID3Tag tag : tags) {
            if (tag instanceof ID3V1_0Tag) {
                ID3V1_0Tag oID3V1_0Tag = (ID3V1_0Tag) tag;
                if (oID3V1_0Tag.getTitle() != null) {
                    mdfWritable.setTitle(oID3V1_0Tag.getTitle());
                    mdfWritable.setAlbum(oID3V1_0Tag.getAlbum());
                    mdfWritable.setArtist(oID3V1_0Tag.getArtist());
                    mdfWritable.setGenreDesc(oID3V1_0Tag.getGenre().toString());
                    mdfWritable.setYear(oID3V1_0Tag.getYear());
                }
            } else if (tag instanceof ID3V1_1Tag) {
                ID3V1_1Tag oID3V1_1Tag = (ID3V1_1Tag) tag;
                if (oID3V1_1Tag.getTitle() != null) {
                    mdfWritable.setTitle(oID3V1_1Tag.getTitle());
                    mdfWritable.setAlbum(oID3V1_1Tag.getAlbum());
                    mdfWritable.setArtist(oID3V1_1Tag.getArtist());
                    mdfWritable.setGenreDesc(oID3V1_1Tag.getGenre().toString());
                    mdfWritable.setYear(oID3V1_1Tag.getYear());
                }
            } else if (tag instanceof ID3V2_3_0Tag) {
                ID3V2_3_0Tag oID3V2_3Tag = (ID3V2_3_0Tag) tag;
                if (oID3V2_3Tag.getTitle() != null) {
                    mdfWritable.setTitle(oID3V2_3Tag.getTitle());
                    mdfWritable.setAlbum(oID3V2_3Tag.getAlbum());
                    mdfWritable.setArtist(oID3V2_3Tag.getArtist());
                    mdfWritable.setGenreDesc(oID3V2_3Tag.getGenre());
                    try {
                        mdfWritable.setYear(String.valueOf(oID3V2_3Tag.getYear()));
                    } catch (ID3Exception e) {
                        mdfWritable.setYear("");
                    }

                }
            }

        }


        // Get frames


        Decoder decoder = new Decoder();
        Bitstream stream = new Bitstream(new FileInputStream(mp3File));
        ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);

        int frameCount = Integer.MAX_VALUE;
        SampleBuffer buff;

        boolean swap = false;


            for (int frame = 0; frame < frameCount; frame++) {
                Header header = stream.readFrame();
                if (header == null) {
                    break;
                }
                if(frame == 0) {
                    mdfWritable.setOutputChannels(header.mode() == Header.SINGLE_CHANNEL ? 1 : 2);
                    mdfWritable.setOutputFrequency(header.frequency());
                    mdfWritable.setBitrate(header.bitrate());
                    mdfWritable.setFramesize(header.framesize);
                    mdfWritable.setVbr(header.vbr());
                    swap = header.frequency() >= 44000; // bkz: http://stackoverflow.com/a/15187707

                }

                buff = (SampleBuffer)decoder.decodeFrame(header, stream);

                //frames = ArrayUtils.addAll(frames, buff.getBuffer());
                short[] pcm = buff.getBuffer();
                for(short s : pcm) {
                    // bkz: http://stackoverflow.com/a/15187707
                    if(swap) {
                        bout.write(s & 0xff);
                        bout.write((s >> 8) & 0xff);
                    } else {
                        bout.write((s >> 8) & 0xff);
                        bout.write(s & 0xff);
                    }
                }

                stream.closeFrame();
            }


        mdfWritable.setFrames(new BytesWritable(bout.toByteArray()));
        return mdfWritable;
    }




    @Override
    public void write(DataOutput out) throws IOException {
        title.write(out);
        artist.write(out);
        album.write(out);
        genreDesc.write(out);
        year.write(out);
        frames.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        title.readFields(in);
        artist.readFields(in);
        album.readFields(in);
        genreDesc.readFields(in);
        year.readFields(in);
        frames.readFields(in);
    }

    public BytesWritable getFrames() {
        return frames;
    }

    public void setFrames(BytesWritable frames) {
        this.frames = frames;
    }

    public Text getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title == null ? new Text() : new Text(title);
    }
    public Text getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist =  artist == null ? new Text() : new Text(artist);
    }

    public Text getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album == null ? new Text() : new Text(album);
    }

    public Text getGenreDesc() {
        return genreDesc;
    }


    public void setGenreDesc(String genreDesc) {
        this.genreDesc = genreDesc == null ? new Text() : new Text(genreDesc);
    }

    public Text getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year == null ? new Text() : new Text(year);
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
