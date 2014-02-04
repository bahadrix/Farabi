package org.farabiproject.mdf;

import org.blinkenlights.jid3.ID3Exception;
import org.junit.*;
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
@Ignore // Bunu simdilik test etmios
public class MusicDataFileTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private MusicDataFile sampleMDF;
    private FileInputStream sampleMP3Stream;
    @Before
    public void createTestData() throws FileNotFoundException, ID3Exception {


        //test no tag mp3 file
        MDFMetaTag.createFromFile(new File("target/test-classes/sample-notag.mp3"));

        //read tags

        sampleMP3Stream = new FileInputStream("target/test-classes/sample.mp3");
        sampleMDF = MusicDataFile.create(sampleMP3Stream);

    }


    @Test
    public void testSerialization() throws Exception {

        JAXBContext context = JAXBContext.newInstance(MusicDataFile.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        //m.marshal(sampleMDF, System.out);

        File testFile = folder.newFile("test.mdf");
        m.marshal(sampleMDF, testFile); // save to file
        System.out.printf(testFile.getAbsolutePath());
        //-- deserialize

        Unmarshaller um = context.createUnmarshaller();
        MusicDataFile mdfRead = (MusicDataFile) um.unmarshal(testFile);

        Assert.assertNotNull("Can't deserialize mdf file", mdfRead);

    }
}
