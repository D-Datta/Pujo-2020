package com.applex.utsav.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.applex.utsav.R;
import com.applex.utsav.ViewMoreHome;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.utility.StoreTemp;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH> {

    private Context mContext;
    private ArrayList<String> itemDatalist;
    private int bool;
    private HomePostModel model;

    public SliderAdapter(Context context, ArrayList<String> itemDatalist, HomePostModel model) {
        this.mContext = context;
        this.itemDatalist = itemDatalist;
        this.model = model;
        this.bool = bool;// 2 = global ; 3 = campus
    }
    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        @SuppressLint("InflateParams")
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_slider_layout, null);
        return new SliderAdapterVH(inflate);
    }
    @Override
    public void onBindViewHolder(final SliderAdapter.SliderAdapterVH viewHolder, int position) {
        final String currentItem = itemDatalist.get(position);

        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.downloader(new OkHttp3Downloader(mContext, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.get().load(currentItem).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.image_background_grey).into(viewHolder.imageViewBackground, new Callback() {
            @Override
            public void onSuccess() {
                Log.d("HELLO","2");

            }

            @Override
            public void onError(Exception e) {
                Log.d("HELLO","1");

                Picasso.Builder builder = new Picasso.Builder(mContext);
                builder.downloader(new OkHttp3Downloader(mContext, Integer.MAX_VALUE));
                Picasso built = builder.build();
                built.setIndicatorsEnabled(true);
                built.setLoggingEnabled(true);
                Picasso.get().load(currentItem).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.imageViewBackground, new Callback() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(currentItem).into(viewHolder.imageViewBackground);
                    }
                });
                Picasso.get().load(currentItem).into(viewHolder.imageViewBackground);
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewMoreHome.class);
                intent.putExtra("username", model.getUsN());
                intent.putExtra("userdp", model.getDp());
                intent.putExtra("docID", model.getDocID());
                StoreTemp.getInstance().setTagTemp(model.getTagL());
                intent.putExtra("comName", model.getComName());
                intent.putExtra("comID", model.getComID());
                intent.putExtra("likeL", model.getLikeL());
                if(model.getImg() != null && model.getImg().size()>0) {
                    Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST", (Serializable)model.getImg());
                    intent.putExtra("BUNDLE", args);
                }
                intent.putExtra("postText", model.getTxt());
                intent.putExtra("bool", "3");
                intent.putExtra("commentNo", Long.toString(model.getCmtNo()));
                intent.putExtra("newTs", Long.toString(model.getNewTs()));
                intent.putExtra("uid", model.getUid());
                intent.putExtra("timestamp", Long.toString(model.getTs()));
                intent.putExtra("type", model.getType());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getCount() {
        return itemDatalist.size();
    }

    public static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;
        // TextView textViewDescription;

        SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            this.itemView = itemView;
        }
    }
}
