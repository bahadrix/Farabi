package me.farabi;

import com.mongodb.MongoClient;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.*;

import java.io.*;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Farabi
 * User: Bahadir
 * Date: 04.02.2014
 * Time: 17:04
 */
@SuppressWarnings("UnusedDeclaration")
public class Util {

    private static org.apache.log4j.Logger log = Logger.getLogger(Util.class);

    static {
        ConsoleAppender a = (ConsoleAppender) Logger.getRootLogger().getAppender("console");
        if(a!=null) {
            a.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} [%-5p %c{1}] %m%n"));
        }


    }

    public static String getMD5(String original) {
        StringBuffer sb = null;
        try {


            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(original.getBytes());

            byte byteData[] = md.digest();

            sb = new StringBuffer();
            for (byte aByteData : byteData) {
                sb.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        assert sb != null;
        return sb.toString();

    }

    public static boolean deleteHDFSFile(Path path) throws IOException {

        boolean deleted = false;

        JobConf conf = new JobConf();
        FileSystem fs = path.getFileSystem(conf);
        int i = 0;
        while (true) { i++;
            if (fs.exists(path)) {
                fs.delete(path, true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if(i>1) // ilk denemede yoksa zaten hic olmamis.
                    deleted = true;
                break;
            }
        }


        return deleted;

    }

    public static org.apache.log4j.Logger getLogger(Class c) {
        //private static org.apache.log4j.Logger log = Logger.getLogger(CreatePack.class);

        org.apache.log4j.Logger logga = Logger.getLogger(c);
        logga.setLevel(Level.DEBUG);


        return logga;
    }

    public static Properties readPropertiesFile(String filePath) throws IOException {
        Properties prop = new Properties();
        InputStream input = new FileInputStream(filePath);

        // load a properties file
        prop.load(input);

        input.close();

        return prop;

    }

    /**
     * Parses command line arguments to map.
     * Example:
     *      program input output -m 128 912 -f ALL
     *      Map Structure:
     *      arguments
     *      |
     *      |--> '_'    ['input','output']
     *      |--> '-m'   ['128','912']
     *      |--> '-f'   ['ALL']
     * @param args Command line parameters array.
     * @return Argument list map.
     */
    public static Map<String, List<String>> parseProgramArguments(String[] args) {
        Map<String, List<String>> parsedArgs = new LinkedHashMap<String, List<String>>();


        String lastParam = "_";
        for (String arg : args) {

            if (arg.startsWith("-")) {
                if (!lastParam.equals(arg)) {
                    parsedArgs.put(arg, new ArrayList<String>());
                    lastParam = arg;
                }

            } else {
                if (lastParam.equals("_")) {
                    if (!parsedArgs.containsKey("_")) {
                        parsedArgs.put("_", new ArrayList<String>());
                    }
                }

                parsedArgs.get(lastParam).add(arg);
            }

        }

        return parsedArgs;

    }

    public static boolean checkMongoDBServer(String host, String port) {

        boolean noprob = false;

        try {
            int portint = Integer.parseInt(port);
            MongoClient mongo = new MongoClient( host , portint );
            noprob = true;
        } catch (NumberFormatException e) {
            log.error("Invalid port: " + port);
        } catch (UnknownHostException e) {
            log.error("Host unknown: " + host);
        }

        return noprob;

    }

    public static Properties getFarabiProps(Map<String, List<String>> parsedProgramArguments) {

        Properties props = null;
        String fileLocation = "farabi.properties";


        if(parsedProgramArguments.get("-p") != null) {
            if(parsedProgramArguments.get("-p").size() == 1) {
                fileLocation = parsedProgramArguments.get("-p").get(0);
            }
        }

        File file = new File(fileLocation);

        if(!file.exists()) {
            log.error("Properties file not found in location: " + fileLocation);

        }

        try {
            props = readPropertiesFile(fileLocation);
        } catch (IOException e) {
            log.error("IO Exception on properties file " + fileLocation);
            log.error(e);

        }

        return props;

    }
}
