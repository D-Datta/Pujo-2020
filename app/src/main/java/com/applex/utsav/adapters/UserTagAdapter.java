package com.applex.utsav.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applex.utsav.R;
import com.applex.utsav.models.UserTagModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserTagAdapter extends ArrayAdapter<UserTagModel> {
    private final Context mContext;

    public UserTagAdapter(Context context, int resourceId, ArrayList<UserTagModel> items) {
        super(context, resourceId, R.id.username, items);
        mContext = context;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        Log.i("BAM", "6");

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_tag_user, parent, false);
            holder = new ViewHolder();
            holder.username = convertView.findViewById(R.id.username);
            holder.userdp = convertView.findViewById(R.id.userdp);
            convertView.setTag(holder);
            Log.i("BAM", "5");
        }
        else {
            holder = (ViewHolder) convertView.getTag();
            Log.i("BAM", "4");
        }

        UserTagModel currentItem = getItem(position);
        if (currentItem != null) {
            Log.i("BAM", "1");
            holder.username.setText(currentItem.getName());
            if (currentItem.getDp() != null && !currentItem.getDp().isEmpty()) {
                Picasso.get().load(currentItem.getDp()).fit().centerCrop()
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .into(holder.userdp, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                if (currentItem.getGender() != null) {
                                    if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")) {
                                        holder.userdp.setImageResource(R.drawable.ic_female);
                                    } else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")) {
                                        holder.userdp.setImageResource(R.drawable.ic_male);
                                    } else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")) {
                                        holder.userdp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                    }
                                } else {
                                    holder.userdp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            }
                        });
            } else {
                if (currentItem.getGender() != null) {
                    if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")) {
                        holder.userdp.setImageResource(R.drawable.ic_female);
                    } else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")) {
                        holder.userdp.setImageResource(R.drawable.ic_male);
                    } else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")) {
                        holder.userdp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
                } else {
                    holder.userdp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            }
        } else {
            Log.i("BAM", "3");

        }

        return convertView;
    }

    @Override
    public int getCount() {
        return 5;
    }

    public static class ViewHolder {
        TextView username;
        ImageView userdp;
    }
}
