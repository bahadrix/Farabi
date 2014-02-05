package me.farabi.job;

import me.farabi.MDFWritable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.Iterator;

/**
 * Farabi
 * User: Bahadir
 * Date: 05.02.2014
 * Time: 17:37
 */
public class CountArtistSongsDist extends Configured implements Tool{

    public static class Mappa extends MapReduceBase
            implements Mapper<Text, MDFWritable, Text, IntWritable> {

        @Override
        public void map(Text key, MDFWritable mdf, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {

            output.collect(mdf.getArtist(), new IntWritable(1));

        }
    }

    public static class Reducca extends MapReduceBase
            implements  Reducer<Text, IntWritable, Text, IntWritable> {

        @Override
        public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
            int sum = 0;
            while(values.hasNext()) {
                sum += values.next().get();
            }
            output.collect(key, new IntWritable(sum));
        }
    }


    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new CountArtistSongsDist(), args);
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

        job.setJobName("MapTouch");
        job.setMapperClass(Mappa.class);
        job.setReducerClass(Reducca.class);

        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputFormat(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        JobClient.runJob(job);

        return 0;
    }
}
