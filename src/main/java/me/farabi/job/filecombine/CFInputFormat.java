package me.farabi.job.filecombine;

/**
 * Farabi
 * User: Bahadir
 * Date: 17.03.2014
 * Time: 15:09
 */

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;


public class CFInputFormat extends CombineFileInputFormat<FileLineWritable, Text> {
    public CFInputFormat() {
        super();
        setMaxSplitSize(134217728); // 128 MB
    }

    public RecordReader<FileLineWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException {
        return new CombineFileRecordReader<FileLineWritable, Text>((CombineFileSplit) split, context, CFRecordReader.class);
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        return false;
    }
}