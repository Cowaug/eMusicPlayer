package project.exos.emusicplayer.Entity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;

import project.exos.emusicplayer.GUI.GUI_Library;
import project.exos.emusicplayer.GUI.GUI_MediaPlayer;

public class MediaController extends AppCompatActivity {
    //region variable
    final public static int REPEAT_OFF = 0, REPEAT_ONE = 1, REPEAT_ALL = 2;
    final public static boolean OFF = false, ON = true;
    final static Handler handler = new Handler();

    static boolean initial = false;
    static MediaPlayer mediaPlayer;
    static int loopingStatus = 0;
    static boolean shufflingStatus = false;
    static boolean playingStatus = false;
    static boolean hasSong = false;
    static CountDownTimer timer;
    static long remainTime = -1;
    static boolean wasHeadset;
    static AssetManager assets;
    static AssetFileDescriptor fileDescriptor;
    //endregion

    private static boolean isHeadsetOn(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (am == null)
            return false;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return am.isWiredHeadsetOn() || am.isBluetoothScoOn() || am.isBluetoothA2dpOn();
        } else {
            AudioDeviceInfo[] devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

            for (int i = 0; i < devices.length; i++) {
                AudioDeviceInfo device = devices[i];

                if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET
                        || device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                    return true;
                }
            }
        }
        return false;
    }

    public MediaController(Context context) {
        if (!initial) {
            wasHeadset = isHeadsetOn(context);
            initial = true;
            Timer(context);
            assets = context.getAssets();
        }
    }

    public static void SetRepeatMode(int MODE) {
        loopingStatus = MODE;
    }

    public static int SetNextRepeatMode(Context context) {
        loopingStatus = (loopingStatus + 1) % 3;
        DataRepository.saveObject(context, MediaController.GetLoopingStatus(), "loopingStatus");
        return loopingStatus;
    }

    public static void SetShuffle(boolean STATUS) {
        shufflingStatus = STATUS;
    }

    public static boolean GetShuffleStatus() {
        return shufflingStatus;
    }

    public static boolean ToggleShuffle(Context context) {
        shufflingStatus = !shufflingStatus;
        if (hasSong) DataRepository.ShufflePlayingList(shufflingStatus);
        DataRepository.saveObject(context, MediaController.GetLoopingStatus(), "loopingStatus");
        return shufflingStatus;
    }

    public static boolean ToggleFavorite(Context context) {
        if (!hasSong) return false;
        MP3Info mp3Info = new MP3Info(DataRepository.GetPathOfPlayingSong());
        boolean ret = mp3Info.ToggleFavorite();
        DataRepository.UpdateSong(context,DataRepository.GetPathOfPlayingSong());
        return ret;
    }

    public static void PutSongInQueueToPlayer() {
        if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
        GUI_MediaPlayer.CallbackNewSong();
        GUI_Library.CallbackNewSong();
        mediaPlayer.reset();
        try {
            String path = DataRepository.GetPathOfPlayingSong();
            if (path == null) {
                Pause();
                hasSong = false;
                return;
            }
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        hasSong = true;
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                player.start();
                if (!playingStatus) player.pause();
                StartTimer();
            }
        });
    }

    static void Timer(final Context context) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isHeadsetOn(context) && wasHeadset) {
                    Pause();
                    GUI_MediaPlayer.CallbackIconUpdate();
                    GUI_Library.CallbackIconUpdate();
                } else {
                    if (hasSong && mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 10000 && playingStatus) {
                        GUI_MediaPlayer.CallbackNextSong(true);
                    } else {
                        GUI_MediaPlayer.CallbackNextSong(false);
                    }
                    if (hasSong && mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 500 && playingStatus) {
                        if (loopingStatus == REPEAT_ONE) mediaPlayer.start();
                        else NextSong();
                        GUI_MediaPlayer.CallbackNextSong(false);
                    }
                }
                wasHeadset = isHeadsetOn(context);
                Timer(context);
            }
        }, 100);
    }

    public static void Play() {
        if (!hasSong) return;
        if (!mediaPlayer.isPlaying()) mediaPlayer.start();
        playingStatus = true;
        StartTimer();
    }

    public static void Pause() {
        if (!hasSong) return;
        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
        playingStatus = false;
        CancelTimer();
    }

    public static boolean NextSong() {
        if (!hasSong) return false;
        if (DataRepository.SetIndexInQueue(DataRepository.GetIndexInQueue() + 1)) {
            if (playingStatus) PutSongInQueueToPlayer();
            else PutSongInQueueToPlayer();
        } else if (loopingStatus == REPEAT_OFF) return false; //TODO: add option disable this
        else {
            DataRepository.SetIndexInQueue(0);
            if (playingStatus) PutSongInQueueToPlayer();
            else PutSongInQueueToPlayer();
        }
        return true;
    }

    public static boolean PreviousSong() {
        if (!hasSong) return false;
        if (DataRepository.SetIndexInQueue(DataRepository.GetIndexInQueue() - 1)) {
            if (playingStatus) PutSongInQueueToPlayer();
            else PutSongInQueueToPlayer();
        } else if (loopingStatus == REPEAT_OFF) return false; //TODO: add option disable this
        else {
            DataRepository.SetIndexInQueue(DataRepository.GetSizeOfQueue() - 1);
            if (playingStatus) PutSongInQueueToPlayer();
            else PutSongInQueueToPlayer();
        }
        return true;
    }

    public static void SeekForward(int amount) {
        if (!hasSong) return;
        if (mediaPlayer.getCurrentPosition() + amount + 1000 < mediaPlayer.getDuration())
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + amount);
    }

    public static void SeekBackward(int amount) {
        if (!hasSong) return;
        if (mediaPlayer.getCurrentPosition() - amount - 1000 > 0)
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - amount);
    }

    public static void SeekTo(int value) {
        if (!hasSong) return;
        mediaPlayer.seekTo(value);
    }

    public static boolean IsPlaying() {
        if (!hasSong) return false;
        return playingStatus;
    }

    public static int GetElapsedTime() {
        if (!hasSong) return 0;
        return mediaPlayer.getCurrentPosition();
    }

    public static int GetDuration() {
        if (!hasSong) return 0;
        return mediaPlayer.getDuration();
    }

    public static boolean HasSong() {
        return hasSong;
    }

    public static Song GetNextSong() {
        if (loopingStatus == REPEAT_ONE) return DataRepository.GetPlayingSong();
        else if (loopingStatus == REPEAT_OFF) return DataRepository.GetNextSong(false);
        else if (loopingStatus == REPEAT_ALL) return DataRepository.GetNextSong(true);
        return null;
    }

    public static int GetLoopingStatus() {
        return loopingStatus;
    }

    public static void SetTimer(long countDownTime) {
        remainTime = countDownTime;
        if (IsPlaying()) StartTimer();
    }

    public static void StartTimer() {
        if (timer != null) timer.cancel();
        if (remainTime < 1000) {
            remainTime = -1;
            return;
        }
        timer = new CountDownTimer(remainTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainTime = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                //android.os.Process.killProcess(android.os.Process.myPid());
                Pause();
                GUI_MediaPlayer.CallbackIconUpdate();
                GUI_Library.CallbackIconUpdate();
            }
        };
        timer.start();
    }

    public static long GetTimerRemainingTime() {
        return remainTime;
    }

    public static void CancelTimer() {
        if (timer != null) timer.cancel();
        timer = null;
    }
}
