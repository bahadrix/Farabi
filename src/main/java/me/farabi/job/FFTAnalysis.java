package me.farabi.job;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.MongoClient;
import me.farabi.AudioFormatWritable;
import me.farabi.MDFWritable;
import me.farabi.Util;
import me.farabi.audio.AudioEvent;
import me.farabi.audio.dsp.fft.FFT;
import me.farabi.model.PeakHolder;
import me.farabi.model.Peaks;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


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
            fft = new FFT(sampleSize);

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
                PeakHolder peakHolder = new PeakHolder();
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
                                peakHolder.freq1 = Hz;
                            }
                        } else if (Hz < 8000) {
                            if (peak2 < decibels[i]) {
                                peak2 = decibels[i];
                                peakHolder.freq2 = Hz;
                            }
                        } else if (Hz < 16000) {
                            if (peak3 < decibels[i]) {
                                peak3 = decibels[i];
                                peakHolder.freq3 = Hz;
                            }
                        }
                    }
                    list.add(peakHolder);
                    peakHolder = new PeakHolder();
                    peak1 = Double.NEGATIVE_INFINITY;
                    peak2=Double.NEGATIVE_INFINITY;
                    peak3=Double.NEGATIVE_INFINITY;
                }

                Peaks peaks = new Peaks();
                //TODO: get id from mdf
                peaks.setList(list);
                ds.save(peaks);

            } catch (UnsupportedAudioFileException e) {
                log.error("Error on getting decoded stream");
            }

            log.info("File decoded and it's meta saved to mongo.");

        }


    }

    static String usage = "\nUsage: \n" +
            "MongoSone <input> <output> [-p <properties file>]\n" +
            "   <input>                 : Package data file location on HDFS\n" +
            "   <output>                : Output location on HDFS for logs and stuff\n" +
            "   -p <properties file>    : Properties file for mongodb connection info and stuff.\n" +
            "                             Default: farabi.properties\n";

    @Override
    public int run(String[] args) throws Exception {

        Map<String, List<String>> arguments = Util.parseProgramArguments(args);

        if(arguments.get("_") == null) {
            log.error("Missing path arguments." + usage);

        } else if(arguments.get("_").size() != 2) {
            log.error("Wrong number of path arguments." + usage);
        }

        Properties props = Util.getFarabiProps(arguments);

        if(props == null)
            System.exit(-1);

        Configuration conf = getConf();

        String mongoHost = String.valueOf(props.get("mongodb.server.host"));
        String mongoPort = String.valueOf(props.get("mongodb.server.port"));
        String mongoDBName = String.valueOf(props.get("mongodb.db"));

        // Check mongo db server
        if(!Util.checkMongoDBServer(mongoHost, mongoPort))
            System.exit(-1);

        conf.set("mongodb.server.host", mongoHost);
        conf.set("mongodb.server.port", mongoPort);
        conf.set("mongodb.db", mongoDBName);

        log.info("MongoDB server: " + mongoHost + ":" + mongoPort);


        Job job = new Job(conf, "FFT2Mongo");

        Path pIn = new Path(args[0]);
        Path pOut = new Path(args[1]);
        FileInputFormat.setInputPaths(job, pIn);
        FileOutputFormat.setOutputPath(job, pOut);

        job.setJarByClass(FFTAnalysis.class);
        job.setMapperClass(Mappa.class);

        //Zero reducers
        job.setNumReduceTasks(0);

        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        if (Util.deleteHDFSFile(pOut)) {
            log.info("Output location cleared.");
        }

        job.waitForCompletion(true);

        return 0;
    }

    public static void main(String[] args) throws Exception {

        int res = ToolRunner.run(new Configuration(), new MongoSone(), args);
        System.exit(res);

    }
}
