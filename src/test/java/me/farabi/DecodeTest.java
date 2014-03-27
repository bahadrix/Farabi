package me.farabi;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Farabi
 * User: Bahadir
 * Date: 04.02.2014
 * Time: 14:56
 */

public class DecodeTest extends TestCase {

    public static Logger log = Util.getLogger(DecodeTest.class);

    File[] testFiles;

    @Before
    public void setUp() throws Exception {
        File sampleDir = new File("target/test-classes");
        testFiles = sampleDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".mp3");
            }
        });
    }


    @Test
    public void testMDFDecoding() throws IOException {

        for(File file : testFiles) {
            try {
                log.info("Testing file '" + file.getName() + "' for MDF.");
                byte[] fileBytes = IOUtils.toByteArray(new FileInputStream(file));
                MDFWritable mdf = new MDFWritable(fileBytes);
                AudioInputStream decodeStream = mdf.getDecodeStream();
                assertNotNull(decodeStream);
            } catch (UnsupportedAudioFileException e) {
                assertEquals(
                        "This error must be thrown only for not-an-mp3.mp3 file",
                        "not-an-mp3.mp3", file.getName());
            }
        }

    }

}
