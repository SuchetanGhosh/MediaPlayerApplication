package com.example.mediaplayerapplication;

public class SongListItem {
    private String songName;
    private String artistName;
    private int menuImage;

    public SongListItem() {
    }

    public SongListItem(String songName, String artistName, int menuImage) {
        this.songName = songName;
        this.artistName = artistName;
        this.menuImage = menuImage;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public int getMenuImage() {
        return menuImage;
    }

    public void setMenuImage(int menuImage) {
        this.menuImage = menuImage;
    }
}
