package project.exos.emusicplayer.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import project.exos.emusicplayer.Entity.DataRepository;
import project.exos.emusicplayer.Entity.MP3Info;
import project.exos.emusicplayer.Entity.SongDisplay;
import project.exos.emusicplayer.R;

public class SongDisplayAdapter extends ArrayAdapter<SongDisplay> {

    private Context mContext;
    private List<SongDisplay> list;

    public SongDisplayAdapter(@NonNull Context context, ArrayList<SongDisplay> list) {
        super(context, 0, list);
        mContext = context;
        this.list = list;
    }

    private static class ViewHolder {
        private TextView firstLine;
        private TextView secondLine;
        private ConstraintLayout isPlaying;
        private ImageView isLoved;
        private ConstraintLayout isSelected;
        private ImageView image_SongArtwork;
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final SongDisplay currentItems = list.get(position);

        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.adapter_song_display, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.firstLine = convertView.findViewById(R.id.text_firstLine);
            viewHolder.secondLine = convertView.findViewById(R.id.text_secondLine);
            viewHolder.isPlaying = convertView.findViewById(R.id.solid_isPlaying);
            viewHolder.isLoved = convertView.findViewById(R.id.image_isLoved);
            viewHolder.isSelected = convertView.findViewById(R.id.solid_isSelected);
            viewHolder.image_SongArtwork = convertView.findViewById(R.id.image_SongArtwork);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (currentItems != null) {
            viewHolder.firstLine.setText(currentItems.getFirstLine());
            viewHolder.secondLine.setText(currentItems.getSecondLine());
            viewHolder.isPlaying.setVisibility(currentItems.isPlaying() ? View.VISIBLE : View.INVISIBLE);
            viewHolder.isLoved.setVisibility(currentItems.isFavorite() ? View.VISIBLE : View.GONE);
            viewHolder.isSelected.setVisibility(currentItems.isSelected() ? View.VISIBLE : View.INVISIBLE);

            // TODO: change by theme

            viewHolder.image_SongArtwork.setImageResource(R.drawable.ic_audiotrack_black_24dp);
            viewHolder.image_SongArtwork.setImageTintList((DataRepository.GetCurrentTheme().second) ?
                    ColorStateList.valueOf(Color.WHITE) :
                    ColorStateList.valueOf(Color.BLACK));

            //region load image async

            if (false && currentItems.getPath() != null) {
                final MP3Info mp3Info = new MP3Info(currentItems.getPath());
                if (mp3Info.IsEditable())
                    new AsyncTask<Void, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(Void... voids) {
                        /*Bitmap bitmap = mp3Info.GetBitmap();
                        if (bitmap != null) {
                            float resize_scale = (10 / (float) bitmap.getWidth());
                            return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * resize_scale), (int) (bitmap.getHeight() * resize_scale), true);
                        }*/
                            return mp3Info.GetBitmap();
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            if (bitmap != null) {
                                viewHolder.image_SongArtwork.setImageTintList((ColorStateList.valueOf(Color.TRANSPARENT)));
                                viewHolder.image_SongArtwork.setImageBitmap(bitmap);
                            } else {
                                viewHolder.image_SongArtwork.setImageResource(R.drawable.ic_audiotrack_black_24dp);
                                viewHolder.image_SongArtwork.setImageTintList((DataRepository.GetCurrentTheme().second) ?
                                        ColorStateList.valueOf(Color.WHITE) :
                                        ColorStateList.valueOf(Color.BLACK));
                            }
                        }
                    }.execute();
            } else {
                viewHolder.image_SongArtwork.setImageResource(R.drawable.ic_audiotrack_black_24dp);
                viewHolder.image_SongArtwork.setImageTintList((DataRepository.GetCurrentTheme().second) ?
                        ColorStateList.valueOf(Color.WHITE) :
                        ColorStateList.valueOf(Color.BLACK));
            }
            //endregion
        }
        return convertView;
    }
}