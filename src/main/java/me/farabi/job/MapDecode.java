package me.farabi.job;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;
import me.farabi.MDFWritable;
import me.farabi.WholeFile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Decodes and reads tags from raw mp3 files on hdfs.
 * Usage:
 * $ hadoop jar farabi.jar me.farabi.job.MapDecode samp3-original samp3-out
 *
 * User: Bahadir
 * Date: 05.02.2014
 * Time: 14:00
 */
public class MapDecode extends Configured implements Tool {

    private static org.apache.log4j.Logger log = Logger.getLogger(MapDecode.class);


    public static class Mappa extends MapReduceBase
            implements Mapper<Text, BytesWritable, Text, MDFWritable> {


        @Override
        public void map(Text text, BytesWritable bytesWritable,
                        OutputCollector<Text, MDFWritable> output, Reporter reporter) throws IOException {

            /*
            File tempFile = File.createTempFile(text.toString(), ".tmp3");

            FileOutputStream fout = new FileOutputStream(tempFile);
            fout.write(bytesWritable.getBytes());
            fout.close();
              */
            long startTime;
            MDFWritable mdf;
            try {
                startTime = System.currentTimeMillis();
                mdf = new MDFWritable(bytesWritable.getBytes(), bytesWritable.getLength(), true);
                output.collect(text, mdf);

                log.info(String.format("%s decoded and MDF object collected in %d ms.",
                        text,
                        System.currentTimeMillis() - startTime));
            } catch (DecoderException e) {
                log.error("Decoder exception while creating MDF object for file " + text);
            } catch (BitstreamException e) {
                log.error("BitstreamException exception while creating MDF object for file " + text);
            } catch (ArrayIndexOutOfBoundsException e) {
                log.error("ArrayIndexOutOfBoundsException (may be it's JLayer bug) exception while creating MDF object for file " + text);
            } catch (UnsupportedTagException e) {
                log.error("UnsupportedTagException exception while creating MDF object for file " + text);
            } catch (InvalidDataException e) {
                log.error("InvalidDataException exception while creating MDF object for file " + text);
            }


        }
    }


    public static void main(String[] args) throws Exception {

        int res = ToolRunner.run(new Configuration(), new MapDecode(), args);
        System.exit(res);

    }


    @Override
    public int run(String[] args) throws Exception {

        Configuration conf = getConf();
        JobConf job = new JobConf(conf, MapDecode.class);

        if(args.length != 2) {
            System.out.println("Usage: <in> <out>");
            return -1;
        }

        Path pIn = new Path(args[0]);
        Path pOut = new Path(args[1]);
        FileInputFormat.setInputPaths(job, pIn);
        FileOutputFormat.setOutputPath(job, pOut);

        job.setJobName("MapdaMP3");
        job.setMapperClass(Mappa.class);
        //job.setReducerClass(Reducca.class);


        job.setNumReduceTasks(0); // No reduce just mappa

        job.setInputFormat(WholeFile.WholeFileInputFormat.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(MDFWritable.class);
        job.setOutputFormat(SequenceFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(MDFWritable.class);

        JobClient.runJob(job);


        return 0;
    }
}
