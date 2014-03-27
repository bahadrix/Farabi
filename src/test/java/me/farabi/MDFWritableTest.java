package me.farabi;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

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
@Ignore
public class MDFWritableTest extends TestCase {



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
    public void testMP3IO() throws Exception {


        for(File file : testFiles) {

            System.out.print("Testing file '" + file.getName() + "' ");
            MDFWritable mdfFromStream = new MDFWritable(FileUtils.readFileToByteArray(file),file.length(), true);
            MDFWritable mdfFromFile = new MDFWritable(file, true);

            //Test tags
            Assert.assertEquals(mdfFromFile.tags, mdfFromStream.tags);
            //Test data
            Assert.assertArrayEquals(mdfFromFile.fileData.getBytes(), mdfFromStream.getFileData().getBytes());

            File wavFile = new File(file.getName().replace(".mp3", ".wav"));
            FileOutputStream fout = new FileOutputStream(wavFile);
            IOUtils.write(mdfFromStream.fileData.getBytes(),fout);
            fout.close();
            System.out.println(" [ OK ] " + wavFile.getAbsolutePath());

        }



    }
}
