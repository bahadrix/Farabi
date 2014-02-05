package me.farabi.job;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;
import me.farabi.MDFWritable;
import me.farabi.WholeFile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.CombineFileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.MediaFile;
import org.blinkenlights.jid3.v1.ID3V1Tag;
import org.blinkenlights.jid3.v2.ID3V2Tag;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Farabi
 * User: Bahadir
 * Date: 05.02.2014
 * Time: 14:00
 */
public class SyncToMapFileDist extends Configured implements Tool {



    public static class Mappa extends MapReduceBase
            implements Mapper<Text, BytesWritable, Text, MDFWritable> {


        @Override
        public void map(Text text, BytesWritable bytesWritable,
                        OutputCollector<Text, MDFWritable> textMDFWritableOutputCollector, Reporter reporter) throws IOException {


            File tempFile = File.createTempFile(text.toString(), ".tmp3");

            FileOutputStream fout = new FileOutputStream(tempFile);
            fout.write(bytesWritable.getBytes());
            fout.close();

            MDFWritable mdf;
            try {
                mdf = MDFWritable.createFromFile(tempFile, true);
                textMDFWritableOutputCollector.collect(text, mdf);
            } catch (ID3Exception e) {
                e.printStackTrace();
            } catch (DecoderException e) {
                e.printStackTrace();
            } catch (BitstreamException e) {
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("JLayer sucks for: " + text.toString());
            }




        }
    }


    public static void main(String[] args) throws Exception {

        int res = ToolRunner.run(new Configuration(), new SyncToMapFileDist(), args);
        System.exit(res);

    }


    @Override
    public int run(String[] args) throws Exception {

        Configuration conf = getConf();
        JobConf job = new JobConf(conf, CountArtistSongs.class);

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
