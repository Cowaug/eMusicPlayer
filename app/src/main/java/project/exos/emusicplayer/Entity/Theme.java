package project.exos.emusicplayer.Entity;

public class Theme {
    private String color;
    private String accentColor;
    private String tintColor;
    private boolean isDarkTheme;
    private boolean preview;

    public Theme(String color, String accentColor, String tintColor, boolean isDarkTheme) {
        this.color = color;
        this.accentColor = accentColor;
        this.tintColor = tintColor;
        this.isDarkTheme = isDarkTheme;
        this.preview = false;
    }

    public String getColor() {
        return color;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public String getTintColor() {
        return tintColor;
    }

    public boolean isDarkTheme() {
        return isDarkTheme;
    }

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        Theme theme = (Theme) obj;
        return accentColor.equals(theme.accentColor);
    }


}
