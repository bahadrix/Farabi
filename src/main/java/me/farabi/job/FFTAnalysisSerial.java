package me.farabi.job;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Farabi
 * User: Bahadir
 * Date: 13.04.2014
 * Time: 12:51
 */
public class FFTAnalysisSerial extends Configured implements Tool {


    public static void main(String args[]) throws Exception {
        int res = ToolRunner.run(new Configuration(), new FFTAnalysisSerial(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {



        return 0;
    }


}
