package project.exos.emusicplayer.GUI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import project.exos.emusicplayer.Adapter.SongDisplayAdapter;
import project.exos.emusicplayer.Entity.CompareSongDisplay;
import project.exos.emusicplayer.Entity.DataRepository;
import project.exos.emusicplayer.Entity.Song;
import project.exos.emusicplayer.Entity.SongDisplay;
import project.exos.emusicplayer.R;

public class GUI_Playlist extends AppCompatActivity {
    static String playlistName;
    static ArrayList<SongDisplay> playlistSongList = new ArrayList<>();
    static ArrayList<SongDisplay> allSongList = new ArrayList<>();
    static Handler handler = new Handler();
    static boolean needListUpdate = false;
    static boolean keepListPosition = false;
    static boolean editPlaylist = false;

    static int index, top; // note: using for saving list position
    Boolean showSelectedItemsOnly = false;
    Song oldSong;
    ListView list_Playlist;
    TextView text_HintPlaylist, editText_PlaylistName, text_SizeOfPlaylist;
    ImageButton button_PreviewPlaylist;
    SongDisplayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(DataRepository.GetCurrentThemeId());
        setContentView(R.layout.gui_playlist);
        list_Playlist = findViewById(R.id.list_Playlist);
        list_Playlist.setBackgroundResource(
                (DataRepository.GetCurrentTheme().second) ?
                        R.color.darkThemeBackground :
                        R.color.white

        );
        text_SizeOfPlaylist = findViewById(R.id.text_SizeOfPlaylist);
        text_HintPlaylist = findViewById(R.id.text_HintPlaylist);
        editText_PlaylistName = findViewById(R.id.editText_PlaylistName);
        button_PreviewPlaylist = findViewById(R.id.button_PreviewPlaylist);
        if (editPlaylist) {
            playlistSongList = DataRepository.GetPlaylistSongDisplayList(playlistName);
            editText_PlaylistName.setText(playlistName);
        }

        GetAsyncGUIData();
        OnButtonClick(findViewById(R.id.button_PreviewPlaylist));
        Timer();
    }

    void Timer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                UpdateGUI();
                Timer();
            }
        }, 100);
    }

    void UpdateGUI() {
        Song tmpSong = DataRepository.GetPlayingSong();
        if (tmpSong != null && !tmpSong.equals(oldSong)) {
            for (SongDisplay songDisplay : allSongList) {
                songDisplay.setNotPlaying();
                if (songDisplay.getFirstLine().equals(tmpSong.getTitle()))
                    songDisplay.setPlaying();
            }
            for (SongDisplay songDisplay : playlistSongList) {
                songDisplay.setNotPlaying();
                if (songDisplay.getFirstLine().equals(tmpSong.getTitle()))
                    songDisplay.setPlaying();
            }
            oldSong = tmpSong;
            needListUpdate = keepListPosition = true;
        }
        if (needListUpdate) {
            needListUpdate = false;

            if (showSelectedItemsOnly) {
                if (playlistSongList != null && playlistSongList.size() == 0) {
                    list_Playlist.setVisibility(View.INVISIBLE);
                } else {
                    list_Playlist.setVisibility(View.VISIBLE);
                }
                adapter = new SongDisplayAdapter(this, playlistSongList);
            } else {
                if (allSongList != null && allSongList.size() == 0) {
                    list_Playlist.setVisibility(View.INVISIBLE);
                } else {
                    list_Playlist.setVisibility(View.VISIBLE);
                }
                adapter = new SongDisplayAdapter(this, allSongList);
            }
            if (keepListPosition && playlistSongList != null && allSongList != null) {
                index = list_Playlist.getFirstVisiblePosition();
                View v = list_Playlist.getChildAt(0);
                top = (v == null) ? 0 : (v.getTop() - list_Playlist.getPaddingTop());
                list_Playlist.setAdapter(adapter);
                list_Playlist.setSelectionFromTop(index, top);
            } else list_Playlist.setAdapter(adapter);

            list_Playlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (showSelectedItemsOnly) {
                        playlistSongList.remove(playlistSongList.get(position));
                        needListUpdate = true;
                        keepListPosition = true;
                    } else {
                        boolean isNew = allSongList.get(position).toggleSelected();
                        if (isNew) {
                            playlistSongList.add(allSongList.get(position));
                            Collections.sort(playlistSongList, new CompareSongDisplay());
                        } else {
                            playlistSongList.remove(allSongList.get(position));
                        }
                        needListUpdate = true;
                        keepListPosition = true;
                    }
                    return true;
                }
            });
            list_Playlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!showSelectedItemsOnly) {
                        boolean isNew = allSongList.get(position).toggleSelected();
                        if (isNew) {
                            playlistSongList.add(allSongList.get(position));
                            Collections.sort(playlistSongList, new CompareSongDisplay());
                        } else {
                            playlistSongList.remove(allSongList.get(position));
                        }
                        needListUpdate = true;
                        keepListPosition = true;
                    } else {
                        Toast.makeText(getApplicationContext(), "Holding a song to remove it",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            text_SizeOfPlaylist.setText(playlistSongList.size() + " song(s)");
        }
    }

    static void GetAsyncGUIData() {
        DataRepository.GetLibrary(0, null, new DataRepository.Callback() {
            @Override
            public void ApplyChanges() {

            }

            @Override
            public void ReturnList(ArrayList<SongDisplay> list) {
                GUI_Playlist.allSongList = list;
                if (playlistSongList != null && playlistSongList.size() > 0) {
                    for (SongDisplay songDisplay : playlistSongList) {
                        allSongList.remove(songDisplay);
                        songDisplay.setSelected();
                        allSongList.add(songDisplay);
                    }
                    Collections.sort(allSongList, new CompareSongDisplay());
                } else {
                    playlistSongList = new ArrayList<>();
                }
                GUI_Playlist.keepListPosition = false;
                GUI_Playlist.needListUpdate = true;
            }
        });
    }

    public void OnButtonClick(View view) {
        switch (view.getId()) {
            case R.id.button_ExitPlaylist: {
                if (!showSelectedItemsOnly) {
                    OnButtonClick(findViewById(R.id.button_PreviewPlaylist));
                    break;
                }
                if (editPlaylist) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setMessage("Exit without saving?");
                    dialog.setCancelable(true);

                    dialog.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Toast.makeText(getApplicationContext(), "Changes discarded",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
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
                } else {
                    finish();
                }
                break;
            }
            case R.id.button_SavePlaylist: {
                if (editPlaylist) {
                    DataRepository.RemovePlaylist(playlistName);
                    DataRepository.AddPlaylist(GUI_Playlist.this, editText_PlaylistName.getText().toString(), playlistSongList);
                } else {
                    if (!DataRepository.isPlaylistNameValid(editText_PlaylistName.getText().toString())) {
                        Toast.makeText(getApplicationContext(), "Playlist name already existed",
                                Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        DataRepository.AddPlaylist(GUI_Playlist.this, editText_PlaylistName.getText().toString(), playlistSongList);
                    }
                }
                finish();
                break;
            }
            case R.id.button_PreviewPlaylist: {
                showSelectedItemsOnly = !showSelectedItemsOnly;
                if (showSelectedItemsOnly) {
                    text_HintPlaylist.setText("Showing selected song(s)");
                    button_PreviewPlaylist.setImageResource(R.drawable.ic_add_black_24dp);
                } else {
                    text_HintPlaylist.setText("Showing all songs");
                    button_PreviewPlaylist.setImageResource(R.drawable.ic_list_black_24dp);
                }
                GetAsyncGUIData();
                break;
            }
            case R.id.button_DeletePlaylist: {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("Delete this playlist?");
                dialog.setCancelable(true);
                dialog.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(getApplicationContext(), "Playlist deleted",
                                        Toast.LENGTH_SHORT).show();
                                DataRepository.RemovePlaylist(playlistName);
                                finish();
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
        }
    }

    public static void CallbackEdit(String playlistName) {
        GUI_Playlist.playlistName = playlistName;
        GUI_Playlist.editPlaylist = true;
        GetAsyncGUIData();
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        OnButtonClick(findViewById(R.id.button_ExitPlaylist));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        playlistSongList = new ArrayList<>();
    }
}
