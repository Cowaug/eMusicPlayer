package project.exos.emusicplayer.Entity;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Pair;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import project.exos.emusicplayer.R;

import static project.exos.emusicplayer.GUI.GUI_Library.ALBUM_DETAIL;
import static project.exos.emusicplayer.GUI.GUI_Library.ALBUM_LIST;
import static project.exos.emusicplayer.GUI.GUI_Library.ARTIST_DETAIL;
import static project.exos.emusicplayer.GUI.GUI_Library.ARTIST_LIST;
import static project.exos.emusicplayer.GUI.GUI_Library.FAVORITE;
import static project.exos.emusicplayer.GUI.GUI_Library.LOCATION_DETAIL;
import static project.exos.emusicplayer.GUI.GUI_Library.LOCATION_LIST;
import static project.exos.emusicplayer.GUI.GUI_Library.PLAYLIST_DETAIL;
import static project.exos.emusicplayer.GUI.GUI_Library.PLAYLIST_LIST;
import static project.exos.emusicplayer.GUI.GUI_Library.TRACK;

public class DataRepository extends Activity {
    private static SongDatabase songDatabase;

    private static ArrayList<Song> playingQueue = new ArrayList<>();
    private static int indexInQueue = -1;

    private static ArrayList<Playlist> playlistList;
    private static ArrayList<String> pathOfFavoriteSong = new ArrayList<>();

    private static Pair<String, Boolean> currentTheme = new Pair<>("Green", false);
    public static boolean scanMedia = true;
    public static String progress = "";

    public interface Callback {
        void ApplyChanges();

        void ReturnList(ArrayList<SongDisplay> list);
    }

    public DataRepository(Context context) {
        //region variable
        if (songDatabase == null) {
            String DB_NAME = "SongDB.db";
            songDatabase = Room.databaseBuilder(context, SongDatabase.class, DB_NAME)
                    //.allowMainThreadQueries()
                    .build();
            LoadData(context);
        }
    }

    public static void DeleteSong(final String path) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                songDatabase.songDAO().DeleteSong(path);
                return null;
            }
        }.execute();
    }

    public static void ScanSong(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                boolean firstTimeLaunch = pathOfFavoriteSong == null;
                if (firstTimeLaunch) pathOfFavoriteSong = new ArrayList<>();
                int count = 0;
                DataRepository.scanMedia = true;
                songDatabase.clearAllTables();
                ContentResolver contentResolver = context.getContentResolver();
                Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String selection = MediaStore.Audio.Media.IS_MUSIC;
                Cursor songCursor = contentResolver.query(songUri, null, selection, null, null);
                if (songCursor != null && songCursor.moveToFirst()) {
                    do {
                        String path = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        String location = path
                                .replace(songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)), "");
                        boolean favorite = false;
                        //System.out.println(path);
                        if (firstTimeLaunch) {
                            if (new MP3Info(path).IsFavorite()) {
                                favorite = true;
                                pathOfFavoriteSong.add(path);
                            } else {
                                pathOfFavoriteSong.remove(path);
                            }
                        } else {
                            for (String favoritePath : pathOfFavoriteSong) {
                                if (favoritePath.equals(path)) favorite = true;
                            }
                        }
                        for (String favoritePath : pathOfFavoriteSong) {
                            if (favoritePath.equals(path)) favorite = true;
                        }
                        songDatabase.songDAO().InsertSong(new Song(
                                path,
                                songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                                songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                                songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                                location.contains("/storage/emulated/0") ? location.replace("/storage/emulated/0", "Internal Storage") : "SD Card" + location.substring(18),
                                songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                                favorite
                        ));
                        progress = count++ + "/" + songCursor.getCount();
                    } while (songCursor.moveToNext());
                }
                if (songCursor != null) songCursor.close();
                saveObject(context, pathOfFavoriteSong, "favoriteSong");
                DataRepository.scanMedia = false;
                return null;
            }
        }.execute();
    }

    public static void UpdateSong(final Context context, final String path) {
        final MP3Info mp3Info = new MP3Info(path);
        final File file = new File(path);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                boolean favorite = new MP3Info(path).IsFavorite();
                if (favorite)
                    pathOfFavoriteSong.add(path);
                else
                    pathOfFavoriteSong.remove(path);
                for (Song song : playingQueue) {
                    if (song.getPath().equals(path)) song.setFavorite(favorite);
                }
                songDatabase.songDAO().UpdateSong(new Song(
                        path,
                        mp3Info.GetTitle(),
                        mp3Info.GetArtist(),
                        mp3Info.GetAlbum(),
                        file.getAbsolutePath().replace(file.getName(), ""),
                        file.getName(),
                        favorite
                ));
                saveObject(context, pathOfFavoriteSong, "favoriteSong");
                return null;
            }
        }.execute();
    }

    public static void GetLibrary(final int viewOption, final String keyword, final Callback callback) {
        new AsyncTask<Void, Void, ArrayList<SongDisplay>>() {
            @Override
            protected ArrayList<SongDisplay> doInBackground(Void... voids) {
                ArrayList<SongDisplay> arrayList = new ArrayList<>();
                arrayList.clear();
                switch (viewOption) {
                    default:
                    case TRACK: {
                        List<Song> tmpList = songDatabase.songDAO().GetSongAsc();
                        for (Song song : tmpList) {
                            if (indexInQueue >= 0 && song.getPath().equals(GetPathOfPlayingSong()))
                                arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), true));
                            else
                                arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), false));
                        }
                        break;
                    }
                    case ARTIST_LIST: {
                        List<String> tmpList = songDatabase.songDAO().GetArtistAsc();
                        if (tmpList.size() == 0) break;
                        String tmpString = tmpList.get(0);
                        int tmpCount = 0;
                        for (String s : tmpList) {
                            if (s.equals(tmpString)) tmpCount++;
                            else {
                                arrayList.add(new SongDisplay(tmpString, tmpCount + "Songs", null, false, false));
                                tmpString = s;
                                tmpCount = 1;
                            }
                        }
                        arrayList.add(new SongDisplay(tmpString, tmpCount + " Songs", null, false, false));
                        break;
                    }
                    case ALBUM_LIST: {
                        List<String> tmpList = songDatabase.songDAO().GetAlbumAsc();
                        if (tmpList.size() == 0) break;
                        String tmpString = tmpList.get(0);
                        int tmpCount = 0;
                        for (String s : tmpList) {
                            if (s.equals(tmpString)) tmpCount++;
                            else {
                                arrayList.add(new SongDisplay(tmpString, tmpCount + "Songs", null, false, false));
                                tmpString = s;
                                tmpCount = 1;
                            }
                        }
                        arrayList.add(new SongDisplay(tmpString, tmpCount + " Songs", null, false, false));
                        break;
                    }
                    case LOCATION_LIST: {
                        List<String> tmpList = songDatabase.songDAO().GetLocationAsc();
                        if (tmpList.size() == 0) break;
                        String tmpString = tmpList.get(0);
                        int tmpCount = 0;
                        for (String s : tmpList) {
                            if (s.equals(tmpString)) tmpCount++;
                            else {
                                arrayList.add(new SongDisplay(tmpString, tmpCount + "Songs", null, false, false));
                                tmpString = s;
                                tmpCount = 1;
                            }
                        }
                        arrayList.add(new SongDisplay(tmpString, tmpCount + " Songs", null, false, false));
                        break;
                    }
                    case PLAYLIST_LIST: {
                        for (Playlist playlist : playlistList) {
                            arrayList.add(new SongDisplay(playlist.GetPlaylistName(), playlist.GetSongList().size() + " Songs", null, false, false));
                        }
                        break;
                    }
                    case ARTIST_DETAIL: {
                        List<Song> tmpList = songDatabase.songDAO().FindSongByArtist(keyword);
                        for (Song song : tmpList) {
                            if (playingQueue.size() != 0 && song.getPath().equals(GetPathOfPlayingSong()))
                                arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), true));
                            else
                                arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), false));
                        }
                        break;
                    }
                    case ALBUM_DETAIL: {
                        List<Song> tmpList = songDatabase.songDAO().FindSongByAlbum(keyword);
                        for (Song song : tmpList) {
                            if (playingQueue.size() != 0 && song.getPath().equals(GetPathOfPlayingSong()))
                                arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), true));
                            else
                                arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), false));
                        }
                        break;
                    }
                    case LOCATION_DETAIL: {
                        List<Song> tmpList = songDatabase.songDAO().FindSongByLocation(keyword);
                        for (Song song : tmpList) {
                            if (playingQueue.size() != 0 && song.getPath().equals(GetPathOfPlayingSong()))
                                arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), true));
                            else
                                arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), false));

                        }
                        break;
                    }
                    case PLAYLIST_DETAIL: {
                        for (Playlist playlist : playlistList) {
                            if (playlist.GetPlaylistName().equals(keyword))
                                for (Song song : playlist.GetSongList()) {
                                    if (indexInQueue >= 0 && song.getPath().equals(GetPathOfPlayingSong()))
                                        arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), true));
                                    else
                                        arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), false));
                                }
                        }
                        break;
                    }
                    case FAVORITE: {
                        List<Song> tmpList = songDatabase.songDAO().FindSongByFavorite();
                        for (Song song : tmpList) {
                            if (playingQueue.size() != 0 && song.getPath().equals(GetPathOfPlayingSong()))
                                arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), true));
                            else
                                arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), false));
                        }
                        break;
                    }
                }

                return arrayList;
            }

            @Override
            protected void onPostExecute(ArrayList<SongDisplay> arrayList) {
                callback.ReturnList(arrayList);
            }
        }.execute();
    }

    public static void GetSongByKeyword(final String keyword, final Callback callback) {
        new AsyncTask<Void, Void, ArrayList<SongDisplay>>() {
            @Override
            protected ArrayList<SongDisplay> doInBackground(Void... voids) {
                ArrayList<SongDisplay> arrayList = new ArrayList<>();
                List<Song> tmpList = songDatabase.songDAO().GetSongAsc();
                for (Song song : tmpList) {
                    if (song.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                        if (playingQueue.size() != 0 && song.getPath().equals(GetPathOfPlayingSong()))
                            arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), true));
                        else
                            arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), false));
                    }
                }
                return arrayList;
            }

            @Override
            protected void onPostExecute(ArrayList<SongDisplay> list) {
                callback.ReturnList(list);
            }
        }.execute();
    }

    public static void AddToNewQueueAndPlay(final ArrayList<SongDisplay> songList, final String songPath) {
        new AsyncTask<Void, Void, Pair>() {
            @Override
            protected Pair doInBackground(Void... voids) {
                ArrayList<Song> tmpQueue = new ArrayList<>();
                int tmpIndex = 0;
                for (SongDisplay songDisplay : songList) {
                    tmpQueue.add(songDatabase.songDAO().FindSongByPath(songDisplay.getPath()));
                }
                for (int i = 0; i < tmpQueue.size(); i++) {
                    if (tmpQueue.get(i).getPath().equals(songPath)) tmpIndex = i;
                }
                if (MediaController.GetShuffleStatus()) {
                    Song playingSong = tmpQueue.get(tmpIndex);
                    Collections.shuffle(tmpQueue);
                    Collections.swap(tmpQueue, tmpQueue.indexOf(playingSong), 0);
                    tmpIndex = 0;
                }
                return new Pair(tmpQueue, tmpIndex);
            }

            @Override
            protected void onPostExecute(Pair pair) {
                DataRepository.playingQueue = (ArrayList<Song>) pair.first;
                indexInQueue = (int) pair.second;
                MediaController.PutSongInQueueToPlayer();
                MediaController.Play();
            }
        }.execute();
    }

    public static void AddPlaylist(final Context context, final String name, final ArrayList<SongDisplay> songList) {
        new AsyncTask<Void, Void, ArrayList<Song>>() {
            @Override
            protected ArrayList<Song> doInBackground(Void... voids) {
                ArrayList<Song> tmpQueue = new ArrayList<>();
                for (SongDisplay songDisplay : songList) {
                    tmpQueue.add(songDatabase.songDAO().FindSongByPath(songDisplay.getPath()));
                }
                return tmpQueue;
            }

            @Override
            protected void onPostExecute(ArrayList<Song> list) {
                if (list.size() > 0) {
                    playlistList.add(new Playlist(name, list));
                    Collections.sort(playlistList, new ComparePlaylist());
                    SaveData(context);
                    Toast.makeText(context, "Playlist saved",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Playlist not created due to empty", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public static void GetSelectedList(final int viewOption, final ArrayList<SongDisplay> requestList, final Callback callback) {
        new AsyncTask<Void, Void, ArrayList<SongDisplay>>() {
            @Override
            protected ArrayList<SongDisplay> doInBackground(Void... voids) {
                ArrayList<SongDisplay> tmpQueue = new ArrayList<>();
                switch (viewOption) {
                    case TRACK:
                    case ARTIST_DETAIL:
                    case ALBUM_DETAIL:
                    case LOCATION_DETAIL:
                    case PLAYLIST_DETAIL: {
                        for (SongDisplay songDisplay : requestList) {
                            if (songDisplay.isSelected()) {
                                songDisplay.setNotSelected();
                                tmpQueue.add(songDisplay);
                            }
                        }
                        break;
                    }
                    case ARTIST_LIST:
                    case ALBUM_LIST:
                    case LOCATION_LIST:
                    case PLAYLIST_LIST:
                    case FAVORITE: {
                        for (SongDisplay songDisplayRequest : requestList) {
                            switch (viewOption) {
                                case ARTIST_LIST: {
                                    for (Song song : songDatabase.songDAO().FindSongByArtist(songDisplayRequest.getFirstLine()))
                                        if (indexInQueue >= 0 && song.getPath().equals(GetPathOfPlayingSong()))
                                            tmpQueue.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), true));
                                        else
                                            tmpQueue.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), false));
                                    break;
                                }
                                case ALBUM_LIST: {
                                    for (Song song : songDatabase.songDAO().FindSongByAlbum(songDisplayRequest.getFirstLine()))
                                        if (indexInQueue >= 0 && song.getPath().equals(GetPathOfPlayingSong()))
                                            tmpQueue.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), true));
                                        else
                                            tmpQueue.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), false));
                                    break;
                                }
                                case LOCATION_LIST: {
                                    for (Song song : songDatabase.songDAO().FindSongByLocation(songDisplayRequest.getFirstLine()))
                                        if (indexInQueue >= 0 && song.getPath().equals(GetPathOfPlayingSong()))
                                            tmpQueue.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), true));
                                        else
                                            tmpQueue.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), false));
                                    break;
                                }
                                case PLAYLIST_LIST: {
                                    for (Playlist playlist : playlistList) {
                                        if (playlist.GetPlaylistName().equals(songDisplayRequest.getFirstLine())) {
                                            for (Song song : playlist.GetSongList())
                                                if (indexInQueue >= 0 && song.getPath().equals(GetPathOfPlayingSong()))
                                                    tmpQueue.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), true));
                                                else
                                                    tmpQueue.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), false));
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
                return tmpQueue;
            }

            @Override
            protected void onPostExecute(ArrayList<SongDisplay> arrayList) {
                callback.ReturnList(arrayList);
            }
        }.execute();
    }

    public static void PlaySelectedList(int viewOption, ArrayList<SongDisplay> requestList) {
        GetSelectedList(viewOption, requestList, new Callback() {
            @Override
            public void ApplyChanges() {

            }

            @Override
            public void ReturnList(ArrayList<SongDisplay> list) {
                if (list.size() > 0) AddToNewQueueAndPlay(list, list.get(0).getPath());
            }
        });
    }

    public static ArrayList<SongDisplay> GetPlayingQueue() {
        ArrayList<SongDisplay> arrayList = new ArrayList<>();
        List<Song> tmpList = playingQueue;
        for (int i = 0; i < tmpList.size(); i++) {
            if (i == indexInQueue)
                arrayList.add(new SongDisplay(tmpList.get(i).getTitle(), tmpList.get(i).getArtist(), tmpList.get(i).getPath(), tmpList.get(i).isFavorite(), true));
            else
                arrayList.add(new SongDisplay(tmpList.get(i).getTitle(), tmpList.get(i).getArtist(), tmpList.get(i).getPath(), tmpList.get(i).isFavorite(), false));
        }
        return arrayList;
    }

    public static void AddToQueue(ArrayList<SongDisplay> arrayList) {
        final ArrayList<SongDisplay> songDisplayArrayList = arrayList;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                for (SongDisplay songDisplay : songDisplayArrayList) {
                    Song song = songDatabase.songDAO().FindSongByPath(songDisplay.getPath());
                    boolean existed = false;
                    for (int i = 0; i < playingQueue.size(); i++) {
                        Song queuedSong = playingQueue.get(i);
                        if (queuedSong.equals(song)) existed = true;
                    }
                    if (!existed) playingQueue.add(song);
                }
                if (!MediaController.HasSong()) {
                    indexInQueue = 0;
                    MediaController.PutSongInQueueToPlayer();
                    MediaController.Play();
                }
                return null;
            }
        }.execute();
    }

    public static void RemoveFromQueue(String path) {
        Song song = songDatabase.songDAO().FindSongByPath(path);
        int positionInQueue = -1;
        for (int i = 0, playingQueueSize = playingQueue.size(); i < playingQueueSize; i++) {
            Song songs = playingQueue.get(i);
            if (songs.getPath().equals(path)) positionInQueue = i;
        }
        boolean existed = false;
        for (int i = 0; i < playingQueue.size(); i++) {
            Song queuedSong = playingQueue.get(i);
            if (queuedSong.equals(song)) existed = true;
        }
        if (existed) {
            String playingSongPath = GetPathOfPlayingSong();
            boolean playingStatus = MediaController.IsPlaying();
            playingQueue.remove(song);
            if (path.equals(playingSongPath)) {
                MediaController.PutSongInQueueToPlayer();
            } else {
                if (positionInQueue >= 0 && positionInQueue < GetIndexInQueue())
                    SetIndexInQueue(GetIndexInQueue() - 1);
            }
            if (!playingStatus) MediaController.Pause();
        }
    }

    public static void ShufflePlayingList(boolean shuffle) {
        if (shuffle) {
            Song playingSong = playingQueue.get(indexInQueue);
            Collections.shuffle(playingQueue);
            Collections.swap(playingQueue, playingQueue.indexOf(playingSong), 0);
            indexInQueue = 0;
        } else {
            Song playingSong = playingQueue.get(indexInQueue);
            Collections.sort(playingQueue, new CompareSong());
            indexInQueue = playingQueue.indexOf(playingSong);
        }
    }

    public static String GetPathOfPlayingSong() {
        return (playingQueue.size() > 0) ? playingQueue.get(indexInQueue).getPath() : null;
    }

    public static boolean SetIndexInQueue(int index) {
        if (index < 0 || index >= playingQueue.size()) return false;
        indexInQueue = index;
        return true;
    }

    public static void SetIndexInQueue(String path) {
        for (int i = 0; i < playingQueue.size(); i++) {
            if (playingQueue.get(i).getPath().equals(path)) indexInQueue = i;
        }
    }

    public static int GetIndexInQueue() {
        return indexInQueue;
    }

    public static Song GetPlayingSong() {
        if (indexInQueue < 0 || indexInQueue >= playingQueue.size()) return null;
        return playingQueue.get(indexInQueue);
    }

    public static Song GetNextSong(boolean overlap) {
        int tmpIndex = indexInQueue + 1;
        if (tmpIndex >= playingQueue.size()) {
            if (overlap && playingQueue.size() > 0) return playingQueue.get(0);
            return null;
        }
        return playingQueue.get(tmpIndex);
    }

    public static int GetSizeOfQueue() {
        return playingQueue.size();
    }

    public static void RemovePlaylist(String name) {
        for (Playlist playlist : playlistList) {
            if (playlist.GetPlaylistName().equals(name)) {
                playlistList.remove(playlist);
            }
        }
    }

    public static ArrayList<SongDisplay> GetPlaylistSongDisplayList(String name) {
        for (Playlist playlist : playlistList) {
            if (playlist.GetPlaylistName().equals(name)) {
                ArrayList<SongDisplay> arrayList = new ArrayList<>();
                ArrayList<Song> songList = playlist.GetSongList();
                for (Song song : songList) {
                    if (indexInQueue >= 0 && song.getPath().equals(GetPathOfPlayingSong()))
                        arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), true));
                    else
                        arrayList.add(new SongDisplay(song.getTitle(), song.getArtist(), song.getPath(), song.isFavorite(), false));
                }
                return arrayList;
            }
        }
        return null;
    }

    public static boolean isPlaylistNameValid(String name) {
        for (Playlist playlist : playlistList) {
            if (playlist.GetPlaylistName().equals(name)) return false;
        }
        return true;
    }

    public static void DeleteSelectedList(final Context context, final int viewOption, final ArrayList<SongDisplay> requestList, final Callback callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                switch (viewOption) {
                    case TRACK:
                    case ARTIST_DETAIL:
                    case ALBUM_DETAIL:
                    case LOCATION_DETAIL:
                    case PLAYLIST_DETAIL:
                    case FAVORITE: {
                        for (SongDisplay songDisplay : requestList) {
                            if (songDisplay.isSelected()) {
                                new File(songDisplay.getPath()).delete();
                                DataRepository.DeleteSong(songDisplay.getPath());
                                final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                final Uri contentUri = Uri.fromFile(new File(songDisplay.getPath()));
                                scanIntent.setData(contentUri);
                                context.sendBroadcast(scanIntent);
                                RemoveFromQueue(songDisplay.getPath());
                            }
                        }
                        break;
                    }
                    case ARTIST_LIST:
                    case ALBUM_LIST:
                    case LOCATION_LIST:
                    case PLAYLIST_LIST: {
                        for (SongDisplay songDisplayRequest : requestList) {
                            switch (viewOption) {
                                case ARTIST_LIST: {
                                    for (Song song : songDatabase.songDAO().FindSongByArtist(songDisplayRequest.getFirstLine())) {
                                        new File(song.getPath()).delete();
                                        DataRepository.DeleteSong(song.getPath());
                                        final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        final Uri contentUri = Uri.fromFile(new File(song.getPath()));
                                        scanIntent.setData(contentUri);
                                        context.sendBroadcast(scanIntent);
                                        RemoveFromQueue(song.getPath());
                                    }

                                    break;
                                }
                                case ALBUM_LIST: {
                                    for (Song song : songDatabase.songDAO().FindSongByAlbum(songDisplayRequest.getFirstLine())) {
                                        new File(song.getPath()).delete();
                                        DataRepository.DeleteSong(song.getPath());
                                        final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        final Uri contentUri = Uri.fromFile(new File(song.getPath()));
                                        scanIntent.setData(contentUri);
                                        context.sendBroadcast(scanIntent);
                                        RemoveFromQueue(song.getPath());
                                    }
                                    break;
                                }
                                case LOCATION_LIST: {
                                    for (Song song : songDatabase.songDAO().FindSongByLocation(songDisplayRequest.getFirstLine())) {
                                        new File(song.getPath()).delete();
                                        DataRepository.DeleteSong(song.getPath());
                                        final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        final Uri contentUri = Uri.fromFile(new File(song.getPath()));
                                        scanIntent.setData(contentUri);
                                        context.sendBroadcast(scanIntent);
                                        RemoveFromQueue(song.getPath());
                                    }
                                    break;
                                }
                                case PLAYLIST_LIST: {
                                    for (Playlist playlist : playlistList) {
                                        if (playlist.GetPlaylistName().equals(songDisplayRequest.getFirstLine())) {
                                            playlistList.remove(playlist);
                                            SaveData(context);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                callback.ApplyChanges();
            }
        }.execute();
    }

    public static int GetCurrentThemeId() {
        switch (currentTheme.first) {
            case "Red":
                return currentTheme.second ? R.style.Red : R.style.RedL;
            case "Pink":
                return currentTheme.second ? R.style.Pink : R.style.PinkL;
            case "Purple":
                return currentTheme.second ? R.style.Purple : R.style.PurpleL;
            case "Deep Purple":
                return currentTheme.second ? R.style.DeepPurple : R.style.DeepPurpleL;
            case "Indigo":
                return currentTheme.second ? R.style.Indigo : R.style.IndigoL;
            case "Blue":
                return currentTheme.second ? R.style.Blue : R.style.BlueL;
            case "Light Blue":
                return currentTheme.second ? R.style.LightBlue : R.style.LightBlueL;
            case "Cyan":
                return currentTheme.second ? R.style.Cyan : R.style.CyanL;
            case "Teal":
                return currentTheme.second ? R.style.Teal : R.style.TealL;
            case "Green":
                return currentTheme.second ? R.style.Green : R.style.GreenL;
            case "Light Green":
                return currentTheme.second ? R.style.LightGreen : R.style.LightGreenL;
            case "Lime":
                return currentTheme.second ? R.style.Lime : R.style.LimeL;
            case "Yellow":
                return currentTheme.second ? R.style.Yellow : R.style.YellowL;
            case "Amber":
                return currentTheme.second ? R.style.Amber : R.style.AmberL;
            case "Orange":
                return currentTheme.second ? R.style.Orange : R.style.OrangeL;
            case "Deep Orange":
                return currentTheme.second ? R.style.DeepOrange : R.style.DeepOrangeL;
            case "Brown":
                return currentTheme.second ? R.style.Brown : R.style.BrownL;
            case "Grey":
                return currentTheme.second ? R.style.Grey : R.style.GreyL;
            case "Blue Grey":
                return currentTheme.second ? R.style.BlueGrey : R.style.BlueGreyL;
            default:
                return R.style.AppTheme;
        }
    }

    public static int GetThemeId(Theme theme) {
        if (theme == null) return GetCurrentThemeId();
        switch (theme.getColor()) {
            case "Red":
                return theme.isDarkTheme() ? R.style.Red : R.style.RedL;
            case "Pink":
                return theme.isDarkTheme() ? R.style.Pink : R.style.PinkL;
            case "Purple":
                return theme.isDarkTheme() ? R.style.Purple : R.style.PurpleL;
            case "Deep Purple":
                return theme.isDarkTheme() ? R.style.DeepPurple : R.style.DeepPurpleL;
            case "Indigo":
                return theme.isDarkTheme() ? R.style.Indigo : R.style.IndigoL;
            case "Blue":
                return theme.isDarkTheme() ? R.style.Blue : R.style.BlueL;
            case "Light Blue":
                return theme.isDarkTheme() ? R.style.LightBlue : R.style.LightBlueL;
            case "Cyan":
                return theme.isDarkTheme() ? R.style.Cyan : R.style.CyanL;
            case "Teal":
                return theme.isDarkTheme() ? R.style.Teal : R.style.TealL;
            case "Green":
                return theme.isDarkTheme() ? R.style.Green : R.style.GreenL;
            case "Light Green":
                return theme.isDarkTheme() ? R.style.LightGreen : R.style.LightGreenL;
            case "Lime":
                return theme.isDarkTheme() ? R.style.Lime : R.style.LimeL;
            case "Yellow":
                return theme.isDarkTheme() ? R.style.Yellow : R.style.YellowL;
            case "Amber":
                return theme.isDarkTheme() ? R.style.Amber : R.style.AmberL;
            case "Orange":
                return theme.isDarkTheme() ? R.style.Orange : R.style.OrangeL;
            case "Deep Orange":
                return theme.isDarkTheme() ? R.style.DeepOrange : R.style.DeepOrangeL;
            case "Brown":
                return theme.isDarkTheme() ? R.style.Brown : R.style.BrownL;
            case "Grey":
                return theme.isDarkTheme() ? R.style.Grey : R.style.GreyL;
            case "Blue Grey":
                return theme.isDarkTheme() ? R.style.BlueGrey : R.style.BlueGreyL;
            default:
                return GetCurrentThemeId();
        }
    }

    public static Pair<String, Boolean> GetCurrentTheme() {
        return currentTheme;
    }

    public static void SetNewTheme(Pair<String, Boolean> newTheme, Callback callback) {
        DataRepository.currentTheme = newTheme;
        callback.ApplyChanges();
    }

    public static void SaveData(Context context) {
        saveObject(context, currentTheme, "theme");
        saveObject(context, playlistList, "playlistList");
        saveObject(context, MediaController.GetLoopingStatus(), "loopingStatus");
        saveObject(context, MediaController.GetShuffleStatus(), "shuffleStatus");
        saveObject(context, pathOfFavoriteSong, "favoriteSong");
    }

    private static void LoadData(Context context) {
        Object tmpObject;
        tmpObject = loadObject(context, "v2.0", new TypeToken<Boolean>() {
                }.getType()
        );

        tmpObject = loadObject(context, "theme", new TypeToken<Pair<String, Boolean>>() {
                }.getType()
        );
        currentTheme = tmpObject != null ? (Pair<String, Boolean>) tmpObject : new Pair<>("Amber", false);


        tmpObject = loadObject(context, "playlistList", new TypeToken<ArrayList<Playlist>>() {
                }.getType()
        );
        playlistList = tmpObject != null ? (ArrayList<Playlist>) tmpObject : new ArrayList<Playlist>();

        tmpObject = loadObject(context, "loopingStatus", new TypeToken<Integer>() {
                }.getType()
        );
        MediaController.SetRepeatMode(tmpObject != null ? (Integer) tmpObject : 0);

        tmpObject = loadObject(context, "shuffleStatus", new TypeToken<Boolean>() {
                }.getType()
        );
        MediaController.SetShuffle(tmpObject != null ? (Boolean) tmpObject : false);

        tmpObject = loadObject(context, "favoriteSong", new TypeToken<ArrayList<String>>() {
                }.getType()
        );
        pathOfFavoriteSong = tmpObject != null ? (ArrayList<String>) tmpObject : null;
    }

    static void saveObject(Context context, Object object, String key) {
        SharedPreferences.Editor editor = context.getSharedPreferences(key, MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(object);
        editor.putString(key, json);
        editor.apply();
    }

    static Object loadObject(Context context, String key, Type type) {
        SharedPreferences prefs = context.getSharedPreferences(key, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        return gson.fromJson(json, type);
    }
}
