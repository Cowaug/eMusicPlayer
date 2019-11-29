package project.exos.emusicplayer.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import project.exos.emusicplayer.Entity.DataRepository;
import project.exos.emusicplayer.R;

public class SpinnerAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private ArrayList<String> stringList = new ArrayList<>();

    public SpinnerAdapter(@NonNull Context context, ArrayList<String> list) {
        super(context, 0, list);
        mContext = context;
        stringList = list;
    }

    private static class ViewHolder {
        private TextView text;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.spinner_shown, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.text = convertView.findViewById(R.id.spinnerTextShown);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (stringList != null) {
            viewHolder.text.setText(stringList.get(position));
        }
        return convertView;
    }

    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.spinner_drop, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.text = convertView.findViewById(R.id.spinnerText);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (stringList != null) {
            viewHolder.text.setText(stringList.get(position));
        }
        return convertView;
    }
}
