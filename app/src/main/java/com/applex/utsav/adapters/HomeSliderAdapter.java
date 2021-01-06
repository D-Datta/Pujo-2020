package com.applex.utsav.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

import com.applex.utsav.R;
import com.applex.utsav.Webview;
import com.applex.utsav.models.SliderModel;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class HomeSliderAdapter extends SliderViewAdapter<HomeSliderAdapter.SliderAdapterVH> {

    private Context mContext;
    private ArrayList<SliderModel> itemDatalist;
    private int bool;

    public HomeSliderAdapter(Context context, ArrayList<SliderModel> itemDatalist, int bool) {
        this.mContext = context;
        this.itemDatalist = itemDatalist;
        this.bool = bool;// 2 = global ; 3 = campus
    }
    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        @SuppressLint("InflateParams")
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_slider_layout, null);
        return new SliderAdapterVH(inflate);
    }
    @Override
    public void onBindViewHolder(HomeSliderAdapter.SliderAdapterVH viewHolder, int position) {
        SliderModel currentItem = itemDatalist.get(position);

        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.downloader(new OkHttp3Downloader(mContext, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.get().load(currentItem.getEventImage()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.image_background_grey).into(viewHolder.imageViewBackground, new Callback() {
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
                Picasso.get().load(currentItem.getEventImage()).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.imageViewBackground, new Callback() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(currentItem.getEventImage()).into(viewHolder.imageViewBackground);
                    }
                });
                Picasso.get().load(currentItem.getEventImage()).into(viewHolder.imageViewBackground);
            }
        });

        viewHolder.imageViewBackground.setOnClickListener(v -> {
            switch (currentItem.getOption()){
                case 1:
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_SEND);
                    String text="Hey! Join me at Utsav App: Virtual Festivals 2020 and experience the world of Celebration. \nDownload now: https://play.google.com/store/apps/details?id=com.applex.utsav";
                    i.putExtra(Intent.EXTRA_TEXT,text);
                    i.setType("text/plain");
                    mContext.startActivity(Intent.createChooser(i,"Share with"));

                    break;
                case 2:
                    Intent intent = new Intent(mContext, Webview.class);
                    intent.putExtra("option", Integer.toString(currentItem.getOption()));
                    intent.putExtra("Link", currentItem.getEventLink());
                    mContext.startActivity(intent);
                    break;
                case 3:
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(currentItem.getEventLink())));
                    break;
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

        SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            this.itemView = itemView;
        }
    }
}
