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

            SongOne so = SongOne.createFromMDF(mdf);

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
                float[] magnitudes;

                while ((decodedStream.read(byteBuffer,0,byteBuffer.length)) != -1) {
                    event.setFloatBuffer(event.getConverter().toFloatArray(byteBuffer, floatBuffer));
                    magnitudes = fft.transformAndGetMagnitudes(event.getFloatBuffer());
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
