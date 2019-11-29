package project.exos.emusicplayer.Entity;

import java.util.ArrayList;

public class Playlist {
    private String playlistName;
    private ArrayList<Song> songList = new ArrayList<>();

    public Playlist(String playlistName, ArrayList<Song> songList) {
        this.playlistName = playlistName;
        this.songList.addAll(songList);
    }

    public String GetPlaylistName() {
        return playlistName;
    }

    public ArrayList<Song> GetSongList() {
        return songList;
    }
}
