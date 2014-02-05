package me.farabi.mdf;

import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.MediaFile;
import org.blinkenlights.jid3.v1.ID3V1_0Tag;
import org.blinkenlights.jid3.v1.ID3V1_1Tag;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

/**
 * MDF File Music Info Tags
 * Created by Bahadir on 02.02.2014.
 *
 */

@Deprecated // MDFWritable olunca buna gerek kalmiyor.
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement(name="tags")
public class MDFMetaTag {
    private String track;
    private String title;
    private String artist;
    private String album;
    private String genreDesc;
    private int genreID;
    private String year;

    public MDFMetaTag() {}

    public static MDFMetaTag createFromFile(File file) throws ID3Exception {

        MediaFile sampleMP3File = new MP3File(file);
        ID3Tag[] tags = sampleMP3File.getTags();

        MDFMetaTag mdfTag = new MDFMetaTag();

        for (ID3Tag tag : tags) {
            if (tag instanceof ID3V1_0Tag) {
                ID3V1_0Tag oID3V1_0Tag = (ID3V1_0Tag) tag;
                if (oID3V1_0Tag.getTitle() != null) {
                    mdfTag.setTitle(oID3V1_0Tag.getTitle());
                    mdfTag.setAlbum(oID3V1_0Tag.getAlbum());
                    mdfTag.setArtist(oID3V1_0Tag.getArtist());
                    mdfTag.setGenreID(oID3V1_0Tag.getGenre().getByteValue());
                    mdfTag.setGenreDesc(oID3V1_0Tag.getGenre().toString());
                    mdfTag.setYear(oID3V1_0Tag.getYear());
                }
            } else if (tag instanceof ID3V1_1Tag) {
                ID3V1_1Tag oID3V1_1Tag = (ID3V1_1Tag) tag;
                if (oID3V1_1Tag.getTitle() != null) {
                    mdfTag.setTitle(oID3V1_1Tag.getTitle());
                    mdfTag.setAlbum(oID3V1_1Tag.getAlbum());
                    mdfTag.setArtist(oID3V1_1Tag.getArtist());
                    mdfTag.setGenreID(oID3V1_1Tag.getGenre().getByteValue());
                    mdfTag.setGenreDesc(oID3V1_1Tag.getGenre().toString());
                    mdfTag.setYear(oID3V1_1Tag.getYear());
                }
            } else if (tag instanceof ID3V2_3_0Tag) {
                ID3V2_3_0Tag oID3V2_3Tag = (ID3V2_3_0Tag) tag;
                if (oID3V2_3Tag.getTitle() != null) {
                    mdfTag.setTitle(oID3V2_3Tag.getTitle());
                    mdfTag.setAlbum(oID3V2_3Tag.getAlbum());
                    mdfTag.setArtist(oID3V2_3Tag.getArtist());
                    mdfTag.setGenreDesc(oID3V2_3Tag.getGenre());
                    try {
                        mdfTag.setYear(String.valueOf(oID3V2_3Tag.getYear()));
                    } catch (ID3Exception e) {
                        mdfTag.setYear("");
                    }

                }
            }

        }

        return mdfTag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenreDesc() {
        return genreDesc;
    }

    public void setGenreDesc(String genreDesc) {
        this.genreDesc = genreDesc;
    }

    public int getGenreID() {
        return genreID;
    }

    public void setGenreID(int genreID) {
        this.genreID = genreID;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

}
