package me.farabi;

import junit.framework.TestCase;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

/**
 * Farabi
 * User: Bahadir
 * Date: 04.02.2014
 * Time: 14:56
 */
public class MDFWritableTest extends TestCase {

    File[] testFiles;

    @Override
    public void setUp() throws Exception {
        File sampleDir = new File("target/test-classes");
        testFiles = sampleDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".mp3");
            }
        });
    }


    public void testMP3IO() throws Exception {


        for(File file : testFiles) {

                System.out.print("Testing file '" + file.getName() + "' ");
                MDFWritable mdfFromStream = new MDFWritable(new FileInputStream(file),file.length(), true);
                MDFWritable mdfFromFile = new MDFWritable(file, true);

                //Test tags
                Assert.assertEquals(mdfFromFile.tags, mdfFromStream.tags);
                //Test data
                Assert.assertArrayEquals(mdfFromFile.fileData.getBytes(), mdfFromStream.getFileData().getBytes());

                System.out.println(" [ OK ]");

        }



    }
}
