package me.farabi.audio;

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

    private void readMP3(InputStream inputStream) {

        /**
         * JLayer did some weird stuff.
         * If the input mp3 is Stereo, the values in the pcmbuffer are encoded
         * like this: leftchannel, rightchannel, leftchannel, ...
         * This is how it should be.
         * But if the input mp3 is Mono the I get the same amount of samples
         * in the pcmbuffer. But its not like: monochannel, 0, monochannel, 0
         * The whole data is in the first half of the pcmbuffer and the
         * second half is all 0. So you just need to cut off the second half.
         *
         * From:
         * http://stackoverflow.com/questions/13959599/jlayer-mono-mp3-to-pcm-decoding
         */

        Decoder decoder = new Decoder();
        Bitstream stream = new Bitstream(inputStream);

        int frameCount = Integer.MAX_VALUE;

        SampleBuffer buff = null;
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
