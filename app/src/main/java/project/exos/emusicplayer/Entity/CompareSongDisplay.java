package project.exos.emusicplayer.Entity;

import java.util.Comparator;

public class CompareSongDisplay implements Comparator<SongDisplay> {
    public int compare(SongDisplay firstSong, SongDisplay secondSong) {
        return firstSong.getFirstLine().compareTo(secondSong.getFirstLine());
    }
}
