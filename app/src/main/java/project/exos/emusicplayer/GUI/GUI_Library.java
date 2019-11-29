package project.exos.emusicplayer.GUI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import project.exos.emusicplayer.Adapter.SongDisplayAdapter;
import project.exos.emusicplayer.Adapter.SpinnerAdapter;
import project.exos.emusicplayer.Entity.CompareSongDisplay;
import project.exos.emusicplayer.Entity.DataRepository;
import project.exos.emusicplayer.Entity.MediaController;
import project.exos.emusicplayer.Entity.Song;
import project.exos.emusicplayer.Entity.SongDisplay;
import project.exos.emusicplayer.R;

public class GUI_Library extends AppCompatActivity {

    //region constant variable + local variable
    static ArrayList<SongDisplay> libraryList = new ArrayList<>();
    ArrayList<SongDisplay> selectedList = new ArrayList<>();
    Handler handler;

    static int index, top; // note: using for saving list position

    static private String selectedItemName; // note: get selected items of list view

    static boolean isMultiSelect; // note: detect if multiple songs were selected
    static boolean needListUpdate = false;
    static boolean keepListPosition = true;
    static boolean applyNewTheme = false;
    static boolean needIconUpdate = false;
    static boolean effect = false;
    static boolean newSong = true;
    AlertDialog alertDialog;
    AlertDialog.Builder dialog;

    static int viewOption = 0; // note: using constant value below
    public static final int TRACK = 0,
            ARTIST_LIST = 1, ARTIST_DETAIL = 6,
            ALBUM_LIST = 2, ALBUM_DETAIL = 7,
            LOCATION_LIST = 3, LOCATION_DETAIL = 8,
            PLAYLIST_LIST = 4, PLAYLIST_DETAIL = 9,
            FAVORITE = 5;

    static int fabMode = 0;
    static final int SHUFFLE = 0, SYNC = 1, ADD = 2, EDIT = 3, PLAY = 4, HIDDEN = 5;
    //endregion

    //region GUI variable
    ConstraintLayout layout_Menu;

    TextView text_LibraryStatus,
            text_SongTitle_2,
            text_SongArtist_2,
            text_Details;
    ImageButton
            button_PreviousSong_2,
            button_NextSong_2,
            button_Play_Pause_2,
            button_Search,
            button_Setting;
    FloatingActionButton fab_FAB;
    Spinner spinner_ViewOption;
    ListView list_Library;
    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region get and check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        while (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ;
        //endregion
        new DataRepository(getApplicationContext());
        new MediaController(this);
        //region apply theme and set view
        setTheme(DataRepository.GetCurrentThemeId());
        setContentView(R.layout.gui_library);
        //endregion
        if (DataRepository.scanMedia) {
            //new DataRepository.ScanSong(null).execute();
            DataRepository.ScanSong(this);

            dialog = new AlertDialog.Builder(GUI_Library.this);
            dialog.setMessage("Scanning media on your device...");
            dialog.setCancelable(false);
            alertDialog = dialog.create();
            alertDialog.show();
        }

        //region initial GUI variable
        handler = new Handler();
        layout_Menu = findViewById(R.id.layout_Menu);
        text_LibraryStatus = findViewById(R.id.text_LibraryStatus);
        text_Details = findViewById(R.id.text_Details);
        text_SongArtist_2 = findViewById(R.id.text_SongArtist_2);
        text_SongTitle_2 = findViewById(R.id.text_SongTitle_2);
        button_NextSong_2 = findViewById(R.id.button_NextSong_2);
        button_Play_Pause_2 = findViewById(R.id.button_Play_Pause_2);
        button_PreviousSong_2 = findViewById(R.id.button_PreviousSong_2);
        button_Search = findViewById(R.id.button_Search);
        button_Setting = findViewById(R.id.button_Setting);
        fab_FAB = findViewById(R.id.fab_FAB);
        spinner_ViewOption = findViewById(R.id.spinner_ViewOption);
        list_Library = findViewById(R.id.list_Library);
        list_Library.setBackgroundResource(
                (DataRepository.GetCurrentTheme().second) ?
                        R.color.darkThemeBackground :
                        R.color.white

        );
        //endregion

        //region initial spinner
        ArrayList<String> choices = new ArrayList<>();
        choices.add("Tracks");
        choices.add("Artists");
        choices.add("Albums");
        choices.add("Locations");
        choices.add("Playlist");
        choices.add("Favorite");

        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, choices);
        spinner_ViewOption.setAdapter(spinnerAdapter);
        spinner_ViewOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == TRACK || position == FAVORITE) {
                    fabMode = SHUFFLE;
                } else if (position == PLAYLIST_LIST) {
                    fabMode = ADD;
                } else {
                    fabMode = HIDDEN;
                }

                if (viewOption != position) {
                    viewOption = position;
                    GetAsyncGUIData(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Animatable animation = (Animatable) fab_FAB.getDrawable();
                animation.start();
            }
        });
        //endregion

        //region Initial marquee textview
        text_SongTitle_2.setSelected(true);
        text_SongTitle_2.post(new Runnable() {
            @Override
            public void run() {
                text_SongTitle_2.setLayoutParams(new ConstraintLayout.LayoutParams(text_SongTitle_2.getWidth(), text_SongTitle_2.getHeight()));
            }
        });
        text_SongArtist_2.setSelected(true);
        text_SongArtist_2.post(new Runnable() {
            @Override
            public void run() {
                text_SongArtist_2.setLayoutParams(new ConstraintLayout.LayoutParams(text_SongArtist_2.getWidth(), text_SongArtist_2.getHeight()));
            }
        });
        text_Details.setSelected(true);
        text_Details.post(new Runnable() {
            @Override
            public void run() {
                text_Details.setLayoutParams(new ConstraintLayout.LayoutParams(text_Details.getWidth(), text_Details.getHeight()));
            }
        });
        //endregion

        //region Display library first time
        if (MediaController.IsPlaying())
            button_Play_Pause_2.setImageResource(R.drawable.ic_pause_black_24dp);
        else
            button_Play_Pause_2.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        Timer();
        //endregion

    }

    void Timer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!DataRepository.scanMedia) {
                    if (alertDialog != null) alertDialog.dismiss();
                }
                if (DataRepository.scanMedia && alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.setMessage("Scanning media on your device...\n\t" + DataRepository.progress);
                }
                UpdateGUI();
                Timer();
            }
        }, 100);
    }

    void UpdateGUI() {
        Song song = DataRepository.GetPlayingSong();
        if (song != null && newSong) {
            newSong = false;
            text_SongTitle_2.setText(song.getTitle());
            text_SongTitle_2.setSelected(false);
            text_SongTitle_2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    text_SongTitle_2.setSelected(true);
                }
            }, 1000);
            text_SongArtist_2.setText(song.getArtist());
            text_SongArtist_2.setSelected(false);
            text_SongArtist_2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    text_SongArtist_2.setSelected(true);
                }
            }, 1000);

            for (SongDisplay songDisplay : libraryList) {
                songDisplay.setNotPlaying();
                if (song.getPath().equals(songDisplay.getPath()))
                    songDisplay.setPlaying();
            }
            needListUpdate = keepListPosition = true;

        } else if (song == null && !newSong) {
            text_SongTitle_2.setText("No song selected");
            text_SongArtist_2.setText("Select a song to play");
        }
        if (needIconUpdate) {
            needIconUpdate = false;
            if (MediaController.IsPlaying())
                button_Play_Pause_2.setImageResource(R.drawable.avd_pause_to_play);
            else
                button_Play_Pause_2.setImageResource(R.drawable.avd_play_to_pause);
            Animatable animation = (Animatable) button_Play_Pause_2.getDrawable();
            animation.start();
        }
        if (needListUpdate) {
            needListUpdate = false;
            //region text update
            if (!isMultiSelect) {
                switch (viewOption) {
                    case TRACK: {
                        text_Details.setText(libraryList.size() + " song(s)");
                        break;
                    }
                    case ARTIST_DETAIL: {
                        text_Details.setText(selectedItemName + " - " + libraryList.size() + " song(s)");
                        break;
                    }
                    case ALBUM_DETAIL: {
                        text_Details.setText(selectedItemName + " - " + libraryList.size() + " song(s)");
                        break;
                    }
                    case LOCATION_DETAIL: {
                        text_Details.setText(selectedItemName + " - " + libraryList.size() + " song(s)");
                        break;
                    }
                    case PLAYLIST_DETAIL: {
                        text_Details.setText(selectedItemName + " - " + libraryList.size() + " song(s)");
                        break;
                    }
                    case FAVORITE: {
                        text_Details.setText(libraryList.size() + " song(s)");
                        break;
                    }
                    case ARTIST_LIST: {
                        text_Details.setText(libraryList.size() + " artist(s)");
                        break;
                    }
                    case ALBUM_LIST: {
                        text_Details.setText(libraryList.size() + " album(s)");
                        break;
                    }
                    case LOCATION_LIST: {
                        text_Details.setText(libraryList.size() + " folder(s)");
                        break;
                    }
                    case PLAYLIST_LIST: {
                        text_Details.setText(libraryList.size() + " playlist(s)");
                        break;
                    }

                }
                text_Details.setSelected(false);
                text_Details.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        text_Details.setSelected(true);
                    }
                }, 1000);
            }
            //endregion

            //region fab button update
            switch (fabMode) {
                case SHUFFLE: {
                    fab_FAB.setImageResource(R.drawable.ic_shuffle_black_24dp);
                    fab_FAB.setEnabled(true);
                    fab_FAB.setAlpha(1f);
                    break;
                }
                case ADD: {
                    fab_FAB.setImageResource(R.drawable.ic_add_black_24dp);
                    fab_FAB.setEnabled(true);
                    fab_FAB.setAlpha(1f);
                    break;
                }
                case EDIT: {
                    fab_FAB.setImageResource(R.drawable.ic_create_black_24dp);
                    fab_FAB.setEnabled(true);
                    fab_FAB.setAlpha(1f);
                    break;
                }
                case PLAY: {
                    fab_FAB.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    fab_FAB.setEnabled(true);
                    fab_FAB.setAlpha(1f);
                    break;
                }
                case HIDDEN: {
                    fab_FAB.setEnabled(false);
                    fab_FAB.setAlpha(0f);
                    break;
                }
            }
            //endregion

            //region list update
            if (libraryList.size() == 0) {
                if (effect) {
                    effect = false;
                    Animation mLoadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
                    mLoadAnimation.setDuration(150);
                    list_Library.startAnimation(mLoadAnimation);
                }
                list_Library.setVisibility(View.INVISIBLE);
                text_LibraryStatus.setVisibility(View.VISIBLE);
                text_LibraryStatus.setText("Nothing to show here :(");
            } else {
                if (effect) {
                    effect = false;
                    Animation mLoadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
                    mLoadAnimation.setDuration(750);
                    list_Library.startAnimation(mLoadAnimation);
                }
                list_Library.setVisibility(View.VISIBLE);
                text_LibraryStatus.setVisibility(View.INVISIBLE);
            }

            SongDisplayAdapter adapter = new SongDisplayAdapter(this, libraryList);

            if (keepListPosition) {
                index = list_Library.getFirstVisiblePosition();
                View v = list_Library.getChildAt(0);
                top = (v == null) ? 0 : (v.getTop() - list_Library.getPaddingTop());
                list_Library.setAdapter(adapter);
                list_Library.setSelectionFromTop(index, top);
            } else list_Library.setAdapter(adapter);

            list_Library.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (!isMultiSelect) {
                        switch (viewOption) {
                            case TRACK:
                            case ARTIST_DETAIL:
                            case ALBUM_DETAIL:
                            case LOCATION_DETAIL:
                            case PLAYLIST_DETAIL:
                            case FAVORITE: {
                                DataRepository.AddToNewQueueAndPlay(libraryList, libraryList.get(i).getPath());
                                libraryList.get(i).setPlaying();
                                text_SongTitle_2.setText(libraryList.get(i).getFirstLine());
                                text_SongTitle_2.setSelected(false);
                                text_SongTitle_2.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        text_SongTitle_2.setSelected(true);
                                    }
                                }, 1000);
                                text_SongArtist_2.setText(libraryList.get(i).getSecondLine());
                                text_SongArtist_2.setSelected(false);
                                text_SongArtist_2.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        text_SongArtist_2.setSelected(true);
                                    }
                                }, 1000);

                                if (!MediaController.IsPlaying()) {
                                    button_Play_Pause_2.setImageResource(R.drawable.avd_pause_to_play);
                                    Animatable animation = (Animatable) button_Play_Pause_2.getDrawable();
                                    animation.start();
                                }
                                break;
                            }
                            case ARTIST_LIST:
                            case ALBUM_LIST:
                            case LOCATION_LIST:
                            case PLAYLIST_LIST: {
                                // TODO: display detail of selected list
                                selectedItemName = libraryList.get(i).getFirstLine();
                                viewOption = viewOption + 5; // note: because of initial value above: ALBUM_DETAIL- ALBUM_LIST = 5
                                if (viewOption == PLAYLIST_DETAIL)
                                    fabMode = EDIT;
                                else fabMode = SHUFFLE;
                                GetAsyncGUIData(false);
                                break;
                            }
                        }
                    } else {
                        boolean isNew = libraryList.get(i).toggleSelected();
                        if (isNew) selectedList.add(libraryList.get(i));
                        else selectedList.remove(libraryList.get(i));
                        text_Details.setText(selectedList.size() + " item(s) selected");
                        keepListPosition = needListUpdate = true;

                        if (selectedList.size() == 0) {
                            ExitMultiSelectMode();
                        }

                    }
                }
            });
            list_Library.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // TODO: update list view to show selected item
                    // TODO: show option menu

                    libraryList.get(i).setSelected();
                    selectedList.add(libraryList.get(i));

                    text_Details.setText(selectedList.size() + " item(s) selected");
                    layout_Menu.setVisibility(View.VISIBLE);
                    spinner_ViewOption.setEnabled(false);
                    spinner_ViewOption.setAlpha(0.3f);
                    keepListPosition = needListUpdate = isMultiSelect = true;
                    fabMode = PLAY;
                    return true;
                }
            });
        }
        //endregion
    }

    void GetAsyncGUIData(final boolean keepListPosition) {
        //text_LibraryStatus.setText("Scanning media...");
        //list_Library.setVisibility(View.INVISIBLE);
        DataRepository.GetLibrary(viewOption, selectedItemName, new DataRepository.Callback() {
            @Override
            public void ApplyChanges() {

            }

            @Override
            public void ReturnList(ArrayList<SongDisplay> list) {
                GUI_Library.libraryList = list;
                needListUpdate = true;
                GUI_Library.keepListPosition = keepListPosition;
                effect = true;
            }
        });
    }

    public void OnButtonClick(final View view) {
        switch (view.getId()) {
            // region menu button
            case R.id.button_AddToQueue: {
                DataRepository.GetSelectedList(viewOption, selectedList, new DataRepository.Callback() {
                    @Override
                    public void ApplyChanges() {

                    }

                    @Override
                    public void ReturnList(ArrayList<SongDisplay> list) {
                        DataRepository.AddToQueue(list);
                    }
                });
                startActivity(new Intent(this, GUI_MediaPlayer.class));
                break;
            }

            case R.id.button_Add: {
                Collections.sort(selectedList, new CompareSongDisplay());
                DataRepository.GetSelectedList(viewOption, selectedList, new DataRepository.Callback() {
                    @Override
                    public void ApplyChanges() {

                    }

                    @Override
                    public void ReturnList(ArrayList<SongDisplay> list) {
                        GUI_Playlist.playlistSongList = list;
                        GUI_Playlist.GetAsyncGUIData();
                    }
                });
                Intent intent = new Intent(this, GUI_Playlist.class);
                startActivity(intent);
                break;
            }

            case R.id.button_Delete: {
                if (viewOption == PLAYLIST_DETAIL) {
                    Toast.makeText(getApplicationContext(), "Use edit instead", Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                if (viewOption != PLAYLIST_LIST)
                    dialog.setMessage("Permanently delete these songs?");
                else
                    dialog.setMessage("Permanently delete these playlist?");
                dialog.setCancelable(true);
                dialog.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DataRepository.DeleteSelectedList(GUI_Library.this, viewOption, selectedList, new DataRepository.Callback() {
                                    @Override
                                    public void ApplyChanges() {
                                        if (viewOption != PLAYLIST_LIST)
                                            Toast.makeText(getApplicationContext(), selectedList.size() + " song(s) deleted",
                                                    Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(getApplicationContext(), selectedList.size() + " playlist(s) deleted",
                                                    Toast.LENGTH_SHORT).show();
                                        ExitMultiSelectMode();
                                        DataRepository.GetLibrary(viewOption, selectedItemName, new DataRepository.Callback() {
                                            @Override
                                            public void ApplyChanges() {

                                            }

                                            @Override
                                            public void ReturnList(ArrayList<SongDisplay> list) {
                                                GUI_Library.libraryList = list;
                                                needListUpdate = needIconUpdate = true;
                                                if (DataRepository.GetPlayingSong() == null) {
                                                    text_SongTitle_2.setText("No song selected");
                                                    text_SongArtist_2.setText("Select a song to play");
                                                }
                                            }
                                        });
                                        GUI_Library.keepListPosition = true;
                                    }

                                    @Override
                                    public void ReturnList(ArrayList<SongDisplay> list) {

                                    }
                                });
                            }
                        });

                dialog.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = dialog.create();
                alertDialog.show();
                break;
            }

            case R.id.button_Info: {
                if (selectedList.size() == 1 && (viewOption == TRACK || viewOption > 5) && new File(selectedList.get(0).getPath()).length() < 20 * 1024 * 1024) {
                    GUI_SongDetails.CallbackSongPath(selectedList, GUI_SongDetails.VIEW);
                    Intent intent = new Intent(this, GUI_SongDetails.class);
                    startActivity(intent);
                } else if (selectedList.size() == 1 && viewOption == PLAYLIST_LIST) {
                    Intent intent = new Intent(this, GUI_Playlist.class);
                    GUI_Playlist.CallbackEdit(selectedList.get(0).getFirstLine());
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Info not available",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }

            //endregion

            case R.id.button_Setting: {
                // TODO: add animation
                /*Intent intent=new Intent(this,ActivitySettingMenu.class);
                Pair<View,String> setting = new Pair<>((View)settingButton,settingButton.getTransitionName());
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,setting);
                startActivity(intent,optionsCompat.toBundle());*/

                Intent intent = new Intent(this, GUI_SettingTheme.class);
                startActivity(intent);
                break;
            }

            case R.id.button_Search: {
                Intent intent = new Intent(this, GUI_Search.class);
                startActivity(intent);
                break;
            }

            case R.id.button_NextSong_2: {
                if (!MediaController.NextSong())
                    Toast.makeText(getApplicationContext(), "Last of queue!",
                            Toast.LENGTH_SHORT).show();
                Animatable animation = (Animatable) button_NextSong_2.getDrawable();
                animation.start();
                break;
            }

            case R.id.button_PreviousSong_2: {
                if (!MediaController.PreviousSong())
                    Toast.makeText(getApplicationContext(), "First of queue!",
                            Toast.LENGTH_SHORT).show();
                Animatable animation = (Animatable) button_PreviousSong_2.getDrawable();
                animation.start();
                break;
            }

            case R.id.button_Play_Pause_2: {
                if (!MediaController.HasSong()) break;
                if (MediaController.IsPlaying()) {
                    MediaController.Pause();
                    button_Play_Pause_2.setImageResource(R.drawable.avd_play_to_pause);
                    Animatable animation = (Animatable) button_Play_Pause_2.getDrawable();
                    animation.start();
                } else {
                    MediaController.Play();
                    button_Play_Pause_2.setImageResource(R.drawable.avd_pause_to_play);
                    Animatable animation = (Animatable) button_Play_Pause_2.getDrawable();
                    animation.start();
                }
                break;
            }

            case R.id.button_OpenMediaPlayer: {
                startActivity(new Intent(this, GUI_MediaPlayer.class));
                break;
            }

            case R.id.fab_FAB: {
                switch (fabMode) {
                    case SHUFFLE: {
                        if (libraryList.size() > 0) {
                            MediaController.SetShuffle(true);
                            int i = new Random().nextInt(libraryList.size());
                            DataRepository.AddToNewQueueAndPlay(libraryList, libraryList.get(new Random().nextInt(libraryList.size())).getPath());
                            if (!MediaController.IsPlaying()) {
                                button_Play_Pause_2.setImageResource(R.drawable.avd_pause_to_play);
                                Animatable animation = (Animatable) button_Play_Pause_2.getDrawable();
                                animation.start();
                            }
                        }
                        break;
                    }
                    case ADD: {
                        Intent intent = new Intent(this, GUI_Playlist.class);
                        startActivity(intent);
                        break;
                    }
                    case EDIT: {
                        Intent intent = new Intent(this, GUI_Playlist.class);
                        GUI_Playlist.CallbackEdit(selectedItemName);
                        startActivity(intent);
                        viewOption -= 5;
                        break;
                    }
                    case PLAY: {
                        DataRepository.PlaySelectedList(viewOption, selectedList);
                        if (!MediaController.IsPlaying()) {
                            button_Play_Pause_2.setImageResource(R.drawable.avd_pause_to_play);
                            Animatable animation = (Animatable) button_Play_Pause_2.getDrawable();
                            animation.start();
                        }
                        startActivity(new Intent(this, GUI_MediaPlayer.class));
                        break;
                    }
                    case HIDDEN: {
                        break;
                    }
                }
                break;
            }
        }

    }

    void reloadGUI() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    void ExitMultiSelectMode() {
        isMultiSelect = false;
        layout_Menu.setVisibility(View.INVISIBLE);
        for (SongDisplay songDisplay : libraryList) {
            songDisplay.setNotSelected();
        }
        selectedList.clear();
        spinner_ViewOption.setEnabled(true);
        spinner_ViewOption.setAlpha(1f);
        switch (viewOption) {
            case TRACK:
            case ARTIST_DETAIL:
            case ALBUM_DETAIL:
            case LOCATION_DETAIL:
            case FAVORITE:
                fabMode = SHUFFLE;
                break;
            case PLAYLIST_LIST:
                fabMode = ADD;
                break;
            case PLAYLIST_DETAIL:
                fabMode = EDIT;
                break;
            default:
                fabMode = HIDDEN;
                break;
        }
        keepListPosition = needListUpdate = true;
    }

    public static void CallbackNewSong() {
        newSong = true;
    }

    public static void CallbackIconUpdate() {
        needIconUpdate = true;
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        if (isMultiSelect) {
            ExitMultiSelectMode();
            return;
        }
        // TODO: exit multi select
        if (viewOption > 5) {
            spinner_ViewOption.setSelection(viewOption - 5, true);
            viewOption -= 5;
            if (viewOption == PLAYLIST_LIST)
                fabMode = ADD;
            else fabMode = HIDDEN;
            GetAsyncGUIData(false);
            return;
        }
        if (viewOption != 0) {
            spinner_ViewOption.setSelection(0, true);
            GetAsyncGUIData(false);
            return;
        }
        //supportFinishAfterTransition();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        needIconUpdate = newSong = true;
        if (applyNewTheme) {
            applyNewTheme = false;
            reloadGUI();
        } else {
            GetAsyncGUIData(true);
        }
        ExitMultiSelectMode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataRepository.SaveData(this);
        handler.removeCallbacksAndMessages(null);

    }
}
