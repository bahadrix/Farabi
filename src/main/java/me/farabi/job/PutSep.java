package me.farabi.job;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Farabi
 * User: Bahadir
 * Date: 07.02.2014
 * Time: 16:27
 */
public class PutSep extends Configured implements Tool{




    @Override
    public int run(String[] args) throws Exception {

        Configuration conf = getConf();
        JobConf job = new JobConf(conf, PutSep.class);

        Path pIn = new Path(args[0]);
        Path pOut = new Path(args[1]);
        FileInputFormat.setInputPaths(job, pIn);
        FileOutputFormat.setOutputPath(job, pOut);

        job.setJobName("MultiOutputJob");

        //job.setMapperClass();


        return 0;
    }
    public static void main(String[] args) throws Exception {

        if(args.length != 2) {
            System.out.println("Usage: <in> <out>");
            System.exit(-1);
        }

        int res = ToolRunner.run(new Configuration(), new MapDecode(), args);
        System.exit(res);

    }
}
