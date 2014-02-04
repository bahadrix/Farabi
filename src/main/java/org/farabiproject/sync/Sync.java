package org.farabiproject.sync;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapFile;
import org.blinkenlights.jid3.ID3Exception;
import org.farabiproject.MDFWritable;

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
public class Sync {
    static enum ErrorType {ARGUMENT, NOWORKTODO}
    static PrintStream out;
    public static void main(String[] args) {

        out = System.out;


        if(args.length == 0) {
            errorOut(ErrorType.ARGUMENT, "No Argument found.");
        }

        File localDir = new File(args[0]);

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
        MapFile.Writer writer = null;
        try {

            Configuration conf = new Configuration();
            conf.set("mapred.child.java.opts", "-Xmx2048m");
            FileSystem fs = FileSystem.get(URI.create(mapName), conf);

            IntWritable key = new IntWritable();
            MDFWritable value = new MDFWritable();

            // Tamam bu deprecated da yerine ne kullancas
            writer = new MapFile.Writer(conf, fs, mapName, key.getClass(), value.getClass());
            out.println("Started");
            int i = 0;
            for(File file : mp3Files) {  i++;
                out.print("Adding file " + String.valueOf(i) +  " \"" + file.getName() + "\" ");
                try {

                    key.set(i);
                    value = MDFWritable.createFromFile(file);
                    writer.append(key, value);
                    out.println("[ OK ]" );
                } catch (ID3Exception e) {
                    out.println("[FAIL]: ID3Exception" );
                } catch (Exception e) {
                    out.println("[FAIL] Exception;");
                    e.printStackTrace();

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(writer);
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
        }


        System.exit(-1);
    }

    public static void errorOut  (ErrorType t) {
        errorOut(t,null);
    }

    private static void showUsage(){
        System.out.println("USAGE: org.farabiproject.sync.Sync <local_sync_dir>");
    }
}