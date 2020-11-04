package com.applex.utsav.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.applex.utsav.R;
import com.applex.utsav.utility.ItemMoveCallback;

import java.util.ArrayList;
import java.util.Collections;

public class MultipleImageAdapter extends RecyclerView.Adapter<MultipleImageAdapter.ProgrammingViewHolder> implements ItemMoveCallback.ItemTouchHelperContract
{
    private ArrayList<byte[]> mList;

    Context mcontext;

    private OnClickListener mListener;

    public interface OnClickListener {
        void onClickListener(int position);
        void onCropClickListener(int position);

    }

    public void onClickListener(OnClickListener listener) {
        mListener = listener;
    }


    public MultipleImageAdapter() {
    }

    public MultipleImageAdapter(ArrayList<byte[]> list, Context context) {
        this.mList = list;
        this.mcontext=context;
    }



    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_multiple_image,viewGroup, false);
        return new ProgrammingViewHolder(v , mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProgrammingViewHolder programmingViewHolder, int i) {
        byte[] currentItem = mList.get(i);

        if (currentItem != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(currentItem, 0 ,currentItem.length);
            programmingViewHolder.image.setImageBitmap(bitmap);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(ProgrammingViewHolder myViewHolder) {
        myViewHolder.itemView.setAlpha(0.5f);
    }

    @Override
    public void onRowClear(ProgrammingViewHolder myViewHolder) {
        myViewHolder.itemView.setAlpha(1.0f);
    }

    public class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        ImageButton unselect;
        ImageButton crop;

        private ProgrammingViewHolder(@NonNull View itemView, OnClickListener listener) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            unselect = itemView.findViewById(R.id.unselect);
            unselect = itemView.findViewById(R.id.crop);

            unselect.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ){
                        listener.onClickListener(position);
                    }
                }
            });

            crop.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ){
                        listener.onCropClickListener(position);
                    }
                }
            });
        }
    }
}
