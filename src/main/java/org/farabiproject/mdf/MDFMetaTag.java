package org.farabiproject.mdf;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Bahadir on 02.02.2014.
 */
@XmlRootElement(name="tags")
public class MDFMetaTag {
    private String track;
    private String artist;
    private String album;
    private String genreDesc;
    private int genreID;
    private int year;

    public MDFMetaTag() {}

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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

}
