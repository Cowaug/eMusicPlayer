package project.exos.emusicplayer.GUI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import project.exos.emusicplayer.Entity.CompareTheme;
import project.exos.emusicplayer.Entity.DataRepository;
import project.exos.emusicplayer.R;
import project.exos.emusicplayer.Entity.SongDisplay;
import project.exos.emusicplayer.Entity.Theme;
import project.exos.emusicplayer.Adapter.ThemeAdapter;

public class GUI_SettingTheme extends AppCompatActivity {
    //region CREATE THEME ARRAY LIST
    String
            white = "#FFFFFF",
            black = "#000000";


    ArrayList<Theme> themeArrayList = new ArrayList<>();
    //endregion

    static int index, top;
    ListView list_Theme;
    ConstraintLayout solid_GUIThemeBackground;
    ImageButton button_ApplyTheme, button_ExitTheme;
    Theme currentPreviewTheme = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GetTheme();
        Collections.sort(themeArrayList, new CompareTheme());
        ApplyTheme();
    }

    public void OnButtonClick(View view) {
        switch (view.getId()) {
            case R.id.button_ApplyTheme: {
                if (currentPreviewTheme != null) {
                    DataRepository.SetNewTheme(new Pair<>(currentPreviewTheme.getColor(), currentPreviewTheme.isDarkTheme()), new DataRepository.Callback() {
                        @Override
                        public void ApplyChanges() {
                            GUI_Library.applyNewTheme = true;
                        }

                        @Override
                        public void ReturnList(ArrayList<SongDisplay> list) {

                        }
                    });
                    // ApplyTheme();
                    Toast.makeText(getApplicationContext(), "Theme applied",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
            case R.id.button_ExitTheme: {
                if (DataRepository.GetThemeId(currentPreviewTheme) != DataRepository.GetCurrentThemeId()) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setMessage("Apply selected theme?");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    OnButtonClick(button_ApplyTheme);
                                }
                            });

                    dialog.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });

                    AlertDialog alertDialog = dialog.create();
                    alertDialog.show();
                } else finish();
            }
        }
    }

    void GetTheme() {
        themeArrayList.add(new Theme("Red", "#F44336", black, true));
        themeArrayList.add(new Theme("Pink", "#E91E63", black, true));
        themeArrayList.add(new Theme("Purple", "#9C27B0", white, true));
        themeArrayList.add(new Theme("Deep Purple", "#673AB7", white, true));
        themeArrayList.add(new Theme("Indigo", "#3F51B5", white, true));
        themeArrayList.add(new Theme("Blue", "#2196f3", black, true));
        themeArrayList.add(new Theme("Light Blue", "#03A9F4", black, true));
        themeArrayList.add(new Theme("Cyan", "#00BCD4", black, true));
        themeArrayList.add(new Theme("Teal", "#009688", black, true));
        themeArrayList.add(new Theme("Green", "#4CAF50", black, true));
        themeArrayList.add(new Theme("Light Green", "#8BC34A", black, true));
        themeArrayList.add(new Theme("Lime", "#CDDC39", black, true));
        themeArrayList.add(new Theme("Yellow", "#FFEE58", black, true));
        themeArrayList.add(new Theme("Amber", "#FFC107", black, true));
        themeArrayList.add(new Theme("Orange", "#FF9800", black, true));
        themeArrayList.add(new Theme("Deep Orange", "#FF5722", black, true));
        themeArrayList.add(new Theme("Brown", "#795548", white, true));
        themeArrayList.add(new Theme("Grey", "#9E9E9E", black, true));
        themeArrayList.add(new Theme("Blue Grey", "#607D8B", black, true));

        themeArrayList.add(new Theme("Red", "#D32F2F", white, false));
        themeArrayList.add(new Theme("Pink", "#C2185B", white, false));
        themeArrayList.add(new Theme("Purple", "#7B1FA2", white, false));
        themeArrayList.add(new Theme("Deep Purple", "#512DA8", white, false));
        themeArrayList.add(new Theme("Indigo", "#303F9F", white, false));
        themeArrayList.add(new Theme("Blue", "#1976D2", white, false));
        themeArrayList.add(new Theme("Light Blue", "#0288D1", black, false));
        themeArrayList.add(new Theme("Cyan", "#0097A7", black, false));
        themeArrayList.add(new Theme("Teal", "#00796B", white, false));
        themeArrayList.add(new Theme("Green", "#388E3C", black, false));
        themeArrayList.add(new Theme("Light Green", "#689F38", black, false));
        themeArrayList.add(new Theme("Lime", "#AFB42B", black, false));
        themeArrayList.add(new Theme("Yellow", "#FBC02D", black, false));
        themeArrayList.add(new Theme("Amber", "#FFA000", black, false));
        themeArrayList.add(new Theme("Orange", "#F57C00", black, false));
        themeArrayList.add(new Theme("Deep Orange", "#E64A19", black, false));
        themeArrayList.add(new Theme("Brown", "#5D4037", white, false));
        themeArrayList.add(new Theme("Grey", "#616161", white, false));
        themeArrayList.add(new Theme("Blue Grey", "#455A64", white, false));
    }

    void ApplyTheme() {


        if (list_Theme != null) {
            index = list_Theme.getFirstVisiblePosition();
            View v = list_Theme.getChildAt(0);
            top = (v == null) ? 0 : (v.getTop() - list_Theme.getPaddingTop());
        } else index = top = 0;

        setTheme(DataRepository.GetThemeId(currentPreviewTheme));
        setContentView(R.layout.gui_setting_theme);

        //region update status bar color
        TypedArray typedArray = obtainStyledAttributes(DataRepository.GetThemeId(currentPreviewTheme), new int[]{android.R.attr.statusBarColor});
        int color = typedArray.getColor(0, 0);
        typedArray.recycle();

        Window window = this.getWindow();
        window.setStatusBarColor(color);
        window.setNavigationBarColor(color);
        View view = window.getDecorView();

        if (currentPreviewTheme != null) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (currentPreviewTheme.getTintColor().equals("#000000")) {
                    view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    //view.setSystemUiVisibility(0);

                    view.setSystemUiVisibility(view.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
            if (Build.VERSION.SDK_INT >= 27) {
                if (currentPreviewTheme.getTintColor().equals("#000000")) {
                    view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                } else {
                    view.setSystemUiVisibility(view.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                    //view.setSystemUiVisibility(0);
                }
            }
        }


        //endregion

        button_ExitTheme = findViewById(R.id.button_ExitTheme);
        button_ApplyTheme = findViewById(R.id.button_ApplyTheme);
        solid_GUIThemeBackground = findViewById(R.id.solid_GUIThemeBackground);

        if (currentPreviewTheme != null)
            if (currentPreviewTheme.isDarkTheme()) {
                solid_GUIThemeBackground.setBackgroundResource(R.color.darkThemeBackground);
            } else {
                solid_GUIThemeBackground.setBackgroundResource(R.color.white);
            }

        for (Theme theme : themeArrayList) {
            theme.setPreview(false);
            if (theme.equals(currentPreviewTheme)) theme.setPreview(true);
        }

        if (currentPreviewTheme == null || (currentPreviewTheme.getColor().equals(DataRepository.GetCurrentTheme().first) && currentPreviewTheme.isDarkTheme() == DataRepository.GetCurrentTheme().second)) {
            button_ApplyTheme.setEnabled(false);
            button_ApplyTheme.setAlpha(0.3f);
        } else {
            button_ApplyTheme.setEnabled(true);
            button_ApplyTheme.setAlpha(1f);
        }
        ThemeAdapter adapter = new ThemeAdapter(this, themeArrayList);
        list_Theme = findViewById(R.id.list_Theme);
        list_Theme.setAdapter(adapter);
        list_Theme.setSelectionFromTop(index, top);
        list_Theme.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentPreviewTheme = themeArrayList.get(position);
                ApplyTheme();
            }
        });
    }

    @Override
    public void onBackPressed() {
        OnButtonClick(button_ExitTheme);
    }
}
