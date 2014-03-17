package me.farabi.job;

import com.mpatric.mp3agic.UnsupportedTagException;
import me.farabi.MDFWritable;
import me.farabi.Util;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;

/**
 * Farabi
 * Yerel MP3 dosyalarini okuyarak HDFS uzerinde tek bir map dosyasına paketler.
 * Bir map/reduce job'i olmasada hadoop jar ile asagidaki gibi calistirilabilir.
 *
 * $ hadoop jar ~/Farabi-1.0-SNAPSHOT.jar me.farabi.job.PutSeparated -d ~/mp3 farabi/inputseq3
 *
 * MP3 tagleri icin ayri bir map dosyasi olusturur ancak bu bilgiler asil map dosyasinda da vardir.
 *

 */
public class CreatePack {

    private static org.apache.log4j.Logger log = Logger.getLogger(CreatePack.class);
    static {
        log.setLevel(Level.DEBUG);
    }
    static enum ErrorType {ARGUMENT, NOWORKTODO}

    private static long startTime;

    private static String outputPath;
    private static boolean decodeFiles = false;
    private static int maxFiles = 0;

    @SuppressWarnings("UnusedDeclaration")
    public static void main(String[] args) {

        String opts;
        File localDir = null;

        // Argument handling ============================================================
        String arg;
        int noargCnt = 0;
        for(int i = 0; i < args.length; i++) {
            arg = args[i];
            if(!arg.startsWith("-")){ //Get directory arguments
                noargCnt++;
                if(noargCnt > 2) {
                    errorOut(ErrorType.ARGUMENT, "Wrong folder arguments");
                } else if(noargCnt == 1) {
                    localDir = new File(arg);
                } else if(noargCnt == 2) {
                    outputPath = arg;
                }
            } else {
                try {
                    if(arg.equals("-d")) {
                        decodeFiles = true;
                    } else if (arg.equals("-m")) {
                        maxFiles = Integer.parseInt(args[++i]);
                    }
                }catch (IndexOutOfBoundsException e) {
                    errorOut(ErrorType.ARGUMENT, "Argument needs parameter");
                }
            }
        }
        if(noargCnt != 2) {
            errorOut(ErrorType.ARGUMENT, "Missing path arguments.");
        }
        // EOF argument handling

        //outputPath always ends with '/'
        if(!outputPath.endsWith("/"))
            outputPath += "/";

        assert localDir != null;
        if(!localDir.isDirectory()) {
            errorOut(ErrorType.ARGUMENT,
                    "Specified local path '" + localDir.getAbsolutePath() + "' is not a directory");
        }

        // Get local files ==============================================================

        log.debug("Getting file list...");


        File[] mp3files = localDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp3");
            }
        });

        if(mp3files.length > 0) {
            log.info(String.valueOf(mp3files.length) + " file(s) found under " + localDir.getAbsolutePath());
        } else {
            errorOut(ErrorType.NOWORKTODO, "No mp3 files found under " + localDir.getAbsolutePath());
        }

        // Delete output path if exist. =================================================
        try {
            boolean outputCleared = Util.deleteHDFSFile(new Path(outputPath));
            if(outputCleared)
                log.info("Output path '" + outputPath + "' deleted.");
        } catch (IOException e) {
            log.error("Can't clear existing output path " + outputPath);
        }

        // Create package
        create(mp3files, localDir.getName());

        System.exit(0);
    }

    private static void create(File[] mp3Files, String mapName) {
        log.debug("Creating map " + mapName);
        startTime = System.currentTimeMillis();
        long tempTime = startTime;
        MapFile.Writer writerAudio = null;
        MapFile.Writer writerTags = null;
        String temp = "";
        try {
            Configuration conf = new Configuration();
            conf.set("mapred.child.java.opts", "-Xmx2048m");
            FileSystem fs = FileSystem.get(URI.create(mapName), conf);

            IntWritable key = new IntWritable();
            MDFWritable value = new MDFWritable();

            // Tamam bu deprecated da yerine ne kullancas a.
            //noinspection deprecation
            writerAudio = new MapFile.Writer(conf, fs, outputPath + mapName + "_audio", key.getClass(), value.getClass());
            //noinspection deprecation
            writerTags  = new MapFile.Writer(conf, fs, outputPath + mapName + "_tags",  key.getClass(), value.tags.getClass());
            log.info("Packaging started");
            int i = 0;
            for(File file : mp3Files) {  i++;

                temp =  " \"" + file.getName() + "\" ";

                try {
                    key.set(i);
                    value = new MDFWritable(file, decodeFiles);
                    writerAudio.append(key, value);
                    writerTags.append(key, value.tags);
                    log.info("File added " + String.format("%s %.2f secs", temp, (double) (System.currentTimeMillis() - tempTime) / 1000));
                } catch (UnsupportedTagException e) {
                    log.error("ID3Exception error on packing file " + temp);
                    log.error(e);
                } catch (Exception e) {
                    log.error("Error on packing file " + temp);
                    log.error(e);
                }
                tempTime = System.currentTimeMillis();

                if(maxFiles != 0 && maxFiles == i) {
                    log.info("Max file limit reached");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(writerAudio);
            IOUtils.closeStream(writerTags);
            log.info("Completed! Total time: " + String.format("%.2f secs", (double) (System.currentTimeMillis() - startTime) / 1000));
        }

    }

    private static void errorOut(ErrorType t, String info) {

        if(info != null) {
            log.error(info);
        }

        switch (t) {
            case ARGUMENT:
                System.out.println("Argument error");
                showUsage();
                break;
            case NOWORKTODO:
                System.out.println("Nothing happened.");
                break;
        }

        System.exit(-1);
    }


    @SuppressWarnings("UnusedDeclaration")
    public static void errorOut(ErrorType t) {
        errorOut(t,null);
    }

    private static void showUsage(){
        System.out.println("USAGE: \n" +
                "me.farabi.job.CreatePack [opts] <local_dir> <hdfs_dir>\n" +
                "   [opts]\n" +
                "       -d          : Decode files.\n" +
                "       -m <num>    : Maximum number of files to be processed.\n");
    }
}
