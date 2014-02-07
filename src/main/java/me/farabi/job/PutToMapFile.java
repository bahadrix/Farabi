package me.farabi.job;

import me.farabi.MDFSongTags;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapFile;
import org.blinkenlights.jid3.ID3Exception;
import me.farabi.MDFWritable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;

/**
 * Farabi
 * Synchronizes specified folder to corresponding map file.
 * Creates data and indexes.
 * User: Bahadir
 * Date: 04.02.2014
 * Time: 11:03
 */
public class PutToMapFile {
    static enum ErrorType {ARGUMENT, NOWORKTODO}
    static PrintStream out;
    private static long startTime;


    public static void main(String[] args) {

        out = System.out;
        String opts;
        String targetPath;
        File localDir = null;

        if(args.length == 0) {
            errorOut(ErrorType.ARGUMENT, "No Argument found.");
        } else if(args.length == 1) {
            localDir = new File(args[0]);
        } else if(args.length == 2) {
            opts = args[0];
            localDir = new File(args[1]);
        }

        if(!localDir.isDirectory()) {
            errorOut(ErrorType.ARGUMENT, "Specified local path is not a directory");
        }

        out.print("Getting file list...");

        File[] mp3files = localDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp3");
            }
        });

        if(mp3files.length > 0) {
            out.println(String.valueOf(mp3files.length) + " file(s) found under " + localDir.getAbsolutePath());
        } else {
            errorOut(ErrorType.NOWORKTODO, "No mp3 files found under " + localDir.getAbsolutePath());
        }

        create(mp3files, localDir.getName());
        System.exit(0);
    }


    private static void create(File[] mp3Files, String mapName) {
        out.println("Creating map " + mapName);
        startTime = System.currentTimeMillis();
        long tempTime = startTime;
        MapFile.Writer writerAudio = null;
        MapFile.Writer writerTags = null;
        try {
            Configuration conf = new Configuration();
            conf.set("mapred.child.java.opts", "-Xmx2048m");
            FileSystem fs = FileSystem.get(URI.create(mapName), conf);

            IntWritable key = new IntWritable();
            MDFWritable value = new MDFWritable();

            // Tamam bu deprecated da yerine ne kullancas a.
            //noinspection deprecation
            writerAudio = new MapFile.Writer(conf, fs, mapName + "_audio", key.getClass(), value.getClass());
            //noinspection deprecation
            writerTags  = new MapFile.Writer(conf, fs, mapName + "_tags",  key.getClass(), value.tags.getClass());
            out.println("Started");
            int i = 0;
            for(File file : mp3Files) {  i++;
                out.print("Adding file " + String.valueOf(i) +  " \"" + file.getName() + "\" ");
                try {
                    key.set(i);
                    value = MDFWritable.createFromFile(file, false);
                    writerAudio.append(key, value);
                    writerTags.append(key, value.tags);
                    out.println("[ OK ]" + String.format("%.2f secs", (double)(System.currentTimeMillis() - tempTime)/1000));
                } catch (ID3Exception e) {
                    out.println("[FAIL]: ID3Exception" );
                } catch (Exception e) {
                    out.println("[FAIL] Exception;");
                    e.printStackTrace();
                }
                tempTime = System.currentTimeMillis();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(writerAudio);
            IOUtils.closeStream(writerTags);
            out.println("Completed! Total time: " + String.format("%.2f secs", (double)(System.currentTimeMillis() - startTime)/1000));
        }

    }

    private static void errorOut(ErrorType t, String info) {


        if(info != null) {
            out.println(info);
        }

        switch (t) {
            case ARGUMENT:
                out.println("Argument error");
                showUsage();
                break;
            case NOWORKTODO:
                out.println("Nothing happened.");
                break;
        }


        System.exit(-1);
    }


    @SuppressWarnings("UnusedDeclaration")
    public static void errorOut(ErrorType t) {
        errorOut(t,null);
    }

    private static void showUsage(){
        System.out.println("USAGE: me.farabi.job.PutToMapFile [opts] <local_sync_dir>");
    }
}
