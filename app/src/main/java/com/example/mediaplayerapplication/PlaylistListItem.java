package com.example.mediaplayerapplication;

public class PlaylistListItem {
    private String playlistName;
    private int menuImage;

    public PlaylistListItem() {
    }

    public PlaylistListItem(String playlistName, int menuImage) {
        this.playlistName = playlistName;
        this.menuImage = menuImage;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public int getMenuImage() {
        return menuImage;
    }

    public void setMenuImage(int menuImage) {
        this.menuImage = menuImage;
    }
}
