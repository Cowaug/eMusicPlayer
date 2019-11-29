package project.exos.emusicplayer.Entity;

import java.util.Comparator;

public class CompareSong implements Comparator<Song> {
    public int compare(Song firstSong,Song secondSong){
        return firstSong.getTitle().compareTo(secondSong.getTitle());
    }
}
