package me.farabi;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

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
    public void testMP3SPI() throws Exception{

        File testFile = null;
        for(File file : testFiles) {
            if(file.getName().contains("notag.mp3")) {
                testFile = file;
                break;
            }
        }
        assertNotNull(testFile);

        File testOutputFile = new File(testFile.getAbsolutePath().replace(".mp3",".wav"));
        testOutputFile.createNewFile();

        log.info(testOutputFile.getAbsolutePath());

        MpegAudioFileReader reader = new MpegAudioFileReader();
        AudioInputStream ain = AudioSystem.getAudioInputStream(testFile);
        assertNotNull(ain);

        AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(testFile);




        /**
         * Example derived from following url.
         * @see http://www.javalobby.org/java/forums/t18465.html
         */


        AudioFormat baseFormat = ain.getFormat();
        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,    // Encoding to use
                baseFormat.getSampleRate(),	        // sample rate (same as base format)
                16,				                    // sample size in bits (thx to Javazoom)
                baseFormat.getChannels(),	        // # of Channels
                baseFormat.getChannels()*2,	        // Frame Size
                baseFormat.getSampleRate(),	        // Frame Rate
                true				                // Big Endian
        );
        log.info(decodedFormat);
        AudioInputStream decodedIn = AudioSystem.getAudioInputStream(decodedFormat, ain);

        FileOutputStream out = new FileOutputStream(testOutputFile);

        byte[] data = new byte[4096];
        int nBytesRead;
        while((nBytesRead = decodedIn.read(data,0,data.length)) != -1) {
            out.write(data,0,nBytesRead);
        }

        out.close();

    }
   /*
    @Ignore
    public void testMP3IO() throws Exception {


        for(File file : testFiles) {

            System.out.print("Testing file '" + file.getName() + "' ");
            MDFWritable mdfFromStream = new MDFWritable(FileUtils.readFileToByteArray(file),file.length(), true);
            MDFWritable mdfFromFile = new MDFWritable(file, true);

            //Test tags
            Assert.assertEquals(mdfFromFile.tags, mdfFromStream.tags);
            //Test data
            Assert.assertArrayEquals(mdfFromFile.fileData.getBytes(), mdfFromStream.getFileData().getBytes());

            File wavFile = new File(file.getAbsolutePath().replace(".mp3", ".wav"));
            FileOutputStream fout = new FileOutputStream(wavFile);
            IOUtils.write(mdfFromStream.fileData.getBytes(),fout);
            fout.close();
            System.out.println(" [ OK ] " + wavFile.getAbsolutePath());

        }



    }
    */
}
