package project.exos.emusicplayer.Entity;

import java.util.Comparator;

public class CompareTheme implements Comparator<Theme> {
    public int compare(Theme firstTheme,Theme secondTheme){
        return firstTheme.getColor().compareTo(secondTheme.getColor());
    }
}
