package me.farabi;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

/**
 * Farabi
 * Special thanks goes to: https://gist.github.com/sritchie/808035
 * User: Bahadir
 * Date: 05.02.2014
 * Time: 15:07
 */
public class WholeFile {
    public static class WholeFileInputFormat extends FileInputFormat<Text, BytesWritable> {
        @Override
        protected boolean isSplitable(FileSystem fs, Path filename) {
            return false;
        }

        @Override
        public RecordReader<Text, BytesWritable> getRecordReader(
                InputSplit split, JobConf job, Reporter reporter) throws IOException {
            return new WholeFileRecordReader((FileSplit) split, job);
        }
    }
    public static class WholeFileRecordReader implements RecordReader<Text, BytesWritable> {

        private FileSplit fileSplit;
        private Configuration conf;
        private boolean processed = false;

        public WholeFileRecordReader(FileSplit fileSplit, Configuration conf) throws IOException {
            this.fileSplit = fileSplit;
            this.conf = conf;
        }

        @Override
        public boolean next(Text key, BytesWritable value) throws IOException {
            if (!processed) {
                byte[] contents = new byte[(int) fileSplit.getLength()];
                Path file = fileSplit.getPath();

                String fileName = file.getName();
                key.set(fileName);

                FileSystem fs = file.getFileSystem(conf);
                FSDataInputStream in = null;
                try {
                    in = fs.open(file);
                    IOUtils.readFully(in, contents, 0, contents.length);
                    value.set(contents, 0, contents.length);
                } finally {
                    IOUtils.closeStream(in);
                }
                processed = true;
                return true;
            }
            return false;
        }

        @Override
        public Text createKey() {
            return new Text();
        }

        @Override
        public BytesWritable createValue() {
            return new BytesWritable();
        }

        @Override
        public long getPos() throws IOException {
            return processed ? fileSplit.getLength() : 0;
        }

        @Override
        public float getProgress() throws IOException {
            return processed ? 1.0f : 0.0f;
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    }
}
