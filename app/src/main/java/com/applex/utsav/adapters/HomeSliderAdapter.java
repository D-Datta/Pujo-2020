package com.applex.utsav.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.applex.utsav.ActivityProfile;
import com.applex.utsav.MainActivity;
import com.applex.utsav.R;
import com.applex.utsav.ReelsActivity;
import com.applex.utsav.ViewMoreHome;
import com.applex.utsav.ViewMoreText;
import com.applex.utsav.Webview;
import com.applex.utsav.models.SliderModel;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class HomeSliderAdapter extends SliderViewAdapter<HomeSliderAdapter.SliderAdapterVH> {

    private final Context mContext;
    private final ArrayList<SliderModel> itemDatalist;
    private final int bool;

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
            public void onSuccess() { }

            @Override
            public void onError(Exception e) {
                Picasso.Builder builder = new Picasso.Builder(mContext);
                builder.downloader(new OkHttp3Downloader(mContext, Integer.MAX_VALUE));
                Picasso built = builder.build();
                built.setIndicatorsEnabled(true);
                built.setLoggingEnabled(true);
                Picasso.get().load(currentItem.getEventImage()).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.imageViewBackground, new Callback() {
                    @Override
                    public void onSuccess() { }

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
                case 4:
                    Uri uri = Uri.parse(currentItem.getEventLink());
                    if(uri!=null) {
                        List<String> params = uri.getPathSegments();
                        if(params.size()>=3){
                            String postID = params.get(3);
                            if(params.get(1).matches("feeds")) {
                                if(params.get(2).matches("0")) {
                                    Intent in = new Intent(mContext, ViewMoreText.class);
                                    in.putExtra("campus", "Text");
                                    in.putExtra("postID", postID);
                                    in.putExtra("from", "link");
                                    mContext.startActivity(in);
                                }
                                else if(params.get(2).matches("1")) {
                                    Intent in = new Intent(mContext, ViewMoreHome.class);
                                    in.putExtra("campus", "Image");
                                    in.putExtra("postID", postID);
                                    in.putExtra("from", "link");
                                    mContext.startActivity(in);
                                }
                                else {
                                    mContext.startActivity(new Intent(mContext, MainActivity.class));
                                }
                            }
                            else if(params.get(1).matches("clips")) {
                                if (params.get(2).matches("1")) {
                                    Intent in = new Intent(mContext, ReelsActivity.class);
                                    in.putExtra("bool", "1");
                                    in.putExtra("docID", postID);
                                    mContext.startActivity(in);
                                } else if (params.get(2).matches("2")) {
                                    Intent in = new Intent(mContext, ReelsActivity.class);
                                    in.putExtra("bool", "2");
                                    in.putExtra("docID", postID);
                                    mContext.startActivity(in);
                                } else if (params.get(2).matches("3")) {
                                    Intent in = new Intent(mContext, ReelsActivity.class);
                                    in.putExtra("bool", "3");
                                    in.putExtra("docID", postID);
                                    mContext.startActivity(in);
                                }
                            }
                        }
                        else {
                            mContext.startActivity(new Intent(mContext, MainActivity.class));
                        }
                    }
                    else {
                        mContext.startActivity(new Intent(mContext, MainActivity.class));
                    }
                    break;
                case 5:
                    Uri uri2 = Uri.parse(currentItem.getEventLink());
                    if(uri2!=null) {
                        List<String> params = uri2.getPathSegments();
                        if (params.get(1).matches("profile")) {
                            Intent in = new Intent(mContext, ActivityProfile.class);
                            in.putExtra("uid", params.get(3));
                            mContext.startActivity(in);
                        }
                    }
                    else {
                        mContext.startActivity(new Intent(mContext, MainActivity.class));
                    }
                    break;
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
