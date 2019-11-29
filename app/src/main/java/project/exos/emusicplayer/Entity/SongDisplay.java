package project.exos.emusicplayer.Entity;

public class SongDisplay {
    private String firstLine;
    private String secondLine;
    private String path;
    private boolean isFavorite;
    private boolean isPlaying;
    private boolean isSelected;

    public SongDisplay(String firstLine, String secondLine, String path, boolean love, boolean playing) {
        this.firstLine = firstLine;
        this.secondLine = secondLine;
        this.path = path;
        this.isFavorite = love;
        this.isPlaying = playing;
        this.isSelected = false;
    }

    public SongDisplay(String firstLine, String secondLine, String path, boolean love, boolean playing, boolean selected) {
        this.firstLine = firstLine;
        this.secondLine = secondLine;
        this.path = path;
        this.isFavorite = love;
        this.isPlaying = playing;
        this.isSelected = selected;
    }

    public String getFirstLine() {
        return this.firstLine;
    }

    public String getSecondLine() {
        return this.secondLine;
    }

    public String getPath() {
        return path;
    }

    public boolean isFavorite() {
        return this.isFavorite;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public boolean toggleSelected() {
        this.isSelected = !this.isSelected;
        return this.isSelected;
    }

    public void setNotSelected() {
        this.isSelected = false;
    }

    public void setSelected() {
        this.isSelected = true;
    }

    public void setPlaying() {
        this.isPlaying = true;
    }

    public void setNotPlaying() {
        this.isPlaying = false;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        SongDisplay songDisplay = (SongDisplay) obj;
        return getFirstLine().equals(songDisplay.getFirstLine()) &&
                getSecondLine().equals(songDisplay.getSecondLine());
    }
}
