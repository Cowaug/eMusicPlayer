package project.exos.emusicplayer.Entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "SongList")
public class Song {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "PATH")
    private String path;

    @ColumnInfo(name = "Title")
    private String title;

    @ColumnInfo(name = "Artist")
    private String artist;

    @ColumnInfo(name = "Album")
    private String album;

    @ColumnInfo(name = "Location")
    private String location;

    @ColumnInfo(name = "Filename")
    private String filename;

    @ColumnInfo(name = "Favorite")
    private boolean favorite;

    public Song(@NonNull String path, String title, String artist, String album, String location, String filename, boolean favorite) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.location = location;
        this.filename = filename;
        this.favorite = favorite;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getLocation() {
        return location;
    }

    public String getFilename() {
        return filename;
    }

    public Boolean isFavorite() {
        return favorite;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        Song song = (Song) obj;
        return getPath().equals(song.getPath());
    }

}
