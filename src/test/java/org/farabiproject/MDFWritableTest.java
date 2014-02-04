package org.farabiproject;

import junit.framework.TestCase;

import java.io.File;

/**
 * Farabi
 * User: Bahadir
 * Date: 04.02.2014
 * Time: 14:56
 */
public class MDFWritableTest extends TestCase {
    public void testCreateFromMp3() throws Exception {

        MDFWritable mdfw = MDFWritable.createFromFile(new File("target/test-classes/sample.mp3"));



    }
}
