package project.exos.emusicplayer.Entity;

import java.util.Comparator;

public class ComparePlaylist implements Comparator<Playlist> {
    public int compare(Playlist firstPlaylist,Playlist secondPlaylist){
        return firstPlaylist.GetPlaylistName().compareTo(secondPlaylist.GetPlaylistName());
    }
}
