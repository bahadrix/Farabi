package org.farabiproject.mdf;

import org.blinkenlights.jid3.ID3Exception;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Tests for MDF Object
 * Created by Bahadir on 02.02.2014.
 *
 */
public class MusicDataFileTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private MusicDataFile sampleMDF;
    private FileInputStream sampleMP3Stream;
    @Before
    public void createTestData() throws FileNotFoundException, ID3Exception {
        sampleMDF = new MusicDataFile();
        sampleMDF.setFrames(new short[]{123, 6468, 6548, 6541, 2616, 5465});
        sampleMDF.setOutputChannels(2);
        sampleMDF.setOutputFrequency(44100);


        MDFMetaTag tags;
        //test no tag mp3 file
        MDFMetaTag.createFromFile(new File("target/test-classes/sample-notag.mp3"));

        //read tags
        tags = MDFMetaTag.createFromFile(new File("target/test-classes/sample.mp3"));

        sampleMDF.setTags(tags);

        sampleMP3Stream = new FileInputStream("target/test-classes/sample.mp3");

        Assert.assertNotNull("Sample mp3 file 'sample.mp3' not found.", sampleMP3Stream);
    }


    @Test
    public void testSerialization() throws Exception {

        JAXBContext context = JAXBContext.newInstance(MusicDataFile.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        m.marshal(sampleMDF, System.out);

        File testFile = folder.newFile("test.mdf");
        m.marshal(sampleMDF, testFile); // save to file

        //-- deserialize

        Unmarshaller um = context.createUnmarshaller();
        MusicDataFile mdfRead = (MusicDataFile) um.unmarshal(testFile);

        Assert.assertNotNull("Can't deserialize mdf file", mdfRead);

    }
}
