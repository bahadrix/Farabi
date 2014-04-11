package me.farabi.job;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.MongoClient;
import me.farabi.AudioFormatWritable;
import me.farabi.MDFWritable;
import me.farabi.Util;
import me.farabi.audio.AudioEvent;
import me.farabi.audio.dsp.fft.FFT;
import me.farabi.audio.dsp.fft.HammingWindow;
import me.farabi.model.PeakHolder;
import me.farabi.model.SongOne;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.Tool;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by umutcanguney on 03/04/14.
 */
public class FFTAnalysis extends Configured implements Tool {
    private static org.apache.log4j.Logger log = Util.getLogger(MongoSone.class);

    public static class Mappa extends Mapper<IntWritable, MDFWritable, Text, NullWritable> {

        Datastore ds;
        public AudioFormatWritable afw;
        public AudioFormat format;
        public AudioEvent event;
        public int sampleSize;
        public FFT fft;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            sampleSize = 2048; //ideal sample size.
            fft = new FFT(sampleSize, new HammingWindow());

            String host = context.getConfiguration().get("mongodb.server.host");
            int port = Integer.parseInt(context.getConfiguration().get("mongodb.server.port"));
            String dbname = context.getConfiguration().get("mongodb.db");
            MongoClient mongo = new MongoClient( host , port );

            Morphia morphia = new Morphia();
            ds = morphia.createDatastore(mongo, dbname, null, null);

        }

        @Override
        protected void map(IntWritable key, MDFWritable mdf, Context context) throws IOException, InterruptedException {

            try {

                /*
                AudioFormatWritable inputFormat'ın bilgisini tutuyor.
                Bize decoded stream'in bilgisi lazım olduğu için bunu kullanmıyoruz.
                 */
//                afw = mdf.getAudioFormatWritable();
//
//                format = new AudioFormat(new AudioFormat.Encoding(afw.getEncoding().toString()), afw.getSampleRate().get(), afw.getSampleSizeInBits().get(), afw.getChannels().get(), afw.getFrameSize().get(), afw.getFrameRate().get(), afw.isBigEndian());


                /**
                 * getDecodeStream parameteresi olarak AudioFormat verilebilir.
                 * Aşağıdaki gibi verilmezse default format yapar.
                 * @see me.farabi.MDFWritable
                 */
                AudioInputStream decodedStream = mdf.getDecodeStream();
                format = decodedStream.getFormat();

                event = new AudioEvent(format, decodedStream.getFrameLength());

                byte[] byteBuffer = new byte[sampleSize*format.getSampleSizeInBits()];
                float[] floatBuffer = new float[sampleSize];
                double[] decibels;
                PeakHolder peaks = new PeakHolder();
                short Hz;
                double peak1 = Double.NEGATIVE_INFINITY, peak2=Double.NEGATIVE_INFINITY, peak3=Double.NEGATIVE_INFINITY;
                ArrayList<PeakHolder> list = new ArrayList<PeakHolder>();

                while ((decodedStream.read(byteBuffer,0,byteBuffer.length)) != -1) {
                    event.setFloatBuffer(event.getConverter().toFloatArray(byteBuffer, floatBuffer));
                    decibels = fft.calculateDecibels(event.getFloatBuffer());
                    for (int i = 0; i < decibels.length; i++) {
                        Hz = (short)fft.binToHz(i,format.getSampleRate());
                        if (Hz < 1000) {
                            if (peak1 < decibels[i]) {
                                peak1 = decibels[i];
                                peaks.freq1 = Hz;
                            }
                        } else if (Hz < 8000) {
                            if (peak2 < decibels[i]) {
                                peak2 = decibels[i];
                                peaks.freq2 = Hz;
                            }
                        } else if (Hz < 16000) {
                            if (peak3 < decibels[i]) {
                                peak3 = decibels[i];
                                peaks.freq3 = Hz;
                            }
                        }
                    }
                    list.add(peaks);
                    peaks = new PeakHolder();
                    peak1 = Double.NEGATIVE_INFINITY;
                    peak2=Double.NEGATIVE_INFINITY;
                    peak3=Double.NEGATIVE_INFINITY;
                }

//                ds.save(SongOne.createFromMDF(mdf));

            } catch (UnsupportedAudioFileException e) {
                log.error("Error on getting decoded stream");
            }

            log.info("File decoded and it's meta saved to mongo.");

        }


    }

    @Override
    public int run(String[] strings) throws Exception {
        return 0;
    }
}
