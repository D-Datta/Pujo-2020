package com.example.pujo360.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pujo360.DisplayPictureActivity;
import com.example.pujo360.R;
import com.example.pujo360.ViewMoreHome;
import com.example.pujo360.util.Utility;
import com.github.chrisbanes.photoview.PhotoView;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class ViewmoreSliderAdapter extends SliderViewAdapter<ViewmoreSliderAdapter.ViewmoreSliderAdapterVH> {

    private Context mContext;
    private ArrayList<String> itemDatalist;
    private int bool;

    public ViewmoreSliderAdapter(Context context, ArrayList<String> itemDatalist) {
        this.mContext = context;
        this.itemDatalist = itemDatalist;
        this.bool = bool;// 2 = global ; 3 = campus
    }
    @Override
    public ViewmoreSliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_viewmore_slider, null);
        return new ViewmoreSliderAdapterVH(inflate);
    }
    @Override
    public void onBindViewHolder(final ViewmoreSliderAdapterVH viewHolder, int position) {
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

        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Picasso.get().load(currentItem).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        save_Dialog(bitmap);
                    }
                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Toast.makeText(mContext, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }

                });
                return true;
            }
        });
    }

    private void save_Dialog(Bitmap bitmap) {
        Dialog myDialogue;
        myDialogue = new Dialog(mContext);
        myDialogue.setContentView(R.layout.dialog_image_options);
        myDialogue.setCanceledOnTouchOutside(TRUE);
        myDialogue.findViewById(R.id.saveToInternal).setOnClickListener(v -> {
            if(!Utility.checkStoragePermission(mContext)){
                Utility.requestStoragePermission(mContext);
            }
            else {
                Boolean bool = Utility.saveImage(bitmap, mContext);

                if(bool){
                    Toast.makeText(mContext, "Saved to device", Toast.LENGTH_SHORT).show();
                    myDialogue.dismiss();
                }
                else{
                    Toast.makeText(mContext, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    myDialogue.dismiss();
                }

            }
        });
        myDialogue.show();
        Objects.requireNonNull(myDialogue.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public int getCount() {
        return itemDatalist.size();
    }

    public static class ViewmoreSliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        PhotoView imageViewBackground;
        // TextView textViewDescription;

        ViewmoreSliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            this.itemView = itemView;
        }
    }
}
