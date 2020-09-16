package com.example.pujo360.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.pujo360.R;
import com.example.pujo360.models.SliderModel;
import com.example.pujo360.util.StoreTemp;
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
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_slider_layout, null);
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
            Intent intent = new Intent(mContext, ViewMoreSlider.class);
            intent.putExtra("docID", currentItem.getDocID());
            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
            intent.putExtra("likeL", currentItem.getLikeL());

            intent.putExtra("themeColor", currentItem.getThemecolor());
            intent.putExtra("eventPic", currentItem.getEventImage());
            intent.putExtra("eventText", currentItem.getEventDetails());
            intent.putExtra("eventName", currentItem.getEventName());
            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
            intent.putExtra("participate", Boolean.toString(currentItem.isParticipate()));
            intent.putExtra("bool", Integer.toString(bool));

            intent.putExtra("uid", currentItem.getUid());
            mContext.startActivity(intent);

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
