package me.farabi.model;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import me.farabi.Util;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Farabi
 * User: Bahadir
 * Date: 18.03.2014
 * Time: 11:08
 */
 @Ignore
public class SongOneTest {

    private static org.apache.log4j.Logger log = Util.getLogger(SongOneTest.class);

    DB db;
    Datastore ds;
    Morphia morphia;
    @Before
    public void setUp() throws Exception {

        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        db = mongo.getDB("testdb");
        morphia = new Morphia();
        ds = morphia.createDatastore(mongo, "testdb", null, null);


    }

    @Test
    public void testMongo() throws Exception {

        SongOne sone = new SongOne();
        sone.setBitrate(128);
        sone.setOutputFrequency(44100);
        sone.setOutputChannels(2);
        sone.setVbr(false);
        sone.setFramesize(64);

        SongOne.Tags tags = new SongOne.Tags();
        tags.title = "Le şe temi kantare";
        tags.ID = 0;
        tags.artist = "Küçük ibo";
        tags.album = "Burning Lies";
        tags.genreDesc = "brutal flowers";
        tags.year = "2014";

        sone.setTags(tags);


        morphia.map(SongOne.class);

        ds.save(sone);

        log.info("Data store saved");



    }
}
