package project.exos.emusicplayer.Entity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.beaglebuddy.mp3.MP3;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;

public class MP3Info {
    private Mp3File mp3File = null;
    private com.beaglebuddy.mp3.MP3 mp3 = null;
    private String songPath;
    private boolean fileValid = false;

    public MP3Info(String songPath) {
        fileValid = false;
        if (new File(songPath).length() < 20 * 1024 * 1024)
            try {
                mp3File = new Mp3File(songPath);
                fileValid = true;
            } catch (Exception ignored) {
            }
        this.songPath = songPath;
    }

    public String GetTitle() {
        if (fileValid) {
            if (mp3File.hasId3v2Tag()) {
                String title = mp3File.getId3v2Tag().getTitle();
                if (title != null && !title.equals("")) return title;
            } else if (mp3File.hasId3v1Tag()) {
                String title = mp3File.getId3v1Tag().getTitle();
                if (title != null && !title.equals("")) return title;
            }
        }
        return new File(songPath).getName();
    }

    public String GetArtist() {
        if (fileValid) {
            if (mp3File.hasId3v2Tag()) {
                String artist = mp3File.getId3v2Tag().getArtist();
                if (artist != null && !artist.equals("")) return artist;
            } else if (mp3File.hasId3v1Tag()) {
                String artist = mp3File.getId3v1Tag().getArtist();
                if (artist != null && !artist.equals("")) return artist;
            }

        }
        return "<Unknown>";
    }

    public String GetAlbum() {
        if (fileValid) {
            if (mp3File.hasId3v2Tag()) {
                String album = mp3File.getId3v2Tag().getArtist();
                if (album != null && !album.equals("")) return album;
            } else if (mp3File.hasId3v1Tag()) {
                String album = mp3File.getId3v1Tag().getAlbum();
                if (album != null && !album.equals("")) return album;
            }

        }
        return "<Unknown>";
    }

    public String GetLyric() {
        if (fileValid) {
            if (mp3File.hasId3v2Tag()) {
                String lyric = mp3File.getId3v2Tag().getLyrics();
                if (lyric != null && !lyric.equals(" ") && !lyric.equals("")) {
                    return lyric.replace("\t", "\n").replace("\r", "\n");
                }
            }
        }
        return null;
    }

    public Bitmap GetBitmap() {
        if (fileValid) {
            if (mp3File.hasId3v2Tag()) {
                byte[] rawImage = mp3File.getId3v2Tag().getAlbumImage();
                if (rawImage != null && rawImage.length != 0) {
                    return BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
                }
            }
        }
        return null;
    }

    public int GetRating() {
        if (fileValid) {
            if (mp3File.hasId3v2Tag()) return mp3File.getId3v2Tag().getWmpRating();
        }
        return -1;
    }

    public boolean SetTitle(String newTitle) {
        if (fileValid) {
            if (mp3File.hasId3v2Tag()) mp3File.getId3v2Tag().setTitle(newTitle);
            else if (mp3File.hasId3v1Tag()) mp3File.getId3v1Tag().setTitle(newTitle);
            SaveOverwrite();
            return true;
        }
        return false;
    }

    public boolean SetArtist(String newArtist) {
        if (fileValid) {
            if (mp3File.hasId3v2Tag()) mp3File.getId3v2Tag().setArtist(newArtist);
            else if (mp3File.hasId3v1Tag()) mp3File.getId3v1Tag().setArtist(newArtist);
            SaveOverwrite();
            return true;
        }
        return false;
    }

    public boolean SetAlbum(String newAlbum) {
        if (fileValid) {
            if (mp3File.hasId3v2Tag()) mp3File.getId3v2Tag().setAlbum(newAlbum);
            else if (mp3File.hasId3v1Tag()) mp3File.getId3v1Tag().setAlbum(newAlbum);
            SaveOverwrite();
            return true;
        }
        return false;
    }

    public boolean SetLyric(String newLyric) {
        if (fileValid) {
            ConvertId3v1ToId3v2();
            if (mp3File.hasId3v2Tag()) {
                mp3File.getId3v2Tag().setLyrics(!newLyric.equals("") ? newLyric : " ");
            }
            SaveOverwrite();
            return newLyric.equals(GetLyric());
        }
        return false;
    }

    public boolean SetBitmap(byte[] rawImage, String imagePath) {
        if (fileValid) {
            String mimeType = "";
            if (imagePath.endsWith(".png")) mimeType = "image/png";
            else if (imagePath.endsWith(".jpg")) mimeType = "image/jpg";
            else if (imagePath.endsWith(".gif")) mimeType = "image/gif";
            else if (imagePath.endsWith(".bmp")) mimeType = "image/bmp";
            else if (imagePath.endsWith(".jpeg")) mimeType = "image/jpeg";
            if (mp3File.hasId3v2Tag()) mp3File.getId3v2Tag().setAlbumImage(rawImage, mimeType);
            else if (mp3File.hasId3v1Tag()) {
                if (ConvertId3v1ToId3v2())
                    mp3File.getId3v2Tag().setAlbumImage(rawImage, mimeType);
                else return false;
            }
            SaveOverwrite();
            return true;
        }
        return false;
    }

    public boolean SetRating(int rating) {
        if (fileValid) {
            if (mp3File.hasId3v2Tag()) mp3File.getId3v2Tag().setWmpRating(rating);
            else if (mp3File.hasId3v1Tag()) {
                if (ConvertId3v1ToId3v2())
                    mp3File.getId3v2Tag().setWmpRating(rating);
                else return false;
            }
            SaveOverwrite();
            return true;
        }
        return false;
    }

    public boolean IsFavorite() {
        if (fileValid) {
            if (mp3File.hasId3v2Tag()) return mp3File.getId3v2Tag().getWmpRating() == 5;
        }
        return false;
    }

    public boolean ToggleFavorite() {
        if (fileValid) {
            if (IsFavorite()) SetRating(0);
            else SetRating(5);
        }
        return IsFavorite();
    }

    public boolean ConvertId3v1ToId3v2() {
        if (fileValid) {
            if (mp3File.hasId3v1Tag()) {
                ID3v2 id3v2 = new ID3v24Tag();
                ID3v1 id3v1 = mp3File.getId3v1Tag();
                mp3File.setId3v2Tag(id3v2);
                id3v2.setTitle(id3v1.getTitle());
                id3v2.setArtist(id3v1.getArtist());
                id3v2.setAlbum(id3v1.getAlbum());
                id3v2.setComment(id3v1.getComment());
                id3v2.setGenre(id3v1.getGenre());
                id3v2.setGenreDescription(id3v1.getGenreDescription());
                id3v2.setTrack(id3v1.getTrack());
                id3v2.setYear(id3v1.getYear());
                id3v2.setLyrics(" ");
                SaveOverwrite();
                return true;
            }
            return mp3File.hasId3v2Tag();
        }
        return false;
    }

    public boolean IsEditable() {
        return fileValid;
    }

    private void SaveOverwrite() {
        if (fileValid)
            try {
                mp3File.save(songPath + ".tmp");
                new File(songPath + ".tmp").renameTo(new File(songPath));
            } catch (Exception ignored) {
            }
    }
}
