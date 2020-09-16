package com.example.pujo360.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.example.pujo360.DisplayPictureActivity;
import com.example.pujo360.R;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH> {

    private Context mContext;
    private ArrayList<String> itemDatalist;
    private int bool;

    public SliderAdapter(Context context, ArrayList<String> itemDatalist) {
        this.mContext = context;
        this.itemDatalist = itemDatalist;
        this.bool = bool;// 2 = global ; 3 = campus
    }
    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
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

            }

            @Override
            public void onError(Exception e) {
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
                Intent intent = new Intent(mContext, DisplayPictureActivity.class);
                intent.putExtra("from", "assignment");
                intent.putExtra("Bitmap", currentItem);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
