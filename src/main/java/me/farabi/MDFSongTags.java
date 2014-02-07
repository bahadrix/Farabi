package me.farabi;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.SequenceFileInputFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Farabi
 * User: Bahadir
 * Date: 07.02.2014
 * Time: 13:59
 */
public class MDFSongTags implements Writable {
    protected IntWritable ID = new IntWritable(0); // can't be null
    protected Text title = new Text("<unknown>");
    protected Text artist = new Text("<unknown>");
    protected Text album = new Text("<unknown>");
    protected Text genreDesc = new Text("<unknown>");
    protected Text year = new Text("<unknown>");

    public MDFSongTags() {}

    public IntWritable getID() {
        return ID;
    }

    public void setID(IntWritable ID) {
        this.ID = ID;
    }



    public Text getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? new Text() : new Text(title);
    }
    public Text getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist =  artist == null ? new Text() : new Text(artist);
    }

    public Text getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album == null ? new Text() : new Text(album);
    }

    public Text getGenreDesc() {
        return genreDesc;
    }


    public void setGenreDesc(String genreDesc) {
        this.genreDesc = genreDesc == null ? new Text() : new Text(genreDesc);
    }

    public Text getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year == null ? new Text() : new Text(year);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {

    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {

    }
}
