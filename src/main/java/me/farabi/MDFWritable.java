package me.farabi;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
import org.apache.log4j.Logger;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Farabi
 *
 * User: Bahadir
 * Date: 04.02.2014
 * Time: 09:37
 */

@SuppressWarnings("UnusedDeclaration") // kullanmamis olabiliris ama kullancas, soz!
public class MDFWritable implements Writable {

    private static org.apache.log4j.Logger log = Logger.getLogger(MDFWritable.class);
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

    /**
     * From this version we always save encoded data in MDFWritable
     * @param inputStream MP3 FileInputStream
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public MDFWritable(InputStream inputStream) throws IOException, UnsupportedAudioFileException {
        byte[] fileData = IOUtils.toByteArray(inputStream);

        // Mark/Read hatasini gidermek icin boyle bir yol denedim.
        // Bu sefer file not supported hatasi geliyo.
        ByteArrayInputStream bis = new ByteArrayInputStream(fileData);

        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(bis);
        tags = new MDFSongTags(fileFormat);

        AudioInputStream in = AudioSystem.getAudioInputStream(inputStream);
        AudioFormat baseFormat = in.getFormat();

        /**
         * We set some properties at this constrution time.
         * For a full list see 'no-tag' in test/resources/tag-states.txt
         */
        setOutputChannels(baseFormat.getChannels());
        setOutputFrequency(Integer.parseInt((String)fileFormat.getProperty("mp3.frequency.hz")));
        setBitrate(Integer.parseInt((String)fileFormat.getProperty("mp3.bitrate.nominal.bps")));
        setFramesize(Integer.parseInt((String)fileFormat.getProperty("mp3.framesize.bytes")));
        setVbr(((String) fileFormat.getProperty("mp3.vbr")).equals("true")); 

        //Set encoded data
        setFileData(new BytesWritable(fileData));

    }

    /**
     * Get decode stream with default output format of
     * PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, big-endian
     * @return Decoded stream of fileData
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public AudioInputStream getDecodeStream() throws IOException, UnsupportedAudioFileException {
        return getDecodeStream(null);
    }

    /**
     *
     * @param outputFormat Format of decoded output
     * @return Decoded stream of fileData
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public AudioInputStream getDecodeStream(AudioFormat outputFormat) throws IOException, UnsupportedAudioFileException {
        AudioInputStream decodedStream = null;

        ByteArrayInputStream bis = new ByteArrayInputStream(getFileData().getBytes());
        AudioInputStream in = AudioSystem.getAudioInputStream(bis);

        /**
         * Optimize this later:
         * Make AudioFormatWritable and read this once at first construction time.
         */
        AudioFormat baseFormat = in.getFormat();
        if(outputFormat == null) { //Make it default
            outputFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,    // Encoding to use
                baseFormat.getSampleRate(),	        // sample rate (same as base format)
                16,				                    // sample size in bits (thx to Javazoom)
                baseFormat.getChannels(),	        // # of Channels
                baseFormat.getChannels()*2,	        // Frame Size
                baseFormat.getSampleRate(),	        // Frame Rate
                true				                // Big Endian
            );
        }
        decodedStream = AudioSystem.getAudioInputStream(outputFormat, in);

        return decodedStream;
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
