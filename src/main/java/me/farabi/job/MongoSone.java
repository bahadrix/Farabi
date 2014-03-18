package me.farabi.job;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.MongoClient;
import me.farabi.MDFWritable;
import me.farabi.Util;
import me.farabi.model.SongOne;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Adım 2: İşle
 * Paketteki decode edilmemiş doyaları okuyup işleyerek meta verileri mongoya
 * yazar.
 *
 * Örnek:
 * $ me.farabi.job.MongoSone farabi/pack1/all/data farabi/mongout ttlinux1.hdcluster

 *
 * User: Bahadir
 * Date: 18.03.2014
 * Time: 10:54
 */
public class MongoSone extends Configured implements Tool {

    private static org.apache.log4j.Logger log = Util.getLogger(MongoSone.class);

    public static class Mappa extends Mapper<IntWritable, MDFWritable, Text, NullWritable> {

        Datastore ds;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            String host = context.getConfiguration().get("mongodb.server.host");
            int port = Integer.parseInt(context.getConfiguration().get("mongodb.server.port"));
            MongoClient mongo = new MongoClient( host , port );

            Morphia morphia = new Morphia();
            ds = morphia.createDatastore(mongo, "testdb", null, null);

        }

        @Override
        protected void map(IntWritable key, MDFWritable mdf, Context context) throws IOException, InterruptedException {

            //Decode, fft gibi işlemler burada yapılırsa leziz olur.
            // Sonucu mongodb'ye aşağıdaki gibi yazılabilir.
            ds.save(SongOne.createFromMDF(mdf));

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

        // Check mongo db server
        if(!Util.checkMongoDBServer(mongoHost, mongoPort))
           System.exit(-1);

        conf.set("mongodb.server.host", mongoHost);
        conf.set("mongodb.server.port", mongoPort);

        log.info("MongoDB server: " + mongoHost + ":" + mongoPort);

        System.exit(0);
        Job job = new Job(conf, "Map2Mongo");

        Path pIn = new Path(args[0]);
        Path pOut = new Path(args[1]);
        FileInputFormat.setInputPaths(job, pIn);
        FileOutputFormat.setOutputPath(job, pOut);

        job.setJarByClass(MongoSone.class);
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
