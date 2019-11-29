package project.exos.emusicplayer.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import project.exos.emusicplayer.R;
import project.exos.emusicplayer.Entity.Theme;

public class ThemeAdapter extends ArrayAdapter<Theme> {

    private Context mContext;
    private List<Theme> list;

    public ThemeAdapter(@NonNull Context context, ArrayList<Theme> list) {
        super(context, 0, list);
        mContext = context;
        this.list = list;
    }

    private static class ViewHolder {
        private ConstraintLayout solid_Background, solid_AccentColor, solid_AccentColor2;
        private ImageView image_Tint, image_Selected;
        private TextView text_Tint, text_TextColor;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Theme currentItems = list.get(position);

        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.adapter_theme, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.solid_AccentColor = convertView.findViewById(R.id.solid_AccentColor);
            viewHolder.solid_AccentColor2 = convertView.findViewById(R.id.solid_AccentColor2);
            viewHolder.image_Tint = convertView.findViewById(R.id.image_Tint);
            viewHolder.image_Selected = convertView.findViewById(R.id.image_Selected);
            viewHolder.text_Tint = convertView.findViewById(R.id.text_Tint);
            viewHolder.text_TextColor = convertView.findViewById(R.id.text_TextColor);
            viewHolder.solid_Background = convertView.findViewById(R.id.solid_Background);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (currentItems != null) {
            viewHolder.solid_Background.setBackgroundTintList(
                    (currentItems.isDarkTheme()) ?
                            ColorStateList.valueOf(Color.parseColor("#303030")) :
                            ColorStateList.valueOf(Color.WHITE)
            );
            viewHolder.solid_AccentColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(currentItems.getAccentColor())));
            viewHolder.solid_AccentColor2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(currentItems.getAccentColor())));


            if (DataRepository.GetCurrentTheme().first.equals(currentItems.getColor()) && DataRepository.GetCurrentTheme().second == currentItems.isDarkTheme()) {
                viewHolder.image_Selected.setImageResource(R.drawable.ic_done_black_24dp);
                viewHolder.image_Selected.setAlpha(1f);
            } else {
                if (currentItems.isPreview()) {
                    viewHolder.image_Selected.setImageResource(R.drawable.ic_visibility_black_24dp);
                    viewHolder.image_Selected.setAlpha(1f);
                } else
                    viewHolder.image_Selected.setAlpha(0f);
            }

            viewHolder.image_Selected.setImageTintList(ColorStateList.valueOf(Color.parseColor(currentItems.getTintColor())));
            viewHolder.image_Tint.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(currentItems.getTintColor())));

            viewHolder.text_Tint.setText(currentItems.getColor());
            viewHolder.text_Tint.setTextColor(ColorStateList.valueOf(Color.parseColor(currentItems.getTintColor())));

            viewHolder.text_TextColor.setTextColor(
                    (currentItems.isDarkTheme()) ?
                            ColorStateList.valueOf(Color.WHITE) :
                            ColorStateList.valueOf(Color.BLACK)
            );
        }
        return convertView;
    }
}