package project.exos.emusicplayer.GUI;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import project.exos.emusicplayer.Entity.DataRepository;
import project.exos.emusicplayer.Entity.MP3Info;
import project.exos.emusicplayer.Entity.SongDisplay;
import project.exos.emusicplayer.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class GUI_SongDetails extends AppCompatActivity {
    static ArrayList<SongDisplay> songDisplayArrayList = new ArrayList<>();
    static int mode = -1;
    static final int VIEW = 0,
            EDIT = 1;
    static boolean editable = false;
    static boolean ready = false;
    static String imagePath = "";
    static byte[] rawImage = {0};
    static boolean newImage = false;


    TextView text_Location,
            editText_SongDetailsTitle,
            editText_SongDetailsArtist,
            editText_SongDetailsAlbum,
            editText_SongDetailsLyric;
    ImageButton button_EditArtwork,
            button_EditTitle,
            button_EditArtist,
            button_EditAlbum,
            button_EditLyric,
            button_RevertEditSongDetails,
            button_DoneEditSongDetails;

    ImageView image_SongDetailsArtwork;

    MP3Info mp3Info;
    boolean sameTitle = true,
            sameArtist = true,
            sameAlbum = true,
            sameLyric = true;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(DataRepository.GetCurrentThemeId());
        setContentView(R.layout.gui_song_details);
        text_Location = findViewById(R.id.text_Location);
        editText_SongDetailsTitle = findViewById(R.id.editText_SongDetailsTitle);
        editText_SongDetailsArtist = findViewById(R.id.editText_SongDetailsArtist);
        editText_SongDetailsAlbum = findViewById(R.id.editText_SongDetailsAlbum);
        editText_SongDetailsLyric = findViewById(R.id.editText_SongDetailsLyric);
        button_EditArtwork = findViewById(R.id.button_EditArtwork);
        button_EditArtwork.setBackgroundTintList(
                (DataRepository.GetCurrentTheme().second) ?
                        ColorStateList.valueOf(getResources().getColor(R.color.darkThemeBackgroundTransparent))
                        :
                        ColorStateList.valueOf(getResources().getColor(R.color.whiteTransparent))
        );
        button_EditTitle = findViewById(R.id.button_EditTitle);
        button_EditArtist = findViewById(R.id.button_EditArtist);
        button_EditAlbum = findViewById(R.id.button_EditAlbum);
        button_EditLyric = findViewById(R.id.button_EditLyric);
        button_RevertEditSongDetails = findViewById(R.id.button_RevertEditSongDetails);
        button_DoneEditSongDetails = findViewById(R.id.button_DoneEditSongDetails);
        image_SongDetailsArtwork = findViewById(R.id.image_SongDetailsArtwork);
        Timer();
    }

    void Timer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ready) UpdateGUI();
                else Timer();
            }
        }, 100);
    }

    public void OnButtonClick(View view) {
        switch (view.getId()) {
            case R.id.button_EditTitle: {
                if (editable) {
                    editText_SongDetailsTitle.setEnabled(true);
                    button_EditTitle.setVisibility(GONE);
                    button_DoneEditSongDetails.setVisibility(VISIBLE);
                    button_RevertEditSongDetails.setVisibility(VISIBLE);
                    mode = EDIT;

                    editText_SongDetailsTitle.setSelectAllOnFocus(true);
                    editText_SongDetailsTitle.requestFocus();
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(editText_SongDetailsTitle, InputMethodManager.SHOW_FORCED);
                } else {
                    Toast.makeText(getApplicationContext(), "Edit not available for this file", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.button_EditArtist: {
                if (editable) {
                    editText_SongDetailsArtist.setEnabled(true);
                    button_EditArtist.setVisibility(GONE);
                    button_DoneEditSongDetails.setVisibility(VISIBLE);
                    button_RevertEditSongDetails.setVisibility(VISIBLE);
                    mode = EDIT;

                    editText_SongDetailsArtist.setSelectAllOnFocus(true);
                    editText_SongDetailsArtist.requestFocus();
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(editText_SongDetailsArtist, InputMethodManager.SHOW_FORCED);
                } else {
                    Toast.makeText(getApplicationContext(), "Edit not available for this file", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.button_EditAlbum: {
                if (editable) {
                    editText_SongDetailsAlbum.setEnabled(true);
                    button_EditAlbum.setVisibility(GONE);
                    button_DoneEditSongDetails.setVisibility(VISIBLE);
                    button_RevertEditSongDetails.setVisibility(VISIBLE);
                    mode = EDIT;

                    editText_SongDetailsAlbum.setSelectAllOnFocus(true);
                    editText_SongDetailsAlbum.requestFocus();
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(editText_SongDetailsAlbum, InputMethodManager.SHOW_FORCED);
                } else {
                    Toast.makeText(getApplicationContext(), "Edit not available for this file", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.button_EditLyric: {
                if (editable) {
                    editText_SongDetailsLyric.setEnabled(true);
                    button_EditLyric.setVisibility(GONE);
                    button_DoneEditSongDetails.setVisibility(VISIBLE);
                    button_RevertEditSongDetails.setVisibility(VISIBLE);
                    mode = EDIT;

                    editText_SongDetailsLyric.setSelectAllOnFocus(true);
                    editText_SongDetailsLyric.requestFocus();
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(editText_SongDetailsLyric, InputMethodManager.SHOW_FORCED);
                } else {
                    Toast.makeText(getApplicationContext(), "Edit not available for this file", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.button_EditArtwork: {
                if (editable) {
                    button_DoneEditSongDetails.setVisibility(VISIBLE);
                    button_RevertEditSongDetails.setVisibility(VISIBLE);
                    mode = EDIT;

                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");
                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                    startActivityForResult(chooserIntent, 1);
                    System.out.println("launch select image");
                } else {
                    Toast.makeText(getApplicationContext(), "Edit not available for this file", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.button_RevertEditSongDetails: {
                UpdateGUI();
                Toast.makeText(getApplicationContext(), "Changes reversed",
                        Toast.LENGTH_SHORT).show();
                newImage = false;
                break;
            }
            case R.id.button_DoneEditSongDetails: {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("Make these changes permanent?");
                dialog.setCancelable(true);

                dialog.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                editText_SongDetailsTitle.setEnabled(false);
                                button_EditTitle.setVisibility(VISIBLE);


                                editText_SongDetailsArtist.setEnabled(false);
                                button_EditArtist.setVisibility(VISIBLE);


                                editText_SongDetailsAlbum.setEnabled(false);
                                button_EditAlbum.setVisibility(VISIBLE);

                                editText_SongDetailsLyric.setEnabled(false);
                                button_EditLyric.setVisibility(VISIBLE);

                                button_DoneEditSongDetails.setVisibility(GONE);
                                button_RevertEditSongDetails.setVisibility(GONE);

                                if (songDisplayArrayList.size() == 1) {
                                    mp3Info.SetTitle(editText_SongDetailsTitle.getText().toString());
                                    mp3Info.SetArtist(editText_SongDetailsArtist.getText().toString());
                                    mp3Info.SetAlbum(editText_SongDetailsAlbum.getText().toString());
                                    mp3Info.SetLyric(editText_SongDetailsLyric.getText().toString());
                                    if (newImage && rawImage != null && imagePath != null)
                                        mp3Info.SetBitmap(rawImage, imagePath);

                                    mp3Info = new MP3Info(songDisplayArrayList.get(0).getPath());
                                    mp3Info.SetTitle(editText_SongDetailsTitle.getText().toString());
                                    mp3Info.SetArtist(editText_SongDetailsArtist.getText().toString());
                                    mp3Info.SetAlbum(editText_SongDetailsAlbum.getText().toString());
                                    mp3Info.SetLyric(editText_SongDetailsLyric.getText().toString());
                                    if (newImage && rawImage != null && imagePath != null)
                                        mp3Info.SetBitmap(rawImage, imagePath);

                                    final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    final Uri contentUri = Uri.fromFile(new File(songDisplayArrayList.get(0).getPath()));
                                    scanIntent.setData(contentUri);
                                    sendBroadcast(scanIntent);

                                    DataRepository.UpdateSong(GUI_SongDetails.this, songDisplayArrayList.get(0).getPath());
                                }
                                Toast.makeText(getApplicationContext(), "Song saved",
                                        Toast.LENGTH_SHORT).show();
                                mode = VIEW;
                                UpdateGUI();
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
            case R.id.button_ExitEditSongDetails: {
                if (mode == VIEW) {
                    finish();
                } else {
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
                }
                if (this.getCurrentFocus() != null)
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
                break;
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            super.onActivityResult(requestCode, resultCode, data);
            Uri uri = data.getData();
            String[] strings = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;
            if (uri != null) {
                cursor = getContentResolver().query(uri, strings, null, null, null);
            }
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                imagePath = cursor.getString(column_index);
            }
            if (cursor != null) {
                cursor.close();
            }
            if (imagePath != null)
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        final Bitmap bitmap, resized;
                        float resize_scale = 1;
                        bitmap = BitmapFactory.decodeFile(imagePath);
                        if (bitmap.getHeight() > 1600 || bitmap.getWidth() > 1600) {
                            if ((1600 / (float) bitmap.getWidth()) > (1600 / (float) bitmap.getHeight()))
                                resize_scale = (1600 / (float) bitmap.getWidth());
                            else resize_scale = (1600 / (float) bitmap.getHeight());
                        }
                        resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * resize_scale), (int) (bitmap.getHeight() * resize_scale), true);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        resized.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        rawImage = stream.toByteArray();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("set bitmap");
                                image_SongDetailsArtwork.setImageBitmap(resized);
                                newImage = true;
                            }
                        });
                        return null;
                    }
                }.execute();
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        OnButtonClick(findViewById(R.id.button_ExitEditSongDetails));
    }

    void UpdateGUI() {
        if (mode == EDIT) {
            editText_SongDetailsTitle.setEnabled(true);
            button_EditTitle.setVisibility(GONE);

            editText_SongDetailsArtist.setEnabled(true);
            button_EditArtist.setVisibility(View.GONE);

            editText_SongDetailsAlbum.setEnabled(true);
            button_EditAlbum.setVisibility(View.GONE);

            editText_SongDetailsLyric.setEnabled(true);
            button_EditLyric.setVisibility(View.GONE);

            button_DoneEditSongDetails.setVisibility(VISIBLE);
            button_RevertEditSongDetails.setVisibility(VISIBLE);
        } else if (mode == VIEW) {
            editText_SongDetailsTitle.setEnabled(false);
            button_EditTitle.setVisibility(VISIBLE);

            editText_SongDetailsArtist.setEnabled(false);
            button_EditArtist.setVisibility(VISIBLE);

            editText_SongDetailsAlbum.setEnabled(false);
            button_EditAlbum.setVisibility(VISIBLE);

            editText_SongDetailsLyric.setEnabled(false);
            button_EditLyric.setVisibility(VISIBLE);

            button_DoneEditSongDetails.setVisibility(GONE);
            button_RevertEditSongDetails.setVisibility(GONE);
        }
        mp3Info = new MP3Info(songDisplayArrayList.get(0).getPath());
        text_Location.setText(songDisplayArrayList.get(0).getPath());
        editText_SongDetailsTitle.setText(mp3Info.GetTitle());
        editText_SongDetailsArtist.setText(mp3Info.GetArtist());
        editText_SongDetailsAlbum.setText(mp3Info.GetAlbum());
        editText_SongDetailsLyric.setText(mp3Info.GetLyric());
        editText_SongDetailsLyric.setHint("Not found");
        image_SongDetailsArtwork.setImageBitmap(mp3Info.GetBitmap());
        editable = mp3Info.IsEditable();
    }

    public static void CallbackSongPath(ArrayList<SongDisplay> songDisplayArrayList, int mode) {
        GUI_SongDetails.songDisplayArrayList = songDisplayArrayList;
        GUI_SongDetails.mode = mode;
        ready = true;
    }
}
