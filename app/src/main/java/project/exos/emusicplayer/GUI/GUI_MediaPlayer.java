package project.exos.emusicplayer.GUI;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import project.exos.emusicplayer.Adapter.SongDisplayAdapter;
import project.exos.emusicplayer.Entity.DataRepository;
import project.exos.emusicplayer.Entity.MP3Info;
import project.exos.emusicplayer.Entity.MediaController;
import project.exos.emusicplayer.Entity.Song;
import project.exos.emusicplayer.Entity.SongDisplay;
import project.exos.emusicplayer.R;

import static android.view.View.GONE;
import static project.exos.emusicplayer.Entity.MediaController.REPEAT_ALL;
import static project.exos.emusicplayer.Entity.MediaController.REPEAT_OFF;
import static project.exos.emusicplayer.Entity.MediaController.REPEAT_ONE;

public class GUI_MediaPlayer extends AppCompatActivity {

    //region variable
    TextView
            text_QueueEmpty,
            text_SongTitle,
            text_ArtistName,
            text_Lyric,
            text_UpcomingSong,
            text_ElapsedTime,
            text_SongDuration,
            text_RemainTimerTime;
    SeekBar seekBar_ElapsedTime;
    ProgressBar progressBar_ElapsedTime;
    ListView list_PlayingQueue;
    ImageButton button_PlayingList,
            button_OpenTimer,
            button_Repeat,
            button_Favorite,
            button_Shuffle,
            button_Play_Pause,
            button_NextSong,
            button_PreviousSong,
            button_SeekForward,
            button_SeekBackward;
    ImageView image_Artwork;
    Handler handler;
    ArrayList<SongDisplay> arrayList = new ArrayList<>();
    boolean showQueue = false;
    boolean needListUpdate = false;
    static boolean needIconUpdate = false;
    static boolean showNextSong = false;
    static boolean newSong = true;
    boolean waitForMarqueeInitial = true;
    int listItemsIndex, listItemsTop;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //System.out.println("ENTER onCreate");
        super.onCreate(savedInstanceState);

        //region apply theme and set view
        setTheme(DataRepository.GetCurrentThemeId());
        setContentView(R.layout.gui_mediaplayer);
        //endregion

        //region initial variable
        text_Lyric = findViewById(R.id.text_Lyric);
        text_Lyric.setBackgroundResource(
                (DataRepository.GetCurrentTheme().second) ?
                        R.color.darkThemeBackgroundTransparent :
                        R.color.whiteTransparent
        );
        text_RemainTimerTime = findViewById(R.id.text_RemainTimerTime);
        text_QueueEmpty = findViewById(R.id.text_QueueEmpty);
        text_ArtistName = findViewById(R.id.text_ArtistName);
        text_ElapsedTime = findViewById(R.id.text_ElapsedTime);
        text_SongDuration = findViewById(R.id.text_SongDuration);
        text_SongTitle = findViewById(R.id.text_SongTitle);
        text_UpcomingSong = findViewById(R.id.text_UpcomingSong);
        seekBar_ElapsedTime = findViewById(R.id.seekBar_ElapsedTime);
        progressBar_ElapsedTime = findViewById(R.id.progressBar_ElapsedTime);
        list_PlayingQueue = findViewById(R.id.list_PlayingQueue);
        list_PlayingQueue.setBackgroundResource(
                (DataRepository.GetCurrentTheme().second) ?
                        R.color.darkThemeBackgroundTransparent :
                        R.color.whiteTransparent
        );
        button_Favorite = findViewById(R.id.button_Favorite);
        button_NextSong = findViewById(R.id.button_NextSong);
        button_OpenTimer = findViewById(R.id.button_OpenTimer);
        button_Play_Pause = findViewById(R.id.button_Play_Pause);
        button_PlayingList = findViewById(R.id.button_PlayingList);
        button_PreviousSong = findViewById(R.id.button_PreviousSong);
        button_Repeat = findViewById(R.id.button_Repeat);
        button_SeekBackward = findViewById(R.id.button_SeekBackward);
        button_SeekForward = findViewById(R.id.button_SeekForward);
        button_Shuffle = findViewById(R.id.button_Shuffle);
        image_Artwork = findViewById(R.id.image_Artwork);

        handler = new Handler();
        //endregion

        //region initial seekBar
        seekBar_ElapsedTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean bool) {
                if (bool) {
                    MediaController.SeekTo(i);
                    UpdateGUI();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //endregion

        //region initial set layout of textview
        text_SongTitle.setSelected(true);
        text_SongTitle.post(new Runnable() {
            @Override
            public void run() {
                text_SongTitle.setLayoutParams(new ConstraintLayout.LayoutParams(text_SongTitle.getWidth(), text_SongTitle.getHeight()));
            }
        });
        text_ArtistName.setSelected(true);
        text_ArtistName.post(new Runnable() {
            @Override
            public void run() {
                text_ArtistName.setLayoutParams(new ConstraintLayout.LayoutParams(text_ArtistName.getWidth(), text_ArtistName.getHeight()));
            }
        });
        text_UpcomingSong.setSelected(true);
        text_UpcomingSong.post(new Runnable() {
            @Override
            public void run() {
                text_UpcomingSong.setVisibility(View.VISIBLE);
                text_UpcomingSong.setLayoutParams(new ConstraintLayout.LayoutParams(text_UpcomingSong.getWidth(), text_UpcomingSong.getHeight()));
                text_UpcomingSong.setVisibility(View.GONE);
                waitForMarqueeInitial = false;
            }
        });
        //endregion
        UpdateGUI();
        Timer();
    }

    void Timer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                UpdateGUI();
                Timer();
            }
        }, 1000);
    }

    public void OnButtonClick(View view) {
        switch (view.getId()) {
            case R.id.button_Play_Pause: {
                if (!MediaController.HasSong()) break;
                if (MediaController.IsPlaying()) {
                    MediaController.Pause();
                    button_Play_Pause.setImageResource(R.drawable.avd_play_to_pause);
                    Animatable animation = (Animatable) button_Play_Pause.getDrawable();
                    animation.start();
                } else {
                    MediaController.Play();
                    button_Play_Pause.setImageResource(R.drawable.avd_pause_to_play);
                    Animatable animation = (Animatable) button_Play_Pause.getDrawable();
                    animation.start();
                }
                break;
            }
            case R.id.button_NextSong: {
                if (!MediaController.NextSong())
                    Toast.makeText(getApplicationContext(), "Last of queue!",
                            Toast.LENGTH_SHORT).show();
                Animatable animation = (Animatable) button_NextSong.getDrawable();
                animation.start();
                arrayList = DataRepository.GetPlayingQueue();
                break;
            }
            case R.id.button_PreviousSong: {
                if (!MediaController.PreviousSong())
                    Toast.makeText(getApplicationContext(), "First of queue!",
                            Toast.LENGTH_SHORT).show();
                Animatable animation = (Animatable) button_PreviousSong.getDrawable();
                animation.start();
                arrayList = DataRepository.GetPlayingQueue();
                break;
            }
            case R.id.button_SeekForward: {
                MediaController.SeekForward(5000);
                Animatable animation = (Animatable) button_SeekForward.getDrawable();
                animation.start();
                break;
            }
            case R.id.button_SeekBackward: {
                MediaController.SeekBackward(5000);
                Animatable animation = (Animatable) button_SeekBackward.getDrawable();
                animation.start();
                break;
            }
            case R.id.button_Favorite: {
                if (MediaController.ToggleFavorite(GUI_MediaPlayer.this)) {
                    button_Favorite.setImageResource(R.drawable.avd_heart_fill);
                    Animatable animation = (Animatable) button_Favorite.getDrawable();
                    animation.start();
                    for (SongDisplay songDisplay : arrayList) {
                        if (songDisplay.getPath().equals(DataRepository.GetPathOfPlayingSong()))
                            songDisplay.setFavorite(true);
                    }
                } else {
                    button_Favorite.setImageResource(R.drawable.avd_heart_break);
                    Animatable animation = (Animatable) button_Favorite.getDrawable();
                    animation.start();
                    for (SongDisplay songDisplay : arrayList) {
                        if (songDisplay.getPath().equals(DataRepository.GetPathOfPlayingSong()))
                            songDisplay.setFavorite(false);
                    }
                }
                needListUpdate = true;
                break;
            }
            case R.id.button_Repeat: {
                switch (MediaController.SetNextRepeatMode(this)) {
                    case REPEAT_OFF: {
                        button_Repeat.setImageResource(R.drawable.avd_all_to_off);
                        Animatable animation = (Animatable) button_Repeat.getDrawable();
                        animation.start();
                        break;
                    }
                    case REPEAT_ONE: {
                        button_Repeat.setImageResource(R.drawable.avd_off_to_one);
                        Animatable animation = (Animatable) button_Repeat.getDrawable();
                        animation.start();
                        break;
                    }
                    case REPEAT_ALL: {
                        button_Repeat.setImageResource(R.drawable.avd_one_to_all);
                        Animatable repeatAnimation = (Animatable) button_Repeat.getDrawable();
                        repeatAnimation.start();
                        break;
                    }
                }
                break;
            }
            case R.id.button_Shuffle: {
                if (MediaController.ToggleShuffle(this)) {
                    button_Shuffle.setImageResource(R.drawable.avd_shuffle_off_to_on);
                    Animatable animation = (Animatable) button_Shuffle.getDrawable();
                    animation.start();
                } else {
                    button_Shuffle.setImageResource(R.drawable.avd_shuffle_on_to_off);
                    Animatable animation = (Animatable) button_Shuffle.getDrawable();
                    animation.start();
                }
                needListUpdate = true;
                break;
            }
            case R.id.button_OpenTimer: {
                Popup_Timer popupTimer = new Popup_Timer(this);
                popupTimer.show();
                break;
            }
            case R.id.button_PlayingList: {
                arrayList = DataRepository.GetPlayingQueue();
                needListUpdate = true;
                showQueue = !showQueue;
                if (showQueue) {
                    if (arrayList != null && arrayList.size() != 0) {
                        list_PlayingQueue.setVisibility(View.VISIBLE);
                    } else text_QueueEmpty.setVisibility(View.VISIBLE);
                } else {
                    list_PlayingQueue.setVisibility(View.INVISIBLE);
                    text_QueueEmpty.setVisibility(View.INVISIBLE);
                }
                break;
            }
            case R.id.button_Lyric: {
                if (text_Lyric.getVisibility() == View.VISIBLE)
                    text_Lyric.setVisibility(View.INVISIBLE);
                else text_Lyric.setVisibility(View.VISIBLE);
                break;
            }
        }
        UpdateGUI();
    }

    void UpdateGUI() {
        Song song = DataRepository.GetPlayingSong();

        //region next song notification
        Song nextSong = MediaController.GetNextSong();
        if (showNextSong && nextSong != null && !("Next song: " + nextSong.getTitle()).equals(text_UpcomingSong.getText().toString())) {
            text_UpcomingSong.setText("Next song: " + nextSong.getTitle());
            text_UpcomingSong.setSelected(false);
            text_UpcomingSong.postDelayed(new Runnable() {
                @Override
                public void run() {
                    text_UpcomingSong.setSelected(true);
                }
            }, 1000);
        } else if (MediaController.GetNextSong() == null) text_UpcomingSong.setText("");

        if (showNextSong && !text_UpcomingSong.getText().toString().equals("")) {
            text_UpcomingSong.setVisibility(View.VISIBLE);
        } else {
            if (!waitForMarqueeInitial) text_UpcomingSong.setVisibility(View.GONE);
        }
        //endregion

        if ((song != null && newSong)) {
            newSong = false;
            //region title + artist + duration text + duration set + lyric
            text_SongTitle.setText(song.getTitle());
            text_SongTitle.setSelected(false);
            text_SongTitle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    text_SongTitle.setSelected(true);
                }
            }, 1000);

            text_ArtistName.setText(song.getArtist());
            text_ArtistName.setSelected(false);
            text_ArtistName.postDelayed(new Runnable() {
                @Override
                public void run() {
                    text_ArtistName.setSelected(true);
                }
            }, 1000);

            text_SongDuration.setText(
                    ((MediaController.GetDuration() / 1000 - MediaController.GetDuration() / 60000 * 60) < 10) ?
                            MediaController.GetDuration() / 60000 + ":" + 0 + (MediaController.GetDuration() / 1000 - MediaController.GetDuration() / 60000 * 60) :
                            MediaController.GetDuration() / 60000 + ":" + (MediaController.GetDuration() / 1000 - MediaController.GetDuration() / 60000 * 60)
            );
            progressBar_ElapsedTime.setMax(MediaController.GetDuration());
            seekBar_ElapsedTime.setMax(MediaController.GetDuration());

            MP3Info mp3Info = new MP3Info(song.getPath());
            String lyric = mp3Info.GetLyric();
            if (lyric != null) {
                text_Lyric.setText(lyric);
            } else {
                text_Lyric.setText("Can't find lyric for this song");
            }
            arrayList = DataRepository.GetPlayingQueue();
            //endregion

            //region button
            if (MediaController.IsPlaying())
                button_Play_Pause.setImageResource(R.drawable.ic_pause_black_24dp);
            else
                button_Play_Pause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            if (mp3Info.IsFavorite())
                button_Favorite.setImageResource(R.drawable.ic_favorite_black_24dp);
            else
                button_Favorite.setImageResource(R.drawable.ic_favorite_border_black_24dp);
            //endregion

            //region get song's art
            Bitmap bitmap = mp3Info.GetBitmap();
            if (bitmap != null) {
                image_Artwork.setImageTintList((ColorStateList.valueOf(Color.TRANSPARENT)));
                image_Artwork.setImageBitmap(bitmap);
            } else {
                // TODO: change by theme
                image_Artwork.setImageTintList((DataRepository.GetCurrentTheme().second) ?
                        ColorStateList.valueOf(Color.WHITE) :
                        ColorStateList.valueOf(Color.BLACK)
                );
                image_Artwork.setImageResource(R.drawable.ic_music_note_black_24dp);
            }
            //endregion
            needListUpdate = true;
        }
        if (needListUpdate) {
            //region list playing queue
            listItemsIndex = list_PlayingQueue.getFirstVisiblePosition();
            View v = list_PlayingQueue.getChildAt(0);
            listItemsTop = (v == null) ? 0 : (v.getTop() - list_PlayingQueue.getPaddingTop());
            final SongDisplayAdapter adapter = new SongDisplayAdapter(this, arrayList);
            list_PlayingQueue.setAdapter(adapter);

            list_PlayingQueue.setSelectionFromTop(listItemsIndex, listItemsTop);

            list_PlayingQueue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    DataRepository.SetIndexInQueue(arrayList.get(i).getPath());// note: set index to selected song
                    MediaController.PutSongInQueueToPlayer();
                    MediaController.Play();
                    if (!MediaController.IsPlaying()) {
                        button_Play_Pause.setImageResource(R.drawable.avd_pause_to_play);
                        Animatable animation = (Animatable) button_Play_Pause.getDrawable();
                        animation.start();
                    }
                    arrayList = DataRepository.GetPlayingQueue();
                    UpdateGUI();
                }
            });
            list_PlayingQueue.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // TODO: show menu (remove song from queue)
                    return true;
                }
            });
            //endregion
            needListUpdate = false;
        }

        //region update icon
        if (needIconUpdate) {
            needIconUpdate = false;
            if (MediaController.IsPlaying())
                button_Play_Pause.setImageResource(R.drawable.ic_pause_black_24dp);
            else
                button_Play_Pause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            switch (MediaController.GetLoopingStatus()) {
                case REPEAT_OFF: {
                    // note: this icon was set default so no need to set here
                    break;
                }
                case REPEAT_ONE: {
                    button_Repeat.setImageResource(R.drawable.ic_repeat_one_black_24dp);
                    break;
                }
                case REPEAT_ALL: {
                    button_Repeat.setImageResource(R.drawable.ic_repeat_black_24dp);
                    break;
                }
            }
            if (MediaController.GetShuffleStatus())
                button_Shuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);
            else button_Shuffle.setImageResource(R.drawable.ic_shuffle_black_off_24dp);
        }
        //endregion

        //region elapsed text + timer
        text_ElapsedTime.setText(
                ((MediaController.GetElapsedTime() / 1000 - MediaController.GetElapsedTime() / 60000 * 60) < 10) ?
                        MediaController.GetElapsedTime() / 60000 + ":" + 0 + (MediaController.GetElapsedTime() / 1000 - MediaController.GetElapsedTime() / 60000 * 60) :
                        MediaController.GetElapsedTime() / 60000 + ":" + (MediaController.GetElapsedTime() / 1000 - MediaController.GetElapsedTime() / 60000 * 60)
        );

        long remainingTime = MediaController.GetTimerRemainingTime();
        if (remainingTime <= 0) findViewById(R.id.layout_Timer).setVisibility(GONE);
        else {
            text_RemainTimerTime.setText(
                    (remainingTime >= 60 * 60 * 1000 ?
                            remainingTime / (60 * 60 * 1000) + " h " + (remainingTime % (60 * 60 * 1000)) / (60 * 1000) + " m" :
                            remainingTime / (60 * 1000) > 0 ?
                                    remainingTime / (60 * 1000) + " m " + remainingTime % (60 * 1000) / 1000 + " s" :
                                    remainingTime / 1000 + " s")

            );
            findViewById(R.id.layout_Timer).setVisibility(View.VISIBLE);
        }
        //endregion

        //region seekBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar_ElapsedTime.setProgress(MediaController.GetElapsedTime(), true);
        } else {
            progressBar_ElapsedTime.setProgress(MediaController.GetElapsedTime());
        }
        //endregion
    }

    public static void CallbackNextSong(boolean showNextSong) {
        GUI_MediaPlayer.showNextSong = showNextSong;
    }

    public static void CallbackNewSong() {
        newSong = true;
    }

    public static void CallbackIconUpdate() {
        needIconUpdate = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        arrayList = DataRepository.GetPlayingQueue();
        needListUpdate = needIconUpdate = newSong = true;
        UpdateGUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}