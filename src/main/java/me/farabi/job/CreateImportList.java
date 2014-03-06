package me.farabi.job;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Farabi
 * User: Bahadir
 * Date: 13.02.2014
 * Time: 09:07
 */
public class CreateImportList extends Configured implements Tool {


    @Override
    public int run(String[] strings) throws Exception {


        Configuration conf = getConf();
        JobConf job = new JobConf(conf, CreateImportList.class);

        job.setJobName("MP3 File Import List Creator");



        return 0;
    }

    public static void main(String[] args) throws Exception {

        int res = ToolRunner.run(new Configuration(), new CreateImportList(), args);
        System.exit(res);

    }
}
