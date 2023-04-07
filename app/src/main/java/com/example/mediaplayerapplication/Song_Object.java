package com.example.mediaplayerapplication;

public class Song_Object {
    private String SongName;
    private String ArtistName;
    private String FileName;

    public Song_Object() {
    }

    public Song_Object(String songName, String artistName, String fileName) {
        SongName = songName;
        ArtistName = artistName;
        FileName = fileName;
    }

    public String getSongName() {
        return SongName;
    }

    public void setSongName(String songName) {
        SongName = songName;
    }

    public String getArtistName() {
        return ArtistName;
    }

    public void setArtistName(String artistName) {
        ArtistName = artistName;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }
}
