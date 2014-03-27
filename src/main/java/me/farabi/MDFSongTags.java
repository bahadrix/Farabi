package me.farabi;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3Stream;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.log4j.Logger;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Farabi
 * User: Bahadir
 * Date: 07.02.2014
 * Time: 13:59
 */
@SuppressWarnings("UnusedDeclaration")
public class MDFSongTags implements Writable {
    private final static Logger log = Util.getLogger(MDFSongTags.class);

    protected IntWritable ID = new IntWritable(0); // can't be null
    protected Text title = new Text("<unknown>");
    protected Text artist = new Text("<unknown>");
    protected Text album = new Text("<unknown>");
    protected Text genreDesc = new Text("<unknown>");
    protected Text year = new Text("<unknown>");

    public MDFSongTags() {}


    @Deprecated // Diger construtor kullanilmali
    public MDFSongTags(InputStream in, long length, int ID) throws InvalidDataException, IOException, UnsupportedTagException {
        Mp3Stream mp3Stream = new Mp3Stream(in, length);
        ID3v1 tags = mp3Stream.getId3v2Tag() != null ?
                mp3Stream.getId3v2Tag() :
                mp3Stream.getId3v1Tag();
        setID(new IntWritable(ID));
        if(tags != null) {
            setAlbum(tags.getAlbum());
            setArtist(tags.getArtist());
            setGenreDesc(tags.getGenreDescription());
            setTitle(tags.getTitle());
            setYear(tags.getYear());
        }
    }

    public MDFSongTags(AudioFileFormat fileFormat) {

        if(fileFormat instanceof TAudioFileFormat) {
                Map properties = fileFormat.properties();
                setAlbum((String) properties.get("album"));
                setArtist((String) properties.get("author"));
                setGenreDesc((String) properties.get("mp3.id3tag.genre"));
                setTitle((String) properties.get("title"));
                setYear((String) properties.get("date"));
        } else {
            log.warn("Current AudioFileformat is not an instance of TAudioFileFormat. Tag read is skipped.");
        }

    }

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
    public void write(DataOutput out) throws IOException {
        ID.write(out);
        title.write(out);
        artist.write(out);
        album.write(out);
        genreDesc.write(out);
        year.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        ID.readFields(in);
        title.readFields(in);
        artist.readFields(in);
        album.readFields(in);
        genreDesc.readFields(in);
        year.readFields(in);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MDFSongTags that = (MDFSongTags) o;

        if (!ID.equals(that.ID)) return false;
        if (!album.equals(that.album)) return false;
        if (!artist.equals(that.artist)) return false;
        if (!genreDesc.equals(that.genreDesc)) return false;
        if (!title.equals(that.title)) return false;
        //noinspection RedundantIfStatement
        if (!year.equals(that.year)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ID.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + artist.hashCode();
        result = 31 * result + album.hashCode();
        result = 31 * result + genreDesc.hashCode();
        result = 31 * result + year.hashCode();
        return result;
    }
}
