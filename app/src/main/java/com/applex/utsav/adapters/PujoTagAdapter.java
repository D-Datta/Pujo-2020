package com.applex.utsav.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applex.utsav.R;
import com.applex.utsav.models.PujoTagModel;
import com.applex.utsav.models.TagModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PujoTagAdapter extends RecyclerView.Adapter<PujoTagAdapter.ProgrammingViewHolder> {

    private ArrayList<PujoTagModel> mList;
    Context mcontext;

    private OnClickListener mListener;

    public interface OnClickListener { void onClickListener(int position, String pujo, String uid, String dp);}

    public void onClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public PujoTagAdapter() {
    }

    public PujoTagAdapter(ArrayList<PujoTagModel> list, Context context) {
        this.mList = list;
        this.mcontext=context;
    }

    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_tag_pujo,viewGroup, false);
        return new ProgrammingViewHolder(v , mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProgrammingViewHolder programmingViewHolder, int i) {
        PujoTagModel currentItem = mList.get(i);

        programmingViewHolder.tag.setText(currentItem.getPujoName());

        if(currentItem.getDp()!=null){
//
            Picasso.get().load(currentItem.getDp()).placeholder(R.drawable.image_background_grey).into(programmingViewHolder.dp);
        }
        else{
            WindowManager manager = (WindowManager)mcontext.getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.durga_ma, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                int widthRatio = Math.round((float) width / (float) displayWidth);
                options.inSampleSize = widthRatio;
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.durga_ma, options);
            programmingViewHolder.dp.setImageBitmap(scaledBitmap);
        }


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        TextView tag;
        ImageView dp;

        private ProgrammingViewHolder(@NonNull View itemView, OnClickListener listener) {
            super(itemView);
            tag = itemView.findViewById(R.id.tag);
            dp = itemView.findViewById(R.id.dp);

            tag.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ){
                            listener.onClickListener(position, mList.get(position).getPujoName(),
                                    mList.get(position).getPujoUid(), mList.get(position).getDp());
                    }
                }
            });
        }
    }
}