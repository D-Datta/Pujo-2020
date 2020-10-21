package com.applex.utsav.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.applex.utsav.R;

import java.util.ArrayList;
import java.util.List;

public class UserTaggingAdapter extends ArrayAdapter<String> {
    private final List<String> mItems;
    private final Context mContext;
    private final LayoutInflater inflater;

    public UserTaggingAdapter(Context context, int resourceId, ArrayList<String> items) {
        super(context, resourceId, items);
        mContext = context;
        mItems = items;
        inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_tag_user, null);
            holder = new ViewHolder();
            holder.text = convertView.findViewById(R.id.username);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        String item = getItem(position);
        if (item != null) {
            holder.text.setText(item);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public String getItem(int position) {
        return mItems.get(position);
    }

    public class ViewHolder {
        TextView text;
    }
}
