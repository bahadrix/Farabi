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
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
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


    protected IntWritable outputFrequency = new IntWritable(0);
    protected IntWritable outputChannels = new IntWritable(0);
    protected IntWritable bitrate = new IntWritable(0);
    protected IntWritable framesize = new IntWritable(0);
    protected BooleanWritable vbr = new BooleanWritable(false);
    protected BytesWritable fileData = new BytesWritable(new byte[]{0});
    protected BooleanWritable decoded = new BooleanWritable(false);

    public MDFWritable() {
        tags = new MDFSongTags();
    }

    public MDFWritable(byte[] fileData, long fileLength, boolean decodeRaw) {

        // Get tags ==================================================================
        try {
            tags = new MDFSongTags(new ByteArrayInputStream(fileData), fileLength, 0);
        } catch (Exception e) {
            log.error("Tag reading error.");
            log.error(e);
        }

        // Read header ===============================================================
        readHeader(fileData);

        // Get fileData ==============================================================

        if (!decodeRaw) { // Set filedata to raw data
            setFileData(new BytesWritable(
                    fileData
            ));
            setDecoded(false);
        } else { // Set filedata to decoded data
            setFileData(new BytesWritable(
                    decodeByteArray(fileData).toByteArray()
            ));
            setDecoded(true);
        }

    }

    public MDFWritable(File mp3File, boolean decodeRaw) throws IOException, DecoderException, BitstreamException, InvalidDataException, UnsupportedTagException {
        this(FileUtils.readFileToByteArray(mp3File), mp3File.length(), decodeRaw);
    }

    /**
     * Decodes file data if it's not already decoded, and set it to the decoded data.
     * Once the method called you can't call decodeStream. Because encoded data is replaced.
     */
    public void decodeData() {
        if(!isDecoded()) {
            setFileData(new BytesWritable(
                    decodeByteArray(getFileData().getBytes()).toByteArray()
            ));
            setDecoded(true);
        } else {
            log.error("Data is already decoded");
        }
    }

    /**
     * Returns decoded bytes stream if file is not already decoded.
     * @return Decoded bytes stream.
     */
    public ByteArrayOutputStream decodeStream() {
        if(isDecoded()) {
            log.error("File is already decoded. You can use directly fileData as decoded.");
            return null;
        }

        return decodeByteArray(getFileData().getBytes());

    }

    /**
     * Reads data into this objects header properties
     * @param data Encoded mp3 bytes
     */
    private void readHeader(byte[] data) {

        Bitstream stream = new Bitstream(new ByteArrayInputStream(data));

        boolean swap = false;

        try {

            Header header = stream.readFrame();

            setOutputChannels(header.mode() == Header.SINGLE_CHANNEL ? 1 : 2);
            setOutputFrequency(header.frequency());
            setBitrate(header.bitrate());
            setFramesize(header.framesize);
            setVbr(header.vbr());

        } catch (BitstreamException e) {
            log.error("Header reading error");
            log.error(e);
        } finally {
            stream.closeFrame();
        }
    }

    /**
     * Decodes given file bytes into a stream.
     * @param data Encoded MP3 file bytes.
     * @return Decoded bytes stream.
     */
    public static ByteArrayOutputStream decodeByteArray(byte[] data) {

        Decoder decoder = new Decoder();
        Bitstream stream = new Bitstream(new ByteArrayInputStream(data));
        ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);

        int frameCount = Integer.MAX_VALUE;
        SampleBuffer buff;

        boolean swap = false;


        try {
            // Decoded veriyi sifir basiyo gibi gozukuyo ama Lame ile 16bit red deneyince de
            // tomarla sifir geliyo.
            for (int frame = 0; frame < frameCount; frame++) {
                Header header = stream.readFrame();
                if (header == null) {
                    break;
                }
                if (frame == 0) {
                    swap = header.frequency() >= 44000; // bkz: http://stackoverflow.com/a/15187707
                }

                buff = (SampleBuffer) decoder.decodeFrame(header, stream);


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
        } catch (BitstreamException e) {
            log.error("Decoding error");
            log.error(e);
        } catch (DecoderException e) {
            log.error("Decoding error");
            log.error(e);
        }
        return bout;
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
        decoded.write(out);
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
        decoded.readFields(in);
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

    public boolean isDecoded() {
        return decoded.get();
    }

    private void setDecoded(Boolean decoded) {
        this.decoded = new BooleanWritable(decoded);
    }
}
