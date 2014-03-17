package me.farabi;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Farabi
 * User: Bahadir
 * Date: 04.02.2014
 * Time: 17:04
 */
@SuppressWarnings("UnusedDeclaration")
public class Util {

    private static org.apache.log4j.Logger log = Logger.getLogger(Util.class);

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

}
