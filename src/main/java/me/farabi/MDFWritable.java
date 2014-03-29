package me.farabi;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Writable;
import org.apache.log4j.Logger;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

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
    protected BytesWritable fileData;
    protected AudioFormatWritable audioFormatWritable;

    public MDFWritable() {
        fileData = new BytesWritable(new byte[]{0});
        audioFormatWritable = new AudioFormatWritable();
        tags = new MDFSongTags();
    }

    /**
     * From this version we always save encoded data in MDFWritable
     * @param byteArray All bytes of file
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public MDFWritable(byte[] byteArray) throws IOException, UnsupportedAudioFileException {

        ByteArrayInputStream binStream = new ByteArrayInputStream(byteArray);

        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(binStream);
        tags = new MDFSongTags(fileFormat);

        AudioInputStream in = AudioSystem.getAudioInputStream(binStream);
        AudioFormat baseFormat = in.getFormat();

        //Set format info
        setAudioFormatWritable(new AudioFormatWritable(baseFormat));

        //Set encoded data
        setFileData(new BytesWritable(byteArray));

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
        AudioInputStream decodedStream;

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
        audioFormatWritable.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        tags.readFields(in);
        fileData.readFields(in);
        audioFormatWritable.readFields(in);
    }

    public AudioFormatWritable getAudioFormatWritable() {
        return audioFormatWritable;
    }

    public void setAudioFormatWritable(AudioFormatWritable audioFormatWritable) {
        this.audioFormatWritable = audioFormatWritable;
    }

    public BytesWritable getFileData() {
        return fileData;
    }

    public void setFileData(BytesWritable fileData) {
        this.fileData = fileData;
    }

}
