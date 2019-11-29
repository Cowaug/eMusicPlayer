package project.exos.emusicplayer.Entity;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface SongDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void InsertSong(Song song);

    @Query("SELECT * FROM SongList ORDER BY title asc")
    List<Song> GetSongAsc();

    @Query("SELECT * FROM SongList ORDER BY title desc")
    List<Song> GetSongDesc();


    @Query("SELECT artist FROM SongList ORDER BY artist asc")
    List<String> GetArtistAsc();

    @Query("SELECT artist FROM SongList ORDER BY artist desc")
    List<String> GetArtistDesc();


    @Query("SELECT album FROM SongList ORDER BY album asc")
    List<String> GetAlbumAsc();

    @Query("SELECT album FROM SongList ORDER BY album desc")
    List<String> GetAlbumDesc();


    @Query("SELECT location FROM SongList ORDER BY location asc")
    List<String> GetLocationAsc();

    @Query("SELECT location FROM SongList ORDER BY location desc")
    List<String> GetLocationDesc();

    @Update
    void UpdateSong(Song song);

    @Query("SELECT * FROM SongList WHERE title LIKE :title")
    Song FindSongByTitle(String title);

    @Query("SELECT * FROM SongList WHERE artist LIKE :artist")
    List<Song> FindSongByArtist(String artist);

    @Query("SELECT * FROM SongList WHERE album LIKE :album")
    List<Song> FindSongByAlbum(String album);

    @Query("SELECT * FROM SongList WHERE location LIKE :location")
    List<Song> FindSongByLocation(String location);

    @Query("SELECT * FROM SongList WHERE path LIKE :path")
    Song FindSongByPath(String path);

    @Query("SELECT * FROM SongList WHERE favorite IS 1 ORDER BY title asc")
    List<Song> FindSongByFavorite();

    @Query("SELECT * FROM SongList WHERE (title LIKE :keyword) OR (artist LIKE :keyword) OR (album LIKE :keyword)")
    List<Song> FindAllByKeyword(String keyword);

    @Query("DELETE FROM Songlist WHERE path LIKE :path")
    void DeleteSong(String path);
}
