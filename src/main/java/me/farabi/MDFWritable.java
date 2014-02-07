package me.farabi;

import javazoom.jl.decoder.*;
import org.apache.commons.io.FileUtils;
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


    public MDFSongTags tags;


    protected IntWritable outputFrequency  = new IntWritable(0);
    protected IntWritable outputChannels  = new IntWritable(0);
    protected IntWritable bitrate  = new IntWritable(0);
    protected IntWritable framesize  = new IntWritable(0);
    protected BooleanWritable vbr  = new BooleanWritable(false);
    protected BytesWritable fileData = new BytesWritable(new byte[]{0});

    public MDFWritable() {
        tags = new MDFSongTags();
    }

    public static MDFWritable createFromFile(File mp3File, boolean decodeRaw) throws ID3Exception, IOException, DecoderException, BitstreamException {
        MDFWritable mdfWritable = new MDFWritable();



        // Get tags ==================================================================


        MediaFile mediaFile = new MP3File(mp3File);
        ID3Tag[] tags = mediaFile.getTags();

        for (ID3Tag tag : tags) {
            if (tag instanceof ID3V1_0Tag) {
                ID3V1_0Tag oID3V1_0Tag = (ID3V1_0Tag) tag;
                if (oID3V1_0Tag.getTitle() != null) {
                    mdfWritable.tags.setTitle(oID3V1_0Tag.getTitle());
                    mdfWritable.tags.setAlbum(oID3V1_0Tag.getAlbum());
                    mdfWritable.tags.setArtist(oID3V1_0Tag.getArtist());
                    mdfWritable.tags.setGenreDesc(oID3V1_0Tag.getGenre().toString());
                    mdfWritable.tags.setYear(oID3V1_0Tag.getYear());
                }
            } else if (tag instanceof ID3V1_1Tag) {
                ID3V1_1Tag oID3V1_1Tag = (ID3V1_1Tag) tag;
                if (oID3V1_1Tag.getTitle() != null) {
                    mdfWritable.tags.setTitle(oID3V1_1Tag.getTitle());
                    mdfWritable.tags.setAlbum(oID3V1_1Tag.getAlbum());
                    mdfWritable.tags.setArtist(oID3V1_1Tag.getArtist());
                    mdfWritable.tags.setGenreDesc(oID3V1_1Tag.getGenre().toString());
                    mdfWritable.tags.setYear(oID3V1_1Tag.getYear());
                }
            } else if (tag instanceof ID3V2_3_0Tag) {
                ID3V2_3_0Tag oID3V2_3Tag = (ID3V2_3_0Tag) tag;
                if (oID3V2_3Tag.getTitle() != null) {
                    mdfWritable.tags.setTitle(oID3V2_3Tag.getTitle());
                    mdfWritable.tags.setAlbum(oID3V2_3Tag.getAlbum());
                    mdfWritable.tags.setArtist(oID3V2_3Tag.getArtist());
                    mdfWritable.tags.setGenreDesc(oID3V2_3Tag.getGenre());
                    try {
                        mdfWritable.tags.setYear(String.valueOf(oID3V2_3Tag.getYear()));
                    } catch (ID3Exception e) {
                        mdfWritable.tags.setYear("");
                    }

                }
            }

        }


        // Get fileData


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
                    if(!decodeRaw)
                        break;
                }

                buff = (SampleBuffer)decoder.decodeFrame(header, stream);

                //fileData = ArrayUtils.addAll(fileData, buff.getBuffer());
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

        if(!decodeRaw) {
            mdfWritable.setFileData(new BytesWritable(
                    FileUtils.readFileToByteArray(mp3File)
            ));
        } else {
            mdfWritable.setFileData(new BytesWritable(
                    bout.toByteArray()
            ));
        }
        return mdfWritable;
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
