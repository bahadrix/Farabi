package me.farabi.job;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.MongoClient;
import me.farabi.MDFWritable;
import me.farabi.Util;
import me.farabi.audio.AudioEvent;
import me.farabi.audio.dsp.fft.FFT;
import me.farabi.model.Peaks;
import me.farabi.model.SongOne;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Farabi
 * User: Bahadir
 * Date: 13.04.2014
 * Time: 12:51
 */
public class FFTAnalysisSerial extends Configured implements Tool {

    private static org.apache.log4j.Logger log = Util.getLogger(FFTAnalysisSerial.class);

    public static AudioFormat format;
    public static AudioEvent event;
    private static Datastore ds;

    public static void main(String args[]) throws Exception {
        int res = ToolRunner.run(new Configuration(), new FFTAnalysisSerial(), args);
        System.exit(res);
    }

    static String usage = "\nUsage: \n" +
            "FFAnalysisSerial <input> <output> [-p <properties file>]\n" +
            "   <input>                 : Package data file location on HDFS\n" +
            "   <output>                : Output location on HDFS for logs and stuff\n" +
            "   -p <properties file>    : Properties file for mongodb connection info and stuff.\n" +
            "                             Default: farabi.properties\n";

    @Override
    public int run(String[] args) throws Exception {

        Map<String, List<String>> arguments = Util.parseProgramArguments(args);


        if(arguments.get("_") == null) {
            log.error("Missing path arguments." + usage);
            log.info(usage);

        } else if(arguments.get("_").size() != 1) {
            log.error("Wrong number of path arguments." + usage);
            log.info(usage);
        }

        Properties props = Util.getFarabiProps(arguments);

        if(props == null) {
            log.error("Properties file not found");
            return 2;
        }

        String mongoHost = String.valueOf(props.get("mongodb.server.host"));
        String mongoPort = String.valueOf(props.get("mongodb.server.port"));
        String mongoDBName = String.valueOf(props.get("mongodb.db"));


        // Getting files
        File localDir = new File(arguments.get("_").get(0));
        if(!localDir.isDirectory()) {
            log.error("Specified local path '" + localDir.getAbsolutePath() + "' is not a directory");
            log.info(usage);
            return 3;
        }

        File[] mp3files = localDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp3");
            }
        });

        if(mp3files.length > 0) {
            log.info(String.valueOf(mp3files.length) + " file(s) found under " + localDir.getAbsolutePath());
        } else {
            log.error("No mp3 files found under " + localDir.getAbsolutePath());
            return 4;

        }

        log.info("Setting up environment");
        try {
            setup(props);
        } catch(UnknownHostException e) {
            log.error(e);
            return 5;
        }

        for(File file : mp3files)
            process(file);

        return 0;
    }


    private static void setup(Properties props) throws UnknownHostException {
        String mongoHost = String.valueOf(props.get("mongodb.server.host"));
        int mongoPort = Integer.parseInt(String.valueOf(props.get("mongodb.server.port")));
        String mongoDBName = String.valueOf(props.get("mongodb.db"));


        MongoClient mongo = new MongoClient( mongoHost , mongoPort );

        Morphia morphia = new Morphia();
        ds = morphia.createDatastore(mongo, mongoDBName, null, null);
    }

    private static void process(File mp3file) {

        log.info("Processing " + mp3file.getName());


        FFT fft = new FFT(FFTAnalysis.SAMPLESIZE);

        try {

            byte[] fileBytes = IOUtils.toByteArray(
                    new FileInputStream(mp3file)
            );

            MDFWritable mdf = new MDFWritable(fileBytes);

            SongOne sone = SongOne.createFromMDF(mdf);

            sone.setPeaks(
                    FFTAnalysis.getPeaks(mdf,format, event, FFTAnalysis.SAMPLESIZE, fft)
            );

            ds.save(sone);

        } catch (IOException e) {
            log.error("IOException on " + mp3file.getName());
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            log.error("Audio format is not supported for " + mp3file.getName());
            e.printStackTrace();
        }

    }

}
