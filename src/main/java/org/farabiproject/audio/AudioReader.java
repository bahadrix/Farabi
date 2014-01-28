package org.farabiproject.audio;

import javazoom.jl.decoder.*;

import java.io.InputStream;

/**
 * User: Bahadir
 * Date: 26.01.2014
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class AudioReader {


    public AudioReader() {

    }

    public static void readMP3(InputStream inputStream) {

        Decoder decoder = new Decoder();
        Bitstream stream = new Bitstream(inputStream);

        int frameCount = Integer.MAX_VALUE;

        SampleBuffer buff = null;
        frameCount = Integer.MAX_VALUE;
        try {
            for (int frame = 0; frame < frameCount; frame++) {
                Header header = stream.readFrame();
                if (header == null) {
                    break;
                }
                /*
                if (decoder.getOutputChannels() == 0) {
                    int channels = (header.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
                    int freq = header.frequency();
                    decoder.initOutputBuffer(channels, freq, destFileName);
                }
                */
                buff = (SampleBuffer)decoder.decodeFrame(header, stream);

                stream.closeFrame();
            }
        } catch (BitstreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (DecoderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            //decoder.close();
        }

    }

}
