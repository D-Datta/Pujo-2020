package com.example.pujo360.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.pujo360.R;
import com.example.pujo360.models.TagModel;

import java.util.ArrayList;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ProgrammingViewHolder> {


    private ArrayList<TagModel> mList;
    Context mcontext;

    private TagAdapter.OnClickListener mListener;
    private TagAdapter.OnLongClickListener Listener;

    public interface OnClickListener {
        void onClickListener(int position, String tag, String color);
    }

    public void onClickListener(TagAdapter.OnClickListener listener) {
        mListener = listener;
    }

    public interface OnLongClickListener {
        void onLongClickListener(int position, String tag, String color);
    }

    public void onLongClickListener(TagAdapter.OnLongClickListener onLongClickListener) {
        Listener= onLongClickListener;
    }


    public TagAdapter() {
    }

    public TagAdapter(ArrayList<TagModel> list, Context context) {
        this.mList = list;
        this.mcontext=context;
    }

    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_tag,viewGroup, false);
        return new ProgrammingViewHolder(v , mListener, Listener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProgrammingViewHolder programmingViewHolder, int i) {
        TagModel currentItem = mList.get(i);

            programmingViewHolder.tag.setText(currentItem.getName_tag());
            programmingViewHolder.tag.setTextColor(Color.parseColor("#000000"));
            programmingViewHolder.tag.setBackgroundColor(Color.parseColor(currentItem.getColor_hex()));

      //  final TagModel currentItem = mList.get(i);

//        programmingViewHolder.tag.setOnClickListener(v -> {
//            if(i>0){
//
//                NewPostActivity.selected_tags.add(mList.get(i));
//                // Utility.showToast(mcontext, "added");
//            }
//
//
//        });
//        programmingViewHolder.tag.setText(currentItem.getName_tag());
//        programmingViewHolder.tag.setBackgroundColor(Color.parseColor(currentItem.getColor_hex()));

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        TextView tag;

        private ProgrammingViewHolder(@NonNull View itemView, OnClickListener listener, OnLongClickListener onLongClickListener) {
            super(itemView);
            tag = itemView.findViewById(R.id.tag);

            tag.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ){
                            listener.onClickListener(position, mList.get(position).getName_tag() , mList.get(position).getColor_hex());
                    }
                }
            });

            tag.setOnLongClickListener(v -> {
                if(onLongClickListener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ){
                        onLongClickListener.onLongClickListener(position, mList.get(position).getName_tag() , mList.get(position).getColor_hex());
                    }
                }
                return true;
            });


        }
    }



}


